package com.maya_steph.virusdefense;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Manages quiz questions loaded from JSON file
 */
public class QuizManager {
    private ArrayList<Question> questions;
    private Random random;
    
    public static class Question {
        private String id;
        private String text;
        private String answer;
        
        public Question(String id, String text, String answer) {
            this.id = id;
            this.text = text;
            this.answer = answer;
        }
        
        public String getId() { return id; }
        public String getText() { return text; }
        public String getAnswer() { return answer; }
        
        public boolean checkAnswer(String userAnswer) {
            if (userAnswer == null) return false;
            return answer.equals(userAnswer.trim());
        }
    }
    
    public QuizManager() {
        questions = new ArrayList<>();
        random = new Random();
        loadQuestions();
    }
    
    private void loadQuestions() {
        JSONParser parser = new JSONParser();
        String[] possiblePaths = {
            "questions.json",
            "./questions.json",
            "../questions.json",
            "../../questions.json"
        };
        
        boolean loaded = false;
        for (String path : possiblePaths) {
            try {
                System.out.println("Trying to load questions from: " + path);
                Object obj = parser.parse(new FileReader(path));
                JSONObject jsonObject = (JSONObject) obj;
                JSONArray questionArray = (JSONArray) jsonObject.get("questions");
                
                if (questionArray != null) {
                    for (Object questionObj : questionArray) {
                        JSONObject question = (JSONObject) questionObj;
                        String id = (String) question.get("id");
                        String text = (String) question.get("text");
                        String answer = (String) question.get("answer");
                        if (id != null && text != null && answer != null) {
                            questions.add(new Question(id, text, answer));
                        }
                    }
                    System.out.println("Successfully loaded " + questions.size() + " questions from " + path);
                    loaded = true;
                    break;
                }
            } catch (IOException | ParseException e) {
                System.out.println("Could not load from " + path + ": " + e.getMessage());
            }
        }
        
        if (!loaded || questions.isEmpty()) {
            System.out.println("Could not load questions file, using fallback questions");
            addFallbackQuestions();
        }
    }
    
    private void addFallbackQuestions() {
        for (int i = 1; i <= 20; i++) {
            questions.add(new Question("Q" + i, "Q" + i, "abc123"));
        }
    }
    
    public Question getRandomQuestion() {
        if (questions.isEmpty()) {
            return new Question("Q1", "Q1", "abc123");
        }
        return questions.get(random.nextInt(questions.size()));
    }
    
    public int getQuestionCount() {
        return questions.size();
    }
}