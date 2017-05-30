package translation;

import minijava.ast.*;
import minillvm.ast.*;
import static minillvm.ast.Ast.*;


public class Translator {

	private final MJProgram javaProg;

	public Translator(MJProgram javaProg) {
		this.javaProg = javaProg;
	}

	public Prog translate() {
		// TODO add your translation code here

		// TODO here is an example of a minimal program (remove this)
		Prog prog = Prog(TypeStructList(), GlobalList(), ProcList());

		BasicBlockList blocks = BasicBlockList();
		Proc mainProc = Proc("main", TypeInt(), ParameterList(), blocks);
		prog.getProcedures().add(mainProc);


		BasicBlock entry = BasicBlock(
				Print(ConstInt(42)),
				ReturnExpr(ConstInt(0))
		);
		entry.setName("entry");
		blocks.add(entry);


		return prog;
	}

}
