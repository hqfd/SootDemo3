package com.example.myplugin.util;


import com.example.myplugin.casper.entity.CallGraph;
import com.example.myplugin.casper.entity.CallSite;
import com.example.myplugin.casper.entity.Edge;
import com.example.myplugin.casper.entity.Method;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import soot.Type;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.tagkit.BytecodeOffsetTag;
import soot.tagkit.LineNumberTag;
import soot.tagkit.SourceFileTag;
import soot.tagkit.Tag;


public class ConstructCallSite {
	private static HashMap<Stmt, CallSite> callSiteMap = new HashMap<Stmt, CallSite>();
	
	public static CallSite getEntryCallSite(String method){
		CallSite callSite = new CallSite();
		int callSiteID = callSiteMap.keySet().size() + 1;
		callSite.setCallSiteID(callSiteID);
		callSite.setByteCodeOffset(0);
		callSite.setEntry(true);
		callSite.setMethod(method);
		Stmt statement = Jimple.v().newNopStmt();
		callSiteMap.put(statement, callSite);
		return callSite;
	}
	public static CallSite getExitCallSite(String method){
		CallSite callSite = new CallSite();
		int callSiteID = callSiteMap.keySet().size() + 1;
		callSite.setCallSiteID(callSiteID);
		callSite.setByteCodeOffset(-1);
		callSite.setExit(true);
		callSite.setMethod(method);
		Stmt statement = Jimple.v().newNopStmt();
		callSiteMap.put(statement, callSite);
		return callSite;
	}
	
	
	@SuppressWarnings("rawtypes")
	public static CallSite getCallSite(Stmt stmt, CallGraph callGraph, String method) {
		if (callSiteMap.get(stmt) == null) {
			CallSite callSite = new CallSite();
			int callSiteID = callSiteMap.keySet().size() + 1;
			callSite.setCallSiteID(callSiteID);
			callSite.setStmt(stmt);
			callSite.setMethod(method);
			if(stmt.containsInvokeExpr()){
				String stmtStr = stmt.toString();
				if(stmtStr.contains("<")&&stmtStr.contains(">")){
					int start = stmtStr.indexOf("<");
					int end = stmtStr.lastIndexOf(">");
					stmtStr = stmtStr.substring(start+1, end);
				}
				callSite.setStmtString(stmtStr);				
			}else{
				callSite.setStmtString(stmt.toString());
			}
			for (Iterator j = stmt.getTags().iterator(); j.hasNext();) {
				Tag tag = (Tag) j.next();
				if (tag instanceof LineNumberTag) {
					System.out.println("*************Method:"+method
							+"***********lineNumber:"+tag);
					byte[] value = tag.getValue();
					int lineNumber = ((value[0] & 0xff) << 8)
							| (value[1] & 0xff);
					callSite.setLineNum(lineNumber);
				} else if (tag instanceof SourceFileTag) {
					System.out.println("*************Method:"+method
							+"***********SourceFileTag:"+tag);
					SourceFileTag sourceFileTag = (SourceFileTag) tag;
					callSite.setSourceFileName(sourceFileTag.getSourceFile());
				}else if(tag instanceof BytecodeOffsetTag){
					System.out.println("*************Method:"+method
							+"***********BytecodeOffsetTag:"+tag);
					BytecodeOffsetTag bytecodeOffsetTag = (BytecodeOffsetTag)tag;
					callSite.setByteCodeOffset(bytecodeOffsetTag.getBytecodeOffset());
				}
			}
			
			HashSet<Edge> edges = callGraph.getEdgesFromStmt(stmt);
			
			if(edges!=null){
				for(Edge edge:edges){
					callSite.setMethod(edge.getCallerMethod().getBytecodeSignature());
					
					Method calleeMethod = new Method();
					calleeMethod.setMethodName(edge.getCalleeMethod()
							.getBytecodeSignature());
					calleeMethod.setAbstract(edge.getCalleeMethod().isAbstract());
					calleeMethod.setNative(edge.getCalleeMethod().isNative());
					calleeMethod.setLibrary(! (edge.getCalleeMethod().getDeclaringClass()
							.isApplicationClass()));
					callSite.getPotentialCalleeMethods().add(calleeMethod);
				}
			}
			callSiteMap.put(stmt, callSite);
			return callSite;
		} else {
			return callSiteMap.get(stmt);
		}
	}
	
	
}
