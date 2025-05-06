# Name Match Score - Java Implementation

This is a Java implementation of the name matching algorithm originally written in Python. It provides exactly the same functionality and returns the same results for the same inputs.

## Requirements

- Java 11 or higher
- Maven 3.6 or higher

## Building the Application

```bash
mvn clean package
```

## Running the Application

```bash
java -jar target/name-match-0.0.1-SNAPSHOT.jar
```

The application will run on port 3000 by default.

## API Endpoint

### GET /nameMatchScore

Calculates the match score between two names.

**Parameters:**
- `name1`: The first name to compare
- `name2`: The second name to compare

**Example:**
```
GET http://localhost:3000/nameMatchScore?name1=John%20Doe&name2=Jon%20Doe
```

**Response:**
```json
{
  "match": "Manual",
  "match_score": 75.5,
  "name1": "John Doe",
  "name2": "Jon Doe"
}
```

## Health Check

Health check is available at:
```
GET http://localhost:3000/actuator/health
```

## Testing Framework

A comprehensive testing framework is included to verify that the Java implementation produces identical results to the Python implementation.

### Test Cases

`test_cases.csv` contains 100 test cases with different name combinations to test various scenarios:
- Exact matches
- Case differences
- Space variations
- Spelling variations
- Name order swapping
- Salutations and suffixes
- International names
- Edge cases (empty values, null values)

### Running Tests

To run the full test suite and compare results between the Python and Java implementations:

```bash
./run_tests.sh
```

This script will:
1. Run the test cases through the Python implementation
2. Build and run the Java implementation with the test cases
3. Compare the results from both implementations
4. Report any differences found

### Manual Testing

You can also run the individual test components:

```bash
# Run Python tests
python test_python_version.py

# Build and run Java tests
mvn clean package -DskipTests
java -jar target/name-match-0.0.1-SNAPSHOT.jar

# Compare results
python compare_results.py
```

Note: The CSV file contains UTF-8 characters for international name tests. Make sure your environment is properly configured for UTF-8 encoding. 