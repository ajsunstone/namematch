package com.name.match.util;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import me.xdrop.fuzzywuzzy.FuzzySearch;

/**
 * Utility class for specialized Indian name matching.
 * This class implements functionality similar to the indian_namematch.fuzzymatch module used in the Python version.
 */
public class IndianNameMatcher {
    
    private static final JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();
    
    /**
     * Compares two Indian names to determine if they are a match.
     * This is an equivalent implementation of the indian_namematch.fuzzymatch.single_compare function.
     * 
     * @param name1 First name to compare
     * @param name2 Second name to compare
     * @return "Match" if names match phonetically/structurally, "NoMatch" otherwise
     */
    public static String singleCompare(String name1, String name2) {
        if (name1 == null || name2 == null || name1.isEmpty() || name2.isEmpty()) {
            return "NoMatch";
        }
        
        // Clean the names
        name1 = name1.toLowerCase().trim();
        name2 = name2.toLowerCase().trim();
        
        // 1. Check for exact match after normalization
        if (normalizeIndianName(name1).equals(normalizeIndianName(name2))) {
            return "Match";
        }
        
        // 2. Check Jaro-Winkler similarity (good for Indian names with transpositions)
        double jaroScore = jaroWinkler.apply(name1, name2);
        if (jaroScore > 0.95) {
            return "Match";
        }
        
        // 3. Check for common South Indian name patterns
        if (matchSouthIndianPatterns(name1, name2)) {
            return "Match";
        }
        
        // 4. Use fuzzy ratio for overall string similarity
        int fuzzyRatio = FuzzySearch.ratio(name1, name2);
        if (fuzzyRatio > 90) {
            return "Match";
        }
        
        // 5. Check for partial token matches (important for multi-part names)
        int partialRatio = FuzzySearch.partialRatio(name1, name2);
        if (partialRatio == 100) {
            return "Match";
        }
        
        return "NoMatch";
    }
    
    /**
     * Normalizes Indian names by handling common patterns.
     */
    private static String normalizeIndianName(String name) {
        if (name == null) return "";
        
        // Remove dots except in initials
        StringBuilder result = new StringBuilder();
        boolean previousWasDot = false;
        
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            
            // Handle dots in initials (like "A.B.")
            if (c == '.') {
                if (i > 0 && Character.isLetter(name.charAt(i-1)) && name.charAt(i-1) != ' ' &&
                    (i == name.length() - 1 || name.charAt(i+1) == ' ')) {
                    // Keep dots after single letters (initials)
                    result.append(c);
                }
                previousWasDot = true;
            } else {
                if (previousWasDot && c != ' ') {
                    // Add space after dots not followed by space
                    result.append(' ');
                }
                result.append(c);
                previousWasDot = false;
            }
        }
        
        return result.toString()
            .replace(".", " ") // Replace remaining dots with spaces
            .replace("(", " ") // Replace parentheses
            .replace(")", " ")
            .replace("-", " ") // Replace hyphens with spaces
            .replaceAll("\\s+", " ") // Replace multiple spaces with single space
            .trim();
    }
    
    /**
     * Checks for common South Indian name patterns.
     */
    private static boolean matchSouthIndianPatterns(String name1, String name2) {
        // 1. Check for names with initials in different positions
        // Example: "K. Ramesh" vs "Ramesh K."
        String[] parts1 = name1.split("\\s+");
        String[] parts2 = name2.split("\\s+");
        
        boolean hasInitial1 = false;
        boolean hasInitial2 = false;
        
        // Check if either name has initials (single letters with optional dots)
        for (String part : parts1) {
            if (isInitial(part)) {
                hasInitial1 = true;
                break;
            }
        }
        
        for (String part : parts2) {
            if (isInitial(part)) {
                hasInitial2 = true;
                break;
            }
        }
        
        // If both have initials, or one has an initial and the other doesn't,
        // compare the non-initial parts
        if (hasInitial1 || hasInitial2) {
            String nonInitial1 = extractNonInitials(parts1);
            String nonInitial2 = extractNonInitials(parts2);
            
            // If the non-initial parts match closely
            if (!nonInitial1.isEmpty() && !nonInitial2.isEmpty()) {
                double score = jaroWinkler.apply(nonInitial1, nonInitial2);
                if (score > 0.9) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Checks if a string is an initial (single letter, optionally with a dot).
     */
    private static boolean isInitial(String str) {
        return str.length() == 1 || 
               (str.length() == 2 && str.charAt(1) == '.') ||
               (str.length() > 1 && str.contains(".") && str.replace(".", "").length() == 1);
    }
    
    /**
     * Extracts non-initial parts of a name.
     */
    private static String extractNonInitials(String[] parts) {
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (!isInitial(part)) {
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(part);
            }
        }
        return result.toString();
    }
} 