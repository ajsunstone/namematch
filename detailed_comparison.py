import json
import csv
import subprocess
import os
from name_match_score import MainFunction

def run_python_tests(csv_file):
    """Run tests through the Python implementation."""
    results = []
    
    with open(csv_file, 'r', encoding='utf-8') as file:
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
                'python_match': match,
                'python_score': score
            })
    
    return results

def run_java_tests(csv_file):
    """Build and run the Java implementation."""
    # Build the Java application
    subprocess.run(['mvn', 'clean', 'package', '-DskipTests'], check=True)
    
    # Run the Java tests
    subprocess.run(['java', '-jar', 'target/name-match-0.0.1-SNAPSHOT.jar'], check=True)
    
    # Load the Java results
    with open('java_results.json', 'r', encoding='utf-8') as f:
        java_results = json.load(f)
    
    return java_results

def merge_results(python_results, java_results):
    """Merge Python and Java results."""
    merged = []
    
    for py_result in python_results:
        test_id = py_result['test_id']
        
        # Find matching Java result
        java_result = next((r for r in java_results if r['test_id'] == test_id), None)
        
        if java_result:
            merged_result = {
                'test_id': test_id,
                'name1': py_result['name1'],
                'name2': py_result['name2'],
                'description': py_result['description'],
                'python_match': py_result['python_match'],
                'python_score': py_result['python_score'],
                'java_match': java_result['match_result'],
                'java_score': java_result['match_score'],
                'matches_identical': py_result['python_match'] == java_result['match_result'],
                'scores_identical': py_result['python_score'] == java_result['match_score'] if py_result['python_score'] != "" and java_result['match_score'] != "" else False
            }
            
            # Calculate score difference if both are numeric
            try:
                py_score = float(py_result['python_score']) if py_result['python_score'] != "" else 0
                java_score = float(java_result['match_score']) if java_result['match_score'] != "" else 0
                score_diff = abs(py_score - java_score)
                merged_result['score_difference'] = score_diff
                merged_result['scores_within_tolerance'] = score_diff <= 0.001
            except (ValueError, TypeError):
                merged_result['score_difference'] = None
                merged_result['scores_within_tolerance'] = False
            
            merged.append(merged_result)
    
    return merged

def analyze_results(merged_results):
    """Analyze the merged results."""
    total = len(merged_results)
    
    match_differences = [r for r in merged_results if not r['matches_identical']]
    score_differences = [r for r in merged_results if not r['scores_within_tolerance']]
    
    analysis = {
        'total_test_cases': total,
        'match_differences_count': len(match_differences),
        'score_differences_count': len(score_differences),
        'match_success_percentage': (total - len(match_differences)) / total * 100 if total > 0 else 0,
        'score_success_percentage': (total - len(score_differences)) / total * 100 if total > 0 else 0,
        'match_differences': match_differences,
        'score_differences': score_differences
    }
    
    return analysis

def run_comparison(csv_file='test_cases_1000.csv'):
    """Run the full comparison process."""
    # First generate the CSV with 1000 test cases
    print("Generating 1000 test cases...")
    subprocess.run(['python', 'generate_test_cases.py'], check=True)
    
    # Update the Java TestRunner to use the new CSV file
    with open('src/main/java/com/name/match/TestRunner.java', 'r', encoding='utf-8') as f:
        content = f.read()
    
    updated_content = content.replace('test_cases.csv', csv_file)
    
    with open('src/main/java/com/name/match/TestRunner.java', 'w', encoding='utf-8') as f:
        f.write(updated_content)
    
    # Run Python tests
    print("Running Python implementation tests...")
    python_results = run_python_tests(csv_file)
    
    # Save Python results
    with open('python_results_detailed.json', 'w', encoding='utf-8') as f:
        json.dump(python_results, f, indent=2, ensure_ascii=False)
    
    # Run Java tests
    print("Building and running Java implementation tests...")
    java_results = run_java_tests(csv_file)
    
    # Merge and analyze
    print("Merging and analyzing results...")
    merged_results = merge_results(python_results, java_results)
    analysis = analyze_results(merged_results)
    
    # Save detailed results
    with open('comparison_results.json', 'w', encoding='utf-8') as f:
        json.dump({
            'analysis': analysis,
            'detailed_results': merged_results
        }, f, indent=2, ensure_ascii=False)
    
    # Print summary
    print(f"\nComparison Results Summary:")
    print(f"Total test cases: {analysis['total_test_cases']}")
    print(f"Match differences: {analysis['match_differences_count']} ({100 - analysis['match_success_percentage']:.2f}% failure rate)")
    print(f"Score differences: {analysis['score_differences_count']} ({100 - analysis['score_success_percentage']:.2f}% failure rate)")
    
    if analysis['match_differences_count'] == 0 and analysis['score_differences_count'] == 0:
        print("\n✅ SUCCESS: Both implementations give identical results for all test cases!")
    else:
        print("\n❌ DIFFERENCES FOUND: See comparison_results.json for details.")
        
        if analysis['match_differences_count'] > 0:
            print("\nSample match differences:")
            for diff in analysis['match_differences'][:5]:  # Show up to 5 examples
                print(f"  Test {diff['test_id']}: '{diff['name1']}' vs '{diff['name2']}' ({diff['description']})")
                print(f"    Python: {diff['python_match']}, Java: {diff['java_match']}")
        
        if analysis['score_differences_count'] > 0:
            print("\nSample score differences:")
            for diff in analysis['score_differences'][:5]:  # Show up to 5 examples
                print(f"  Test {diff['test_id']}: '{diff['name1']}' vs '{diff['name2']}' ({diff['description']})")
                print(f"    Python: {diff['python_score']}, Java: {diff['java_score']}, Diff: {diff.get('score_difference', 'N/A')}")

if __name__ == "__main__":
    run_comparison() 