package com.example.myplugin.casper.android.instrument;


import com.example.myplugin.util.DoAndroidAppInstrumentation;
import com.example.myplugin.util.OptionParser;
import com.example.myplugin.util.StoreMetaData;

import java.io.File;
import java.util.Date;
import java.util.List;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.options.Options;

public class Main {
	public static void main(String[] args) {
		instrument("E://IdeaProjects/Demo6/target/classes","");
	}
	public static void instrument(String processDir,String ouputDir) {
		Date start = new Date();
		Options.v().set_output_dir(ouputDir);
		SearchingEntryPoints
			entryPointsSearcher = new SearchingEntryPoints();
		PackManager.v().getPack("wjpp").add
			(new Transform(SearchingEntryPoints.PHASE_NAME,
					entryPointsSearcher));
		String[] param = {"--optimize","0","-process-path",processDir};
		String[] args = param;
		Instrumenter instrumenter = null;
		Transform instrumentTransformer = null;
		instrumenter = new FullInstrumentation();
		instrumentTransformer = new Transform(FullInstrumentation.PHASE_NAME,instrumenter);

		PackManager.v().getPack("wjtp").add(instrumentTransformer);
		for (String dumpingClass : Instrumenter.DUMPING_CLASSES) {
			Scene.v().addBasicClass(dumpingClass, SootClass.SIGNATURES);
		}

		args = OptionParser.parse_options(args);
		// make the solution
		try {
			soot.Main.main(args);
		} catch (Exception exception) {
//			System.out
//					.println("soot cannot output correct code in this step, however "
//							+ "our solution can be continued to output to code, "
//							+ "since we leverage bcel and asm to do instrumentation");
//			exception.printStackTrace();
		}




		// do instrumentation based on the solution

		List<String> processDirsList = Options.v().process_dir();
		String outputDir = Options.v().output_dir();
//		if (outputDir.equals("")) {
//			outputDir = "sootOutput";
//		}
//		outputDir = "E://IdeaProjects/Demo6/target/classes";
		//before do instrumentation, clean the output dir
		System.out.println("*****************before doInstrumentation***************");
		DoAndroidAppInstrumentation doInstrumentation = new DoAndroidAppInstrumentation();
		doInstrumentation.doInstrumentation(processDirsList,
				instrumenter.methodInstrumentSolutions, outputDir);


		StoreMetaData storeMetaData = new StoreMetaData();
		String metaDataFile = outputDir + File.separator + "meta.xml";
		storeMetaData.storeMetaData(metaDataFile, instrumenter.methodInstrumentSolutions, instrumenter.callSiteCFGMap);


		Date end = new Date();
		double seconds = (end.getTime() - start.getTime()) / 1000;
		System.out.println("It cost " + seconds + " seconds to instrument.");
	}

}
