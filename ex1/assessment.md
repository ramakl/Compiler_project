What about the other members of the group?

# Lexer

Correct adaptation of the scanner.

Points: 1/1

# Parser

You added the right productions for subtraction and division as well as negation. The explanation for the precedence rules seem to not be complete, though.

The extension of the AST classes is correct as well.

Points: 3/3

# Visitor

Instead of implementing the visitor pattern once and generic enough for all algorithms, you implemented several `accept` methods with different return types. This is not correct since the visitor pattern should be implemented sufficiently generic such that all different algorithms over the AST can be implemented just by implementing the *one* visitor interface.

The general idea of the `accept` and `visit` methods seem to be clear, though.

The implementation of the pretty printer could be easily adapted to not return `String` for the `visit` methods and instead use a class field of type `StringBuilder`.

Points: 2/3

# Evaluator

The evaluator should be a subtype of the visitor interface `ExprVisitor`. The way you implemented it, it does not use the visitor pattern but reimplements everything. The general idea behind the visitor pattern is still visible in your implementation. Think about how the implementation could instead be done using `accept` methods that retun `void` and using a stack to save intermediate results.

Points: 1/3
