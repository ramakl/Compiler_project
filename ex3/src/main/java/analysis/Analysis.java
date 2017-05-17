package analysis;

import minijava.ast.*;

import java.util.*;

public class Analysis {

    private final MJProgram prog;
    private List<TypeError> typeErrors = new ArrayList<>();

    public void addError(MJElement element, String message) {
        typeErrors.add(new TypeError(element, message));
    }

    public Analysis(MJProgram prog) {
        this.prog = prog;
    }

    public void check() {
        //TODO implement type checking here!

        MJClassDeclList g = prog.getClassDecls();
        for (int j = 0; j <= g.size(); j++) {
            MJClassDecl c = g.get(j);
            MJExtended cc = c.getExtended();

            if (!(cc.toString().equals("ExtendsNothing"))) {
                //for (int k = j; k < g.size(); k++) {
                if (cc.equals(g.get(j).getExtended())) {
                    System.out.println(cc);
                } else {
                    addError(cc, "not exist");
                }
            }
        }

    }

    public List<TypeError> getTypeErrors() {
        return new ArrayList<>(typeErrors);
    }
}
