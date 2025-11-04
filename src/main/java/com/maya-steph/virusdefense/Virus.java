package com.maya_steph.virusdefense;

import java.awt.*;

/**
 * Represents a virus that falls from the top
 */
public class Virus {
    private int x;
    private double y;
    private double speed;
    private int size;
    private Color color;
    
    public Virus(int x, int y, double speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.size = 30;
        this.color = Color.RED;
    }
    
    public void update() {
        y += speed;
    }
    
    public void draw(Graphics2D g2d) {
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
}

