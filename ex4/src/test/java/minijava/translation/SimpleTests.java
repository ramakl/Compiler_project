package minijava.translation;


import org.junit.Test;

public class SimpleTests {

	@Test
	public void println() throws Exception {

		testStatements(
				//"System.out.println(42);"
				"System.out.println(42);"

		);
	}

	@Test
	public void test0() throws Exception {
		testStatements(
				"System.out.println(42*7+3);"
		);
	}
	@Test
	public void test55() throws Exception {
		testStatements(
				"System.out.println(-1);"
		);
	}
	@Test
	public void test1() throws Exception {
		testStatements(
				"int x;",
				"x = 42;",
				"x = x + 1;",
				"System.out.println(x);"
		);
	}
	@Test
	public void test11() throws Exception {
		testStatements(
				"int x;",
				"x = 42;"

		);
	}
	@Test
	public void test14() throws Exception {
		testStatements(
				"int [] a;","a= new int[10];"

		);
	}

    @Test
    public void test13() throws Exception {
        testStatements(
                "int x;",
                "x = 42;",
                "if (true)",
                "{System.out.println(4);System.out.println(2);}",
                "else","{System.out.println(5);}",
                "System.out.println(6);"

        );
    }
	@Test
	public void test2() throws Exception {
		testStatements(
				"int x;",
				"x = 42;",
				"while (true)",
				"	{System.out.println(x);",
				" System.out.println(3);}"
		);
	}

	private void testStatements(String...inputLines) throws Exception {
		String input = "class Main { public static void main(String[] args) {\n"
				+ String.join("\n", inputLines)
				+ "\n}}\n";
		TranslationTestHelper.testTranslation("Test.java", input);
	}


}
