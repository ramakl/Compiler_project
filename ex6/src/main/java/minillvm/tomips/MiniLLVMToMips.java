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
            for(BasicBlock basicBlock:basicBlocks)
            {
                for(Instruction everyInstruction: basicBlock)
                {
                    everyInstruction.match(llvmMatcher);

                }
            }
        }



        MipsProg mipsProg = llvmMatcher.returnMipsProg();

        System.out.println(mipsProg);
		return mipsProg;

	}
}
