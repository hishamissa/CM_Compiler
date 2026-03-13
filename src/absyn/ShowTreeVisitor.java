package absyn;

import java.io.PrintWriter;

public class ShowTreeVisitor implements AbsynVisitor {
    
    private static final int INDENT = 4;

    private PrintWriter out;

    public ShowTreeVisitor(PrintWriter output) {
        this.out = output;
    }

    // Keep backward compatibility for existing usage
    public ShowTreeVisitor() {
        this.out = new PrintWriter(System.out, true);
    }
    
    private void indent(int level) {
        for (int i = 0; i < level * INDENT; i++) {
            out.print(" ");
        }
    }
    
    private String typeToString(int type) {
        switch(type) {
            case 0: return "bool";
            case 1: return "int";
            case 2: return "void";
            default: return "unknown";
        }
    }
    
    // Declaration visitors
    
    public void visit(SimpleDec dec, int level) {
        indent(level);
        out.println("SimpleDec: " + dec.name + " (type: " + typeToString(dec.typ) + ")");
    }
    
    public void visit(ArrayDec dec, int level) {
        indent(level);
        if (dec.size == 0) {
            out.println("ArrayDec: " + dec.name + "[] (type: " + typeToString(dec.typ) + ")");
        } else {
            out.println("ArrayDec: " + dec.name + "[" + dec.size + "] (type: " + typeToString(dec.typ) + ")");
        }
    }
    
    public void visit(FunctionDec dec, int level) {
        indent(level);
        out.println("FunctionDec: " + dec.name + " (return type: " + typeToString(dec.typ) + ")");
        level++;
        indent(level);
        out.println("Parameters:");
        if (dec.params != null) {
            dec.params.accept(this, level + 1);
        }
        indent(level);
        out.println("Body:");
        if (dec.body != null) {
            dec.body.accept(this, level + 1);
        }
    }
    
    public void visit(FunctionProtoDec dec, int level) {
        indent(level);
        out.println("FunctionProtoDec: " + dec.name + " (return type: " + typeToString(dec.typ) + ")");
        level++;
        indent(level);
        out.println("Parameters:");
        if (dec.params != null) {
            dec.params.accept(this, level + 1);
        }
    }
    
    // Variable visitors
    
    public void visit(SimpleVar var, int level) {
        indent(level);
        out.println("SimpleVar: " + var.name);
    }
    
    public void visit(IndexVar var, int level) {
        indent(level);
        out.println("IndexVar: " + var.name);
        indent(level + 1);
        out.println("Index:");
        var.index.accept(this, level + 2);
    }
    
    // Expression visitors
    
    public void visit(IntExp exp, int level) {
        indent(level);
        out.println("IntExp: " + exp.value);
    }
    
    public void visit(BoolExp exp, int level) {
        indent(level);
        out.println("BoolExp: " + exp.value);
    }
    
    public void visit(VarExp exp, int level) {
        indent(level);
        out.println("VarExp:");
        exp.variable.accept(this, level + 1);
    }
    
    public void visit(CallExp exp, int level) {
        indent(level);
        out.println("CallExp: " + exp.func);
        if (exp.args != null) {
            indent(level + 1);
            out.println("Arguments:");
            exp.args.accept(this, level + 2);
        }
    }
    
    public void visit(OpExp exp, int level) {
        indent(level);
        out.print("OpExp: ");
        switch(exp.op) {
            case OpExp.PLUS:
                out.println("+");
                break;
            case OpExp.MINUS:
                out.println("-");
                break;
            case OpExp.TIMES:
                out.println("*");
                break;
            case OpExp.OVER:
                out.println("/");
                break;
            case OpExp.EQ:
                out.println("==");
                break;
            case OpExp.NE:
                out.println("!=");
                break;
            case OpExp.LT:
                out.println("<");
                break;
            case OpExp.LE:
                out.println("<=");
                break;
            case OpExp.GT:
                out.println(">");
                break;
            case OpExp.GE:
                out.println(">=");
                break;
            case OpExp.AND:
                out.println("&&");
                break;
            case OpExp.OR:
                out.println("||");
                break;
            case OpExp.NOT:
                out.println("!");
                break;
            case OpExp.UMINUS:
                out.println("- (unary)");
                break;
            default:
                out.println("unknown op");
        }
        
        if (exp.left != null) {
            exp.left.accept(this, level + 1);
        }
        if (exp.right != null) {
            exp.right.accept(this, level + 1);
        }
    }
    
    public void visit(AssignExp exp, int level) {
        indent(level);
        out.println("AssignExp:");
        indent(level + 1);
        out.println("LHS:");
        exp.lhs.accept(this, level + 2);
        indent(level + 1);
        out.println("RHS:");
        exp.rhs.accept(this, level + 2);
    }
    
    public void visit(IfExp exp, int level) {
        indent(level);
        out.println("IfExp:");
        indent(level + 1);
        out.println("Test:");
        exp.test.accept(this, level + 2);
        indent(level + 1);
        out.println("Then:");
        exp.thenpart.accept(this, level + 2);
        if (exp.elsepart != null) {
            indent(level + 1);
            out.println("Else:");
            exp.elsepart.accept(this, level + 2);
        }
    }
    
    public void visit(WhileExp exp, int level) {
        indent(level);
        out.println("WhileExp:");
        indent(level + 1);
        out.println("Test:");
        exp.test.accept(this, level + 2);
        indent(level + 1);
        out.println("Body:");
        exp.body.accept(this, level + 2);
    }
    
    public void visit(ReturnExp exp, int level) {
        indent(level);
        out.println("ReturnExp:");
        if (exp.exp != null) {
            exp.exp.accept(this, level + 1);
        }
    }
    
    public void visit(CompoundExp exp, int level) {
        indent(level);
        out.println("CompoundExp:");
        if (exp.decs != null) {
            indent(level + 1);
            out.println("Local declarations:");
            exp.decs.accept(this, level + 2);
        }
        if (exp.exps != null) {
            indent(level + 1);
            out.println("Statements:");
            exp.exps.accept(this, level + 2);
        }
    }
    
    public void visit(NilExp exp, int level) {
        indent(level);
        out.println("NilExp");
    }
    
    // List visitors
    
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