package com.example.myplugin.casper.entity;

import java.util.HashSet;

public class MethodInstrumentSolution {
	private String method = "";
	private HashSet<CallSite> instrumentForLogging = new HashSet<CallSite>();
	private HashSet<CallSite> instrumentForPushingDepth = new HashSet<CallSite>();	
	private HashSet<CallSite> instrumentForPushingDepthAndID = new HashSet<CallSite>();
	private CallSiteCFG callSiteCFG = null;
	private boolean isCompleted = false;
	public boolean isCompleted() {
		return isCompleted;
	}

	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public CallSiteCFG getCallSiteCFG() {
		return callSiteCFG;
	}

	public HashSet<CallSite> getInstrumentForLogging() {
		return instrumentForLogging;
	}

	public HashSet<CallSite> getInstrumentForPushingDepth() {
		return instrumentForPushingDepth;
	}

	public HashSet<CallSite> getInstrumentForPushingDepthAndID() {
		return instrumentForPushingDepthAndID;
	}
	
	public String getMethod() {
		return method;
	}

	public void setCallSiteCFG(CallSiteCFG callSiteCFG) {
		this.callSiteCFG = callSiteCFG;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
}
