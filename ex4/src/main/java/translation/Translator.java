
package translation;

import minijava.ast.*;
import minillvm.ast.*;
import minillvm.ast.BasicBlock;
import minillvm.ast.BasicBlockList;
import minillvm.ast.InstructionList;
import minillvm.ast.Load;
import minillvm.ast.OperandList;
import minillvm.ast.Parameter;
import minillvm.ast.ParameterList;
import minillvm.ast.Proc;
import minillvm.ast.Prog;
import minillvm.ast.Sdiv;
import minillvm.ast.StructField;
import minillvm.ast.StructFieldList;
import minillvm.ast.Sub;
import minillvm.ast.TemporaryVar;
import minillvm.ast.TypeBool;
import minillvm.ast.TypePointer;
import minillvm.ast.TypeStruct;
import minillvm.ast.TypeStructList;

import java.util.HashMap;
import java.util.Map;

import static minillvm.ast.Ast.*;

//import com.sun.org.apache.xpath.internal.operations.Div;
//import com.sun.javafx.fxml.expression.Expression;

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
    private BasicBlock entry = BasicBlock();
    private BasicBlock end= BasicBlock();
    private BasicBlockList blocks;
    private BasicBlock currentBlock;
    private boolean br=false;
    private boolean arr=false;
    private Operand ArraySize;
    public int o =0;
    private boolean arrlok=false;
    private Prog prog;
    private TypePointer  arrpoint ;
    private TypeStruct ClassStruct;
    private TemporaryVar array;
    private Proc currentproc=null;
    private TypeStructList typeStructList = TypeStructList();
    private TypeStruct arraystruct;
    private final MJProgram javaProg;
    // @mahsa: We need to have a block to add serveral submethods to one basic block of a parent method
    //public BasicBlock getOpenBlock() {
    //return BKL;
    //}
    private TypePointer getpointer(){
        arraystruct = TypeStruct("intArray",
                StructFieldList(StructField(TypeInt(), "size"), 
                        StructField(TypeArray(TypeInt(), 0), 
                                "values ")));

        typeStructList.add(arraystruct);
        
        return TypePointer(arraystruct);
    }

    private class OperatorMatcher implements MJOperator.Matcher<Operator>{

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
    }


    private class UnaryOperatorMatcher implements MJUnaryOperator.Matcher<Operator>{

        @Override
        public Operator case_UnaryMinus(MJUnaryMinus unaryMinus) {
            return Sub();
        }

        @Override
        public Operator case_Negate(MJNegate negate) {
            return Sub();
        }
    }
    Map< MJVarDecl, TemporaryVar > tempVars = new HashMap< MJVarDecl, TemporaryVar >();
    //Map<MJNewObject, MJClassDecl> clsdec = new HashMap<MJNewObject,MJClassDecl>();
    Map<MJClassDecl ,TypePointer> objCls = new HashMap<MJClassDecl ,TypePointer>();
    Map< VarRef,TemporaryVar > temprefense = new HashMap<  VarRef,TemporaryVar >();
    public Translator(MJProgram javaProg) {
        this.javaProg = javaProg;

    }
    public Prog translate() {
        // TODO add your translation code here
        // TODO here is an example of a minimal program (remove this)
        prog = Prog(typeStructList, GlobalList(), ProcList());
        blocks = BasicBlockList();
        //BasicBlockList blocks = BKL;
        Proc mainProc = Proc("main", TypeInt(), ParameterList(), blocks);
        prog.getProcedures().add(mainProc);
        entry.setName("entry");
        end.setName("end");
        //blocks.add(entry);
        currentBlock = entry;
        blocks.add(currentBlock);
        //blocks.add(end);

        arrpoint = getpointer();

        for (MJStatement stmt : javaProg.getMainClass().getMainBody()) {
            Object match = stmt.match(new StmtMatcher());
            if(match instanceof Instruction)
            {
                currentBlock.add((Instruction)match);
            }
        }
        MJClassDeclList classlist =  javaProg.getClassDecls();


        currentBlock.add(ReturnExpr(ConstInt(0)));
        if(!br){
            currentBlock = entry;
        }

        boolean blockFound = false;
        for(BasicBlock b : blocks)
        {
            if(b == currentBlock){
                blockFound = true;
                break;
            }

        }
        if(!blockFound)
            blocks.add(currentBlock);

        prog.accept(this);
        //For-loop to read each stmt of main class -> main bod
        //for (MJStatement stmt : javaProg.getMainClass().getMainBody()) {
        //   stmt.match(new StmtMatcher());
        // TypeStruct vMethodTable = TypeStruct("vatable",StructFieldList(StructField(TypePointer(),"metod")));
        //}
        return prog;
    }
    //: we should do this for the first part (Block, StmtIf, StmtWhile, StmtReturn, StmtPrint, StmtExpr,
    //StmtAssign, ExprBinary, ExprUnary, BoolConst, VarUse, Number.)

    private class StmtMatcher implements MJStatement.Matcher {


        //Method Decl written by @rama

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

                    return arrpoint;
                }

                @Override
                public Type case_TypeClass(MJTypeClass typeClass) {
                    String name =typeClass.getName();
                    TypePointer typePointer;
                    MJClassDecl ClasDecl = typeClass.getClassDeclaration();
                    if (objCls.containsKey(ClasDecl)) {

                        typePointer = objCls.get(ClasDecl);


                    } else {


                        MJVarDeclList filds=ClasDecl.getFields();
                        StructFieldList st=StructFieldList();
                        for(MJVarDecl fild :filds)
                        {
                            Object f= fild.match(new StmtMatcher());

                            TemporaryVar s=TemporaryVar("s");
                            if(f instanceof Operand) {
                                currentBlock.add(Load(s, (Operand) f));
                                StructField sff = StructField(s.calculateType(), fild.toString());
                                st.add(sff);
                            }
                        }
                        ClassStruct = TypeStruct(name,st);
                        objCls.put(ClasDecl,TypePointer(ClassStruct));
                        typePointer = objCls.get(ClasDecl);


                    }

                    return typePointer.getTo();
                }

                @Override
                public Type case_TypeInt(MJTypeInt typeInt) {
                    return TypeInt();
                }
            });
            TemporaryVar x = TemporaryVar(varDecl.getName());
            currentBlock.add(Alloca(x,llvmtype));

            tempVars.put(varDecl, x);

            return VarRef(x);
        }


        //stm-while pair Working Rama Madhusudhan
        @Override
        public Object case_StmtWhile(MJStmtWhile stmtWhile) {


            BasicBlock loopEnd = BasicBlock();
            loopEnd.setName("loopEnd");
            BasicBlock loopCondition= BasicBlock() ;
            loopCondition.setName("loopCondition");
            BasicBlock loopBody = BasicBlock();
            loopBody.setName("loopBody");

            BasicBlock previousBlock = currentBlock;
            previousBlock.add(Jump(loopCondition));
            currentBlock=loopCondition;
            Operand condition = get_R(stmtWhile.getCondition());




            currentBlock = loopBody;
            stmtWhile.getLoopBody().match(new StmtMatcher());



            loopCondition.add(Branch(condition.copy(),loopBody,loopEnd));



            currentBlock.add(Jump(loopCondition));
            br=true;
            blocks.add(loopCondition);
            blocks.add(loopBody);
            blocks.add(loopEnd);



            currentBlock = loopEnd;
            return null;

        }


        //ExprUnary  written by @rama inside StmtMatcher









        //stm-assign
        @Override
        public Object case_StmtAssign(MJStmtAssign stmtAssign) {
            Operand rightOp = get_R(stmtAssign.getRight());
            Operand leftOp = get_L(stmtAssign.getLeft());


            currentBlock.add(Store(leftOp,rightOp));
            return null;

        }



        //Block written by @rama
        @Override
        public Object case_Block(MJBlock block) {


            for(MJStatement statement : block)
            {
                statement.match(new StmtMatcher());

            }
            return null;
        }



        //ExprBinary written by @rama

        //stm-return written by @rama in stmtMacher
        @Override
        public Object case_StmtReturn(MJStmtReturn stmtReturn) {
            MJExpr e= stmtReturn.getResult();

            Operand u = get_R(e);
            currentBlock.add(ReturnExpr(u));
            return null;
        }

        @Override
        public Object case_StmtExpr(MJStmtExpr stmtExpr) {
            MJExpr ex=stmtExpr.getExpr();
            return get_R(ex);
        }







        //stm-print writen by @rama
        @Override
        public Object case_StmtPrint(MJStmtPrint stmtPrint) {
            MJExpr ex=  stmtPrint.getPrinted();

            Operand u = get_R(ex);
            currentBlock.add(Print(u));

            return  u;
        }





        //BoolConst


        //matcher




        //stm-if written by @rama
        @Override
        public Object case_StmtIf(MJStmtIf stmtIf) {
            MJExpr co =stmtIf.getCondition();
            MJStatement t =stmtIf.getIfTrue();
            MJStatement f =stmtIf.getIfFalse();
            //Object coo=co.match(new StmtMatcher());
            Operand coo = get_R(co);
            BasicBlock futureblock=BasicBlock(

            );
            BasicBlock previousBlock = currentBlock;
            BasicBlock trueLabel = BasicBlock() ;
            currentBlock=trueLabel;
            t.match(new StmtMatcher());
            currentBlock.add(Jump(futureblock));
            BasicBlock falseLabel= BasicBlock() ;
            //futureblock=falseLabel;
            currentBlock= falseLabel;

            f.match(new StmtMatcher());
            currentBlock.add(Jump(futureblock));
            previousBlock.add(Branch(coo, trueLabel, falseLabel));



            futureblock.setName("futureblock");
            br=true;


            blocks.add(trueLabel);


            blocks.add(falseLabel);
            currentBlock= futureblock;
            blocks.add(futureblock);
            return null;

        }





    }
    //This Method Gets Right side of the exp, written by @Monireh
    public Operand get_R(MJExpr exp) {
        Operand rightop = exp.match(new MJExpr.Matcher<Operand>() {
            @Override
            public Operand case_FieldAccess(MJFieldAccess fieldAccess) {
                return null;
            }

            @Override
            public Operand case_MethodCall(MJMethodCall methodCall) {
                /*MJExprList ArgumentsList=   methodCall.getArguments();
                String MetodName =methodCall.getMethodName();
                MJMethodDecl methoddecl = methodCall.getMethodDeclaration();
                Object ob=methoddecl.match(new StmtMatcher());
                MJExpr Receiver= methodCall.getReceiver();
                OperandList OpList =OperandList();
                for(MJExpr Arg:ArgumentsList)
                {
                    Object Argument =Arg.match(new StmtMatcher());
                    if(Argument instanceof Operand) {
                        Operand Op = (Operand) Argument;
                        OpList.add(Op);
                    }
                }
                TemporaryVar temp = TemporaryVar("temp" );
                currentBlock.add(Call(temp,(Operand) ob,OpList));*/
                return null;
            }

            @Override
            public Operand case_NewObject(MJNewObject newObject) {
                String name = newObject.getClassName();
                MJClassDecl ClasDecl= newObject.getClassDeclaration();

                TemporaryVar f = TemporaryVar("pointer");
                TypePointer  typePointer;

                typePointer = objCls.get(ClasDecl);
                TemporaryVar xg=TemporaryVar("x");
                TemporaryVar y=TemporaryVar("y");
                currentBlock.add(Alloc(xg,(Operand)ConstInt( ClasDecl.size())));
                currentBlock.add(Bitcast(y, (Type)typePointer.getTo().copy(),VarRef(xg).copy()));

                return VarRef(y);
            }



            @Override
            public Operand case_ArrayLength(MJArrayLength arrayLength) {
                TemporaryVar t3 = TemporaryVar("t3");

                Operand arrayLen =  get_R(arrayLength.getArrayExpr());
                currentBlock.add(GetElementPtr(t3, arrayLen, OperandList(ConstInt(0),
                        ConstInt(0))));
                TemporaryVar tempArrayLength = TemporaryVar("tempArrayLength");
                currentBlock.add(Load(tempArrayLength,VarRef(t3)));
                return VarRef(tempArrayLength);

            }

            @Override
            public Operand case_ArrayLookup(MJArrayLookup arrayLookup) {

                Operand l = get_L(arrayLookup);
                TemporaryVar address = TemporaryVar("address");
                currentBlock.add(Load(address, l));
                return VarRef(address);
            }
            @Override
            public Operand case_BoolConst(MJBoolConst boolConst) {
                return ConstBool(boolConst.getBoolValue());
                //return ConstBool(true);
            }

            @Override
            public Operand case_ExprNull(MJExprNull exprNull) {
                TemporaryVar x = TemporaryVar("x");
                currentBlock.add(Alloca(x,TypeNullpointer()));
                return ConstInt(0);
            }

            //right
            @Override
            public Operand case_ExprBinary(MJExprBinary exprBinary) {
                Operand right = exprBinary.getRight().match(this);
                Operand left = exprBinary.getLeft().match(this);

                Operator op = exprBinary.getOperator().match(new OperatorMatcher());

                //Load lR = Load(s,right);
                if(op instanceof Sdiv){
                    TemporaryVar divRes = TemporaryVar("divRes");
                    TemporaryVar result = TemporaryVar("result");
                    currentBlock.add(BinaryOperation(result,right,Eq(),ConstInt(0)));
                    BasicBlock L1 = BasicBlock(
                            BinaryOperation(divRes, left, Sdiv(), right.copy())

                    );
                    L1.setName("L1");
                    L1.add(Jump(end));
                    blocks.add(L1);

                    BasicBlock L2 = BasicBlock(
                            HaltWithError("Divid by Zero")
                    );
                    L2.setName("L2");
                    blocks.add(L2);
                    br = true;

                    currentBlock.add( Branch(VarRef(result), L2, L1));
                    currentBlock = end;
                    //Doesn't make scense
                    //currentBlock.add(BinaryOperation(divRes, left, Sdiv(), right.copy()));
                    return VarRef(divRes);
                }

                TemporaryVar result = TemporaryVar("result" );
                currentBlock.add(BinaryOperation(result, left, op, right));
                return VarRef(result);
            }
            @Override
            public Operand case_ExprThis(MJExprThis exprThis) {
                return null;
            }
            //right
            @Override
            public Operand case_NewIntArray(MJNewIntArray newIntArray) {

                BasicBlock previousBlock = currentBlock;
                MJExpr size=  newIntArray.getArraySize();

                Operand sizeArray = get_R(size);


                BasicBlock initialBlock= BasicBlock();

                BasicBlock validSize= BasicBlock();
                BasicBlock loopBody= BasicBlock();

                BasicBlock loopEnd= BasicBlock();
                blocks.add(validSize);
                blocks.add(loopBody);
                blocks.add(loopEnd);



                currentBlock=initialBlock;
                previousBlock.add(Jump(currentBlock));
                blocks.add(currentBlock);

                TemporaryVar less=TemporaryVar("caseless");
                currentBlock.add(BinaryOperation(less,sizeArray,Slt(),ConstInt(0)));
                BasicBlock negativeSize= BasicBlock(HaltWithError("negative"));
                blocks.add(negativeSize);
                currentBlock.add(Branch(VarRef(less),negativeSize,validSize));

                currentBlock=validSize;

                TemporaryVar arrSizeWithLen = TemporaryVar("NewSize");
                TemporaryVar arraySizeInBytes = TemporaryVar("NewSize2");
                //Load lR = Load(s,right);
                currentBlock.add(BinaryOperation(arrSizeWithLen, sizeArray.copy(), Add(), ConstInt(1)));
                currentBlock.add(BinaryOperation(arraySizeInBytes,VarRef(arrSizeWithLen), Mul(), ConstInt(4)));
                TemporaryVar arraySizeRef=TemporaryVar("t");
                currentBlock.add(Alloc(arraySizeRef,VarRef(arraySizeInBytes)));
                array=TemporaryVar("arraySizeRef");
                
                currentBlock.add(Bitcast(array,arrpoint,VarRef(arraySizeRef)));


                //store size
                Parameter Length= Parameter(TypeInt(),"length");
                /*TemporaryVar d = TemporaryVar("D");
                //TypePointer t=TypePointer(sizeArray);
                Load lArray = Load(d, sizeArray.copy());
                currentBlock.add(lArray);*/


                TemporaryVar lengthAdrres =TemporaryVar("lengthAdrres");
                currentBlock.add(GetElementPtr(lengthAdrres,VarRef(array),OperandList(ConstInt(0),ConstInt(0))));
                //ppppppppppppppppppppppproooooblllemm here
                currentBlock.add(Store(VarRef(lengthAdrres),sizeArray.copy()));


                //initialize the array to 0
                BasicBlock loopStart= BasicBlock();
                currentBlock.add(Jump(loopStart));
                currentBlock=loopStart;
                blocks.add(loopStart);
                TemporaryVar i=TemporaryVar("i");
                TemporaryVar nextI=TemporaryVar("temp");
                //b1 ggod b2 loopbody
                loopStart.add(PhiNode(i,
                        TypeInt(),
                        PhiNodeChoiceList(PhiNodeChoice(validSize,ConstInt(0)),
                                PhiNodeChoice(loopBody,VarRef(nextI)))));
                TemporaryVar  smal =TemporaryVar("smal");
                currentBlock.add(BinaryOperation(smal,VarRef(i),Slt(),sizeArray.copy()));
                //b2 body b3 loopend

                currentBlock.add(Branch(VarRef(smal),loopBody,loopEnd));
                currentBlock=loopBody;
                TemporaryVar adressofcounter= TemporaryVar("address");

                currentBlock.add(GetElementPtr(adressofcounter,
                        VarRef(array),
                        OperandList(ConstInt(0),
                                ConstInt(1),
                                VarRef(i))));

                currentBlock.add(Store(
                        VarRef(adressofcounter),
                        ConstInt(0)));
                //i+1;
                currentBlock.add(BinaryOperation(nextI,VarRef(i),Add(),ConstInt(1)));
                currentBlock.add(Jump(loopStart));


                currentBlock = loopEnd;






                //loopEnd.add(ReturnExpr(ConstInt(0)));
                BasicBlock procbody= BasicBlock(ReturnExpr(VarRef(array)));


                // Proc pr =Proc("arraylength",getpointer(),ParameterList(Length),BasicBlockList(procbody));
                //prog.getProcedures().add(pr);
                //currentproc=pr;










                return VarRef(array);

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
                //MJVarUse varDecl = (MJVarUse) exp;
                MJVarDecl varDeclvar = varUse.getVariableDeclaration();
                TemporaryVar y = TemporaryVar("y");

                if (tempVars.containsKey(varDeclvar)) {
                    TemporaryVar x = tempVars.get(varDeclvar);

                    Load l = Load(y,VarRef(x));
                    currentBlock.add(l);
                    return VarRef(l.getVar());
                } else {
                    // This should never happen
                    throw new RuntimeException(
                            "Variable not found during translation");
                }

            }
            //right in get_R
            @Override
            public Operand case_ExprUnary(MJExprUnary exprUnary) {

                MJExpr ex= exprUnary.getExpr();
                MJUnaryOperator  o=exprUnary.getUnaryOperator();
                Operand e = get_R(ex);
                Operator llvmminus= o.match(new UnaryOperatorMatcher());

                if(e.calculateType() instanceof TypeBool){
                    TemporaryVar NegBool = TemporaryVar("NegBool");
                    currentBlock.add(BinaryOperation(NegBool, ConstBool(true), llvmminus, e));
                    return VarRef(NegBool);

                } else {
                    TemporaryVar Negresult = TemporaryVar("Negresult");
                    currentBlock.add(BinaryOperation(Negresult, (ConstInt(0)), llvmminus, e));
                    return VarRef(Negresult);
                }


              /*  if(ad instanceof MJNegate){

                    TemporaryVar result = TemporaryVar("ss");

                    currentBlock.add((Instruction) BinaryOperation(result,(ConstInt(0)),(Operator)ad,(Operand)(e)));
                    return VarRef(result);
                }*/
                //return ConstInt(0);
            }

        });
        return rightop;
    }
    //This Method Gets Left side of the exp, written by @Monireh
    public Operand get_L(MJExpr exp) {

        //Left side of an Assign could be one of the following cases, So we should define them first
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
                Operand arrayLen =  get_R(arrayLength.getArrayExpr());
                return arrayLen ;
            }

            //left
            @Override
            public Operand case_ArrayLookup(MJArrayLookup arrayLookup) {
                arrlok=true;
                MJExpr exp= arrayLookup.getArrayExpr();
                MJExpr in=  arrayLookup.getArrayIndex();
                Operand index=get_R(in);
                Operand expr=get_R(exp);

                TemporaryVar x = TemporaryVar("x");

                Load(x,expr);
                //three blocks for handling the cases
                // a == null
                // i<0
                // i>= a.length
                TemporaryVar t2 = TemporaryVar("t2");
                TemporaryVar smallerThanZero = TemporaryVar("smallerThanZero");
                TemporaryVar greaterThanLength = TemporaryVar("greaterThanLength");
                TemporaryVar outOfBounds = TemporaryVar("outOfBounds");

                currentBlock.add(
                        BinaryOperation(
                                smallerThanZero,index,Slt(),ConstInt(0)
                        )
                );

                TemporaryVar length = TemporaryVar("length");

                currentBlock.add(GetElementPtr(length,
                        expr.copy(),
                        OperandList(ConstInt(0),
                                ConstInt(0))));



                TemporaryVar tempArrayLength = TemporaryVar("tempArrayLength");
                currentBlock.add(Load(tempArrayLength,VarRef(length)));

                TemporaryVar newArrayLength = TemporaryVar("newArrayLength");


                currentBlock.add(
                        BinaryOperation(
                                newArrayLength,VarRef(tempArrayLength),Sub(),ConstInt(1)
                        )
                );


                currentBlock.add(
                        BinaryOperation(
                                 greaterThanLength,VarRef(newArrayLength),Slt(),index.copy()
                        )
                );


                currentBlock.add(
                        BinaryOperation(
                                outOfBounds,VarRef(smallerThanZero),Or(),VarRef(greaterThanLength)
                        )
                );

                BasicBlock outOfBoundsError = BasicBlock();
                outOfBoundsError.add(HaltWithError("Array Index out of Bounds"));
                blocks.add(outOfBoundsError);

                BasicBlock indexInRange = BasicBlock();

                blocks.add(indexInRange);

                currentBlock.add(
                        Branch(
                                VarRef(outOfBounds),outOfBoundsError,indexInRange)
                        );


                currentBlock = indexInRange;

                currentBlock.add(GetElementPtr(t2,
                        expr.copy(),
                        OperandList(ConstInt(0),
                                ConstInt(1),
                                index.copy())));
                //navigate to the specific position in the Element Structure. Operand list = 0*1*index
                /*TemporaryVar comp1 = TemporaryVar("comp1");
                TemporaryVar comp2 = TemporaryVar("comp2");
                TemporaryVar comp3 = TemporaryVar("comp3");
                currentBlock.add(BinaryOperation(comp1, index,Slt(),VarRef(x)));
                currentBlock.add(BinaryOperation(comp2, ConstInt(-1),Slt(), index.copy()));
                currentBlock.add(BinaryOperation(comp3,VarRef(comp1),And(),VarRef(comp2)));
                BasicBlock L3 =BasicBlock(
                        Jump(end)
                );
                L3.setName("L3");


                OperandList t22=OperandList();
                t22.add(VarRef(t2).copy());

                TemporaryVar b= TemporaryVar("t2");
                BasicBlock L1 =BasicBlock(

                        BinaryOperation(t2, index.copy(),Add(),ConstInt(1)),
                        GetElementPtr(t3,expr.copy(),t22),
                        Load(b,VarRef(t3)),
                        Jump(L3)
                );
                L1.setName("L1");
                BasicBlock L2 =BasicBlock(
                        HaltWithError( "error")

                );
                L2.setName("L2");
                arrlok=true;
                currentBlock.add(Branch(VarRef(comp3),L1,L2));*/

                return VarRef(t2);

            }


            //left
            @Override
            public Operand case_BoolConst(MJBoolConst boolConst) {
                return ConstBool(boolConst.getBoolValue()) ;
            }

            @Override
            public Operand case_ExprNull(MJExprNull exprNull) {
                TemporaryVar x = TemporaryVar("x");
                currentBlock.add(Alloca(x,TypeNullpointer()));
                return ConstInt(0);
            }

            //left
            @Override
            public Operand case_ExprBinary(MJExprBinary exprBinary) {
                //Operand right = get_R(exprBinary.getRight());
                Operand right = exprBinary.getRight().match(this);
                //Operand left = get_L(exprBinary.getLeft());
                Operand left = exprBinary.getLeft().match(this);
                Operator op = exprBinary.getOperator().match(new OperatorMatcher());

                //Load lR = Load(s,right);
                if(op instanceof Sdiv){

                    TemporaryVar resultr = TemporaryVar("ss");
                    TemporaryVar resul = TemporaryVar("s");
                    currentBlock.add(BinaryOperation(resul,right,(Operator)Eq(),ConstInt(0)));
                    BasicBlock L1 =BasicBlock(
                            BinaryOperation(resultr,left,(Operator)Sdiv(),right.copy())

                    );
                    L1.setName("L1");

                    L1.add( Jump(end));
                    blocks.add(L1);
                    BasicBlock L2 =BasicBlock(

                            HaltWithError("devby Zero")
                    );
                    L1.setName("L2");
                    blocks.add(L2);
                    br=true;

                    currentBlock.add((Instruction) Branch(VarRef(resul),L1,L2));
                    currentBlock = end;
                    //currentBlock=end;

                    return VarRef(resultr);
                }

                TemporaryVar result = TemporaryVar("result" );
                currentBlock.add(BinaryOperation(result, left, op, right));
                return VarRef(result);

            }

            @Override
            public Operand case_ExprThis(MJExprThis exprThis) {
                return null;
            }

            //left
            @Override
            public Operand case_NewIntArray(MJNewIntArray newIntArray) {
                throw new RuntimeException("Is not used!");
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

                    return VarRef(x);
                } else {
                    // This should never happen
                    throw new RuntimeException(
                            "Variable not found during translation");
                }
            }
            //unary left written by rama in get_L
            @Override
            public Operand case_ExprUnary(MJExprUnary exprUnary) {

                MJExpr ex= exprUnary.getExpr();
                MJUnaryOperator  o=exprUnary.getUnaryOperator();
                Operand e=get_R(ex);
                Operator ad=o.match(new UnaryOperatorMatcher());
                if(ad instanceof Sub){
                    //Sub(VarRef(x));???? how we can use sub instead binary operation
                    TemporaryVar result = TemporaryVar("ss");
                    currentBlock.add((Instruction) BinaryOperation(result,(ConstInt(0)),(Operator)ad,(Operand)(e)));
                    return VarRef(result);
                }
                return null;
            }

        });
        return leftop;
    }//To do the rest of the cases


}

