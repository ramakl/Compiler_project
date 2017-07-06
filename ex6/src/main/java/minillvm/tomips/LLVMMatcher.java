package minillvm.tomips;

import minillvm.ast.*;
import mips.ast.Mips;

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
}
