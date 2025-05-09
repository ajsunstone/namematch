package com.name.match.service;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Service;
import com.name.match.util.IndianNameMatcher;

import java.util.*;

/**
 * Service for matching Indian names with special handling for South Indian name patterns.
 * This implementation mirrors the functionality of name-match-srini.py.
 */
@Service
public class IndianNameMatchService {

    /**
     * Evaluates phonetic and fuzzy similarity specifically optimized for Indian names.
     * 
     * @param name1 First name to compare
     * @param name2 Second name to compare
     * @param matchScore Existing match score from previous comparisons
     * @return List containing "Manual" and a modified score if the match requires human review, otherwise empty list
     */
    public List<Object> soundexMatch(String name1, String name2, double matchScore) {
        JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();
        double soundexScore = jaroWinkler.apply(soundex(name1), soundex(name2));
        
        // Use our IndianNameMatcher for specialized Indian name comparison
        String match1 = IndianNameMatcher.singleCompare(name1, name2);
        String match2 = IndianNameMatcher.singleCompare(name2, name1);

        List<Object> res = new ArrayList<>();
        
        // Note the change here: only using soundexScore > 0.85 without the match1/match2 checks
        if (soundexScore > 0.85 && matchScore > 40) {
            res.add("Manual");
            res.add((soundexScore * 100) - 5); // Adding adjusted score as in Python version
        }
        
        return res;
    }

    /**
     * Implements the Soundex phonetic algorithm for indexing names by sound.
     */
    private String soundex(String s) {
        if (s == null || s.isEmpty()) {
            return "0000";
        }
        
        char[] x = s.toUpperCase().toCharArray();
        char firstLetter = x[0];

        // Convert letters to digits
        for (int i = 0; i < x.length; i++) {
            switch (x[i]) {
                case 'B':
                case 'F':
                case 'P':
                case 'V':
                    x[i] = '1';
                    break;

                case 'C':
                case 'G':
                case 'J':
                case 'K':
                case 'Q':
                case 'S':
                case 'X':
                case 'Z':
                    x[i] = '2';
                    break;

                case 'D':
                case 'T':
                    x[i] = '3';
                    break;

                case 'L':
                    x[i] = '4';
                    break;

                case 'M':
                case 'N':
                    x[i] = '5';
                    break;

                case 'R':
                    x[i] = '6';
                    break;

                default:
                    x[i] = '0';
                    break;
            }
        }

        // Remove duplicates
        String output = "" + firstLetter;
        for (int i = 1; i < x.length; i++) {
            if (x[i] != x[i-1] && x[i] != '0') {
                output += x[i];
            }
        }

        // Pad with zeros and truncate
        output = output + "0000";
        return output.substring(0, 4);
    }

    /**
     * Creates doublets (character pairs and special boundary markers) for Indian name comparison.
     * This follows the makeduo function in the Python implementation.
     * 
     * @param nameList List of name parts to generate doublets from
     * @return Map containing the doublets and their frequencies, with a "length" entry
     */
    public Map<String, Integer> makeduo(List<String> nameList) {
        List<String> duovalue = new ArrayList<>();
        Map<String, Integer> duovaluemap = new HashMap<>();
        
        for (String i : nameList) {
            if (i.length() == 1) {
                // Special handling for single character with boundary marker
                String str = "_" + i.charAt(0);
                duovalue.add(str);
            } else if (i.length() > 1) {
                // Add beginning boundary marker
                String startStr = "_" + i.charAt(0);
                duovalue.add(startStr);
                
                // Add ending boundary marker
                String endStr = i.charAt(i.length()-1) + "_";
                duovalue.add(endStr);
                
                // Add all consecutive character pairs
                for (int j = 0; j < i.length() - 1; j++) {
                    duovalue.add(i.substring(j, j + 2));
                }
                
                // Add the full word if length > 2
                if (i.length() > 2) {
                    duovalue.add(i);
                }
            }
        }
        
        // Store the length of the list
        duovaluemap.put("length", duovalue.size());
        
        // Count frequencies of each doublet
        for (String item : duovalue) {
            duovaluemap.put(item, duovaluemap.getOrDefault(item, 0) + 1);
        }
        
        return duovaluemap;
    }

    /**
     * Compares two sets of doublets to determine name similarity.
     * 
     * @param str1 First map of doublets
     * @param str2 Second map of doublets
     * @return Similarity percentage from 0-100
     */
    public double compareString(Map<String, Integer> str1, Map<String, Integer> str2) {
        // Make a copy of the maps to avoid modifying the originals
        Map<String, Integer> map1 = new HashMap<>(str1);
        Map<String, Integer> map2 = new HashMap<>(str2);
        
        // Ensure str1 has fewer or equal doublets compared to str2
        if (map1.get("length") > map2.get("length")) {
            Map<String, Integer> temp = map1;
            map1 = map2;
            map2 = temp;
        }
        
        int match = 0;
        int total = map1.get("length");
        
        // Remove the length entry before processing
        map1.remove("length");
        map2.remove("length");
        
        // Compare frequencies and count matches
        for (Map.Entry<String, Integer> entry : map1.entrySet()) {
            String key = entry.getKey();
            if (map2.containsKey(key)) {
                int minVal = Math.min(entry.getValue(), map2.get(key));
                match += minVal;
                map2.put(key, map2.get(key) - minVal);
            }
        }
        
        // Calculate percentage
        if (total == 0) {
            total = 1; // Avoid division by zero
        }
        
        return (match / (double) total) * 100;
    }

    /**
     * Main function that implements the Indian name matching algorithm.
     * 
     * @param firstString First name to compare
     * @param secondString Second name to compare
     * @return Map containing match result, score, and original names
     */
    public Map<String, Object> mainFunction(String firstString, String secondString) {
        String aadhaar = firstString;
        String pan = secondString;
        double score = 0;
        String match = "";
        
        if (aadhaar == null || aadhaar.isEmpty() || pan == null || pan.isEmpty()) {
            score = 0.0;
        } else {
            // Clean the strings using the same rules as the Python version
            aadhaar = aadhaar.replace(".", " ").toLowerCase().trim();
            aadhaar = aadhaar.replace("(", "");
            aadhaar = aadhaar.replace(")", "");
            pan = pan.replace(".", " ").toLowerCase().trim();
            aadhaar = aadhaar.replace("  ", " ");
            pan = pan.replace("  ", " ");
            
            List<String> aadhaarArray = new ArrayList<>(Arrays.asList(aadhaar.split(" ")));
            List<String> panArray = new ArrayList<>(Arrays.asList(pan.split(" ")));
            
            List<String> tempAadhaarArray = new ArrayList<>(aadhaarArray);
            List<String> tempPanArray = new ArrayList<>(panArray);
            
            aadhaarArray.sort(String::compareTo);
            panArray.sort(String::compareTo);
            
            if (aadhaar.equals(" ") || pan.equals(" ")) {
                score = 0.0;
            } else {
                List<String> aadhaarName = Arrays.asList(aadhaar.split(" "));
                List<String> panName = Arrays.asList(pan.split(" "));
                
                Map<String, Integer> aadhaarNameDoublets = makeduo(aadhaarName);
                Map<String, Integer> panNameDoublets = makeduo(panName);
                
                // Calculate the score
                score = compareString(aadhaarNameDoublets, panNameDoublets);
                
                // Special case for exact matches
                if (aadhaar.replace(" ", "").equals(pan.replace(" ", ""))) {
                    score = 100;
                } else if (score == 100 && (!aadhaar.equals(pan) && !aadhaarArray.equals(panArray))) {
                    score = 98;
                }
                
                // Check for names that are exactly the same but in different order
                if (score == 100) {
                    if (tempAadhaarArray.equals(tempPanArray)) {
                        score = 100;
                    } else {
                        score = 98;
                    }
                }
                
                // Apply Soundex for scores in the 40-75 range
                if (score > 40 && score < 75) {
                    List<Object> output = new ArrayList<>();
                    try {
                        output = soundexMatch(aadhaar, pan, score);
                    } catch (Exception e) {
                        // Ignore exceptions
                    }
                    
                    if (!output.isEmpty()) {
                        score = (double) output.get(1);
                    }
                }
            }
        }
        
        // Normalize score as in Python version
        score = Math.round(score / 100 * 100.0) / 100.0;
        
        // Determine match result
        if (score >= 0.99) {
            match = "Accept";
        } else if (score >= 0.70) {
            match = "Manual";
        } else {
            match = "Reject";
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("match", match);
        result.put("match_score", score);
        result.put("name1", firstString);
        result.put("name2", secondString);
        
        return result;
    }
} 