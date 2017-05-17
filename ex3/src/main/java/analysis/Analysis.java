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
            if(!(cc.toString().equals("ExtendsNothing"))) {
                for (int ii = 0; ii <= g.size(); ii++) {

                    if (cc.equals(g.get(ii).getName())) {
                    } else {
                        addError(cc, "not exisit");
                    }
                }
            }
        }

    }

    public List<TypeError> getTypeErrors() {
        return new ArrayList<>(typeErrors);
    }
}
