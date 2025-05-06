package com.name.match;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.name.match.service.NameMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SpringBootApplication
public class TestRunner {

    @Autowired
    private NameMatchService nameMatchService;

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(TestRunner.class, args);
        ctx.close();
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            List<Map<String, Object>> results = new ArrayList<>();
            String csvFile = "test_cases_1000.csv";  // Use the 1000 test cases file by default
            
            System.out.println("Processing test cases from " + csvFile);
            
            // Read the CSV file
            try (BufferedReader br = new BufferedReader(new FileReader(csvFile, StandardCharsets.UTF_8))) {
                // Skip header line
                String line = br.readLine();
                int testId = 1;
                
                while ((line = br.readLine()) != null) {
                    // Parse the CSV line more robustly
                    List<String> values = parseCSVLine(line);
                    
                    if (values.size() >= 2) {
                        String name1 = "null".equals(values.get(0)) ? null : values.get(0);
                        String name2 = "null".equals(values.get(1)) ? null : values.get(1);
                        String description = values.size() > 2 ? values.get(2) : "";
                        
                        // Run the name match algorithm
                        Map<String, Object> result = nameMatchService.mainFunction(name1, name2);
                        
                        // Create test result entry
                        Map<String, Object> testResult = new HashMap<>();
                        testResult.put("test_id", testId++);
                        testResult.put("name1", name1);
                        testResult.put("name2", name2);
                        testResult.put("description", description);
                        testResult.put("match_result", result.get("match"));
                        testResult.put("match_score", result.get("match_score"));
                        
                        results.add(testResult);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error processing CSV: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Write results to JSON file
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File("java_results.json"), results);
            
            System.out.println("Completed " + results.size() + " test cases. Results saved to java_results.json");
        };
    }
    
    /**
     * Parse a CSV line properly handling quoted values and commas within fields
     */
    private List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(field.toString());
                field.setLength(0);
            } else {
                field.append(c);
            }
        }
        
        // Add the last field
        result.add(field.toString());
        
        return result;
    }
} 