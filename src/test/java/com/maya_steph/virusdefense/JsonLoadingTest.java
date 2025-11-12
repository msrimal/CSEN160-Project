package com.maya_steph.virusdefense;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Test class specifically for JSON loading functionality
 */
public class JsonLoadingTest {
    
    private File testFile;
    
    @BeforeEach
    void setUp() {
        // Clean up any existing test files
        testFile = new File("questions.json");
        if (testFile.exists()) {
            testFile.delete();
        }
    }
    
    @AfterEach
    void tearDown() {
        // Clean up test files
        if (testFile != null && testFile.exists()) {
            testFile.delete();
        }
    }
    
    @Test
    void testValidJsonLoading() {
        createValidJsonFile();
        
        QuizManager manager = new QuizManager();
        assertEquals(5, manager.getQuestionCount(), "Should load 5 questions from valid JSON");
        
        QuizManager.Question question = manager.getRandomQuestion();
        assertNotNull(question, "Should get a valid question");
        assertEquals("testAnswer", question.getAnswer(), "All questions should have 'testAnswer' as answer");
    }
    
    @Test
    void testMalformedJsonHandling() {
        createMalformedJsonFile();
        
        QuizManager manager = new QuizManager();
        assertEquals(20, manager.getQuestionCount(), "Should fall back to 20 default questions when JSON is malformed");
        
        QuizManager.Question question = manager.getRandomQuestion();
        assertEquals("abc123", question.getAnswer(), "Fallback questions should have 'abc123' as answer");
    }
    
    @Test
    void testMissingJsonFile() {
        // Don't create any file
        QuizManager manager = new QuizManager();
        assertEquals(20, manager.getQuestionCount(), "Should create 20 fallback questions when file is missing");
        
        QuizManager.Question question = manager.getRandomQuestion();
        assertEquals("abc123", question.getAnswer(), "Fallback questions should have 'abc123' as answer");
        assertTrue(question.getText().startsWith("Q"), "Fallback question text should start with 'Q'");
    }
    
    @Test
    void testEmptyJsonArray() {
        createEmptyJsonArrayFile();
        
        QuizManager manager = new QuizManager();
        assertEquals(20, manager.getQuestionCount(), "Should fall back when JSON array is empty");
    }
    
    @Test
    void testJsonWithMissingFields() {
        createJsonWithMissingFields();
        
        // This should either load partially or fall back completely
        QuizManager manager = new QuizManager();
        assertTrue(manager.getQuestionCount() >= 1, "Should handle missing fields gracefully");
        
        QuizManager.Question question = manager.getRandomQuestion();
        assertNotNull(question, "Should still be able to get a question");
        assertNotNull(question.getAnswer(), "Question should have an answer");
    }
    
    @Test
    void testLargeJsonFile() {
        createLargeJsonFile(50);
        
        QuizManager manager = new QuizManager();
        assertEquals(50, manager.getQuestionCount(), "Should load all 50 questions from large file");
        
        // Test multiple random selections
        for (int i = 0; i < 10; i++) {
            QuizManager.Question question = manager.getRandomQuestion();
            assertNotNull(question, "Should get valid questions from large file");
            assertTrue(question.getId().matches("Q\\d+"), "Question ID should match pattern Q[number]");
        }
    }
    
    @Test
    void testJsonWithSpecialCharacters() {
        createJsonWithSpecialCharacters();
        
        QuizManager manager = new QuizManager();
        assertTrue(manager.getQuestionCount() > 0, "Should handle special characters");
        
        QuizManager.Question question = manager.getRandomQuestion();
        assertNotNull(question, "Should get question with special characters");
    }
    
    @Test
    void testJsonFilePermissions() {
        createValidJsonFile();
        
        // Make file unreadable (this might not work on all systems)
        if (testFile.setReadable(false)) {
            QuizManager manager = new QuizManager();
            assertEquals(20, manager.getQuestionCount(), "Should fall back when file is unreadable");
            
            // Restore permissions for cleanup
            testFile.setReadable(true);
        }
    }
    
    // Helper methods for creating test files
    
    private void createValidJsonFile() {
        try {
            FileWriter writer = new FileWriter(testFile);
            writer.write("{\n");
            writer.write("  \"questions\": [\n");
            for (int i = 1; i <= 5; i++) {
                writer.write("    {\n");
                writer.write("      \"id\": \"Q" + i + "\",\n");
                writer.write("      \"text\": \"Question " + i + "\",\n");
                writer.write("      \"answer\": \"testAnswer\"\n");
                writer.write("    }");
                if (i < 5) writer.write(",");
                writer.write("\n");
            }
            writer.write("  ]\n");
            writer.write("}\n");
            writer.close();
        } catch (IOException e) {
            fail("Failed to create valid JSON file: " + e.getMessage());
        }
    }
    
    private void createMalformedJsonFile() {
        try {
            FileWriter writer = new FileWriter(testFile);
            writer.write("{\n");
            writer.write("  \"questions\": [\n");
            writer.write("    {\n");
            writer.write("      \"id\": \"Q1\",\n");
            writer.write("      \"text\": \"Question 1\"\n");  // Missing comma and answer field
            writer.write("    }\n");
            writer.write("    {\n");  // Missing comma between objects
            writer.write("      \"id\": \"Q2\",\n");
            writer.write("      \"text\": \"Question 2\",\n");
            writer.write("      \"answer\": \"answer2\"\n");
            writer.write("    }\n");
            writer.write("  ]\n");
            // Missing closing brace
            writer.close();
        } catch (IOException e) {
            fail("Failed to create malformed JSON file: " + e.getMessage());
        }
    }
    
    private void createEmptyJsonArrayFile() {
        try {
            FileWriter writer = new FileWriter(testFile);
            writer.write("{\n");
            writer.write("  \"questions\": []\n");
            writer.write("}\n");
            writer.close();
        } catch (IOException e) {
            fail("Failed to create empty JSON array file: " + e.getMessage());
        }
    }
    
    private void createJsonWithMissingFields() {
        try {
            FileWriter writer = new FileWriter(testFile);
            writer.write("{\n");
            writer.write("  \"questions\": [\n");
            writer.write("    {\n");
            writer.write("      \"id\": \"Q1\",\n");
            writer.write("      \"answer\": \"answer1\"\n");  // Missing text field
            writer.write("    },\n");
            writer.write("    {\n");
            writer.write("      \"text\": \"Question 2\",\n");  // Missing id and answer
            writer.write("    },\n");
            writer.write("    {\n");
            writer.write("      \"id\": \"Q3\",\n");
            writer.write("      \"text\": \"Question 3\",\n");
            writer.write("      \"answer\": \"answer3\"\n");  // Complete question
            writer.write("    }\n");
            writer.write("  ]\n");
            writer.write("}\n");
            writer.close();
        } catch (IOException e) {
            fail("Failed to create JSON with missing fields: " + e.getMessage());
        }
    }
    
    private void createLargeJsonFile(int questionCount) {
        try {
            FileWriter writer = new FileWriter(testFile);
            writer.write("{\n");
            writer.write("  \"questions\": [\n");
            for (int i = 1; i <= questionCount; i++) {
                writer.write("    {\n");
                writer.write("      \"id\": \"Q" + i + "\",\n");
                writer.write("      \"text\": \"Large file question " + i + "\",\n");
                writer.write("      \"answer\": \"answer" + i + "\"\n");
                writer.write("    }");
                if (i < questionCount) writer.write(",");
                writer.write("\n");
            }
            writer.write("  ]\n");
            writer.write("}\n");
            writer.close();
        } catch (IOException e) {
            fail("Failed to create large JSON file: " + e.getMessage());
        }
    }
    
    private void createJsonWithSpecialCharacters() {
        try {
            FileWriter writer = new FileWriter(testFile);
            writer.write("{\n");
            writer.write("  \"questions\": [\n");
            writer.write("    {\n");
            writer.write("      \"id\": \"Q1\",\n");
            writer.write("      \"text\": \"Question with special chars: @#$%^&*()!\",\n");
            writer.write("      \"answer\": \"special@answer#123\"\n");
            writer.write("    },\n");
            writer.write("    {\n");
            writer.write("      \"id\": \"Q2\",\n");
            writer.write("      \"text\": \"Question with unicode: ñáéíóú\",\n");
            writer.write("      \"answer\": \"unicode_answer\"\n");
            writer.write("    }\n");
            writer.write("  ]\n");
            writer.write("}\n");
            writer.close();
        } catch (IOException e) {
            fail("Failed to create JSON with special characters: " + e.getMessage());
        }
    }
}