import json
import sys

def compare_results():
    # Load both result files
    try:
        with open('python_results.json', 'r', encoding='utf-8') as f:
            python_results = json.load(f)
    except FileNotFoundError:
        print("Error: python_results.json not found. Please run test_python_version.py first.")
        return
    
    try:
        with open('java_results.json', 'r', encoding='utf-8') as f:
            java_results = json.load(f)
    except FileNotFoundError:
        print("Error: java_results.json not found. Please run the Java TestRunner first.")
        return
    
    # Check if the number of test cases match
    if len(python_results) != len(java_results):
        print(f"WARNING: Number of test cases doesn't match: Python ({len(python_results)}) vs Java ({len(java_results)})")
    
    # Sort by test_id to ensure proper comparison
    python_results.sort(key=lambda x: x['test_id'])
    java_results.sort(key=lambda x: x['test_id'])
    
    # Compare the results
    match_differences = []
    score_differences = []
    
    max_test_cases = min(len(python_results), len(java_results))
    
    for i in range(max_test_cases):
        py_result = python_results[i]
        java_result = java_results[i]
        
        # Check if the test cases are the same
        if py_result['name1'] != java_result['name1'] or py_result['name2'] != java_result['name2']:
            print(f"WARNING: Test case {i+1} inputs don't match:")
            print(f"  Python: {py_result['name1']} / {py_result['name2']}")
            print(f"  Java:   {java_result['name1']} / {java_result['name2']}")
            continue
        
        # Compare match results
        if py_result['match_result'] != java_result['match_result']:
            match_differences.append({
                'test_id': i+1,
                'name1': py_result['name1'],
                'name2': py_result['name2'],
                'description': py_result['description'],
                'python_match': py_result['match_result'],
                'java_match': java_result['match_result']
            })
        
        # Compare score results (handle different score types)
        py_score = float(py_result['match_score']) if py_result['match_score'] != "" else 0
        java_score = float(java_result['match_score']) if java_result['match_score'] != "" else 0
        
        # Allow a small tolerance for floating point differences
        if abs(py_score - java_score) > 0.001:
            score_differences.append({
                'test_id': i+1,
                'name1': py_result['name1'],
                'name2': py_result['name2'],
                'description': py_result['description'],
                'python_score': py_score,
                'java_score': java_score
            })
    
    # Print results
    print(f"\nComparison Results:")
    print(f"Total test cases compared: {max_test_cases}")
    print(f"Match differences found: {len(match_differences)}")
    print(f"Score differences found: {len(score_differences)}")
    
    if match_differences:
        print("\nMatch Differences:")
        for diff in match_differences:
            print(f"Test {diff['test_id']}: '{diff['name1']}' vs '{diff['name2']}' ({diff['description']})")
            print(f"  Python: {diff['python_match']}")
            print(f"  Java:   {diff['java_match']}")
    
    if score_differences:
        print("\nScore Differences:")
        for diff in score_differences:
            print(f"Test {diff['test_id']}: '{diff['name1']}' vs '{diff['name2']}' ({diff['description']})")
            print(f"  Python: {diff['python_score']}")
            print(f"  Java:   {diff['java_score']}")
    
    if not match_differences and not score_differences:
        print("\n✅ SUCCESS: Both implementations give identical results for all test cases!")
    else:
        print("\n❌ FAILURE: Differences found between implementations.")

if __name__ == "__main__":
    compare_results() 