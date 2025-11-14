package com.maya_steph.virusdefense;

import java.awt.*;

/**
 * Manages screen overlay effects like darkening when player loses a life
 */
public class OverlayEffect {
    private float darknessLevel;
    private final float targetDarkness;
    private final float fadeSpeed;
    private boolean fading;
    private boolean isDarkening;
    
    // New round flash effect
    private float flashLevel;
    private Color flashColor;
    private boolean flashActive;
    private int flashFramesRemaining;
    
    // Moving background effect (forward movement illusion)
    private boolean movingBackgroundActive;
    private float backgroundScrollOffset;
    private static final float BACKGROUND_SCROLL_SPEED = 3.0f; // Speed of scrolling lines
    
    // Life loss shake and red overlay effect
    private boolean shakeActive;
    private int shakeFramesRemaining;
    private float shakeOffsetX;
    private float shakeOffsetY;
    private boolean redOverlayActive;
    private int redOverlayFramesRemaining;
    private static final int SHAKE_DURATION = 60; // 1 second at 60fps
    private static final float SHAKE_INTENSITY = 10.0f; // Maximum shake distance in pixels

    public OverlayEffect() {
        darknessLevel = 0.0f;
        targetDarkness = 0.6f; //target darkness when triggered
        fadeSpeed = 0.05f;
        isDarkening = false;
        fading = false;
        
        // Initialize flash effect
        flashLevel = 0.0f;
        flashColor = Color.GREEN;
        flashActive = false;
        flashFramesRemaining = 0;
        
        // Initialize moving background effect
        movingBackgroundActive = false;
        backgroundScrollOffset = 0.0f;
        
        // Initialize shake and red overlay effect
        shakeActive = false;
        shakeFramesRemaining = 0;
        shakeOffsetX = 0.0f;
        shakeOffsetY = 0.0f;
        redOverlayActive = false;
        redOverlayFramesRemaining = 0;
    }
    public void darkerAfterLoss(){
        darknessLevel += 0.2f;
    }
    
    public void triggerDarken() {
        isDarkening = true;
        fading = false;
    }
    
    public void triggerLifeLossFlash() {
        flashActive = true;
        flashLevel = 0.7f; // Strong flash
        flashColor = Color.RED; // Red for life loss
        flashFramesRemaining = 15; // Flash for about 1/4 second at 60fps
    }
    
    public void triggerLifeLossShakeAndRedOverlay() {
        // Trigger shake effect
        shakeActive = true;
        shakeFramesRemaining = SHAKE_DURATION;
        shakeOffsetX = 0.0f;
        shakeOffsetY = 0.0f;
        
        // Trigger red overlay for 1 second
        redOverlayActive = true;
        redOverlayFramesRemaining = SHAKE_DURATION; // 60 frames = 1 second at 60fps
    }
    
    public void triggerNewRoundFlash() {
        flashActive = true;
        flashLevel = 0.8f; // Bright flash
        flashColor = Color.GREEN; // Green for positive/new round
        flashFramesRemaining = 20; // Flash for about 1/3 second at 60fps
    }
    
    public void startMovingBackground() {
        movingBackgroundActive = true;
        backgroundScrollOffset = 0.0f;
    }
    
    public void stopMovingBackground() {
        movingBackgroundActive = false;
    }

    public void update() {
        // Update darkness effect (life loss)
        if (isDarkening) {
            darknessLevel = Math.min(darknessLevel + fadeSpeed, targetDarkness);
            if (darknessLevel >= targetDarkness) {
                isDarkening = false;
                fading = true;
            }
        } else if (fading) {
            darknessLevel = Math.max(darknessLevel - fadeSpeed, 0.0f);
            if (darknessLevel <= 0.0f) {
                fading = false;
            }
        }
        
        // Update flash effect (new round)
        if (flashActive) {
            flashFramesRemaining--;
            if (flashFramesRemaining <= 0) {
                flashActive = false;
                flashLevel = 0.0f;
            } else {
                // Fade out flash over remaining frames
                flashLevel = (float) flashFramesRemaining / 20.0f * 0.6f; // Max 0.6 alpha
            }
        }
        
        // Update moving background effect (forward movement)
        if (movingBackgroundActive) {
            backgroundScrollOffset += BACKGROUND_SCROLL_SPEED;
            // Reset offset when it gets too large to prevent overflow
            if (backgroundScrollOffset > 40) {
                backgroundScrollOffset = 0.0f;
            }
        }
        
        // Update shake effect
        if (shakeActive) {
            shakeFramesRemaining--;
            if (shakeFramesRemaining <= 0) {
                shakeActive = false;
                shakeOffsetX = 0.0f;
                shakeOffsetY = 0.0f;
            } else {
                // Create shake effect with random offsets that decrease over time
                float intensity = SHAKE_INTENSITY * (shakeFramesRemaining / (float)SHAKE_DURATION);
                shakeOffsetX = (float)(Math.random() * intensity * 2 - intensity);
                shakeOffsetY = (float)(Math.random() * intensity * 2 - intensity);
            }
        }
        
        // Update red overlay effect
        if (redOverlayActive) {
            redOverlayFramesRemaining--;
            if (redOverlayFramesRemaining <= 0) {
                redOverlayActive = false;
            }
        }
    }

    
    public void draw(Graphics2D g2d, int width, int height) {
        // Draw moving background effect (forward movement illusion) - drawn first so it's behind everything
        if (movingBackgroundActive) {
            drawMovingBackground(g2d, width, height);
        }
        
        // Draw darkness effect (life loss)
        if (darknessLevel > 0.0f) {
            Color overlayColor = new Color(0, 0, 0, darknessLevel);
            g2d.setColor(overlayColor);
            g2d.fillRect(0, 0, width, height);
        }
        
        // Draw red overlay for life loss (1 second duration)
        if (redOverlayActive) {
            // Fade out over time
            float alpha = redOverlayFramesRemaining / (float)SHAKE_DURATION;
            Color redOverlay = new Color(255, 0, 0, (int)(alpha * 180)); // Red with fading alpha
            g2d.setColor(redOverlay);
            g2d.fillRect(0, 0, width, height);
        }
        
        // Draw flash effect (new round)
        if (flashActive && flashLevel > 0.0f) {
            int red = flashColor.getRed();
            int green = flashColor.getGreen();
            int blue = flashColor.getBlue();
            Color flashOverlay = new Color(red, green, blue, (int)(flashLevel * 255));
            g2d.setColor(flashOverlay);
            g2d.fillRect(0, 0, width, height);
        }
    }
    
    private void drawMovingBackground(Graphics2D g2d, int width, int height) {
        // Draw horizontal speed lines that scroll upward to create forward movement illusion
        g2d.setColor(new Color(200, 0, 0, 100)); // Dark red semi-transparent lines
        g2d.setStroke(new BasicStroke(2.0f));
        
        // Draw horizontal lines that move upward
        float lineSpacing = 40.0f;
        float startY = backgroundScrollOffset;
        
        while (startY < height) {
            g2d.drawLine(0, (int)startY, width, (int)startY);
            startY += lineSpacing;
        }
        
        // Draw additional lines that wrap around from top
        if (backgroundScrollOffset > 0) {
            startY = backgroundScrollOffset - lineSpacing;
            while (startY >= 0) {
                g2d.drawLine(0, (int)startY, width, (int)startY);
                startY -= lineSpacing;
            }
        }
        
        // Draw vertical lines for depth effect
        g2d.setColor(new Color(180, 0, 0, 80)); // Slightly darker red
        int verticalLineSpacing = 60;
        for (int x = 0; x < width; x += verticalLineSpacing) {
            // Make lines slightly curved or angled for perspective
            int offset = (int)(Math.sin((x + backgroundScrollOffset) * 0.1) * 5);
            g2d.drawLine(x + offset, 0, x + offset, height);
        }
    }
    
    public void reset() {
        darknessLevel = 0.0f;
        fading = false;
        isDarkening = false;
        flashActive = false;
        flashLevel = 0.0f;
        flashFramesRemaining = 0;
        movingBackgroundActive = false;
        backgroundScrollOffset = 0.0f;
        shakeActive = false;
        shakeFramesRemaining = 0;
        shakeOffsetX = 0.0f;
        shakeOffsetY = 0.0f;
        redOverlayActive = false;
        redOverlayFramesRemaining = 0;
    }
    
    public float getDarknessLevel() {
        return darknessLevel;
    }
    
    public float getShakeOffsetX() {
        return shakeActive ? shakeOffsetX : 0.0f;
    }
    
    public float getShakeOffsetY() {
        return shakeActive ? shakeOffsetY : 0.0f;
    }
    
    public boolean isShaking() {
        return shakeActive;
    }
}

