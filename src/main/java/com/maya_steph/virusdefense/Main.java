package com.maya_steph.virusdefense;

import javax.swing.*;
import java.awt.Component;

/**
 * Main entry point for Virus Defense game
 */
public class Main {
    public static void main(String[] args) {
        // Set up uncaught exception handler to catch any crashes
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            System.err.println("Uncaught exception in thread " + thread.getName() + ":");
            exception.printStackTrace();
        });
        
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Virus Defense");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setResizable(false);
                
                GamePanel gamePanel = new GamePanel();
                frame.add(gamePanel);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                
                // Don't start game immediately - wait for Enter key on home screen
                // Request focus after frame is visible (GamePanel is already setFocusable(true))
                SwingUtilities.invokeLater(() -> {
                    try {
                        if (gamePanel instanceof Component) {
                            ((Component) gamePanel).requestFocus();
                        }
                    } catch (Exception e) {
                        System.err.println("Error requesting focus: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                System.err.println("Error initializing game: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}

