package absyn;

public class FunctionDec extends Dec {
    public int typ;
    public String name;
    public VarDecList params;
    public Exp body;

    public FunctionDec(int pos, int typ, String name, VarDecList params, Exp body) {
        this.pos = pos;
        this.typ = typ;
        this.name = name;
        this.params = params;
        this.body = body;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
