package com.maya_steph.virusdefense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.Color;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Virus class
 */
public class VirusTest {
    private Virus virus;
    private static final int INITIAL_X = 300;
    private static final int INITIAL_Y = 0;
    private static final double INITIAL_SPEED = 2.5;
    
    @BeforeEach
    void setUp() {
        virus = new Virus(INITIAL_X, INITIAL_Y, INITIAL_SPEED);
    }
    
    @Test
    void testVirusInitialization() {
        assertEquals(INITIAL_X, virus.getX());
        assertEquals(INITIAL_Y, virus.getY(), 0.001);
        assertEquals(INITIAL_SPEED, virus.getSpeed(), 0.001);
        assertEquals(30, virus.getSize()); // Size is hardcoded to 30
    }
    
    @Test
    void testUpdate() {
        double initialY = virus.getY();
        
        virus.update();
        
        assertEquals(initialY + INITIAL_SPEED, virus.getY(), 0.001);
    }
    
    @Test
    void testUpdateMultipleTimes() {
        double initialY = virus.getY();
        int updates = 5;
        
        for (int i = 0; i < updates; i++) {
            virus.update();
        }
        
        assertEquals(initialY + (INITIAL_SPEED * updates), virus.getY(), 0.001);
    }
    
    @Test
    void testSetSpeed() {
        double newSpeed = 5.0;
        
        virus.setSpeed(newSpeed);
        
        assertEquals(newSpeed, virus.getSpeed(), 0.001);
        
        // Verify update uses new speed
        double initialY = virus.getY();
        virus.update();
        assertEquals(initialY + newSpeed, virus.getY(), 0.001);
    }
    
    @Test
    void testVirusFallsDown() {
        // Verify virus moves down (positive Y direction)
        double initialY = virus.getY();
        virus.update();
        assertTrue(virus.getY() > initialY);
    }
    
    @Test
    void testDifferentSpeeds() {
        Virus slowVirus = new Virus(100, 0, 1.0);
        Virus fastVirus = new Virus(200, 0, 5.0);
        
        slowVirus.update();
        fastVirus.update();
        
        assertTrue(fastVirus.getY() > slowVirus.getY());
    }
    
    @Test
    void testVirusInitialHitCount() {
        assertEquals(0, virus.getHitCount());
        assertFalse(virus.isDead());
    }
    
    @Test
    void testVirusFirstHit() {
        virus.hit();
        
        assertEquals(1, virus.getHitCount());
        assertFalse(virus.isDead());
    }
    
    @Test
    void testVirusSecondHitKillsVirus() {
        virus.hit();
        virus.hit();
        
        assertEquals(2, virus.getHitCount());
        assertTrue(virus.isDead());
    }
    
    @Test
    void testVirusMultipleHits() {
        // Test that virus stays dead after more than 2 hits
        virus.hit();
        virus.hit();
        virus.hit();
        
        assertEquals(3, virus.getHitCount());
        assertTrue(virus.isDead());
    }
    
    @Test
    void testProjectileCollisionDetection() {
        // Create a projectile at the same position as virus
        Weapons.ProjectileBall projectile = new Weapons.ProjectileBall(INITIAL_X, INITIAL_Y);
        
        assertTrue(projectile.collidesWith(virus));
    }
    
    @Test
    void testProjectileNoCollision() {
        // Create a projectile far from virus
        Weapons.ProjectileBall projectile = new Weapons.ProjectileBall(INITIAL_X + 100, INITIAL_Y + 100);
        
        assertFalse(projectile.collidesWith(virus));
    }
    
    @Test
    void testProjectileBoundaryCollision() {
        // Test collision at the edge of virus hitbox
        int virusRadius = virus.getSize() / 2;
        Weapons.ProjectileBall projectile = new Weapons.ProjectileBall(INITIAL_X + virusRadius + 5, INITIAL_Y);
        
        assertTrue(projectile.collidesWith(virus));
    }
    
    @Test
    void testProjectileMovement() {
        Weapons.ProjectileBall projectile = new Weapons.ProjectileBall(100, 100);
        double initialY = projectile.getY();
        
        projectile.update();
        
        assertTrue(projectile.getY() < initialY); // Projectile moves up (negative Y)
    }
    
    @Test
    void testProjectileProperties() {
        Weapons.ProjectileBall projectile = new Weapons.ProjectileBall(150, 200);
        
        assertEquals(150, projectile.getX(), 0.001);
        assertEquals(200, projectile.getY(), 0.001);
        assertEquals(12, projectile.getSize());
    }
}

