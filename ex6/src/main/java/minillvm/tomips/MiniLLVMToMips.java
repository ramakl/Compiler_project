package minillvm.tomips;

import minillvm.ast.*;
import mips.ast.MipsProg;



public class MiniLLVMToMips {
	public static MipsProg translateProgram(Prog prog) {

        ProcList llvmProcedures = prog.getProcedures();

        LLVMMatcher llvmMatcher = new LLVMMatcher();

        TypeStructList structTypes = prog.getStructTypes();


        for(Proc procedure: llvmProcedures)
        {
            BasicBlockList basicBlocks = procedure.getBasicBlocks();
            for(BasicBlock basicBlock:basicBlocks)
            {
                String basicBlockName = basicBlock.getName();
                llvmMatcher.createLabel(basicBlockName);
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
