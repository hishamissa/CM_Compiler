package absyn;

public abstract class Var extends Absyn {
    public Dec dtype;  // Type information for this variable reference
    
    public abstract void accept(AbsynVisitor visitor, int level);
}