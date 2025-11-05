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
    private boolean gameRunning;
    private boolean gameOver;
    
    private int lives = 3;
    
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
        
        gameTimer = new Timer(16, this); // ~60 FPS
        virusSpawnTimer = new Timer(2000, e -> spawnVirus()); // Spawn every 2 seconds initially
    }
    
    public void startGame() {
        gameRunning = true;
        gameOver = false;
        lives = 3;
        viruses.clear();
        roundManager.startRound();
        overlay.reset();
        gameTimer.start();
        virusSpawnTimer.start();
        requestFocus();
    }
    
    private void spawnVirus() {
        if (!gameRunning || gameOver) return;
        
        int lane = random.nextInt(LANE_COUNT);
        int x = lane * LANE_WIDTH + LANE_WIDTH / 2;
        double speed = roundManager.getVirusSpeed();
        viruses.add(new Virus(x, 0, speed));
        roundManager.virusSpawned();
        
        // Adjust spawn timer based on round (faster spawns in later rounds)
        int baseInterval = 2000;
        int newInterval = Math.max(500, baseInterval - (roundManager.getCurrentRound() - 1) * 100);
        virusSpawnTimer.setDelay(newInterval);
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
    }
    
    private void loseLife() {
        lives--;
        overlay.triggerDarken();
        playSound("life_lost"); // Placeholder for sound effect
        
        if (lives <= 0) {
            gameOver = true;
            gameTimer.stop();
            virusSpawnTimer.stop();
            playSound("game_over"); // Placeholder for sound effect
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
                    // Hit the virus
                    virus.hit();
                    projectileIterator.remove();
                    playSound("hit");
                    
                    // Check if virus is dead
                    if (virus.isDead()) {
                        virusIterator.remove();
                        playSound("virus_destroyed");
                    }
                    break; // Projectile can only hit one virus
                }
            }
        }
    }
    
    private void playSound(String soundName) {
        // Placeholder for sound effects
        System.out.println("Playing sound: " + soundName);
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
        
        // Draw game over screen
        if (gameOver) {
            drawGameOver(g2d);
        }
    }
    
    private void drawUI(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        
        // Draw lives
        g2d.drawString("Lives: " + lives, 10, 30);
        
        // Draw round info
        g2d.drawString("Round: " + roundManager.getCurrentRound(), 10, 60);
        g2d.drawString("Speed: " + String.format("%.1f", roundManager.getVirusSpeed()), 10, 90);
    }
    
    private void drawGameOver(Graphics2D g2d) {
        // Semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Game Over text
        g2d.setColor(Color.RED);
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
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        if (gameOver && key == KeyEvent.VK_R) {
            startGame();
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
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        // Not used
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
}

