package calculator.ast;

public class ExprNumber extends Expr {
	private int value;

	public ExprNumber(int value) {
		super();
		this.value = value;
	}

	public ExprNumber(String value) {
	    this.value = Integer.parseInt(value);
  }

	public int getValue() {
		return value;
	}


	public String  accept(  ExprVisitor v)
	{
		return v.visit(this);
	}
}
