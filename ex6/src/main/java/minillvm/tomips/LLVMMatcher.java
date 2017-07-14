package minillvm.tomips;

import minillvm.ast.*;
import mips.ast.*;
import java.util.ArrayList;
import java.util.HashMap;

import static minillvm.ast.Ast.TypeInt;

/**
 * Created by madhu on 7/6/17.
 */
public class LLVMMatcher implements minillvm.ast.Instruction.MatcherVoid{

    private MipsStmtList mipsStmtList;
    private MipsProg mipsProg;
    private ArrayList<MipsRegister> temporaryRegisters;
    private int temporaryRegisterIndex;
    private String procedureName;
    private ArrayList<String> labelRefs;
    private HashMap<String,String> jumpHandler;
    private int nextBlockCounter;
    private MipsRegister accumulator;
    private MipsRegister valueRegister1;
    private MipsRegister valueRegister2;
    private MipsRegister stackPointer = Mips.Register(29);
    private MipsRegister framePointer = Mips.Register(30);
    private HashMap<MipsRegister, ArrayList<MipsRegister>> functionParameterMap;
    private int stackSize;
    //private HashMap<MipsRegister,Te>
    /**
     * This HashMap is used to hold the TemporaryVar and MipsRegister Hash.
     * This is used in printing method later.
     */
    //TODO Instead of using the temporary variables, $a0 to $a7, spill all the variables on the stack.
    //TODO Handling the blocks inside the procedures.
    private HashMap<TemporaryVar,MipsRegister> variableRegisterMap;
    private HashMap<TemporaryVar,MipsAddress> variableAdressMap;
    private int offsetCounter;
    LLVMMatcher()
    {
            mipsStmtList = Mips.StmtList(
                Mips.Move(framePointer, stackPointer)
            );

            mipsProg= Mips.Prog(mipsStmtList);
            variableRegisterMap = new HashMap<>();
             variableAdressMap=new HashMap<>();
            temporaryRegisters = new ArrayList<>();
            for(int i=8;i<16;i++)
                temporaryRegisters.add(Mips.Register(i));
            temporaryRegisterIndex = 0;
            labelRefs = new ArrayList<>();
            jumpHandler = new HashMap<>();
            nextBlockCounter = 0;
            valueRegister1 = Mips.Register(2);
            valueRegister2 = Mips.Register(3);
            accumulator = Mips.Register(4);
            offsetCounter = 0;
            functionParameterMap = new HashMap();
            stackSize = 0;
    }

    void Prologue()
    {

    }
    void Epilogue()
    {

    }
    void setStackPointer(Proc procedure)
    {

        
        BasicBlockList basicBlocks = procedure.getBasicBlocks();
        for(BasicBlock basicBlock:basicBlocks)
        {
            for(Instruction i : basicBlock)
            {
                if(i instanceof Assign)
                {
                    Assign assign = (Assign) i;
                    TemporaryVar assignVar = assign.getVar();
                    int size = calculateType(assignVar.calculateType());
                    stackSize = stackSize + size;
                }
            }
        }
        mipsStmtList.add(Mips.BinaryOpI(Mips.Sub(),stackPointer.copy(),stackPointer.copy(),stackSize));
    }
    private int calculateType(Type variableType)
    {
        int numberOfBytes = 4;
        if(variableType.equalsType(TypeInt()))
            numberOfBytes = 4;
        else if(variableType.equalsType(Ast.TypeBool()))
        {
            numberOfBytes = 1;
        }
        else if(variableType.equalsType(Ast.TypeByte()))
        {
            numberOfBytes = 1;
        }
        return numberOfBytes;

//TODO handle it for case Array, NullPointer and TypePointer
    }

    void setProcedure(String name)
    {


        procedureName = name;
    }

    private void checkRegisterIndex()
    {
        if(temporaryRegisterIndex > 7)
            temporaryRegisterIndex = 0;
    }

    private MipsBaseAddress spillOnStack(int offset)
    {
        MipsBaseAddress address = Mips.BaseAddress(offsetCounter, stackPointer.copy());
        offsetCounter=offsetCounter+offset;
        return address;
    }

    private void RememberLabelRef(String labelRef)
    {
        labelRefs.add(labelRef);
    }
    boolean getLabelRef(String labelRef)
    {
        return labelRefs.contains(labelRef);
    }

    void createLabel(String labelName)
    {
        MipsLabel newLabel = Mips.Label(labelName);
        mipsStmtList.add(newLabel);
    }

    void addExit()
    {
        mipsStmtList.add(Mips.Jal(Mips.LabelRef("_exit")));
    }
    void addJumpStatement()
    {
        String jumpLabelName = null;
        for(String p : jumpHandler.keySet())
        {
            if(p.equalsIgnoreCase(procedureName)) {
                jumpLabelName = jumpHandler.get(p);
                break;
            }
        }
        MipsLabelRef labelRef = Mips.LabelRef(jumpLabelName);
        mipsStmtList.add(Mips.J(labelRef));
    }

    MipsProg returnMipsProg()
    {
        return mipsProg;
    }

    @Override
    public void case_Alloc(Alloc alloc) {
        TemporaryVar allocVar = alloc.getVar();
        MipsBaseAddress adddress = spillOnStack(allocVar.size());
        variableAdressMap.put(allocVar,adddress);
        alloc.getSizeInBytes();
    }

    @Override
    public void case_Call(Call call) {
        TemporaryVar callVar = call.getVar();
        OperandList callArguments = call.getArguments();
        ArrayList parameterList = new ArrayList();
        for(Operand operand: callArguments)
        {
            MipsRegister register = operand.match(new LLVMOperandMatcher());
            parameterList.add(register);

        }
        MipsRegister mipsRegister = call.getFunction().match(new LLVMOperandMatcher());
        String label = "nextBlock_"+nextBlockCounter++;
        createLabel(label);
        variableRegisterMap.put(callVar,mipsRegister);
        String procedureName = labelRefs.get(labelRefs.size() - 1);

        functionParameterMap.put(mipsRegister,parameterList);
        jumpHandler.put(procedureName,label);
    }

    @Override
    public void case_Load(Load load) {
        MipsRegister match = load.getAddress().match(new LLVMOperandMatcher());
        TemporaryVar var = load.getVar();
        variableRegisterMap.put(var,match);
    }

    @Override
    public void case_Branch(Branch branch) {
        Operand condition = branch.getCondition();
        MipsRegister conditionRegister = condition.match(new LLVMOperandMatcher());
        String ifFalseLabel = branch.getIfFalseLabel().getName()+"_"+procedureName;
        MipsLabelRef mipsIfFalseLabel = Mips.LabelRef(ifFalseLabel);
        mipsStmtList.add(Mips.Beqz(conditionRegister.copy(),mipsIfFalseLabel.copy()));


    }

    @Override
    public void case_Bitcast(Bitcast bitcast) {
        
    }

    @Override
    public void case_GetElementPtr(GetElementPtr getElementPtr) {
       
    }

    @Override
    public void case_HaltWithError(HaltWithError haltWithError) {

        mipsStmtList.add(Mips.Li(valueRegister1,4));

        mipsStmtList.add(Mips.Jal(Mips.LabelRef("_error")));

    }

    @Override
    public void case_ReturnVoid(ReturnVoid returnVoid) {
       System.out.println("This is a return void method");
    }

    @Override
    public void case_CommentInstr(CommentInstr commentInstr) {
         
    }

    @Override
    public void case_Jump(Jump jump) {
        BasicBlock jumpLabel = jump.getLabel();
        String jumpLabelName = jumpLabel.getName()+"_"+procedureName;
        MipsLabelRef labelRef = Mips.LabelRef(jumpLabelName);
        mipsStmtList.add(Mips.J(labelRef));

    }

    @Override
    public void case_BinaryOperation(BinaryOperation binaryOperation) {
        TemporaryVar result =  binaryOperation.getVar();
        Operand left = binaryOperation.getLeft();
        Operand right = binaryOperation.getRight();
        Operator operator = binaryOperation.getOperator();
        LLVMOperandMatcher operandMatcher = new LLVMOperandMatcher();
        MipsRegister leftRegister = left.match(operandMatcher);
        MipsRegister rightRegister = right.match(operandMatcher);
        MipsOperator mipsOperator = operator.match(new LLVMOperatorMatcher());
        checkRegisterIndex();
        MipsRegister resultRegister = temporaryRegisters.get(temporaryRegisterIndex++);
        mipsStmtList.add(
                Mips.BinaryOp(
                        mipsOperator,resultRegister.copy(),
                        leftRegister.copy(),
                        rightRegister.copy()
                )
        );
        int sizeOfVariable = calculateType(result.calculateType());
        MipsBaseAddress address = spillOnStack(sizeOfVariable);
        variableAdressMap.put(result,address);


       mipsStmtList.add(
                Mips.Sw(
                        resultRegister.copy(),address)

        );



    }

    @Override
    public void case_PhiNode(PhiNode phiNode) {
         
    }

    @Override
    public void case_Print(Print print) {

        MipsRegister register = null;
        for(TemporaryVar v: variableRegisterMap.keySet())
        {
            Operand e = print.getE();
            if(((VarRef) e).getVariable().structuralEquals(v))
            {
                register = variableRegisterMap.get(v);
                break;
            }
        }
        if(null == register)
        {
            MipsRegister match = print.getE().match(new LLVMOperandMatcher());
            checkRegisterIndex();
            register= temporaryRegisters.get(temporaryRegisterIndex++);
            mipsStmtList.add(Mips.Move(register.copy(),match.copy()));
        }
        mipsStmtList.add(Mips.Li(valueRegister1.copy(), 1));
        mipsStmtList.add(Mips.Move(accumulator.copy(), register.copy()));
        mipsStmtList.add(Mips.Jal(Mips.LabelRef("_print")));

    }

    @Override
    public void case_Store(Store store) {
        Operand value = store.getValue();
        MipsRegister match = value.match(new LLVMOperandMatcher());
        MipsRegister mipsRegister = store.getAddress().match(new LLVMOperandMatcher());
        mipsStmtList.add(Mips.Sw(match.copy(),Mips.BaseAddress(0,mipsRegister.copy())));

    }

    @Override
    public void case_ReturnExpr(ReturnExpr returnExpr) {
        Integer returnValue = Integer.parseInt(returnExpr.getReturnValue().toString());
        mipsStmtList.add(Mips.Li(accumulator.copy(),returnValue));

        mipsStmtList.add(
                Mips.BinaryOpI(
                        Mips.Add(),Mips.Register(29),Mips.Register(29),stackSize
                )
        );
        offsetCounter=0;
        mipsStmtList.add(Mips.Jal(Mips.Register(31)));
    }

    @Override
    public void case_Alloca(Alloca alloca) {
        Type variableType = alloca.getType();
        int numberOfBytes = calculateType(variableType);
        mipsStmtList.add(Mips.Li(accumulator,numberOfBytes));
        mipsStmtList.add(Mips.Jal(Mips.LabelRef("_halloc")));
        TemporaryVar var = alloca.getVar();
        checkRegisterIndex();
        MipsRegister register = temporaryRegisters.get(temporaryRegisterIndex++);
        mipsStmtList.add(Mips.Move(register,valueRegister1));
        variableRegisterMap.put(var,register);
    }

    public class LLVMOperatorMatcher implements Operator.Matcher<MipsOperator>{

        @Override
        public MipsOperator case_Eq(Eq eq) {
            return Mips.Seq();
        }

        @Override
        public MipsOperator case_Srem(Srem srem) {
            return Mips.Rem();
        }

        @Override
        public MipsOperator case_Add(Add add) {
            return Mips.Add();
        }

        @Override
        public MipsOperator case_Xor(Xor xor) {
            return Mips.Xor();
        }

        @Override
        public MipsOperator case_And(And and) {
            return Mips.And();
        }

        @Override
        public MipsOperator case_Slt(Slt slt) {
            return Mips.Slt();
        }

        @Override
        public MipsOperator case_Sub(Sub sub) {
            return Mips.Sub();
        }

        @Override
        public MipsOperator case_Sdiv(Sdiv sdiv) {
            return Mips.Div();
        }

        @Override
        public MipsOperator case_Or(Or or) {
            return Mips.Or();
        }

        @Override
        public MipsOperator case_Mul(Mul mul) {
            return Mips.Mul();
        }
    }

    public class LLVMOperandMatcher implements Operand.Matcher<MipsRegister>{

        @Override
        public MipsRegister case_GlobalRef(GlobalRef globalRef) {
            return null;
        }

        @Override
        public MipsRegister case_Sizeof(Sizeof sizeof) {
            return null;
        }

        @Override
        public MipsRegister case_ConstStruct(ConstStruct constStruct) {
            return null;
        }

        @Override
        public MipsRegister case_Nullpointer(Nullpointer nullpointer) {
            return null;
        }

        @Override
        public MipsRegister case_ConstInt(ConstInt constInt) {
            checkRegisterIndex(); //TODO call method ReserveVariableOnStack
            MipsRegister tempRegister = temporaryRegisters.get(temporaryRegisterIndex++);
            mipsStmtList.add(Mips.Li(tempRegister,constInt.getIntVal()));
            return tempRegister;
        }

        @Override
        public MipsRegister case_ConstBool(ConstBool constBool) {
            checkRegisterIndex();
            MipsRegister tempRegister = temporaryRegisters.get(temporaryRegisterIndex++);
            int value = 0;
            if(constBool.getBoolVal())
                value = 1;
            mipsStmtList.add(Mips.Li(tempRegister,value));
            return tempRegister;
        }

        @Override
        public MipsRegister case_ProcedureRef(ProcedureRef procedureRef) {

            Proc procedure = procedureRef.getProcedure();

            MipsLabelRef labelRef = Mips.LabelRef(procedure.getName());

            mipsStmtList.add(Mips.Jal(labelRef));
            RememberLabelRef(procedure.getName());
            mipsStmtList.add(Mips.La(accumulator.copy(),labelRef.copy()));

            return accumulator.copy();
        }

        @Override
        public MipsRegister case_VarRef(VarRef varRef) {

            Variable variable = varRef.getVariable();
            MipsAddress Address = null;
            MipsRegister register;
            for(TemporaryVar v : variableAdressMap.keySet())
            {
                if(v.structuralEquals(variable)) {
                    Address = variableAdressMap.get(v);
                    break;
                }

            }
            checkRegisterIndex();
            register = temporaryRegisters.get(temporaryRegisterIndex++);

            mipsStmtList.add(Mips.Lw(register.copy(),Address.copy()));
            return register.copy();

        }
    }
}
