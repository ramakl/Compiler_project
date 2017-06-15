
package translation;
import analysis.TypeContext;
import analysis.TypeInformation;
//import com.sun.org.apache.xpath.internal.operations.Div;
import com.sun.org.apache.xpath.internal.operations.Bool;
//import com.sun.javafx.fxml.expression.Expression;
import com.sun.org.apache.xpath.internal.operations.Neg;
import jdk.nashorn.internal.ir.Block;

import minijava.ast.*;

import minillvm.ast.*;

import org.omg.CORBA._IDLTypeStub;



import static minillvm.ast.Ast.*;

import static minillvm.ast.Ast.GetElementPtr;

import static minillvm.ast.Ast.TypePointer;



import java.security.PublicKey;

import java.util.Map;

import java.util.HashMap;
import java.util.concurrent.locks.Condition;

//basicly translation overview, you get minijava program and our translator have to translate it to LLVM

// start from AST:miijava.ast.MJProgram --> then --> AST:llvm.ast.Prog

//we have 2kind of blocks:

//Minijava Block

//Basic Block

//for Simple Loop,in llvm we need at least 3basic blocks. jump to basic block

//for using Temporary variable we have to varef to it and we should make a copy

//it's only assign to it once.
public class Translator extends Element.DefaultVisitor{
	public static InstructionList BKL = InstructionList();
	BasicBlock entry = BasicBlock();
	public int o =0;
	private final MJProgram javaProg;
	// @mahsa: We need to have a block to add serveral submethods to one basic block of a parent method
	//public BasicBlock getOpenBlock() {
	//return BKL;
	//}
	Map< MJVarDecl, TemporaryVar > tempVars = new HashMap< MJVarDecl, TemporaryVar >();
	public Translator(MJProgram javaProg) {
		this.javaProg = javaProg;

	}
	public Prog translate() {
		// TODO add your translation code here
		// TODO here is an example of a minimal program (remove this)
		Prog prog = Prog(TypeStructList(), GlobalList(), ProcList());
		BasicBlockList blocks = BasicBlockList();
		//BasicBlockList blocks = BKL;
		Proc mainProc = Proc("main", TypeInt(), ParameterList(), blocks);
		prog.getProcedures().add(mainProc);
		entry.setName("entry");
		//blocks.add(entry);
		for (MJStatement stmt : javaProg.getMainClass().getMainBody()) {
			Object match = stmt.match(new StmtMatcher());
			if(match instanceof Instruction)
			{
				entry.add((Instruction)match);
			}
			//stmt.match(new StmtMatcher());
		}
		//for (Instruction i: BKL){

		//entry.add(i);
		//}
		entry.add(ReturnExpr(ConstInt(0)));
		blocks.add(entry);
		prog.accept(this);
		//For-loop to read each stmt of main class -> main bod
		//for (MJStatement stmt : javaProg.getMainClass().getMainBody()) {
		//   stmt.match(new StmtMatcher());
		//}
		return prog;
	}
	//: we should do this for the first part (Block, StmtIf, StmtWhile, StmtReturn, StmtPrint, StmtExpr,
	//StmtAssign, ExprBinary, ExprUnary, BoolConst, VarUse, Number.)
	private class StmtMatcher implements MJElement.Matcher {
		@Override
		public Object case_Program(MJProgram program) {
			return null;

		}
		@Override
		public Object case_FieldAccess(MJFieldAccess fieldAccess) {

			return null;
		}
		@Override
		public Object case_MethodDecl(MJMethodDecl methodDecl) {

			return null;

		}
		@Override
		public Object case_VarDecl(MJVarDecl varDecl) {
			MJType type = varDecl.getType();
			Type llvmtype = type.match(new MJType.Matcher<Type>() {
				@Override
				public Type case_TypeBool(MJTypeBool typeBool) {
					return TypeBool();
				}

				@Override
				public Type case_TypeIntArray(MJTypeIntArray typeIntArray) {
					return TypeArray(TypeInt(), typeIntArray.size());
				}

				@Override
				public Type case_TypeClass(MJTypeClass typeClass) {
					return null;
				}

				@Override
				public Type case_TypeInt(MJTypeInt typeInt) {
					return TypeInt();
				}
			});
			TemporaryVar x = TemporaryVar(varDecl.getName());
			entry.add(Alloca(x,llvmtype));
			//Hesitated about varDecl in tempVars
			tempVars.put(varDecl, x);
			return VarRef(x);

		}
		@Override
		public Object case_Plus(MJPlus plus) {
			return Add();
		}
		//stm-while
		@Override
		public Object case_StmtWhile(MJStmtWhile stmtWhile) {
			MJExpr condition = stmtWhile.getCondition();

			MJStatement loopBody = stmtWhile.getLoopBody();

			loopBody.match(new StmtMatcher()); //looping through the body

			//how to get the TrueLabel and FalseLabel?

			//use Branch and Jump statement together

			return null;
		}
		@Override
		public Object case_MethodCall(MJMethodCall methodCall) {
			return null;
		}
		@Override
		public Object case_Negate(MJNegate negate) {

			return null;

		}
		@Override
		public Object case_And(MJAnd and) {

			return And();
		}
		//ExprUnary
		@Override
		public Object case_ExprUnary(MJExprUnary exprUnary) {
			MJExpr ex= exprUnary.getExpr();
			MJUnaryOperator  o=exprUnary.getUnaryOperator();
			Object e=ex.match(new StmtMatcher());
			Object ad=o.match(new StmtMatcher());
			if(ad instanceof Sub){
				//Sub(VarRef(x));???? how we can use sub instead binary operation
				TemporaryVar result = TemporaryVar("ss");
				entry.add((Instruction) BinaryOperation(result,(ConstInt(0)),(Operator)ad,(Operand)(e)));
				return VarRef(result);
			}
			return null;
		}
		@Override
		public Object case_Times(MJTimes times) {

			return Mul();

		}
		@Override
		public Object case_ExtendsNothing(MJExtendsNothing extendsNothing) {

			return null;

		}
		//Number
		@Override
		//Number writen by @rama
		public Operand case_Number(MJNumber number) {
			int val = number.getIntValue();
			return ConstInt(val);

		}
		//VarUse of the Matcher
		@Override
		public Object case_VarUse(MJVarUse varUse) {
			MJVarUse varDecl = varUse;
			MJVarDecl varDeclvar = varDecl.getVariableDeclaration();
			//TemporaryVar y = TemporaryVar("y"+varUse.getVarName());

			if (tempVars.containsKey(varDeclvar)) {
				TemporaryVar x = tempVars.get(varDeclvar);
				//BKL.add(Load(y, varDeclvar));
				//BKL.add(Load(y, varDeclvar.getSourcePosition().toString()));
				//BKL.add(Load(y,VarRef(x)));
				return VarRef(x);
				//return x;
			} else {
				// This should never happen
				throw new RuntimeException(
						"Variable not found during translation");
			}
		}
		@Override
		public Object case_ExprList(MJExprList exprList) {
			return null;
		}
		//stm-assign
		@Override
		public Object case_StmtAssign(MJStmtAssign stmtAssign) {

			MJExpr left =stmtAssign.getLeft();
			//Operand leftOp = get_L(left);
			Operand leftOp=(Operand) left.match(new StmtMatcher());
			MJExpr right=stmtAssign.getRight();
			//Operand rightOp = get_R(right);
			Operand rightOp=(Operand) right.match(new StmtMatcher());
			entry.add(Store(leftOp,rightOp));
			return ConstInt(0);

		}
		@Override
		public Object case_TypeInt(MJTypeInt typeInt) {

			return TypeInt();

		}
		@Override
		public Object case_Equals(MJEquals equals) {

			return Eq();

		}
		@Override
		public Object case_Less(MJLess less) {

			return  Slt();

		}
		@Override
		public Object case_Div(MJDiv div) {

			return Sdiv();

		}
		@Override
		public Object case_NewObject(MJNewObject newObject) {

			return null;

		}

		//Block
		@Override
		public Object case_Block(MJBlock block) {
            o=o+1;
			InstructionList i = null;
			//BasicBlockList blockss = BasicBlockList();
			BasicBlock bloc = BasicBlock();
			bloc.setName("bloc"+o+"");
			for(MJStatement statement : block)
			{
				Object statementt=statement.match(new StmtMatcher());
                if(statementt instanceof Instruction)
				{

				bloc.add((Instruction)statementt);
				}
			}

			return bloc;
		}
		@Override
		public Object case_ClassDeclList(MJClassDeclList classDeclList) {

			return null;

		}
		//ExprBinary
		@Override
		public Object case_ExprBinary(MJExprBinary exprBinary) {

			MJExpr left=exprBinary.getLeft();

			Object l=left.match(new StmtMatcher());

			MJExpr right=exprBinary.getRight();

			Object r=right.match(new StmtMatcher());
			//Operand r = get_R(exprBinary.getRight());

			//Operand l = get_L(exprBinary.getLeft());

			//MJOperator op= exprBinary.getOperator();

			MJOperator  op = exprBinary.getOperator();

			Object ad = op.match(new StmtMatcher());

			//Operand runOp = op.match();

			//TemporaryVar x=TemporaryVar(l.toString());

			//TemporaryVar y=TemporaryVar(r.toString());
			//TemporaryVar R=TemporaryVar(l.toString());
			//BinaryOperation(R,VarRef(x),(Operator) ad,VarRef(y));
			TemporaryVar result = TemporaryVar("s");
			entry.add((Instruction) BinaryOperation(result,(Operand)(l),(Operator)ad,(Operand)(r)));
			//addToAssign(BinaryOperation(result,VarRef(x),(Operator) ad,VarRef(y)));
			//return (Operand)(result);
			return VarRef(result);
		}
		//stm-return
		@Override
		public Object case_StmtReturn(MJStmtReturn stmtReturn) {
			MJExpr e= stmtReturn.getResult();

			Object u=e.match(new StmtMatcher());
			if(u instanceof Operand )
			{
				entry.add(ReturnExpr((Operand)u));
			}
			else {
				entry.add(ReturnExpr(ConstInt(Integer.parseInt(u.toString()))));
			}
			return null;

			//we cannot use Integer.parseInt if there is, for example return 2.0;

		}
		//stm-expr
		@Override
		public Object case_StmtExpr(MJStmtExpr stmtExpr) {
			MJExpr ex=stmtExpr.getExpr();
			return ex.match(new StmtMatcher());
		}
		@Override
		public Object case_Minus(MJMinus minus) {

			return Sub();

		}
		@Override
		public Operand case_ExprNull(MJExprNull exprNull) {

			return Nullpointer();
		}
		@Override
		public Object case_ClassDecl(MJClassDecl classDecl) {

			return null;

		}
		//stm-print writen by @rama
		@Override
		public Object case_StmtPrint(MJStmtPrint stmtPrint) {
			MJExpr ex=  stmtPrint.getPrinted();
			Object u=ex.match(new StmtMatcher());
			//Operand u = get_L(ex);
			if(u instanceof Operand){
				if (u instanceof TypePointer){
					//entry.add(Print(ConstInt(1)));
					return ((Instruction)Print(ConstInt(1)));
				}
				else {
					//entry.add(Print((Operand) u));

					return ((Instruction)Print((Operand) u));
				}
				//return ConstInt(0);
			}
			else if(u != null){
				//entry.add((Instruction)Print(ConstInt(Integer.parseInt(u.toString()))));

				return ((Instruction)Print(ConstInt(Integer.parseInt(u.toString()))));

			}
			//TemporaryVar g=TemporaryVar(u.toString());

			// Parameter xx= Parameter(TypeInt(), u.toString());

			//ParameterList().add(xx);

			else

				return null;

		}
		@Override
		public Object case_ExtendsClass(MJExtendsClass extendsClass) {

			return null;

		}
		@Override
		public Object case_MainClass(MJMainClass mainClass) {

			return null;

		}
		//BoolConst
		@Override
		public Object case_BoolConst(MJBoolConst boolConst) {

			return boolConst.getBoolValue();

		}
		@Override
		public Object case_TypeClass(MJTypeClass typeClass) {

			return null;

		}
		@Override
		public Object case_NewIntArray(MJNewIntArray newIntArray) {

			return null;

		}
		@Override
		public Object case_TypeIntArray(MJTypeIntArray typeIntArray) {

			return null;

		}
		//stm-if
		@Override
		public Object case_StmtIf(MJStmtIf stmtIf) {
			MJExpr co =stmtIf.getCondition();
			MJStatement t =stmtIf.getIfTrue();
			MJStatement f =stmtIf.getIfFalse();
			//BasicBlock trueLabel = (BasicBlock) t;
			//BasicBlock falseLabel = (BasicBlock) f;
		//Operand o = (Operand) co;
			Object coo=co.match(new StmtMatcher());
			Object tt=t.match(new StmtMatcher());
			Object ff=f.match(new StmtMatcher());
			BasicBlock trueLabel = (BasicBlock) tt;
			BasicBlock falseLabel = (BasicBlock) ff;
			TemporaryVar x=TemporaryVar(coo.toString());
            /*BasicBlock block1 = BasicBlock(//Load(a1, VarRef(x)));
            block1.setName("b1");
            BasicBlock block2 = BasicBlock(//Load(a2, VarRef(y)));
            block2.setName("b2");
            BasicBlock block3 = BasicBlock(
                                  //PhiNode(a, TypeInt(), PhiNodeChoiceList(
                                 // PhiNodeChoice(block1, Ast.VarRef(a1)),
                                // PhiNodeChoice(block2, Ast.VarRef(a2))
                                //) );
            block3.setName("b3");
            block1.add(Jump(block3));
            block2.add(Jump(block3));
            BasicBlockList blocks = BasicBlockList(
               block1, block2, block3
            );
            */
			//Branch(VarRef(x), tt, ff);
			//Branch(o, trueLabel, falseLabel); what is more crrect?
			entry.add(Branch(VarRef(x), trueLabel, falseLabel));
			return null;

		}
		@Override
		public Object case_ExprThis(MJExprThis exprThis) {

			return null;

		}
		@Override
		public Object case_VarDeclList(MJVarDeclList varDeclList) {

			return null;

		}
		@Override
		public Object case_UnaryMinus(MJUnaryMinus unaryMinus) {

			return Sub();

		}
		@Override
		public Object case_TypeBool(MJTypeBool typeBool) {

			return TypeBool();

		}
		@Override
		public Object case_ArrayLength(MJArrayLength arrayLength) {

			return null;

		}
		@Override
		public Object case_ArrayLookup(MJArrayLookup arrayLookup) {

			return null;
		}
		@Override
		public Object case_MethodDeclList(MJMethodDeclList methodDeclList) {
			return null;
		}
	}

	//This Method Gets Right side of the exp
	/*public Operand get_R(MJExpr exp) {
		Operand rightop = exp.match(new MJExpr.Matcher<Operand>() {

			@Override

			public Operand case_FieldAccess(MJFieldAccess fieldAccess) {

				return null;

			}



			@Override

			public Operand case_MethodCall(MJMethodCall methodCall) {

				return null;

			}



			@Override

			public Operand case_NewObject(MJNewObject newObject) {

				return null;

			}



			@Override

			public Operand case_ArrayLength(MJArrayLength arrayLength) {

				return null;

			}



			@Override

			public Operand case_ArrayLookup(MJArrayLookup arrayLookup) {

				return null;

			}



			@Override

			public Operand case_BoolConst(MJBoolConst boolConst) {

				TemporaryVar x = TemporaryVar(boolConst.toString());

				//addToAssign(Alloca(x, TypeBool() ));

				return ConstInt(0);



			}



			@Override

			public Operand case_ExprNull(MJExprNull exprNull) {
				TemporaryVar x = TemporaryVar("x");
				entry.add(Alloca(x,TypeNullpointer()));
				return ConstInt(0);

			}



			@Override

			public Operand case_ExprBinary(MJExprBinary exprBinary) {

				Operand right = get_R(exprBinary.getRight());

				Operand left = get_L(exprBinary.getLeft());

				Operator op = exprBinary.getOperator().match(new MJOperator.Matcher<Operator>() {

					@Override

					public Operator case_Div(MJDiv div) {

						return Sdiv();

					}



					@Override

					public Operator case_And(MJAnd and) {

						return And();

					}



					@Override

					public Operator case_Equals(MJEquals equals) {

						return Eq();

					}



					@Override

					public Operator case_Less(MJLess less) {

						return Slt();

					}



					@Override

					public Operator case_Minus(MJMinus minus) {

						return Sub();

					}



					@Override

					public Operator case_Plus(MJPlus plus) {

						return Add();

					}

					@Override

					public Operator case_Times(MJTimes times) {

						return Mul();

					}

				});

				TemporaryVar result = TemporaryVar("result" );
				entry.add(BinaryOperation(result, left, op, right));
				return VarRef(result);
			}



			@Override

			public Operand case_ExprThis(MJExprThis exprThis) {

				return null;

			}



			@Override

			public Operand case_NewIntArray(MJNewIntArray newIntArray) {

				return null;

			}



			@Override
			//Number writen by @mahsa in get_R
			public Operand case_Number(MJNumber number) {

				int val = number.getIntValue();
				return ConstInt(val);
			}
			//VarUse of get_R
			@Override
			public Operand case_VarUse(MJVarUse varUse) {
				return null;

			}
			@Override

			public Operand case_ExprUnary(MJExprUnary exprUnary) {

				return null;

			}

		});

		return rightop;

	}*/


	//This Method Gets Left side of the exp
/*	public Operand get_L(MJExpr exp) {

		//Left side of an Assign could be one of the following cases, So we should define them first

		Map<MJNewObject, MJClassDecl> objCls = new HashMap<MJNewObject,MJClassDecl>();

		Map<MJVarUse, MJVarDecl> varUseDecl = new HashMap<MJVarUse,MJVarDecl>();

		Map<MJFieldAccess, MJVarDecl> fieldAccVarDecl = new HashMap<MJFieldAccess,MJVarDecl>();

		Map<MJMethodCall, MJMethodDecl> methodCallsDecl = new HashMap<MJMethodCall,MJMethodDecl>();

		//return 212 doesn't mean anything

		Operand leftop = exp.match(new MJExpr.Matcher<Operand>() {

			@Override

			public Operand case_FieldAccess(MJFieldAccess fieldAccess) {

				return null;

			}



			@Override

			public Operand case_MethodCall(MJMethodCall methodCall) {

				return null;

			}



			@Override

			public Operand case_NewObject(MJNewObject newObject) {

				return null;

			}



			@Override

			public Operand case_ArrayLength(MJArrayLength arrayLength) {

				return null;

			}



			@Override

			public Operand case_ArrayLookup(MJArrayLookup arrayLookup) {

				return null;

			}



			@Override

			public Operand case_BoolConst(MJBoolConst boolConst) {

				return null;

			}



			@Override

			public Operand case_ExprNull(MJExprNull exprNull) {

				return null;

			}



			@Override

			public Operand case_ExprBinary(MJExprBinary exprBinary) {

				return null;



			}



			@Override

			public Operand case_ExprThis(MJExprThis exprThis) {

				return null;

			}



			@Override

			public Operand case_NewIntArray(MJNewIntArray newIntArray) {

				return null;
			}

			@Override
			// Number writen by @Mahsa in get_L
			public Operand case_Number(MJNumber number) {

				int val = number.getIntValue();
				return ConstInt(val);
			}

			//VarUse of get_L
			@Override

			public Operand case_VarUse(MJVarUse varUse) {

				MJVarUse varDecl = (MJVarUse) exp;
				MJVarDecl varDeclvar = varDecl.getVariableDeclaration();
				//TemporaryVar y = TemporaryVar("y"+varUse.getVarName());

				if (tempVars.containsKey(varDeclvar)) {
					TemporaryVar x = tempVars.get(varDeclvar);
					//BKL.add(Load(y, varDeclvar));
					//BKL.add(Load(y, varDeclvar.getSourcePosition().toString()));
					//BKL.add(Load(y,VarRef(x)));
					return VarRef(x);
				} else {
					// This should never happen
					throw new RuntimeException(
							"Variable not found during translation");
				}


			}

			@Override

			public Operand case_ExprUnary(MJExprUnary exprUnary) {

				return null;

			}

		});
		return leftop;

	}//To do the rest of the cases*/
}



