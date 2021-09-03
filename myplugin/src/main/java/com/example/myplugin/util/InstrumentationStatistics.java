package com.example.myplugin.util;


import com.example.myplugin.casper.entity.CallSite;
import com.example.myplugin.casper.entity.CallSiteCFG;
import com.example.myplugin.casper.entity.MethodInstrumentSolution;

import java.util.HashMap;


public class InstrumentationStatistics {
	public static void doStatistics(
			HashMap<String, MethodInstrumentSolution> methodInstrumentSolutions, HashMap<String, CallSiteCFG> callSiteCFGMap) {
		int numOfMethods = 0;
		int numOfInstrumentationForLogging = 0;
		int numOfInstrumentationForPushingDepth = 0;
		int numOfInstrumentationForPushingDepthAndID = 0;
		int numOfTotalCallSites = 0;
		int numOfCallSitesContainingLibrary = 0;
		int numOfCallSitesContainVirtualCall = 0;
		
		for(String method:callSiteCFGMap.keySet()){
			MethodInstrumentSolution solution = methodInstrumentSolutions.get(method);
			CallSiteCFG cfg = callSiteCFGMap.get(method);
			numOfMethods++;
			
			if(solution==null){
				continue;
			}
			
			numOfInstrumentationForLogging += solution
					.getInstrumentForLogging().size();
			numOfInstrumentationForPushingDepth += solution.getInstrumentForPushingDepth().size();
			numOfInstrumentationForPushingDepthAndID += solution.getInstrumentForPushingDepthAndID().size();
			numOfTotalCallSites += cfg.getCallSites().size()-2;
			
			for(CallSite callSite:cfg.getCallSites()){
				if(callSite.isContainLibraryCall()){
					numOfCallSitesContainingLibrary++;
				}
				if(callSite.isVirtual()){
					numOfCallSitesContainVirtualCall++;
				}
			}
		}
		System.out.println("num of total methods: " + numOfMethods);
		System.out.println("num of total call sites: " + numOfTotalCallSites);
		System.out.println("num of virtual calls: " + numOfCallSitesContainVirtualCall);
		System.out.println("num of library calls: " + numOfCallSitesContainingLibrary);
		System.out.println("num of instrumentation for logging: " + numOfInstrumentationForLogging);
		System.out.println("num of instrumentation for pushing depth: " + numOfInstrumentationForPushingDepth);
		System.out.println("num of instrumentation for pushing depth and call site ID: " + numOfInstrumentationForPushingDepthAndID);
		
		
		
	}
}
