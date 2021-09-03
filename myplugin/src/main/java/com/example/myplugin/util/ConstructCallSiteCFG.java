package com.example.myplugin.util;


import com.example.myplugin.casper.entity.CallGraph;
import com.example.myplugin.casper.entity.CallSite;
import com.example.myplugin.casper.entity.CallSiteCFG;

import java.util.HashSet;
import java.util.List;

import soot.SootMethod;
import soot.jimple.IfStmt;
import soot.jimple.Stmt;
import soot.toolkits.graph.DirectedGraph;
import soot.util.cfgcmd.CFGGraphType;

public class ConstructCallSiteCFG {

    public CallSiteCFG constructCallSiteCFG(SootMethod method,
                                            CallGraph callGraph) {

        if (!method.hasActiveBody()) {
            return null;
        }
        CFGGraphType graphtype = CFGGraphType
                .getGraphType("ExceptionalUnitGraph");
        @SuppressWarnings("unchecked")
        DirectedGraph<Stmt> graph = graphtype
                .buildGraph(method.getActiveBody());
        List<Stmt> heads = graph.getHeads();
        HashSet<Stmt> toDoStmts = new HashSet<Stmt>();
        HashSet<Stmt> doingStmts = new HashSet<Stmt>();
        HashSet<Stmt> doneStmts = new HashSet<Stmt>();

        CallSiteCFG callSiteCFG = new CallSiteCFG();
        callSiteCFG.setMethod(method.getBytecodeSignature());

        CallSite entryCallSite = ConstructCallSite.getEntryCallSite(method
                .getBytecodeSignature());
        CallSite exitCallSite = ConstructCallSite.getExitCallSite(method
                .getBytecodeSignature());

        callSiteCFG.setEntryCallSite(entryCallSite);
        callSiteCFG.setExitCallSite(exitCallSite);
        callSiteCFG.getCallSites().add(entryCallSite);
        callSiteCFG.getCallSites().add(exitCallSite);

        for (Stmt stmt : heads) {
            if (stmt.containsInvokeExpr()) {
                toDoStmts.add(stmt);
            } else if (graph.getTails().contains(stmt)) {
                toDoStmts.add(stmt);
            } else {
                toDoStmts.addAll(getSuccessorCallSites(graph, stmt));
            }
        }

        for (Stmt headStmt : toDoStmts) {
            if (headStmt.containsInvokeExpr()) {
//				 isContainedMethodCall = true;
            }
            CallSite callSite = ConstructCallSite.getCallSite(headStmt,
                    callGraph, method.getBytecodeSignature());
            callSiteCFG.getEntryCallSite().getSuccessors().add(callSite);
            callSite.getPredecessors().add(entryCallSite);
            if (graph.getTails().contains(headStmt)) {
                callSiteCFG.getExitCallSite().getPredecessors().add(callSite);
                callSite.getSuccessors().add(callSiteCFG.getExitCallSite());
            }
            callSiteCFG.getCallSites().add(callSite);
        }

        while (toDoStmts.size() != 0) {
            doingStmts.addAll(toDoStmts);
            toDoStmts.clear();
            for (Stmt stmt : doingStmts) {
                doneStmts.add(stmt);
                CallSite callSite = ConstructCallSite.getCallSite(stmt,
                        callGraph, method.getBytecodeSignature());
                callSiteCFG.getCallSites().add(callSite);

                HashSet<Stmt> successorCallSites = getSuccessorCallSites(graph,
                        stmt);
                if (successorCallSites.size() == 0) {
                    callSiteCFG.getExitCallSite().getPredecessors().add(callSite);
                    callSite.getSuccessors().add(callSiteCFG.getExitCallSite());
                } else {
                    for (Stmt successorCallSite : successorCallSites) {
                        CallSite sCallSite = ConstructCallSite.getCallSite(
                                successorCallSite, callGraph, method.getBytecodeSignature());
                        callSiteCFG.getCallSites().add(sCallSite);
                        sCallSite.getPredecessors().add(callSite);
                        callSite.getSuccessors().add(sCallSite);
                        if (!doneStmts.contains(successorCallSite)
                                && !doingStmts.contains(successorCallSite)) {
                            toDoStmts.add(successorCallSite);
                        }
                    }
                }
            }
            doingStmts.clear();
        }

        return callSiteCFG;
    }

    private HashSet<Stmt> getSuccessorCallSites(DirectedGraph<Stmt> graph,
                                                Stmt stmt) {
        HashSet<Stmt> toDoStmts = new HashSet<Stmt>();
        HashSet<Stmt> doingStmts = new HashSet<Stmt>();
        HashSet<Stmt> doneStmts = new HashSet<Stmt>();
        HashSet<Stmt> successorStmts = new HashSet<Stmt>();
        doneStmts.add(stmt);
        toDoStmts.addAll(graph.getSuccsOf(stmt));
        while (toDoStmts.size() != 0) {
            doingStmts.addAll(toDoStmts);
            toDoStmts.clear();
            for (Stmt doingStmt : doingStmts) {
                doneStmts.add(doingStmt);
                if (doingStmt.containsInvokeExpr()) {
                    successorStmts.add(doingStmt);
                } else if (graph.getTails().contains(doingStmt)) {
                    successorStmts.add(doingStmt);
                } else {
                    for (Stmt toDoStmt : graph.getSuccsOf(doingStmt)) {
                        if (!doneStmts.contains(toDoStmt)
                                && !doingStmts.contains(toDoStmt)) {
                            toDoStmts.add(toDoStmt);
                        }
                    }
                }
            }
            doingStmts.clear();
        }
        return successorStmts;
    }

}
