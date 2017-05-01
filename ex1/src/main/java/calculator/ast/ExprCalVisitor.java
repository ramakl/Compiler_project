package calculator.ast;

/**
 * Created by mahsa on 01.05.17.
 */
public class ExprCalVisitor {
    int result = 0;
    public int visit(Expr exp) {
        return exp.accept(this);
    }

    public int visit(ExprAdd exA) {
        return exA.getLeft().accept(this) + exA.getRight().accept(this);

    }
    public int visit(ExprSub exS) {
        return exS.getLeft().accept(this) - exS.getRight().accept(this);

    }
    public int visit(ExprNeg exUM) {
        return ((-1) * exUM.getRight().accept(this));
    }
    public int visit(ExprDiv exD) {
        return exD.getLeft().accept(this) / exD.getRight().accept(this);

    }
    public int visit(ExprMult exM) {

       return exM.getLeft().accept(this) * exM.getRight().accept(this);
    }
    public int visit(ExprNumber exN) {
        return exN.getValue();
    }
}