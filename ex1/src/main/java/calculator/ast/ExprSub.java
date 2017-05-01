package calculator.ast;

/**
 * Created by mahsa on 27.04.17.
 */
public class ExprSub extends ExprBinary {
    public ExprSub(Expr left, Expr right) {
        super(left, right);
    }
    public String accept(  ExprVisitor v)
    {
        return v.visit(this);
    }
    public int accept(  ExprCalVisitor v)
    {
        return v.visit(this);
    }
}
