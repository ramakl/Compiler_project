package calculator.ast;

/**
 * Created by rama on 4/30/2017.
 */

public class ExprPrintVisitor implements ExprVisitor {

    public String visit(Expr exp) {
        StringBuilder result = new StringBuilder();
        result.append("(");
        result.append(exp.accept(this));
        result.append(")");
        return result.toString();
    }

    public String visit(ExprAdd exA) {
        StringBuilder result = new StringBuilder();

        result.append(exA.getLeft().accept(this));
        result.append(" + ");
        result.append(exA.getRight().accept(this));

        return result.toString();
    }
    public String visit(ExprSub exS) {
        StringBuilder result = new StringBuilder();
        result.append("(");
        result.append(exS.getLeft().accept(this));
        result.append(" - ");
        result.append(exS.getRight().accept(this));
        result.append(")");
        return result.toString();
    }
    public String visit(ExprNeg exUM) {
        StringBuilder result = new StringBuilder();
        result.append("-");
        result.append(exUM.getRight().accept(this));
        return result.toString();
    }
    public String visit(ExprDiv exD) {
        StringBuilder result = new StringBuilder();
        result.append("(");
        result.append(exD.getLeft().accept(this));
        result.append(" / ");
        result.append(exD.getRight().accept(this));
        result.append(")");
        return result.toString();
    }
    public String visit(ExprMult exM) {
        StringBuilder result = new StringBuilder();
        result.append("(");
        result.append(exM.getLeft().accept(this));
        result.append(" * ");
        result.append(exM.getRight().accept(this));
        result.append(")");
        return result.toString();
    }
    public String visit(ExprNumber exN) {
        StringBuilder result = new StringBuilder();
        result.append(exN.getValue());
        return result.toString();
    }
}