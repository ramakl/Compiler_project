## Group 11

### Implementation

- sensible type for representing objects of a class
- memory is allocated for object creation, but initialization is completely wrong
- field access is not implemented
- virtual method table only makes sense when not considering method overriding
  - overridden methods appear multiple times in the table of the subtype
  - method calls to not consider the vtable
  - multiple LLVM-procedures are generated for overridden methods
- method calls are not implemented correctly
  - receiver object not accessible from inside the method implementation
  - vtable not considered

Points: 3/12

### Theory

- the control-flow-graph is ok
- the placement of phi-nodes is wrong
  - phi-nodes need to be placed in the beginning of all blocks with more than one incoming edge of block with a definition of the same variable. In this case for exmaple n5 (because res is defined in n3 and n4)
  - you are missing the labels: the labels in phi-nodes may only refer to direct predecessors of the block the phi-node is placed in

1/3 points

Total: 4/15
