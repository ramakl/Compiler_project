package calculator.ast;

/**
 * Created by mweber on 04/04/2017.
 */
public class ExprAdd extends ExprBinary  {
    public ExprAdd(Expr left, Expr right) {
        super(left, right);
    }

    public String  accept(  ExprVisitor v)
    {
        return v.visit(this);
    }
    public int accept(  ExprCalVisitor v)
    {
        return v.visit(this);
    }
}
