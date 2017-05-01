package calculator.ast;

public class ExprNeg extends ExprUnary {
    public ExprNeg( Expr right) {
        super(right);
    }

    public String  accept(  ExprVisitor v)
    {
        return v.visit(this);
    }
    public int  accept(  ExprCalVisitor v)
    {
        return v.visit(this);
    }
}
