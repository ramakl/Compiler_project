package analysis;

import frontend.SourcePosition;
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

    public void check() throws Exception {
        //TODO implement type checking here!
        ArrayList<MJMethodDecl> parentMethodList = new ArrayList<MJMethodDecl>();
        ArrayList<MJMethodDecl> childMethodList = new ArrayList<MJMethodDecl>();
        Hashtable<String, ArrayList<MJMethodDecl>> methodInfo = new Hashtable<>();
        MJClassDeclList classDeclList = prog.getClassDecls();
        Hashtable<String, String> classInfo = new Hashtable<>(); //key - className; value - Extends
        /* for example class A extends B then key : A, value : B; (A->B) */
        for (MJClassDecl classDecl : classDeclList) {
            String className = classDecl.getName();
            String extendsClass = classDecl.getExtended().toString();
            if (!extendsClass.equalsIgnoreCase("ExtendsNothing"))
                extendsClass = extendsClass.substring(13, extendsClass.length() - 1);
            classInfo.put(className, extendsClass);
        }

        Set<String> classNames = classInfo.keySet();

        for(String className : classNames)
        {
            String extendsClass = classInfo.get(className);
            if(!extendsClass.equalsIgnoreCase("extendsnothing")) {
                for (MJClassDecl classDecl : classDeclList) {
                    if (classDecl.getName().equalsIgnoreCase(className)) {
                        childMethodList.addAll(classDecl.getMethods());
                        methodInfo.put(className, childMethodList);
                    }

                }
            }
            else
            {
                for(MJClassDecl classDecl : classDeclList)
                {
                    if(classDecl.getName().equalsIgnoreCase(className)) {
                        parentMethodList.addAll(classDecl.getMethods());
                        methodInfo.put(className, parentMethodList);
                    }

                }
            }
        }


        ArrayList<MJMethodDecl> parentMethods = new ArrayList<MJMethodDecl>();
        ArrayList<MJMethodDecl> childMethods = new ArrayList<MJMethodDecl>();
        ArrayList<MJVarDecl> parentParameters = new ArrayList<MJVarDecl>();
        ArrayList<MJVarDecl> childParameters = new ArrayList<MJVarDecl>();
        for(String className : classNames)
        {
            String parentClass = classInfo.get(className);
            if(!parentClass.equalsIgnoreCase("extendsnothing"))
            {
                    boolean signatureFound = false;
                   for(MJMethodDecl methodDecl : methodInfo.get(parentClass))
                       parentMethods.add(methodDecl);
                   for(MJMethodDecl methodDecl : methodInfo.get(className))
                       childMethods.add(methodDecl);
                   for(MJMethodDecl methodDecl:childMethods)
                   {
                        MJType parentReturnType = null;
                       String methodName = methodDecl.getName();
                       for(MJMethodDecl m: parentMethods)
                       {
                           if(m.getName().equalsIgnoreCase(methodName)){
                               signatureFound = true;
                               for(MJVarDecl varDecl : m.getFormalParameters()) {
                                   parentParameters.add(varDecl);
                               }
                               parentReturnType = m.getReturnType();
                               break;
                           }

                       }
                       boolean orderMaintained = false;
                       boolean returnTypeMaintained = false;
                       if(signatureFound)
                       {
                           MJType childReturnType = methodDecl.getReturnType();
                           for(MJVarDecl varDecl : methodDecl.getFormalParameters())
                           {
                               childParameters.add(varDecl);
                           }
                           if(parentParameters.size() == childParameters.size())
                           {
                               for(int i=0;i<parentParameters.size();i++)
                               {
                                   MJType parentType = parentParameters.get(i).getType();
                                   MJType childType = childParameters.get(i).getType();
                                   if(parentType.structuralEquals(childType))
                                       orderMaintained = true;
                               }
                               if(parentReturnType.structuralEquals(childReturnType)) {
                                   returnTypeMaintained = true;
                               }
                           }
                            if(!orderMaintained || !returnTypeMaintained)
                            {
                                addError(methodDecl,"override Error : signature Do not match");
                            }
                       }

                   }
            }

        }



        for (int i = 0; i < classDeclList.size(); i++) {
            for (int j = i + 1; j < classDeclList.size(); j++) {
                if (classDeclList.get(i).getName().equals(classDeclList.get(j).getName())) {
                    addError(classDeclList.get(i), "duplicateClassName");
                    break;
                }
            }
        }


        for (int i = 0; i < classDeclList.size(); i++) {
            MJMethodDeclList m = classDeclList.get(i).getMethods();
            for (int ii = 0; ii < m.size(); ii++) {
                for (int j = ii + 1; j < m.size(); j++) {
                    if (m.get(ii).getName().equals(m.get(j).getName())) {
                        addError(classDeclList.get(i), "duplicateMethodName");
                        break;
                    }

                }
            }
        }


        for (int i = 0; i < classDeclList.size(); i++) {
            MJVarDeclList v = classDeclList.get(i).getFields();
            for (int ii = 0; ii < v.size(); ii++) {
                for (int j = ii + 1; j < v.size(); j++) {
                    if (v.get(ii).getName().equals(v.get(j).getName())) {
                        addError(classDeclList.get(i), "duplicateFieldName");
                        break;
                    }

                }
            }
        }


        String mainClass = this.prog.getMainClass().getName();
        for (String className : classNames) {
            String extendsClass = classInfo.get(className);
            if (!extendsClass.equalsIgnoreCase("extendsNothing")) {
                //for example Class A extends A : add to the list of errors
                //for test case inheritanceCycle1() : ClassChecks.java
                if (className.equalsIgnoreCase(extendsClass)) {
                    addError(classDeclList, "Self extension found");
                }
                //for example test case see extendsMainClass() : ClassChecks.java
                if (extendsClass.equalsIgnoreCase(mainClass)) {
                    addError(classDeclList, "Main class cannot be extended");
                }

            }
        }

/*

        argName() : ClassChecks.java

*/
            String mainArgs = this.prog.getMainClass().getArgsName();
            MJBlock mainBlock = this.prog.getMainClass().getMainBody();
            for (MJStatement statement : mainBlock) {

                if (statement instanceof MJVarDecl) {
                    MJVarDecl varDecl = (MJVarDecl) statement;
                    if (varDecl.getName().equalsIgnoreCase(mainArgs)) {
                        addError(varDecl, "variable with same name as main signature found");
                    }
                } else if (statement instanceof MJVarDeclList) {
                    MJVarDeclList varDeclList = (MJVarDeclList) statement;
                    for (MJVarDecl varDecl : varDeclList) {
                        if (varDecl.getName().equalsIgnoreCase(mainArgs)) {
                            addError(varDecl, "variable with same name as main signature found");
                        }
                    }
                }

            }




/*

        Handling the duplicateParamName() : ClassChecks.java
*/

            for (MJClassDecl classDecl : classDeclList) {
                MJMethodDeclList methodDeclList = classDecl.getMethods();
                for (MJMethodDecl methodDecl : methodDeclList) {
                    MJVarDeclList varDeclList = methodDecl.getFormalParameters();
                    Hashtable<String, MJType> paramTable = new Hashtable<>();
                    for (MJVarDecl varDecl : varDeclList) {
                        String variable = varDecl.getName();
                        MJType paramType = varDecl.getType();
                        if (!paramTable.containsKey(variable)) {
                            paramTable.put(variable, paramType);
                        } else {
                            addError(varDecl, "Two parameters with same name but different types found");
                        }
                    }

                }
            }


            for (MJClassDecl classDecl : classDeclList) {
                List<MJExtended> extendedLinkedList = new LinkedList<>();
                MJExtended classDeclExtended = classDecl.getExtended();

                if (!(classDeclExtended.toString().equals("ExtendsNothing"))) {
                    boolean parentFound = false;
                    extendedLinkedList.add(classDeclExtended);
                    for (MJClassDecl aClassDeclList : classDeclList) {

                        if (classDeclExtended.toString().equals("ExtendsClass(" + aClassDeclList.getName() + ")")) {
                            MJExtended copyClassDeclExtended = aClassDeclList.getExtended();
                             parentFound = true;
                            boolean foundINCycle = false;
                            for (int kk = 0; kk < extendedLinkedList.size(); kk++) {
                                if (copyClassDeclExtended.equals(extendedLinkedList.get(kk))) {
                                    foundINCycle = true;
                                    addError(copyClassDeclExtended, "Cycle");
                                    break;

                                    //@Madhu May 17 10:55 AM
                                    // do we need to collect all errors? If not, then we can use break here.

                                }
                            }
                            if (!(copyClassDeclExtended.toString().equals("ExtendsNothing")) && !foundINCycle) {
                                extendedLinkedList.add(copyClassDeclExtended);
                                classDeclExtended = copyClassDeclExtended;

                            }


                        }

                    }
                    if (!parentFound) {
                        addError(classDeclExtended, "not exist");
                    }
                }


            }

        }





    public List<TypeError> getTypeErrors () {
            return new ArrayList<>(typeErrors);
        }
    }