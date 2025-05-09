package com.name.match;

import com.name.match.service.IndianNameMatchService;
import java.util.*;

/**
 * Command line tool for testing the Indian Name Match implementation.
 * Specifically tailored for testing South Indian name patterns.
 */
public class IndianNameMatchCommandLine {
    public static void main(String[] args) {
        // Check if we should run the predefined test cases or use command line arguments
        if (args.length > 0 && args[0].equals("--test")) {
            runPredefinedTestCases();
        } else if (args.length > 0 && args[0].equals("--compare")) {
            String pythonResultsPath = args.length > 1 ? args[1] : "python_results.json";
            compareWithPythonResults(pythonResultsPath);
        } else if (args.length == 2) {
            // Run a single test with command line arguments
            String name1 = args[0];
            String name2 = args[1];
            
            IndianNameMatchService service = new IndianNameMatchService();
            Map<String, Object> result = service.mainFunction(name1, name2);
            
            System.out.println("Match: " + result.get("match"));
            System.out.println("Score: " + result.get("match_score"));
        } else {
            System.out.println("Usage:");
            System.out.println("  java IndianNameMatchCommandLine <name1> <name2>");
            System.out.println("  java IndianNameMatchCommandLine --test");
            System.out.println("  java IndianNameMatchCommandLine --compare [python_results.json]");
            System.exit(1);
        }
    }
    
    private static void runPredefinedTestCases() {
        // Define test cases with Indian name pairs and descriptions
        List<Map<String, String>> testCases = new ArrayList<>();
        
        // Test case 1: Exact match
        addTestCase(testCases, "Rajesh Kumar", "Rajesh Kumar", "Exact match");
           addTestCase(testCases, "Rajesh Kumar", "Ankit  Kumar", "50%  mismatch");
             addTestCase(testCases, "Rajesh Kumar", "Ankit  Jain", "100%  mismatch");

                // Test case 1: Exact match
        addTestCase(testCases, "Karan Singh", "Aaran Singh", "One char mis-match");
        
        addTestCase(testCases, "Mr Karan Singh", "Karan Singh", "Salutationh");

        addTestCase(testCases, "Shaun singh", "Shon Singh", "phoenetics");

        // Test case 2: South Indian name variations
        addTestCase(testCases, "Venkatanarasimharajuvaripeta", "Venkata Narasimha Raju Varipeta", "South Indian name with/without spaces");
        
        // Test case 3: Common Indian name prefix
        addTestCase(testCases, "Sri Ganesh", "Sriganesh", "Common prefix with/without space");
        
        // Test case 4: Case difference
        addTestCase(testCases, "Suresh PATEL", "suresh patel", "Case difference");
        
        // Test case 5: Multiple spaces
        addTestCase(testCases, "Arun  Kumar", "Arun Kumar", "Multiple spaces");
        
        // Test case 6: Minor spelling variation
        addTestCase(testCases, "Kiran Sharma", "Kiran Sarma", "Minor spelling variation");
        
        // Test case 7: Different last name
        addTestCase(testCases, "Anil Kumar", "Anil Singh", "Different last name");
        
        // Test case 8: First and last name swapped (common in South India)
        addTestCase(testCases, "Reddy Srinivas", "Srinivas Reddy", "First and last name swapped");
        
        // Test case 9: Initial forms common in South India
        addTestCase(testCases, "K. Ramesh", "Ramesh K.", "Initial positions swapped");
        
        // Test case 10: Multiple initials
        addTestCase(testCases, "A.B. Krishnan", "Krishnan A.B.", "Multiple initials");
        
        // Test case 11: Expanded initials
        addTestCase(testCases, "R Chandrasekhar", "Ramesh Chandrasekhar", "Initial expanded to full name");
        
        // Test case 12: Abbreviated name parts
        addTestCase(testCases, "V. Subramanian", "Venkat Subramanian", "Abbreviated first name");
        
        // Test case 13: Phonetic variations
        addTestCase(testCases, "Lakshmi", "Laxmi", "Phonetic variation in transliteration");
        
        // Test case 14: Special characters
        addTestCase(testCases, "Ram (Ramesh) Sharma", "Ram Sharma", "Name with parenthetical part");
        
        // Test case 15: Dots in names
        addTestCase(testCases, "Dr. Mohan.K", "Mohan K", "Dots in names");
        
        // Test case 16: Common South Indian pattern
        addTestCase(testCases, "Nandamuri Taraka Rama Rao", "N.T. Rama Rao", "Full name with initials");
        
        // Test case 17: Similar sounding names
        addTestCase(testCases, "Shyam", "Syam", "Phonetically similar names (sh/s)");
        
        // Test case 18: Father's name as initial (common in Tamil Nadu)
        addTestCase(testCases, "P. Anandhan", "Palaniappan Anandhan", "Father's name as initial");
        
        // Test case 19: Double-barrelled surname (married women)
        addTestCase(testCases, "Sudha Murthy-Kulkarni", "Sudha Murthy Kulkarni", "Hyphenated surname");
        
        // Test case 20: Empty strings
        addTestCase(testCases, "", "", "Both empty");
        
        // Run all test cases
        IndianNameMatchService service = new IndianNameMatchService();
        System.out.println("Running 20 predefined Indian name test cases:");
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-4s %-25s %-25s %-10s %-10s %s%n", 
                "No.", "Name1", "Name2", "Match", "Score", "Description");
        System.out.println("------------------------------------------------------------");
        
        for (int i = 0; i < testCases.size(); i++) {
            Map<String, String> testCase = testCases.get(i);
            String name1 = testCase.get("name1");
            String name2 = testCase.get("name2");
            String description = testCase.get("description");
            
            Map<String, Object> result = service.mainFunction(name1, name2);
            
            System.out.printf("%-4d %-25s %-25s %-10s %-10s %s%n", 
                    (i + 1), 
                    truncate(name1, 25), 
                    truncate(name2, 25),
                    result.get("match"), 
                    result.get("match_score"), 
                    description);
        }
        System.out.println("------------------------------------------------------------");
    }
    
    private static void compareWithPythonResults(String pythonResultsPath) {
        System.out.println("This feature would compare Java implementation results with Python results.");
        System.out.println("Not implemented in this demo version.");
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