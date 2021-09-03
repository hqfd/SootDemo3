package com.example.myplugin.casper.android.instrument.util;

import android.util.Log;

public class LoggingClassCompress {	
	private static ThreadBuffer[] threadCaches = new ThreadBuffer[Constant.THREAD_COUNT];
	private static Integer[] DepthInThreads = new Integer[Constant.THREAD_COUNT];
	private static Stack[] ImportantDepth = new Stack[Constant.THREAD_COUNT];
	private static Integer[] ImportantIDs = new Integer[Constant.THREAD_COUNT];
	static {
		LoggingThreadCompress.loggingThread.start();
		for (int i = 0; i < Constant.THREAD_COUNT; i++) {
			threadCaches[i] = new ThreadBuffer(Constant.BUFFER_SIZE);
			threadCaches[i].tid = i;
			DepthInThreads[i] = new Integer(0);
			ImportantDepth[i] = new Stack();
			ImportantDepth[i].push(1);
			ImportantIDs[i] = new Integer(0);
		}
//		Runtime.getRuntime().addShutdownHook(new Hooker());
//		Log.i("LOGGING", "the static initializer in LoggingClassCompress in Process " + android.os.Process.myPid());
//		Thread.setDefaultUncaughtExceptionHandler(new UnCaughtExceptionHandler());
	}

	public static ThreadBuffer[] getThreadCaches(){
		return threadCaches;
	}

	public static void logCallSiteID(int callSiteID){
		int id = (int)Thread.currentThread().getId();
//		Log.e("LOGGING", "callsite ID:"+callSiteID+"");

		if(id<Constant.THREAD_COUNT){
//			int depth = DepthInThreads[id];
//			long content = depth;
			long content = DepthInThreads[id].value;
			content = (content << 32) + callSiteID;
			ThreadBuffer buffer = threadCaches[id];
			buffer.add(content);
		}else{
			//it should happen very rare
			synchronized(LoggingClassCompress.class){
				enlargeThreadNum(id);
			}
//			int depth = DepthInThreads[id];
//			long content = depth;
			long content = DepthInThreads[id].value;
			content = (content << 32) + callSiteID;

			ThreadBuffer buffer = threadCaches[id];
			buffer.add(content);
		}
	}

	public static void pushDepth(){
		int id = (int)Thread.currentThread().getId();
//		int depth = DepthInThreads[id];
		ImportantDepth[id].push(DepthInThreads[id].value+1);
	}

	public static void popDepth(){
		int id = (int)Thread.currentThread().getId();
		ImportantDepth[id].pop();
	}

	public static void pushDepthAndID(int callSiteID){
		int id = (int)Thread.currentThread().getId();
//		int depth = DepthInThreads[id];
		ImportantDepth[id].push(DepthInThreads[id].value+1);
		ImportantIDs[id].value = callSiteID;
	}

	public static void popDepthAndID(){
		int id = (int)Thread.currentThread().getId();
		ImportantDepth[id].pop();
		ImportantIDs[id] =  new Integer(0);
	}

	public static void methodEntry(int callSiteID){
		try{
			int id = (int)Thread.currentThread().getId();
//			Log.e("LOGGING", "start: pid,tid,cid: <"+android.os.Process.myPid() + ","+ id + ","+callSiteID+">");
//			Log.e("LOGGING", "callsite ID:"+callSiteID+"");
			if(id<Constant.THREAD_COUNT){
				DepthInThreads[id].value++;
			}else{
				synchronized (LoggingClassCompress.class) {
					enlargeThreadNum(id);
				}
				DepthInThreads[id].value++;
			}

			int depth = DepthInThreads[id].value;
//			if(depth<=0){
//				Log.e("LOGGING", "there is wrong depth information at the call site "+callSiteID);
//				Log.e("LOGGING", "depth is :"+depth);
//				Log.e("LOGGING", "exception: pid,tid,cid: <"+android.os.Process.myPid() + ","+ id + ","+callSiteID+">");
//			}

//			ThreadBuffer buffer = threadCaches[id];
			if (ImportantDepth[id].peek() == depth ){
				if(ImportantIDs[id].value !=0){
					long content = depth - 1;
					content = (content << 32) + ImportantIDs[id].value;
					threadCaches[id].add(content);
					ImportantIDs[id].value = 0;
				}
				long content = depth;
				content = (content << 32) + callSiteID;
				threadCaches[id].add(content);
			}
		}catch(Exception e){
			Log.e("LOGGING", "exception happens in the methodEntry(int) with the call site "+callSiteID);
			Log.e("LOGGING", e.toString());
		}
	}

	public static void methodExit(){
		try{
			int id = (int)Thread.currentThread().getId();
//			Log.e("LOGGING", "end: pid,tid,...: <"+android.os.Process.myPid() + ","+ id + ","+">");
//			int depth = 0;
			if(id<Constant.THREAD_COUNT){
//				depth = DepthInThreads[id];
				--DepthInThreads[id].value;
			}else{
				synchronized(LoggingClassCompress.class){
					enlargeThreadNum(id);
				}
				--DepthInThreads[id].value;
//				depth = DepthInThreads[id];
			}
//			DepthInThreads[id] = --depth;
		}catch(Exception e){
			Log.e("LOGGING", "Exception happened in methodExit()");
			Log.e("LOGGING", e.toString());
		}

	}

	public static void methodExit(int callSiteID){
		try{

			int id = (int)Thread.currentThread().getId();
//			Log.e("LOGGING", "end: pid,tid,cid: <"+android.os.Process.myPid() + ","+ id + ","+callSiteID+">");
			int depth;
			if(id<Constant.THREAD_COUNT){
				depth = DepthInThreads[id].value;
			}else{
				synchronized(LoggingClassCompress.class){
					enlargeThreadNum(id);
				}
				depth = DepthInThreads[id].value;
			}

			long content = depth;
			content = (content << 32) + callSiteID;
			ThreadBuffer buffer = threadCaches[id];
			buffer.add(content);
			DepthInThreads[id].value = --depth;
		}catch(Exception e){
			Log.e("LOGGING", "Exception happened in methodExit(int) with the call site "+ callSiteID);
			Log.e("LOGGING", e.toString());
		}
	}


//	public static ThreadBuffer[] getThreadCaches(){
//		return threadCaches;
//	}

	/**
	 * enlarge the thread counter so that id < THREAD_COUNT-1
	 * @param id
	 */
	private static void enlargeThreadNum(int id){
		try{
			if(id<Constant.THREAD_COUNT-1){
				return;
			}
			int counter = 2* Constant.THREAD_COUNT;
			while(counter-1<id){
				counter = 2 * counter;
			}

			ThreadBuffer[] newThreadCaches = new ThreadBuffer[counter];
			Integer[] newDepthInThreads = new Integer[counter];
			Stack[] newImportantDepth = new Stack[counter];
			Integer[] newImportantIDs = new Integer[counter];
			for(int i=0; i< counter; i++){
				if(i<Constant.THREAD_COUNT){
					newThreadCaches[i] = threadCaches[i];
					newDepthInThreads[i] = DepthInThreads[i];
					newImportantDepth[i] = ImportantDepth[i];
					newImportantIDs[i] = ImportantIDs[i];
				}else{
					newThreadCaches[i] = new ThreadBuffer(Constant.BUFFER_SIZE);
					newThreadCaches[i].tid = i;
					newDepthInThreads[i] = new Integer(0);
					newImportantDepth[i] = new Stack();
					newImportantDepth[i].push(1);
					newImportantIDs[i] = new Integer(0);
				}
			}
			threadCaches = newThreadCaches;
			DepthInThreads = newDepthInThreads;
			ImportantDepth = newImportantDepth;
			ImportantIDs = newImportantIDs;		
			LoggingThreadCompress.loggingThread.enlargeThreadNumbers(id);
			Constant.THREAD_COUNT = counter;
		}catch(Exception e){
		}		
	}	
}
