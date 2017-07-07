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
    /**
     * This HashMap is used to hold the TemporaryVar and MipsRegister Hash.
     * This is used in printing method later.
     */
    private HashMap<TemporaryVar,MipsRegister> variableRegisterMap;
    LLVMMatcher()
    {
        mipsStmtList = Mips.StmtList(
                       Mips.Label("main"),
                       Mips.Move(Mips.Register(30), Mips.Register(29))
                );
            mipsProg= Mips.Prog(mipsStmtList);
            variableRegisterMap = new HashMap<>();
            temporaryRegisters = new ArrayList<>();
            for(int i=8;i<16;i++)
                temporaryRegisters.add(Mips.Register(i));
            temporaryRegisterIndex = 0;
    }

    private void checkRegisterIndex()
    {
        if(temporaryRegisterIndex > 7)
            temporaryRegisterIndex = 0;
    }
    void createLabel(String labelName)
    {
        MipsLabel newLabel = Mips.Label(labelName);
        mipsStmtList.add(newLabel);
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
        
    }

    @Override
    public void case_Load(Load load) {
        
    }

    @Override
    public void case_Branch(Branch branch) {
       
    }

    @Override
    public void case_Bitcast(Bitcast bitcast) {
        
    }

    @Override
    public void case_GetElementPtr(GetElementPtr getElementPtr) {
       
    }

    @Override
    public void case_HaltWithError(HaltWithError haltWithError) {
       
    }

    @Override
    public void case_ReturnVoid(ReturnVoid returnVoid) {
       
    }

    @Override
    public void case_CommentInstr(CommentInstr commentInstr) {
         
    }

    @Override
    public void case_Jump(Jump jump) {
        BasicBlock jumpLabel = jump.getLabel();
        String jumpLabelName = jumpLabel.getName();
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
            int printValue = Integer.parseInt(print.getE().toString());
            mipsStmtList.add(Mips.Li(Mips.Register(11),printValue));
            register = Mips.Register(11);
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
        mipsStmtList.add(Mips.Jal(Mips.LabelRef("_exit")));
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
            checkRegisterIndex();
            MipsRegister tempRegister = temporaryRegisters.get(temporaryRegisterIndex++);
            mipsStmtList.add(Mips.Li(tempRegister,constInt.getIntVal()));
            return tempRegister;
        }

        @Override
        public MipsRegister case_ConstBool(ConstBool constBool) {
            return null;
        }

        @Override
        public MipsRegister case_ProcedureRef(ProcedureRef procedureRef) {
            return null;
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
