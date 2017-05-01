package calculator.ast;

/**
 * Created by mahsa on 01.05.17.
 */
public abstract class ExprUnary extends Expr {
    private Expr right;

    public ExprUnary( Expr right) {
        this.right = right;
    }

    public Expr getRight() {
        return right;
    }
}
