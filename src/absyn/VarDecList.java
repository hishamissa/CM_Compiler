package absyn;

public class VarDecList extends Absyn {
    public Dec head;
    public VarDecList tail;

    public VarDecList(Dec head, VarDecList tail) {
        this.head = head;
        this.tail = tail;
    }

    public void accept(AbsynVisitor visitor, int level) {
        visitor.visit(this, level);
    }
}
