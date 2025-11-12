package com.maya_steph.virusdefense;

import java.awt.*;

/**
 * Represents a virus that falls from the top
 */
public class Virus {
    public enum VirusType {
        SPIKY_VIRUS("Spiky", Color.RED, Weapons.WeaponType.SPIKY_BALL),
        ROUND_VIRUS("Round", Color.GREEN, Weapons.WeaponType.BALL),
        STAR_VIRUS("Star", Color.BLUE, Weapons.WeaponType.STAR),
        ARROW_VIRUS("Arrow", Color.ORANGE, Weapons.WeaponType.ARROW);
        
        private final String displayName;
        private final Color baseColor;
        private final Weapons.WeaponType weakness;
        
        VirusType(String displayName, Color baseColor, Weapons.WeaponType weakness) {
            this.displayName = displayName;
            this.baseColor = baseColor;
            this.weakness = weakness;
        }
        
        public String getDisplayName() { return displayName; }
        public Color getBaseColor() { return baseColor; }
        public Weapons.WeaponType getWeakness() { return weakness; }
    }
    
    private int x;
    private double y;
    private double speed;
    private int size;
    private Color color;
    private int hitCount;
    private VirusType virusType;
    
    public Virus(int x, int y, double speed) {
        this(x, y, speed, VirusType.SPIKY_VIRUS); // Default to spiky for backwards compatibility
    }
    
    public Virus(int x, int y, double speed, VirusType virusType) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.size = 30;
        this.virusType = virusType;
        this.hitCount = 0;
        updateColorBasedOnDamage(); // Set initial color based on damage state
    }
    
    public void update() {
        y += speed;
    }
    
    public void draw(Graphics2D g2d) {
        switch (virusType) {
            case SPIKY_VIRUS:
                drawSpikyVirus(g2d);
                break;
            case ROUND_VIRUS:
                drawRoundVirus(g2d);
                break;
            case STAR_VIRUS:
                drawStarVirus(g2d);
                break;
            case ARROW_VIRUS:
                drawArrowVirus(g2d);
                break;
        }
    }
    
    private void drawSpikyVirus(Graphics2D g2d) {
        // Draw virus as a circle with spikes
        g2d.setColor(color);
        g2d.fillOval(x - size / 2, (int)y - size / 2, size, size);
        
        // Draw spikes (simple triangles)
        int spikeLength = size / 4;
        g2d.setColor(color.darker());
        
        // Top spike
        int[] xPoints = {x, x - spikeLength / 2, x + spikeLength / 2};
        int[] yPoints = {(int)y - size / 2, (int)y - size / 2 - spikeLength, (int)y - size / 2 - spikeLength};
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // Bottom spike
        yPoints = new int[]{(int)y + size / 2, (int)y + size / 2 + spikeLength, (int)y + size / 2 + spikeLength};
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // Left spike
        xPoints = new int[]{x - size / 2, x - size / 2 - spikeLength, x - size / 2 - spikeLength};
        yPoints = new int[]{(int)y, (int)y - spikeLength / 2, (int)y + spikeLength / 2};
        g2d.fillPolygon(xPoints, yPoints, 3);
        
        // Right spike
        xPoints = new int[]{x + size / 2, x + size / 2 + spikeLength, x + size / 2 + spikeLength};
        g2d.fillPolygon(xPoints, yPoints, 3);
    }
    
    private void drawRoundVirus(Graphics2D g2d) {
        // Simple round virus
        g2d.setColor(color);
        g2d.fillOval(x - size / 2, (int)y - size / 2, size, size);
        
        // Add concentric circles for texture
        g2d.setColor(color.brighter());
        g2d.fillOval(x - size / 3, (int)y - size / 3, size * 2 / 3, size * 2 / 3);
        g2d.setColor(color.darker());
        g2d.drawOval(x - size / 2, (int)y - size / 2, size, size);
        g2d.drawOval(x - size / 3, (int)y - size / 3, size * 2 / 3, size * 2 / 3);
    }
    
    private void drawStarVirus(Graphics2D g2d) {
        g2d.setColor(color);
        int centerX = x;
        int centerY = (int)y;
        int radius = size / 2;
        
        // Create star shape with 6 points
        int[] starX = new int[12];
        int[] starY = new int[12];
        
        for (int i = 0; i < 12; i++) {
            double angle = Math.PI * i / 6.0;
            int r = (i % 2 == 0) ? radius : radius / 2;
            starX[i] = centerX + (int)(r * Math.cos(angle - Math.PI / 2));
            starY[i] = centerY + (int)(r * Math.sin(angle - Math.PI / 2));
        }
        
        g2d.fillPolygon(starX, starY, 12);
        
        // Add outline
        g2d.setColor(color.darker());
        g2d.drawPolygon(starX, starY, 12);
    }
    
    private void drawArrowVirus(Graphics2D g2d) {
        g2d.setColor(color);
        int centerX = x;
        int centerY = (int)y;
        int halfSize = size / 2;
        
        // Arrow pointing down
        // Arrow head (triangle at bottom)
        int[] headX = {centerX - halfSize, centerX, centerX + halfSize};
        int[] headY = {centerY, centerY + halfSize, centerY};
        g2d.fillPolygon(headX, headY, 3);
        
        // Arrow body (rectangle)
        int bodyWidth = size / 4;
        g2d.fillRect(centerX - bodyWidth / 2, centerY - halfSize, bodyWidth, halfSize);
        
        // Arrow tail (small triangle at top)
        int[] tailX = {centerX - bodyWidth / 2, centerX, centerX + bodyWidth / 2};
        int[] tailY = {centerY - halfSize, centerY - halfSize - size / 6, centerY - halfSize};
        g2d.fillPolygon(tailX, tailY, 3);
        
        // Add outline
        g2d.setColor(color.darker());
        g2d.drawPolygon(headX, headY, 3);
        g2d.drawRect(centerX - bodyWidth / 2, centerY - halfSize, bodyWidth, halfSize);
        g2d.drawPolygon(tailX, tailY, 3);
    }
    
    public int getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    // Claude implemented function
    public int getSize() {
        return size;
    }
    
    public void hit() {
        hitCount++;
        updateColorBasedOnDamage();
    }
    
    private void updateColorBasedOnDamage() {
        Color baseColor = virusType.getBaseColor();
        
        if (hitCount == 0) {
            // No damage - original color
            color = baseColor;
        } else if (hitCount == 1) {
            // First hit - lighter version of original color
            color = lightenColor(baseColor, 0.6f); // 60% lighter
        } else {
            // Dead or nearly dead - very light version
            color = lightenColor(baseColor, 0.8f); // 80% lighter
        }
    }
    
    private Color lightenColor(Color original, float factor) {
        // Convert to RGB values
        int red = original.getRed();
        int green = original.getGreen();
        int blue = original.getBlue();
        
        // Lighten by moving towards white (255)
        red = (int) (red + (255 - red) * factor);
        green = (int) (green + (255 - green) * factor);
        blue = (int) (blue + (255 - blue) * factor);
        
        // Ensure values stay within 0-255 range
        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));
        
        return new Color(red, green, blue);
    }
    
    public boolean hitWithWeapon(Weapons.WeaponType weaponType) {
        // Only effective if weapon matches virus weakness
        if (weaponType == virusType.getWeakness()) {
            hit();
            return true; // Effective hit
        }
        return false; // Ineffective hit
    }
    
    public boolean isDead() {
        return hitCount >= 2;
    }
    
    public int getHitCount() {
        return hitCount;
    }
    
    public VirusType getVirusType() {
        return virusType;
    }
    
    public Weapons.WeaponType getWeakness() {
        return virusType.getWeakness();
    }
}

