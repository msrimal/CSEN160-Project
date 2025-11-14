package com.maya_steph.virusdefense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Test class for QuizManager functionality
 */
public class QuizManagerTest {
    
    private QuizManager quizManager;
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Create a temporary questions.json file for testing
        createTestQuestionFile();
        quizManager = new QuizManager();
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
            writer.write("    },\n");
            writer.write("    {\n");
            writer.write("      \"id\": \"Q2\",\n");
            writer.write("      \"text\": \"Q2\",\n");
            writer.write("      \"answer\": \"abc123\"\n");
            writer.write("    },\n");
            writer.write("    {\n");
            writer.write("      \"id\": \"Q3\",\n");
            writer.write("      \"text\": \"Q3\",\n");
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
    void testQuizManagerInitialization() {
        assertNotNull(quizManager, "QuizManager should be initialized");
        assertTrue(quizManager.getQuestionCount() >= 3, "Should have at least 3 questions (test file or fallback)");
    }
    
    @Test
    void testGetRandomQuestion() {
        QuizManager.Question question = quizManager.getRandomQuestion();
        assertNotNull(question, "Should return a question");
        assertNotNull(question.getId(), "Question should have an ID");
        assertNotNull(question.getText(), "Question should have text");
        assertNotNull(question.getAnswer(), "Question should have an answer");
    }
    
    @Test
    void testQuestionAnswerValidation() {
        QuizManager.Question question = new QuizManager.Question("Q1", "Q1", "abc123");
        
        assertTrue(question.checkAnswer("abc123"), "Correct answer should return true");
        assertFalse(question.checkAnswer("wrong"), "Incorrect answer should return false");
        assertFalse(question.checkAnswer("ABC123"), "Case sensitive - should return false");
        assertFalse(question.checkAnswer(""), "Empty answer should return false");
        assertFalse(question.checkAnswer(null), "Null answer should return false");
    }
    
    @Test
    void testQuestionGetters() {
        QuizManager.Question question = new QuizManager.Question("TEST1", "Test Question", "test_answer");
        
        assertEquals("TEST1", question.getId(), "ID should match");
        assertEquals("Test Question", question.getText(), "Text should match");
        assertEquals("test_answer", question.getAnswer(), "Answer should match");
    }
    
    @Test
    void testMultipleRandomQuestions() {
        // Test that we can get multiple questions (randomness test)
        QuizManager.Question q1 = quizManager.getRandomQuestion();
        QuizManager.Question q2 = quizManager.getRandomQuestion();
        QuizManager.Question q3 = quizManager.getRandomQuestion();
        
        assertNotNull(q1, "First question should not be null");
        assertNotNull(q2, "Second question should not be null");
        assertNotNull(q3, "Third question should not be null");
        
        // Questions are loaded from resources (20 questions) or test file (3 questions)
        // Just verify they have valid answers
        assertNotNull(q1.getAnswer(), "Question should have an answer");
        assertNotNull(q2.getAnswer(), "Question should have an answer");
        assertNotNull(q3.getAnswer(), "Question should have an answer");
    }
    
    @Test
    void testFallbackQuestions() {
        // Test fallback functionality by creating a QuizManager with no file
        File questionsFile = new File("questions.json");
        if (questionsFile.exists()) {
            questionsFile.delete();
        }
        
        QuizManager fallbackManager = new QuizManager();
        // QuizManager loads from resources first, so it will get 20 questions from resources
        assertTrue(fallbackManager.getQuestionCount() >= 20, "Should have questions from resources");
        
        QuizManager.Question question = fallbackManager.getRandomQuestion();
        assertNotNull(question, "Should get a question");
        assertNotNull(question.getAnswer(), "Question should have an answer");
    }
    
    @Test
    void testQuestionCount() {
        int count = quizManager.getQuestionCount();
        assertTrue(count > 0, "Should have at least one question");
        
        // If we successfully loaded our test file, should have 3 questions
        // If fallback was used, should have 20 questions
        assertTrue(count == 3 || count == 20, "Should have either 3 (test file) or 20 (fallback) questions");
    }
    
    @Test
    void testAnswerEdgeCases() {
        QuizManager.Question question = new QuizManager.Question("Q1", "Q1", "abc123");
        
        // Test with spaces - implementation trims, so leading/trailing spaces are ignored
        assertTrue(question.checkAnswer(" abc123 "), "Answer with leading/trailing spaces should be trimmed and match");
        assertFalse(question.checkAnswer("abc 123"), "Answer with middle space should return false");
        
        // Test with different cases
        assertFalse(question.checkAnswer("Abc123"), "Different case should return false");
        assertFalse(question.checkAnswer("ABC123"), "All caps should return false");
    }
}