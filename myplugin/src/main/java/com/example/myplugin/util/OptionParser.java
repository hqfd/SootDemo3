package com.example.myplugin.util;



import com.example.myplugin.casper.android.instrument.SearchingEntryPoints;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import soot.options.Options;

@SuppressWarnings("unchecked")
public class OptionParser {
	
	@SuppressWarnings("rawtypes")
	private static Map phaseOptions = new HashMap();
	private static ArrayList<String> defaultSootOptions = new ArrayList<String>();
	public static String TEMP_LIB_PATH = "TEMP_LIB";
	static{
		phaseOptions.put(SearchingEntryPoints.PHASE_NAME, "on");
		defaultSootOptions.add("-keep-line-number");
		defaultSootOptions.add("-keep-bytecode-offset");
		defaultSootOptions.add("-pp");
		defaultSootOptions.add("-w");
		defaultSootOptions.add("-allow-phantom-refs");
//		defaultSootOptions.add("-v");
//		defaultSootOptions.add("-output-format n");
//		phaseOptions.put("-output-format", "n");
	}
	
	public static void usage() {
		System.out.println("usage:");
		
		
		System.out
				.println("[--soot-classpath ...] [--process-dir ...] [-d sootOutputDir]");
		System.out
				.println("--soot-classpath classPath   (multiple classpaths can be speparated by \";\" in Windows OS and \":\" in Linux or Mac)"
						+ "\t");
		System.out
				.println("--process-dir processDir  (multiple dirs can be assigned by multiple times)"
						+ "\t");
		
//		System.out.println("--optimize  optimizeLevel  (0 represents full instrument, 1 represents branch instrument, 3 represents casper instrument.) "
//				+ " If not specified, the default value is 0");
		
		System.out.println("-d sootOutputDir " + "\t");
	}
	
	@SuppressWarnings("unchecked")
	public static String[] parse_options(String[] args) {
		ArrayList<String> proessDirs = new ArrayList<String>();
		if (args.length == 0) {
			usage();
			System.exit(0);
		}
		
		String sootClassPathString = "";
		@SuppressWarnings("rawtypes")
		List sootArgs = new ArrayList();
		for (int i = 0, n = args.length; i < n; i++) {
			if (args[i].equals("--soot-class-path")
					|| args[i].equals("-soot-class-path")
					|| args[i].equals("--soot-classpath")
					|| args[i].equals("-soot-classpath")) {
				// Pass classpaths without treating ":" as a method specifier.
//				String currentClassPath = System.getProperty("java.class.path");				
//				sootArgs.add(args[i]);							
//				sootArgs.add(args[++i] + File.pathSeparator + currentClassPath);
				sootClassPathString = args[++i];
			} else if (args[i].equals("--process-dir")
					|| args[i].equals("-process-dir")) {
				sootArgs.add(args[i]);
				sootArgs.add(args[++i]);
				proessDirs.add(args[i]);
			} else if (args[i].equals("-d") || args[i].equals("--d")) {
				sootArgs.add(args[i]);
				sootArgs.add(args[++i]);
				
			} else if(args[i].equals("--optimize")){
				++i;
			} else if(args[i].equals("-exclude")||args[i].equals("-x")){
				i++;
//				Scene.v().excludedPackages.add(args[i]);
			} else {
				sootArgs.add(args[i]);
			}
		}
		
		
		//handling sootClassPath
		if(sootClassPathString!=null && !sootClassPathString.equals("")){
			String sootClassPath = "";
			for(String split:sootClassPathString.split(File.pathSeparator,-1)){
				if(split.toLowerCase().endsWith(".aar")){
					HashSet<String> jarFiles = AARFileHandler.extractJarFilesFromAARFile(split, TEMP_LIB_PATH);
					for(String jarFile:jarFiles){
						sootClassPath += jarFile + File.pathSeparator;
					}
				}else{
					sootClassPath += split + File.pathSeparator;
				}
			}
			if(sootClassPath.endsWith(File.pathSeparator)){
				sootClassPath = sootClassPath.substring(0, sootClassPath.length()-1);
			}
			String currentClassPath = System.getProperty("java.class.path");
			sootClassPath += File.pathSeparator + currentClassPath;
			sootArgs.add(0, "--soot-classpath");
			sootArgs.add(1, sootClassPath);			
		}
		
		// set up the necessary options for phases
		for (Object key : phaseOptions.keySet()) {
			sootArgs.add("--p");
			sootArgs.add(key);
			sootArgs.add(phaseOptions.get(key));
		}
		// set up the necessary options
		for (Object option : defaultSootOptions) {
			sootArgs.add(option);
		}
		sootArgs.add("-output-format");
		sootArgs.add("n");
		
		String[] sootArgsArray = new String[sootArgs.size()];
		Options.v().set_soot_classpath("");
		return (String[]) sootArgs.toArray(sootArgsArray);
	}
	
	public static int getOptimizeLevel(String[] args){
		int optimizeLevel = 0;		
		for (int i = 0, n = args.length; i < n; i++) {
			if(args[i].equals("--optimize")){
				++i;
				try{
					optimizeLevel = Integer.parseInt(args[i]);
				}catch(Exception e){
					optimizeLevel = 0;
				}								
			}
		}	
		return optimizeLevel;
	}
	
}
