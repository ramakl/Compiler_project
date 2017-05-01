package calculator.ast;

/**
 * Created by mahsa on 01.05.17.
 */
public class ExprCalVisitor  {

    public int visit(Expr exp) {
        return exp.accept(this);
    }

    public int visit(ExprAdd exA) {
        return exA.getLeft().accept(this) + exA.getRight().accept(this);

    }
//    public String visit(ExprSub exS) {
//        StringBuilder result = new StringBuilder();
//        result.append("(");
//        result.append(exS.getLeft().accept(this));
//        result.append(" - ");
//        result.append(exS.getRight().accept(this));
//        result.append(")");
//        return result.toString();
//    }
//    public String visit(ExprNeg exUM) {
//        StringBuilder result = new StringBuilder();
//        result.append("-");
//        result.append(exUM.getRight().accept(this));
//        return result.toString();
//    }
//    public String visit(ExprDiv exD) {
//        StringBuilder result = new StringBuilder();
//        result.append("(");
//        result.append(exD.getLeft().accept(this));
//        result.append(" / ");
//        result.append(exD.getRight().accept(this));
//        result.append(")");
//        return result.toString();
//    }
//    public String visit(ExprMult exM) {
//        StringBuilder result = new StringBuilder();
//        result.append("(");
//        result.append(exM.getLeft().accept(this));
//        result.append(" * ");
//        result.append(exM.getRight().accept(this));
//        result.append(")");
//        return result.toString();
//    }
    public int visit(ExprNumber exN) {
        return exN.getValue();
    }
}