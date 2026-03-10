package absyn;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.PrintWriter;

/**
 * SemanticAnalyzer performs semantic analysis on the AST.
 * Builds symbol table, checks for undefined/redefined identifiers,
 * and performs type checking on expressions and statements.
 */
public class SemanticAnalyzer implements AbsynVisitor {
    
    // Symbol table mapping names to lists of declarations (for nested scopes)
    private HashMap<String, ArrayList<NodeType>> table;
    
    // Current nesting level (0 = global, 1 = function, 2+ = blocks)
    private int level;
    
    // Current indentation level for symbol table output
    private int indentLevel;
    
    // Output stream for symbol table
    private PrintWriter out;
    
    // Track whether any semantic errors have occurred
    public static boolean hasSemanticErrors = false;
    
    private static final int INDENT = 2;
    
    public SemanticAnalyzer(PrintWriter output) {
        table = new HashMap<String, ArrayList<NodeType>>();
        level = 0;
        indentLevel = 0;
        out = output;
        hasSemanticErrors = false;
        
        // Add predefined input() and output() functions
        insertPredefinedFunctions();
    }
    
    // Print indentation based on current nesting level
    private void indent() {
        for (int i = 0; i < indentLevel * INDENT; i++) {
            out.print(" ");
        }
    }
    
    // Convert type integer (0=bool, 1=int, 2=void) to string
    private String typeToString(int type) {
        switch(type) {
            case 0: return "bool";
            case 1: return "int";
            case 2: return "void";
            default: return "unknown";
        }
    }
    
    // Insert C- predefined functions: int input(void) and void output(int)
    private void insertPredefinedFunctions() {
        // int input(void)
        VarDecList inputParams = new VarDecList(new SimpleDec(0, 2, ""), null);
        FunctionDec inputFunc = new FunctionDec(0, 1, "input", inputParams, null);
        insert("input", inputFunc);
        
        // void output(int)
        VarDecList outputParams = new VarDecList(new SimpleDec(0, 1, ""), null);
        FunctionDec outputFunc = new FunctionDec(0, 2, "output", outputParams, null);
        insert("output", outputFunc);
    }
    
    // Insert a declaration into the symbol table
    // Reports error if name already exists at current level
    private void insert(String name, Dec dec) {
        ArrayList<NodeType> list = table.get(name);
        if (list == null) {
            list = new ArrayList<NodeType>();
            table.put(name, list);
        }
        
        // Check for redefinition at current scope level
        for (NodeType node : list) {
            if (node.level == level) {
                hasSemanticErrors = true;
                System.err.println("Error at line " + (dec.pos + 1) + 
                    ": Symbol '" + name + "' is already defined in this scope");
                return;
            }
        }
        
        // Add to front of list (most recent declaration first)
        list.add(0, new NodeType(name, dec, level));
    }
    
    // Lookup a name in the symbol table
    // Returns the most recently declared version visible at current level
    private NodeType lookup(String name) {
        ArrayList<NodeType> list = table.get(name);
        if (list == null) {
            return null;
        }
        
        // Find first declaration at or above current level (most closely nested)
        for (NodeType node : list) {
            if (node.level <= level) {
                return node;
            }
        }
        return null;
    }
    
    // Remove all declarations at given level from symbol table
    private void delete(int lev) {
        for (String key : table.keySet()) {
            ArrayList<NodeType> list = table.get(key);
            list.removeIf(node -> node.level == lev);
        }
    }
    
    // Enter a new scope (function or block)
    private void enterScope(String scopeName) {
        indent();
        out.println("Entering scope: " + scopeName);
        indentLevel++;
        level++;
    }
    
    // Exit current scope, display symbols, and remove them from table
    private void exitScope(String scopeName) {
        displayCurrentScope();
        level--;
        delete(level + 1);
        indentLevel--;
        indent();
        out.println("Leaving scope: " + scopeName);
    }
    
    // Display all symbols declared at current level
    private void displayCurrentScope() {
        for (String key : table.keySet()) {
            ArrayList<NodeType> list = table.get(key);
            for (NodeType node : list) {
                if (node.level == level) {
                    indent();
                    out.print(node.name + ": ");
                    
                    if (node.def instanceof SimpleDec) {
                        SimpleDec dec = (SimpleDec) node.def;
                        out.println(typeToString(dec.typ));
                    } else if (node.def instanceof ArrayDec) {
                        ArrayDec dec = (ArrayDec) node.def;
                        if (dec.size == 0) {
                            out.println(typeToString(dec.typ) + "[]");
                        } else {
                            out.println(typeToString(dec.typ) + "[" + dec.size + "]");
                        }
                    } else if (node.def instanceof FunctionDec) {
                        FunctionDec dec = (FunctionDec) node.def;
                        out.print("(" + getParamTypes(dec.params) + ") -> " + typeToString(dec.typ));
                        out.println();
                    }
                }
            }
        }
    }
    
    // Build parameter type string for function signature
    private String getParamTypes(VarDecList params) {
        if (params == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        VarDecList current = params;
        boolean first = true;
        
        while (current != null) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            
            if (current.head instanceof SimpleDec) {
                SimpleDec dec = (SimpleDec) current.head;
                // void parameter is represented as SimpleDec with empty name
                if (dec.name.isEmpty() && dec.typ == 2) {
                    sb.append("void");
                } else {
                    sb.append(typeToString(dec.typ));
                }
            } else if (current.head instanceof ArrayDec) {
                ArrayDec dec = (ArrayDec) current.head;
                sb.append(typeToString(dec.typ) + "[]");
            }
            
            current = current.tail;
        }
        
        return sb.toString();
    }
    
    public void visit(SimpleDec dec, int level) {
        // Check for void variable (semantic error)
        if (dec.typ == 2 && !dec.name.isEmpty()) {
            hasSemanticErrors = true;
            System.err.println("Error at line " + (dec.pos + 1) + 
                ": Variable '" + dec.name + "' cannot be of type void");
            // For error recovery, treat as int
            dec.typ = 1;
        }
        
        // Insert into symbol table (insert handles redefinition errors)
        if (!dec.name.isEmpty()) {
            insert(dec.name, dec);
        }
    }
    
    public void visit(ArrayDec dec, int level) {
        // Check for void array (semantic error)
        if (dec.typ == 2) {
            hasSemanticErrors = true;
            System.err.println("Error at line " + (dec.pos + 1) + 
                ": Array '" + dec.name + "' cannot be of type void");
            // For error recovery, treat as int
            dec.typ = 1;
        }
        
        // Insert into symbol table
        insert(dec.name, dec);
    }
    
    public void visit(FunctionDec dec, int level) {
        // Insert function into symbol table at current level
        insert(dec.name, dec);
        
        // Enter function scope
        enterScope("function " + dec.name);
        
        // Process parameters at current level (level was already incremented by enterScope)
        if (dec.params != null) {
            dec.params.accept(this, this.level);
        }
        
        // Process function body
        if (dec.body != null) {
            dec.body.accept(this, this.level);
        }
        
        // Exit function scope
        exitScope("function " + dec.name);
    }
    
    public void visit(FunctionProtoDec dec, int level) {
        // Insert function prototype into symbol table
        insert(dec.name, dec);
    }
    
    public void visit(SimpleVar var, int level) {
    }
    
    public void visit(IndexVar var, int level) {
    }
    
    public void visit(IntExp exp, int level) {
    }
    
    public void visit(BoolExp exp, int level) {
    }
    
    public void visit(VarExp exp, int level) {
    }
    
    public void visit(CallExp exp, int level) {
    }
    
    public void visit(OpExp exp, int level) {
    }
    
    public void visit(AssignExp exp, int level) {
    }
    
    public void visit(IfExp exp, int level) {
        // Visit test expression
        if (exp.test != null) {
            exp.test.accept(this, this.level);
        }
        
        // Visit then part (creates new scope if it's a compound statement)
        if (exp.thenpart != null) {
            exp.thenpart.accept(this, this.level);
        }
        
        // Visit else part if present
        if (exp.elsepart != null) {
            exp.elsepart.accept(this, this.level);
        }
    }
    
    public void visit(WhileExp exp, int level) {
        // Visit test expression
        if (exp.test != null) {
            exp.test.accept(this, this.level);
        }
        
        // Visit body
        if (exp.body != null) {
            exp.body.accept(this, this.level);
        }
    }
    
    public void visit(ReturnExp exp, int level) {
    }
    
    public void visit(CompoundExp exp, int level) {
        // CompoundExp is used for:
        // 1. Function bodies (already in function scope, don't create new scope)
        // 2. Nested blocks inside if/while (should create new scope)
        
        // Process local declarations
        if (exp.decs != null) {
            exp.decs.accept(this, this.level);
        }
        
        // Process statements  
        if (exp.exps != null) {
            exp.exps.accept(this, this.level);
        }
    }
        
    public void visit(NilExp exp, int level) {
    }
    
    public void visit(DecList list, int level) {
        // Global scope - only entered once at start
        boolean isGlobal = (this.level == 0);
        
        if (isGlobal) {
            enterScope("global");
        }
        
        // Process all declarations
        while (list != null) {
            if (list.head != null) {
                list.head.accept(this, this.level);
            }
            list = list.tail;
        }
        
        if (isGlobal) {
            // Check that main() exists and is the last declaration
            NodeType mainFunc = lookup("main");
            if (mainFunc == null) {
                hasSemanticErrors = true;
                System.err.println("Error: Program must have a 'main' function");
            }
            
            exitScope("global");
        }
    }
    
    public void visit(VarDecList list, int level) {
        while (list != null) {
            if (list.head != null) {
                list.head.accept(this, level);
            }
            list = list.tail;
        }
    }
    
    public void visit(ExpList list, int level) {
        while (list != null) {
            if (list.head != null) {
                list.head.accept(this, level);
            }
            list = list.tail;
        }
    }
}
