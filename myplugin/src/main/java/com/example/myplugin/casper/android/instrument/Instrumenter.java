package com.example.myplugin.casper.android.instrument;



import com.example.myplugin.casper.entity.CallGraph;
import com.example.myplugin.casper.entity.CallSiteCFG;
import com.example.myplugin.casper.entity.MethodInstrumentSolution;
import com.example.myplugin.util.ConstructCallGraph;
import com.example.myplugin.util.ConstructCallSiteCFG;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.options.Options;

public abstract class Instrumenter extends SceneTransformer {
    public HashMap<String, MethodInstrumentSolution> methodInstrumentSolutions =
            new HashMap<String, MethodInstrumentSolution>();
    public HashMap<String, CallSiteCFG> callSiteCFGMap = new HashMap<String, CallSiteCFG>();


    public static final String INSTRUMENT_CLASS = "com.example.myplugin.casper.android" +
            ".instrument.util.LoggingClassCompress";
    public static final String[] DUMPING_CLASSES = {
            "com.example.myplugin.casper.android.instrument.util.Constant",
            "com.example.myplugin.casper.android.instrument.util.LoggingClassCompress",
            "com.example.myplugin.casper.android.instrument.util.LoggingThreadCompress",
            "com.example.myplugin.casper.android.instrument.util.Stack",
            "com.example.myplugin.casper.android.instrument.util.ThreadBuffer",
            "com.example.myplugin.casper.android.instrument.util.UnCaughtExceptionHandler",
            "com.example.myplugin.casper.android.instrument.util.Integer"
//			,"casper.android.instrument.util.Hooker"
    };

    protected SootClass logClass;
    protected CallGraph appCallGraph = null;

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        // initialize the instrument class and methods
        if (logClass == null) {
            for (String dumpingClassName : DUMPING_CLASSES) {
                SootClass dumpClass = Scene.v().loadClassAndSupport(
                        dumpingClassName);
                if (dumpingClassName.equals(INSTRUMENT_CLASS)) {
                    logClass = dumpClass;
                }
                try {
                    String fileName = SourceLocator.v().getFileNameFor(
                            dumpClass, Options.output_format_class);
                    (new File(fileName)).getParentFile().mkdirs();

                    ClassLoader loader = Thread.currentThread()
                            .getContextClassLoader();
                    InputStream in = loader
                            .getResourceAsStream(dumpingClassName.replace(".",
                                    "/") + ".class");
                    FileOutputStream outputStream = new FileOutputStream(
                            new File(fileName));
                    byte[] bytes = new byte[1024];
                    int length = in.read(bytes);
                    while (length > 0) {
                        outputStream.write(bytes, 0, length);
                        length = in.read(bytes);
                    }
                    outputStream.close();
                    in.close();
                } catch (Exception e) {
                }
            }
        }


        // construct call graph of application methods
        ConstructCallGraph callGraphConstructor = new ConstructCallGraph();
        appCallGraph = callGraphConstructor.constructAppCallGraph();
        // construct call site control flow graph
        ConstructCallSiteCFG constructCallSiteCFG = new ConstructCallSiteCFG();
        for (SootClass appClass : Scene.v().getApplicationClasses()) {
            /**
             * 过滤资源类文件
             */
            String fileName = appClass.getName();
            if (fileName.endsWith("R.class")
                    || fileName.endsWith("BuildConfig.class")
                    || fileName.contains("R$")
                    || fileName.contains(".BuildConfig")
                    || fileName.endsWith(".R"))
                continue;
            for (SootMethod method : appClass.getMethods()) {
                if (method.hasActiveBody()) {
                    CallSiteCFG callSiteCFG = constructCallSiteCFG
                            .constructCallSiteCFG(method, appCallGraph);
                    callSiteCFGMap.put(method.getBytecodeSignature(),
                            callSiteCFG);
                    if (callSiteCFG == null) {
                        continue;
                    }
//							System.out.println(callSiteCFG);
                }
            }
        }
    }
}
