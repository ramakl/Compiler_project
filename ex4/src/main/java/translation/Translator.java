package translation;import frontend.SourcePosition;import minijava.ast.*;import minillvm.ast.*;import minillvm.ast.BasicBlock;import minillvm.ast.BasicBlockList;import minillvm.ast.Load;import minillvm.ast.Proc;import minillvm.ast.Prog;import minillvm.ast.Sdiv;import minillvm.ast.StructField;import minillvm.ast.StructFieldList;import minillvm.ast.Sub;import minillvm.ast.TemporaryVar;import minillvm.ast.TypeBool;import minillvm.ast.TypePointer;import minillvm.ast.TypeStruct;import minillvm.ast.TypeStructList;import java.util.HashMap;import java.util.Map;import static minillvm.ast.Ast.*;public class Translator extends Element.DefaultVisitor{    private BasicBlock entry = BasicBlock();    private BasicBlockList blocks;    private BasicBlock currentBlock;    protected Prog prog;    Proc currentproc=null;    private TypePointer  arrpoint ;    private TypeStruct ClassStruct;    private TemporaryVar array;    Map<MJMethodDecl ,Operand> methls = new HashMap<MJMethodDecl ,Operand>();    private TypeStructList typeStructList = TypeStructList();    private final MJProgram javaProg;    // @mahsa: We need to have a block to add serveral submethods to one basic block of a parent method    //public BasicBlock getOpenBlock() {    //return BKL;    //}    private TypePointer getPointer(){        TypeStruct arraystruct  = TypeStruct("intArray",                StructFieldList(StructField(TypeInt(), "size"),                        StructField(TypeArray(TypeInt(), 0),                                "values ")));        typeStructList.add(arraystruct);        return TypePointer(arraystruct);    }    //writtten by rama   private Object methoddeclCase(  MJMethodDecl methodDecl) {        String name =methodDecl.getName();        MJVarDeclList param= methodDecl.getFormalParameters();        ParameterList paramlist= ParameterList() ;        for (MJVarDecl VAr :param)        {            Object VarDecl =VAr.match(new StmtMatcher());            TemporaryVar s=TemporaryVar("s");            Load(s,(Operand)VarDecl);              /*  TemporaryVar xg=TemporaryVar("x");                TemporaryVar y=TemporaryVar("y");                Alloc(xg,(Operand) ((Operand) VarDecl).copy()).size();                Bitcast(y, (Type)s.calculateType().copy(),VarRef(xg).copy());                //VarRef(y);*/            //Parameter  pr=Parameter((Type)y.calculateType(),VarDecl.toString());            Parameter  pr=Parameter((Type)s.calculateType().copy(),VarDecl.toString());            paramlist.add(pr);        }        //Object parameters=parameter.match(new StmtMatcher());        //BasicBlock prev =currentBlock;        //prev.add(ReturnExpr(ConstInt(0)));        MJBlock methodBody=  methodDecl.getMethodBody();        MJType  returnType=methodDecl.getReturnType();        Object retType=returnType.match(new TypeMatcher());        BasicBlockList blokl =BasicBlockList();        BasicBlock BBlok =BasicBlock();        //blocks.add(BBlok);       BasicBlock prev =BasicBlock();       prev=currentBlock;       prev.add(ReturnExpr(ConstInt(0)));        currentBlock=BBlok;        for(MJStatement st :methodBody)        {            Object stmt=st.match(new StmtMatcher());            if(stmt instanceof Instruction){                currentBlock.add((Instruction)stmt);            }        }        blokl.add(currentBlock);        BasicBlock next =BasicBlock();        currentBlock=next;        //Object methBoy=methodBody.match(new StmtMatcher());        Proc p=Proc(name, (Type)retType,paramlist,blokl);        //   currentBlock=end;        prog.getProcedures().add(p);        currentproc=p;            /*for (MJStatement stmt : (MJBlock)methBoy) {                Object matchh = stmt.match(new StmtMatcher());                if(matchh instanceof Instruction)                {                    currentBlock.add((Instruction)matchh);                }*/        methls.put(methodDecl,ProcedureRef(p));        return ProcedureRef(p);    }    private Object methoddecllist(MJMethodDeclList methodDeclList )    {        Object methoDec =null;        for(MJMethodDecl ml :methodDeclList)        {            methoddeclCase(ml);        }    return null;    }    private Object  classdecl(MJClassDecl classDecl )    {String className= classDecl.getName();        MJVarDeclList filds=classDecl.getFields();        MJMethodDeclList methods=classDecl.getMethods();        MJExtended extended=classDecl.getExtended();        StructFieldList stfl=StructFieldList();        //stfl.add(StructField(ProcedureRef(currentproc.copy()).calculateType(),"method"));        for (MJVarDecl VAr :filds)        {            Object VarDecl =VAr.match(new StmtMatcher());            TemporaryVar s=TemporaryVar("s");            Load(s,(Operand)VarDecl);            StructField  sf=StructField(s.calculateType(),VarDecl.toString());            stfl.add(sf);        }        ClassStruct = TypeStruct(className,stfl);        prog.getStructTypes().add(ClassStruct);        // Map<TypePointer,String> objCls = new HashMap<TypePointer,String>();        //objCls.put(className,TypePointer(ClassStruct));        TemporaryVar tv=TemporaryVar("classADreddd");        //TypePointer(ClassStruct);        //return ClassStruct;        objCls.put(classDecl,TypePointer(ClassStruct));        for (MJMethodDecl method :methods)        {           methoddeclCase(method);        }        return TypePointer(ClassStruct);    }    private void  classdecllist(MJClassDeclList list )    {        Object classdecl =null;        for(MJClassDecl cl :list)        {            classdecl(cl);        }        return ;    }    private class OperatorMatcher implements MJOperator.Matcher<Operator>{        @Override        public Operator case_Div(MJDiv div) {            return Sdiv();        }        @Override        public Operator case_And(MJAnd and) {            return And();        }        @Override        public Operator case_Equals(MJEquals equals) {            return Eq();        }        @Override        public Operator case_Less(MJLess less) {            return Slt();        }        @Override        public Operator case_Minus(MJMinus minus) {            return Sub();        }        @Override        public Operator case_Plus(MJPlus plus) {            return Add();        }        @Override        public Operator case_Times(MJTimes times) {            return Mul();        }    }    private class UnaryOperatorMatcher implements MJUnaryOperator.Matcher<Operator>{        @Override        public Operator case_UnaryMinus(MJUnaryMinus unaryMinus) {            return Sub();        }        @Override        public Operator case_Negate(MJNegate negate) {            return Sub();        }    }    private class TypeMatcher implements MJType.Matcher<Type>{            @Override            public Type case_TypeBool(MJTypeBool typeBool) {                return TypeBool();            }            @Override            public Type case_TypeIntArray(MJTypeIntArray typeIntArray) {                return arrpoint;            }            @Override            public Type case_TypeClass(MJTypeClass typeClass) {                String name =typeClass.getName();                TypePointer typePointer;                MJClassDecl ClasDecl = typeClass.getClassDeclaration();                if (objCls.containsKey(ClasDecl)) {                    //TemporaryVar x = objCls.get(name);                    typePointer = objCls.get(ClasDecl);                    //typePointer.getTo();                } else {                    // This should never happen                    //throw new RuntimeException(                    //      "Variable not found during translation");                    MJVarDeclList filds=ClasDecl.getFields();                    StructFieldList st=StructFieldList();                    for(MJVarDecl fild :filds)                    {                        Object f= fild.match(new StmtMatcher());                        TemporaryVar s=TemporaryVar("s");                        if(f instanceof Operand) {                            currentBlock.add(Load(s, (Operand) f));                            StructField sff = StructField(s.calculateType(), fild.toString());                            st.add(sff);                        }                    }                    ClassStruct = TypeStruct(name,st);                    objCls.put(ClasDecl,TypePointer(ClassStruct));                    typePointer = objCls.get(ClasDecl);                }                //TypeStruct(name,st);                return typePointer.getTo();            }            @Override            public Type case_TypeInt(MJTypeInt typeInt) {                return TypeInt();            }    }    private Map< MJVarDecl, TemporaryVar > tempVars = new HashMap<>();    private Map<MJClassDecl ,TypePointer> objCls = new HashMap<>();    public Translator(MJProgram javaProg) {        this.javaProg = javaProg;    }    public Prog translate() {        prog = Prog(typeStructList, GlobalList(), ProcList());        blocks = BasicBlockList();        Proc mainProc = Proc("main", TypeInt(), ParameterList(), blocks);        prog.getProcedures().add(mainProc);        entry.setName("entry");        currentBlock = entry;        blocks.add(currentBlock);        arrpoint = getPointer();        MJClassDeclList classlist =  javaProg.getClassDecls();         classdecllist(classlist);        for (MJStatement stmt : javaProg.getMainClass().getMainBody()) {            Object match = stmt.match(new StmtMatcher());            if(match instanceof Instruction)            {                currentBlock.add((Instruction)match);            }        }        currentBlock.add(ReturnExpr(ConstInt(0)));        boolean blockFound = false;        for(BasicBlock b : blocks)        {            if(b == currentBlock){                blockFound = true;                break;            }        }        if(!blockFound)            blocks.add(currentBlock);        prog.accept(this);        return prog;    }    private class StmtMatcher implements MJStatement.Matcher {        @Override        public Object case_VarDecl(MJVarDecl varDecl) {            MJType type = varDecl.getType();            Type llvmtype = type.match(new TypeMatcher());            TemporaryVar x = TemporaryVar(varDecl.getName());            currentBlock.add(Alloca(x,llvmtype));            tempVars.put(varDecl, x);            return VarRef(x);        }        //stm-while pair Working Rama Madhusudhan        @Override        public Object case_StmtWhile(MJStmtWhile stmtWhile) {            BasicBlock loopEnd = BasicBlock();            loopEnd.setName("loopEnd");            BasicBlock loopCondition= BasicBlock() ;            loopCondition.setName("loopCondition");            BasicBlock loopBody = BasicBlock();            loopBody.setName("loopBody");            BasicBlock previousBlock = currentBlock;            previousBlock.add(Jump(loopCondition));            currentBlock=loopCondition;            Operand condition = get_R(stmtWhile.getCondition());            currentBlock = loopBody;            stmtWhile.getLoopBody().match(new StmtMatcher());            loopCondition.add(Branch(condition.copy(),loopBody,loopEnd));            currentBlock.add(Jump(loopCondition));            blocks.add(loopCondition);            blocks.add(loopBody);            blocks.add(loopEnd);            currentBlock = loopEnd;            return null;        }        //ExprUnary  written by @rama inside StmtMatcher        //stm-assign        @Override        public Object case_StmtAssign(MJStmtAssign stmtAssign) {            Operand rightOp = get_R(stmtAssign.getRight());            Operand leftOp = get_L(stmtAssign.getLeft());            currentBlock.add(Store(leftOp,rightOp));            return null;        }        //Block written by @rama        @Override        public Object case_Block(MJBlock block) {            for(MJStatement statement : block)            {                statement.match(new StmtMatcher());            }            return null;        }        //ExprBinary written by @rama        //stm-return written by @rama in stmtMacher        @Override        public Object case_StmtReturn(MJStmtReturn stmtReturn) {            MJExpr e= stmtReturn.getResult();            Operand u = get_R(e);            currentBlock.add(ReturnExpr(u));            return null;        }        @Override        public Object case_StmtExpr(MJStmtExpr stmtExpr) {            MJExpr ex=stmtExpr.getExpr();            return get_R(ex);        }        //stm-print writen by @rama        @Override        public Object case_StmtPrint(MJStmtPrint stmtPrint) {            MJExpr ex=  stmtPrint.getPrinted();            Operand u = get_R(ex);            currentBlock.add(Print(u));            return  u;        }        //BoolConst        //matcher        //stm-if written by @rama        @Override        public Object case_StmtIf(MJStmtIf stmtIf) {            MJExpr co =stmtIf.getCondition();            MJStatement t =stmtIf.getIfTrue();            MJStatement f =stmtIf.getIfFalse();            //Object coo=co.match(new StmtMatcher());            Operand coo = get_R(co);            BasicBlock futureblock=BasicBlock(            );            BasicBlock previousBlock = currentBlock;            BasicBlock trueLabel = BasicBlock() ;            currentBlock=trueLabel;            t.match(new StmtMatcher());            currentBlock.add(Jump(futureblock));            blocks.add(trueLabel);            BasicBlock falseLabel= BasicBlock() ;            currentBlock= falseLabel;            f.match(new StmtMatcher());            currentBlock.add(Jump(futureblock));            previousBlock.add(Branch(coo, trueLabel, falseLabel));            currentBlock= futureblock;            futureblock.setName("futureblock");            blocks.add(falseLabel);            blocks.add(futureblock);            return null;        }    }    //This Method Gets Right side of the exp, written by @Monireh    private Operand get_R(MJExpr exp) {        return exp.match(new MJExpr.Matcher<Operand>() {            @Override            public Operand case_FieldAccess(MJFieldAccess fieldAccess) {                return null;            }            @Override            public Operand case_MethodCall(MJMethodCall methodCall) {                MJExprList ArgumentsList=   methodCall.getArguments();                String MetodName =methodCall.getMethodName();                MJMethodDecl methoddecl = methodCall.getMethodDeclaration();                Proc pr=currentproc;                MJExpr Receiver= methodCall.getReceiver();                OperandList OpList =OperandList();                for(MJExpr Arg:ArgumentsList)                {                    Object Argument =get_R(Arg);                    if(Argument instanceof Operand) {                        Operand Op = (Operand) Argument;                        OpList.add(Op);                    }                }                TemporaryVar temp = TemporaryVar("temp" );                Ast.Call(temp,(Operand)ProcedureRef(pr) ,OpList);                return VarRef(temp);            }            @Override            public Operand case_NewObject(MJNewObject newObject) {                String name = newObject.getClassName();                MJClassDecl ClasDecl= newObject.getClassDeclaration();                TypePointer  typePointer;                typePointer = objCls.get(ClasDecl);                TemporaryVar y=TemporaryVar("y");                TemporaryVar s=TemporaryVar("s");                TemporaryVar xg=TemporaryVar("x");                currentBlock.add(Alloc(xg,Sizeof(ClassStruct)));                currentBlock.add(Bitcast(y, typePointer,VarRef(xg)));                currentBlock.add(Load(s,VarRef(y)));                return VarRef(s);        }            @Override            public Operand case_ArrayLength(MJArrayLength arrayLength) {                TemporaryVar t3 = TemporaryVar("t3");                Operand arrayLen =  get_R(arrayLength.getArrayExpr());                currentBlock.add(GetElementPtr(t3, arrayLen, OperandList(ConstInt(0),                        ConstInt(0))));                TemporaryVar tempArrayLength = TemporaryVar("tempArrayLength");                currentBlock.add(Load(tempArrayLength,VarRef(t3)));                return VarRef(tempArrayLength);            }            @Override            public Operand case_ArrayLookup(MJArrayLookup arrayLookup) {                Operand l = get_L(arrayLookup);                TemporaryVar address = TemporaryVar("address");                currentBlock.add(Load(address, l));                return VarRef(address);            }            @Override            public Operand case_BoolConst(MJBoolConst boolConst) {                return ConstBool(boolConst.getBoolValue());            }            @Override            public Operand case_ExprNull(MJExprNull exprNull) {                TemporaryVar x = TemporaryVar("x");                currentBlock.add(Alloca(x,TypeNullpointer()));                return ConstInt(0);            }            //right            @Override            public Operand case_ExprBinary(MJExprBinary exprBinary) {                Operand right = exprBinary.getRight().match(this);                Operand left = exprBinary.getLeft().match(this);                Operator op = exprBinary.getOperator().match(new OperatorMatcher());                if(op instanceof Sdiv){                    TemporaryVar divRes = TemporaryVar("divRes");                    TemporaryVar result = TemporaryVar("result");                    currentBlock.add(BinaryOperation(result,right,Eq(),ConstInt(0)));                    BasicBlock L1 = BasicBlock(                            BinaryOperation(divRes, left, Sdiv(), right.copy())                    );                    BasicBlock futureBlock = BasicBlock();                    L1.setName("L1");                    L1.add(Jump(futureBlock));                    blocks.add(L1);                    BasicBlock L2 = BasicBlock(                            HaltWithError("Divid by Zero")                    );                    L2.setName("L2");                    blocks.add(L2);                    currentBlock.add( Branch(VarRef(result), L2, L1));                    currentBlock = futureBlock;                    //Doesn't make scense                    //currentBlock.add(BinaryOperation(divRes, left, Sdiv(), right.copy()));                    return VarRef(divRes);                }                TemporaryVar result = TemporaryVar("result" );                currentBlock.add(BinaryOperation(result, left, op, right));                return VarRef(result);            }            @Override            public Operand case_ExprThis(MJExprThis exprThis) {                return null;            }            //right            @Override            public Operand case_NewIntArray(MJNewIntArray newIntArray) {                BasicBlock previousBlock = currentBlock;                MJExpr size=  newIntArray.getArraySize();                Operand sizeArray = get_R(size);                BasicBlock initialBlock= BasicBlock();                BasicBlock validSize= BasicBlock();                BasicBlock loopBody= BasicBlock();                BasicBlock loopEnd= BasicBlock();                blocks.add(validSize);                blocks.add(loopBody);                blocks.add(loopEnd);                currentBlock=initialBlock;                previousBlock.add(Jump(currentBlock));                blocks.add(currentBlock);                TemporaryVar less=TemporaryVar("caseless");                currentBlock.add(BinaryOperation(less,sizeArray,Slt(),ConstInt(0)));                BasicBlock negativeSize= BasicBlock(HaltWithError("negative"));                blocks.add(negativeSize);                currentBlock.add(Branch(VarRef(less),negativeSize,validSize));                currentBlock=validSize;                TemporaryVar arrnewsize = TemporaryVar("arrnewsize");                TemporaryVar arraySizeInBytes = TemporaryVar("NewSize2");                //Load lR = Load(s,right);                currentBlock.add(BinaryOperation(arrnewsize, sizeArray.copy(), Add(), ConstInt(1)));                currentBlock.add(BinaryOperation(arraySizeInBytes,VarRef(arrnewsize), Mul(), ConstInt(4)));                TemporaryVar arraySizeRef=TemporaryVar("t");                currentBlock.add(Alloc(arraySizeRef,VarRef(arraySizeInBytes)));                array=TemporaryVar("arraySizeRef");                currentBlock.add(Bitcast(array,arrpoint,VarRef(arraySizeRef)));                TemporaryVar lengthAdrres =TemporaryVar("lengthAdrres");                currentBlock.add(GetElementPtr(lengthAdrres,VarRef(array),OperandList(ConstInt(0),ConstInt(0))));                currentBlock.add(Store(VarRef(lengthAdrres),sizeArray.copy()));                //here we initialize the array                BasicBlock loopStart= BasicBlock();                currentBlock.add(Jump(loopStart));                currentBlock=loopStart;                blocks.add(loopStart);                TemporaryVar i=TemporaryVar("i");                TemporaryVar next=TemporaryVar("next");                loopStart.add(PhiNode(i,                        TypeInt(),                        PhiNodeChoiceList(PhiNodeChoice(validSize,ConstInt(0)),                                PhiNodeChoice(loopBody,VarRef(next)))));                TemporaryVar  smal =TemporaryVar("smal");                currentBlock.add(BinaryOperation(smal,VarRef(i),Slt(),sizeArray.copy()));                currentBlock.add(Branch(VarRef(smal),loopBody,loopEnd));                currentBlock=loopBody;                TemporaryVar adressofcounter= TemporaryVar("address");                currentBlock.add(GetElementPtr(adressofcounter,                        VarRef(array),                        OperandList(ConstInt(0),                                ConstInt(1),                                VarRef(i))));                currentBlock.add(Store(                        VarRef(adressofcounter),                        ConstInt(0)));                currentBlock.add(BinaryOperation(next,VarRef(i),Add(),ConstInt(1)));                currentBlock.add(Jump(loopStart));                currentBlock = loopEnd;                return VarRef(array);            }            @Override            //Number writen by @mahsa in get_R            public Operand case_Number(MJNumber number) {                int val = number.getIntValue();                return ConstInt(val);            }            //VarUse of get_R            @Override            public Operand case_VarUse(MJVarUse varUse) {                //MJVarUse varDecl = (MJVarUse) exp;                MJVarDecl varDeclvar = varUse.getVariableDeclaration();                TemporaryVar y = TemporaryVar("y");                if (tempVars.containsKey(varDeclvar)) {                    TemporaryVar x = tempVars.get(varDeclvar);                    Load l = Load(y,VarRef(x));                    currentBlock.add(l);                    return VarRef(l.getVar());                } else {                    // This should never happen                    throw new RuntimeException(                            "Variable not found during translation");                }            }            //right in get_R            @Override            public Operand case_ExprUnary(MJExprUnary exprUnary) {                MJExpr ex= exprUnary.getExpr();                MJUnaryOperator  o=exprUnary.getUnaryOperator();                Operand e = get_R(ex);                Operator llvmminus= o.match(new UnaryOperatorMatcher());                if(e.calculateType() instanceof TypeBool){                    TemporaryVar NegBool = TemporaryVar("NegBool");                    currentBlock.add(BinaryOperation(NegBool, ConstBool(true), llvmminus, e));                    return VarRef(NegBool);                } else {                    TemporaryVar Negresult = TemporaryVar("Negresult");                    currentBlock.add(BinaryOperation(Negresult, (ConstInt(0)), llvmminus, e));                    return VarRef(Negresult);                }            }        });    }    //This Method Gets Left side of the exp, written by @Monireh    private Operand get_L(MJExpr exp) {        //Left side of an Assign could be one of the following cases, So we should define them first        return exp.match(new MJExpr.Matcher<Operand>() {            @Override            public Operand case_FieldAccess(MJFieldAccess fieldAccess) {                return null;            }            @Override            public Operand case_MethodCall(MJMethodCall methodCall) {                return null;            }            @Override            public Operand case_NewObject(MJNewObject newObject) {                return null;            }            @Override            public Operand case_ArrayLength(MJArrayLength arrayLength) {                return get_R(arrayLength.getArrayExpr());            }            //left            @Override            public Operand case_ArrayLookup(MJArrayLookup arrayLookup) {                MJExpr exp1 = arrayLookup.getArrayExpr();                MJExpr in=  arrayLookup.getArrayIndex();                Operand index=get_R(in);                Operand expr=get_R(exp1);                TemporaryVar x = TemporaryVar("x");                Load(x,expr);                //three blocks for handling the cases                // a == null                // i<0                // i>= a.length                TemporaryVar t2 = TemporaryVar("t2");                TemporaryVar smallerThanZero = TemporaryVar("smallerThanZero");                TemporaryVar greaterThanLength = TemporaryVar("greaterThanLength");                TemporaryVar outOfBounds = TemporaryVar("outOfBounds");                currentBlock.add(                        BinaryOperation(                                smallerThanZero,index,Slt(),ConstInt(0)                        )                );                TemporaryVar length = TemporaryVar("length");                currentBlock.add(GetElementPtr(length,                        expr.copy(),                        OperandList(ConstInt(0),                                ConstInt(0))));                TemporaryVar tempArrayLength = TemporaryVar("tempArrayLength");                currentBlock.add(Load(tempArrayLength,VarRef(length)));                TemporaryVar newArrayLength = TemporaryVar("newArrayLength");                currentBlock.add(                        BinaryOperation(                                newArrayLength,VarRef(tempArrayLength),Sub(),ConstInt(1)                        )                );                currentBlock.add(                        BinaryOperation(                                 greaterThanLength,VarRef(newArrayLength),Slt(),index.copy()                        )                );                currentBlock.add(                        BinaryOperation(                                outOfBounds,VarRef(smallerThanZero),Or(),VarRef(greaterThanLength)                        )                );                BasicBlock outOfBoundsError = BasicBlock();                outOfBoundsError.add(HaltWithError("Array Index out of Bounds"));                blocks.add(outOfBoundsError);                BasicBlock correctindex = BasicBlock();                blocks.add(correctindex);                currentBlock.add(                        Branch(                                VarRef(outOfBounds),outOfBoundsError,correctindex)                        );                currentBlock = correctindex;                currentBlock.add(GetElementPtr(t2,                        expr.copy(),                        OperandList(ConstInt(0),                                ConstInt(1),                                index.copy())));                return VarRef(t2);            }            //left            @Override            public Operand case_BoolConst(MJBoolConst boolConst) {                return ConstBool(boolConst.getBoolValue()) ;            }            @Override            public Operand case_ExprNull(MJExprNull exprNull) {                TemporaryVar x = TemporaryVar("x");                currentBlock.add(Alloca(x,TypeNullpointer()));                return ConstInt(0);            }            //left            @Override            public Operand case_ExprBinary(MJExprBinary exprBinary) {                //Operand right = get_R(exprBinary.getRight());                Operand right = exprBinary.getRight().match(this);                //Operand left = get_L(exprBinary.getLeft());                Operand left = exprBinary.getLeft().match(this);                Operator op = exprBinary.getOperator().match(new OperatorMatcher());                //Load lR = Load(s,right);                if(op instanceof Sdiv){                    TemporaryVar resultr = TemporaryVar("ss");                    TemporaryVar resul = TemporaryVar("s");                    currentBlock.add(BinaryOperation(resul,right,Eq(),ConstInt(0)));                    BasicBlock L1 =BasicBlock(                            BinaryOperation(resultr,left,Sdiv(),right.copy())                    );                    L1.setName("L1");                    BasicBlock futureBlock = BasicBlock();                    L1.add( Jump(futureBlock));                    blocks.add(L1);                    BasicBlock L2 =BasicBlock(                            HaltWithError("devby Zero")                    );                    L1.setName("L2");                    blocks.add(L2);                    currentBlock.add(Branch(VarRef(resul),L1,L2));                    currentBlock = futureBlock;                    //currentBlock=end;                    return VarRef(resultr);                }                TemporaryVar result = TemporaryVar("result" );                currentBlock.add(BinaryOperation(result, left, op, right));                return VarRef(result);            }            @Override            public Operand case_ExprThis(MJExprThis exprThis) {                return null;            }            //left            @Override            public Operand case_NewIntArray(MJNewIntArray newIntArray) {                throw new RuntimeException("Is not used!");            }            @Override            // Number writen by @Mahsa in get_L            public Operand case_Number(MJNumber number) {                int val = number.getIntValue();                return ConstInt(val);            }            //VarUse of get_L            @Override            public Operand case_VarUse(MJVarUse varUse) {                MJVarUse varDecl = (MJVarUse) exp;                MJVarDecl varDeclvar = varDecl.getVariableDeclaration();                //TemporaryVar y = TemporaryVar("y"+varUse.getVarName());                if (tempVars.containsKey(varDeclvar)) {                    TemporaryVar x = tempVars.get(varDeclvar);                    return VarRef(x);                } else {                    // This should never happen                    throw new RuntimeException(                            "Variable not found during translation");                }            }            //unary left written by rama in get_L            @Override            public Operand case_ExprUnary(MJExprUnary exprUnary) {                MJExpr ex= exprUnary.getExpr();                MJUnaryOperator  o=exprUnary.getUnaryOperator();                Operand e=get_R(ex);                Operator ad = o.match(new UnaryOperatorMatcher());                if(ad instanceof Sub){                    TemporaryVar result = TemporaryVar("ss");                    currentBlock.add(BinaryOperation(result,(ConstInt(0)),ad,e));                    return VarRef(result);                }                return null;            }        });    }//To do the rest of the cases}