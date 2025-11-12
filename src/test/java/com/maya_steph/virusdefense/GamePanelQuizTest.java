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

/**
 * Test class for GamePanel quiz integration functionality
 */
public class GamePanelQuizTest {
    
    private GamePanel gamePanel;
    
    @BeforeEach
    void setUp() {
        // Create test questions file
        createTestQuestionFile();
        gamePanel = new GamePanel();
    }
    
    @AfterEach
    void tearDown() {
        // Clean up test file
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
    void testGamePanelInitialization() {
        assertNotNull(gamePanel, "GamePanel should be initialized");
        
        // Use reflection to check if quiz-related fields are initialized
        try {
            Field quizManagerField = GamePanel.class.getDeclaredField("quizManager");
            quizManagerField.setAccessible(true);
            QuizManager quizManager = (QuizManager) quizManagerField.get(gamePanel);
            assertNotNull(quizManager, "QuizManager should be initialized");
            
            Field showingQuizField = GamePanel.class.getDeclaredField("showingQuiz");
            showingQuizField.setAccessible(true);
            boolean showingQuiz = (Boolean) showingQuizField.get(gamePanel);
            assertFalse(showingQuiz, "Should not be showing quiz initially");
            
            Field userInputField = GamePanel.class.getDeclaredField("userInput");
            userInputField.setAccessible(true);
            String userInput = (String) userInputField.get(gamePanel);
            assertEquals("", userInput, "User input should be empty initially");
            
        } catch (Exception e) {
            fail("Failed to access private fields: " + e.getMessage());
        }
    }
    
    @Test
    void testShowQuiz() {
        try {
            // Use reflection to call private showQuiz method
            Method showQuizMethod = GamePanel.class.getDeclaredMethod("showQuiz");
            showQuizMethod.setAccessible(true);
            
            // Start the game first
            gamePanel.startGame();
            
            // Call showQuiz
            showQuizMethod.invoke(gamePanel);
            
            // Check that quiz is now showing
            Field showingQuizField = GamePanel.class.getDeclaredField("showingQuiz");
            showingQuizField.setAccessible(true);
            boolean showingQuiz = (Boolean) showingQuizField.get(gamePanel);
            assertTrue(showingQuiz, "Should be showing quiz after showQuiz() call");
            
            Field waitingForAnswerField = GamePanel.class.getDeclaredField("waitingForAnswer");
            waitingForAnswerField.setAccessible(true);
            boolean waitingForAnswer = (Boolean) waitingForAnswerField.get(gamePanel);
            assertTrue(waitingForAnswer, "Should be waiting for answer");
            
            Field currentQuestionField = GamePanel.class.getDeclaredField("currentQuestion");
            currentQuestionField.setAccessible(true);
            QuizManager.Question currentQuestion = (QuizManager.Question) currentQuestionField.get(gamePanel);
            assertNotNull(currentQuestion, "Should have a current question");
            
        } catch (Exception e) {
            fail("Failed to test showQuiz: " + e.getMessage());
        }
    }
    
    @Test
    void testQuizInputHandling() {
        try {
            // Start game and show quiz
            gamePanel.startGame();
            Method showQuizMethod = GamePanel.class.getDeclaredMethod("showQuiz");
            showQuizMethod.setAccessible(true);
            showQuizMethod.invoke(gamePanel);
            
            // Simulate typing input
            Field userInputField = GamePanel.class.getDeclaredField("userInput");
            userInputField.setAccessible(true);
            
            // Test character input
            KeyEvent charEvent = new KeyEvent(gamePanel, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'a');
            gamePanel.keyTyped(charEvent);
            
            String userInput = (String) userInputField.get(gamePanel);
            assertEquals("a", userInput, "Should add character to user input");
            
            // Test more characters
            charEvent = new KeyEvent(gamePanel, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'b');
            gamePanel.keyTyped(charEvent);
            charEvent = new KeyEvent(gamePanel, KeyEvent.KEY_TYPED, System.currentTimeMillis(), 0, KeyEvent.VK_UNDEFINED, 'c');
            gamePanel.keyTyped(charEvent);
            
            userInput = (String) userInputField.get(gamePanel);
            assertEquals("abc", userInput, "Should accumulate characters");
            
        } catch (Exception e) {
            fail("Failed to test quiz input: " + e.getMessage());
        }
    }
    
    @Test
    void testBackspaceHandling() {
        try {
            // Start game and show quiz
            gamePanel.startGame();
            Method showQuizMethod = GamePanel.class.getDeclaredMethod("showQuiz");
            showQuizMethod.setAccessible(true);
            showQuizMethod.invoke(gamePanel);
            
            // Add some input
            Field userInputField = GamePanel.class.getDeclaredField("userInput");
            userInputField.setAccessible(true);
            userInputField.set(gamePanel, "abc123");
            
            // Test backspace
            KeyEvent backspaceEvent = new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE, KeyEvent.CHAR_UNDEFINED);
            gamePanel.keyPressed(backspaceEvent);
            
            String userInput = (String) userInputField.get(gamePanel);
            assertEquals("abc12", userInput, "Backspace should remove last character");
            
        } catch (Exception e) {
            fail("Failed to test backspace: " + e.getMessage());
        }
    }
    
    @Test
    void testCorrectAnswerSubmission() {
        try {
            // Start game and show quiz
            gamePanel.startGame();
            Method showQuizMethod = GamePanel.class.getDeclaredMethod("showQuiz");
            showQuizMethod.setAccessible(true);
            showQuizMethod.invoke(gamePanel);
            
            // Get initial lives count
            Field livesField = GamePanel.class.getDeclaredField("lives");
            livesField.setAccessible(true);
            int initialLives = (Integer) livesField.get(gamePanel);
            
            // Set correct answer
            Field userInputField = GamePanel.class.getDeclaredField("userInput");
            userInputField.setAccessible(true);
            userInputField.set(gamePanel, "abc123");
            
            // Submit answer with Enter key
            KeyEvent enterEvent = new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
            gamePanel.keyPressed(enterEvent);
            
            // Check that lives didn't decrease
            int currentLives = (Integer) livesField.get(gamePanel);
            assertEquals(initialLives, currentLives, "Lives should not decrease for correct answer");
            
            // Check that quiz is no longer showing
            Field showingQuizField = GamePanel.class.getDeclaredField("showingQuiz");
            showingQuizField.setAccessible(true);
            boolean showingQuiz = (Boolean) showingQuizField.get(gamePanel);
            assertFalse(showingQuiz, "Should not be showing quiz after correct answer");
            
        } catch (Exception e) {
            fail("Failed to test correct answer: " + e.getMessage());
        }
    }
    
    @Test
    void testIncorrectAnswerSubmission() {
        try {
            // Start game and show quiz
            gamePanel.startGame();
            Method showQuizMethod = GamePanel.class.getDeclaredMethod("showQuiz");
            showQuizMethod.setAccessible(true);
            showQuizMethod.invoke(gamePanel);
            
            // Get initial lives count
            Field livesField = GamePanel.class.getDeclaredField("lives");
            livesField.setAccessible(true);
            int initialLives = (Integer) livesField.get(gamePanel);
            
            // Set incorrect answer
            Field userInputField = GamePanel.class.getDeclaredField("userInput");
            userInputField.setAccessible(true);
            userInputField.set(gamePanel, "wrong_answer");
            
            // Submit answer with Enter key
            KeyEvent enterEvent = new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED);
            gamePanel.keyPressed(enterEvent);
            
            // Check that lives decreased by 1
            int currentLives = (Integer) livesField.get(gamePanel);
            assertEquals(initialLives - 1, currentLives, "Lives should decrease by 1 for incorrect answer");
            
            // Check that quiz is no longer showing
            Field showingQuizField = GamePanel.class.getDeclaredField("showingQuiz");
            showingQuizField.setAccessible(true);
            boolean showingQuiz = (Boolean) showingQuizField.get(gamePanel);
            assertFalse(showingQuiz, "Should not be showing quiz after incorrect answer");
            
        } catch (Exception e) {
            fail("Failed to test incorrect answer: " + e.getMessage());
        }
    }
    
    @Test
    void testQuizDoesNotShowWhenGameNotRunning() {
        try {
            // Don't start the game
            Method showQuizMethod = GamePanel.class.getDeclaredMethod("showQuiz");
            showQuizMethod.setAccessible(true);
            showQuizMethod.invoke(gamePanel);
            
            // Check that quiz is not showing
            Field showingQuizField = GamePanel.class.getDeclaredField("showingQuiz");
            showingQuizField.setAccessible(true);
            boolean showingQuiz = (Boolean) showingQuizField.get(gamePanel);
            assertFalse(showingQuiz, "Quiz should not show when game is not running");
            
        } catch (Exception e) {
            fail("Failed to test quiz not showing: " + e.getMessage());
        }
    }
    
    @Test
    void testGameInputBlockedDuringQuiz() {
        try {
            // Start game and show quiz
            gamePanel.startGame();
            Method showQuizMethod = GamePanel.class.getDeclaredMethod("showQuiz");
            showQuizMethod.setAccessible(true);
            showQuizMethod.invoke(gamePanel);
            
            // Try to use game controls (should be blocked)
            KeyEvent spaceEvent = new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_SPACE, ' ');
            KeyEvent leftEvent = new KeyEvent(gamePanel, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED);
            
            // These should not cause any errors and should be ignored
            assertDoesNotThrow(() -> gamePanel.keyPressed(spaceEvent), "Game controls should be ignored during quiz");
            assertDoesNotThrow(() -> gamePanel.keyPressed(leftEvent), "Game controls should be ignored during quiz");
            
        } catch (Exception e) {
            fail("Failed to test game input blocking: " + e.getMessage());
        }
    }
}