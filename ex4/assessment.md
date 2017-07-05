## Group 11

### Initial

- over 50 tests failing
- many cases not implemented
- translation of null makes no sense (assigning a null pointer to some variable which you can never use again because you do not return it)
- array length not implemented
- tried to implement bound checks, but you compare between the index and the array pointer instead of the length
- translation of `new int[]` does not allocate any memory
- branches always jump from a common entry block to a common end block, which is wrong for sequence of statements
- implementation of the ==0 check for division is wrong (should be the other way round)
- in many places, it seems like you try to find work-arounds for problems instead of fixing the solution (e.g. in the assignment)
- printing and binary operations work (kind of)

### Final version

- Assignment of `null` to an array variable not working correctly (-1 point)
  - you should not allocate a nullpointer
  - you assign 0 instead of `nullpointer`, which gives an error message because of type mismatch
- you used phi-nodes in the initialization of arrays, which would have to be removed for the translation to MIPS
- an array-length expression can never occur at the left-hand side of an assignment. Why did you implement it in `get_L`?
  - same for binary expressions, unary expressions and numbers

Points: 14/15
