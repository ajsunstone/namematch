import sys
import csv
import json
from name_match_score import MainFunction

def run_tests():
    results = []
    
    with open('test_cases.csv', 'r', encoding='utf-8') as file:
        reader = csv.DictReader(file)
        
        for i, row in enumerate(reader):
            name1 = row['name1'] if row['name1'] != 'null' else None
            name2 = row['name2'] if row['name2'] != 'null' else None
            
            # Run the name match algorithm
            match, score, _, _ = MainFunction(name1, name2)
            
            # Store the results
            results.append({
                'test_id': i+1,
                'name1': name1,
                'name2': name2,
                'description': row['description'],
                'match_result': match,
                'match_score': score
            })
    
    # Save results to JSON file
    with open('python_results.json', 'w', encoding='utf-8') as f:
        json.dump(results, f, indent=2, ensure_ascii=False)
    
    print(f"Completed {len(results)} test cases. Results saved to python_results.json")

if __name__ == "__main__":
    run_tests() 