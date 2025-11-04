package com.maya_steph.virusdefense;

import java.awt.*;

/**
 * Manages screen overlay effects like darkening when player loses a life
 */
public class OverlayEffect {
    private float darknessLevel;
    private float targetDarkness;
    private float fadeSpeed;
    private boolean isDarkening;
    
    public OverlayEffect() {
        darknessLevel = 0.0f;
        targetDarkness = 0.0f;
        fadeSpeed = 0.05f;
        isDarkening = false;
    }
    
    public void triggerDarken() {
        targetDarkness = 0.6f; // 60% darkness
        isDarkening = true;
    }
    
    public void update() {
        if (isDarkening) {
            // Fade to dark
            if (darknessLevel < targetDarkness) {
                darknessLevel = Math.min(darknessLevel + fadeSpeed, targetDarkness);
            } else {
                // Fade back to normal
                darknessLevel = Math.max(darknessLevel - fadeSpeed, 0.0f);
                if (darknessLevel <= 0.0f) {
                    isDarkening = false;
                    targetDarkness = 0.0f;
                }
            }
        }
    }
    
    public void draw(Graphics2D g2d, int width, int height) {
        if (darknessLevel > 0.0f) {
            // Draw semi-transparent black overlay
            Color overlayColor = new Color(0, 0, 0, darknessLevel);
            g2d.setColor(overlayColor);
            g2d.fillRect(0, 0, width, height);
        }
    }
    
    public void reset() {
        darknessLevel = 0.0f;
        targetDarkness = 0.0f;
        isDarkening = false;
    }
    
    public float getDarknessLevel() {
        return darknessLevel;
    }
}

