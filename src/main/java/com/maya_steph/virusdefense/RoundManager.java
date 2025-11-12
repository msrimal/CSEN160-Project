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
        baseVirusSpeed = 1.0; // Start slower for first round
        speedIncreasePerRound = 0.3; // More gradual speed increase
        virusesSpawnedThisRound = 0;
        virusesPerRound = 3; // Start with only 3 viruses in first round
    }
    
    public void startRound() {
        currentRound = 1;
        virusesSpawnedThisRound = 0;
        virusesPerRound = 3; // Reset to initial value (3 viruses for round 1)
    }
    
    public void virusSpawned() {
        virusesSpawnedThisRound++;
    }
    
    public boolean checkRoundComplete() {
        // Round is complete when all viruses have been spawned
        return virusesSpawnedThisRound >= virusesPerRound;
    }
    
    public void advanceToNextRound() {
        advanceRound();
    }
    
    private void advanceRound() {
        currentRound++;
        virusesSpawnedThisRound = 0;
        
        // Progressive virus count: Round 1=3, Round 2=5, Round 3=7, Round 4=10, Round 5=13, etc.
        if (currentRound <= 3) {
            virusesPerRound = 1 + currentRound * 2; // 3, 5, 7
        } else {
            virusesPerRound = 7 + (currentRound - 3) * 3; // 10, 13, 16, 19...
        }
        
        System.out.println("Advanced to Round " + currentRound + " - " + virusesPerRound + " viruses, speed: " + String.format("%.1f", getVirusSpeed()));
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
    
    public int getVirusesRemaining() {
        return virusesPerRound - virusesSpawnedThisRound;
    }
    
    public int getVirusesPerRound() {
        return virusesPerRound;
    }
    
    public int getVirusesSpawnedThisRound() {
        return virusesSpawnedThisRound;
    }
}

