package frontend;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.Symbol;
import minijava.ast.MJElement;
import minijava.ast.MJProgram;
import minijava.ast.MJStmtExpr;
import minijava.ast.MJExpr;
import minijava.ast.MJMethodCall;
import minijava.ast.MJNewObject;
import minijava.ast.MJStmtAssign;
import minijava.ast.MJVarUse;
import minijava.ast.MJArrayLookup;
import minijava.ast.MJFieldAccess;

import minijava.syntax.Lexer;
import minijava.syntax.MiniJavaParser;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class MJFrontend {

	/**
	 * a list of syntax errors collected while parsing
	 */
	private List<SyntaxError> syntaxErrors = new ArrayList<>();

	/**
	 * parses a MiniJava program from a Reader
	 */
	public MJProgram parse(Reader in) throws Exception {
		ComplexSymbolFactory sf = new MJSymbolFactory();
		Lexer lexer = new Lexer(sf, in);
		MiniJavaParser parser = new MiniJavaParser(lexer, sf);

		parser.onError(syntaxErrors::add);

		Symbol result = parser.parse();
		if (result != null && result.value instanceof MJProgram) {
			MJProgram program = (MJProgram) result.value;
			detectInvalidStatements(program);
			return program;
		}
		return null;
	}

	/** parses a MiniJava program from a file */
	public MJProgram parseFile(File file) throws Exception {
		try (FileReader reader = new FileReader(file)) {
			return parse(reader);
		}
	}

	/** parses a MiniJava program from the given input string */
	public MJProgram parseString(String input) throws Exception {
		return parse(new StringReader(input));
	}

	/**
	 * detects invalid statements and adds SyntaxErrors to
	 */
	private void detectInvalidStatements(MJProgram value) {
		// Post-checker
		value.accept(
                new MJElement.DefaultVisitor() {
					@Override
					public void visit(MJStmtExpr stmtExpr) {
						// Get the Expression from a statement Expression.
						// In minijava statement expression should be only Method and Object
						// Otherwise it's not a valid syntax
						MJExpr expr = stmtExpr.getExpr();
						if (!(expr instanceof MJMethodCall) || (expr instanceof MJNewObject)){
							syntaxErrors.add(new SyntaxError(stmtExpr,
									"SyntaxError-Invalid Expression found: "+expr));
						}

					}
					@Override
					public void visit(MJStmtAssign stmtAssign) {
						// From one statement Assignment we get the left side of it to check
						// In Minijava only valid to have a variable in the left side of Assignment
						// v = x , MJVarUse
						// v = x[3], MJArrayLookup
						// v = P.name, MJFieldAccess

						MJExpr leftExpr = stmtAssign.getLeft();
						if (!(leftExpr instanceof MJVarUse)
								|| (leftExpr instanceof MJArrayLookup)
								|| (leftExpr instanceof MJFieldAccess)) {
							syntaxErrors.add(new SyntaxError(stmtAssign.getLeft(),
									"SyntaxError-Invalid assignment in the left side: must be a variable. Instead "
											+ stmtAssign.getLeft() + " was found."));
						}
					}

				}

        );
	}



	/** get the syntax errors produced while parsing */
	public List<SyntaxError> getSyntaxErrors() {
		return syntaxErrors;
	}

	/**
	 * a symbol factory, which sets the source position of MJElements created by the parser
	 */
	static class MJSymbolFactory extends ComplexSymbolFactory {

		@Override
		public Symbol newSymbol(String name, int id, Location left, Location right, Object value) {
			if (value instanceof MJElement) {
				MJElement e = (MJElement) value;
				e.setSourcePosition(new SourcePosition(left.getUnit(), left.getLine(), left.getColumn(), right.getLine(), right.getColumn()));
			}
			return super.newSymbol(name, id, left, right, value);
		}

		@Override
		public Symbol newSymbol(String name, int id, Symbol l, Symbol r, Object value) {
			if (value instanceof MJElement) {
				MJElement e = (MJElement) value;
				ComplexSymbol leftS = (ComplexSymbol) l;
				ComplexSymbol rightS = (ComplexSymbol) r;
				Location left = leftS.getLeft();
				Location right = rightS.getRight();
				e.setSourcePosition(new SourcePosition(left.getUnit(), left.getLine(), left.getColumn(), right.getLine(), right.getColumn()));
			}
			return super.newSymbol(name, id, l, r, value);
		}

		@Override
		public Symbol newSymbol(String name, int id, Symbol l, Object value) {
			if (value instanceof MJElement) {
				MJElement e = (MJElement) value;
				ComplexSymbol leftS = (ComplexSymbol) l;
				Location left = leftS.getLeft();
				Location right = leftS.getRight();
				e.setSourcePosition(new SourcePosition(left.getUnit(), left.getLine(), left.getColumn(), right.getLine(), right.getColumn()));
			}
			return super.newSymbol(name, id, l, value);
		}

	}
}
