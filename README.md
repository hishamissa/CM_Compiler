# C- Compiler - CIS*4650

**Hisham Issa** | 1194466 | hissa01@uoguelph.ca | March 2026

## Overview

This is a complete compiler for the C- language, developed across three checkpoints for CIS*4650 (Compilers). The compiler performs lexical analysis, syntax analysis, semantic analysis, type checking, and TM assembly code generation.

Built using:
- **JFlex** for lexical analysis (scanning)
- **CUP** for syntax analysis (parsing)
- **Custom visitor pattern** for semantic analysis and code generation

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

**Parse and display Abstract Syntax Tree (`-a`):**
```bash
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -a <filename.cm>
```
Creates `<filename>.abs` containing the abstract syntax tree.

**Perform semantic analysis and display symbol table (`-s`):**
```bash
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -s <filename.cm>
```
Creates `<filename>.sym` containing the symbol table with scope information.

**Generate TM assembly code (`-c`):**
```bash
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -c <filename.cm>
```
Creates `<filename>.tm` containing TM assembly code. Only generates code if the program has no parse or semantic errors.

**Combined flags:**
```bash
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -a -s -c <filename.cm>
```
Creates `.abs`, `.sym`, and `.tm` output files.

**Parse only (no output files):**
```bash
java -cp ./lib/java-cup-11b-runtime.jar:bin CM <filename.cm>
```
Reports errors to stderr if any are found.

---

## Running Generated TM Code

Generated `.tm` files can be executed using the TM simulator:
```bash
cd TMSimulator
./tm <filename.tm>
```

Inside the simulator:
- `g` — run the program
- `t` — toggle instruction trace
- `s N` — step N instructions
- `r` — show register contents
- `d b n` — show n memory locations starting at b
- `q` — quit

---

## Test Files

The `test/` directory contains 15 C- programs:

### Required 10 Test Files
| File | Description | Expected Result |
|------|-------------|-----------------|
| `1.cm` | Basic arithmetic and while loop | Compiles and runs, outputs 10 5 50 2 |
| `2.cm` | Recursion and boolean variable | Compiles and runs, outputs factorial of input |
| `3.cm` | Global array and array parameter | Compiles and runs, outputs input in reverse |
| `4.cm` | Syntax errors | Rejected with parse errors |
| `5.cm` | Semantic errors: undefined/redefined symbols | Rejected with semantic errors |
| `6.cm` | Semantic errors: type mismatches | Rejected with semantic errors |
| `7.cm` | Runtime bounds error (above) | Compiles, outputs -2000000 at runtime |
| `8.cm` | Runtime bounds error (below) | Compiles, outputs -1000000 at runtime |
| `9.cm` | Multiple mixed errors | Rejected with parse errors |
| `0.cm` | Stress test with many errors | Rejected with parse errors |

### Provided Test Programs
| File | Description |
|------|-------------|
| `fac.cm` | Factorial (input: integer, output: factorial) |
| `gcd.cm` | Greatest common divisor (input: two integers) |
| `sort.cm` | Selection sort (input: 10 integers) |
| `booltest.cm` | Factorial using boolean variable |
| `mutual.cm` | Mutual recursion with function prototype |

### Running the Tests

**Clean programs (should compile and run):**
```bash
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -c test/1.cm
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -c test/fac.cm
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -c test/sort.cm
```

**Error programs (should be rejected):**
```bash
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -c test/4.cm
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -c test/5.cm
```

---

## Error Reporting

All errors are reported to stderr with line numbers and descriptive messages. The compiler attempts error recovery to report multiple issues in a single run.

- If syntax errors are detected, semantic analysis and code generation are skipped
- If semantic errors are detected, code generation is skipped
- Runtime errors (array bounds violations) output -1000000 (below) or -2000000 (above) and halt

---

## Project Documentation

For more details, see the `docs/` directory:
- `Checkpoint One Documentation.pdf` - Lexical and syntax analysis
- `Checkpoint Two Documentation.pdf` - Semantic analysis and type checking
