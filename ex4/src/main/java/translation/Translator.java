package translation;

import analysis.TypeContext;
//import com.sun.org.apache.xpath.internal.operations.Div;
import minijava.ast.*;
import minillvm.ast.*;

import java.util.concurrent.locks.Condition;

import static minillvm.ast.Ast.*;


public class Translator extends Element.DefaultVisitor {

	private final MJProgram javaProg;

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
				Print(ConstInt(42)),
				ReturnExpr(ConstInt(0))
		);
		entry.setName("entry");
		blocks.add(entry);

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
			else if(i instanceof TerminatingInstruction)
            {
                TerminatingInstruction tI = (TerminatingInstruction) i;
                if(tI instanceof Branch)
                {
                    Operand condition = ((Branch) tI).getCondition();
                    BasicBlock ifTrueLabel = ((Branch) tI).getIfTrueLabel();
                    BasicBlock ifFalseLabel = ((Branch) tI).getIfFalseLabel();
                    Branch(condition, ifTrueLabel, ifFalseLabel); //usage of ref?
                }
                else if(tI instanceof Jump)
                {
                    BasicBlock label = ((Jump) tI).getLabel();
                    Jump(label); //usage of ref?
                }
                else if(tI instanceof ReturnExpr)
                {
                    Operand returnValue = ((ReturnExpr) tI).getReturnValue();
                    ReturnExpr(returnValue);
                }
                else if(tI instanceof ReturnVoid)
                {
                    ReturnVoid();
                }
                else if(tI instanceof HaltWithError)
                {
                    String message = ((HaltWithError) tI).getMsg();
                    HaltWithError(message);
                }

            }
		}
	}

}

