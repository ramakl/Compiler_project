package calculator;

import calculator.ast.Expr;
import exprs.ExprParser.ParserError;
import org.junit.Test;

import static calculator.Main.parseString;
import static calculator.Main.prettyPrint;
import static org.junit.Assert.assertEquals;

/**
 * This class tests the parser with some input strings.
 * <p>
 * Before you run this test you have to make the method Main.parseToAST public.
 **/
public class ParserTest {

    @Test
    public void testOk1() throws Exception {
        String input = "((5*3) + 4)";
        Expr e = parseString(input);
        String output = prettyPrint(e);
        assertEquals("((5 * 3) + 4)", output);
    }

    @Test
    public void testOk2() throws Exception {
        String input = "2 + 3";
        Expr e = parseString(input);
        String output = prettyPrint(e);
        assertEquals("(2 + 3)", output);
    }

    @Test
    public void testOk3() throws Exception {
        String input = "2 + 3 * 4";
        Expr e = parseString(input);
        String output = prettyPrint(e);
        assertEquals("(2 + (3 * 4))", output);
    }

    @Test
    public void testOk4() throws Exception {
        String input = "2 * 3 + 4 * 5";
        Expr e = parseString(input);
        System.out.println(input);
        String output = prettyPrint(e);
        assertEquals("((2 * 3) + (4 * 5))", output);
    }

    @Test
    public void testOk5() throws Exception {
        String input = "-5";
        Expr e = parseString(input);
        String output = prettyPrint(e);
        assertEquals("(-5)", output);
    }

    @Test(expected = ParserError.class)
    public void testFail1() throws Exception {
        String input = "((5*3) + 4";
        Main.parseString(input);
    }

    @Test(expected = ParserError.class)
    public void testFail2() throws Exception {
        String input = "3+";
        Main.parseString(input);
    }

}
