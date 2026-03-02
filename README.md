# CIS 4650 - Checkpoint One

Hisham Issa | 1194466 | hissa01@uoguelph.ca | Feb 28, 2026

## The Project

This is a compiler front-end for the C- language. It takes C- source code and scans it into tokens, parses it based to the C- grammar, builds an abstract syntax tree, and recovers from errors to report them with line and column numbers.

Built using JFlex for scanning and CUP for parsing.

## Building the Compiler

Requirements: Java 8 or higher, JFlex, CUP

Build commands:
- make clean (removes all generated files)
- make (builds everything)

This generates the scanner, parser, and compiles everything to the bin directory.

## Running the Compiler

Parse a file and show the AST:
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -a yourfile.cm

Parse a file without showing the AST:
java -cp ./lib/java-cup-11b-runtime.jar:bin CM yourfile.cm

If there are errors, they'll be reported with line and column numbers. The compiler will try to find multiple errors in one run.

## Test Files

I included 5 test programs in the test directory:

test/1.cm - Clean program with no errors (tests that everything parses correctly)
test/2.cm - 3 statement-level errors from invalid tokens
test/3.cm - 3 syntax errors in statements (missing expressions, bad syntax, missing semicolon)
test/4.cm - 3 errors showing specific error messages (array indexing, if conditions, while conditions)
test/5.cm - Stress test with 10+ errors of different types

Quick test commands:
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -a test/1.cm (should show full AST)
java -cp ./lib/java-cup-11b-runtime.jar:bin CM test/4.cm (should show 3 specific errors)


## How It Works

Scanner: Recognizes all C- tokens including keywords, operators, identifiers, numbers, and handles C-style comments. Tracks line and column numbers for error reporting.

Parser: Builds a complete abstract syntax tree using a simplified expression grammar with precedence directives. Supports all C- language constructs including functions, arrays, control flow, and expressions.

Error Recovery: Reports multiple errors per file with line and column numbers. Provides context-specific error messages when possible. Continues parsing after errors to find as many problems as possible in one run.

## Example Output

Running a clean file:
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -a test/1.cm

Shows the full AST with proper indentation showing the tree structure.

Running a file with errors:
java -cp ./lib/java-cup-11b-runtime.jar:bin CM test/4.cm

Output:
Line 14, column 11: Invalid array index expression
Line 17, column 8: Invalid condition in if statement
Line 21, column 14: Invalid condition in while loop

## Notes

The compiler is designed to run on linux.socs.uoguelph.ca and has been tested there. All test files work correctly and demonstrate the different capabilities of the scanner, parser, and error recovery system.

For more details, see the project report in docs/report.pdf.