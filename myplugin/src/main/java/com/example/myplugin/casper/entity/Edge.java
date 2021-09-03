package com.example.myplugin.casper.entity;

import soot.SootMethod;
import soot.jimple.Stmt;

public class Edge {
	/**
	 * the caller method (source) of the edge
	 */
	private SootMethod callerMethod;
	/**
	 * the callee method (target) of the edge
	 */
	private SootMethod calleeMethod;
	/**
	 * the statement that the edge happens
	 */
	private Stmt callSiteStmt;

	public SootMethod getCallerMethod() {
		return callerMethod;
	}

	public void setCallerMethod(SootMethod callerMethod) {
		this.callerMethod = callerMethod;
	}

	public SootMethod getCalleeMethod() {
		return calleeMethod;
	}

	public void setCalleeMethod(SootMethod calleeMethod) {
		this.calleeMethod = calleeMethod;
	}

	public Stmt getCallSiteStmt() {
		return callSiteStmt;
	}

	public void setCallSiteStmt(Stmt callSiteStmt) {
		this.callSiteStmt = callSiteStmt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callSiteStmt == null) ? 0 : callSiteStmt.hashCode());
		result = prime
				* result
				+ ((calleeMethod == null) ? 0 : calleeMethod.getSignature()
						.hashCode());
		result = prime
				* result
				+ ((callerMethod == null) ? 0 : callerMethod.getSignature()
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		if (callSiteStmt == null) {
			if (other.callSiteStmt != null)
				return false;
		} else if (!callSiteStmt.equals(other.callSiteStmt))
			return false;
		if (calleeMethod == null) {
			if (other.calleeMethod != null)
				return false;
		} else if (!calleeMethod.getSignature().equals(
				other.calleeMethod.getSignature()))
			return false;
		if (callerMethod == null) {
			if (other.callerMethod != null)
				return false;
		} else if (!callerMethod.getSignature().equals(
				other.callerMethod.getSignature()))
			return false;
		return true;
	}

}