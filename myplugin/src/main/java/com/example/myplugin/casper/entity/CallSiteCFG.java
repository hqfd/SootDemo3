package com.example.myplugin.casper.entity;
import java.util.ArrayList;
import java.util.HashSet;

public class CallSiteCFG {
	/**
	 * the entry call site of the method
	 */
	private CallSite entryCallSite = null;
	/**
	 * the exit call site of the method
	 */
	private CallSite exitCallSite = null;
	

	public CallSite getEntryCallSite() {
		return entryCallSite;
	}

	public void setEntryCallSite(CallSite entryCallSite) {
		this.entryCallSite = entryCallSite;
	}

	public CallSite getExitCallSite() {
		return exitCallSite;
	}

	public void setExitCallSite(CallSite exitCallSite) {
		this.exitCallSite = exitCallSite;
	}
	
	/**
	 * all the call sites in the method
	 */
	private HashSet<CallSite> callSites = new HashSet<CallSite>();
	
	/**
	 * the method where the call site is
	 */
	private String method = "";

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public HashSet<CallSite> getCallSites() {
		return callSites;
	}

	public boolean containedMethodCall() {
		boolean isContainedMethodCall = false;
		for (CallSite callSite : callSites) {
			if (callSite.getStmt()!=null && callSite.getStmt().containsInvokeExpr()) {
				isContainedMethodCall = true;
				break;
			}
		}
		return isContainedMethodCall;
	}

	public ArrayList<CallSite> getCallSitesInReverseTopologicalOrder() {
		ArrayList<CallSite> orderedCallSites = new ArrayList<CallSite>();
		HashSet<CallSite> callSitesTraversing = new HashSet<CallSite>();
		HashSet<CallSite> callSitesTraversed = new HashSet<CallSite>();

		traverseInReverseTopologicalOrder(entryCallSite,
				callSitesTraversing, callSitesTraversed, orderedCallSites);
		
		return orderedCallSites;
	}

	private void traverseInReverseTopologicalOrder(CallSite callSite,
			HashSet<CallSite> callSitesTraversing,
			HashSet<CallSite> callSitesTraversed,
			ArrayList<CallSite> orderedCallSites) {
		callSitesTraversing.add(callSite);

		if (callSite.getSuccessors() != null
				&& callSite.getSuccessors().size() > 0) {
			for (CallSite successor : callSite.getSuccessors()) {
				if (!callSitesTraversing.contains(successor)
						&& !callSitesTraversed.contains(successor)) {
					traverseInReverseTopologicalOrder(successor,
							callSitesTraversing, callSitesTraversed,
							orderedCallSites);
				}
			}
		}		
		callSitesTraversing.remove(callSite);
		callSitesTraversed.add(callSite);
		orderedCallSites.add(callSite);
	}

	public HashSet<EdgeInCallSiteCFG> getBackEdges() {
		HashSet<EdgeInCallSiteCFG> backEdges = new HashSet<EdgeInCallSiteCFG>();
		HashSet<CallSite> callSitesTraversing = new HashSet<CallSite>();
		HashSet<CallSite> callSitesTraversed = new HashSet<CallSite>();
		traverse(entryCallSite, callSitesTraversing, callSitesTraversed,
				backEdges);
		return backEdges;
	}

	private void traverse(CallSite callSite,
			HashSet<CallSite> callSitesTraversing,
			HashSet<CallSite> callSitesTraversed,
			HashSet<EdgeInCallSiteCFG> backEdges) {
		callSitesTraversing.add(callSite);

		if (callSite.getSuccessors() != null
				&& callSite.getSuccessors().size() > 0) {
			for (CallSite successor : callSite.getSuccessors()) {
				if (callSitesTraversing.contains(successor)) {
					EdgeInCallSiteCFG edge = new EdgeInCallSiteCFG();
					edge.setPredecessor(callSite);
					edge.setSuccessor(successor);
					backEdges.add(edge);
				} else if (callSitesTraversed.contains(successor)) {
					// do nothing
				} else {
					traverse(successor, callSitesTraversing,
							callSitesTraversed, backEdges);
				}
			}
		}
		callSitesTraversing.remove(callSite);
		callSitesTraversed.add(callSite);
	}

	@Override
	public String toString() {
		StringBuffer buffer=new StringBuffer("call site CFG of the method " );
		buffer.append(method);
		buffer.append("\n");
		ArrayList<CallSite> orderedCallSites = getCallSitesInReverseTopologicalOrder();
		buffer.append("topological order is:\n");
		for(int i= orderedCallSites.size()-1; i>=0; i--){
			CallSite callSite = orderedCallSites.get(i);
			buffer.append(callSite.getCallSiteID() + "\t"); 
			buffer.append("isEntry:" + callSite.isEntry() + "\t");
			buffer.append("isExit:" + callSite.isExit() + "\t");
			for(CallSite sCallSite:callSite.getSuccessors()){
				buffer.append(sCallSite.getCallSiteID() + ";");
			}
			buffer.append("\n");
		}
		
		return buffer.toString();
	}
	
}