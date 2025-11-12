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
    
    // New round flash effect
    private float flashLevel;
    private Color flashColor;
    private boolean flashActive;
    private int flashFramesRemaining;

    public OverlayEffect() {
        darknessLevel = 0.0f;
        targetDarkness = 0.6f; //target darkness when triggered
        fadeSpeed = 0.05f;
        isDarkening = false;
        fading = false;
        
        // Initialize flash effect
        flashLevel = 0.0f;
        flashColor = Color.GREEN;
        flashActive = false;
        flashFramesRemaining = 0;
    }
    public void darkerAfterLoss(){
        darknessLevel += 0.2f;
    }
    
    public void triggerDarken() {
        isDarkening = true;
        fading = false;
    }
    
    public void triggerLifeLossFlash() {
        flashActive = true;
        flashLevel = 0.7f; // Strong flash
        flashColor = Color.RED; // Red for life loss
        flashFramesRemaining = 15; // Flash for about 1/4 second at 60fps
    }
    
    public void triggerNewRoundFlash() {
        flashActive = true;
        flashLevel = 0.8f; // Bright flash
        flashColor = Color.GREEN; // Green for positive/new round
        flashFramesRemaining = 20; // Flash for about 1/3 second at 60fps
    }

    public void update() {
        // Update darkness effect (life loss)
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
        
        // Update flash effect (new round)
        if (flashActive) {
            flashFramesRemaining--;
            if (flashFramesRemaining <= 0) {
                flashActive = false;
                flashLevel = 0.0f;
            } else {
                // Fade out flash over remaining frames
                flashLevel = (float) flashFramesRemaining / 20.0f * 0.6f; // Max 0.6 alpha
            }
        }
    }

    
    public void draw(Graphics2D g2d, int width, int height) {
        // Draw darkness effect (life loss)
        if (darknessLevel > 0.0f) {
            Color overlayColor = new Color(0, 0, 0, darknessLevel);
            g2d.setColor(overlayColor);
            g2d.fillRect(0, 0, width, height);
        }
        
        // Draw flash effect (new round)
        if (flashActive && flashLevel > 0.0f) {
            int red = flashColor.getRed();
            int green = flashColor.getGreen();
            int blue = flashColor.getBlue();
            Color flashOverlay = new Color(red, green, blue, (int)(flashLevel * 255));
            g2d.setColor(flashOverlay);
            g2d.fillRect(0, 0, width, height);
        }
    }
    
    public void reset() {
        darknessLevel = 0.0f;
        fading = false;
        isDarkening = false;
        flashActive = false;
        flashLevel = 0.0f;
        flashFramesRemaining = 0;
    }
    
    public float getDarknessLevel() {
        return darknessLevel;
    }
}

