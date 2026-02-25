package absyn;

public class FunctionProtoDec extends Dec {
    public int typ;
    public String name;
    public VarDecList params;

    public FunctionProtoDec(int pos, int typ, String name, VarDecList params) {
        this.pos = pos;
        this.typ = typ;
        this.name = name;
        this.params = params;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
