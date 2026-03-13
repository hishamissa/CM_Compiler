# C- Compiler - CIS*4650

**Hisham Issa** | 1194466 | hissa01@uoguelph.ca | March 16, 2026

## Overview

This is a compiler for the C- language, developed across multiple checkpoints for CIS 4650 (Compilers). My compiler performs lexical analysis, syntax analysis, semantic analysis, and type checking on C- source programs.

Built using:
- **JFlex** for lexical analysis (scanning)
- **CUP** for syntax analysis (parsing)
- **Custom visitor pattern** for semantic analysis

---

## Building the Compiler

**Requirements:** Java 8 or higher, JFlex, CUP (included in `lib/`)

**Build commands:**
```bash
make clean    # Removes all generated files
make          # Builds the complete compiler
```

This generates the scanner (`Lexer.java`), parser (`parser.java` and `sym.java`), and compiles everything to the `bin/` directory.

---

## Running the Compiler

### Command-Line Options

**Parse and display Abstract Syntax Tree:**
```bash
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -a <filename.cm>
```
Creates `<filename>.abs` containing the abstract syntax tree.

**Perform semantic analysis and display symbol table:**
```bash
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -s <filename.cm>
```
Creates `<filename>.sym` containing the symbol table with scope information.

**Both AST and symbol table:**
```bash
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -a -s <filename.cm>
```
Creates both `.abs` and `.sym` output files.

**Parse only (no output files):**
```bash
java -cp ./lib/java-cup-11b-runtime.jar:bin CM <filename.cm>
```
Reports errors to stderr if any are found.

### Error Reporting

All errors are reported to stderr with line numbers and descriptive messages. My compiler attempts to recover from errors and continue processing to report as many issues as possible in a single run.

**Note:** If syntax errors are detected then semantic analysis is skipped (partial ASTs are not analyzed).

---

## Test Files

The five test programs are included in the `test/` directory (more specific information about the errors can be found in the header comments inside each file):

**test/1.cm**: Clean program with no errors
- Tests: global variables, arrays, functions with parameters, local variables, function calls, control flow, arithmetic and boolean operations
- Expected: No errors, complete symbol table with proper scoping

**test/2.cm**: Symbol table errors (3 errors)
- Redefined variable in same scope
- Undefined variable reference
- Undefined function call

**test/3.cm**: Type checking errors (3 errors)
- Type mismatch in arithmetic operation (bool and int)
- Type mismatch in assignment (bool to int)
- Non-integer array index (bool)

**test/4.cm**: Function call and return errors (3 errors)
- Void function returning a value
- Wrong number of function arguments
- Argument type mismatch

**test/5.cm** - Comprehensive stress test (18 errors)
- Multiple error types: undefined symbols, redefinitions, type mismatches, void variables, array errors, function errors, return type mismatches

**Additional tests that were provided:**
- `test/fac.cm`: Factorial calculation
- `test/gcd.cm`: Greatest common divisor (Euclid's algorithm)

### Running the Tests
```bash
# Test clean program (should produce no errors)
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -s test/1.cm

# Test semantic errors (should report 3 specific errors)
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -s test/2.cm

# Test with both AST and symbol table output
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -a -s test/gcd.cm
```
---

## Example Output

### Successful Compilation with Symbol Table
```bash
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -s test/gcd.cm
```

**Output:**
```
Symbol table saved to test/gcd.sym
```

**test/gcd.sym contents:**
```
Entering scope: global
  Entering scope: function gcd
    u: int
    v: int
  Leaving scope: function gcd
  Entering scope: function main
    x: int
    y: int
  Leaving scope: function main
  gcd: (int, int) -> int
  main: (void) -> void
Leaving scope: global
```

### Error Detection
```bash
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -s test/3.cm
```

**Output:**
```
Error at line 15: Left operand of arithmetic operator must be integer
Error at line 16: Type mismatch in assignment
Error at line 17: Array index must be an integer

Semantic analysis completed with errors.
Symbol table saved to test/3.sym
```

---

## Project Documentation

For more details, see the project documentation for each checkpoint. These can be found in the `docs/` directory:
- `Checkpoint One Documentation.pdf` - Lexical and syntax analysis design and implementation
- `Checkpoint Two Documentation.pdf` - Semantic analysis and type checking design and implementation

---