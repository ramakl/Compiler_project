package translation;

import analysis.TypeContext;
//import com.sun.org.apache.xpath.internal.operations.Div;

import minijava.ast.*;
import minillvm.ast.*;
import static minillvm.ast.Ast.*;
import java.util.concurrent.locks.Condition;



public class Translator extends Element.DefaultVisitor {

	private final MJProgram javaProg;
	// @mahsa: We need to have a block to add serveral submethods to one basic block of a parent method
    BasicBlock BKL = BasicBlock();
    public BasicBlock getOpenBlock() {
        return BKL;
    }

	public Translator(MJProgram javaProg) {
		this.javaProg = javaProg;
	}

	public Prog translate() {
		// TODO add your translation code here

		// TODO here is an example of a minimal program (remove this)
		Prog prog = Prog(TypeStructList(), GlobalList(), ProcList());

		BasicBlockList blocks = BasicBlockList();
		Proc mainProc = Proc("main", TypeInt(), ParameterList(), blocks);
		prog.getProcedures().add(mainProc);

		BasicBlock entry = BasicBlock(
				//Print(ConstInt(42)),
				//ReturnExpr(ConstInt(0))
		);
		entry.setName("entry");
		blocks.add(entry);

        this.BKL= entry;
        BKL.add( ReturnExpr(ConstInt(0)) );
        prog.accept(this);

		return prog;



	}


	@Override
	public void visit(Print print) {
		int op = Integer.parseInt(print.getE().toString()) ;
		Print(ConstInt(op));
		super.visit(print);
	}
	@Override
	public void visit(Add add) {

		super.visit(add);

	}

	@Override
	public void visit(InstructionList instructionList) {
		for (Instruction i : instructionList ) {
			if(i instanceof MJExprBinary)
			{
				MJExpr el= ((MJExprBinary) i).getLeft();
				MJExpr er=((MJExprBinary) i).getRight();
				TemporaryVar x=TemporaryVar(el.toString());
				TemporaryVar y=TemporaryVar(er.toString());
				MJOperator op=((MJExprBinary) i).getOperator();
				TemporaryVar R=TemporaryVar(el.toString());

				if(op instanceof MJPlus)
				{
					BinaryOperation(R,VarRef(x),Add(),VarRef(y));
				}
				if(op instanceof MJMinus)
				{
					BinaryOperation(R,VarRef(x),Sub(),VarRef(y));
				}
				if(op instanceof MJDiv)
				{
					BinaryOperation(R,VarRef(x), Sdiv(),VarRef(y));

				}
				if(op instanceof MJTimes)
				{
					BinaryOperation(R,VarRef(x), Mul(),VarRef(y));

				}
				if(op instanceof MJAnd)
				{
					BinaryOperation(R,VarRef(x), And(),VarRef(y));

				}
			}
			else if(i instanceof TerminatingInstruction) {
                TerminatingInstruction ti = (TerminatingInstruction) i;
                ti.accept(new Element.DefaultVisitor() {

					@Override
					public void visit(Branch branch) {
						Operand condition = branch.getCondition();
						BasicBlock ifTrueLabel = branch.getIfTrueLabel();
						BasicBlock ifFalseLabel = branch.getIfFalseLabel();
						Branch(condition, ifTrueLabel, ifFalseLabel);
					}

					@Override
					public void visit(Jump jump) {
						BasicBlock label = jump.getLabel();
						Jump(label);
					}

					@Override
					public void visit(ReturnExpr returnExpr) {
						Operand returnValue = returnExpr.getReturnValue();
						ReturnExpr(returnValue);
					}

					@Override
					public void visit(ReturnVoid returnVoid) {
						ReturnVoid();
					}

					@Override
					public void visit(HaltWithError haltWithError) {
						String message = haltWithError.getMsg();
						HaltWithError(message);
					}
				});
            }
            else if (i instanceof Assign) {
				Assign asg = (Assign) i;
				asg.accept(new Element.DefaultVisitor() {
                    @Override
                    public void visit(Alloc alloc) {
                        super.visit(alloc);
                        //Alloc(TemporaryVar("t"), ConstInt(100))
                        TemporaryVar tpacclocvar = TemporaryVar("t");
                        addToAssign(Alloc(tpacclocvar, ConstInt(100)));

                    }

                    @Override
                    public void visit(Alloca alloca) {

                        super.visit(alloca);
                        //Alloca(TemporaryVar("x"), TypeInt())
                        TemporaryVar tpacclocavar = TemporaryVar("x");
                        addToAssign(Alloca(tpacclocavar, TypeInt()));
                    }

                    @Override
                    public void visit(BinaryOperation binaryOperation) {

                        super.visit(binaryOperation);
                        //BinaryOperation(x,ConstInt(5), Add(), ConstInt(4)),
                        TemporaryVar IndexX = TemporaryVar("X");
                        addToAssign(BinaryOperation(IndexX,ConstInt(5), Add(), ConstInt(4)));
                        //BinaryOperation(y,VarRef(x), Sdiv(), ConstInt(2))
                        TemporaryVar IndexY = TemporaryVar("Y");
                        addToAssign(BinaryOperation(IndexY,VarRef(IndexX), Sdiv(), ConstInt(2)));
                        //BinaryOperation(z,VarRef(x), Slt(), VarRef(y))
                        TemporaryVar IndexZ = TemporaryVar("Z");
                        addToAssign(BinaryOperation(IndexZ,VarRef(IndexX), Slt(), VarRef(IndexY)));


                    }

                    @Override
                    public void visit(Bitcast bitcast) {
                        super.visit(bitcast);
                    }

                    @Override
                    public void visit(Call call) {
                        super.visit(call);
                    }

                    @Override
                    public void visit(GetElementPtr getElementPtr) {
                        super.visit(getElementPtr);
                    }

                    @Override
                    public void visit(Load load) {
                        super.visit(load);
                    }

                    @Override
                    public void visit(PhiNode phiNode) {
                        super.visit(phiNode);
                    }
                });
			}

		}
	}

    @Override
	public void visit(Branch branch)
    {
        Operand condition = branch.getCondition();
        BasicBlock ifTrueLabel = branch.getIfTrueLabel();
        BasicBlock ifFalseLabel = branch.getIfFalseLabel();
        Branch(condition, ifTrueLabel, ifFalseLabel); //usage of ref?
    }
    @Override
    public  void visit(Jump jump)
    {
        BasicBlock label = jump.getLabel();
        //BKL.add(label);
        Jump(label);
    }

    @Override
    public void visit(ReturnExpr returnExpr)
    {
        Operand returnValue = returnExpr.getReturnValue();
        ReturnExpr(returnValue);
    }
    @Override
    public void visit(ReturnVoid returnVoid)
    {
        ReturnVoid();

    }
    @Override
    public void visit(HaltWithError haltWithError)
    {
        String message = haltWithError.getMsg();
        HaltWithError(message);
    }
    //Add to the Assign Block
    void addToAssign(Instruction i){
        BKL.add(i);
    }

}

