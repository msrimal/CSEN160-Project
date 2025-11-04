package com.maya_steph.virusdefense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Player class
 */
public class PlayerTest {
    private Player player;
    private static final int INITIAL_X = 300;
    private static final int INITIAL_Y = 700;
    
    @BeforeEach
    void setUp() {
        player = new Player(INITIAL_X, INITIAL_Y);
    }
    
    @Test
    void testPlayerInitialization() {
        assertEquals(INITIAL_X, player.getX());
        assertEquals(INITIAL_Y, player.getY());
        assertEquals(40, player.getSize());
    }
    
    @Test
    void testMoveLeft() {
        int laneWidth = 200;
        int initialX = player.getX();
        
        player.moveLeft(laneWidth);
        
        assertEquals(initialX - laneWidth, player.getX());
    }
    
    @Test
    void testMoveLeftBoundary() {
        int laneWidth = 200;
        // Move to left edge
        player.moveLeft(laneWidth);
        player.moveLeft(laneWidth);
        player.moveLeft(laneWidth);
        
        // Should not go below laneWidth / 2
        int leftBoundary = laneWidth / 2;
        assertTrue(player.getX() >= leftBoundary);
    }
    
    @Test
    void testMoveRight() {
        int laneWidth = 200;
        int screenWidth = 600;
        int initialX = player.getX();
        
        player.moveRight(laneWidth, screenWidth);
        
        assertEquals(initialX + laneWidth, player.getX());
    }
    
    @Test
    void testMoveRightBoundary() {
        int laneWidth = 200;
        int screenWidth = 600;
        int rightBoundary = screenWidth - laneWidth / 2;
        
        // Move to right edge
        player.moveRight(laneWidth, screenWidth);
        player.moveRight(laneWidth, screenWidth);
        player.moveRight(laneWidth, screenWidth);
        
        // Should not exceed right boundary
        assertTrue(player.getX() <= rightBoundary);
    }
    
    @Test
    void testUpdate() {
        // Update should not crash
        assertDoesNotThrow(() -> player.update());
        // Position should remain unchanged
        assertEquals(INITIAL_X, player.getX());
        assertEquals(INITIAL_Y, player.getY());
    }
}

