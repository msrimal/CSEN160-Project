package com.maya_steph.virusdefense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RoundManager class
 */
public class RoundManagerTest {
    private RoundManager roundManager;
    
    @BeforeEach
    void setUp() {
        roundManager = new RoundManager();
    }
    
    @Test
    void testInitialState() {
        assertEquals(1, roundManager.getCurrentRound());
        assertEquals(1.0, roundManager.getBaseVirusSpeed(), 0.001);
        assertEquals(1.0, roundManager.getVirusSpeed(), 0.001);
    }
    
    @Test
    void testStartRound() {
        roundManager.setCurrentRound(5);
        roundManager.startRound();
        
        assertEquals(1, roundManager.getCurrentRound());
    }
    
    @Test
    void testVirusSpeedIncreasesWithRound() {
        // Round 1: baseSpeed (1.0) + (1-1) * 0.3 = 1.0
        assertEquals(1.0, roundManager.getVirusSpeed(), 0.001);
        
        // Round 2: baseSpeed (1.0) + (2-1) * 0.3 = 1.3
        roundManager.setCurrentRound(2);
        assertEquals(1.3, roundManager.getVirusSpeed(), 0.001);
        
        // Round 3: baseSpeed (1.0) + (3-1) * 0.3 = 1.6
        roundManager.setCurrentRound(3);
        assertEquals(1.6, roundManager.getVirusSpeed(), 0.001);
        
        // Round 5: baseSpeed (1.0) + (5-1) * 0.3 = 2.2
        roundManager.setCurrentRound(5);
        assertEquals(2.2, roundManager.getVirusSpeed(), 0.001);
    }
    
    @Test
    void testVirusSpawned() {
        // Spawn viruses but don't reach threshold (threshold is 3 for round 1)
        for (int i = 0; i < 2; i++) {
            roundManager.virusSpawned();
        }
        
        // Should still be round 1
        assertEquals(1, roundManager.getCurrentRound());
        assertFalse(roundManager.checkRoundComplete());
    }
    
    @Test
    void testRoundAdvancement() {
        // Spawn 3 viruses (default threshold for round 1)
        for (int i = 0; i < 3; i++) {
            roundManager.virusSpawned();
        }
        
        // Should advance to round 2 (but only if advanceToNextRound is called)
        // Note: virusSpawned() doesn't auto-advance, it just increments the counter
        assertTrue(roundManager.checkRoundComplete());
        roundManager.advanceToNextRound();
        assertEquals(2, roundManager.getCurrentRound());
    }
    
    @Test
    void testMultipleRoundAdvancements() {
        // Advance to round 2 (needs 3 viruses for round 1)
        for (int i = 0; i < 3; i++) {
            roundManager.virusSpawned();
        }
        roundManager.advanceToNextRound();
        assertEquals(2, roundManager.getCurrentRound());
        
        // Advance to round 3 (needs 5 viruses for round 2)
        for (int i = 0; i < 5; i++) {
            roundManager.virusSpawned();
        }
        roundManager.advanceToNextRound();
        assertEquals(3, roundManager.getCurrentRound());
    }
    
    @Test
    void testSetBaseVirusSpeed() {
        roundManager.setBaseVirusSpeed(3.0);
        
        assertEquals(3.0, roundManager.getBaseVirusSpeed(), 0.001);
        assertEquals(3.0, roundManager.getVirusSpeed(), 0.001);
        
        // Verify speed increases from new base: 3.0 + (2-1) * 0.3 = 3.3
        roundManager.setCurrentRound(2);
        assertEquals(3.3, roundManager.getVirusSpeed(), 0.001);
    }
    
    @Test
    void testSetCurrentRound() {
        roundManager.setCurrentRound(5);
        assertEquals(5, roundManager.getCurrentRound());
        
        // Speed should reflect round 5: 1.0 + (5-1) * 0.3 = 2.2
        assertEquals(2.2, roundManager.getVirusSpeed(), 0.001);
    }
}

