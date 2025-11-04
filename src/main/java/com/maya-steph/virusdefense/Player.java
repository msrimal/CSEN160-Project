package com.maya_steph.virusdefense;

import java.awt.*;

/**
 * Represents the player (immune cell) that can move between lanes
 */
public class Player {
    private int x;
    private int y;
    private int size;
    
    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.size = 40;
    }
    
    public void update() {
        // Player updates (if needed for animations, etc.)
    }
    
    public void moveLeft(int laneWidth) {
        x = Math.max(laneWidth / 2, x - laneWidth);
    }
    
    public void moveRight(int laneWidth, int screenWidth) {
        x = Math.min(screenWidth - laneWidth / 2, x + laneWidth);
    }
    
    public void draw(Graphics2D g2d) {
        // Draw immune cell as a circle
        g2d.setColor(Color.CYAN);
        g2d.fillOval(x - size / 2, y - size / 2, size, size);
        
        // Draw inner circle for detail
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x - size / 4, y - size / 4, size / 2, size / 2);
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getSize() {
        return size;
    }
}

