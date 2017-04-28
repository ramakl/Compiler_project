package calculator.ast;

public abstract class ExprBinary extends Expr {
    private Expr left;
    private Expr right;

    public ExprBinary(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }

    public Expr getLeft() {
        return left;
    }

    public Expr getRight() {
        return right;
    }
}
