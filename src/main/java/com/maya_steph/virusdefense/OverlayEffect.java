package com.maya_steph.virusdefense;

import java.awt.*;

/**
 * Manages screen overlay effects like darkening when player loses a life
 */
public class OverlayEffect {
    private float darknessLevel;
    private final float targetDarkness;
    private final float fadeSpeed;
    private boolean fading;
    private boolean isDarkening;

    public OverlayEffect() {
        darknessLevel = 0.0f;
        targetDarkness = 0.6f; //target darkness when triggered
        fadeSpeed = 0.05f;
        isDarkening = false;
        fading = false;
    }
    public void darkerAfterLoss(){
        darknessLevel += 0.2f;
    }
    
    public void triggerDarken() {
        isDarkening = true;
        fading = false;
    }

    public void update() {
        if (isDarkening) {
            darknessLevel = Math.min(darknessLevel + fadeSpeed, targetDarkness);
            if (darknessLevel >= targetDarkness) {
                isDarkening = false;
                fading = true;
            }
        } else if (fading) {
            darknessLevel = Math.max(darknessLevel - fadeSpeed, 0.0f);
            if (darknessLevel <= 0.0f) {
                fading = false;
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
        fading = false;
        isDarkening = false;
    }
    
    public float getDarknessLevel() {
        return darknessLevel;
    }
}

