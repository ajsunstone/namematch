#!/bin/bash

echo "---------------- Running Test Framework ----------------"

echo "Step 1: Running Python implementation tests..."
python test_python_version.py

echo ""
echo "Step 2: Building Java implementation..."
mvn clean package -DskipTests

echo ""
echo "Step 3: Running Java implementation tests..."
java -jar target/name-match-0.0.1-SNAPSHOT.jar

echo ""
echo "Step 4: Comparing results between implementations..."
python compare_results.py

echo ""
echo "Test execution completed. Check results above for any differences." 