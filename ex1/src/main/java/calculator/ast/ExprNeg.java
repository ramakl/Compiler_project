package calculator.ast;

/**
 * Created by mahsa on 28.04.17.
 */
public class ExprNeg extends ExprBinary{
    public ExprNeg(Expr left, Expr right) {
        super(null, right);
    }
}
