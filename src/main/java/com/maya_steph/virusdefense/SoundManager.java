package com.maya_steph.virusdefense;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages sound effects and background music for the game
 */
public class SoundManager {
    private static final int SAMPLE_RATE = 44100;
    private static final int SAMPLE_SIZE = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    private static final int MAX_CONCURRENT_SOUNDS = 3; // Limit concurrent sounds
    
    private Clip backgroundMusic;
    private boolean musicEnabled = false; // Disabled - background music removed
    private boolean soundEnabled = true;
    private Map<String, byte[]> soundCache;
    private Map<String, java.util.Queue<Clip>> clipPools; // Pools of pre-opened clips for immediate playback
    private ExecutorService soundExecutor;
    private AtomicInteger activeSoundCount = new AtomicInteger(0);
    private long lastMoveSoundTime = 0;
    private static final long MOVE_SOUND_THROTTLE_MS = 100; // Throttle move sounds
    private static final int CLIP_POOL_SIZE = 5; // Number of pre-opened clips per sound
    
    public SoundManager() {
        soundCache = new HashMap<>();
        clipPools = new HashMap<>();
        // Use cached thread pool for better responsiveness - creates threads as needed
        soundExecutor = Executors.newCachedThreadPool();
        initializeSounds();
        preOpenCriticalSounds(); // Pre-open frequently used sounds for immediate playback
    }
    
    private void initializeSounds() {
        // Pre-generate sound effects - optimized for immediate response
        soundCache.put("move", generateTone(400, 30, 0.25)); // Shorter, quieter move sound
        soundCache.put("shoot", generateTone(700, 40, 0.5)); // Quick, sharp shoot sound for immediate feedback
        soundCache.put("weapon_switch", generateTone(550, 50, 0.35)); // Quick switch sound
        soundCache.put("effective_hit", generateTone(850, 80, 0.4)); // Satisfying hit sound
        soundCache.put("virus_destroyed", generateToneSequence(new int[]{600, 800, 1000}, new int[]{60, 60, 100}, 0.4)); // Victory sound
        soundCache.put("ineffective_hit", generateTone(250, 120, 0.25)); // Low thud for ineffective
        soundCache.put("life_lost", generateTone(180, 180, 0.5)); // Warning sound
        soundCache.put("round_complete", generateToneSequence(new int[]{400, 600, 800, 1000}, new int[]{80, 80, 80, 150}, 0.5)); // Success fanfare
    }
    
    /**
     * Pre-open clips for critical sounds that need immediate playback
     */
    private void preOpenCriticalSounds() {
        String[] criticalSounds = {"shoot", "move", "weapon_switch"};
        AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNELS, SIGNED, BIG_ENDIAN);
        
        for (String soundName : criticalSounds) {
            byte[] audioData = soundCache.get(soundName);
            if (audioData != null) {
                java.util.Queue<Clip> clipPool = new java.util.concurrent.ConcurrentLinkedQueue<>();
                int successfulClips = 0;
                for (int i = 0; i < CLIP_POOL_SIZE; i++) {
                    try {
                        // Create a fresh copy of audio data for each clip
                        ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                        AudioInputStream audioInputStream = new AudioInputStream(bais, format, audioData.length / format.getFrameSize());
                        Clip clip = AudioSystem.getClip();
                        clip.open(audioInputStream);
                        clipPool.offer(clip);
                        successfulClips++;
                    } catch (Exception e) {
                        // If pre-opening fails, will fall back to on-demand creation
                        System.err.println("Could not pre-open clip " + i + " for " + soundName + ": " + e.getMessage());
                    }
                }
                if (successfulClips > 0) {
                    clipPools.put(soundName, clipPool);
                }
            }
        }
    }
    
    /**
     * Generate a simple tone
     */
    private byte[] generateTone(int frequency, int durationMs, double volume) {
        int samples = (int) (SAMPLE_RATE * durationMs / 1000.0);
        byte[] audioData = new byte[samples * 2];
        
        for (int i = 0; i < samples; i++) {
            double angle = 2.0 * Math.PI * i * frequency / SAMPLE_RATE;
            short sample = (short) (Math.sin(angle) * Short.MAX_VALUE * volume);
            audioData[i * 2] = (byte) (sample & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        
        return audioData;
    }
    
    /**
     * Generate a sequence of tones
     */
    private byte[] generateToneSequence(int[] frequencies, int[] durations, double volume) {
        int totalSamples = 0;
        for (int duration : durations) {
            totalSamples += (int) (SAMPLE_RATE * duration / 1000.0);
        }
        
        byte[] audioData = new byte[totalSamples * 2];
        int offset = 0;
        
        for (int tone = 0; tone < frequencies.length; tone++) {
            int samples = (int) (SAMPLE_RATE * durations[tone] / 1000.0);
            for (int i = 0; i < samples; i++) {
                double angle = 2.0 * Math.PI * (offset + i) * frequencies[tone] / SAMPLE_RATE;
                short sample = (short) (Math.sin(angle) * Short.MAX_VALUE * volume);
                audioData[(offset + i) * 2] = (byte) (sample & 0xFF);
                audioData[(offset + i) * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
            }
            offset += samples;
        }
        
        return audioData;
    }
    
    /**
     * Play a sound effect (non-blocking, thread-safe, immediate response)
     */
    public void playSound(String soundName) {
        if (!soundEnabled) return;
        
        // Throttle move sounds to prevent rapid-fire sounds
        if ("move".equals(soundName)) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMoveSoundTime < MOVE_SOUND_THROTTLE_MS) {
                return; // Skip if too soon since last move sound
            }
            lastMoveSoundTime = currentTime;
        }
        
        // For critical sounds (shoot), always allow playback - no limit
        boolean isCritical = "shoot".equals(soundName);
        if (!isCritical && activeSoundCount.get() >= MAX_CONCURRENT_SOUNDS) {
            return; // Skip if too many sounds playing (except for shoot)
        }
        
        // Check if we have a pre-opened clip pool for immediate playback
        java.util.Queue<Clip> clipPool = clipPools.get(soundName);
        if (clipPool != null) {
            Clip availableClip = clipPool.poll();
            if (availableClip != null) {
                // Start clip immediately - minimal checks for zero delay
                try {
                    // Quick reset and start - no state checks that add delay
                    availableClip.setFramePosition(0);
                    availableClip.start(); // Non-blocking - starts immediately
                    activeSoundCount.incrementAndGet();
                    
                    // Handle cleanup in background thread (don't block)
                    final Clip clipToCleanup = availableClip;
                    final java.util.Queue<Clip> poolToReturn = clipPool;
                    soundExecutor.submit(() -> {
                        try {
                            // Wait for clip to finish
                            long duration = clipToCleanup.getMicrosecondLength() / 1000;
                            if (duration > 0 && duration < 2000) {
                                Thread.sleep(duration);
                            } else {
                                Thread.sleep(500);
                            }
                        } catch (Exception e) {
                            // Silently handle errors
                        } finally {
                            // Return clip to pool for reuse
                            try {
                                if (clipToCleanup.isOpen()) {
                                    if (clipToCleanup.isRunning()) {
                                        clipToCleanup.stop();
                                    }
                                    clipToCleanup.setFramePosition(0);
                                }
                                poolToReturn.offer(clipToCleanup);
                            } catch (Exception e) {
                                // If clip is broken, don't return it to pool
                            }
                            activeSoundCount.decrementAndGet();
                        }
                    });
                } catch (Exception e) {
                    // If start fails, return clip to pool
                    try {
                        clipPool.offer(availableClip);
                    } catch (Exception ignored) {}
                    activeSoundCount.decrementAndGet();
                }
                return;
            }
        }
        
        // Fallback: create clip on-demand for non-critical sounds
        byte[] audioData = soundCache.get(soundName);
        if (audioData == null) {
            return; // Silently fail if sound not found
        }
        
        // Use thread pool for playback
        soundExecutor.submit(() -> {
            Clip clip = null;
            try {
                activeSoundCount.incrementAndGet();
                
                AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNELS, SIGNED, BIG_ENDIAN);
                ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                AudioInputStream audioInputStream = new AudioInputStream(bais, format, audioData.length / format.getFrameSize());
                
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start(); // Start immediately
                
                // Wait for clip to finish
                long duration = clip.getMicrosecondLength() / 1000;
                if (duration > 0 && duration < 2000) {
                    Thread.sleep(duration);
                } else {
                    Thread.sleep(500);
                }
                
            } catch (Exception e) {
                // Silently handle errors
            } finally {
                if (clip != null) {
                    try {
                        if (clip.isOpen()) {
                            clip.stop();
                            clip.close();
                        }
                    } catch (Exception ignored) {}
                }
                activeSoundCount.decrementAndGet();
            }
        });
    }
    
    /**
     * Start playing background music (looping)
     */
    public void startBackgroundMusic() {
        if (!musicEnabled) return;
        
        stopBackgroundMusic(); // Stop any existing music
        
        try {
            // Generate a simple ambient background music (low frequency, slow melody)
            byte[] musicData = generateBackgroundMusic();
            AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNELS, SIGNED, BIG_ENDIAN);
            ByteArrayInputStream bais = new ByteArrayInputStream(musicData);
            AudioInputStream audioInputStream = new AudioInputStream(bais, format, musicData.length / format.getFrameSize());
            
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioInputStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundMusic.start();
            
        } catch (LineUnavailableException | IOException e) {
            System.err.println("Error starting background music: " + e.getMessage());
        }
    }
    
    /**
     * Generate background music - tense, urgent theme for defending the heart
     */
    private byte[] generateBackgroundMusic() {
        // Create an 8-second loop of tense, urgent music
        int loopDurationMs = 8000;
        int samples = (int) (SAMPLE_RATE * loopDurationMs / 1000.0);
        byte[] audioData = new byte[samples * 2];
        
        for (int i = 0; i < samples; i++) {
            double t = (double) i / SAMPLE_RATE;
            
            // Create a tense, pulsing rhythm - like a heartbeat under pressure
            // Base tone - low, urgent (like a heartbeat)
            double heartbeat = Math.sin(2.0 * Math.PI * 60 * t) * 0.08; // Very low, like heartbeat
            
            // Pulsing rhythm - creates urgency
            double pulseFreq = 2.0; // 2 pulses per second
            double pulse = 1.0 + 0.15 * Math.sin(2.0 * Math.PI * pulseFreq * t);
            
            // Tense harmonic layer - minor key feeling
            double tense1 = Math.sin(2.0 * Math.PI * 220 * t) * 0.10 * pulse; // A3
            double tense2 = Math.sin(2.0 * Math.PI * 262 * t) * 0.10 * pulse; // C4 (minor)
            double tense3 = Math.sin(2.0 * Math.PI * 311 * t) * 0.08 * pulse; // D#4 (minor)
            
            // Higher frequency layer for urgency (but subtle)
            double urgency = Math.sin(2.0 * Math.PI * 440 * t) * 0.06 * (0.5 + 0.5 * Math.sin(2.0 * Math.PI * 0.25 * t));
            
            // Combine all layers
            double combined = heartbeat + tense1 + tense2 + tense3 + urgency;
            
            // Apply envelope to avoid clicks at loop boundaries
            double envelope = 1.0;
            double fadeTime = 0.2; // 200ms fade
            if (i < SAMPLE_RATE * fadeTime) {
                envelope = i / (SAMPLE_RATE * fadeTime); // Fade in
            } else if (i > samples - SAMPLE_RATE * fadeTime) {
                envelope = (samples - i) / (SAMPLE_RATE * fadeTime); // Fade out
            }
            
            short sample = (short) (combined * Short.MAX_VALUE * envelope);
            audioData[i * 2] = (byte) (sample & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        
        return audioData;
    }
    
    /**
     * Stop background music
     */
    public void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            backgroundMusic.close();
            backgroundMusic = null;
        }
    }
    
    /**
     * Pause background music (can be resumed)
     */
    public void pauseBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }
    
    /**
     * Resume background music if it was paused
     */
    public void resumeBackgroundMusic() {
        if (backgroundMusic != null && !backgroundMusic.isRunning() && musicEnabled) {
            backgroundMusic.start();
        } else if (backgroundMusic == null && musicEnabled) {
            // Music was stopped, restart it
            startBackgroundMusic();
        }
    }
    
    /**
     * Set music enabled/disabled
     */
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            stopBackgroundMusic();
        } else if (backgroundMusic == null) {
            startBackgroundMusic();
        }
    }
    
    /**
     * Set sound effects enabled/disabled
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        stopBackgroundMusic();
        
        // Close pre-opened clips in pools
        for (java.util.Queue<Clip> clipPool : clipPools.values()) {
            Clip clip;
            while ((clip = clipPool.poll()) != null) {
                try {
                    if (clip.isOpen()) {
                        clip.stop();
                        clip.close();
                    }
                } catch (Exception ignored) {}
            }
        }
        clipPools.clear();
        
        if (soundExecutor != null) {
            soundExecutor.shutdown();
        }
        soundCache.clear();
    }
}

