package minillvm.tomips;

import minillvm.ast.*;
import mips.ast.MipsProg;



public class MiniLLVMToMips {
	public static MipsProg translateProgram(Prog prog) {

        ProcList llvmProcedures = prog.getProcedures();

        LLVMMatcher llvmMatcher = new LLVMMatcher();



        for(Proc procedure: llvmProcedures)
        {
            BasicBlockList basicBlocks = procedure.getBasicBlocks();
            llvmMatcher.createLabel(procedure.getName());
            llvmMatcher.setStackPointer(procedure);
            for(BasicBlock basicBlock:basicBlocks)
            {

                    llvmMatcher.createLabel(basicBlock.getName()+"_"+procedure.getName());
                    llvmMatcher.setProcedure(procedure.getName());
                for(Instruction everyInstruction: basicBlock)
                {
                    everyInstruction.match(llvmMatcher);

                }
            }
            if(llvmMatcher.getLabelRef(procedure.getName()))
            {
                llvmMatcher.addJumpStatement();
            }
            if(procedure.getName().equalsIgnoreCase("main"))
            {
                llvmMatcher.addExit();
            }
        }


        //mipsProg(Mips.StmtList(Mips.Jal(Mips.LabelRef("_exit"))));

		return llvmMatcher.returnMipsProg();

	}
}
