package com.example.myplugin.casper.android.instrument;


import com.example.myplugin.casper.entity.CallSite;
import com.example.myplugin.casper.entity.CallSiteCFG;
import com.example.myplugin.casper.entity.MethodInstrumentSolution;
import com.example.myplugin.util.InstrumentationStatistics;

import java.util.ArrayList;
import java.util.Map;

import soot.SootMethod;


public class FullInstrumentation extends Instrumenter {

    public static final String PHASE_NAME = "wjtp.instrument";


    @SuppressWarnings("rawtypes")
    protected void internalTransform(String phaseName, Map options) {
        super.internalTransform(phaseName, options);
        instrument();
    }

    private void instrument() {

        // solve the instrumentation problem
        ArrayList<SootMethod> methodsInReverseToplogicalOrder = appCallGraph
                .getMethodsInReverseTopologicalOrder();
        for (SootMethod method : methodsInReverseToplogicalOrder) {
            instrumentMethod(method);
        }

//		TestMethodInstrumentationSolution tester = new TestMethodInstrumentationSolution();
//		tester.testMethodInstrumentation(methodInstrumentSolutions,
//				callSiteCFGMap);
        InstrumentationStatistics.doStatistics(methodInstrumentSolutions,
                callSiteCFGMap);
    }

    private void instrumentMethod(SootMethod method) {
        // initialize solution of method
        MethodInstrumentSolution solution = getMethodInstrumentationSolution(method
                .getBytecodeSignature());

        CallSiteCFG callSiteCFG = callSiteCFGMap.get(method
                .getBytecodeSignature());

        solution.setMethod(method.getBytecodeSignature());
        solution.setCallSiteCFG(callSiteCFG);

        if (callSiteCFG == null) {
            return;
        }


        for (CallSite callSite : callSiteCFG.getCallSites()) {
            if (!callSite.isEntry() && !callSite.isExit()) {
                addInstrumentationForLogging(method.getBytecodeSignature(), callSite);
//				if(callSite.isContainLibraryCall() || callSite.isVirtual()){
//					addInstrumentationForPushingDepth(method.getBytecodeSignature(), callSite);
//				}
            }
        }
        solution.setCompleted(true);
    }

    private MethodInstrumentSolution getMethodInstrumentationSolution(String method) {
        if (methodInstrumentSolutions.get(method) == null) {
            methodInstrumentSolutions.put(method,
                    new MethodInstrumentSolution());
            methodInstrumentSolutions.get(method).setCallSiteCFG(
                    callSiteCFGMap.get(method));
        }
        return methodInstrumentSolutions.get(method);
    }

    private void addInstrumentationForLogging(String method, CallSite callSite) {
        MethodInstrumentSolution solution = getMethodInstrumentationSolution(method);
        solution.getInstrumentForLogging().add(callSite);
    }

}
