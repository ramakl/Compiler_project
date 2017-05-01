package calculator.ast;

public abstract class Expr {

    String accept(ExprVisitor visitor) {
        return visitor.visit(this);

    }
    int accept(ExprCalVisitor visitor) {
        return visitor.visit(this);

    }
}

