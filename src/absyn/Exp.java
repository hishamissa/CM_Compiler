package absyn;

public abstract class Exp extends Absyn {
    public Dec dtype;  //type information for this expression
    
    // existing accept method stays the same
    public abstract void accept(AbsynVisitor visitor, int level);
}