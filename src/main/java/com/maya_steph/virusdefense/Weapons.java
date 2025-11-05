package com.maya_steph.virusdefense;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Weapons {
    //declaring WeaponType enumeration
    public enum WeaponType {
        BALL("Ball"),
        STAR("Star"),
        SPIKY_BALL("Spiky Ball"),
        ARROW("Arrow");

        private final String displayName;

        WeaponType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    //variable declarations
    private ArrayList<ProjectileBall> projectiles;
    private long lastShotTime;
    private static final long SHOT_COOLDOWN = 250; // milliseconds between shots
    private WeaponType currentWeapon;

    //constructor
    public Weapons() {
        this.projectiles = new ArrayList<>(); //stores the bullets/projectiles that are currently on screen, and they are removed or addeded in update()
        this.lastShotTime = 0;
        this.currentWeapon = WeaponType.BALL;
    }

    public void shoot(int playerX, int playerY) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime >= SHOT_COOLDOWN) {
            projectiles.add(new ProjectileBall(playerX, playerY - 30, currentWeapon));
            lastShotTime = currentTime;
        }
    }

    public void switchWeapon() {
        WeaponType[] weapons = WeaponType.values(); //returns an array of enums constants in order of the way they were declared
        int currentIndex = currentWeapon.ordinal(); //get current position
        currentWeapon = weapons[(currentIndex + 1) % weapons.length];
    }

    public WeaponType getCurrentWeapon() {
        return currentWeapon;
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

    //CLASS PROJECTILEBALL
    public static class ProjectileBall {
        private double x;
        private double y;
        private double speed;
        private int size;
        private Color color;
        private WeaponType weaponType;

        public ProjectileBall(int startX, int startY, WeaponType weaponType) {
            this.x = startX;
            this.y = startY;
            this.speed = 8.0;
            this.weaponType = weaponType;

            switch (weaponType) {
                case BALL:
                    this.size = 12;
                    this.color = Color.YELLOW;
                    break;
                case STAR:
                    this.size = 16;
                    this.color = Color.CYAN;
                    break;
                case SPIKY_BALL:
                    this.size = 14;
                    this.color = Color.MAGENTA;
                    break;
                case ARROW:
                    this.size = 18;
                    this.color = Color.WHITE;
                    break;
            }
        }

        // Backward compatibility constructor
        public ProjectileBall(int startX, int startY) {
            this(startX, startY, WeaponType.BALL);
        }

        public void update() {
            y -= speed;
        }

        public void draw(Graphics2D g2d) {
            switch (weaponType) {
                case BALL:
                    drawBall(g2d);
                    break;
                case STAR:
                    drawStar(g2d);
                    break;
                case SPIKY_BALL:
                    drawSpikyBall(g2d);
                    break;
                case ARROW:
                    drawArrow(g2d);
                    break;
            }
        }

        private void drawBall(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.fillOval((int)(x - size / 2), (int)(y - size / 2), size, size);

            // Add glow effect
            g2d.setColor(new Color(255, 255, 0, 100));
            g2d.fillOval((int)(x - size / 2 - 2), (int)(y - size / 2 - 2), size + 4, size + 4);
        }

        private void drawStar(Graphics2D g2d) {
            g2d.setColor(color);
            int centerX = (int)x;
            int centerY = (int)y;
            int radius = size / 2;

            // Create star shape with 5 points
            int[] starX = new int[10];
            int[] starY = new int[10];

            for (int i = 0; i < 10; i++) {
                double angle = Math.PI * i / 5.0;
                int r = (i % 2 == 0) ? radius : radius / 2;
                starX[i] = centerX + (int)(r * Math.cos(angle - Math.PI / 2));
                starY[i] = centerY + (int)(r * Math.sin(angle - Math.PI / 2));
            }

            g2d.fillPolygon(starX, starY, 10);

            // Add glow effect
            g2d.setColor(new Color(0, 255, 255, 100));
            g2d.fillPolygon(starX, starY, 10);
        }

        private void drawSpikyBall(Graphics2D g2d) {
            g2d.setColor(color);
            int centerX = (int)x;
            int centerY = (int)y;
            int radius = size / 2;

            // Draw main ball
            g2d.fillOval(centerX - radius, centerY - radius, size, size);

            // Draw spikes around the ball
            g2d.setColor(color.darker());
            int spikeCount = 8;
            int spikeLength = radius / 2;

            for (int i = 0; i < spikeCount; i++) {
                double angle = 2 * Math.PI * i / spikeCount;
                int baseX = centerX + (int)(radius * Math.cos(angle));
                int baseY = centerY + (int)(radius * Math.sin(angle));
                int tipX = centerX + (int)((radius + spikeLength) * Math.cos(angle));
                int tipY = centerY + (int)((radius + spikeLength) * Math.sin(angle));

                g2d.drawLine(baseX, baseY, tipX, tipY);
                g2d.fillOval(tipX - 2, tipY - 2, 4, 4);
            }
        }

        private void drawArrow(Graphics2D g2d) {
            g2d.setColor(color);
            int centerX = (int)x;
            int centerY = (int)y;
            int length = size;

            // Arrow body (vertical line)
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(centerX, centerY + length/3, centerX, centerY - length/3);

            // Arrow head (triangle)
            int[] arrowX = {centerX, centerX - length/4, centerX + length/4};
            int[] arrowY = {centerY - length/2, centerY - length/4, centerY - length/4};
            g2d.fillPolygon(arrowX, arrowY, 3);

            // Arrow tail (small line)
            g2d.drawLine(centerX - length/6, centerY + length/4, centerX + length/6, centerY + length/4);

            // Reset stroke
            g2d.setStroke(new BasicStroke(1));
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