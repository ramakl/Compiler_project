Group 11

Monireh Pourjafarian, mpourjaf@rhrk.uni-kl.de


In total @Monireh Pair working and programming with @Rama
Q2.
Pair working and programming with @rama
Binding strength or as it called Precedence will reduce the ambiguous of grammer.
By write the grammer rules for all binary and unary operarators, we still have very ambiguous grammer with many parsing conflicts, with regards to disambiguting our grammer rules we specified precedence for all the operators and the associativity of the binary operators.
in the following form:
/* Precedences */
precedence left PLUS, MINUS;
precedence left TIMES, DIV;
precedence left UMINUS;
Then we descibe it that UMINUS has the most priority.
From cup doc 'http://www2.in.tum.de/~petter/cup2/':
More experienced grammar authors may encouter situations, in which it is helpful to fine-tune precedences of specific production alternatives. For these cases, CUP permitted to annotate grammar alternatives with a %prec TERMINAL term, which adjusted this productions alternative precedence to the precedence of the specified terminal. CUP2 also supports the concept of explicitely defining the precedence of a production alternative.
