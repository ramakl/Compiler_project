package translation;

import analysis.TypeContext;
import analysis.TypeInformation;
//import com.sun.org.apache.xpath.internal.operations.Div;


import com.sun.org.apache.xpath.internal.operations.Bool;

//import com.sun.javafx.fxml.expression.Expression;

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
    private final MJProgram javaProg;
    // @mahsa: We need to have a block to add serveral submethods to one basic block of a parent method


    //public BasicBlock getOpenBlock() {
        //return BKL;
    //}

    Map< MJVarDecl, TemporaryVar > tempVars = new HashMap< MJVarDecl, TemporaryVar >();;

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

        BasicBlock entry = BasicBlock(
                //Print(ConstInt(42)),
                //ReturnExpr(ConstInt(0))

        );

        entry.setName("entry");
        //blocks.add(entry);
		for (MJStatement stmt : javaProg.getMainClass().getMainBody()) {
            //Object match = stmt.match(new StmtMatcher());
            stmt.match(new StmtMatcher());
            for (Instruction i: BKL){
                entry.add(i.copy());
            }


		}
//		for (Instruction i: BKL){
//		    entry.add(i);
//		}
        entry.add(ReturnExpr(ConstInt(0)));
        blocks.add(entry);
        prog.accept(this);
        //For-loop to read each stmt of main class -> main body
        //for (MJStatement stmt : javaProg.getMainClass().getMainBody()) {
         //   stmt.match(new StmtMatcher());


		//}
		return prog;
    }

//: we should do this for the first üart (Block, StmtIf, StmtWhile, StmtReturn, StmtPrint, StmtExpr,
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
            //addToAssign(Alloca(x.copy(),llvmtype));
            //blocks.add(BKL);

            //return ConstInt(0);
            BKL.add(Alloca(x,llvmtype));
            return VarRef(x.copy());

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
			TemporaryVar x=TemporaryVar(e.toString());
			Object ad=o.match(new StmtMatcher());
			if(ad instanceof Sub){
				//Sub(VarRef(x));???? how we can use sub instead binary operation
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
		public Object case_Number(MJNumber number) {
			int x=number.getIntValue();
			return x;
		}
        //VarUse
		@Override
		public Object case_VarUse(MJVarUse varUse) {
//		    VarRef v = null;
//            for(int i=BKL.size()-1; i>0;i--)
//            {
//                if(BKL.get(i) instanceof Store)
//                {
//                    Store s = (Store) BKL.get(i);
//                    if(s.getAddress().toString().contains(varUse.getVarName()) ) {
//                        v = (VarRef) s.getValue();
//                        break;
//                    }
//                }
//            }
//			return v;
			return ConstInt(0);
		}

		@Override
		public Object case_ExprList(MJExprList exprList) {
			return null;
		}
        //stm-assign
		@Override
		public Object case_StmtAssign(MJStmtAssign stmtAssign) {
			MJExpr left =stmtAssign.getLeft();
			Operand leftOp = get_L(left);

			MJExpr right=stmtAssign.getRight();
            Operand rightOp = get_R(right);

            //addToAssign(Store(leftOp.copy(),rightOp.copy()));

			return Store(leftOp,rightOp);
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
			return null;
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
			InstructionList i = null;
			for(MJStatement statement : block)
			{
				i.add((Instruction)statement);
			}
			return BasicBlock(i);

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
            Operator op = exprBinary.getOperator().match(new MJOperator.Matcher<Operator>() {
                     @Override
                     public Operator case_Plus(MJPlus plus) {
                         return Add();
                     }

                     @Override
                     public Operator case_Div(MJDiv div) {
                         return null;
                     }

                     @Override
                     public Operator case_Times(MJTimes times) {
                         return Mul();
                     }

                     @Override
                     public Operator case_Less(MJLess less) {
                         return null;
                     }

                     @Override
                     public Operator case_And(MJAnd and) {
                         return null;
                     }

                     @Override
                     public Operator case_Minus(MJMinus minus) {
                         return null;
                     }

                     @Override
                     public Operator case_Equals(MJEquals equals) {
                         return null;
                     }
                 });

            //Object ad = op.match(new StmtMatcher());
			//Operand runOp = op.match();
			TemporaryVar x=TemporaryVar(l.toString());
			TemporaryVar y=TemporaryVar(r.toString());

			//TemporaryVar R=TemporaryVar(l.toString());
			//BinaryOperation(R,VarRef(x),(Operator) ad,VarRef(y));

			TemporaryVar result = TemporaryVar(
					"BOpResultLine" + exprBinary.getSourcePosition().getLine());
			BKL.add((Instruction) BinaryOperation(result.copy(),VarRef(x),op,VarRef(y)));
			//Parameter(TypeInt(), x.toString());
			//Parameter xx= Parameter(TypeInt(), x.toString());
			//Parameter yy= Parameter(TypeInt(), y.toString());
			//ParameterList().add(xx);
			//ParameterList().add(yy);


			//addToAssign(BinaryOperation(result,VarRef(x),(Operator) ad,VarRef(y)));
			return VarRef(result.copy());
			//return VarRef(R);
		}

        //stm-return
		@Override
		public Object case_StmtReturn(MJStmtReturn stmtReturn) {
		MJExpr e=	stmtReturn.getResult();
			Object u=e.match(new StmtMatcher());

			return ReturnExpr(ConstInt(Integer.parseInt(u.toString())));
			//return null;
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
        //stm-print
		@Override
		public Object case_StmtPrint(MJStmtPrint stmtPrint) {
			MJExpr ex=  stmtPrint.getPrinted();
		    Object u=ex.match(new StmtMatcher());

		    if(u instanceof Operand){
                BKL.add((Instruction)Print((Operand) u));
			}
			else if(u != null){
                BKL.add((Instruction)Print(ConstInt(Integer.parseInt(u.toString()))));
            }
				//TemporaryVar g=TemporaryVar(u.toString());
			//	Parameter xx= Parameter(TypeInt(), u.toString());
			//ParameterList().add(xx);

            else
                return null;


			// ReturnExpr(ConstInt(0));
//            Operand printed = get_R(stmtPrint.getPrinted());
//            Print print = Print(printed);
//            addToAssign(print);
//			return  ConstInt(0);


        return ConstInt(0);
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
			return null;
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
			Operand o = (Operand) co;
			Object coo=co.match(new StmtMatcher());
			Object tt=t.match(new StmtMatcher());

 			Object ff=f.match(new StmtMatcher());
			BasicBlock trueLabel = (BasicBlock) tt;
			BasicBlock falseLabel = (BasicBlock) ff;
			TemporaryVar x=TemporaryVar(coo.toString());
			/*BasicBlock block1 = BasicBlock(
					//Load(a1, VarRef(x))
			);
			block1.setName("b1");
			BasicBlock block2 = BasicBlock(
					//Load(a2, VarRef(y))
			);
			block2.setName("b2");
			BasicBlock block3 = BasicBlock(
					//PhiNode(a, TypeInt(), PhiNodeChoiceList(
					//		PhiNodeChoice(block1, Ast.VarRef(a1)),
						//	PhiNodeChoice(block2, Ast.VarRef(a2))
					//)
					);
			block3.setName("b3");
			block1.add(Jump(block3));
			block2.add(Jump(block3));
			BasicBlockList blocks = BasicBlockList(
					block1, block2, block3
			);

		*/

			//Branch(VarRef(x), tt, ff);


			//Branch(o, trueLabel, falseLabel); what is more crrect?

			return Branch(VarRef(x), trueLabel, falseLabel);
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
			return null;
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

	public Operand get_R(MJExpr exp) {

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
                TemporaryVar x = TemporaryVar(exprNull.toString());
                //addToAssign(Alloca(x,TypeNullpointer()));
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
                TemporaryVar result = TemporaryVar(left.toString() );

                //addToAssign(BinaryOperation(result, left, op, right));
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
            public Operand case_Number(MJNumber number) {
                int val = number.getIntValue();
                String numberAsString = Integer.toString(val);
                TemporaryVar x = TemporaryVar(numberAsString);
                //addToAssign(Alloca(x, TypeInt() ));
                return VarRef(x);

            }

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
        }




	public Operand get_L(MJExpr exp) {
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
            public Operand case_Number(MJNumber number) {
                int val = number.getIntValue();
                String numberAsString = Integer.toString(val);
                TemporaryVar x = TemporaryVar(numberAsString);
                //addToAssign(Alloca(x, TypeInt() ));
                return VarRef(x);

            }

            @Override
            public Operand case_VarUse(MJVarUse varUse) {
                TemporaryVar varU = TemporaryVar(varUse.getVarName());
                //addToAssign();
                return VarRef(varU);
            }

            @Override
            public Operand case_ExprUnary(MJExprUnary exprUnary) {
                return null;
            }
        });

        return leftop;

    }//To do the rest of the cases





	//    //Add to the Assign Block
	//void addToAssign(Instruction i){
	//	BKL.add(i);
	//}
}





//	@Override
//	public void visit(InstructionList instructionList) {
//		for (Instruction i : instructionList ) {
//
//			if(i instanceof TerminatingInstruction) {
//                TerminatingInstruction ti = (TerminatingInstruction) i;
//                ti.accept(new Element.DefaultVisitor() {
//
//					@Override
//					public void visit(Branch branch) {
//						super.visit(branch);
//						Operand condition = branch.getCondition();
//						BasicBlock ifTrueLabel = branch.getIfTrueLabel();
//						BasicBlock ifFalseLabel = branch.getIfFalseLabel();
//                        branch = Branch(condition, ifTrueLabel, ifFalseLabel);
//                        addToAssign(branch);
//					}
//
//
//					@Override
//					public void visit(Jump jump) {
//					    super.visit(jump);
//						BasicBlock label = jump.getLabel();
//						jump = Jump(label);
//						addToAssign(jump);
//					}
//
//					@Override
//					public void visit(ReturnExpr returnExpr) {
//					    super.visit(returnExpr);
//						Operand returnValue = returnExpr.getReturnValue();
//						returnExpr = ReturnExpr(returnValue);
//						addToAssign(returnExpr);
//					}
//
//					@Override
//					public void visit(ReturnVoid returnVoid) {
//					    super.visit(returnVoid);
//						returnVoid = ReturnVoid();
//						addToAssign(returnVoid);
//					}
//
//					@Override
//					public void visit(HaltWithError haltWithError) {
//					    super.visit(haltWithError);
//						String message = haltWithError.getMsg();
//						haltWithError = HaltWithError(message);
//						addToAssign(haltWithError);
//					}
//				});
//            }//@Mahsa start working
//            else if (i instanceof Assign) {
//				Assign asg = (Assign) i;
//				asg.accept(new Element.DefaultVisitor() {
//                    @Override
//                    public void visit(Alloc alloc) {
//                        super.visit(alloc);
//                        //Alloc(TemporaryVar("t"), ConstInt(100))
//                        TemporaryVar tpacclocvar = TemporaryVar("t");
//                        addToAssign(Alloc(tpacclocvar, ConstInt(100)));
//
//                    }
//
//                    @Override
//                    public void visit(Alloca alloca) {
//
//                        super.visit(alloca);
//                        //Alloca(TemporaryVar("x"), TypeInt())
//                        TemporaryVar tpacclocavar = TemporaryVar("x");
//                        addToAssign(Alloca(tpacclocavar, TypeInt()));
//                    }
//
//                    @Override
//                    public void visit(BinaryOperation binaryOperation) {
//
//                        super.visit(binaryOperation);
//
//                        //BinaryOperation(x,ConstInt(5), Add(), ConstInt(4)),
//                        TemporaryVar IndexX = TemporaryVar("X");
//                        addToAssign(BinaryOperation(IndexX,ConstInt(5), Add(), ConstInt(4)));
//                        //BinaryOperation(y,VarRef(x), Sdiv(), ConstInt(2))
//                        TemporaryVar IndexY = TemporaryVar("Y");
//                        addToAssign(BinaryOperation(IndexY,VarRef(IndexX), Sdiv(), ConstInt(2)));
//                        //BinaryOperation(z,VarRef(x), Slt(), VarRef(y))
//                        TemporaryVar IndexZ = TemporaryVar("Z");
//                        addToAssign(BinaryOperation(IndexZ,VarRef(IndexX), Slt(), VarRef(IndexY)));
//
//
//                    }
//
//                    @Override
//                    public void visit(Bitcast bitcast) {
//                        super.visit(bitcast);
//                        TemporaryVar IndexX = TemporaryVar("X");
//                        TemporaryVar IndexY = TemporaryVar("Y");
//                        addToAssign(Alloc(IndexX, ConstInt(128)));
//                        addToAssign(Bitcast(IndexY, TypePointer(Ast.TypeByte()), VarRef(IndexX)));
//
//
//                    }
//
//                    @Override
//                    public void visit(Call call) {
//                        super.visit(call);
//                        //Call(x, ProcedureRef(f), OperandList(ConstInt(4), ConstBool(true)))
//                        BasicBlockList bbl = BasicBlockList();
//                        Proc callProc = Proc("f", TypeInt(), ParameterList(),bbl);;
//                        TemporaryVar IndexX = TemporaryVar("X");
//                        addToAssign(Call(IndexX, ProcedureRef(callProc),  OperandList(ConstInt(4),ConstBool(true))));
//                    }
//
//                    @Override
//                    public void visit(GetElementPtr getElementPtr) {
//
//                        super.visit(getElementPtr);
//                        TemporaryVar IndexX = TemporaryVar("X");
//                        TemporaryVar IndexY = TemporaryVar("Y");
//                        TypeStruct myStruct = TypeStruct("myStruct",StructFieldList(StructField(TypeBool(), "a"),StructField(TypeBool(), "b"),StructField(TypeInt(), "c")));
//                        TypePointer p = TypePointer(myStruct);
//                        //Operand baseAddress = (TypePointer) myStruct;
//                        //addToAssign(GetElementPtr(IndexX,VarRef(p),OperandList(ConstInt(0), ConstInt(1)));
//                        addToAssign(Load(IndexY ,VarRef(IndexX)));
//
//                    }
//
//                    @Override
//                    public void visit(Load load) {
//                        super.visit(load);
//                        TemporaryVar IndexX = TemporaryVar("X");
//                        TemporaryVar IndexY = TemporaryVar("Y");
//                        addToAssign(Alloca(IndexX, TypeInt()));
//                        addToAssign(Store(VarRef(IndexX), ConstInt(32)));
//                        addToAssign(Load(IndexY, VarRef(IndexX)));
//                    }
//
//                    @Override
//                    public void visit(PhiNode phiNode) {
//                        super.visit(phiNode);
//                        TemporaryVar a1 = TemporaryVar("a1");
//                        TemporaryVar IndexX = TemporaryVar("X");
//                        BasicBlock block1 = BasicBlock(
//                                Load(a1, VarRef(IndexX))
//                        );
//                        block1.setName("b1");
//                        TemporaryVar a2 = TemporaryVar("a2");
//                        TemporaryVar IndexY = TemporaryVar("Y");
//                        BasicBlock block2 = BasicBlock(
//                                Load(a2, VarRef(IndexY))
//                        );
//                        block2.setName("b2");
//                        TemporaryVar a = TemporaryVar("a");
//                        BasicBlock block3 = BasicBlock(PhiNode(a,TypeInt(),
//                                PhiNodeChoiceList(
//                                PhiNodeChoice(block1, Ast.VarRef(a1)),
//                                PhiNodeChoice(block2, Ast.VarRef(a2))
//                                )
//                        ));
//                        block3.setName("b3");
//                        block1.add(Jump(block3));
//                        block2.add(Jump(block3));
//                        BasicBlockList blocks = BasicBlockList(
//                                block1, block2, block3
//                        );
//                    }
//                });
//			}//@mahsa End
//			else if(i instanceof Print){
//
//				Print p = (Print) i;
//				p.accept(new Element.DefaultVisitor() {
//				@Override
//				public void visit(Print print){
//					int op = Integer.parseInt(print.getE().toString()) ;
//					Print(ConstInt(op));
//					super.visit(print);
//
//				}
//
//
//			});
//			}
//
//			else if(i instanceof Store){
//
//				Store s = (Store) i;
//				s.accept(new Element.DefaultVisitor() {
//					@Override
//					public void visit(Store store){
//						Operand y=store.getAddress();
//						Operand  v =store.getValue();
//						TemporaryVar x = TemporaryVar(y.toString());
//
//						Alloca(x, TypeInt());
//
//						Store(VarRef(x), ConstInt(Integer.parseInt(v.toString())));
//
//						super.visit(store);
//
//					}
//
//
//				});
//			}
//			else if(i instanceof CommentInstr ){
//				CommentInstr com =(CommentInstr)i;
//				com.accept(new Element.DefaultVisitor() {
//					@Override
//					public void visit(CommentInstr commentinstr){
//						String com =commentinstr.getText();
//						CommentInstr(com);
//					}
//
//				});
//			}
//
//		}
//
//		}
//
//    @Override
//    public void visit(TypeArray typeArray)
//    {
//        Type of = typeArray.getOf();
//        int size = typeArray.getSize();
//        TypeArray(of.copy(), size);
//
//
//    }
//    @Override
//    public void visit(TypePointer typePointer)
//    {
//        Type to = typePointer.getTo();
//        TypePointer(to.copy());
//    }
//
//    @Override
//    public void visit(TypeProc typeProc)
//    {
//        Type resultType = typeProc.getResultType();
//        TypeRefList typeRefList = typeProc.getArgTypes();
//        TypeProc(typeRefList, resultType.copy());
//        super.visit(typeProc);
//
//    }
//
//    @Override
//    public void visit(TypeStruct typeStruct)
//    {
//        String name = typeStruct.getName();
//        StructFieldList structFieldList = typeStruct.getFields();
//        TypeStruct(name, structFieldList);
//        super.visit(typeStruct);
//    }
//
//    @Override
//    public void visit(StructField structField)
//    {
//        Type type = structField.getType(); //ref
//        String name = structField.getName();
//        StructField(type.copy(), name); //copy instead of ref?
//    }
//

//
//    //All the types to be added together just like addToAssign?
//}

