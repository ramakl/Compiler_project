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
        MJClassDeclList cdl= prog.getClassDecls();
        for (int j = 0; j < cdl.size(); j++) {
            MJClassDecl cd = cdl.get(j);
            MJExtended ce = cd.getExtended();
            boolean find=false;
            if(!(ce.toString().equals("ExtendsNothing"))) {
                for (int ii = 0; ii < cdl.size(); ii++) {

                    if (ce.toString().equals("ExtendsClass(" + cdl.get(ii).getName() + ")")) {
                       find = true;
                    }

                }
                if (find== false) {
                    addError(ce,"not exist");
                }


            }
        }

    }

    public List<TypeError> getTypeErrors() {
        return new ArrayList<>(typeErrors);
    }
}
