package calculator.ast;

/**
 * Created by rama on 4/30/2017.
 */
public  interface ExprVisitor {

    public String visit(Expr exp);
    public String visit(ExprAdd exA);
    public String visit(ExprDiv exD);
    public String visit(ExprMult exM);
    public String visit(ExprSub exS);
    public String visit(ExprNeg exUM);
    public String visit(ExprNumber exN);

   /*{

       *//*   *//**//**//**//*  if (e instanceof ExpAdd) {
                ExpAdd t= (ExpAdd) e;
                return visit(t.getleft())+visit(t.getright());
            } else if (e instanceof MinusExp) {
                MinusExp t= (MinusExp) e;
                return visit(t.getleft())-visit(t.getright());
            } else if (e instanceof NumExp) {
                NumExp t= (NumExp) e;
                return t.getdatum();*//**//**//**//*
            }
        *//*
    }*/

}
