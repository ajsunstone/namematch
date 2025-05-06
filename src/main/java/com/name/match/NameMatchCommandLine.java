package com.name.match;

import com.name.match.service.NameMatchService;
import java.util.*;

public class NameMatchCommandLine {
    public static void main(String[] args) {
        // Check if we should run the predefined test cases or use command line arguments

        runPredefinedTestCases();
        /*
        if (args.length > 0 && args[0].equals("--test")) {
            runPredefinedTestCases();
        } else if (args.length == 2) {
            // Run a single test with command line arguments
            String name1 = args[0];
            String name2 = args[1];
            
            NameMatchService service = new NameMatchService();
            var result = service.mainFunction(name1, name2);
            
            System.out.println("Match: " + result.get("match"));
            System.out.println("Score: " + result.get("match_score"));
        } else {
            System.out.println("Usage:");
            System.out.println("  java NameMatchCommandLine <name1> <name2>");
            System.out.println("  java NameMatchCommandLine --test");
            System.exit(1);
        }*/
    }
    
    private static void runPredefinedTestCases() {
        // Define 20 test cases with name pairs and descriptions
        List<Map<String, String>> testCases = new ArrayList<>();
        
        // Test case 1: Exact match
        addTestCase(testCases, "John Doe", "John Doe", "Exact match");
        
        // Test case 2: Case difference
        addTestCase(testCases, "Jhon Doe", "john doe", "phonetic difference");
        
        // Test case 3: Space removal
        addTestCase(testCases, "John Doe", "JohnDoe", "Space removal");
        
        // Test case 4: All caps vs lowercase
        addTestCase(testCases, "JOHN DOE", "john doe", "All caps vs lowercase");
        
        // Test case 5: Multiple spaces
        addTestCase(testCases, "John Doe", "John  Doe", "Multiple spaces");
        
        // Test case 6: Minor spelling variation
        addTestCase(testCases, "John Doe", "Jon Doe", "Minor spelling variation");
        
        // Test case 7: Different last name
        addTestCase(testCases, "John Doe", "John Smith", "Different last name");
        
        // Test case 8: First and last name swapped
        addTestCase(testCases, "John Doe", "Doe John", "First and last name swapped");
        
        // Test case 9: Middle initial missing in second
        addTestCase(testCases, "John M Doe", "John Doe", "Middle initial missing in second");
        
        // Test case 10: Middle initial missing in first
        addTestCase(testCases, "John Doe", "John M Doe", "Middle initial missing in first");
        
        // Test case 11: Full middle name vs initial
        addTestCase(testCases, "John Michael Doe", "John M Doe", "Full middle name vs initial");
        
        // Test case 12: Abbreviated first name
        addTestCase(testCases, "J. Doe", "John Doe", "Abbreviated first name");
        
        // Test case 13: Nickname
        addTestCase(testCases, "William Smith", "Bill Smith", "Common nickname");
        
        // Test case 14: Special character
        addTestCase(testCases, "John O'Brien", "John OBrien", "Apostrophe vs without");
        
        // Test case 15: Hyphenated vs space
        addTestCase(testCases, "Mary-Jane Smith", "Mary Jane Smith", "Hyphenated vs space in first name");
        
        // Test case 16: International name
        addTestCase(testCases, "José García", "Jose Garcia", "Spanish name with/without accents");
        
        // Test case 17: Very different names
        addTestCase(testCases, "Robert Johnson", "Sarah Williams", "Completely different names");
        
        // Test case 18: Names with numbers
        addTestCase(testCases, "John Doe 3rd", "John Doe III", "Different formats of the same suffix");
        
        // Test case 19: Empty strings
        addTestCase(testCases, "", "", "Both empty");
        
        // Test case 20: Null values
        addTestCase(testCases, null, null, "Both null");
        
        // Run all test cases
        NameMatchService service = new NameMatchService();
        System.out.println("Running 20 predefined test cases:");
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-4s %-20s %-20s %-10s %-10s %s%n", 
                "No.", "Name1", "Name2", "Match", "Score", "Description");
        System.out.println("------------------------------------------------------------");
        
        for (int i = 0; i < testCases.size(); i++) {
            Map<String, String> testCase = testCases.get(i);
            String name1 = testCase.get("name1");
            String name2 = testCase.get("name2");
            String description = testCase.get("description");
            
            Map<String, Object> result = service.mainFunction(name1, name2);
            
            System.out.printf("%-4d %-20s %-20s %-10s %-10s %s%n", 
                    (i + 1), 
                    truncate(name1, 20), 
                    truncate(name2, 20),
                    result.get("match"), 
                    result.get("match_score"), 
                    description);
        }
        System.out.println("------------------------------------------------------------");
    }
    
    private static void addTestCase(List<Map<String, String>> testCases, 
                                  String name1, String name2, String description) {
        Map<String, String> testCase = new HashMap<>();
        testCase.put("name1", name1);
        testCase.put("name2", name2);
        testCase.put("description", description);
        testCases.add(testCase);
    }
    
    private static String truncate(String str, int length) {
        if (str == null) return "null";
        return str.length() <= length ? str : str.substring(0, length - 3) + "...";
    }
}