package translation;

import analysis.TypeContext;
//import com.sun.org.apache.xpath.internal.operations.Div;

import minijava.ast.*;
import minillvm.ast.*;
import org.omg.CORBA._IDLTypeStub;

import static minillvm.ast.Ast.*;
import static minillvm.ast.Ast.GetElementPtr;
import static minillvm.ast.Ast.TypePointer;

import java.security.PublicKey;
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


        this.BKL = entry;
        BKL.add(ReturnExpr(ConstInt(0)));
        prog.accept(this);
        //For loop for read an each stmt of main class -> main body
        for (MJStatement stmt : javaProg.getMainClass().getMainBody()) {
            stmt.match(new StmtMatcher());

		}

		return prog;



        //p = i.machter(stmt);
        //Overriting the machter case_stmt (){}

//
//
//                match(
//                MJStmtIf();
//        );


    }




	private class StmtMatcher implements MJStatement.MatcherVoid {
        @Override
        public void case_StmtIf(MJStmtIf stmtIf) {


        }

        @Override
        public void case_StmtWhile(MJStmtWhile stmtWhile) {

        }

        @Override
        public void case_StmtReturn(MJStmtReturn stmtReturn) {

        }

        @Override
        public void case_StmtPrint(MJStmtPrint stmtPrint) {
        	//MJExpr ex=stmtPrint.getPrinted();
			//Operand o=(Operand)ex;

			Print(ConstInt(42));





        }

        @Override
        public void case_Block(MJBlock block) {

        }

        @Override
        public void case_StmtAssign(MJStmtAssign stmtAssign) {

        }

        @Override
        public void case_StmtExpr(MJStmtExpr stmtExpr) {

        }

        @Override
        public void case_VarDecl(MJVarDecl varDecl) {

        }
    }
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
//    //Add to the Assign Block
//    void addToAssign(Instruction i){
//        BKL.add(i);
//    }
//
//    //All the types to be added together just like addToAssign?
//}

