package com.maya_steph.virusdefense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for game components working together
 */
public class GameIntegrationTest {
    private Player player;
    private RoundManager roundManager;
    private OverlayEffect overlay;
    
    @BeforeEach
    void setUp() {
        player = new Player(300, 700);
        roundManager = new RoundManager();
        overlay = new OverlayEffect();
    }
    
    @Test
    void testPlayerMovementWithLanes() {
        int laneWidth = 200;
        int screenWidth = 600;
        
        // Move player across all 3 lanes
        int initialX = player.getX();
        player.moveRight(laneWidth, screenWidth);
        assertEquals(initialX + laneWidth, player.getX());
        
        player.moveRight(laneWidth, screenWidth);
        assertEquals(initialX + (laneWidth * 2), player.getX());
        
        // Move back left
        player.moveLeft(laneWidth);
        assertEquals(initialX + laneWidth, player.getX());
    }
    
    @Test
    void testVirusSpeedMatchesRoundManager() {
        // Create virus with speed from round manager
        double speed = roundManager.getVirusSpeed();
        Virus virus = new Virus(300, 0, speed);
        
        assertEquals(roundManager.getVirusSpeed(), virus.getSpeed(), 0.001);
        
        // Advance round and verify speed increases
        roundManager.setCurrentRound(2);
        virus.setSpeed(roundManager.getVirusSpeed());
        assertTrue(virus.getSpeed() > speed);
    }
    
    @Test
    void testRoundProgressionWithVirusSpeed() {
        double initialSpeed = roundManager.getVirusSpeed();
        
        // Advance rounds
        for (int round = 1; round <= 5; round++) {
            roundManager.setCurrentRound(round);
            double currentSpeed = roundManager.getVirusSpeed();
            assertTrue(currentSpeed >= initialSpeed);
            assertTrue(currentSpeed == 2.0 + (round - 1) * 0.5);
        }
    }
    
    @Test
    void testLifeLossTriggersOverlay() {
        // Simulate life loss
        overlay.triggerDarken();
        
        // Overlay should start darkening
        assertTrue(overlay.getDarknessLevel() >= 0.0f);
        
        // Update overlay
        for (int i = 0; i < 5; i++) {
            overlay.update();
        }
        
        // Should have some darkness
        assertTrue(overlay.getDarknessLevel() > 0.0f);
    }
    
    @Test
    void testGameReset() {
        // Set up game state
        player.moveRight(200, 600);
        roundManager.setCurrentRound(3);
        overlay.triggerDarken();
        
        // Reset overlay
        overlay.reset();
        assertEquals(0.0f, overlay.getDarknessLevel(), 0.001f);
        
        // Reset round
        roundManager.startRound();
        assertEquals(1, roundManager.getCurrentRound());
    }
    
    @Test
    void testVirusFallsCorrectly() {
        Virus virus = new Virus(300, 0, 2.5);
        
        // Update multiple times
        for (int i = 0; i < 10; i++) {
            virus.update();
        }
        
        // Virus should have moved down
        assertTrue(virus.getY() > 0);
        assertEquals(25.0, virus.getY(), 0.001); // 10 updates * 2.5 speed
    }
    
    @Test
    void testPlayerStaysInBounds() {
        int laneWidth = 200;
        int screenWidth = 600;
        
        // Move to extreme positions
        for (int i = 0; i < 10; i++) {
            player.moveLeft(laneWidth);
        }
        assertTrue(player.getX() >= laneWidth / 2);
        
        for (int i = 0; i < 10; i++) {
            player.moveRight(laneWidth, screenWidth);
        }
        assertTrue(player.getX() <= screenWidth - laneWidth / 2);
    }
}

