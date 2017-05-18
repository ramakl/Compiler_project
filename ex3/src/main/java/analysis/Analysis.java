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

        for (MJClassDecl classDecl : classDeclList) {
            List<MJExtended> extendedLinkedList = new LinkedList<>();
            MJExtended classDeclExtended = classDecl.getExtended();
            boolean parentFound = false;
            if (!(classDeclExtended.toString().equals("ExtendsNothing"))) {
                extendedLinkedList.add(classDeclExtended);
                for (MJClassDecl aClassDeclList : classDeclList) {

                    if (classDeclExtended.toString().equals("ExtendsClass(" + aClassDeclList.getName() + ")")) {
                        MJExtended copyClassDeclExtended = aClassDeclList.getExtended();
                        parentFound = true;
                        boolean found2 = false;
                        for (int kk = 0; kk < extendedLinkedList.size(); kk++) {
                            if (copyClassDeclExtended.equals(extendedLinkedList.get(kk))) {
                                found2 = true;
                                addError(copyClassDeclExtended, "Cycle");
                            }
                        }
                            if (!(copyClassDeclExtended.toString().equals("ExtendsNothing"))) {
                                extendedLinkedList.add(copyClassDeclExtended);
                                classDeclExtended = copyClassDeclExtended;

                            }



                   //     if (!(copyClassDeclExtended.toString().equals("ExtendsNothing")))
                          //  extendedLinkedList.add(copyClassDeclExtended);
                    }
                }


                if (!parentFound) {
                    addError(classDeclExtended, "not exist");
                }

            }
            }




       // MJClassDeclList cdll = prog.getClassDecls();
      //  List<MJExtended> ex = new LinkedList<>();

          //  }
      //  }*/
        /*MJClassDeclList cdll = prog.getClassDecls();

>>>>>>> master
        for (int j = 0; j < cdll.size(); j++) {
            MJClassDecl cdd = cdll.get(j);
            MJExtended cee = cdd.getExtended();





                  if (!(cee.toString().equals("ExtendsNothing"))) {
                      ex.add(cee);
                    for (int ii = 0; ii < classDeclList.size(); ii++) {

                        if (cee.toString().equals("ExtendsClass(" + cdll.get(ii).getName() + ")")) {

                            cee = cdll.get(ii).getExtended();
                            for (int kk = 0; kk < ex.size(); kk++)
                                if (cee.equals(ex.get(kk)))
                                    addError(cee, "cycle");
                                else
                                    ex.add(cee);

                        }





                    }

                }



        }*/
    }




    public List<TypeError> getTypeErrors() {
        return new ArrayList<>(typeErrors);
    }
}
