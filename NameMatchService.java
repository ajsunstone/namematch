package com.name.match.service;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NameMatchService {

    /**
     * Evaluates phonetic and fuzzy similarity between two names to identify potential matches.
     * 
     * This function is critical to the name matching algorithm as it detects names that
     * may sound similar even when spelled differently. It combines multiple matching strategies:
     * 
     * 1. Soundex phonetic similarity: Identifies names that sound alike when pronounced
     * 2. Full string fuzzy matching: Identifies overall string similarity
     * 3. Partial string matching: Identifies similarities in substrings
     * 
     * When similarity thresholds are met AND the existing match score is in a specific range,
     * the function identifies the match as requiring manual review.
     * 
     * Use cases:
     * - Detecting spelling variations: "Jon" vs "John" 
     * - Identifying phonetically similar names: "Smith" vs "Smyth"
     * - Handling transliteration differences: "Mohamed" vs "Muhammad"
     * 
     * @param name1 First name to compare
     * @param name2 Second name to compare
     * @param matchScore Existing match score from previous comparisons
     * @return List containing "Manual" if the match requires human review, otherwise empty list
     */
    public List<String> soundexMatch(String name1, String name2, double matchScore) {
        JaroWinklerSimilarity jaroWinkler = new JaroWinklerSimilarity();
        double soundexScore = jaroWinkler.apply(soundex(name1), soundex(name2));
        double match1 = FuzzySearch.ratio(name1, name2);
        double match2 = FuzzySearch.partialRatio(name1, name2);

        //System.out.println("soundexScore : " + soundexScore);
        //System.out.println("match1 : " + match1);
        //System.out.println("match2 : " + match2);
        List<String> res = new ArrayList<>();


        if ((soundexScore > 0.85 || match1 == 100 || match2 == 100) && matchScore > 40) {
            res.add("Manual");
        }

        return res;
    }

    /**
     * Implements the Soundex phonetic algorithm for indexing names by sound.
     * 
     * Soundex is a phonetic algorithm that converts a name into a code representing its 
     * pronunciation, making it possible to match names with similar sounds but different spellings.
     * The algorithm:
     * 
     * 1. Preserves the first letter of the name
     * 2. Converts remaining letters to digits according to the Soundex encoding rules:
     *    - 1 = B, F, P, V (labial consonants)
     *    - 2 = C, G, J, K, Q, S, X, Z (guttural and sibilant consonants)
     *    - 3 = D, T (dental consonants)
     *    - 4 = L (liquid consonant)
     *    - 5 = M, N (nasal consonants)
     *    - 6 = R (liquid consonant)
     *    - Vowels and H, W, Y are ignored (coded as 0)
     * 3. Removes consecutive duplicates of the same code
     * 4. Removes all zeros
     * 5. Returns a 4-character code (padded with zeros if necessary)
     * 
     * For example:
     * - "Robert" and "Rupert" both yield "R163"
     * - "Smith" and "Smyth" both yield "S530"
     * 
     * This is essential for matching names that sound the same despite spelling variations.
     * 
     * @param s Input name string to convert to Soundex code
     * @return Four-character Soundex code
     */
    private String soundex(String s) {
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
     * Generates character bigrams (pairs of adjacent characters) from a list of name parts.
     * 
     * This function is a key part of the name matching algorithm that breaks names into
     * character pairs which can be compared to determine similarity even with minor
     * spelling differences.
     * 
     * For each string in the list:
     * - If length 1: adds the single character
     * - If length 2: adds the entire string (already a bigram)
     * - If length > 2: extracts all consecutive character pairs
     *   (e.g., "John" → "Jo", "oh", "hn")
     * 
     * While the function tracks bigram frequencies in a map, it only returns the list
     * of all generated bigrams without frequency information.
     * 
     * @param nameList List of name parts to generate bigrams from
     * @return List of all bigrams extracted from the input names
     */
    public List<String> makeDoublet(List<String> nameList) {
        List<String> duovaluenap = new ArrayList<>();
        Map<String, Integer> duovaluemap = new HashMap<>();

        for (String i : nameList) {
            if (i.length() == 1) {
                String str = String.valueOf(i.charAt(0));
                duovaluenap.add(str);
                duovaluemap.put(str, duovaluemap.getOrDefault(str, 0) + 1);
            } else if (i.length() == 2) {
                duovaluenap.add(i);
                duovaluemap.put(i, duovaluemap.getOrDefault(i, 0) + 1);
            } else {
                for (int j = 0; j < i.length() - 1; j++) {
                    String doublet = i.substring(j, j + 2);
                    duovaluenap.add(doublet);
                    duovaluemap.put(doublet, duovaluemap.getOrDefault(doublet, 0) + 1);
                }
            }
        }
        return duovaluenap;
    }

    /**
     * Calculates a similarity percentage between two lists of strings (typically bigrams).
     * 
     * This function is essential to the name matching algorithm as it determines how
     * similar two sets of character bigrams are, providing a numerical score for name similarity.
     * The algorithm:
     * 
     * 1. Ensures str1 is the smaller list (for optimization)
     * 2. Counts how many elements from str1 appear in str2 (each element counted only once)
     * 3. Calculates percentage: (matches / total elements in smaller list) * 100
     * 
     * This approach is effective for name matching because:
     * - It identifies common character patterns between names
     * - It handles transpositions and minor spelling differences
     * - It produces a normalized similarity score from 0-100%
     * 
     * @param str1 First list of strings to compare (typically bigrams from first name)
     * @param str2 Second list of strings to compare (typically bigrams from second name)
     * @return Similarity percentage from 0-100
     */
    public double compareString(List<String> str1, List<String> str2) {
        if (str1.size() > str2.size()) {
            List<String> temp = str1;
            str1 = str2;
            str2 = new ArrayList<>(temp);
        }

        int match = 0;
        int total = str1.size();
        List<String> str2Copy = new ArrayList<>(str2);

        for (String i : str1) {
            if (str2Copy.contains(i)) {
                match++;
                str2Copy.remove(i);
            }
        }

        if (total != 0) {
            return (match / (double) total) * 100;
        }
        return 0;
    }

    /**
     * Equivalent to MainFunction in Python code
     */
    public Map<String, Object> mainFunction(String firstString, String secondString) {
        String aadhaar = firstString;
        String pan = secondString;
        double score = 0;

        //System.out.println("aadhaar : " + aadhaar);
        //System.out.println("pan : " + pan);

        String match;
        if (aadhaar == null || aadhaar.isEmpty() || pan == null || pan.isEmpty()) {
            score = 0;
        } else {
            aadhaar = aadhaar.replace(" ", "").toLowerCase().trim();
            pan = pan.replace(" ", "").toLowerCase().trim();
            
            // Split would normally split by whitespace, but since we just replaced all spaces,
            // create arrays with a single element for consistent behavior with Python
            List<String> aadhaarArray = new ArrayList<>();
            aadhaarArray.add(aadhaar);
            
            List<String> panArray = new ArrayList<>();
            panArray.add(pan);
            Collections.sort(aadhaarArray);
            Collections.sort(panArray);
            List<String> tempPanArray = new ArrayList<>(aadhaarArray);
            Collections.sort(tempPanArray);

            if (aadhaar.equals(pan)) {
                score = 100;
            } else if (aadhaar.replace(" ", "").equals(pan.replace(" ", ""))) {
                score = 100;
            } else {
                List<String> aadhaarNameDoublets = makeDoublet(aadhaarArray);
                List<String> panNameDoublets = makeDoublet(panArray);
                score = compareString(aadhaarNameDoublets, panNameDoublets);

                if (score >= 40 && score <= 75) {
                    try {
                        List<String> output = soundexMatch(aadhaar, pan, score);
                        if (!output.isEmpty()) {
                            score = 40;
                        }
                    } catch (Exception e) {
                        // Do nothing, similar to Python's pass
                    }
                }
            }
        }

        if (score >= 99) {
            match = "Accept";
        } else if (score >= 70) {
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