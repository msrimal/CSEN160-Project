package com.maya_steph.virusdefense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Integration test for quiz timer functionality and life system
 */
public class QuizTimerIntegrationTest {
    
    private GamePanel gamePanel;
    private CountDownLatch quizLatch;
    
    @BeforeEach
    void setUp() {
        createTestQuestionFile();
        gamePanel = new GamePanel();
        quizLatch = new CountDownLatch(1);
    }
    
    @AfterEach
    void tearDown() {
        File questionsFile = new File("questions.json");
        if (questionsFile.exists()) {
            questionsFile.delete();
        }
    }
    
    private void createTestQuestionFile() {
        try {
            File questionsFile = new File("questions.json");
            FileWriter writer = new FileWriter(questionsFile);
            writer.write("{\n");
            writer.write("  \"questions\": [\n");
            writer.write("    {\n");
            writer.write("      \"id\": \"Q1\",\n");
            writer.write("      \"text\": \"Q1\",\n");
            writer.write("      \"answer\": \"abc123\"\n");
            writer.write("    }\n");
            writer.write("  ]\n");
            writer.write("}\n");
            writer.close();
        } catch (IOException e) {
            fail("Failed to create test question file: " + e.getMessage());
        }
    }
    
    @Test
    void testQuizTimerScheduling() {
        try {
            gamePanel.startGame();
            
            // Check that quiz timer is created and started
            Field quizTimerField = GamePanel.class.getDeclaredField("quizTimer");
            quizTimerField.setAccessible(true);
            Timer quizTimer = (Timer) quizTimerField.get(gamePanel);
            
            assertNotNull(quizTimer, "Quiz timer should be created");
            assertTrue(quizTimer.isRunning(), "Quiz timer should be running after game starts");
            assertFalse(quizTimer.isRepeats(), "Quiz timer should not repeat (single shot)");
            
            // Check that delay is within expected range (35-60 seconds = 35000-60000 ms)
            int delay = quizTimer.getDelay();
            assertTrue(delay >= 35000 && delay <= 60000, 
                "Quiz timer delay should be between 35-60 seconds, was: " + delay + "ms");
            
        } catch (Exception e) {
            fail("Failed to test quiz timer scheduling: " + e.getMessage());
        }
    }
    
    @Test
    void testQuizTimerRescheduling() {
        try {
            gamePanel.startGame();
            
            // Trigger quiz manually
            Method showQuizMethod = GamePanel.class.getDeclaredMethod("showQuiz");
            showQuizMethod.setAccessible(true);
            showQuizMethod.invoke(gamePanel);
            
            // Answer correctly
            Field userInputField = GamePanel.class.getDeclaredField("userInput");
            userInputField.setAccessible(true);
            userInputField.set(gamePanel, "abc123");
            
            KeyEvent enterEvent = new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
            gamePanel.keyPressed(enterEvent);
            
            // Check that a new timer is scheduled
            Field quizTimerField = GamePanel.class.getDeclaredField("quizTimer");
            quizTimerField.setAccessible(true);
            Timer newQuizTimer = (Timer) quizTimerField.get(gamePanel);
            
            assertNotNull(newQuizTimer, "New quiz timer should be scheduled after answering");
            assertTrue(newQuizTimer.isRunning(), "New quiz timer should be running");
            
        } catch (Exception e) {
            fail("Failed to test quiz timer rescheduling: " + e.getMessage());
        }
    }
    
    @Test
    void testLifeLossOnWrongAnswer() {
        try {
            gamePanel.startGame();
            
            // Get initial lives
            Field livesField = GamePanel.class.getDeclaredField("lives");
            livesField.setAccessible(true);
            int initialLives = (Integer) livesField.get(gamePanel);
            assertEquals(3, initialLives, "Game should start with 3 lives");
            
            // Show quiz manually
            Method showQuizMethod = GamePanel.class.getDeclaredMethod("showQuiz");
            showQuizMethod.setAccessible(true);
            showQuizMethod.invoke(gamePanel);
            
            // Give wrong answer
            Field userInputField = GamePanel.class.getDeclaredField("userInput");
            userInputField.setAccessible(true);
            userInputField.set(gamePanel, "wrong_answer");
            
            // Submit answer
            KeyEvent enterEvent = new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
            gamePanel.keyPressed(enterEvent);
            
            // Check lives decreased
            int currentLives = (Integer) livesField.get(gamePanel);
            assertEquals(2, currentLives, "Lives should decrease by 1 for wrong answer");
            
        } catch (Exception e) {
            fail("Failed to test life loss: " + e.getMessage());
        }
    }
    
    @Test
    void testMultipleWrongAnswersLeadToGameOver() {
        try {
            gamePanel.startGame();
            
            Field livesField = GamePanel.class.getDeclaredField("lives");
            livesField.setAccessible(true);
            Field gameOverField = GamePanel.class.getDeclaredField("gameOver");
            gameOverField.setAccessible(true);
            
            Method showQuizMethod = GamePanel.class.getDeclaredMethod("showQuiz");
            showQuizMethod.setAccessible(true);
            Field userInputField = GamePanel.class.getDeclaredField("userInput");
            userInputField.setAccessible(true);
            
            // Answer wrong 3 times
            for (int i = 0; i < 3; i++) {
                showQuizMethod.invoke(gamePanel);
                userInputField.set(gamePanel, "wrong_answer_" + i);
                
                KeyEvent enterEvent = new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, 
                    System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
                gamePanel.keyPressed(enterEvent);
                
                // Manually trigger hideQuizAndResume to clear quiz state for next iteration
                Method hideQuizAndResumeMethod = GamePanel.class.getDeclaredMethod("hideQuizAndResume");
                hideQuizAndResumeMethod.setAccessible(true);
                hideQuizAndResumeMethod.invoke(gamePanel);
                
                int currentLives = (Integer) livesField.get(gamePanel);
                assertEquals(2 - i, currentLives, "Lives should decrease after wrong answer " + (i + 1));
            }
            
            // Game should be over
            boolean gameOver = (Boolean) gameOverField.get(gamePanel);
            assertTrue(gameOver, "Game should be over after 3 wrong answers");
            
        } catch (Exception e) {
            fail("Failed to test game over: " + e.getMessage());
        }
    }
    
    @Test
    void testNoLifeLossOnCorrectAnswer() {
        try {
            gamePanel.startGame();
            
            Field livesField = GamePanel.class.getDeclaredField("lives");
            livesField.setAccessible(true);
            int initialLives = (Integer) livesField.get(gamePanel);
            
            // Show quiz and answer correctly multiple times
            Method showQuizMethod = GamePanel.class.getDeclaredMethod("showQuiz");
            showQuizMethod.setAccessible(true);
            Field userInputField = GamePanel.class.getDeclaredField("userInput");
            userInputField.setAccessible(true);
            Field currentQuestionField = GamePanel.class.getDeclaredField("currentQuestion");
            currentQuestionField.setAccessible(true);
            
            for (int i = 0; i < 5; i++) {
                showQuizMethod.invoke(gamePanel);
                
                // Get the current question's correct answer
                QuizManager.Question currentQuestion = (QuizManager.Question) currentQuestionField.get(gamePanel);
                assertNotNull(currentQuestion, "Should have a current question");
                String correctAnswer = currentQuestion.getAnswer();
                
                userInputField.set(gamePanel, correctAnswer);
                
                KeyEvent enterEvent = new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, 
                    System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
                gamePanel.keyPressed(enterEvent);
                
                int currentLives = (Integer) livesField.get(gamePanel);
                assertEquals(initialLives, currentLives, "Lives should not change for correct answers");
            }
            
        } catch (Exception e) {
            fail("Failed to test no life loss on correct answer: " + e.getMessage());
        }
    }
    
    @Test
    void testGamePauseAndResumeWithQuiz() {
        try {
            gamePanel.startGame();
            
            Field gameTimerField = GamePanel.class.getDeclaredField("gameTimer");
            gameTimerField.setAccessible(true);
            Timer gameTimer = (Timer) gameTimerField.get(gamePanel);
            
            Field virusSpawnTimerField = GamePanel.class.getDeclaredField("virusSpawnTimer");
            virusSpawnTimerField.setAccessible(true);
            Timer virusSpawnTimer = (Timer) virusSpawnTimerField.get(gamePanel);
            
            assertTrue(gameTimer.isRunning(), "Game timer should be running initially");
            assertTrue(virusSpawnTimer.isRunning(), "Virus spawn timer should be running initially");
            
            // Show quiz
            Method showQuizMethod = GamePanel.class.getDeclaredMethod("showQuiz");
            showQuizMethod.setAccessible(true);
            showQuizMethod.invoke(gamePanel);
            
            // Timers should be stopped
            assertFalse(gameTimer.isRunning(), "Game timer should be stopped during quiz");
            assertFalse(virusSpawnTimer.isRunning(), "Virus spawn timer should be stopped during quiz");
            
            // Answer quiz
            Field userInputField = GamePanel.class.getDeclaredField("userInput");
            userInputField.setAccessible(true);
            userInputField.set(gamePanel, "abc123");
            
            KeyEvent enterEvent = new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, 
                System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
            gamePanel.keyPressed(enterEvent);
            
            // Manually trigger hideQuizAndResume to simulate timer completion
            Method hideQuizAndResumeMethod = GamePanel.class.getDeclaredMethod("hideQuizAndResume");
            hideQuizAndResumeMethod.setAccessible(true);
            hideQuizAndResumeMethod.invoke(gamePanel);
            
            // Timers should resume after quiz is hidden
            assertTrue(gameTimer.isRunning(), "Game timer should resume after quiz");
            assertTrue(virusSpawnTimer.isRunning(), "Virus spawn timer should resume after quiz");
            
        } catch (Exception e) {
            fail("Failed to test game pause/resume: " + e.getMessage());
        }
    }
    
    @Test
    void testQuizNotShownWhenAlreadyShowing() {
        try {
            gamePanel.startGame();
            
            Method showQuizMethod = GamePanel.class.getDeclaredMethod("showQuiz");
            showQuizMethod.setAccessible(true);
            
            Field showingQuizField = GamePanel.class.getDeclaredField("showingQuiz");
            showingQuizField.setAccessible(true);
            Field currentQuestionField = GamePanel.class.getDeclaredField("currentQuestion");
            currentQuestionField.setAccessible(true);
            
            // Show first quiz
            showQuizMethod.invoke(gamePanel);
            assertTrue((Boolean) showingQuizField.get(gamePanel), "Should be showing quiz");
            QuizManager.Question firstQuestion = (QuizManager.Question) currentQuestionField.get(gamePanel);
            assertNotNull(firstQuestion, "Should have a question");
            
            // Try to show another quiz while first is still showing
            showQuizMethod.invoke(gamePanel);
            QuizManager.Question secondQuestion = (QuizManager.Question) currentQuestionField.get(gamePanel);
            
            // Should be the same question (no change)
            assertEquals(firstQuestion, secondQuestion, "Should not change question when quiz already showing");
            
        } catch (Exception e) {
            fail("Failed to test quiz not shown when already showing: " + e.getMessage());
        }
    }
    
    @Test
    void testRandomTimerIntervals() {
        // Test that scheduleNextQuiz creates different intervals
        try {
            Method scheduleNextQuizMethod = GamePanel.class.getDeclaredMethod("scheduleNextQuiz");
            scheduleNextQuizMethod.setAccessible(true);
            Field quizTimerField = GamePanel.class.getDeclaredField("quizTimer");
            quizTimerField.setAccessible(true);
            
            int[] delays = new int[10];
            for (int i = 0; i < 10; i++) {
                scheduleNextQuizMethod.invoke(gamePanel);
                Timer timer = (Timer) quizTimerField.get(gamePanel);
                delays[i] = timer.getDelay();
                timer.stop(); // Stop to allow next scheduling
            }
            
            // Check all delays are in valid range
            for (int delay : delays) {
                assertTrue(delay >= 35000 && delay <= 60000, 
                    "All delays should be between 35-60 seconds, found: " + delay);
            }
            
            // Check for some variation (not all the same)
            boolean hasVariation = false;
            for (int i = 1; i < delays.length; i++) {
                if (delays[i] != delays[0]) {
                    hasVariation = true;
                    break;
                }
            }
            assertTrue(hasVariation, "Timer intervals should show some random variation");
            
        } catch (Exception e) {
            fail("Failed to test random timer intervals: " + e.getMessage());
        }
    }
}