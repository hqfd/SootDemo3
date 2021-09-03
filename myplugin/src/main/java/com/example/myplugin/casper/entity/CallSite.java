package com.example.myplugin.casper.entity;
import java.util.HashSet;

import soot.jimple.Stmt;

public class CallSite {

	/**
	 * EMPTY_CALL_SITE is a call site of which
	 * callSiteID is 0
	 * **/
	public static CallSite EMPTY_CALL_SITE = new CallSite();
	
	/**
	 * EMPTY_CALL_SITE_2 is a call site 
	 * which is used to label some special call site 
	 * and whose callSiteID is -1
	 * 
	 */
	public static CallSite EMPTY_CALL_SITE_2 = new CallSite(-1);
	
	/**
	 * the offset in the original bytecode
	 */
	private int byteCodeOffset = -1;
	/**
	 * the unique ID of the call site
	 */
	private int callSiteID = 0;
	/**
	 * whether the call site is an entry call site
	 */
	private boolean isEntry = false;
	/**
	 * whether the call site is an exit call site
	 */
	private boolean isExit = false;

	/**
	 * the line number in the source code
	 */
	private int lineNum = -1;
	/**
	 * the caller method name
	 */
	private String callerMethod = "";
	/**
	 * the potential callee methods
	 */
	private HashSet<Method> potentialCalleeMethods = new HashSet<Method>();
	/**
	 * the predecessor call sites of the current call site
	 */
	private HashSet<CallSite> predecessors = new HashSet<CallSite>();
	/**
	 * the source file name
	 */
	private String sourceFileName = "";
	/**
	 * the statement where the call site is in
	 * this statement stmt is used to make the call site unique,
	 * since the soot analysis will generate each statemetn only once
	 */
	private Stmt stmt;
	
	/**
	 * the successor call sites of the current call site
	 */
	private HashSet<CallSite> successors = new HashSet<CallSite>();

	private String stmtString = "";
	
	
	public String getStmtString() {
		return stmtString;
	}

	public void setStmtString(String stmtString) {
		this.stmtString = stmtString;
	}

	public boolean isReturnSite(){
		boolean isReturnSite = false;
		for(CallSite successor:successors){
			if(successor.isExit){
				isReturnSite = true;
			}
		}
		return isReturnSite;
	}
	
	public CallSite(){
		
	}
	public CallSite(int callSiteID){
		this.callSiteID = callSiteID;				
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CallSite other = (CallSite) obj;
		if (callSiteID != other.callSiteID)
			return false;
		return true;
	}
	
	

	public int getByteCodeOffset() {
		return byteCodeOffset;
	}

	public int getCallSiteID() {
		return callSiteID;
	}

	public int getLineNum() {
		return lineNum;
	}

	public String getMethod() {
		return callerMethod;
	}

	public HashSet<Method> getPotentialCalleeMethods() {
		return potentialCalleeMethods;
	} 
	
	public HashSet<CallSite> getPredecessors() {
		return predecessors;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public Stmt getStmt() {
		return stmt;
	}

	public HashSet<CallSite> getSuccessors() {
		return successors;
	}



	@Override
	public int hashCode() {
		return callSiteID;
	}


	public boolean isEntry() {
		return isEntry;
	}

	public boolean isExit() {
		return isExit;
	}

	public boolean isContainLibraryCall() {
		boolean isLibraryCall = false;
		for(Method method:potentialCalleeMethods){
			if(method.isLibrary()){
				isLibraryCall = true;
			}
		}
		return isLibraryCall;
	}
	
	public boolean isVirtual(){
		if(potentialCalleeMethods!=null && potentialCalleeMethods.size()>1){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean isInDeterministicProductionRule(){
		if(this.getPredecessors()!=null){
			for(CallSite pCallSite:this.getPredecessors()){
				if(pCallSite.getSuccessors().size()>1){
					return false;
				}
			}
		}
		return true;
	}

	public void setByteCodeOffset(int byteCodeOffset) {
		this.byteCodeOffset = byteCodeOffset;
	}

	public void setCallSiteID(int callSiteID) {
		this.callSiteID = callSiteID;
	}


	public void setEntry(boolean isEntry) {
		this.isEntry = isEntry;
	}

	public void setExit(boolean isExit) {
		this.isExit = isExit;
	}



	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	public void setMethod(String method) {
		this.callerMethod = method;
	}

	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	public void setStmt(Stmt stmt) {
		this.stmt = stmt;
	}


	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		if(this.equals(EMPTY_CALL_SITE)){
			buffer.append("EMPTY_CALL_SITE");
		}else if(this.equals(EMPTY_CALL_SITE_2)){
			buffer.append("EMPTY_CALL_SITE_2");
		}else{
			buffer.append("CallSite [callSiteID=" + callSiteID + "]");
		}
		buffer.append("[");
		for(Method callee:potentialCalleeMethods){
			buffer.append(callee.toString());
		}
		buffer.append("]");
		return buffer.toString();
	}

	
}