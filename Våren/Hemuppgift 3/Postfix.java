/**
 * The Postfix class implements an evaluator for integer postfix expressions.
 *
 * Postfix notation is a simple way to define and write arithmetic expressions
 * without the need for parentheses or priority rules. For example, the postfix
 * expression "1 2 - 3 4 + *" corresponds to the ordinary infix expression
 * "(1 - 2) * (3 + 4)". The expressions may contain decimal 32-bit integer
 * operands and the four operators +, -, *, and /. Operators and operands must
 * be separated by whitespace.
 *
 * @author  NN // TODO
 * @version 2013-02-01
 */
public class Postfix {
	/**
	 * Evaluates the given postfix expression.
	 * 
	 * @param expr  Arithmetic expression in postfix notation
	 * @return      The value of the evaluated expression
	 * @throws      A subclass of RuntimeException if the expression is wrong
	 */
	public static int evaluate(String expr) throws RuntimeException {
		// TODO
		return 0;
	}
	
	/**
	 * Returns true if s is an operator.
	 * An operator is one of '+', '-', '*', '/'.
	 */
	private static boolean isOperator(String s) {
		// TODO
		return false;
	}
	
	/**
	 * Returns true if s is an integer.
	 *
	 * We accept two types of integers:
	 *
	 * - the first type consists of an optional '-' 
	 *   followed by a non-zero digit
	 *   followed by zero or more digits,
	 *
	 * - the second type consists of an optional '-'
	 *   followed by a single '0'.
	 */
	private static boolean isInteger(String s) {
		// TODO
		return false;
	}
	
	/**
	 * Unit test. Run with "java -ea Postfix".
	 */
	public static void main(String[] args) {
		assert evaluate("0") == 0;
		assert evaluate("-0") == -0;
		assert evaluate("1234567890") == 1234567890;
		assert evaluate("-1234567890") == -1234567890;
		assert evaluate("1 23 +") == 1 + 23;
		assert evaluate("0 1 /") == 0 / 1;
		assert evaluate("1 2 + -3 *") == (1 + 2) * -3;
		assert evaluate("12 34 - 56 -78 + *") == (12 - 34) * (56 + -78);
		assert evaluate("1 2 + 3 * 4 - 5 /") == (((1 + 2) * 3) - 4) / 5;
		assert evaluate("2 3 4 -0 + - *") == 2 * (3 - (4 + -0));
		assert evaluate("  		1 	-2	 + ") == 1 - 2; // tabs and spaces

		assert explodes("");
		assert explodes("+");
		assert explodes("--1");
		assert explodes("-1-0");
		assert explodes("-0-1");
		assert explodes("1 +");
		assert explodes("1 2 ,");
		assert explodes("1 2 .");
		assert explodes("1 2 3 +");
		assert evaluate("4") == 4;
		assert explodes("1 2 + +");
		assert explodes("017");
		assert explodes("0x17");
		assert explodes("-03");
		assert explodes("x");
		assert explodes("1234L");
		assert explodes("9876543210"); // larger than maxint
		assert explodes("1 0 /");
		assert explodes("1 2+");
		assert explodes("1 2 3 +*");
	}
	
	/**
	 * Returns true if <code>evaluate(expr)</code> throws
	 * a subclass of RuntimeException.
	 */
	private static boolean explodes(String expr) {
		try {
			evaluate(expr);
		} catch (RuntimeException e) {
			return true;
		}
		return false;
	}
}
