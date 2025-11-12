package com.maya_steph.virusdefense;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Main game panel handling rendering, game loop, and input
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 800;
    private static final int LANE_COUNT = 3;
    private static final int LANE_WIDTH = WIDTH / LANE_COUNT;
    
    private Player player;
    private ArrayList<Virus> viruses;
    private RoundManager roundManager;
    private OverlayEffect overlay;
    private final Random random;
    private Weapons weapons;
    
    private Timer gameTimer;
    private Timer virusSpawnTimer;
    private Timer quizTimer;
    private boolean gameRunning;
    private boolean gameOver;
    
    private int lives = 3;
    
    // UI visibility
    private boolean weaponKeyVisible = true;
    
    // Quiz system
    private QuizManager quizManager;
    private QuizManager.Question currentQuestion;
    private boolean showingQuiz;
    private String userInput;
    private boolean waitingForAnswer;
    private boolean showingResult;
    private boolean answerWasCorrect;
    private Timer resultDisplayTimer;
    
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
        player = new Player(WIDTH / 2, HEIGHT - 100);
        viruses = new ArrayList<>();
        roundManager = new RoundManager();
        overlay = new OverlayEffect();
        random = new Random();
        weapons = new Weapons();
        quizManager = new QuizManager();
        System.out.println("QuizManager initialized with " + quizManager.getQuestionCount() + " questions");
        userInput = "";
        showingQuiz = false;
        waitingForAnswer = false;
        showingResult = false;
        answerWasCorrect = false;
        
        gameTimer = new Timer(16, this); // ~60 FPS
        virusSpawnTimer = new Timer(4000, e -> spawnVirus()); // Start slower (4 seconds) for first round
        
        // Quiz timer will be started when game begins
    }
    
    public void startGame() {
        gameRunning = true;
        gameOver = false;
        lives = 3;
        viruses.clear();
        roundManager.startRound();
        overlay.reset();
        showingQuiz = false;
        waitingForAnswer = false;
        showingResult = false;
        answerWasCorrect = false;
        userInput = "";
        gameTimer.start();
        virusSpawnTimer.start();
        scheduleNextQuiz(true); // First question with longer delay
        requestFocus();
    }
    
    private void spawnVirus() {
        if (!gameRunning || gameOver) return;
        
        // Check if we've already spawned enough viruses for this round
        if (roundManager.checkRoundComplete()) {
            virusSpawnTimer.stop(); // Stop spawning more viruses
            System.out.println("Round virus spawning complete - waiting for viruses to be cleared");
            return;
        }
        
        int lane = random.nextInt(LANE_COUNT);
        int x = lane * LANE_WIDTH + LANE_WIDTH / 2;
        double speed = roundManager.getVirusSpeed();
        
        // Randomly select virus type
        Virus.VirusType[] virusTypes = Virus.VirusType.values();
        Virus.VirusType virusType = virusTypes[random.nextInt(virusTypes.length)];
        
        viruses.add(new Virus(x, 0, speed, virusType));
        roundManager.virusSpawned();
        System.out.println("Spawned " + virusType.getDisplayName() + " virus (weak to " + virusType.getWeakness().getDisplayName() + ") - " + roundManager.getVirusesSpawnedThisRound() + "/" + roundManager.getVirusesPerRound());
        
        // Adjust spawn timer based on round (slower start, gradual increase)
        int baseInterval;
        int round = roundManager.getCurrentRound();
        
        if (round == 1) {
            baseInterval = 4000; // 4 seconds between viruses in first round
        } else if (round <= 3) {
            baseInterval = 3000; // 3 seconds for rounds 2-3
        } else if (round <= 5) {
            baseInterval = 2000; // 2 seconds for rounds 4-5
        } else {
            baseInterval = Math.max(800, 2000 - (round - 5) * 200); // Gradually faster after round 5
        }
        
        virusSpawnTimer.setDelay(baseInterval);
        System.out.println("Round " + round + " - Spawn interval: " + (baseInterval/1000.0) + " seconds");
    }
    
    private void scheduleNextQuiz() {
        scheduleNextQuiz(false);
    }
    
    private void scheduleNextQuiz(boolean isFirstQuestion) {
        if (quizTimer != null) {
            quizTimer.stop();
        }
        
        int interval;
        if (isFirstQuestion) {
            // First question same timing as others (35-60 seconds)
            interval = 35000 + random.nextInt(25000);
            System.out.println("Scheduling FIRST quiz in " + (interval/1000) + " seconds");
        } else {
            // Regular interval between 35-60 seconds (35000-60000 milliseconds)
            interval = 35000 + random.nextInt(25000);
            System.out.println("Scheduling next quiz in " + (interval/1000) + " seconds");
        }
        
        quizTimer = new Timer(interval, e -> showQuiz());
        quizTimer.setRepeats(false); // Only trigger once
        quizTimer.start();
    }
    
    private void showQuiz() {
        System.out.println("showQuiz() called - gameRunning: " + gameRunning + ", gameOver: " + gameOver + ", showingQuiz: " + showingQuiz);
        if (!gameRunning || gameOver || showingQuiz) {
            System.out.println("Quiz not shown - conditions not met");
            return;
        }
        
        currentQuestion = quizManager.getRandomQuestion();
        showingQuiz = true;
        waitingForAnswer = true;
        userInput = "";
        
        System.out.println("Showing quiz: " + currentQuestion.getText());
        
        // Pause the game while quiz is showing
        gameTimer.stop();
        virusSpawnTimer.stop();
        
        // Force a repaint to show the quiz immediately
        repaint();
    }
    
    private void submitQuizAnswer() {
        System.out.println("submitQuizAnswer() called - waitingForAnswer: " + waitingForAnswer + ", userInput: '" + userInput + "'");
        if (!waitingForAnswer || currentQuestion == null) {
            System.out.println("Answer submission blocked - not waiting or no question");
            return;
        }
        
        boolean correct = currentQuestion.checkAnswer(userInput);
        System.out.println("Answer '" + userInput + "' is " + (correct ? "CORRECT" : "WRONG") + ". Expected: '" + currentQuestion.getAnswer() + "'");
        
        // Stop waiting for answer but keep showing quiz
        waitingForAnswer = false;
        answerWasCorrect = correct;
        showingResult = true;
        
        if (!correct) {
            // Wrong answer - lose a life
            System.out.println("Wrong answer - losing a life");
            loseLife();
        } else {
            System.out.println("Correct answer - continuing");
        }
        
        // Show result for 2 seconds, then hide quiz and resume game
        if (resultDisplayTimer != null) {
            resultDisplayTimer.stop();
        }
        resultDisplayTimer = new Timer(2000, e -> hideQuizAndResume());
        resultDisplayTimer.setRepeats(false);
        resultDisplayTimer.start();
        
        // Force immediate repaint to show the result
        repaint();
    }
    
    private void hideQuizAndResume() {
        // Hide quiz and resume game
        showingQuiz = false;
        showingResult = false;
        userInput = "";
        currentQuestion = null;
        
        // Resume game
        if (gameRunning && !gameOver) {
            System.out.println("Resuming game and scheduling next quiz");
            gameTimer.start();
            virusSpawnTimer.start();
            scheduleNextQuiz(); // Schedule next quiz
        } else {
            System.out.println("Not resuming - gameRunning: " + gameRunning + ", gameOver: " + gameOver);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameRunning || gameOver) return;
        
        updateGame();
        repaint();
    }
    
    private void updateGame() {
        // Update player
        player.update();
        
        // Update weapons
        weapons.update();
        
        // Update viruses
        Iterator<Virus> virusIterator = viruses.iterator();
        while (virusIterator.hasNext()) {
            Virus virus = virusIterator.next();
            virus.update();
            
            // Check if virus reached bottom
            if (virus.getY() > HEIGHT) {
                virusIterator.remove();
                loseLife();
            }
        }
        
        // Check collisions between projectiles and viruses
        checkProjectileCollisions();
        
        // Update overlay fade effect
        overlay.update();
        
        // Check if round is complete (all viruses spawned AND no viruses left on screen)
        checkRoundComplete();
    }
    
    private void checkRoundComplete() {
        // Round is complete when all viruses have been spawned AND no viruses remain on screen
        if (roundManager.checkRoundComplete() && viruses.isEmpty()) {
            // Advance to next round
            roundManager.advanceToNextRound();
            
            // Trigger flash effect and play sound
            overlay.triggerNewRoundFlash();
            playSound("round_complete");
            System.out.println("ROUND COMPLETE! NEW ROUND FLASH TRIGGERED!");
            
            // Restart virus spawning for the new round
            virusSpawnTimer.start();
        }
    }
    
    private void loseLife() {
        lives--;
        overlay.triggerLifeLossFlash(); // Red flash when virus passes through
        
        if (lives <= 0) {
            gameOver = true;
            gameTimer.stop();
            virusSpawnTimer.stop();
            if (quizTimer != null) {
                quizTimer.stop();
            }
            if (resultDisplayTimer != null) {
                resultDisplayTimer.stop();
            }
            showingQuiz = false;
            waitingForAnswer = false;
            showingResult = false;
            // No sounds at all when losing life or dying
        } else {
            // Only play life_lost sound if game continues (not when dying)
            playSound("life_lost");
        }
    }
    
    private void checkProjectileCollisions() {
        Iterator<Weapons.ProjectileBall> projectileIterator = weapons.getProjectiles().iterator();
        while (projectileIterator.hasNext()) {
            Weapons.ProjectileBall projectile = projectileIterator.next();
            
            Iterator<Virus> virusIterator = viruses.iterator();
            while (virusIterator.hasNext()) {
                Virus virus = virusIterator.next();
                
                if (projectile.collidesWith(virus)) {
                    // Check if weapon is effective against this virus type
                    boolean effectiveHit = virus.hitWithWeapon(projectile.getWeaponType());
                    projectileIterator.remove();
                    
                    if (effectiveHit) {
                        playSound("effective_hit");
                        System.out.println("Effective hit! " + projectile.getWeaponType().getDisplayName() + " vs " + virus.getVirusType().getDisplayName());
                        
                        // Check if virus is dead
                        if (virus.isDead()) {
                            virusIterator.remove();
                            playSound("virus_destroyed");
                        }
                    } else {
                        playSound("ineffective_hit");
                        System.out.println("Ineffective hit! " + projectile.getWeaponType().getDisplayName() + " vs " + virus.getVirusType().getDisplayName() + " (need " + virus.getWeakness().getDisplayName() + ")");
                    }
                    break; // Projectile can only hit one virus
                }
            }
        }
    }
    
    private void playSound(String soundName) {
        System.out.println("Playing sound: " + soundName);
        
        // Only play actual sound for round completion
        if ("round_complete".equals(soundName)) {
            try {
                // *** CHANGE THIS LINE TO MODIFY THE ROUND COMPLETION SOUND ***
                // Current: Single system beep - replace with custom sound file or different beep sequence
                java.awt.Toolkit.getDefaultToolkit().beep();
                
                // Examples of alternatives:
                // Multiple beeps: add more beep() calls with Thread.sleep() between them
                // Custom sound file: use AudioSystem.getAudioInputStream() and Clip.start()
                // Different system sound: use Runtime.getRuntime().exec("osascript -e \"beep 2\"")
                
            } catch (Exception e) {
                // If sound fails, just continue silently
                System.err.println("Could not play round complete sound: " + e.getMessage());
            }
        }
        // All other sounds are silent (just console output for debugging)
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw lane dividers
        g2d.setColor(Color.GRAY);
        for (int i = 1; i < LANE_COUNT; i++) {
            int x = i * LANE_WIDTH;
            g2d.drawLine(x, 0, x, HEIGHT);
        }
        
        // Draw player
        player.draw(g2d);
        
        // Draw viruses
        for (Virus virus : viruses) {
            virus.draw(g2d);
        }
        
        // Draw weapons/projectiles
        weapons.draw(g2d);
        
        // Draw overlay
        overlay.draw(g2d, WIDTH, HEIGHT);
        
        // Draw UI
        drawUI(g2d);
        
        // Draw quiz screen
        if (showingQuiz) {
            drawQuiz(g2d);
        }
        
        // Draw game over screen
        if (gameOver) {
            drawGameOver(g2d);
        }
    }
    
    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Draw lives as hearts
        drawHearts(g2d);
        
        // Draw round info
        g2d.drawString("Round: " + roundManager.getCurrentRound(), 10, 60);
        g2d.drawString("Viruses: " + roundManager.getVirusesRemaining() + "/" + roundManager.getVirusesPerRound(), 10, 90);
        g2d.drawString("Speed: " + String.format("%.1f", roundManager.getVirusSpeed()), 10, 120);
        
        // Draw current weapon
        g2d.setColor(Color.CYAN);
        g2d.drawString("Weapon: " + weapons.getCurrentWeapon().getDisplayName(), 10, 150);
        
        // Draw basic controls at bottom left
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("Controls:", 10, 180);
        g2d.drawString("Space = shoot", 10, 200);
        g2d.drawString("Arrows = move left/right", 10, 220);
        
        // Draw weapon selection map in top right (if visible)
        if (weaponKeyVisible) {
            drawWeaponMap(g2d);
        }
    }
    
    private void drawHearts(Graphics2D g2d) {
        int heartSize = 20;
        int heartSpacing = 30;
        int startX = 90; // Move hearts to the right to make room for "Lives: " text
        int startY = 30;
        
        // Draw "Lives: " text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Lives: ", 10, startY + 7); // Adjust Y to align with hearts
        
        // Draw filled hearts for remaining lives
        g2d.setColor(Color.RED);
        for (int i = 0; i < lives; i++) {
            drawHeart(g2d, startX + i * heartSpacing, startY, heartSize, true);
        }
        
        // Draw empty hearts for lost lives
        g2d.setColor(Color.DARK_GRAY);
        for (int i = lives; i < 3; i++) {
            drawHeart(g2d, startX + i * heartSpacing, startY, heartSize, false);
        }
    }
    
    private void drawHeart(Graphics2D g2d, int centerX, int centerY, int size, boolean filled) {
        // Better heart shape using smooth curves
        int width = size;
        int height = (int)(size * 0.8);
        
        // Calculate heart coordinates for a more accurate shape
        int[] heartX = new int[13];
        int[] heartY = new int[13];
        
        // Start from bottom point and work clockwise
        heartX[0] = centerX;                           // Bottom point
        heartY[0] = centerY + height/2;
        
        heartX[1] = centerX - width/6;                 // Bottom left curve
        heartY[1] = centerY + height/4;
        
        heartX[2] = centerX - width/3;                 // Left side
        heartY[2] = centerY;
        
        heartX[3] = centerX - width/3;                 // Left top curve start
        heartY[3] = centerY - height/6;
        
        heartX[4] = centerX - width/4;                 // Left top
        heartY[4] = centerY - height/3;
        
        heartX[5] = centerX - width/6;                 // Left top curve end
        heartY[5] = centerY - height/3;
        
        heartX[6] = centerX;                           // Center top
        heartY[6] = centerY - height/6;
        
        heartX[7] = centerX + width/6;                 // Right top curve start
        heartY[7] = centerY - height/3;
        
        heartX[8] = centerX + width/4;                 // Right top
        heartY[8] = centerY - height/3;
        
        heartX[9] = centerX + width/3;                 // Right top curve end
        heartY[9] = centerY - height/6;
        
        heartX[10] = centerX + width/3;                // Right side
        heartY[10] = centerY;
        
        heartX[11] = centerX + width/6;                // Bottom right curve
        heartY[11] = centerY + height/4;
        
        heartX[12] = centerX;                          // Back to bottom point
        heartY[12] = centerY + height/2;
        
        if (filled) {
            g2d.fillPolygon(heartX, heartY, heartX.length);
        } else {
            g2d.setStroke(new java.awt.BasicStroke(2)); // Thicker outline
            g2d.drawPolygon(heartX, heartY, heartX.length);
            g2d.setStroke(new java.awt.BasicStroke(1)); // Reset stroke
        }
    }
    
    private void drawWeaponMap(Graphics2D g2d) {
        // Position in top right corner
        int mapX = WIDTH - 200;
        int mapY = 20;
        int lineHeight = 25;
        
        // Background box (made taller for virus patterns, wider for title, and extra height for hide instruction)
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(mapX - 10, mapY - 5, 200, 170, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(mapX - 10, mapY - 5, 200, 170, 10, 10);
        
        // Title (moved down to fit better inside the box)
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("WEAPONS KEY", mapX, mapY + 15);
        
        // Weapon mappings with visual indicators
        g2d.setFont(new Font("Courier New", Font.BOLD, 12));
        Weapons.WeaponType currentWeapon = weapons.getCurrentWeapon();
        
        // 1 - Spiky Ball -> Spiky Virus (moved down by 15 pixels to fit after title)
        g2d.setColor(currentWeapon == Weapons.WeaponType.SPIKY_BALL ? Color.CYAN : Color.LIGHT_GRAY);
        g2d.drawString("[1]", mapX, mapY + lineHeight + 15);
        g2d.setColor(Color.MAGENTA);
        drawMiniSpikyBall(g2d, mapX + 32, mapY + lineHeight + 15 - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.SPIKY_BALL ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("vs", mapX + 44, mapY + lineHeight + 15);
        g2d.setColor(Color.RED);
        drawMiniVirusSpiky(g2d, mapX + 72, mapY + lineHeight + 15 - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.SPIKY_BALL ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("Spiky", mapX + 92, mapY + lineHeight + 15);
        
        // 2 - Ball -> Round Virus
        g2d.setColor(currentWeapon == Weapons.WeaponType.BALL ? Color.CYAN : Color.LIGHT_GRAY);
        g2d.drawString("[2]", mapX, mapY + lineHeight * 2 + 15);
        g2d.setColor(Color.YELLOW);
        drawMiniBall(g2d, mapX + 32, mapY + lineHeight * 2 + 15 - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.BALL ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("vs", mapX + 44, mapY + lineHeight * 2 + 15);
        g2d.setColor(Color.GREEN);
        drawMiniVirusRound(g2d, mapX + 72, mapY + lineHeight * 2 + 15 - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.BALL ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("Round", mapX + 92, mapY + lineHeight * 2 + 15);
        
        // 3 - Star -> Star Virus
        g2d.setColor(currentWeapon == Weapons.WeaponType.STAR ? Color.CYAN : Color.LIGHT_GRAY);
        g2d.drawString("[3]", mapX, mapY + lineHeight * 3 + 15);
        g2d.setColor(Color.CYAN);
        drawMiniStar(g2d, mapX + 32, mapY + lineHeight * 3 + 15 - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.STAR ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("vs", mapX + 44, mapY + lineHeight * 3 + 15);
        g2d.setColor(Color.BLUE);
        drawMiniVirusStar(g2d, mapX + 72, mapY + lineHeight * 3 + 15 - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.STAR ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("Star", mapX + 92, mapY + lineHeight * 3 + 15);
        
        // 4 - Arrow -> Arrow Virus
        g2d.setColor(currentWeapon == Weapons.WeaponType.ARROW ? Color.CYAN : Color.LIGHT_GRAY);
        g2d.drawString("[4]", mapX, mapY + lineHeight * 4 + 15);
        g2d.setColor(Color.WHITE);
        drawMiniArrow(g2d, mapX + 32, mapY + lineHeight * 4 + 15 - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.ARROW ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("vs", mapX + 44, mapY + lineHeight * 4 + 15);
        g2d.setColor(Color.ORANGE);
        drawMiniVirusArrow(g2d, mapX + 72, mapY + lineHeight * 4 + 15 - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.ARROW ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("Arrow", mapX + 92, mapY + lineHeight * 4 + 15);
        
        // Legend (moved down to fit after weapon entries)
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("Only effective against matching virus!", mapX, mapY + lineHeight * 5 + 13);
        
        // Hide instruction
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.GRAY);
        g2d.drawString("Press ' i ' to hide this key", mapX, mapY + lineHeight * 5 + 30);
    }
    
    private void drawMiniBall(Graphics2D g2d, int x, int y) {
        g2d.fillOval(x - 4, y - 4, 8, 8);
    }
    
    private void drawMiniStar(Graphics2D g2d, int x, int y) {
        int[] starX = new int[5];
        int[] starY = new int[5];
        int radius = 5;
        
        for (int i = 0; i < 5; i++) {
            double angle = Math.PI * 2 * i / 5.0 - Math.PI / 2;
            starX[i] = x + (int)(radius * Math.cos(angle));
            starY[i] = y + (int)(radius * Math.sin(angle));
        }
        g2d.fillPolygon(starX, starY, 5);
    }
    
    private void drawMiniSpikyBall(Graphics2D g2d, int x, int y) {
        // Main ball
        g2d.fillOval(x - 3, y - 3, 6, 6);
        // Mini spikes
        g2d.drawLine(x - 5, y, x - 3, y);
        g2d.drawLine(x + 3, y, x + 5, y);
        g2d.drawLine(x, y - 5, x, y - 3);
        g2d.drawLine(x, y + 3, x, y + 5);
    }
    
    private void drawMiniArrow(Graphics2D g2d, int x, int y) {
        // Arrow body
        g2d.drawLine(x, y + 3, x, y - 3);
        // Arrow head
        g2d.drawLine(x, y - 3, x - 2, y - 1);
        g2d.drawLine(x, y - 3, x + 2, y - 1);
    }
    
    // Mini virus drawing methods
    private void drawMiniVirusSpiky(Graphics2D g2d, int x, int y) {
        // Mini spiky virus
        g2d.fillOval(x - 4, y - 4, 8, 8);
        // Mini spikes
        g2d.drawLine(x - 5, y, x - 4, y);
        g2d.drawLine(x + 4, y, x + 5, y);
        g2d.drawLine(x, y - 5, x, y - 4);
        g2d.drawLine(x, y + 4, x, y + 5);
    }
    
    private void drawMiniVirusRound(Graphics2D g2d, int x, int y) {
        // Simple round virus
        g2d.fillOval(x - 4, y - 4, 8, 8);
        g2d.drawOval(x - 3, y - 3, 6, 6);
    }
    
    private void drawMiniVirusStar(Graphics2D g2d, int x, int y) {
        // Mini 6-pointed star virus
        int[] starX = {x, x - 2, x - 3, x - 2, x, x + 2, x + 3, x + 2};
        int[] starY = {y - 3, y - 1, y, y + 1, y + 3, y + 1, y, y - 1};
        g2d.fillPolygon(starX, starY, 8);
    }
    
    private void drawMiniVirusArrow(Graphics2D g2d, int x, int y) {
        // Mini arrow virus pointing down
        int[] headX = {x - 3, x, x + 3};
        int[] headY = {y, y + 3, y};
        g2d.fillPolygon(headX, headY, 3);
        g2d.fillRect(x - 1, y - 3, 2, 3);
    }
    
    private void drawGameOver(Graphics2D g2d) {
        // Pure bright red semi-transparent overlay background (sheer)
        g2d.setColor(new Color(255, 0, 0, 120)); // Pure bright red: R=255, G=0, B=0
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Game Over text in white for contrast
        g2d.setColor(Color.WHITE); // White text stands out against red background
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "GAME OVER";
        int x = (WIDTH - fm.stringWidth(text)) / 2;
        int y = HEIGHT / 2 - 50;
        g2d.drawString(text, x, y);
        
        // Final round info
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        String roundText = "Final Round: " + roundManager.getCurrentRound();
        fm = g2d.getFontMetrics();
        x = (WIDTH - fm.stringWidth(roundText)) / 2;
        y = HEIGHT / 2 + 20;
        g2d.drawString(roundText, x, y);
        
        // Restart instruction
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        String restartText = "Press R to restart";
        fm = g2d.getFontMetrics();
        x = (WIDTH - fm.stringWidth(restartText)) / 2;
        y = HEIGHT / 2 + 60;
        g2d.drawString(restartText, x, y);
    }
    
    private void drawQuiz(Graphics2D g2d) {
        if (currentQuestion == null) return;
        
        // Semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Quiz title
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "QUIZ TIME!";
        int x = (WIDTH - fm.stringWidth(title)) / 2;
        int y = HEIGHT / 2 - 100;
        g2d.drawString(title, x, y);
        
        // Question text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        String questionText = "Question: " + currentQuestion.getText();
        fm = g2d.getFontMetrics();
        x = (WIDTH - fm.stringWidth(questionText)) / 2;
        y = HEIGHT / 2 - 40;
        g2d.drawString(questionText, x, y);
        
        if (showingResult) {
            // Show result instead of input box
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            if (answerWasCorrect) {
                g2d.setColor(Color.GREEN);
                String correctText = "CORRECT!";
                fm = g2d.getFontMetrics();
                x = (WIDTH - fm.stringWidth(correctText)) / 2;
                y = HEIGHT / 2 + 40;
                g2d.drawString(correctText, x, y);
            } else {
                g2d.setColor(Color.RED);
                String wrongText = "WRONG!";
                fm = g2d.getFontMetrics();
                x = (WIDTH - fm.stringWidth(wrongText)) / 2;
                y = HEIGHT / 2 + 40;
                g2d.drawString(wrongText, x, y);
                
                // Show correct answer
                g2d.setFont(new Font("Arial", Font.PLAIN, 24));
                g2d.setColor(Color.YELLOW);
                String correctAnswerText = "Correct answer: " + currentQuestion.getAnswer();
                fm = g2d.getFontMetrics();
                x = (WIDTH - fm.stringWidth(correctAnswerText)) / 2;
                y = HEIGHT / 2 + 80;
                g2d.drawString(correctAnswerText, x, y);
            }
        } else {
            // Show input box for answering
            // Input box background
            g2d.setColor(new Color(50, 50, 50, 200));
            int boxWidth = 400;
            int boxHeight = 50;
            int boxX = (WIDTH - boxWidth) / 2;
            int boxY = HEIGHT / 2 + 10;
            g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
            
            // Input box border
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 10, 10);
            
            // Input text label
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            g2d.setColor(Color.CYAN);
            String label = "Your Answer:";
            fm = g2d.getFontMetrics();
            x = boxX + 10;
            y = boxY - 10;
            g2d.drawString(label, x, y);
            
            // Input text with cursor
            g2d.setFont(new Font("Courier New", Font.BOLD, 24));
            g2d.setColor(Color.WHITE);
            String displayText = userInput + "|";
            fm = g2d.getFontMetrics();
            x = boxX + 15;
            y = boxY + 32;
            g2d.drawString(displayText, x, y);
            
            // Instructions
            g2d.setFont(new Font("Arial", Font.PLAIN, 16));
            g2d.setColor(Color.LIGHT_GRAY);
            String instructions = "Type your answer and press ENTER";
            fm = g2d.getFontMetrics();
            x = (WIDTH - fm.stringWidth(instructions)) / 2;
            y = HEIGHT / 2 + 100;
            g2d.drawString(instructions, x, y);
            
            // Warning
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            String warning = "Wrong answer will cost you a life!";
            fm = g2d.getFontMetrics();
            x = (WIDTH - fm.stringWidth(warning)) / 2;
            y = HEIGHT / 2 + 130;
            g2d.drawString(warning, x, y);
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (gameOver && key == KeyEvent.VK_R) {
            startGame();
            return;
        }
        
        // Handle quiz input
        if (showingQuiz && waitingForAnswer) {
            if (key == KeyEvent.VK_ENTER) {
                submitQuizAnswer();
                return;
            } else if (key == KeyEvent.VK_BACK_SPACE) {
                if (userInput.length() > 0) {
                    userInput = userInput.substring(0, userInput.length() - 1);
                    repaint(); // Show the deleted character immediately
                    System.out.println("Backspace pressed, current input: '" + userInput + "'");
                }
                return;
            }
            // Let keyTyped handle character input for quiz
            return;
        }
        
        if (!gameRunning || gameOver) return;
        
        switch (key) {
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                player.moveLeft(LANE_WIDTH);
                playSound("move"); // Placeholder
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                player.moveRight(LANE_WIDTH, WIDTH);
                playSound("move"); // Placeholder
                break;
            case KeyEvent.VK_SPACE:
                weapons.shoot(player.getX(), player.getY());
                playSound("shoot"); // Placeholder
                break;
            case KeyEvent.VK_P:
                weapons.switchWeapon();
                playSound("weapon_switch"); // Placeholder
                break;
            case KeyEvent.VK_1:
                weapons.setWeapon(Weapons.WeaponType.SPIKY_BALL);
                playSound("weapon_switch"); // Placeholder
                System.out.println("Switched to Spiky Ball");
                break;
            case KeyEvent.VK_2:
                weapons.setWeapon(Weapons.WeaponType.BALL);
                playSound("weapon_switch"); // Placeholder
                System.out.println("Switched to Ball");
                break;
            case KeyEvent.VK_3:
                weapons.setWeapon(Weapons.WeaponType.STAR);
                playSound("weapon_switch"); // Placeholder
                System.out.println("Switched to Star");
                break;
            case KeyEvent.VK_4:
                weapons.setWeapon(Weapons.WeaponType.ARROW);
                playSound("weapon_switch"); // Placeholder
                System.out.println("Switched to Arrow");
                break;
            case KeyEvent.VK_I:
                weaponKeyVisible = !weaponKeyVisible;
                System.out.println("Weapon key " + (weaponKeyVisible ? "shown" : "hidden"));
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Handle quiz text input
        if (showingQuiz && waitingForAnswer) {
            char c = e.getKeyChar();
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
                userInput += c;
                repaint(); // Immediately show the typed character
                System.out.println("User typed: '" + c + "', current input: '" + userInput + "'");
            }
        }
    }
}

