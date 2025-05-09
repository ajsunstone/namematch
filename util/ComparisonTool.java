package com.name.match.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.name.match.service.IndianNameMatchService;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utility class for comparing results between Java and Python implementations.
 */
public class ComparisonTool {

    /**
     * Generates a predefined set of Indian name test cases.
     * @return List of test case maps with name1, name2, and description
     */
    public static List<Map<String, String>> generateIndianNameTestCases() {
        List<Map<String, String>> testCases = new ArrayList<>();
        
        // Common South Indian test cases
        addTestCase(testCases, "Venkatanarasimharajuvaripeta", "Venkata Narasimha Raju Varipeta", "Long South Indian name with/without spaces");
        addTestCase(testCases, "Sri Krishna", "SriKrishna", "Sri prefix with/without space");
        addTestCase(testCases, "T.N. Seshan", "Tirunellai Narayana Seshan", "Initials expanded");
        addTestCase(testCases, "A.P.J. Abdul Kalam", "Abdul Kalam", "Multiple initials vs name without initials");
        addTestCase(testCases, "Rajinikanth", "Shivaji Rao Gaekwad", "Stage name vs birth name");
        
        // Common North Indian test cases
        addTestCase(testCases, "Sharma Rahul", "Rahul Sharma", "Inverted name order");
        addTestCase(testCases, "Lal Krishna Advani", "L.K. Advani", "Full name vs initials");
        addTestCase(testCases, "Narendra Damodardas Modi", "N.D. Modi", "Full middle name vs initials");
        addTestCase(testCases, "Rajnath Singh", "R. Singh", "First name as initial");
        
        // Phonetic variations in Indian names
        addTestCase(testCases, "Lakshmi", "Laxmi", "Common phonetic variation (ksh/x)");
        addTestCase(testCases, "Saurabh", "Saurav", "Common phonetic variation (bh/v)");
        addTestCase(testCases, "Shyam", "Syam", "Common phonetic variation (sh/s)");
        addTestCase(testCases, "Krishan", "Krishna", "Common phonetic variation (a/an)");
        addTestCase(testCases, "Kamath", "Kamat", "Common phonetic variation (th/t)");
        
        // Name parts with dots and variations
        addTestCase(testCases, "Dr. Rajendra Prasad", "R. Prasad", "Salutation and initial");
        addTestCase(testCases, "B.R. Ambedkar", "Bhimrao Ramji Ambedkar", "Initials vs full name");
        addTestCase(testCases, "M.S. Subbulakshmi", "M.S. Subalakshmi", "Spelling variation with initials");
        addTestCase(testCases, "C.V. Raman", "Chandrasekhara Venkata Raman", "Initials vs full name");
        
        // Special character handling
        addTestCase(testCases, "Ram (Krishna) Sharma", "Ram Sharma", "Name with parentheses");
        addTestCase(testCases, "Sita.Patel", "Sita Patel", "Name with dot instead of space");
        addTestCase(testCases, "Jaya-prakash Narayan", "Jayaprakash Narayan", "Name with hyphen");
        addTestCase(testCases, "AISHWARYA RAI", "aishwarya rai", "Name with all caps");
        addTestCase(testCases, "amitabh  bachan", "Amitabh Bachan", "Name with multiple spaces");
        
        // Edge cases
        addTestCase(testCases, "", "", "Both empty");
        addTestCase(testCases, null, null, "Both null");
        addTestCase(testCases, "Raj", "", "One empty");
        addTestCase(testCases, "S", "S.", "Single character with/without dot");
        
        return testCases;
    }
    
    /**
     * Runs test cases using both the Java and Python implementations and compares the results.
     * @param pythonScript Path to the Python script
     * @param outputPath Path to save the comparison results
     * @return Number of differences found
     */
    public static int compareImplementations(String pythonScript, String outputPath) {
        List<Map<String, String>> testCases = generateIndianNameTestCases();
        List<Map<String, Object>> comparisonResults = new ArrayList<>();
        int differences = 0;
        
        // Java implementation
        IndianNameMatchService javaService = new IndianNameMatchService();
        
        for (Map<String, String> testCase : testCases) {
            String name1 = testCase.get("name1");
            String name2 = testCase.get("name2");
            String description = testCase.get("description");
            
            // Get Java result
            Map<String, Object> javaResult = javaService.mainFunction(name1, name2);
            
            // Get Python result via process execution
            Map<String, Object> pythonResult = executePythonScript(pythonScript, name1, name2);
            
            // Compare results
            boolean matchEquals = String.valueOf(javaResult.get("match"))
                    .equals(String.valueOf(pythonResult.get("match")));
            
            // Compare scores with a tolerance
            double javaScore = Double.parseDouble(String.valueOf(javaResult.get("match_score")));
            double pythonScore = Double.parseDouble(String.valueOf(pythonResult.get("match_score")));
            boolean scoreEquals = Math.abs(javaScore - pythonScore) < 0.01;
            
            if (!matchEquals || !scoreEquals) {
                differences++;
            }
            
            // Create comparison result
            Map<String, Object> comparisonResult = new HashMap<>();
            comparisonResult.put("name1", name1);
            comparisonResult.put("name2", name2);
            comparisonResult.put("description", description);
            comparisonResult.put("java_match", javaResult.get("match"));
            comparisonResult.put("java_score", javaResult.get("match_score"));
            comparisonResult.put("python_match", pythonResult.get("match"));
            comparisonResult.put("python_score", pythonResult.get("match_score"));
            comparisonResult.put("match_equals", matchEquals);
            comparisonResult.put("score_equals", scoreEquals);
            
            comparisonResults.add(comparisonResult);
        }
        
        // Save comparison results to JSON file
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputPath), comparisonResults);
        } catch (IOException e) {
            System.err.println("Error saving comparison results: " + e.getMessage());
        }
        
        return differences;
    }
    
    /**
     * Executes the Python script with the given name parameters and returns the result.
     * @param pythonScript Path to the Python script
     * @param name1 First name
     * @param name2 Second name
     * @return Result map with match and score
     */
    private static Map<String, Object> executePythonScript(String pythonScript, String name1, String name2) {
        Map<String, Object> result = new HashMap<>();
        result.put("match", "Reject");
        result.put("match_score", 0.0);
        
        try {
            // Build the command
            List<String> command = new ArrayList<>();
            command.add("python3");
            command.add(pythonScript);
            command.add(name1 != null ? name1 : "null");
            command.add(name2 != null ? name2 : "null");
            
            // Create process
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            
            // Read the output
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                // Parse the JSON output
                ObjectMapper mapper = new ObjectMapper();
                result = mapper.readValue(output.toString(), Map.class);
            }
        } catch (Exception e) {
            System.err.println("Error executing Python script: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Helper method to add a test case.
     */
    private static void addTestCase(List<Map<String, String>> testCases, 
                                   String name1, String name2, String description) {
        Map<String, String> testCase = new HashMap<>();
        testCase.put("name1", name1);
        testCase.put("name2", name2);
        testCase.put("description", description);
        testCases.add(testCase);
    }
    
    /**
     * Main method for standalone execution.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ComparisonTool <python_script_path> <output_json_path>");
            System.exit(1);
        }
        
        String pythonScript = args[0];
        String outputPath = args[1];
        
        int differences = compareImplementations(pythonScript, outputPath);
        System.out.println("Comparison completed. Found " + differences + " differences.");
        System.out.println("Results saved to: " + outputPath);
    }
} 