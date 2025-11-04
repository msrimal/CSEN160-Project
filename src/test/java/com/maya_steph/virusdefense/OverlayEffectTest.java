package com.maya_steph.virusdefense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for OverlayEffect class
 */
public class OverlayEffectTest {
    private OverlayEffect overlay;
    
    @BeforeEach
    void setUp() {
        overlay = new OverlayEffect();
    }
    
    @Test
    void testInitialState() {
        assertEquals(0.0f, overlay.getDarknessLevel(), 0.001f);
    }
    
    @Test
    void testTriggerDarken() {
        overlay.triggerDarken();
        
        // Should start darkening process
        assertTrue(overlay.getDarknessLevel() >= 0.0f);
    }
    
    @Test
    void testDarknessIncreasesOnUpdate() {
        overlay.triggerDarken();
        float initialDarkness = overlay.getDarknessLevel();
        
        overlay.update();
        
        // Darkness should increase
        assertTrue(overlay.getDarknessLevel() >= initialDarkness);
    }
    
    @Test
    void testDarknessReachesTarget() {
        overlay.triggerDarken();
        
        // Update many times to reach target
        for (int i = 0; i < 20; i++) {
            overlay.update();
        }
        
        // Should reach target darkness (0.6f)
        assertTrue(overlay.getDarknessLevel() >= 0.5f);
    }
    
    @Test
    void testDarknessFadesBack() {
        overlay.triggerDarken();
        
        // Darken to target
        for (int i = 0; i < 20; i++) {
            overlay.update();
        }
        
        float maxDarkness = overlay.getDarknessLevel();
        
        // Continue updating to fade back
        for (int i = 0; i < 20; i++) {
            overlay.update();
        }
        
        // Should fade back towards 0
        assertTrue(overlay.getDarknessLevel() < maxDarkness);
    }
    
    @Test
    void testReset() {
        overlay.triggerDarken();
        
        // Update to add some darkness
        for (int i = 0; i < 10; i++) {
            overlay.update();
        }
        
        overlay.reset();
        
        assertEquals(0.0f, overlay.getDarknessLevel(), 0.001f);
    }
    
    @Test
    void testMultipleTriggers() {
        overlay.triggerDarken();
        overlay.update();
        overlay.update();
        
        float darknessAfterTwo = overlay.getDarknessLevel();
        
        // Trigger again
        overlay.triggerDarken();
        overlay.update();
        
        // Should continue darkening
        assertTrue(overlay.getDarknessLevel() >= darknessAfterTwo);
    }
    
    @Test
    void testDarknessStaysInRange() {
        overlay.triggerDarken();
        
        // Update many times
        for (int i = 0; i < 100; i++) {
            overlay.update();
            float darkness = overlay.getDarknessLevel();
            assertTrue(darkness >= 0.0f && darkness <= 1.0f);
        }
    }
}

