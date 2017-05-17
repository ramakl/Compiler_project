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
        MJClassDeclList cdl = prog.getClassDecls();
        for (int j = 0; j < cdl.size(); j++) {
            MJClassDecl cd = cdl.get(j);
            MJExtended ce = cd.getExtended();
            boolean find = false;
            if (!(ce.toString().equals("ExtendsNothing"))) {
                for (int ii = 0; ii < cdl.size(); ii++) {

                    if (ce.toString().equals("ExtendsClass(" + cdl.get(ii).getName() + ")")) {
                        find = true;
                    }

                }
                if (find == false) {
                    addError(ce, "not exist");
                }


            }
        }
        //check for cycle
      /*  List<MJExtended> ex = new LinkedList<>();
        for (int j = 0; j < cdl.size(); j++) {
            MJClassDecl cd = cdl.get(j);
            MJExtended ce = cd.getExtended();
            if (!(ce.toString().equals("ExtendsNothing"))) {
                ex.add(ce);
                int k = 0;
                do {
                       for (int h=0;h<ex.size();h++)
                       {
                           cx.
                       }

                    if (ce.equals(ex.get(k))) {
                        addError(ce, "there is a cycle");
                    } else ex.add(ce);
                    k++;
                } while (k < ex.size());

            }
        }*/
        MJClassDeclList cdll = prog.getClassDecls();
        List<MJExtended> ex = new LinkedList<>();
        for (int j = 0; j < cdll.size(); j++) {
            MJClassDecl cdd = cdll.get(j);
            MJExtended cee = cdd.getExtended();

            //boolean find = false;



                  if (!(cee.toString().equals("ExtendsNothing"))) {
                      ex.add(cee);
                    for (int ii = 0; ii < cdl.size(); ii++) {

                        if (cee.toString().equals("ExtendsClass(" + cdll.get(ii).getName() + ")")) {
                            //find = true;
                            cee = cdll.get(ii).getExtended();
                            for (int kk = 0; kk < ex.size(); kk++)
                                if (cee.equals(ex.get(kk)))
                                    addError(cee, "cycle");
                                else
                                    ex.add(cee);

                        }





                    }
                   // if (find == false) {
                     //   addError(ce, "not exist");
                    //}
                }



        }
    }




    public List<TypeError> getTypeErrors() {
        return new ArrayList<>(typeErrors);
    }
}
