package calculator.ast;

public class ExprNeg extends Expr {
    private int value;

    public ExprNeg(int value) {
        super();
        System.out.println("----Print Value---");
        System.out.println(value);
        this.value = (0 - value);
    }

    public ExprNeg(String value) {
        this.value = Integer.parseInt(value);
    }

    public int getValue() {
        return value;
    }
}
