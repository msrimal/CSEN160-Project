package com.maya_steph.virusdefense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Virus class
 */
public class VirusTest {
    private Virus virus;
    private static final int INITIAL_X = 300;
    private static final int INITIAL_Y = 0;
    private static final double INITIAL_SPEED = 2.5;
    
    @BeforeEach
    void setUp() {
        virus = new Virus(INITIAL_X, INITIAL_Y, INITIAL_SPEED);
    }
    
    @Test
    void testVirusInitialization() {
        assertEquals(INITIAL_X, virus.getX());
        assertEquals(INITIAL_Y, virus.getY(), 0.001);
        assertEquals(INITIAL_SPEED, virus.getSpeed(), 0.001);
        assertEquals(30, virus.getSize()); // Size is hardcoded to 30
    }
    
    @Test
    void testUpdate() {
        double initialY = virus.getY();
        
        virus.update();
        
        assertEquals(initialY + INITIAL_SPEED, virus.getY(), 0.001);
    }
    
    @Test
    void testUpdateMultipleTimes() {
        double initialY = virus.getY();
        int updates = 5;
        
        for (int i = 0; i < updates; i++) {
            virus.update();
        }
        
        assertEquals(initialY + (INITIAL_SPEED * updates), virus.getY(), 0.001);
    }
    
    @Test
    void testSetSpeed() {
        double newSpeed = 5.0;
        
        virus.setSpeed(newSpeed);
        
        assertEquals(newSpeed, virus.getSpeed(), 0.001);
        
        // Verify update uses new speed
        double initialY = virus.getY();
        virus.update();
        assertEquals(initialY + newSpeed, virus.getY(), 0.001);
    }
    
    @Test
    void testVirusFallsDown() {
        // Verify virus moves down (positive Y direction)
        double initialY = virus.getY();
        virus.update();
        assertTrue(virus.getY() > initialY);
    }
    
    @Test
    void testDifferentSpeeds() {
        Virus slowVirus = new Virus(100, 0, 1.0);
        Virus fastVirus = new Virus(200, 0, 5.0);
        
        slowVirus.update();
        fastVirus.update();
        
        assertTrue(fastVirus.getY() > slowVirus.getY());
    }
}

