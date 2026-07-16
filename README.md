# C- Compiler

A compiler for the C- language, written in Java. It takes C- source code and works through the whole pipeline, scanning it into tokens, parsing it into a syntax tree, checking it for semantic and type errors, and finally generating TM assembly that runs on the TM simulator.

I built this over a semester for CIS*4650 (Compilers) at the University of Guelph, across three checkpoints that each added another stage of the pipeline. It was one of the more challenging things I've built, and easily the one that taught me the most about how the languages I use every day actually work underneath.

## How it's built

The front end uses two standard tools: **JFlex** generates the lexer (the part that turns raw text into tokens), and **CUP** generates the parser (the part that turns tokens into a syntax tree). From there, semantic analysis and code generation are my own, built with a **visitor pattern** that walks the tree — one pass to check the program makes sense, another to emit code.

The stages run in order and fail safely: if parsing finds syntax errors, it skips semantic analysis; if semantic analysis finds errors, it skips code generation. Errors are reported to stderr with line numbers, and the compiler tries to recover and keep going so it can report several problems in one run instead of stopping at the first.

Requirements: Java 8+, plus JFlex and CUP (both included in `lib/`).

## Building it

```bash
make clean    # remove generated files
make          # build the whole compiler
```

This generates the lexer and parser and compiles everything into `bin/`.

## Running it

The compiler is driven by flags depending on how far through the pipeline you want to go:

```bash
# Parse and write out the abstract syntax tree (.abs)
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -a <file.cm>

# Semantic analysis + symbol table with scope info (.sym)
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -s <file.cm>

# Generate TM assembly (.tm) — only if there are no parse or semantic errors
java -cp ./lib/java-cup-11b-runtime.jar:bin CM -c <file.cm>
```

You can combine flags (`-a -s -c`) to produce all three output files at once. Running with no flags just parses and reports any errors.

## Running the generated code

The `.tm` files run on the TM simulator:

```bash
./tm <file.tm>
```

Once it's open, `g` runs the program, `t` toggles the instruction trace, `s N` steps N instructions, `r` shows the registers, `d b n` dumps n memory locations from address b, and `q` quits.

## Tests

The `test/` directory has 15 C- programs covering the cases that actually matter for a compiler — not just the happy path. A few examples:

- Basic arithmetic and loops, recursion, and arrays (including passing arrays as parameters and printing input in reverse)
- Programs that *should* be rejected: syntax errors, undefined and redefined symbols, and type mismatches
- Runtime array-bounds violations, which the generated code catches and halts on

There are also the standard sample programs — factorial, GCD, selection sort, mutual recursion — as a sanity check that real, correct programs compile and run the way they should.

## A note on the design

The part I found most interesting was the visitor pattern for the back half. Once the tree exists, semantic analysis and code generation are really just two different ways of walking the same structure, and building it that way kept each stage separate and a lot easier to reason about than trying to do everything in one pass. If you're reading this to get a sense of how I write code, the semantic analysis and code-gen stages are the parts worth looking at.

## Docs

Fuller write-ups for each checkpoint are in `docs/` — lexical and syntax analysis, semantic analysis and type checking, and code generation.
