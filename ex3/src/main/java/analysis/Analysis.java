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
        MJClassDeclList classDeclList = prog.getClassDecls();
        List<MJExtended> extendedLinkedList = new LinkedList<>();
        for (MJClassDecl classDecl : classDeclList) {
            MJExtended classDeclExtended = classDecl.getExtended();
            boolean parentFound = false;
            if (!(classDeclExtended.toString().equals("ExtendsNothing"))) {
                extendedLinkedList.add(classDeclExtended);
                for (MJClassDecl aClassDeclList : classDeclList) {

                    if (classDeclExtended.toString().equals("ExtendsClass(" + aClassDeclList.getName() + ")")) {
                        MJExtended copyClassDeclExtended = aClassDeclList.getExtended();
                        parentFound = true;
                        for (int kk = 0; kk < extendedLinkedList.size(); kk++) {
                            if (copyClassDeclExtended.equals(extendedLinkedList.get(kk))) {
                                addError(copyClassDeclExtended, "Cycle");
                            } else {
                                extendedLinkedList.add(copyClassDeclExtended);
                            }
                        }
                    }

                }
                if (!parentFound) {
                    addError(classDeclExtended, "not exist");
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
        /*MJClassDeclList cdll = prog.getClassDecls();

        for (int j = 0; j < cdll.size(); j++) {
            MJClassDecl cdd = cdll.get(j);
            MJExtended cee = cdd.getExtended();

            //boolean find = false;



                  if (!(cee.toString().equals("ExtendsNothing"))) {
                      ex.add(cee);
                    for (int ii = 0; ii < classDeclList.size(); ii++) {

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



        }*/
    }




    public List<TypeError> getTypeErrors() {
        return new ArrayList<>(typeErrors);
    }
}
