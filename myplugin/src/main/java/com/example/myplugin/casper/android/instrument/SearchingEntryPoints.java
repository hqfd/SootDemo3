package com.example.myplugin.casper.android.instrument;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;


public class SearchingEntryPoints extends SceneTransformer {
	public static final String PHASE_NAME =  "wjpp.myTrans";
	private static final String ACT_CLS_NAME = "android.app.Activity";
	private static final String SER_CLS_NAME = "android.app.Service";
	private static final String REV_CLS_NAME = "android.content.BroadcastReceiver";
	private static final String PRO_CLS_NAME = "android.content.ContentProvider";
	private static final String LISTENER_NAME = "Listener";
	
	static final int CLS_TYPE_ACT = 0; //indicate the class is a activity component
	static final int CLS_TYPE_SER = 1; //indicate the class is a service component
	static final int CLS_TYPE_REV = 2; //indicate the class is a broadcast receiver component
	static final int CLS_TYPE_PRO = 3; //indicate the class is a content provider component
	static final int CLS_TYPE_LIS = 4; //indicate the class is a event listener (e.g., button click event listener, sensor event listener)
	
	
	
	protected static final String TAG_ANALYSIS_STATUS = "STATUS";
	protected static final String TAG_ANALYSIS_TIME = "ANALYSIS TIME";
	
	
	private static HashMap<SootClass, Integer> compClassTypeMap = new HashMap<>();
	private static final HashSet<String> allClsNames = new HashSet<String>();	
	protected static boolean verboseMode = false;
	private static final HashMap<String, String> allClsNameToFilePathMap = new HashMap<String, String>();
	
	
	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		
		ArrayList<SootMethod> entryPoints = new ArrayList<SootMethod>();
		for(SootClass klass : Scene.v().getApplicationClasses()){
			boolean componentClass = false;
			boolean eventListenerClass = false;
			SootClass superCls = klass;

			//we set activity, service, broadcast receiver and content provider classes as application classes
			while(superCls.hasSuperclass()){
				superCls = superCls.getSuperclass();
				if(superCls.getName().contains(ACT_CLS_NAME)){
					compClassTypeMap.put(klass, new Integer(CLS_TYPE_ACT));
					componentClass = true;
					break;
				}
				if(superCls.getName().contains(SER_CLS_NAME)){
					compClassTypeMap.put(klass, new Integer(CLS_TYPE_SER));
					componentClass = true;
					break;
				}
				if(superCls.getName().contains(REV_CLS_NAME)){
					compClassTypeMap.put(klass, new Integer(CLS_TYPE_REV));
					componentClass = true;
					break;
				}
				if(superCls.getName().contains(PRO_CLS_NAME)){
					compClassTypeMap.put(klass, new Integer(CLS_TYPE_PRO));
					componentClass = true;
					break;
				}
			}
			if (!componentClass) {
				// if not set as component class, check whether the current
				// class implements any gui widget listeners
				Iterator<SootClass> interfaces = klass.getInterfaces().iterator();
				while (interfaces.hasNext()) {
					SootClass implementedInterface = interfaces.next();
					String interfaceName = implementedInterface.getName();
					if (interfaceName.contains("android") && interfaceName.contains(LISTENER_NAME)) {
						eventListenerClass = true;
						break;
					}
				}
			}

			// set entry points such that soot can start analysis (e.g.,
			// building whole program call graph)
			if (componentClass || eventListenerClass) {
				List<SootMethod> methods = klass.getMethods();
				for (SootMethod sm : methods) {
					if (sm.getName().matches("on[A-Z][a-zA-Z0-9]*")) {
						if (sm.isAbstract()) {
							if (verboseMode) {
								sysout(TAG_ANALYSIS_STATUS,
										"setting entry point: ignore abstract method " + sm.getSignature());
							}
							continue;
						}
						entryPoints.add(sm);
						if (verboseMode) {
							sysout(TAG_ANALYSIS_STATUS,
									"setting entry point: adding method as entry point " + sm.getSignature());
						}
					}
				}
			}
		}

		sysout(TAG_ANALYSIS_STATUS, "after wjpp phase, " + Scene.v().getClasses().size() + " soot classes found");
		sysout(TAG_ANALYSIS_STATUS, "after wjpp phase, " + Scene.v().getApplicationClasses().size() + " app classes set");
		sysout(TAG_ANALYSIS_STATUS, "after wjpp phase, " + entryPoints.size() + " entry points set");
		
		syserr(TAG_ANALYSIS_STATUS, "after wjpp phase, " + Scene.v().getClasses().size() + " soot classes found");
		syserr(TAG_ANALYSIS_STATUS, "after wjpp phase, " + Scene.v().getApplicationClasses().size() + " app classes set");
		syserr(TAG_ANALYSIS_STATUS, "after wjpp phase, " + entryPoints.size() + " entry points set");
		
		Scene.v().setEntryPoints(entryPoints);
	}
	
	public static void sysout(String tag, String output){
		System.out.println("[WLA:" + tag + "] " + output);
	}
	
	public static void syserr(String tag, String output){
		System.err.println("[WLA:" + tag + "] " + output);
	}
	
}
