package absyn;

public class ShowTreeVisitor implements AbsynVisitor {
    
    private static final int INDENT = 4;
    
    private void indent(int level) {
        for (int i = 0; i < level * INDENT; i++) {
            System.out.print(" ");
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
        System.out.println("SimpleDec: " + dec.name + " (type: " + typeToString(dec.typ) + ")");
    }
    
    public void visit(ArrayDec dec, int level) {
        indent(level);
        if (dec.size == 0) {
            System.out.println("ArrayDec: " + dec.name + "[] (type: " + typeToString(dec.typ) + ")");
        } else {
            System.out.println("ArrayDec: " + dec.name + "[" + dec.size + "] (type: " + typeToString(dec.typ) + ")");
        }
    }
    
    public void visit(FunctionDec dec, int level) {
        indent(level);
        System.out.println("FunctionDec: " + dec.name + " (return type: " + typeToString(dec.typ) + ")");
        level++;
        indent(level);
        System.out.println("Parameters:");
        if (dec.params != null) {
            dec.params.accept(this, level + 1);
        }
        indent(level);
        System.out.println("Body:");
        if (dec.body != null) {
            dec.body.accept(this, level + 1);
        }
    }
    
    public void visit(FunctionProtoDec dec, int level) {
        indent(level);
        System.out.println("FunctionProtoDec: " + dec.name + " (return type: " + typeToString(dec.typ) + ")");
        level++;
        indent(level);
        System.out.println("Parameters:");
        if (dec.params != null) {
            dec.params.accept(this, level + 1);
        }
    }
    
    // Variable visitors
    
    public void visit(SimpleVar var, int level) {
        indent(level);
        System.out.println("SimpleVar: " + var.name);
    }
    
    public void visit(IndexVar var, int level) {
        indent(level);
        System.out.println("IndexVar: " + var.name);
        indent(level + 1);
        System.out.println("Index:");
        var.index.accept(this, level + 2);
    }
    
    // Expression visitors
    
    public void visit(IntExp exp, int level) {
        indent(level);
        System.out.println("IntExp: " + exp.value);
    }
    
    public void visit(BoolExp exp, int level) {
        indent(level);
        System.out.println("BoolExp: " + exp.value);
    }
    
    public void visit(VarExp exp, int level) {
        indent(level);
        System.out.println("VarExp:");
        exp.variable.accept(this, level + 1);
    }
    
    public void visit(CallExp exp, int level) {
        indent(level);
        System.out.println("CallExp: " + exp.func);
        if (exp.args != null) {
            indent(level + 1);
            System.out.println("Arguments:");
            exp.args.accept(this, level + 2);
        }
    }
    
    public void visit(OpExp exp, int level) {
        indent(level);
        System.out.print("OpExp: ");
        switch(exp.op) {
            case OpExp.PLUS:
                System.out.println("+");
                break;
            case OpExp.MINUS:
                System.out.println("-");
                break;
            case OpExp.TIMES:
                System.out.println("*");
                break;
            case OpExp.OVER:
                System.out.println("/");
                break;
            case OpExp.EQ:
                System.out.println("==");
                break;
            case OpExp.NE:
                System.out.println("!=");
                break;
            case OpExp.LT:
                System.out.println("<");
                break;
            case OpExp.LE:
                System.out.println("<=");
                break;
            case OpExp.GT:
                System.out.println(">");
                break;
            case OpExp.GE:
                System.out.println(">=");
                break;
            case OpExp.AND:
                System.out.println("&&");
                break;
            case OpExp.OR:
                System.out.println("||");
                break;
            case OpExp.NOT:
                System.out.println("!");
                break;
            case OpExp.UMINUS:
                System.out.println("- (unary)");
                break;
            default:
                System.out.println("unknown op");
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
        System.out.println("AssignExp:");
        indent(level + 1);
        System.out.println("LHS:");
        exp.lhs.accept(this, level + 2);
        indent(level + 1);
        System.out.println("RHS:");
        exp.rhs.accept(this, level + 2);
    }
    
    public void visit(IfExp exp, int level) {
        indent(level);
        System.out.println("IfExp:");
        indent(level + 1);
        System.out.println("Test:");
        exp.test.accept(this, level + 2);
        indent(level + 1);
        System.out.println("Then:");
        exp.thenpart.accept(this, level + 2);
        if (exp.elsepart != null) {
            indent(level + 1);
            System.out.println("Else:");
            exp.elsepart.accept(this, level + 2);
        }
    }
    
    public void visit(WhileExp exp, int level) {
        indent(level);
        System.out.println("WhileExp:");
        indent(level + 1);
        System.out.println("Test:");
        exp.test.accept(this, level + 2);
        indent(level + 1);
        System.out.println("Body:");
        exp.body.accept(this, level + 2);
    }
    
    public void visit(ReturnExp exp, int level) {
        indent(level);
        System.out.println("ReturnExp:");
        if (exp.exp != null) {
            exp.exp.accept(this, level + 1);
        }
    }
    
    public void visit(CompoundExp exp, int level) {
        indent(level);
        System.out.println("CompoundExp:");
        if (exp.decs != null) {
            indent(level + 1);
            System.out.println("Local declarations:");
            exp.decs.accept(this, level + 2);
        }
        if (exp.exps != null) {
            indent(level + 1);
            System.out.println("Statements:");
            exp.exps.accept(this, level + 2);
        }
    }
    
    public void visit(NilExp exp, int level) {
        indent(level);
        System.out.println("NilExp");
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