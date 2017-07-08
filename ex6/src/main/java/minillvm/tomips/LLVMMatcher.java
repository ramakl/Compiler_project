package minillvm.tomips;

import minillvm.ast.*;
import mips.ast.*;
import java.util.ArrayList;
import java.util.HashMap;

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
    private int returnBlockCounter;
    /**
     * This HashMap is used to hold the TemporaryVar and MipsRegister Hash.
     * This is used in printing method later.
     */
    //TODO Instead of using the temporary variables, $a0 to $a7, spill all the variables on the stack.
    //TODO Handling the blocks inside the procedures.
    private HashMap<TemporaryVar,MipsRegister> variableRegisterMap;
    LLVMMatcher()
    {
        mipsStmtList = Mips.StmtList(
                       Mips.Move(Mips.Register(30), Mips.Register(29))
                );
            mipsProg= Mips.Prog(mipsStmtList);
            variableRegisterMap = new HashMap<>();
            temporaryRegisters = new ArrayList<>();
            for(int i=8;i<16;i++)
                temporaryRegisters.add(Mips.Register(i));
            temporaryRegisterIndex = 0;
            labelRefs = new ArrayList<>();
            jumpHandler = new HashMap<>();
            returnBlockCounter = 0;
    }

    void ReserveVariableOnStack()
    {

    }

    void setProcedure(String name)
    {
        procedureName = name;
    }

    private void checkRegisterIndex()
    {
        if(temporaryRegisterIndex > 7)
            temporaryRegisterIndex = 0; //TODO use instead Stack Pointer - Determine the type of variable and allocate the space and
                                        //TODO add it to the variableRegister Map
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
       
    }

    @Override
    public void case_Call(Call call) {
        TemporaryVar callVar = call.getVar();

        MipsRegister mipsRegister = call.getFunction().match(new LLVMOperandMatcher());
        String label = "returnBlock_"+returnBlockCounter++;
        createLabel(label);
        variableRegisterMap.put(callVar,mipsRegister);
        String procedureName = labelRefs.get(labelRefs.size() - 1);
        jumpHandler.put(procedureName,label);
    }

    @Override
    public void case_Load(Load load) {
        
    }

    @Override
    public void case_Branch(Branch branch) {
        Operand condition = branch.getCondition();
        MipsRegister conditionRegister = condition.match(new LLVMOperandMatcher());
        String ifTrueLabel = branch.getIfTrueLabel().getName()+"_"+procedureName;
        MipsLabelRef mipsIfTrueLabel = Mips.LabelRef(ifTrueLabel);
        mipsStmtList.add(Mips.Beqz(conditionRegister.copy(),mipsIfTrueLabel.copy()));


    }

    @Override
    public void case_Bitcast(Bitcast bitcast) {
        
    }

    @Override
    public void case_GetElementPtr(GetElementPtr getElementPtr) {
       
    }

    @Override
    public void case_HaltWithError(HaltWithError haltWithError) {
        String errorMessage = haltWithError.getMsg();
        MipsLabelRef label = Mips.LabelRef(errorMessage);
        checkRegisterIndex();
        MipsRegister register = temporaryRegisters.get(temporaryRegisterIndex++);
        mipsStmtList.add(Mips.La(register,label));
        mipsStmtList.add(Mips.Li(Mips.Register(2), 4));//preparing Mips to print a string
        mipsStmtList.add(Mips.Move(Mips.Register(4), register.copy()));
        //mipsStmtList.add(Mips.Jal(Mips.LabelRef("_print")));
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
                        mipsOperator,resultRegister,
                        leftRegister.copy(),
                        rightRegister.copy()
                )
        );
        mipsStmtList.add(
                Mips.BinaryOpI(
                        Mips.Add(),Mips.Register(29),Mips.Register(29),-20
                )
        );
        mipsStmtList.add(
                Mips.Sw(
                        resultRegister.copy(),Mips.BaseAddress(0,Mips.Register(29))
                )
        );
        variableRegisterMap.put(result,resultRegister);

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
            mipsStmtList.add(Mips.Move(register,match.copy()));


        }
        mipsStmtList.add(Mips.Li(Mips.Register(2), 1));
        mipsStmtList.add(Mips.Move(Mips.Register(4), register.copy()));
        mipsStmtList.add(Mips.Jal(Mips.LabelRef("_print")));

    }

    @Override
    public void case_Store(Store store) {
         
    }

    @Override
    public void case_ReturnExpr(ReturnExpr returnExpr) {
        Integer returnValue = Integer.parseInt(returnExpr.getReturnValue().toString());
        mipsStmtList.add(Mips.Li(Mips.Register(4),returnValue));

    }

    @Override
    public void case_Alloca(Alloca alloca) {
         
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
            mipsStmtList.add(Mips.La(Mips.Register(4),labelRef.copy()));

            return Mips.Register(4);
        }

        @Override
        public MipsRegister case_VarRef(VarRef varRef) {
            Variable variable = varRef.getVariable();
            MipsRegister register = null;
            for(TemporaryVar v : variableRegisterMap.keySet())
            {
                if(v.structuralEquals(variable)) {
                    register = variableRegisterMap.get(v);
                    break;
                }
            }
            return register;
        }
    }
}
