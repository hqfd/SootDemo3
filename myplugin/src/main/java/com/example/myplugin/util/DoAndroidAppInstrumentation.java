package com.example.myplugin.util;


import com.example.myplugin.casper.android.instrument.Instrumenter;
import com.example.myplugin.casper.entity.CallSite;
import com.example.myplugin.casper.entity.MethodInstrumentSolution;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ALOAD;
import org.apache.bcel.generic.ASTORE;
import org.apache.bcel.generic.ATHROW;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.PUSH;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Type;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import soot.Scene;


public class DoAndroidAppInstrumentation {
    private URLClassLoader loader = null;
    private static final String LoggingClassCompressName = Instrumenter.INSTRUMENT_CLASS;
    private static final String LogCallSiteIDMethodName = "logCallSiteID";
    private static final String PushDepthMethodName = "pushDepth";
    private static final String PopDepthMethodName = "popDepth";
    private static final String PushDepthAndIDMethodName = "pushDepthAndID";
    private static final String PopDepthAndIDMethodName = "popDepthAndID";
    private static final String MethodEntryMethodName = "methodEntry";
    private static final String MethodExitMethodName = "methodExit";

    HashSet<String> typeInformations = new HashSet<String>();

    public void doInstrumentation(List<String> processDirs,
                                  HashMap<String, MethodInstrumentSolution> methodInstrumentSolutions,
                                  String outputDir) {
        try {
            String sootClassPath = Scene.v().getSootClassPath();
            String[] classPath = sootClassPath.split(File.pathSeparator, -1);

//			URL[] urls = new URL[processDirs.size() + 1 ];
            URL[] urls = new URL[processDirs.size() + 1 + classPath.length];
            int i = 0;
            for (i = 0; i < processDirs.size(); i++) {
                String processDir = processDirs.get(i);
                File file = new File(processDir);
                if (!file.exists()) {
                    System.out.println("Unable to read path" + file.getAbsolutePath());
                }
                if (file.isDirectory() && !file.getAbsolutePath().endsWith("/")) {
                    file = new File(file.getAbsolutePath() + "/");
                }
                try {
                    urls[i] = file.getCanonicalFile().toURI().toURL();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            int j = 0;
            for (; i < urls.length - 1; i++) {
                File file = new File(classPath[j]);
                if (!file.exists()) {
                    System.out.println("Unable to read path" + file.getAbsolutePath());
                }
                if (file.isDirectory() && !file.getAbsolutePath().endsWith("/")) {
                    file = new File(file.getAbsolutePath() + "/");
                }
                try {
                    urls[i] = file.getCanonicalFile().toURI().toURL();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                j++;
            }


            loader = new URLClassLoader(urls, DoInstrumentation.class.getClassLoader());

            File outputDirectory = new File(outputDir);
            for (String processDir : processDirs) {
                File file = new File(processDir);
                if (file.isDirectory()) {
                    processDir(file, outputDirectory, methodInstrumentSolutions, true);
                } else if (file.getName().endsWith(".jar")) {
                    processJar(file, outputDirectory, methodInstrumentSolutions);
                } else if (file.getName().endsWith(".zip")) {
                    processZip(file, outputDirectory, methodInstrumentSolutions);
                } else if (file.getName().endsWith(".class")) {
                    processClass(file, outputDirectory, methodInstrumentSolutions);
                } else {
                    System.out.println("unknown type for path " + file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processDir(
            File f,
            File parentOutputDir,
            HashMap<String, MethodInstrumentSolution> methodInstrumentSolutions,
            boolean isFirstLevel) {
        File thisOutputDir;
        if (isFirstLevel) {
            thisOutputDir = parentOutputDir;
        } else {
            thisOutputDir = new File(parentOutputDir.getAbsolutePath() + File.separator
                    + f.getName());
            thisOutputDir.mkdirs();
        }

        for (File fi : f.listFiles()) {
            if (fi.isDirectory()) {
                processDir(fi, thisOutputDir, methodInstrumentSolutions, false);
            } else if (fi.getName().endsWith(".class")) {
                processClass(fi, thisOutputDir, methodInstrumentSolutions);
            } else if (fi.getName().endsWith(".jar")) {
                if (!thisOutputDir.exists())
                    thisOutputDir.mkdirs();
                processJar(fi, thisOutputDir, methodInstrumentSolutions);
            } else if (fi.getName().endsWith(".zip")) {
                processZip(fi, thisOutputDir, methodInstrumentSolutions);
            }
        }
    }

    private void processJar(File f, File outputDir, HashMap<String, MethodInstrumentSolution> methodInstrumentSolutions) {
        try {
            JarFile jar = new JarFile(f);
            JarOutputStream jos = null;
            jos = new JarOutputStream(new FileOutputStream(outputDir.getPath()
                    + File.separator + f.getName()));
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                if (e.getName().endsWith(".class")) {

                    JarEntry outEntry = new JarEntry(e.getName());
                    jos.putNextEntry(outEntry);
                    byte[] clazz = instrumentClassInJarOrZip(f,
                            jar.getInputStream(e), methodInstrumentSolutions);
                    if (clazz == null) {
                        System.out.println("Failed to instrument "
                                + e.getName());
                        InputStream is = jar.getInputStream(e);
                        byte[] buffer = new byte[1024];
                        while (true) {
                            int count = is.read(buffer);
                            if (count == -1)
                                break;
                            jos.write(buffer, 0, count);
                        }
                    } else
                        jos.write(clazz);
                    jos.closeEntry();

                } else {
                    JarEntry outEntry = new JarEntry(e.getName());
                    if (e.isDirectory()) {
                        jos.putNextEntry(outEntry);
                        jos.closeEntry();
                    } else if (e.getName().startsWith("META-INF")
                            && (e.getName().endsWith(".SF") || e.getName()
                            .endsWith(".RSA"))) {
                        // don't copy this
                    } else if (e.getName().equals("META-INF/MANIFEST.MF")) {
                        Scanner s = new Scanner(jar.getInputStream(e));
                        jos.putNextEntry(outEntry);

                        String curPair = "";
                        while (s.hasNextLine()) {
                            String line = s.nextLine();
                            if (line.equals("")) {
                                curPair += "\n";
                                if (!curPair.contains("SHA1-Digest:"))
                                    jos.write(curPair.getBytes());
                                curPair = "";
                            } else {
                                curPair += line + "\n";
                            }
                        }
                        s.close();
                        jos.write("\n".getBytes());
                        jos.closeEntry();
                    } else {
                        jos.putNextEntry(outEntry);
                        InputStream is = jar.getInputStream(e);
                        byte[] buffer = new byte[1024];
                        while (true) {
                            int count = is.read(buffer);
                            if (count == -1)
                                break;
                            jos.write(buffer, 0, count);
                        }
                        jos.closeEntry();
                    }
                }
            }

            if (jos != null) {
                jos.close();
            }
            jar.close();

        } catch (Exception exception) {
            exception.printStackTrace();
            System.exit(-1);
        }
    }

    private void processZip(File f, File outputDir, HashMap<String, MethodInstrumentSolution> methodInstrumentSolutions) {
        try {

            ZipFile zip = new ZipFile(f);
            ZipOutputStream zos = null;

            zos = new ZipOutputStream(new FileOutputStream(outputDir.getPath()
                    + File.separator + f.getName()));
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();

                if (e.getName().endsWith(".class")) {
                    {
                        ZipEntry outEntry = new ZipEntry(e.getName());
                        zos.putNextEntry(outEntry);
                        byte[] clazz = instrumentClassInJarOrZip(f,
                                zip.getInputStream(e),
                                methodInstrumentSolutions);
                        if (clazz == null) {
                            InputStream is = zip.getInputStream(e);
                            byte[] buffer = new byte[1024];
                            while (true) {
                                int count = is.read(buffer);
                                if (count == -1)
                                    break;
                                zos.write(buffer, 0, count);
                            }
                        } else
                            zos.write(clazz);
                        zos.closeEntry();
                    }

                } else if (e.getName().endsWith(".jar")) {
                    ZipEntry outEntry = new ZipEntry(e.getName());
                    File tmp = new File("/tmp/classfile");
                    if (tmp.exists())
                        tmp.delete();
                    FileOutputStream fos = new FileOutputStream(tmp);
                    byte buf[] = new byte[1024];
                    int len;
                    InputStream is = zip.getInputStream(e);
                    while ((len = is.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                    }
                    is.close();
                    fos.close();
                    // System.out.println("Done reading");
                    processJar(tmp, new File("tmp2"), methodInstrumentSolutions);

                    zos.putNextEntry(outEntry);
                    is = new FileInputStream("tmp2/classfile");
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int count = is.read(buffer);
                        if (count == -1)
                            break;
                        zos.write(buffer, 0, count);
                    }
                    is.close();
                    zos.closeEntry();
                    // jos.closeEntry();
                } else {
                    ZipEntry outEntry = new ZipEntry(e.getName());
                    if (e.isDirectory()) {
                        zos.putNextEntry(outEntry);
                        zos.closeEntry();
                    } else if (e.getName().startsWith("META-INF")
                            && (e.getName().endsWith(".SF") || e.getName()
                            .endsWith(".RSA"))) {
                        // don't copy this
                    } else if (e.getName().equals("META-INF/MANIFEST.MF")) {
                        Scanner s = new Scanner(zip.getInputStream(e));
                        zos.putNextEntry(outEntry);

                        String curPair = "";
                        while (s.hasNextLine()) {
                            String line = s.nextLine();
                            if (line.equals("")) {
                                curPair += "\n";
                                if (!curPair.contains("SHA1-Digest:"))
                                    zos.write(curPair.getBytes());
                                curPair = "";
                            } else {
                                curPair += line + "\n";
                            }
                        }
                        s.close();
                        zos.write("\n".getBytes());
                        zos.closeEntry();
                    } else {
                        zos.putNextEntry(outEntry);
                        InputStream is = zip.getInputStream(e);
                        byte[] buffer = new byte[1024];
                        while (true) {
                            int count = is.read(buffer);
                            if (count == -1)
                                break;
                            zos.write(buffer, 0, count);
                        }
                        zos.closeEntry();
                    }
                }
            }
            zos.close();
            zip.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processClass(File classFile, File outputDir,
                              HashMap<String, MethodInstrumentSolution> methodInstrumentSolutions) {
        byte[] bytes = instrumentClass(classFile,
                methodInstrumentSolutions);
        if (bytes != null) {
            try {
                FileOutputStream fos = new FileOutputStream(outputDir.getPath()
                        + File.separator + classFile.getName());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bos.write(bytes);
                bos.writeTo(fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] instrumentClassInJarOrZip(File jarOrZipFile,
                                             InputStream inStream,
                                             HashMap<String, MethodInstrumentSolution> methodInstrumentSolutions) {

        try {
            JavaClass jclas = new ClassParser(inStream,
                    jarOrZipFile.getAbsolutePath()).parse();
            ClassGen cgen = new ClassGen(jclas);
            Method[] methods = jclas.getMethods();
            for (int index = 0; index < methods.length; index++) {
                Method method = methods[index];
                String bytecodeSignature = getByteCodeSignature(cgen, method);
                MethodInstrumentSolution solution = methodInstrumentSolutions
                        .get(bytecodeSignature);
                processMethod(cgen, method, solution);
            }

            // use ASM to reset Stack Map Table
            InputStream in = new ByteArrayInputStream(cgen.getJavaClass()
                    .getBytes(), 0, cgen.getJavaClass().getBytes().length);
            ClassReader cr = new ClassReader(in);
            ClassNode classNode = new ClassNode();
            cr.accept(classNode, 0);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS
                    | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(cw);
            return cw.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private byte[] instrumentClass(File classFile,
                                   HashMap<String, MethodInstrumentSolution> methodInstrumentSolutions) {
        try {
            // use BCEL to instrument
            JavaClass jclas = new ClassParser(classFile.getAbsolutePath())
                    .parse();

            ClassGen cgen = new ClassGen(jclas);
            Method[] methods = jclas.getMethods();
            for (int index = 0; index < methods.length; index++) {
                Method method = methods[index];
                String bytecodeSignature = getByteCodeSignature(cgen, method);
                MethodInstrumentSolution solution = methodInstrumentSolutions
                        .get(bytecodeSignature);
                processMethod(cgen, method, solution);
            }


            // use ASM to reset Stack Map Table
            try {
                InputStream in = new ByteArrayInputStream(cgen.getJavaClass()
                        .getBytes(), 0, cgen.getJavaClass().getBytes().length);
                ClassReader cr = new ClassReader(in);
                ClassNode classNode = new ClassNode();
                cr.accept(classNode, 0);
                ClassWriter cw = new InstrumenterClassWriter(cr, ClassWriter.COMPUTE_MAXS
                        | ClassWriter.COMPUTE_FRAMES, this.loader);
                classNode.accept(cw);
                return cw.toByteArray();

            } catch (RuntimeException exception) {
                return cgen.getJavaClass().getBytes();
            }
        } catch (Exception ex) {

            ex.printStackTrace();
        }
        return null;
    }

    private void processMethod(ClassGen cgen, Method method, MethodInstrumentSolution solution) {
        if (solution == null) {
            return;
        }
        HashMap<CallSite, InstructionHandle> callSiteMapInstruction = new HashMap<CallSite, InstructionHandle>();
        MethodGen newMethodGen = new MethodGen(method, cgen.getClassName(), cgen.getConstantPool());
        InstructionList ilist = newMethodGen.getInstructionList();
        ConstantPoolGen pgen = cgen.getConstantPool();
        InstructionFactory ifact = new InstructionFactory(cgen);
        int size = ilist.getInstructionHandles().length;
        HashMap<BranchHandle, InstructionHandle> branchingMap = new HashMap<BranchHandle, InstructionHandle>();

        HashSet<CallSite> importantCallSites = new HashSet<CallSite>();
        importantCallSites.addAll(solution.getInstrumentForLogging());
        importantCallSites.addAll(solution.getInstrumentForPushingDepth());
        importantCallSites.addAll(solution.getInstrumentForPushingDepthAndID());
        for (int i = 0; i < size; i++) {
            int offset = ilist.getInstructionPositions()[i];
            InstructionHandle instructionHandle = ilist.getInstructionHandles()[i];
            System.out.println("**********method:" + method.getName() + "***********offset:" + offset);
            for (CallSite callsite : importantCallSites) {
                int targetOffset = callsite.getByteCodeOffset();
                if (offset == targetOffset) {
                    callSiteMapInstruction.put(callsite, instructionHandle);
                }
            }

            if (instructionHandle instanceof BranchHandle) {
                BranchHandle branchHandle = ((BranchHandle) instructionHandle);
                InstructionHandle targetHandler = branchHandle.getTarget();
                branchingMap.put(branchHandle, targetHandler);
            }
        }

        int originalSizeExceptionTable = 0;
        if (newMethodGen.getExceptionHandlers() != null) {
            originalSizeExceptionTable = newMethodGen.getExceptionHandlers().length;
        }
        int exceptionOfIndexInLocalTable = newMethodGen.getMaxLocals();

        HashSet<InstructionHandle> insHasbeenSetTarget = new HashSet<InstructionHandle>();

        for (Entry<CallSite, InstructionHandle> insEntry : callSiteMapInstruction.entrySet()) {
            CallSite callSite = insEntry.getKey();

            InstructionHandle insHandle = insEntry.getValue();
            HashSet<BranchHandle> sources = sourceOfBranch(insHandle, branchingMap);
            boolean isSetTarget = false;
            if (solution.getInstrumentForLogging().contains(callSite)) {
                //single dispatched object
                System.out.println("************* instrument call site ***************");
                InstructionList insertList = new InstructionList();
                PUSH pushInstruction = new PUSH(pgen, callSite.getCallSiteID());
                insertList.append(pushInstruction);
                Type[] paramTypes = new Type[1];
                paramTypes[0] = Type.INT;

                Instruction staticInvokeInstruction = ifact.createInvoke(
                        LoggingClassCompressName,
                        LogCallSiteIDMethodName, Type.VOID, paramTypes,
                        Constants.INVOKESTATIC);
                insertList.append(staticInvokeInstruction);
                ilist.insert(insHandle, insertList);

                for (BranchHandle branch : sources) {
                    isSetTarget = true;
                    branch.setTarget(insHandle.getPrev().getPrev());
                    insHasbeenSetTarget.add(insHandle);
                }
            }

            if (solution.getInstrumentForPushingDepth().contains(callSite)) {
                InstructionList insertList = new InstructionList();
                Type[] paramTypes = new Type[0];
                Instruction staticInvokeInstruction = ifact.createInvoke(
                        LoggingClassCompressName,
                        PushDepthMethodName, Type.VOID, paramTypes,
                        Constants.INVOKESTATIC);
                insertList.append(staticInvokeInstruction);
                ilist.insert(insHandle, insertList);

                // after insHandle
                InstructionList insertList2 = new InstructionList();
                Type[] paramTypes2 = new Type[0];
                Instruction staticInvokeInstruction2 = ifact.createInvoke(
                        LoggingClassCompressName,
                        PopDepthMethodName, Type.VOID, paramTypes2,
                        Constants.INVOKESTATIC);
                insertList2.append(staticInvokeInstruction2);
                ilist.append(insHandle, insertList2);

                // GOTO
                GOTO gotoInstruction = new GOTO(insHandle.getNext());
                ilist.append(insHandle, gotoInstruction);


                // exception handling instructions
                InstructionList insertList3 = new InstructionList();
                ASTORE astore = new ASTORE(exceptionOfIndexInLocalTable);
                insertList3.append(astore);
                Type[] paramTypes3 = new Type[0];
                Instruction staticInvokeInstruction3 = ifact.createInvoke(
                        LoggingClassCompressName,
                        PopDepthMethodName, Type.VOID, paramTypes3,
                        Constants.INVOKESTATIC);
                insertList3.append(staticInvokeInstruction3);
                ALOAD aload = new ALOAD(exceptionOfIndexInLocalTable);
                insertList3.append(aload);
                insertList3.append(new ATHROW());

                ilist.append(insHandle.getNext(), insertList3);

                // exception handling
                newMethodGen.addExceptionHandler(insHandle.getPrev(),
                        insHandle.getNext(), insHandle.getNext().getNext(), null);

                //handle the jump
                if (!isSetTarget) {
                    for (BranchHandle branchHandle : sources) {
                        isSetTarget = true;
                        branchHandle.setTarget(insHandle.getPrev());
                    }
                }
            }

            if (solution.getInstrumentForPushingDepthAndID().contains(callSite)) {
                InstructionList insertList = new InstructionList();
                Type[] paramTypes = new Type[1];
                paramTypes[0] = Type.INT;
                PUSH pushInstruction = new PUSH(pgen, callSite.getCallSiteID());
                insertList.append(pushInstruction);
                Instruction staticInvokeInstruction = ifact.createInvoke(
                        LoggingClassCompressName,
                        PushDepthAndIDMethodName, Type.VOID, paramTypes,
                        Constants.INVOKESTATIC);
                insertList.append(staticInvokeInstruction);
                ilist.insert(insHandle, insertList);

                // after insHandle
                InstructionList insertList2 = new InstructionList();
                Type[] paramTypes2 = new Type[0];
                Instruction staticInvokeInstruction2 = ifact.createInvoke(
                        LoggingClassCompressName,
                        PopDepthAndIDMethodName, Type.VOID, paramTypes2,
                        Constants.INVOKESTATIC);
                insertList2.append(staticInvokeInstruction2);
                ilist.append(insHandle, insertList2);

                // GOTO
                GOTO gotoInstruction = new GOTO(insHandle.getNext());
                ilist.append(insHandle, gotoInstruction);


                // exception handling instructions
                InstructionList insertList3 = new InstructionList();
                ASTORE astore = new ASTORE(exceptionOfIndexInLocalTable);
                insertList3.append(astore);
                Type[] paramTypes3 = new Type[0];
                Instruction staticInvokeInstruction3 = ifact.createInvoke(
                        LoggingClassCompressName,
                        PopDepthAndIDMethodName, Type.VOID, paramTypes3,
                        Constants.INVOKESTATIC);
                insertList3.append(staticInvokeInstruction3);
                ALOAD aload = new ALOAD(exceptionOfIndexInLocalTable);
                insertList3.append(aload);
                insertList3.append(new ATHROW());

                ilist.append(insHandle.getNext(), insertList3);

                // exception handling
                newMethodGen.addExceptionHandler(insHandle.getPrev().getPrev(),
                        insHandle.getNext(), insHandle.getNext().getNext(), null);

                //handle the jump
                if (!isSetTarget) {
                    for (BranchHandle branchHandle : sources) {
                        branchHandle.setTarget(insHandle.getPrev().getPrev());
                    }
                }
            }
        }


        if (originalSizeExceptionTable != 0) {
            CodeExceptionGen[] arrayCodeExceptionGens = newMethodGen
                    .getExceptionHandlers();
            ArrayList<CodeExceptionGen> rearrangeCodeExceptions = new ArrayList<CodeExceptionGen>();
            for (int i = originalSizeExceptionTable; i < arrayCodeExceptionGens.length; i++) {
                rearrangeCodeExceptions.add(arrayCodeExceptionGens[i]);
            }
            for (int i = 0; i < originalSizeExceptionTable; i++) {
                rearrangeCodeExceptions.add(arrayCodeExceptionGens[i]);
            }

            newMethodGen.removeExceptionHandlers();
            for (CodeExceptionGen codeExceptionGen : rearrangeCodeExceptions) {
                newMethodGen.addExceptionHandler(codeExceptionGen.getStartPC(),
                        codeExceptionGen.getEndPC(),
                        codeExceptionGen.getHandlerPC(),
                        codeExceptionGen.getCatchType());
            }
        }


        //inject methodEntry
        {
            System.out.println("************* instrument methodEntry ***************");
            InstructionHandle firstInst = ilist.getInstructionHandles()[0];
            InstructionList insertList = new InstructionList();

            PUSH pushInstruction = new PUSH(pgen, solution.getCallSiteCFG().getEntryCallSite().getCallSiteID());
            insertList.append(pushInstruction);
            Type[] paramTypes = new Type[1];
            paramTypes[0] = Type.INT;

            Instruction staticInvokeInstruction = ifact.createInvoke(
                    LoggingClassCompressName,
                    MethodEntryMethodName, Type.VOID, paramTypes,
                    Constants.INVOKESTATIC);
            insertList.append(staticInvokeInstruction);
            ilist.insert(firstInst, insertList);
        }


//        //inject IF
//        {
//            System.out.println("************* instrument IF ***************");
//            for (InstructionHandle insHandler : ilist.getInstructionHandles()) {
//                if (insHandler.getInstruction() instanceof GotoInstruction) {
//
//                    InstructionList insertList = new InstructionList();
//                    Type[] paramTypes = new Type[0];
//                    Instruction staticInvokeInstruction = ifact.createInvoke(
//                            LoggingClassCompressName,
//                            MethodExitMethodName, Type.VOID, paramTypes,
//                            Constants.INVOKESTATIC);
//                    insertList.append(staticInvokeInstruction);
//                    ilist.insert(insHandler, insertList);
//
//                    if (!insHasbeenSetTarget.contains(insHandler)) {
//                        for (BranchHandle branch : sourceOfBranch(insHandler, branchingMap)) {
//                            branch.setTarget(insHandler.getPrev());
//                        }
//                    }
//
//                }
//            }
//        }



        //inject methodExit
        {
            System.out.println("************* instrument methodExit ***************");
            for (InstructionHandle insHandler : ilist.getInstructionHandles()) {
                if (insHandler.getInstruction() instanceof ReturnInstruction
                        ) {

                    InstructionList insertList = new InstructionList();
                    Type[] paramTypes = new Type[0];
                    Instruction staticInvokeInstruction = ifact.createInvoke(
                            LoggingClassCompressName,
                            MethodExitMethodName, Type.VOID, paramTypes,
                            Constants.INVOKESTATIC);
                    insertList.append(staticInvokeInstruction);
                    ilist.insert(insHandler, insertList);

                    if (!insHasbeenSetTarget.contains(insHandler)) {
                        for (BranchHandle branch : sourceOfBranch(insHandler, branchingMap)) {
                            branch.setTarget(insHandler.getPrev());
                        }
                    }

//					InstructionList insertList = new InstructionList();
//					Type[] paramTypes = new Type[1];
//					paramTypes[0] = Type.INT;
//					
//					Instruction staticInvokeInstruction = ifact.createInvoke(
//							LoggingClassCompressName,
//							MethodExitMethodName, Type.VOID, paramTypes,
//							Constants.INVOKESTATIC);				
//					PUSH pushInstruction = new PUSH(pgen,solution.getCallSiteCFG().getExitCallSite().getCallSiteID());
//					insertList.append(pushInstruction);
//					insertList.append(staticInvokeInstruction);				
//					ilist.insert(insHandler, insertList);
//					
//					if(!insHasbeenSetTarget.contains(insHandler)){
//						for(BranchHandle branch:sourceOfBranch(insHandler, branchingMap)){
//							branch.setTarget(insHandler.getPrev().getPrev());
//						}
//					}
                }
            }
        }

        //for the case that the method throws exception before return
        {
            int length = ilist.getInstructionHandles().length;
            InstructionHandle lastIns = ilist.getInstructionHandles()[length - 1];
            //single dispatched object
//			InstructionList insertList = new InstructionList();
//			Type[] paramTypes = new Type[0];					
//			Instruction staticInvokeInstruction = ifact.createInvoke(
//					LoggingClassCompressName,
//					MethodExitMethodName, Type.VOID, paramTypes,
//					Constants.INVOKESTATIC);				
//			insertList.append(staticInvokeInstruction);
//			ilist.append(lastIns,insertList);

            // exception handling instructions
            InstructionList insertList3 = new InstructionList();
            ASTORE astore = new ASTORE(exceptionOfIndexInLocalTable);
            insertList3.append(astore);
//			Type[] paramTypes3 = new Type[0];
            Type[] paramTypes3 = new Type[1];
            paramTypes3[0] = Type.INT;

            Instruction staticInvokeInstruction3 = ifact.createInvoke(
                    LoggingClassCompressName,
                    MethodExitMethodName, Type.VOID, paramTypes3,
                    Constants.INVOKESTATIC);
            PUSH pushInstruction = new PUSH(pgen, solution.getCallSiteCFG().getExitCallSite().getCallSiteID());
            insertList3.append(pushInstruction);
            insertList3.append(staticInvokeInstruction3);
            ALOAD aload = new ALOAD(exceptionOfIndexInLocalTable);
            insertList3.append(aload);
            insertList3.append(new ATHROW());

//			ilist.append(lastIns.getNext(), insertList3);
            ilist.append(lastIns, insertList3);

            // exception handling
//			newMethodGen.addExceptionHandler(ilist.getInstructionHandles()[0],
//					lastIns.getNext(), lastIns.getNext().getNext(), null);
//			newMethodGen.addExceptionHandler(ilist.getInstructionHandles()[2],
//					lastIns, lastIns.getNext(), new ObjectType("java.lang.Exception"));

            newMethodGen.addExceptionHandler(ilist.getInstructionHandles()[2],
                    lastIns, lastIns.getNext(), null);
        }

        newMethodGen.stripAttributes(false);
        newMethodGen.setMaxStack();
        newMethodGen.setMaxLocals();
        newMethodGen.update();
        cgen.removeMethod(method);
        cgen.addMethod(newMethodGen.getMethod());
        cgen.setConstantPool(new ConstantPoolGen(pgen.getFinalConstantPool()));
        ilist.dispose();

    }

    private HashSet<BranchHandle> sourceOfBranch(
            InstructionHandle instructionHandle,
            HashMap<BranchHandle, InstructionHandle> branchingMap) {
        HashSet<BranchHandle> sourceInstructionHandlers = new HashSet<BranchHandle>();
        for (BranchHandle source : branchingMap.keySet()) {
            if (branchingMap.get(source) == instructionHandle) {
                sourceInstructionHandlers.add(source);
            }
        }
        return sourceInstructionHandlers;
    }


    private String getByteCodeSignature(ClassGen cgen, Method method) {
        StringBuffer buf = new StringBuffer();
        buf.append("<");
        buf.append(cgen.getClassName());
        buf.append(": ");
        buf.append(method.getName());
        buf.append("(");
        for (Type argType : method.getArgumentTypes()) {
            buf.append(argType.getSignature());
        }
        buf.append(")");
        buf.append(method.getReturnType().getSignature());
        buf.append(">");
        String byteCodeSignature = buf.toString();
        return byteCodeSignature;
    }

}
