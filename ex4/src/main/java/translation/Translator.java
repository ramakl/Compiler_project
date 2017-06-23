
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

import org.apache.tools.ant.types.Environment;
import org.omg.CORBA._IDLTypeStub;



import static minillvm.ast.Ast.*;

import static minillvm.ast.Ast.GetElementPtr;

import static minillvm.ast.Ast.TypePointer;


import java.lang.reflect.Array;
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
   // private Translator tr;
    //Translator (Translator translator){
      //  this.tr=translator;
    //}
    public static InstructionList BKL = InstructionList();
    BasicBlock entry = BasicBlock();
    BasicBlock end= BasicBlock();
    BasicBlockList blocks;
    BasicBlock currentBlock;
    boolean br=false;
    boolean arr=false;
    Operand ArraySize;
    public int o =0;
    private final MJProgram javaProg;
    // @mahsa: We need to have a block to add serveral submethods to one basic block of a parent method
    //public BasicBlock getOpenBlock() {
    //return BKL;
    //}
    Map< MJVarDecl, TemporaryVar > tempVars = new HashMap< MJVarDecl, TemporaryVar >();
    Map< VarRef,TemporaryVar > temprefense = new HashMap<  VarRef,TemporaryVar >();
    public Translator(MJProgram javaProg) {
        this.javaProg = javaProg;

    }

    public Prog translate() {
        // TODO add your translation code here
        // TODO here is an example of a minimal program (remove this)
        Prog prog = Prog(TypeStructList(), GlobalList(), ProcList());
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
        for (MJStatement stmt : javaProg.getMainClass().getMainBody()) {
            Object match = stmt.match(new StmtMatcher());
            if(match instanceof Instruction)
            {
                currentBlock.add((Instruction)match);
            }
        }
        currentBlock = end;
        if(!br){
            currentBlock = entry;
        }

        currentBlock.add(ReturnExpr(ConstInt(0)));
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
                   /* TypeStruct aarr =Ast.TypeStruct("array",
                            Ast.StructFieldList(
                                    Ast.StructField(Ast.TypeInt(),"length"),
                                            Ast.StructField(Ast.TypeArray(Ast.TypeInt(),0),"data")
                            ));
                    return  aarr;*/
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
            currentBlock.add(Alloca(x,llvmtype));
            //Hesitated about varDecl in tempVars
            tempVars.put(varDecl, x);
            //temprefense.put(VarRef(x),x);
            return VarRef(x);
        }
        @Override
        public Object case_Plus(MJPlus plus) {
            return Add();
        }
        //stm-while pair Working Rama Madhusudhan
        @Override
        public Object case_StmtWhile(MJStmtWhile stmtWhile) {

            MJExpr condition = stmtWhile.getCondition();
            Object cond=condition.match(new StmtMatcher());

            MJStatement loopBody = stmtWhile.getLoopBody();
            BasicBlock loop= BasicBlock() ;
            currentBlock=loop;
            BasicBlock loopp =(BasicBlock)loopBody.match(new StmtMatcher());
            for(Instruction i : loopp)
                currentBlock.add(i.copy());
            //looping through the body
            BasicBlock L2=BasicBlock(
                    Jump(end)
            );
            loop.setName("loop");

            BasicBlock condi =BasicBlock(
                    Branch((Operand)cond, loop, L2)
            );
            condi.setName("condi");
            loop.add( Jump(condi));
            L2.setName("L2");
            br=true;
            blocks.add(loop);
            currentBlock = L2;
            blocks.add(currentBlock);
            blocks.add(condi);
            entry.add(ReturnExpr(ConstInt(0)));
            return ConstInt(0);

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
        //ExprUnary  written by @rama
        @Override
        public Object case_ExprUnary(MJExprUnary exprUnary) {
            MJExpr ex= exprUnary.getExpr();
            MJUnaryOperator  o=exprUnary.getUnaryOperator();
            Object e=ex.match(new StmtMatcher());
            Object ad=o.match(new StmtMatcher());
            if(ad instanceof Sub){
                //Sub(VarRef(x));???? how we can use sub instead binary operation
                TemporaryVar result = TemporaryVar("ss");
                currentBlock.add((Instruction) BinaryOperation(result,(ConstInt(0)),(Operator)ad,(Operand)(e)));
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
            if (tempVars.containsKey(varDeclvar)) {
                TemporaryVar x = tempVars.get(varDeclvar);
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
            Operand rightOp = get_R(stmtAssign.getRight());
            Operand leftOp = get_L(stmtAssign.getLeft());
            if (arr) {

                TemporaryVar x = TemporaryVar("C");
                TemporaryVar d = TemporaryVar("D");
                Load lArray = Load(x, leftOp);
                currentBlock.add(lArray);
                currentBlock.add(Alloca(d, rightOp.calculateType()));

                currentBlock.add(Store(VarRef(d), ArraySize.copy()));
                arr=false;
                return ConstInt(0);
            }
            return (Store(leftOp,rightOp));
            //entry.add(Alloc((TemporaryVar) leftOp,rightOp));
            //return ConstInt(0);
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

        //Block written by @rama
        @Override
        public Object case_Block(MJBlock block) {
            o=o+1;
            BasicBlock bloc = BasicBlock();
            bloc.setName("bloc"+o+"");
            for(MJStatement statement : block)
            {
                Object statementt=statement.match(new StmtMatcher());
                if(statementt instanceof Instruction)
                {
                    //entry.add((Instruction)statementt);
                    bloc.add((Instruction)statementt);

                }
            }
            return bloc;
        }

        @Override
        public Object case_ClassDeclList(MJClassDeclList classDeclList) {
            return null;
        }

        //ExprBinary written by @rama
        @Override
        public Object case_ExprBinary(MJExprBinary exprBinary) {

            Operand right = get_R(exprBinary.getRight());
            Operand left = get_L(exprBinary.getLeft());

            MJOperator  op = exprBinary.getOperator();

            Object ad = op.match(new StmtMatcher());

            TemporaryVar result = TemporaryVar("result");
            TemporaryVar s = TemporaryVar("s");
            //Load lR = Load(s,right);
            if(ad instanceof Sdiv){
                TemporaryVar resultr = TemporaryVar("ss");
                TemporaryVar resul = TemporaryVar("s");
                currentBlock.add(BinaryOperation(resul,right,(Operator)Eq(),ConstInt(0)));
                BasicBlock L1 =BasicBlock(
                        BinaryOperation(resultr,left,(Operator)Sdiv(),right.copy()),
                        Jump(end)
                );
                L1.setName("L1");
                blocks.add(L1);

                currentBlock.add((Instruction) Branch(VarRef(resul),L1,end));

                return VarRef(resultr);
            }
            currentBlock.add(BinaryOperation(result, left,(Operator) ad, right));

            return VarRef(result);
        }
        //stm-return written by @rama
        @Override
        public Object case_StmtReturn(MJStmtReturn stmtReturn) {
            MJExpr e= stmtReturn.getResult();
            Object u=e.match(new StmtMatcher());
            if(u instanceof Operand )
            {
                currentBlock.add(ReturnExpr((Operand)u));
            }
            else {
                currentBlock.add(ReturnExpr(ConstInt(Integer.parseInt(u.toString()))));
            }
            return null;
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
            TemporaryVar x = TemporaryVar("x");
            currentBlock.add(Alloca(x,TypeNullpointer()));
            return ConstInt(0);
        }

        @Override
        public Object case_ClassDecl(MJClassDecl classDecl) {

          String className= classDecl.getName();
           MJVarDeclList filds=classDecl.getFields();
           MJMethodDeclList methods=classDecl.getMethods();
           MJExtended extended=classDecl.getExtended();
            TypeStruct ClassStruct;
            StructFieldList stfl=StructFieldList();
           for (MJVarDecl VAr :filds)
           {
               Object VarDecl =VAr.match(new StmtMatcher());
              StructField  sf=StructField((Type)VarDecl,VarDecl.toString());
              stfl.add(sf);
           }
            ClassStruct =Ast.TypeStruct(className,stfl);
           //i
            return ClassStruct;



        }

        //stm-print writen by @rama
        @Override
        public Object case_StmtPrint(MJStmtPrint stmtPrint) {
            MJExpr ex=  stmtPrint.getPrinted();

            Operand u = get_R(ex);
            TemporaryVar val = TemporaryVar("val");

            if(u instanceof Operand){
                return Print(u);
            }
            else if(u != null){
                //entry.add((Instruction)Print(ConstInt(Integer.parseInt(u.toString()))));
                return ((Instruction)Print(ConstInt(Integer.parseInt(u.toString()))));
            }

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
            return ConstBool(boolConst.getBoolValue()) ;
        }

        @Override
        public Object case_TypeClass(MJTypeClass typeClass) {
            return null;
        }

        //matcher
        @Override
        public Object case_NewIntArray(MJNewIntArray newIntArray) {
       /*     Parameter sizee =Ast.Parameter(Ast.TypeInt(),"size");
           TypePointer arrayPointerType;
            ProcedureRef newIntArrayFunc= Ast.Proc("newInAr",arrayPointerType,Ast.ParameterList(sizee),Ast.BasicBlockList());
            addProcedure(newIntArray);
            setCurrentproc(newIntArrayFunc);*/
            MJExpr size=  newIntArray.getArraySize();
            Object  sizeArray=size.match(new StmtMatcher());
            TemporaryVar NewSize = TemporaryVar("NewSize");
            TemporaryVar NewSize2 = TemporaryVar("NewSize2");
            //Load lR = Load(s,right);
            entry.add(BinaryOperation(NewSize,(Operand) sizeArray,(Operator) Mul(), ConstInt(4)));
            entry.add(BinaryOperation(NewSize2,(Operand)NewSize,(Operator) Add(), ConstInt(4)));

            arr=true;
            return VarRef(NewSize2);
        }

        @Override
        public Object case_TypeIntArray(MJTypeIntArray typeIntArray) {
            return TypeArray(TypeInt(), typeIntArray.size());
        }

        //stm-if written by @rama
        @Override
        public Object case_StmtIf(MJStmtIf stmtIf) {
            MJExpr co =stmtIf.getCondition();
            MJStatement t =stmtIf.getIfTrue();
            MJStatement f =stmtIf.getIfFalse();
            Object coo=co.match(new StmtMatcher());
            Object tt=t.match(new StmtMatcher());
            Object ff=f.match(new StmtMatcher());
            BasicBlock trueLabel = (BasicBlock) tt;
            BasicBlock falseLabel = (BasicBlock) ff;
            BasicBlock bloc3=BasicBlock(
                    Jump(end)
            );
            bloc3.setName("b3");
            br=true;

            trueLabel.add(Jump(bloc3));
            blocks.add(trueLabel);

            falseLabel.add(Jump(bloc3));
            blocks.add(falseLabel);
            blocks.add(bloc3);
            currentBlock = end;

            entry.add(Branch((Operand)coo, trueLabel, falseLabel));
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
            MJExpr exp= arrayLookup.getArrayExpr();
            MJExpr in=  arrayLookup.getArrayIndex();
            Object index=in.match(new StmtMatcher());
            Object expr=exp.match(new StmtMatcher());
            TemporaryVar x = TemporaryVar("x");
            Load(x,(Operand)expr);
            TemporaryVar comp1 = TemporaryVar("comp1");
            TemporaryVar comp2 = TemporaryVar("comp2");
            TemporaryVar comp3 = TemporaryVar("comp3");

            currentBlock.add(BinaryOperation(comp1, (Operand)index,(Operator)Slt(),VarRef(x)));
            currentBlock.add(BinaryOperation(comp2, ConstInt(-1),(Operator)Slt(),(Operand)((Operand) index).copy()));
            currentBlock.add(BinaryOperation(comp3,VarRef(comp1),(Operator)And(),VarRef(comp2)));
            BasicBlock L3 =BasicBlock(
                    Jump(end)
            );
            L3.setName("L3");

            TemporaryVar t2 = TemporaryVar("t2");
            //OperandList t22=OperandList();
            //t22.add(VarRef(t2).copy());
            TemporaryVar t3 = TemporaryVar("t3");
            TemporaryVar b= TemporaryVar("b");
            BasicBlock L1 =BasicBlock(

                    BinaryOperation(t2, (Operand)((Operand) index).copy(),(Operator)Add(),ConstInt(1)),
                    GetElementPtr(t3,(Operand)((Operand) expr).copy(),Ast.OperandList(Ast.ConstInt(0),
                            Ast.ConstInt(1),(VarRef(t2)).copy())),
                    Load(b,VarRef(t3)),
                    Jump(L3)
            );
            L1.setName("L1");
            BasicBlock L2 =BasicBlock(
                    HaltWithError( "error")

            );
            L2.setName("L2");
            currentBlock.add(Branch(VarRef(comp3),L1,L2));
            currentBlock=end;

            return null;
        }

        @Override
        public Object case_MethodDeclList(MJMethodDeclList methodDeclList) {
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
            //right

            @Override
            public Operand case_ArrayLookup(MJArrayLookup arrayLookup) {
                blocks.add(end);
                MJExpr exp= arrayLookup.getArrayExpr();
                MJExpr in=  arrayLookup.getArrayIndex();
                Object index=in.match(new StmtMatcher());
                Object expr=exp.match(new StmtMatcher());

                TemporaryVar x = TemporaryVar("x");

                currentBlock.add(Load(x,(Operand)expr));
                TemporaryVar comp1 = TemporaryVar("comp1");
                TemporaryVar comp2 = TemporaryVar("comp2");
                TemporaryVar comp3 = TemporaryVar("comp3");
                //currentBlock.add(BinaryOperation(comp1, (Operand)index,(Operator)Slt(),VarRef(x)));
                currentBlock.add(BinaryOperation(comp2, ConstInt(-1),(Operator)Slt(),(Operand)((Operand) index).copy()));
                //currentBlock.add(BinaryOperation(comp3,VarRef(comp1),(Operator)And(),VarRef(comp2)));
                BasicBlock L3 =BasicBlock(
                        Jump(end)
                );
                L3.setName("L3");
                blocks.add(L3);

                TemporaryVar t2 = TemporaryVar("t2");
                //OperandList t22=OperandList();
                //t22.add(VarRef(t2).copy());
                TemporaryVar t3 = TemporaryVar("t3");
                TemporaryVar b= TemporaryVar("b");
                BasicBlock L1 =BasicBlock(

                        BinaryOperation(t2, (Operand)((Operand) index).copy(),(Operator)Add(),ConstInt(1)),
                        GetElementPtr(t3,(Operand)((Operand) expr).copy(),Ast.OperandList(Ast.ConstInt(0),
                                Ast.ConstInt(1),(VarRef(t2)).copy())),
                        Load(b,VarRef(t3)),
                        Jump(L3)
                );
                L1.setName("L1");
                blocks.add(L1);
                BasicBlock L2 =BasicBlock(
                        HaltWithError( "error")

                );
                L2.setName("L2");
                blocks.add(L2);
                currentBlock.add(Branch(VarRef(comp2),L1,L2));
                currentBlock=end;
                br=true;


                return VarRef(t3);
            }
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

            //right
            @Override
            public Operand case_ExprBinary(MJExprBinary exprBinary) {
                Operand right = exprBinary.getRight().match(this);
                Operand left = exprBinary.getLeft().match(this);

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


                    return VarRef(resultr);
                }

                TemporaryVar result = TemporaryVar("result" );
                currentBlock.add(Ast.BinaryOperation(result, left, op, right));
                return VarRef(result);
            }
            @Override
            public Operand case_ExprThis(MJExprThis exprThis) {
                return null;
            }
            //right
            @Override
            public Operand case_NewIntArray(MJNewIntArray newIntArray) {

                MJExpr size=  newIntArray.getArraySize();
                Object  s=size.match(new StmtMatcher());
                TemporaryVar res = TemporaryVar("res");
                TemporaryVar res2 = TemporaryVar("res2");

                entry.add(BinaryOperation(res, (Operand)s,(Operator) Mul(), ConstInt(4)));

                ArraySize = (Operand) s;
                entry.add(BinaryOperation(res2, VarRef(res),(Operator) Add(), ConstInt(4)));
                arr=true;
                //Alloc(res2)
                return VarRef(res2);

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
            //right
            @Override
            public Operand case_ExprUnary(MJExprUnary exprUnary) {

                MJExpr ex= exprUnary.getExpr();
                MJUnaryOperator  o=exprUnary.getUnaryOperator();
                Object e=ex.match(new StmtMatcher());
                Object ad=o.match(new StmtMatcher());
                if(ad instanceof Sub){

                    TemporaryVar result = TemporaryVar("ss");
                    currentBlock.add((Instruction) BinaryOperation(result,(ConstInt(0)),(Operator)ad,(Operand)(e)));
                    return VarRef(result);
                }
                return null;

            }

        });
        return rightop;
    }
    //This Method Gets Left side of the exp, written by @Monireh
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

            //left
            @Override
            public Operand case_ArrayLookup(MJArrayLookup arrayLookup) {
                MJExpr exp= arrayLookup.getArrayExpr();
                MJExpr in=  arrayLookup.getArrayIndex();
                Object index=in.match(new StmtMatcher());
                Object expr=exp.match(new StmtMatcher());

                TemporaryVar x = TemporaryVar("x");

                Load(x,(Operand)expr);
                TemporaryVar comp1 = TemporaryVar("comp1");
                TemporaryVar comp2 = TemporaryVar("comp2");
                TemporaryVar comp3 = TemporaryVar("comp3");
                currentBlock.add(BinaryOperation(comp1, (Operand)index,(Operator)Slt(),VarRef(x)));
                currentBlock.add(BinaryOperation(comp2, ConstInt(-1),(Operator)Slt(),(Operand)((Operand) index).copy()));
                currentBlock.add(BinaryOperation(comp3,VarRef(comp1),(Operator)And(),VarRef(comp2)));
                BasicBlock L3 =BasicBlock(
                        Jump(end)
                );
                L3.setName("L3");

                TemporaryVar t2 = TemporaryVar("t2");
                OperandList t22=OperandList();
                t22.add(VarRef(t2).copy());
                TemporaryVar t3 = TemporaryVar("t2");
                TemporaryVar b= TemporaryVar("t2");
                BasicBlock L1 =BasicBlock(

                        BinaryOperation(t2, (Operand)((Operand) index).copy(),(Operator)Add(),ConstInt(1)),
                        GetElementPtr(t3,(Operand)((Operand) expr).copy(),t22),
                        Load(b,VarRef(t3)),
                        Jump(L3)
                );
                L1.setName("L1");
                BasicBlock L2 =BasicBlock(
                        HaltWithError( "error")

                );
                L2.setName("L2");
                currentBlock.add(Branch(VarRef(comp3),L1,L2));

                return null;

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
                HaltWithError("must be no expr in left side ");
                return null;
            }

            @Override
            public Operand case_ExprThis(MJExprThis exprThis) {
                return null;
            }

            //left
            @Override
            public Operand case_NewIntArray(MJNewIntArray newIntArray) {
                MJExpr size=  newIntArray.getArraySize();
                Object  s=size.match(new StmtMatcher());
                TemporaryVar res = TemporaryVar("s");
                TemporaryVar res2 = TemporaryVar("s");
                //Load lR = Load(s,right);
                BinaryOperation(res, (Operand)size,(Operator) Mul(), ConstInt(4));
                BinaryOperation(res2, (Operand)res,(Operator) Add(), ConstInt(4));
                //Alloc(res2)
                return VarRef(res2);
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
                    //temprefense.put( VarRef(x),x);
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
            //unary left written by rama
            @Override
            public Operand case_ExprUnary(MJExprUnary exprUnary) {

                MJExpr ex= exprUnary.getExpr();
                MJUnaryOperator  o=exprUnary.getUnaryOperator();
                Object e=ex.match(new StmtMatcher());
                Object ad=o.match(new StmtMatcher());
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

