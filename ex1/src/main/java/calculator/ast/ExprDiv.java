package calculator.ast;

/**
 * Created by mahsa on 27.04.17.
 */
public class ExprDiv extends ExprBinary {
    public ExprDiv(Expr left, Expr right) {   super(left, right);    }

    public String accept(  ExprVisitor v)
    {
        return v.visit(this);
    }
    public int accept(  ExprCalVisitor v)
    {
        return v.visit(this);
    }
}
