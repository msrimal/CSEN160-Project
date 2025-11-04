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
        assertEquals(2.0, roundManager.getBaseVirusSpeed(), 0.001);
        assertEquals(2.0, roundManager.getVirusSpeed(), 0.001);
    }
    
    @Test
    void testStartRound() {
        roundManager.setCurrentRound(5);
        roundManager.startRound();
        
        assertEquals(1, roundManager.getCurrentRound());
    }
    
    @Test
    void testVirusSpeedIncreasesWithRound() {
        // Round 1
        assertEquals(2.0, roundManager.getVirusSpeed(), 0.001);
        
        // Round 2
        roundManager.setCurrentRound(2);
        assertEquals(2.5, roundManager.getVirusSpeed(), 0.001);
        
        // Round 3
        roundManager.setCurrentRound(3);
        assertEquals(3.0, roundManager.getVirusSpeed(), 0.001);
        
        // Round 5
        roundManager.setCurrentRound(5);
        assertEquals(4.0, roundManager.getVirusSpeed(), 0.001);
    }
    
    @Test
    void testVirusSpawned() {
        // Spawn viruses but don't reach threshold
        for (int i = 0; i < 9; i++) {
            roundManager.virusSpawned();
        }
        
        // Should still be round 1
        assertEquals(1, roundManager.getCurrentRound());
    }
    
    @Test
    void testRoundAdvancement() {
        // Spawn 10 viruses (default threshold for round 1)
        for (int i = 0; i < 10; i++) {
            roundManager.virusSpawned();
        }
        
        // Should advance to round 2
        assertEquals(2, roundManager.getCurrentRound());
    }
    
    @Test
    void testMultipleRoundAdvancements() {
        // Advance to round 2
        for (int i = 0; i < 10; i++) {
            roundManager.virusSpawned();
        }
        assertEquals(2, roundManager.getCurrentRound());
        
        // Advance to round 3 (needs 12 viruses: 10 + 2*2)
        for (int i = 0; i < 12; i++) {
            roundManager.virusSpawned();
        }
        assertEquals(3, roundManager.getCurrentRound());
    }
    
    @Test
    void testSetBaseVirusSpeed() {
        roundManager.setBaseVirusSpeed(3.0);
        
        assertEquals(3.0, roundManager.getBaseVirusSpeed(), 0.001);
        assertEquals(3.0, roundManager.getVirusSpeed(), 0.001);
        
        // Verify speed increases from new base
        roundManager.setCurrentRound(2);
        assertEquals(3.5, roundManager.getVirusSpeed(), 0.001);
    }
    
    @Test
    void testSetCurrentRound() {
        roundManager.setCurrentRound(5);
        assertEquals(5, roundManager.getCurrentRound());
        
        // Speed should reflect round 5
        assertEquals(4.0, roundManager.getVirusSpeed(), 0.001);
    }
}

