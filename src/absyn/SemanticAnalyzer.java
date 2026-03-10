package absyn;

import java.util.HashMap;
import java.util.ArrayList;

public class SemanticAnalyzer implements AbsynVisitor {
    
    // Symbol table: maps names to list of NodeType entries (for nested scopes)
    private HashMap<String, ArrayList<NodeType>> table;
    
    // Current nesting level (0=global, 1=function, 2+=blocks)
    private int level;
    
    // Track if semantic errors occurred
    public static boolean hasSemanticErrors = false;
    
    // Constructor
    public SemanticAnalyzer() {
        table = new HashMap<String, ArrayList<NodeType>>();
        level = 0;
        hasSemanticErrors = false;
    }
    
    // Helper to convert type int to string
    private String typeToString(int type) {
        switch(type) {
            case 0: return "bool";
            case 1: return "int";
            case 2: return "void";
            default: return "unknown";
        }
    }
    
    // Visitor methods - all empty for now, we'll fill them in later phases
    
    public void visit(SimpleDec dec, int level) {
        // TODO
    }
    
    public void visit(ArrayDec dec, int level) {
        // TODO
    }
    
    public void visit(FunctionDec dec, int level) {
        // TODO
    }
    
    public void visit(FunctionProtoDec dec, int level) {
        // TODO
    }
    
    public void visit(SimpleVar var, int level) {
        // TODO
    }
    
    public void visit(IndexVar var, int level) {
        // TODO
    }
    
    public void visit(IntExp exp, int level) {
        // TODO
    }
    
    public void visit(BoolExp exp, int level) {
        // TODO
    }
    
    public void visit(VarExp exp, int level) {
        // TODO
    }
    
    public void visit(CallExp exp, int level) {
        // TODO
    }
    
    public void visit(OpExp exp, int level) {
        // TODO
    }
    
    public void visit(AssignExp exp, int level) {
        // TODO
    }
    
    public void visit(IfExp exp, int level) {
        // TODO
    }
    
    public void visit(WhileExp exp, int level) {
        // TODO
    }
    
    public void visit(ReturnExp exp, int level) {
        // TODO
    }
    
    public void visit(CompoundExp exp, int level) {
        // TODO
    }
    
    public void visit(NilExp exp, int level) {
        // Nothing to do for NilExp
    }
    
    public void visit(DecList list, int level) {
        while (list != null) {
            if (list.head != null) {
                list.head.accept(this, level);
            }
            list = list.tail;
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
