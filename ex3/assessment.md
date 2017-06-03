- 24 standard tests fail
- it is bad style to compare parts of the AST by comparing the String representation with `equalsIgnoreCase`
- correct class name analysis (uniqueness of class/field/method names, acyclic inheritance, existence of super classes)
- check that args name of main method does not appear as local variable ok
- type check of main procedure implemented, but still buggy ()
- type check of method bodies missing (-7)

7 points

### Theory

for-rule

- we asked for one type rule for a for-loop. Why do you give use multiple rules?
- `i` cannot appear in e0, which is why you should not add `i` to the type environment when checking e0
- in MiniJava, there are no subtypes of `int`. You can directly type e0 as int
- I do not get what you mean with tau_e1. You wrote that e1 should be typed to bool, right?
- You cannot type statements to a type, since you do not know that they are expression. `s2` can, for example, also be a sequence of statements, then typing it to `int` does not make any sense


derivation

- your application of the sequence rule does not make sense. You cannot simply move the type environment into the statements
- in 3) you apply the var-decl rule only to the declaration of y and the assignment. But you need the information about y in the remaining statements as well. The var-decl rule has to be applied first!
- in 5) you cannot type the expression `y` to type `int[]` using the int-literal rule. This rule can only be applied to int literals like 1 and 13
- in 6) the type of `y[1]` is `int`, not `int[]`. The premise `int[] < int` would not be dischargable since integer arrays are no integers!!!

1 point

overall: 8/18
