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

    // Track current function being analyzed (for return type checking)
    private FunctionDec currentFunction = null;
    
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

    // Create dummy declarations for type inference/error recovery
    private static final SimpleDec dummyInt = new SimpleDec(0, 1, "");
    private static final SimpleDec dummyBool = new SimpleDec(0, 0, "");
    private static final SimpleDec dummyVoid = new SimpleDec(0, 2, "");

    // Check if a declaration represents an integer type
    private boolean isInteger(Dec dec) {
        if (dec instanceof SimpleDec) {
            return ((SimpleDec) dec).typ == 1;
        } else if (dec instanceof ArrayDec) {
            return ((ArrayDec) dec).typ == 1;
        } else if (dec instanceof FunctionDec) {
            return ((FunctionDec) dec).typ == 1;
        }
        return false;
    }

    // Check if a declaration represents a boolean type
    private boolean isBool(Dec dec) {
        if (dec instanceof SimpleDec) {
            return ((SimpleDec) dec).typ == 0;
        } else if (dec instanceof ArrayDec) {
            return ((ArrayDec) dec).typ == 0;
        } else if (dec instanceof FunctionDec) {
            return ((FunctionDec) dec).typ == 0;
        }
        return false;
    }

    // Check if a declaration represents a void type
    private boolean isVoid(Dec dec) {
        if (dec instanceof SimpleDec) {
            return ((SimpleDec) dec).typ == 2;
        } else if (dec instanceof FunctionDec) {
            return ((FunctionDec) dec).typ == 2;
        }
        return false;
    }

    // Check if two types match (for assignments and equality)
    private boolean typesMatch(Dec type1, Dec type2) {
        if (type1 == null || type2 == null) {
            return false;
        }
        // Both SimpleDec
        if (type1 instanceof SimpleDec && type2 instanceof SimpleDec) {
            return ((SimpleDec) type1).typ == ((SimpleDec) type2).typ;
        }
        // Both ArrayDec
        if (type1 instanceof ArrayDec && type2 instanceof ArrayDec) {
            return ((ArrayDec) type1).typ == ((ArrayDec) type2).typ;
        }
        // Both FunctionDec
        if (type1 instanceof FunctionDec && type2 instanceof FunctionDec) {
            return ((FunctionDec) type1).typ == ((FunctionDec) type2).typ;
        }
        // Array element type matches simple type (for array indexing)
        if (type1 instanceof SimpleDec && type2 instanceof ArrayDec) {
            return ((SimpleDec) type1).typ == ((ArrayDec) type2).typ;
        }
        if (type1 instanceof ArrayDec && type2 instanceof SimpleDec) {
            return ((ArrayDec) type1).typ == ((SimpleDec) type2).typ;
        }
        
        return false;
    }

    // Count number of parameters (excluding void)
    private int countParams(VarDecList params, boolean hasVoidParam) {
        if (params == null || hasVoidParam) {
            return 0;
        }
        
        int count = 0;
        VarDecList current = params;
        while (current != null) {
            count++;
            current = current.tail;
        }
        return count;
    }

    // Count number of arguments
    private int countArgs(ExpList args) {
        if (args == null) {
            return 0;
        }
        
        int count = 0;
        ExpList current = args;
        while (current != null) {
            count++;
            current = current.tail;
        }
        return count;
    }

    // Check that argument types match parameter types
    private void checkArgumentTypes(VarDecList params, ExpList args, int pos, String funcName) {
        VarDecList currentParam = params;
        ExpList currentArg = args;
        int argNum = 1;
        
        while (currentParam != null && currentArg != null) {
            if (currentParam.head != null && currentArg.head != null && currentArg.head.dtype != null) {
                Dec paramType = currentParam.head;
                Dec argType = currentArg.head.dtype;
                
                // Special case: array parameters (size 0) can match array variables
                if (paramType instanceof ArrayDec && ((ArrayDec) paramType).size == 0) {
                    // Parameter is array type - argument must be array of same base type
                    if (argType instanceof ArrayDec) {
                        ArrayDec paramArr = (ArrayDec) paramType;
                        ArrayDec argArr = (ArrayDec) argType;
                        if (paramArr.typ != argArr.typ) {
                            hasSemanticErrors = true;
                            System.err.println("Error at line " + (pos + 1) + 
                                ": Argument " + argNum + " type mismatch in call to '" + funcName + "'");
                        }
                    } else {
                        hasSemanticErrors = true;
                        System.err.println("Error at line " + (pos + 1) + 
                            ": Argument " + argNum + " type mismatch in call to '" + funcName + "' (expected array)");
                    }
                } else if (!typesMatch(paramType, argType)) {
                    hasSemanticErrors = true;
                    System.err.println("Error at line " + (pos + 1) + 
                        ": Argument " + argNum + " type mismatch in call to '" + funcName + "'");
                }
            }
            
            currentParam = currentParam.tail;
            currentArg = currentArg.tail;
            argNum++;
        }
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
        
        // Enter function scope and track current function
        FunctionDec savedFunction = currentFunction;
        currentFunction = dec;
        enterScope("function " + dec.name);
        
        // Process parameters at current level
        if (dec.params != null) {
            dec.params.accept(this, this.level);
        }
        
        // Process function body
        if (dec.body != null) {
            dec.body.accept(this, this.level);
        }
        
        // Exit function scope and restore previous function
        exitScope("function " + dec.name);
        currentFunction = savedFunction;
    }
    
    public void visit(FunctionProtoDec dec, int level) {
        // Insert function prototype into symbol table
        insert(dec.name, dec);
    }
    
    public void visit(SimpleVar var, int level) {
        // Look up variable in symbol table
        NodeType entry = lookup(var.name);
        
        if (entry == null) {
            hasSemanticErrors = true;
            System.err.println("Error at line " + (var.pos + 1) + 
                ": Undefined variable '" + var.name + "'");
            // For error recovery, assume it's an int
            var.dtype = dummyInt;
        } else {
            // Set dtype to the declaration
            var.dtype = entry.def;
        }
    }
    
    public void visit(IndexVar var, int level) {
        // Look up array variable in symbol table
        NodeType entry = lookup(var.name);
        
        if (entry == null) {
            hasSemanticErrors = true;
            System.err.println("Error at line " + (var.pos + 1) + 
                ": Undefined array '" + var.name + "'");
            var.dtype = dummyInt;
        } else {
            // Check that it's actually an array
            if (!(entry.def instanceof ArrayDec)) {
                hasSemanticErrors = true;
                System.err.println("Error at line " + (var.pos + 1) + 
                    ": Variable '" + var.name + "' is not an array");
                var.dtype = dummyInt;
            } else {
                ArrayDec arrDec = (ArrayDec) entry.def;
                // Array access returns the element type
                var.dtype = new SimpleDec(var.pos, arrDec.typ, "");
            }
        }
        
        // Visit the index expression
        if (var.index != null) {
            var.index.accept(this, this.level);
            
            // Check that index is an integer
            if (var.index.dtype != null && !isInteger(var.index.dtype)) {
                hasSemanticErrors = true;
                System.err.println("Error at line " + (var.pos + 1) + 
                    ": Array index must be an integer");
            }
        }
    }
    
    public void visit(IntExp exp, int level) {
        // Integer literals are always type int
        exp.dtype = dummyInt;
    }
    
    public void visit(BoolExp exp, int level) {
        // Boolean literals are always type bool
        exp.dtype = dummyBool;
    }
    
    public void visit(VarExp exp, int level) {
        // Visit the variable to set its dtype
        if (exp.variable != null) {
            exp.variable.accept(this, this.level);
            // VarExp gets its type from the variable
            exp.dtype = exp.variable.dtype;
        }
    }
    
    public void visit(CallExp exp, int level) {
        // Visit arguments first
        if (exp.args != null) {
            exp.args.accept(this, this.level);
        }
        
        // Look up function
        NodeType entry = lookup(exp.func);
        if (entry == null) {
            hasSemanticErrors = true;
            System.err.println("Error at line " + (exp.pos + 1) + 
                ": Undefined function '" + exp.func + "'");
            exp.dtype = dummyInt;
            return;
        }
        
        if (!(entry.def instanceof FunctionDec)) {
            hasSemanticErrors = true;
            System.err.println("Error at line " + (exp.pos + 1) + 
                ": '" + exp.func + "' is not a function");
            exp.dtype = dummyInt;
            return;
        }
        
        FunctionDec func = (FunctionDec) entry.def;
        
        // Check argument count and types
        VarDecList params = func.params;
        ExpList args = exp.args;
        
        // Handle void parameters (represented as single SimpleDec with empty name)
        boolean hasVoidParam = false;
        if (params != null && params.head instanceof SimpleDec) {
            SimpleDec firstParam = (SimpleDec) params.head;
            if (firstParam.name.isEmpty() && firstParam.typ == 2) {
                hasVoidParam = true;
            }
        }
        
        // Check argument count
        int paramCount = countParams(params, hasVoidParam);
        int argCount = countArgs(args);
        
        if (paramCount != argCount) {
            hasSemanticErrors = true;
            System.err.println("Error at line " + (exp.pos + 1) + 
                ": Function '" + exp.func + "' expects " + paramCount + 
                " argument(s) but got " + argCount);
        } else if (!hasVoidParam) {
            // Check argument types
            checkArgumentTypes(params, args, exp.pos, exp.func);
        }
        
        // Function call has the return type of the function
        exp.dtype = new SimpleDec(exp.pos, func.typ, "");
    }
    
    public void visit(OpExp exp, int level) {
        // Visit left operand (if present - unary ops have null left)
        if (exp.left != null) {
            exp.left.accept(this, this.level);
        }
        // Visit right operand
        if (exp.right != null) {
            exp.right.accept(this, this.level);
        }
        // Type check based on operator
        switch (exp.op) {
            case OpExp.PLUS:
            case OpExp.MINUS:
            case OpExp.TIMES:
            case OpExp.OVER:
                // Arithmetic operators: both operands must be int, result is int
                if (exp.left != null && exp.left.dtype != null && !isInteger(exp.left.dtype)) {
                    hasSemanticErrors = true;
                    System.err.println("Error at line " + (exp.pos + 1) + 
                        ": Left operand of arithmetic operator must be integer");
                }
                if (exp.right != null && exp.right.dtype != null && !isInteger(exp.right.dtype)) {
                    hasSemanticErrors = true;
                    System.err.println("Error at line " + (exp.pos + 1) + 
                        ": Right operand of arithmetic operator must be integer");
                }
                exp.dtype = dummyInt;
                break;
                
            case OpExp.LT:
            case OpExp.LE:
            case OpExp.GT:
            case OpExp.GE:
                // Relational operators: both operands must be int, result is bool
                if (exp.left != null && exp.left.dtype != null && !isInteger(exp.left.dtype)) {
                    hasSemanticErrors = true;
                    System.err.println("Error at line " + (exp.pos + 1) + 
                        ": Left operand of relational operator must be integer");
                }
                if (exp.right != null && exp.right.dtype != null && !isInteger(exp.right.dtype)) {
                    hasSemanticErrors = true;
                    System.err.println("Error at line " + (exp.pos + 1) + 
                        ": Right operand of relational operator must be integer");
                }
                exp.dtype = dummyBool;
                break;
                
            case OpExp.EQ:
            case OpExp.NE:
                // Equality operators: operands must be same type, result is bool
                if (exp.left != null && exp.right != null && 
                    exp.left.dtype != null && exp.right.dtype != null) {
                    if (!typesMatch(exp.left.dtype, exp.right.dtype)) {
                        hasSemanticErrors = true;
                        System.err.println("Error at line " + (exp.pos + 1) + 
                            ": Operands of equality operator must have the same type");
                    }
                }
                exp.dtype = dummyBool;
                break;
                
            case OpExp.AND:
            case OpExp.OR:
                // Boolean operators: operands can be int or bool, result is bool
                exp.dtype = dummyBool;
                break;
                
            case OpExp.NOT:
                // Unary NOT: operand can be int or bool, result is bool
                exp.dtype = dummyBool;
                break;
                
            case OpExp.UMINUS:
                // Unary minus: operand must be int, result is int
                if (exp.right != null && exp.right.dtype != null && !isInteger(exp.right.dtype)) {
                    hasSemanticErrors = true;
                    System.err.println("Error at line " + (exp.pos + 1) + 
                        ": Operand of unary minus must be integer");
                }
                exp.dtype = dummyInt;
                break;
                
            default:
                exp.dtype = dummyInt;
        }
    }
    
    public void visit(AssignExp exp, int level) {
        // Visit left-hand side (variable reference)
        if (exp.lhs != null) {
            exp.lhs.accept(this, this.level);
        }
        
        // Visit right-hand side (expression)
        if (exp.rhs != null) {
            exp.rhs.accept(this, this.level);
        }
        
        // Check type compatibility
        if (exp.lhs != null && exp.rhs != null && 
            exp.lhs.dtype != null && exp.rhs.dtype != null) {
            
            // Check for void assignment
            if (isVoid(exp.rhs.dtype)) {
                hasSemanticErrors = true;
                System.err.println("Error at line " + (exp.pos + 1) + 
                    ": Cannot assign void value");
            }
            // Check type match
            else if (!typesMatch(exp.lhs.dtype, exp.rhs.dtype)) {
                hasSemanticErrors = true;
                System.err.println("Error at line " + (exp.pos + 1) + 
                    ": Type mismatch in assignment");
            }
        }
        
        // Assignment expression has the type of the LHS
        if (exp.lhs != null) {
            exp.dtype = exp.lhs.dtype;
        }
    }
    
    public void visit(IfExp exp, int level) {
        // Visit and check test expression
        if (exp.test != null) {
            exp.test.accept(this, this.level);
            
            // Test condition can be int or bool, but not void
            if (exp.test.dtype != null && isVoid(exp.test.dtype)) {
                hasSemanticErrors = true;
                System.err.println("Error at line " + (exp.pos + 1) + 
                    ": If condition cannot be void");
            }
        }
        
        // Visit then part
        if (exp.thenpart != null) {
            exp.thenpart.accept(this, this.level);
        }
        
        // Visit else part if present
        if (exp.elsepart != null) {
            exp.elsepart.accept(this, this.level);
        }
    }
    
    public void visit(WhileExp exp, int level) {
        // Visit and check test expression
        if (exp.test != null) {
            exp.test.accept(this, this.level);
            
            // Test condition can be int or bool, but not void
            if (exp.test.dtype != null && isVoid(exp.test.dtype)) {
                hasSemanticErrors = true;
                System.err.println("Error at line " + (exp.pos + 1) + 
                    ": While condition cannot be void");
            }
        }
        
        // Visit body
        if (exp.body != null) {
            exp.body.accept(this, this.level);
        }
    }
    
    public void visit(ReturnExp exp, int level) {
        // Visit the return expression if present
        if (exp.exp != null) {
            exp.exp.accept(this, this.level);
        }
        
        // Check return type matches function return type
        if (currentFunction != null) {
            if (exp.exp == null) {
                // Return with no value - function should be void
                if (currentFunction.typ != 2) {
                    hasSemanticErrors = true;
                    System.err.println("Error at line " + (exp.pos + 1) + 
                        ": Function '" + currentFunction.name + "' must return a value");
                }
            } else {
                // Return with value
                if (currentFunction.typ == 2) {
                    hasSemanticErrors = true;
                    System.err.println("Error at line " + (exp.pos + 1) + 
                        ": Void function '" + currentFunction.name + "' cannot return a value");
                } else if (exp.exp.dtype != null) {
                    // Check type match
                    SimpleDec expectedType = new SimpleDec(0, currentFunction.typ, "");
                    if (!typesMatch(expectedType, exp.exp.dtype)) {
                        hasSemanticErrors = true;
                        System.err.println("Error at line " + (exp.pos + 1) + 
                            ": Return type mismatch in function '" + currentFunction.name + "'");
                    }
                }
            }
        }
    }
    
    public void visit(CompoundExp exp, int level) {
        // CompoundExp is used for:
        // Function bodies (already in function scope, don't create new scope)
        // Nested blocks inside if/while (should create new scope)
        
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
