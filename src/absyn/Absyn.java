package absyn;

public abstract class Absyn {
    public int pos;

    public abstract void accept(AbsynVisitor visitor, int level);
}
