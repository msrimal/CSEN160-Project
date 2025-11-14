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
    private SoundManager soundManager;
    
    private Timer gameTimer;
    private Timer virusSpawnTimer;
    private Timer quizTimer;
    private boolean gameRunning;
    private boolean gameOver;
    private boolean showingHomeScreen;
    
    private int lives = 3;
    
    // UI visibility
    private boolean weaponKeyVisible = true; // Show weapon key by default
    
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
        setBackground(Color.RED); // Red background for game
        setFocusable(true);
        addKeyListener(this);
        
        player = new Player(WIDTH / 2, HEIGHT - 100);
        viruses = new ArrayList<>();
        roundManager = new RoundManager();
        overlay = new OverlayEffect();
        random = new Random();
        weapons = new Weapons();
        quizManager = new QuizManager();
        soundManager = new SoundManager();
        System.out.println("QuizManager initialized with " + quizManager.getQuestionCount() + " questions");
        userInput = "";
        showingQuiz = false;
        waitingForAnswer = false;
        showingResult = false;
        answerWasCorrect = false;
        showingHomeScreen = true;
        gameRunning = false;
        
        gameTimer = new Timer(16, this); // ~60 FPS
        gameTimer.start(); // Start timer for home screen blinking effect
        virusSpawnTimer = new Timer(4000, e -> spawnVirus()); // Start slower (4 seconds) for first round
        
        // Quiz timer will be started when game begins
    }
    
    public void startGame() {
        showingHomeScreen = false;
        gameRunning = true;
        gameOver = false;
        lives = 3;
        viruses.clear();
        roundManager.startRound();
        overlay.reset();
        overlay.startMovingBackground(); // Start moving background effect for forward movement illusion
        showingQuiz = false;
        waitingForAnswer = false;
        showingResult = false;
        answerWasCorrect = false;
        userInput = "";
        gameTimer.start();
        virusSpawnTimer.start();
        scheduleNextQuiz(true); // First question with longer delay
        // Background music disabled
        requestFocus();
    }
    
    private void returnToHomeScreen() {
        showingHomeScreen = true;
        gameRunning = false;
        gameOver = false;
        lives = 3;
        viruses.clear();
        showingQuiz = false;
        waitingForAnswer = false;
        showingResult = false;
        answerWasCorrect = false;
        userInput = "";
        currentQuestion = null;
        
        // Stop all timers
        gameTimer.stop();
        virusSpawnTimer.stop();
        if (quizTimer != null) {
            quizTimer.stop();
        }
        if (resultDisplayTimer != null) {
            resultDisplayTimer.stop();
        }
        
        // Reset game state
        roundManager = new RoundManager();
        overlay.reset();
        overlay.stopMovingBackground(); // Stop moving background effect
        weapons = new Weapons();
        player = new Player(WIDTH / 2, HEIGHT - 100);
        
        // Restart game timer for home screen blinking effect
        gameTimer.start();
        requestFocus();
        repaint();
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
        
        // Disable sounds during quiz (no background music to pause)
        if (soundManager != null) {
            soundManager.setSoundEnabled(false);
        }
        
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
            // Wrong answer - lose a life (from quiz, so no shake/red overlay)
            System.out.println("Wrong answer - losing a life");
            loseLife(true); // Pass true to indicate it's from quiz
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
            
            // Re-enable sounds (no background music to resume)
            if (soundManager != null) {
                soundManager.setSoundEnabled(true);
            }
            
            scheduleNextQuiz(); // Schedule next quiz
        } else {
            System.out.println("Not resuming - gameRunning: " + gameRunning + ", gameOver: " + gameOver);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (showingHomeScreen) {
            repaint(); // Repaint for blinking effect
            return;
        }
        if (!gameRunning || gameOver) return;
        
        try {
            updateGame();
            repaint();
        } catch (Exception ex) {
            // Catch any exceptions to prevent game from crashing
            System.err.println("Error in game loop: " + ex.getMessage());
            ex.printStackTrace();
        }
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
            
            // Trigger flash effect
            overlay.triggerNewRoundFlash();
            System.out.println("ROUND COMPLETE! NEW ROUND FLASH TRIGGERED!");
            
            // Restart virus spawning for the new round
            virusSpawnTimer.start();
        }
    }
    
    private void loseLife() {
        loseLife(false); // Default: not from quiz
    }
    
    private void loseLife(boolean fromQuiz) {
        lives--;
        
        // Only trigger shake and red overlay if NOT from quiz wrong answer
        if (!fromQuiz) {
            overlay.triggerLifeLossShakeAndRedOverlay(); // Shake and red overlay for 1 second
        }
        
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
            soundManager.stopBackgroundMusic(); // Stop music on game over
            playSound("game_over"); // Play game over sound
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
                        System.out.println("Effective hit! " + projectile.getWeaponType().getDisplayName() + " vs " + virus.getVirusType().getDisplayName());
                        
                        // Check if virus is dead
                        if (virus.isDead()) {
                            virusIterator.remove();
                        }
                    } else {
                        System.out.println("Ineffective hit! " + projectile.getWeaponType().getDisplayName() + " vs " + virus.getVirusType().getDisplayName() + " (need " + virus.getWeakness().getDisplayName() + ")");
                    }
                    break; // Projectile can only hit one virus
                }
            }
        }
    }
    
    private void playSound(String soundName) {
        // Play sound immediately - no exception handling overhead
        if (soundManager != null) {
            soundManager.playSound(soundName);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw home screen first - if showing, don't draw game elements
        if (showingHomeScreen) {
            drawHomeScreen(g2d);
            return;
        }
        
        // Apply shake effect to entire screen if active
        if (overlay.isShaking()) {
            g2d.translate(overlay.getShakeOffsetX(), overlay.getShakeOffsetY());
        }
        
        // Draw red background
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Draw overlay (includes moving background effect) - drawn before game elements
        overlay.draw(g2d, WIDTH, HEIGHT);
        
        // Lane dividers removed - no lines separating lanes
        
        // Draw player
        player.draw(g2d);
        
        // Draw viruses
        for (Virus virus : viruses) {
            virus.draw(g2d);
        }
        
        // Draw weapons/projectiles
        weapons.draw(g2d);
        
        // Reset shake transform
        if (overlay.isShaking()) {
            g2d.translate(-overlay.getShakeOffsetX(), -overlay.getShakeOffsetY());
        }
        
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
        // Draw combined player info box (Game Stats, Lives, Weapon, Controls) on the right
        drawPlayerInfoBox(g2d);
        
        // Draw weapon selection map in top right (if visible)
        if (weaponKeyVisible) {
            drawWeaponMap(g2d);
        }
    }
    
    private void drawPlayerInfoBox(Graphics2D g2d) {
        // Position in top left
        int boxX = 10;
        int boxY = 20;
        int boxWidth = 200;
        int boxHeight = 240; // Increased height to fit all information
        
        // Background box
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(boxX - 10, boxY - 5, boxWidth, boxHeight, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(boxX - 10, boxY - 5, boxWidth, boxHeight, 10, 10);
        
        // Draw Game Stats section at the top
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("GAME STATS", boxX, boxY + 15);
        
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Round: " + roundManager.getCurrentRound(), boxX, boxY + 35);
        g2d.drawString("Viruses: " + roundManager.getVirusesRemaining() + "/" + roundManager.getVirusesPerRound(), boxX, boxY + 55);
        g2d.drawString("Speed: " + String.format("%.1f", roundManager.getVirusSpeed()), boxX, boxY + 75);
        
        // Draw Lives section
        int livesY = boxY + 95;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("LIVES", boxX, livesY);
        
        // Draw hearts for lives
        int heartSize = 15;
        int heartSpacing = 20;
        int heartStartX = boxX;
        int heartStartY = livesY + 20;
        g2d.setColor(Color.RED);
        for (int i = 0; i < lives; i++) {
            drawHeart(g2d, heartStartX + i * heartSpacing, heartStartY, heartSize, true);
        }
        g2d.setColor(Color.DARK_GRAY);
        for (int i = lives; i < 3; i++) {
            drawHeart(g2d, heartStartX + i * heartSpacing, heartStartY, heartSize, false);
        }
        
        // Draw current weapon info
        int weaponInfoY = boxY + 145;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("CURRENT WEAPON", boxX, weaponInfoY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(Color.WHITE);
        g2d.drawString(weapons.getCurrentWeapon().getDisplayName(), boxX, weaponInfoY + 18);
        
        // Draw Controls section
        int controlsY = boxY + 185;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("CONTROLS", boxX, controlsY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Space = shoot", boxX, controlsY + 18);
        g2d.drawString("Arrows = move", boxX, controlsY + 33);
    }
    
    private void drawHearts(Graphics2D g2d) {
        int heartSize = 20;
        int heartSpacing = 30;
        int startX = 90; // Move hearts to the right to make room for "Lives: " text
        int startY = 30;
        
        // Draw "Lives: " text
        g2d.setColor(Color.BLACK); // Changed to black for visibility on red background
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
        // Position in top right (opposite of game stats on left)
        int mapX = WIDTH - 200 - 10; // Right side, same offset as game stats (10 from left = 10 from right)
        int mapY = 20; // Top of page, same Y as game stats box
        int lineHeight = 25;
        
        // Background box (restored to original size)
        int boxHeight = 170;
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(mapX - 10, mapY - 5, 200, boxHeight, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(mapX - 10, mapY - 5, 200, boxHeight, 10, 10);
        
        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("WEAPONS KEY", mapX, mapY + 15);
        
        // Weapon mappings with visual indicators
        g2d.setFont(new Font("Courier New", Font.BOLD, 12));
        Weapons.WeaponType currentWeapon = weapons.getCurrentWeapon();
        
        // 1 - Spiky Ball -> Spiky Virus
        int weaponStartY = mapY + 40;
        g2d.setColor(currentWeapon == Weapons.WeaponType.SPIKY_BALL ? Color.CYAN : Color.LIGHT_GRAY);
        g2d.drawString("[1]", mapX, weaponStartY);
        g2d.setColor(Color.CYAN); // Changed from MAGENTA to CYAN to match projectile color
        drawMiniSpikyBall(g2d, mapX + 32, weaponStartY - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.SPIKY_BALL ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("vs", mapX + 44, weaponStartY);
        g2d.setColor(new Color(139, 69, 19)); // Brown color to match Spiky virus
        drawMiniVirusSpiky(g2d, mapX + 72, weaponStartY - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.SPIKY_BALL ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("Spiky", mapX + 92, weaponStartY);
        
        // 2 - Ball -> Round Virus
        g2d.setColor(currentWeapon == Weapons.WeaponType.BALL ? Color.CYAN : Color.LIGHT_GRAY);
        g2d.drawString("[2]", mapX, weaponStartY + lineHeight);
        g2d.setColor(Color.YELLOW);
        drawMiniBall(g2d, mapX + 32, weaponStartY + lineHeight - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.BALL ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("vs", mapX + 44, weaponStartY + lineHeight);
        g2d.setColor(Color.GREEN);
        drawMiniVirusRound(g2d, mapX + 72, weaponStartY + lineHeight - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.BALL ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("Round", mapX + 92, weaponStartY + lineHeight);
        
        // 3 - Star -> Star Virus
        g2d.setColor(currentWeapon == Weapons.WeaponType.STAR ? Color.CYAN : Color.LIGHT_GRAY);
        g2d.drawString("[3]", mapX, weaponStartY + lineHeight * 2);
        g2d.setColor(Color.CYAN);
        drawMiniStar(g2d, mapX + 32, weaponStartY + lineHeight * 2 - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.STAR ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("vs", mapX + 44, weaponStartY + lineHeight * 2);
        g2d.setColor(Color.BLUE);
        drawMiniVirusStar(g2d, mapX + 72, weaponStartY + lineHeight * 2 - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.STAR ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("Star", mapX + 92, weaponStartY + lineHeight * 2);
        
        // 4 - Arrow -> Arrow Virus
        g2d.setColor(currentWeapon == Weapons.WeaponType.ARROW ? Color.CYAN : Color.LIGHT_GRAY);
        g2d.drawString("[4]", mapX, weaponStartY + lineHeight * 3);
        g2d.setColor(Color.WHITE);
        drawMiniArrow(g2d, mapX + 32, weaponStartY + lineHeight * 3 - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.ARROW ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("vs", mapX + 44, weaponStartY + lineHeight * 3);
        g2d.setColor(Color.ORANGE);
        drawMiniVirusArrow(g2d, mapX + 72, weaponStartY + lineHeight * 3 - 4);
        g2d.setColor(currentWeapon == Weapons.WeaponType.ARROW ? Color.WHITE : Color.LIGHT_GRAY);
        g2d.drawString("Arrow", mapX + 92, weaponStartY + lineHeight * 3);
        
        // Legend
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("Only effective against", mapX, weaponStartY + lineHeight * 4 + 5);
        g2d.drawString("matching virus!", mapX, weaponStartY + lineHeight * 4 + 18);
        
        // Hide instruction
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.GRAY);
        g2d.drawString("Press ' i ' to hide", mapX, weaponStartY + lineHeight * 4 + 35);
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
    
    private void drawHomeScreen(Graphics2D g2d) {
        // Background - black
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Title
        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "HEARTATTACK";
        int x = (WIDTH - fm.stringWidth(title)) / 2;
        int y = 80;
        g2d.drawString(title, x, y);
        
        // Subtitle
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        fm = g2d.getFontMetrics();
        String subtitle = "Defend the Heart!";
        x = (WIDTH - fm.stringWidth(subtitle)) / 2;
        y = 130;
        g2d.drawString(subtitle, x, y);
        
        // Premise
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        fm = g2d.getFontMetrics();
        String premise = "Game Premise:";
        x = (WIDTH - fm.stringWidth(premise)) / 2;
        y = 200;
        g2d.drawString(premise, x, y);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        String premiseText1 = "You are defending the heart organ from";
        String premiseText2 = "different viruses. Answer heart biology";
        String premiseText3 = "questions correctly to survive!";
        fm = g2d.getFontMetrics();
        x = (WIDTH - fm.stringWidth(premiseText1)) / 2;
        y = 240;
        g2d.drawString(premiseText1, x, y);
        x = (WIDTH - fm.stringWidth(premiseText2)) / 2;
        y = 265;
        g2d.drawString(premiseText2, x, y);
        x = (WIDTH - fm.stringWidth(premiseText3)) / 2;
        y = 290;
        g2d.drawString(premiseText3, x, y);
        
        // Controls section
        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        fm = g2d.getFontMetrics();
        String controlsTitle = "Controls:";
        x = (WIDTH - fm.stringWidth(controlsTitle)) / 2;
        y = 350;
        g2d.drawString(controlsTitle, x, y);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        int startY = 385;
        int lineHeight = 25;
        String[] controls = {
            "Arrow Keys or A/D - Move left/right",
            "Spacebar - Shoot current weapon",
            "1-4 Keys - Switch weapons directly",
            "P - Cycle through weapons",
            "I - Toggle weapon key visibility"
        };
        
        for (int i = 0; i < controls.length; i++) {
            fm = g2d.getFontMetrics();
            x = (WIDTH - fm.stringWidth(controls[i])) / 2;
            y = startY + i * lineHeight;
            g2d.drawString(controls[i], x, y);
        }
        
        // Weapon info
        g2d.setColor(Color.MAGENTA);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        fm = g2d.getFontMetrics();
        String weaponInfo = "Match weapons to virus types to destroy them!";
        x = (WIDTH - fm.stringWidth(weaponInfo)) / 2;
        y = startY + controls.length * lineHeight + 20;
        g2d.drawString(weaponInfo, x, y);
        
        // Start instruction with blinking effect
        long time = System.currentTimeMillis();
        if ((time / 500) % 2 == 0) {
            g2d.setColor(Color.GREEN);
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            fm = g2d.getFontMetrics();
            String startText = "Press ENTER to Start";
            x = (WIDTH - fm.stringWidth(startText)) / 2;
            y = HEIGHT - 80;
            g2d.drawString(startText, x, y);
        }
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
        
        // Exit to main menu instruction
        String exitText = "Press ESC to return to main menu";
        fm = g2d.getFontMetrics();
        x = (WIDTH - fm.stringWidth(exitText)) / 2;
        y = HEIGHT / 2 + 100;
        g2d.drawString(exitText, x, y);
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
        int titleX = (WIDTH - fm.stringWidth(title)) / 2;
        int titleY = HEIGHT / 2 - 100;
        g2d.drawString(title, titleX, titleY);
        
        // Question text - display the actual question, not the ID
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 22));
        String questionText = currentQuestion.getText();
        
        // Word wrap for long questions
        FontMetrics questionFm = g2d.getFontMetrics();
        int maxWidth = WIDTH - 80; // Leave margins
        String[] words = questionText.split(" ");
        StringBuilder line = new StringBuilder();
        int questionY = HEIGHT / 2 - 60;
        int lineHeight = 30;
        
        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            int width = questionFm.stringWidth(testLine);
            
            if (width > maxWidth && line.length() > 0) {
                // Draw current line and start new one
                int questionX = (WIDTH - questionFm.stringWidth(line.toString())) / 2;
                g2d.drawString(line.toString(), questionX, questionY);
                line = new StringBuilder(word);
                questionY += lineHeight;
            } else {
                line.append(line.length() == 0 ? word : " " + word);
            }
        }
        // Draw last line
        if (line.length() > 0) {
            int questionX = (WIDTH - questionFm.stringWidth(line.toString())) / 2;
            g2d.drawString(line.toString(), questionX, questionY);
        }
        
        if (showingResult) {
            // Show result instead of input box
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            if (answerWasCorrect) {
                g2d.setColor(Color.GREEN);
                String correctText = "CORRECT!";
                FontMetrics resultFm = g2d.getFontMetrics();
                int resultX = (WIDTH - resultFm.stringWidth(correctText)) / 2;
                int resultY = HEIGHT / 2 + 40;
                g2d.drawString(correctText, resultX, resultY);
            } else {
                g2d.setColor(Color.RED);
                String wrongText = "WRONG!";
                FontMetrics resultFm = g2d.getFontMetrics();
                int resultX = (WIDTH - resultFm.stringWidth(wrongText)) / 2;
                int resultY = HEIGHT / 2 + 40;
                g2d.drawString(wrongText, resultX, resultY);
                
                // Show correct answer
                g2d.setFont(new Font("Arial", Font.PLAIN, 24));
                g2d.setColor(Color.YELLOW);
                String correctAnswerText = "Correct answer: " + currentQuestion.getAnswer();
                resultFm = g2d.getFontMetrics();
                resultX = (WIDTH - resultFm.stringWidth(correctAnswerText)) / 2;
                resultY = HEIGHT / 2 + 80;
                g2d.drawString(correctAnswerText, resultX, resultY);
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
            FontMetrics inputFm = g2d.getFontMetrics();
            int labelX = boxX + 10;
            int labelY = boxY - 10;
            g2d.drawString(label, labelX, labelY);
            
            // Input text with cursor
            g2d.setFont(new Font("Courier New", Font.BOLD, 24));
            g2d.setColor(Color.WHITE);
            String displayText = userInput + "|";
            inputFm = g2d.getFontMetrics();
            int inputX = boxX + 15;
            int inputY = boxY + 32;
            g2d.drawString(displayText, inputX, inputY);
            
            // Instructions
            g2d.setFont(new Font("Arial", Font.PLAIN, 16));
            g2d.setColor(Color.LIGHT_GRAY);
            String instructions = "Type your answer and press ENTER";
            inputFm = g2d.getFontMetrics();
            int instX = (WIDTH - inputFm.stringWidth(instructions)) / 2;
            int instY = HEIGHT / 2 + 100;
            g2d.drawString(instructions, instX, instY);
            
            // Warning
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            String warning = "Wrong answer will cost you a life!";
            inputFm = g2d.getFontMetrics();
            int warnX = (WIDTH - inputFm.stringWidth(warning)) / 2;
            int warnY = HEIGHT / 2 + 130;
            g2d.drawString(warning, warnX, warnY);
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        try {
            int key = e.getKeyCode();
            
            // Handle ESC key - return to home screen from anywhere
            if (key == KeyEvent.VK_ESCAPE) {
                if (gameOver || gameRunning) {
                    returnToHomeScreen();
                }
                return;
            }
            
            // Handle home screen - Enter key starts the game
            if (showingHomeScreen) {
                if (key == KeyEvent.VK_ENTER) {
                    startGame();
                }
                return;
            }
            
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
                playSound("move");
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                player.moveRight(LANE_WIDTH, WIDTH);
                playSound("move");
                break;
            case KeyEvent.VK_SPACE:
                // Play sound first, then shoot - ensures sound executes immediately
                playSound("shoot");
                // Shoot immediately with current weapon - state is already set
                weapons.shoot(player.getX(), player.getY());
                break;
            case KeyEvent.VK_P:
                // Switch weapon atomically - no delays
                weapons.switchWeapon();
                break;
            case KeyEvent.VK_1:
                weapons.setWeapon(Weapons.WeaponType.SPIKY_BALL);
                break;
            case KeyEvent.VK_2:
                weapons.setWeapon(Weapons.WeaponType.BALL);
                break;
            case KeyEvent.VK_3:
                weapons.setWeapon(Weapons.WeaponType.STAR);
                break;
            case KeyEvent.VK_4:
                weapons.setWeapon(Weapons.WeaponType.ARROW);
                break;
            case KeyEvent.VK_I:
                weaponKeyVisible = !weaponKeyVisible;
                break;
        }
        } catch (Exception ex) {
            // Catch any exceptions to prevent glitches
            System.err.println("Error handling key press: " + ex.getMessage());
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        try {
            // Handle quiz text input
            if (showingQuiz && waitingForAnswer) {
                char c = e.getKeyChar();
                if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
                    userInput += c;
                    repaint(); // Immediately show the typed character
                }
            }
        } catch (Exception ex) {
            // Catch any exceptions to prevent glitches
            System.err.println("Error handling key typed: " + ex.getMessage());
        }
    }
}

