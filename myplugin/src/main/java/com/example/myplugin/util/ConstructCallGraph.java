package com.example.myplugin.util;


import com.example.myplugin.casper.entity.CallGraph;

import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Hierarchy;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.Chain;


public class ConstructCallGraph {

	public CallGraph constructLibraryCallGraph() {
		soot.jimple.toolkits.callgraph.CallGraph callGraphInSoot = Scene.v()
				.getCallGraph();
		CallGraph callGraph = getCallGraphFromSootClass(Scene.v()
				.getLibraryClasses(), callGraphInSoot, Scene.v()
				.getActiveHierarchy());
		return callGraph;
	}

	public CallGraph constructAppCallGraph() {
		soot.jimple.toolkits.callgraph.CallGraph callGraphInSoot = Scene.v()
				.getCallGraph();
		CallGraph callGraph = getCallGraphFromSootClass(Scene.v()
				.getApplicationClasses(), callGraphInSoot, Scene.v()
				.getActiveHierarchy());
		return callGraph;
	}

	private CallGraph getCallGraphFromSootClass(Chain<SootClass> klasses,
			soot.jimple.toolkits.callgraph.CallGraph callGraphInSoot,
			Hierarchy hierarchy) {
		CallGraph callGraph = new CallGraph();
		for (SootClass klass : klasses) {
			/**
			 * 过滤资源类文件
			 */
			String fileName = klass.getName();
			if (fileName.endsWith("R.class")
					|| fileName.endsWith("BuildConfig.class")
					|| fileName.contains("R$")
					|| fileName.contains(".BuildConfig")
					|| fileName.endsWith(".R"))
				continue;
			for (SootMethod method : klass.getMethods()) {
				if (!method.hasActiveBody()) {
					continue;
				}
				Body body = method.getActiveBody();
				PatchingChain<Unit> units = body.getUnits();
				for (Unit unit : units) {
					Stmt stmt = (Stmt) unit;
					if (!stmt.containsInvokeExpr()) {
						continue;
					}

					InvokeExpr expr = stmt.getInvokeExpr();
					if (expr instanceof SpecialInvokeExpr) {
						SpecialInvokeExpr specialExpr = (SpecialInvokeExpr) expr;
						SootMethod callee = specialExpr.getMethod();
						callGraph.addEdge(method, callee, stmt);
						continue;
					} else if (expr instanceof StaticInvokeExpr) {
						StaticInvokeExpr staticExpr = (StaticInvokeExpr) expr;
						SootMethod callee = staticExpr.getMethod();
						callGraph.addEdge(method, callee, stmt);
						continue;
					}

					Iterator<Edge> edges = callGraphInSoot.edgesOutOf(unit);
					if (edges != null) {
						int size = 0;
						while (edges.hasNext()) {
							size++;
							Edge edge = edges.next();
							SootMethod callee = edge.tgt();
							if (callee.getBytecodeSignature().contains(
									"<clinit>()V")) {
								size--;
								continue;
							}
							callGraph.addEdge(method, callee, stmt);
						}

						if (size == 0) {
							List<SootClass> subClasses = null;
							SootClass declareClass = stmt.getInvokeExpr()
									.getMethod().getDeclaringClass();
							if (declareClass.isInterface()) {
								subClasses = hierarchy
										.getImplementersOf(declareClass);
							} else {
								subClasses = hierarchy
										.getSubclassesOfIncluding(declareClass);
							}

							for (SootClass subClass : subClasses) {
								try {

									SootMethod callee = subClass.getMethod(stmt
											.getInvokeExpr().getMethod()
											.getSubSignature());
									if (!callee.isAbstract()) {
										callGraph.addEdge(method, callee, stmt);
									}
								} catch (Exception e) {

								}
							}
						}
					}
				}
			}
		}
		return callGraph;
	}
}
