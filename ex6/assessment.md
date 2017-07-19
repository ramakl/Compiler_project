- many NullPointerExceptions, not a single more complex test works
- where is you documentation of the stack frame layout?
- calculation of the stack size
  - usage of 1 byte for boolean problematic (addressing)
- jump-and-link to $ra in epilog of main? Why? This is wrong and can lead to problems, because you are jumping to some location you do not know where...
- jumps are implemented correctly
- branches are highly fragile
  - the beqz jumps to one branch, but the jump to the other branch is missing. If the block the other jump leads to is the next one, it works, otherwise it fails
- the translation of variable reference seems to be wrong, because it leads to a lot of NullPointerExceptions
- your translation of the call seems wrong
  - you store some address to $ra, this should never be done. Instead you should always use `jal` to jump the procedure
  - you prepare some list of parameters, but you never actually store them in $a0-$a3 or on the stack
  - you never jump to the procedure, so the execution will never reach the body of the procedure you are calling
- Globals and GetElementPtr not implemented

Points: 3/15
