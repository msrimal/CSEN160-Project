package com.maya_steph.virusdefense;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Weapons {
    private ArrayList<ProjectileBall> projectiles;
    private long lastShotTime;
    private static final long SHOT_COOLDOWN = 250; // milliseconds between shots
    
    public Weapons() {
        this.projectiles = new ArrayList<>();
        this.lastShotTime = 0;
    }
    
    public void shoot(int playerX, int playerY) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime >= SHOT_COOLDOWN) {
            projectiles.add(new ProjectileBall(playerX, playerY - 30));
            lastShotTime = currentTime;
        }
    }
    
    public void update() {
        Iterator<ProjectileBall> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            ProjectileBall projectile = iterator.next();
            projectile.update();
            
            // Remove projectiles that are off screen
            if (projectile.getY() < 0) {
                iterator.remove();
            }
        }
    }
    
    public void draw(Graphics2D g2d) {
        for (ProjectileBall projectile : projectiles) {
            projectile.draw(g2d);
        }
    }
    
    public ArrayList<ProjectileBall> getProjectiles() {
        return projectiles;
    }
    
    public void removeProjectile(ProjectileBall projectile) {
        projectiles.remove(projectile);
    }
    
    public static class ProjectileBall {
        private double x;
        private double y;
        private double speed;
        private int size;
        private Color color;
        
        public ProjectileBall(int startX, int startY) {
            this.x = startX;
            this.y = startY;
            this.speed = 8.0;
            this.size = 12;
            this.color = Color.YELLOW;
        }
        
        public void update() {
            y -= speed;
        }
        
        public void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.fillOval((int)(x - size / 2), (int)(y - size / 2), size, size);
            
            // Add glow effect
            g2d.setColor(new Color(255, 255, 0, 100));
            g2d.fillOval((int)(x - size / 2 - 2), (int)(y - size / 2 - 2), size + 4, size + 4);
        }
        
        public double getX() {
            return x;
        }
        
        public double getY() {
            return y;
        }
        
        public int getSize() {
            return size;
        }
        
        public boolean collidesWith(Virus virus) {
            double dx = x - virus.getX();
            double dy = y - virus.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            return distance < (size / 2 + virus.getSize() / 2);
        }
    }
}