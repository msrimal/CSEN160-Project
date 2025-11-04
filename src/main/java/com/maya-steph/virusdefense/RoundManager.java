package com.maya_steph.virusdefense;

/**
 * Manages game rounds and difficulty progression
 */
public class RoundManager {
    private int currentRound;
    private double baseVirusSpeed;
    private double speedIncreasePerRound;
    private int virusesSpawnedThisRound;
    private int virusesPerRound;
    
    public RoundManager() {
        currentRound = 1;
        baseVirusSpeed = 2.0;
        speedIncreasePerRound = 0.5;
        virusesSpawnedThisRound = 0;
        virusesPerRound = 10; // Start with 10 viruses per round
    }
    
    public void startRound() {
        currentRound = 1;
        virusesSpawnedThisRound = 0;
    }
    
    public void virusSpawned() {
        virusesSpawnedThisRound++;
        
        // Check if round should advance
        if (virusesSpawnedThisRound >= virusesPerRound) {
            advanceRound();
        }
    }
    
    private void advanceRound() {
        currentRound++;
        virusesSpawnedThisRound = 0;
        virusesPerRound = (int) (10 + currentRound * 2); // Increase viruses per round
    }
    
    public double getVirusSpeed() {
        return baseVirusSpeed + (currentRound - 1) * speedIncreasePerRound;
    }
    
    public int getCurrentRound() {
        return currentRound;
    }
    
    public void setCurrentRound(int round) {
        this.currentRound = round;
    }
    
    public double getBaseVirusSpeed() {
        return baseVirusSpeed;
    }
    
    public void setBaseVirusSpeed(double speed) {
        this.baseVirusSpeed = speed;
    }
}

