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

Points: 2/15 (to be adapted after the next submission)
