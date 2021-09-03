package com.example.myplugin.casper.entity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import soot.SootMethod;
import soot.jimple.Stmt;

public class CallGraph {

	private HashSet<SootMethod> nodes = new HashSet<SootMethod>();
	private HashSet<Edge> edges = new HashSet<Edge>();

	public HashSet<Edge> getEdges() {
		return edges;
	}

	private HashMap<SootMethod, HashSet<Edge>> srcEdgesMapping = new HashMap<SootMethod, HashSet<Edge>>();
	private HashMap<SootMethod, HashSet<Edge>> tgtEdgesMapping = new HashMap<SootMethod, HashSet<Edge>>();
	private HashMap<Stmt, HashSet<Edge>> stmtEdgesMapping = new HashMap<Stmt, HashSet<Edge>>();

	public HashSet<SootMethod> getEntries() {
		HashSet<SootMethod> entries = new HashSet<SootMethod>();
		entries.addAll(nodes);
		entries.removeAll(tgtEdgesMapping.keySet());
		return entries;
	}

	public HashSet<Edge> getBackEdges() {
		HashSet<Edge> backEdges = new HashSet<Edge>();
		HashSet<SootMethod> methodsTraversing = new HashSet<SootMethod>();
		HashSet<SootMethod> methodsTraversed = new HashSet<SootMethod>();

		for (SootMethod method : getEntries()) {
			traverse(method, methodsTraversing, methodsTraversed, backEdges);
		}
		return backEdges;
	}

	public HashSet<Edge> getEdgesFromStmt(Stmt stmt) {
		return stmtEdgesMapping.get(stmt);
	}

	public HashMap<Stmt, HashSet<Edge>> getVirtualCallEdges() {
		HashMap<Stmt, HashSet<Edge>> virtualEdges = new HashMap<Stmt, HashSet<Edge>>();

		for (Stmt stmt : stmtEdgesMapping.keySet()) {
			if (stmtEdgesMapping.get(stmt) != null
					&& stmtEdgesMapping.get(stmt).size() > 1) {
				
				virtualEdges.put(stmt, new HashSet<Edge>());
				virtualEdges.get(stmt).addAll(stmtEdgesMapping.get(stmt));
			}
		}
		return virtualEdges;
	}

	public ArrayList<SootMethod> getMethodsInReverseTopologicalOrder() {
		ArrayList<SootMethod> methodsInReverseTopologicalOrder = new ArrayList<SootMethod>();
		HashSet<SootMethod> methodsTraversing = new HashSet<SootMethod>();
		HashSet<SootMethod> methodsTraversed = new HashSet<SootMethod>();
		for (SootMethod method : getEntries()) {
			traverseInReverseTopologicalOrder(method, methodsTraversing,
					methodsTraversed, methodsInReverseTopologicalOrder);
		}
		
		
		return methodsInReverseTopologicalOrder;
	}

	private void traverseInReverseTopologicalOrder(SootMethod method,
                                                   HashSet<SootMethod> methodsTraversing,
                                                   HashSet<SootMethod> methodsTraversed,
                                                   ArrayList<SootMethod> methodsInTopologicalOrder) {
		if(!method.getDeclaringClass().isApplicationClass()){
			return;
		}
		methodsTraversing.add(method);
		if(srcEdgesMapping.get(method)!=null){
			for (Edge edge : srcEdgesMapping.get(method)) {
				SootMethod callee = edge.getCalleeMethod();
				if (!methodsTraversing.contains(callee)
						&& !methodsTraversed.contains(callee)) {
					// method has not been traversed
					traverseInReverseTopologicalOrder(callee, methodsTraversing,
							methodsTraversed, methodsInTopologicalOrder);
				}
			}
		}
		methodsTraversing.remove(method);
		methodsTraversed.add(method);
		methodsInTopologicalOrder.add(method);
	}

	private void traverse(SootMethod method,
                          HashSet<SootMethod> methodsTraversing,
                          HashSet<SootMethod> methodsTraversed, HashSet<Edge> backEdges) {
		if(!method.getDeclaringClass().isApplicationClass()){
			return;
		}
		methodsTraversing.add(method);
		if(srcEdgesMapping.get(method)!=null){
			for (Edge edge : srcEdgesMapping.get(method)) {
				SootMethod callee = edge.getCalleeMethod();
				if (methodsTraversing.contains(callee)) {
					// back edge
					backEdges.add(edge);
				} else if (methodsTraversed.contains(callee)) {
					// method has been traversed
					// do nothing
				} else {
					// method has not been traversed
					traverse(callee, methodsTraversing, methodsTraversed, backEdges);
				}
			}
		}
		
		methodsTraversing.remove(method);
		methodsTraversed.add(method);
	}

	public void addEdge(SootMethod caller, SootMethod callee, Stmt callSiteStmt) {
		nodes.add(caller);
		nodes.add(callee);

		Edge edge = new Edge();
		edge.setCalleeMethod(callee);
		edge.setCallerMethod(caller);
		edge.setCallSiteStmt(callSiteStmt);
		edges.add(edge);

		if (srcEdgesMapping.get(caller) == null) {
			srcEdgesMapping.put(caller, new HashSet<Edge>());
		}
		srcEdgesMapping.get(caller).add(edge);

		if (tgtEdgesMapping.get(callee) == null) {
			tgtEdgesMapping.put(callee, new HashSet<Edge>());
		}
		tgtEdgesMapping.get(callee).add(edge);

		if (stmtEdgesMapping.get(edge.getCallSiteStmt()) == null) {
			stmtEdgesMapping.put(callSiteStmt, new HashSet<Edge>());
		}
		stmtEdgesMapping.get(callSiteStmt).add(edge);
	}

	public void removeEdge(SootMethod caller, SootMethod callee,
                           Stmt callSiteStmt) {
		HashSet<Edge> edges = srcEdgesMapping.get(caller);
		Edge removeEdge = null;
		for (Edge edge : edges) {
			if (edge.getCalleeMethod().getSignature()
					.equals(callee.getSignature())
					&& edge.getCallSiteStmt() == callSiteStmt) {
				removeEdge = edge;
				break;
			}
		}
		if (removeEdge == null) {
			throw new RuntimeException("no such edge.");
		} else {
			srcEdgesMapping.get(caller).remove(removeEdge);
			tgtEdgesMapping.get(callee).remove(removeEdge);
		}
	}
	
	public boolean hasMethod(SootMethod method){
		return nodes.contains(method);
	}
	
	public HashSet<Edge> getTargets(Stmt cs)
	{
		return stmtEdgesMapping.get(cs);
	}
}