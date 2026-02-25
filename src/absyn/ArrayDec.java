package absyn;

public class ArrayDec extends Dec {
    public int typ;
    public String name;
    public int size;

    public ArrayDec(int pos, int typ, String name, int size) {
        this.pos = pos;
        this.typ = typ;
        this.name = name;
        this.size = size;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
