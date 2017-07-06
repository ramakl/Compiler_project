package minillvm.tomips;

import minillvm.ast.*;
import mips.ast.Mips;

import mips.ast.MipsOperator;
import mips.ast.MipsProg;
import mips.ast.MipsStmtList;

/**
 * Created by madhu on 7/6/17.
 */
public class LLVMMatcher implements minillvm.ast.Instruction.MatcherVoid{

    private MipsStmtList mipsStmtList;
    private MipsProg mipsProg;
    LLVMMatcher()
    {
        mipsStmtList = Mips.StmtList(
                       Mips.Label("main"),
                       Mips.Move(Mips.Register(30), Mips.Register(29))
                );
            mipsProg= Mips.Prog(mipsStmtList);
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
         
    }

    @Override
    public void case_BinaryOperation(BinaryOperation binaryOperation) {
        TemporaryVar result = binaryOperation.getVar();
        Operand left = binaryOperation.getLeft();
        Operand right = binaryOperation.getRight();
        Operator operator = binaryOperation.getOperator();
        int leftValue = Integer.parseInt(left.toString());
        int rightValue = Integer.parseInt((right.toString()));
        MipsOperator mipsOperator = operator.match(new LLVMOperatorMatcher());
        mipsStmtList.add(Mips.Li(Mips.Register(8),leftValue));
        mipsStmtList.add(Mips.Li(Mips.Register(9),rightValue));
        mipsStmtList.add(
                Mips.BinaryOp(
                        mipsOperator,Mips.Register(10),
                        Mips.Register(8),
                        Mips.Register(9)
                )
        );
        mipsStmtList.add(
                Mips.BinaryOpI(
                        Mips.Add(),Mips.Register(29),Mips.Register(29),-20
                )
        );
        mipsStmtList.add(
                Mips.Sw(
                        Mips.Register(10),Mips.BaseAddress(0,Mips.Register(29))
                )
        );

    }

    @Override
    public void case_PhiNode(PhiNode phiNode) {
         
    }

    @Override
    public void case_Print(Print print) {

        int printValue = Integer.parseInt(print.getE().toString());
        mipsStmtList.add(Mips.Li(Mips.Register(4),printValue));
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
}
