import csv
import random
import string
import unicodedata

def generate_test_cases(count=1000):
    """Generate diverse test cases for name matching."""
    test_cases = []
    
    # Start with our existing test cases as a foundation
    basic_test_cases = [
        ("John Doe", "John Doe", "Exact match"),
        ("John Doe", "john doe", "Case difference"),
        ("John Doe", "JohnDoe", "Space removal"),
        ("JOHN DOE", "john doe", "All caps vs lowercase"),
        ("John Doe", "John  Doe", "Multiple spaces"),
        ("John Doe", "Jon Doe", "Minor spelling variation"),
        ("John Doe", "John Smith", "Different last name"),
        ("John Doe", "Doe John", "First and last name swapped"),
        ("John M Doe", "John Doe", "Middle initial missing in second"),
        ("John Doe", "John M Doe", "Middle initial missing in first"),
        ("John Michael Doe", "John M Doe", "Full middle name vs initial"),
        ("J. Doe", "John Doe", "Abbreviated first name"),
        ("John D.", "John Doe", "Abbreviated last name"),
        ("Mr. John Doe", "John Doe", "Salutation in first only"),
        ("John Doe", "Mr. John Doe", "Salutation in second only"),
        ("Mr. John Doe", "Mr John Doe", "Salutation with/without period"),
        ("Dr. John Doe", "Mr. John Doe", "Different salutations"),
        ("John Doe Jr.", "John Doe", "Suffix in first only"),
        ("John Doe", "John Doe Jr.", "Suffix in second only"),
        ("John Doe Jr.", "John Doe Junior", "Abbreviated vs full suffix"),
        ("John Doe-Smith", "John Doe Smith", "Hyphenated vs space"),
        ("John O'Brien", "John OBrien", "Apostrophe vs without"),
        ("John O'Brien", "John O Brien", "Apostrophe vs space"),
        ("Johnathan Doe", "John Doe", "Longer form of first name"),
        ("John Doe", "Johnny Doe", "Nickname variation"),
        ("", "", "Both empty"),
        (None, None, "Both null"),
        ("", None, "Empty and null"),
        (None, "", "Null and empty"),
    ]
    
    test_cases.extend(basic_test_cases)
    
    # Generate nicknames test cases
    nicknames = [
        ("William", "Bill"),
        ("Robert", "Bob"),
        ("Richard", "Dick"),
        ("Elizabeth", "Beth"),
        ("Margaret", "Peggy"),
        ("Michael", "Mike"),
        ("Charles", "Chuck"),
        ("James", "Jim"),
        ("Katherine", "Kate"),
        ("Jennifer", "Jenny"),
        ("Thomas", "Tom"),
        ("Edward", "Ed"),
        ("Theodore", "Ted"),
        ("Joseph", "Joe"),
        ("Christopher", "Chris"),
        ("Alexander", "Alex"),
        ("Daniel", "Dan"),
        ("Benjamin", "Ben"),
        ("Donald", "Don"),
        ("Matthew", "Matt"),
    ]
    
    for first, nickname in nicknames:
        test_cases.append((f"{first} Smith", f"{nickname} Smith", "Common nickname"))
    
    # International names
    international_names = [
        ("José García", "Jose Garcia", "Spanish name with/without accents"),
        ("François Dubois", "Francois Dubois", "French name with/without accents"),
        ("Jürgen Müller", "Jurgen Muller", "German name with/without umlauts"),
        ("陈伟明", "陈 伟明", "Chinese name with/without space"),
        ("김영희", "Kim Young-Hee", "Korean name in Hangul and romanized"),
        ("سعيد أحمد", "سعيد احمد", "Arabic name with/without diacritics"),
        ("Иван Петров", "Ivan Petrov", "Russian name in Cyrillic and romanized"),
        ("Χρήστος Παπαδόπουλος", "Christos Papadopoulos", "Greek name in Greek and romanized"),
        ("राम शर्मा", "Ram Sharma", "Hindi name in Devanagari and romanized"),
        ("محمد علی", "Mohammad Ali", "Persian name in Arabic script and romanized"),
    ]
    
    test_cases.extend(international_names)
    
    # Generate edge cases with special characters
    special_chars = [".", ",", "-", "'", "\"", "/", "\\", "(", ")", "!", "@", "#", "$", "%", "^", "&", "*", "+", "=", "<", ">", "?", "|", "{", "}", "[", "]", "`", "~", ";", ":"]
    
    for char in special_chars:
        test_cases.append((f"John{char}Doe", f"John Doe", f"Special character: {char}"))
        test_cases.append((f"John {char} Doe", f"John Doe", f"Special character with spaces: {char}"))
    
    # Names with numbers
    for i in range(5):
        test_cases.append((f"John{i} Doe", f"John Doe", f"Name with number {i}"))
        test_cases.append((f"John Doe {i}th", f"John Doe", f"Name with ordinal number {i}th"))
    
    # Extremely long names
    long_first_names = ["".join(random.choices(string.ascii_lowercase, k=random.randint(30, 50))) for _ in range(5)]
    long_last_names = ["".join(random.choices(string.ascii_lowercase, k=random.randint(30, 50))) for _ in range(5)]
    
    for first in long_first_names[:3]:
        for last in long_last_names[:3]:
            test_cases.append((f"{first} {last}", f"{first} {last}", "Extremely long name exact match"))
            test_cases.append((f"{first} {last}", f"{first[:len(first)-1]} {last}", "Extremely long name with one character different"))
    
    # Repeated characters
    for char in 'abcdefg':
        repeated = char * random.randint(5, 10)
        test_cases.append((f"John {repeated}", f"John {repeated}", f"Name with repeated character: {char}"))
        test_cases.append((f"John {repeated}", f"John {repeated[:-1]}", f"Name with repeated character minus one: {char}"))
    
    # Unicode confusables (characters that look similar but are different code points)
    confusables = [
        ("John Doe", "Јоhn Dое", "Latin vs Cyrillic characters"),  # Some characters are Cyrillic
        ("O'Neill", "O'Neill", "Regular apostrophe vs. right single quotation mark"),  # Different apostrophes
        ("John-Smith", "John‐Smith", "Hyphen vs. non-breaking hyphen"),  # Different hyphens
        ("John Smith Jr.", "John Smith Jr․", "Period vs. one-dot leader"),  # Different periods
    ]
    
    test_cases.extend(confusables)
    
    # Names with different spacing patterns
    spacing_variations = [
        ("John  Doe", "John Doe", "Double space vs single space"),
        ("John   Doe", "John Doe", "Triple space vs single space"),
        ("John\tDoe", "John Doe", "Tab vs space"),
        ("John\nDoe", "John Doe", "Newline vs space"),
        ("John\rDoe", "John Doe", "Carriage return vs space"),
        ("John\u00A0Doe", "John Doe", "Non-breaking space vs regular space"),
        ("John\u2002Doe", "John Doe", "En space vs regular space"),
        ("John\u2003Doe", "John Doe", "Em space vs regular space"),
        ("John\u2009Doe", "John Doe", "Thin space vs regular space"),
    ]
    
    test_cases.extend(spacing_variations)
    
    # Generate additional tests with Levenshtein distance variations
    names = ["John", "Robert", "William", "James", "Charles", "Thomas", "David", "Michael", "Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson"]
    
    for i in range(min(100, count - len(test_cases))):
        name1 = random.choice(names)
        name2 = random.choice(names)
        
        # Create variations with specific edit distances
        variations = []
        
        # 1-character different (substitution)
        if len(name1) > 0:
            pos = random.randint(0, len(name1) - 1)
            new_char = random.choice(string.ascii_lowercase)
            variation = name1[:pos] + new_char + name1[pos+1:]
            variations.append((f"{name1} {name2}", f"{variation} {name2}", "One character substitution"))
        
        # 1-character insertion
        pos = random.randint(0, len(name1))
        new_char = random.choice(string.ascii_lowercase)
        variation = name1[:pos] + new_char + name1[pos:]
        variations.append((f"{name1} {name2}", f"{variation} {name2}", "One character insertion"))
        
        # 1-character deletion
        if len(name1) > 0:
            pos = random.randint(0, len(name1) - 1)
            variation = name1[:pos] + name1[pos+1:]
            variations.append((f"{name1} {name2}", f"{variation} {name2}", "One character deletion"))
        
        # Transposition of adjacent characters
        if len(name1) > 1:
            pos = random.randint(0, len(name1) - 2)
            variation = name1[:pos] + name1[pos+1] + name1[pos] + name1[pos+2:]
            variations.append((f"{name1} {name2}", f"{variation} {name2}", "Adjacent character transposition"))
        
        test_cases.extend(variations)
    
    # Add duplicate test cases with varying whitespace to test case normalization
    duplicate_with_spaces = [
        (" John Doe ", "John Doe", "Leading and trailing spaces"),
        ("  John  Doe  ", "John Doe", "Multiple spaces everywhere"),
        ("\t John \t Doe \t", "John Doe", "Tabs and spaces mixed"),
    ]
    
    test_cases.extend(duplicate_with_spaces)
    
    # Add more random test cases if we haven't reached the target count
    while len(test_cases) < count:
        name_length1 = random.randint(1, 15)
        name_length2 = random.randint(1, 15)
        
        name1 = "".join(random.choices(string.ascii_letters, k=name_length1))
        name2 = "".join(random.choices(string.ascii_letters, k=name_length2))
        
        test_cases.append((name1, name2, f"Random name pair {len(test_cases)}"))
    
    # Keep only the requested number of test cases
    return test_cases[:count]

def write_test_cases_csv(test_cases, filename='test_cases_1000.csv'):
    """Write the test cases to a CSV file."""
    with open(filename, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['name1', 'name2', 'description'])
        for name1, name2, description in test_cases:
            # Convert None to the string 'null' for CSV
            csv_name1 = 'null' if name1 is None else name1
            csv_name2 = 'null' if name2 is None else name2
            writer.writerow([csv_name1, csv_name2, description])

if __name__ == "__main__":
    test_cases = generate_test_cases(1000)
    write_test_cases_csv(test_cases)
    print(f"Generated {len(test_cases)} test cases and saved to test_cases_1000.csv") 