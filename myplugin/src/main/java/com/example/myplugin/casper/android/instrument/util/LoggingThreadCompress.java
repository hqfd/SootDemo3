package com.example.myplugin.casper.android.instrument.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class LoggingThreadCompress extends Thread {
	
	public static Lock logLock = new ReentrantLock();
	public static final LoggingThreadCompress loggingThread = new LoggingThreadCompress();
	private Integer fileSize[] = new Integer[Constant.THREAD_COUNT];
	private Integer fileIndex[] = new Integer[Constant.THREAD_COUNT];


	public synchronized void enlargeThreadNumbers(long id){
		if(id  < fileSize.length){
			return;
		}
		int counter = 2 * fileSize.length;
		while(counter-1<id){
			counter = 2 * counter;
		}
		try {
			Integer[] newFileSize = new Integer[counter];
			Integer[] newFileIndex = new Integer[counter];

			for(int i=0; i< counter; i++){
				if(i<fileSize.length){
					newFileSize[i] = fileSize[i];
					newFileIndex[i] = fileIndex[i];
				}else{
					newFileSize[i] = new Integer(0);
					newFileIndex[i] = new Integer(0);
				}
			}

			fileSize = newFileSize;
			fileIndex = newFileIndex;

		}catch(Exception e){

		}
	}

	public LoggingThreadCompress() {
		setDaemon(true);
		setPriority(MAX_PRIORITY);
		try {
			Log.i("LOGGING", "begin the logging thread.");
			Log.i("LOGGING", "the initial logging place:" + Constant.LOG_DIR);

			{
				Properties prop = new Properties();
				try{
					for(int i=0; i<fileSize.length;i++){
						fileSize[i] = new Integer(0);
						fileIndex[i] = new Integer(0);
					}
					
					FileInputStream inputStream = new FileInputStream(Constant.PROPERTY_FILE_PATH);
					prop.load(inputStream);		
					String logDir = prop.getProperty("LOG_DIR");
					if(logDir!=null && !logDir.equals("null")){
//						System.out.println(logDir);
						Constant.LOG_DIR = logDir;
					}
					String bufferSizeStr = prop.getProperty("BUFFER_SIZE");
					if(bufferSizeStr!=null&&!bufferSizeStr.equals("null")){
						try{
							int bufferSize = java.lang.Integer.parseInt(bufferSizeStr);
//							System.out.println(bufferSize);
							Constant.BUFFER_SIZE = bufferSize;
							Log.i("LOGGING", "initialize the buffer size as " + Constant.BUFFER_SIZE+"");
						}catch(Exception e){
							Log.e("LOGGING", e.toString());
						}
					}
					
					String fileSizeStr = prop.getProperty("FILE_SIZE");
					if(fileSizeStr!=null && !fileSizeStr.equals("null")){
						try{
							int fileSize = java.lang.Integer.parseInt(fileSizeStr);
//							System.out.println(fileSize);
							Constant.FILE_SIZE = fileSize;
							Log.i("LOGGING", "initialize the file size as "+ Constant.FILE_SIZE);
						}catch(Exception e){
							Log.e("LOGGING", e.toString());
						}
						
					}
					
					String logFileNumberPerThreadStr = prop.getProperty("LOG_FILE_NUMBER_PER_THREAD");
					if(logFileNumberPerThreadStr!=null && !logFileNumberPerThreadStr.equals("null")){
						try{
							int logFileNumberPerThread = java.lang.Integer.parseInt(logFileNumberPerThreadStr);
//							System.out.println(logFileNumberPerThread);
							Constant.LOG_FILE_NUMBER_PER_THREAD = logFileNumberPerThread;
							Log.i("LOGGING", "initialize the log file number per thread as "+ Constant.LOG_FILE_NUMBER_PER_THREAD);
						}catch(Exception e){
							Log.e("LOGGING", e.toString());
						}
					}
					
					String IOFlushTimeStr = prop.getProperty("IO_FLUSH_TIME");
					if(IOFlushTimeStr!=null && !IOFlushTimeStr.equals("null")){
						try{
							long IOFlushTime = Long.parseLong(IOFlushTimeStr);
//							System.out.println(IOFlushTime);
							Constant.IO_FLUSH_TIME = IOFlushTime;
							Log.i("LOGGING", "initialize IO flush time as "+ Constant.IO_FLUSH_TIME);
						}catch(Exception e){
							Log.e("LOGGING", e.toString());
						}
					}			
					
					String timeIntervalCleanLogStr = prop.getProperty("TIME_INTERVAL_CLEAN_LOG");
					if(timeIntervalCleanLogStr!=null && !timeIntervalCleanLogStr.equals("null")){
						try{
							long intervalCleanLog = Long.parseLong(timeIntervalCleanLogStr);
							Constant.TIME_INTERVAL_CLEAN_LOG = intervalCleanLog;
							Log.i("LOGGING", "initialize TIME_INTERVAL_CLEAN_LOG as "+ Constant.IO_FLUSH_TIME);
						}catch(Exception e){
							Log.e("LOGGING", e.toString());
						}
					}
					
				}catch(Exception e){
					Log.e("LOGGING", e.toString());
				}
			}
			Log.i("LOGGING", "after the initialization, the logging place is:" + Constant.LOG_DIR);
			
			File logDir = new File(Constant.LOG_DIR); 
			logDir.mkdirs();
//			//clean up directory
//			for(File subFile:logDir.listFiles()){
//				if(!subFile.isDirectory()&&subFile.getName().endsWith(".txt")){
//					try{
//						subFile.delete();
//					}catch(Exception e){
//						
//					}
//				}
//			}			
		} catch (Exception e) {
			Log.e("LOGGING", e.toString());
		}
	}
	@Override
	public void run() {
		try {
			while (true) {
				try {
					Thread.sleep(Constant.IO_FLUSH_TIME);
					logLock.lock();					
					for(int i=0; i<LoggingClassCompress.getThreadCaches().length; i++){						
						ThreadBuffer tBuffer = LoggingClassCompress.getThreadCaches()[i];						
						if(tBuffer.buffer!=null){
							ThreadBuffer copy = null;
							synchronized(tBuffer){		
								if(tBuffer.lastPos!=0 || tBuffer.isCorrupted){
									copy = (ThreadBuffer)tBuffer.clone();														
									tBuffer.flush();
								}								
							}
							if(copy!=null){
//								logToFile(copy);
								logToFileWithTimeStamp(copy, System.currentTimeMillis());
								//help gc to clean the memory
								copy.clear();
								copy = null;
							}
						}
					}
					clearProcessTraceFile(android.os.Process.myPid(), System.currentTimeMillis());
					
				} catch (Exception e) {
					Log.e("LOGGING", e.getMessage());
				} finally{
					logLock.unlock();
				}
			}
		} catch (Exception exception) {
			Log.e("LOGGING", exception.getMessage());
		}
	}
	
	public void logToFile(ThreadBuffer tBuffer){
		try{
			int id = (int)tBuffer.tid;
//			byte[] toWrite = transferLongArraytoBytes(tBuffer.buffer, tBuffer.startPos, tBuffer.lastPos);
			byte[] toWrite = transferLongArraytoBytes(tBuffer.buffer, tBuffer.lastPos, tBuffer.isCorrupted);
			int pid = android.os.Process.myPid();
			File logFile = new File(Constant.LOG_DIR+File.separator + pid + "_" + id+ "_" + fileIndex[id].value + ".txt");
			if(fileSize[id].value + toWrite.length <= Constant.FILE_SIZE){
				FileOutputStream fos = new FileOutputStream(logFile,true);
				fos.write(toWrite);
				fos.close();
				fileSize[id].value = fileSize[id].value + toWrite.length;
			}else{
				fileIndex[id].value++;
				fileSize[id].value = 0;
				logFile = new File(Constant.LOG_DIR+File.separator + pid + "_" + id +  "_" + fileIndex[id].value + ".txt");
				FileOutputStream fos = new FileOutputStream(logFile,true);
				fos.write(toWrite);
				fos.close();
				fileSize[id].value = fileSize[id].value + toWrite.length;
				if(fileIndex[id].value>Constant.LOG_FILE_NUMBER_PER_THREAD){
					(new File(Constant.LOG_DIR+File.separator + pid + "_"
						+ id +"_"+(fileIndex[id].value-Constant.LOG_FILE_NUMBER_PER_THREAD) + ".txt")).delete();
				}
			}
			toWrite = null;
		}catch(Exception e){
			Log.e("LOGGING", e.toString());
		}
	}
	
	public byte[] transferLongArraytoBytes(long[] temp, int lastPos, boolean isCorrupted){
		byte[] b = null;
		int index = 0;
		//we use 8 bytes to mark whether the log cache is corrupted
		//if the starting 8 bytes are 0, then it is not;
		//if the starting 8 bytes are -1, then it is corrupted.					
		if(!isCorrupted){
			b = new byte[8*(lastPos+2)];
			for(;index<16; index++){
				b[index] = 0;
			}
			for(int i=0; i!=lastPos; i++){
				long first = temp[i];
				b[index++] = ((byte) (first >>> 56));
				b[index++] = ((byte) (first >>> 48));
				b[index++] = ((byte) (first >>> 40));
				b[index++] = ((byte) (first >>> 32));
				b[index++] = ((byte) (first >>> 24));
				b[index++] = ((byte) (first >>> 16));
				b[index++] = ((byte) (first >>> 8));
				b[index++] = ((byte) (first >>> 0));
 			}
		}else{
			b = new byte[8*(temp.length+2)];
			for(;index<8; index++){
				b[index] = -1;
			}
			for(;index<16; index++){
				b[index] = 0;
			}

			for(int i=0; i < temp.length; i++){
				long l = temp[(i+lastPos)%(temp.length)];
				b[index++] = ((byte) (l >>> 56));
				b[index++] = ((byte) (l >>> 48));
				b[index++] = ((byte) (l >>> 40));
				b[index++] = ((byte) (l >>> 32));
				b[index++] = ((byte) (l >>> 24));
				b[index++] = ((byte) (l >>> 16));
				b[index++] = ((byte) (l >>> 8));
				b[index++] = ((byte) (l >>> 0));
			}
		}			
		return b;
	}
	
	public void logToFileWithTimeStamp(ThreadBuffer tBuffer, long timestamp){
		try{
			int id = (int)tBuffer.tid;
			int pid = android.os.Process.myPid();
			byte[] toWrite = 
				transferLongArraytoBytesWithTimeStamp(tBuffer.buffer, tBuffer.lastPos, 
					tBuffer.isCorrupted,timestamp);
			
			File logFile = new File(Constant.LOG_DIR+File.separator + pid + "_" + id+ "_" + fileIndex[id].value + ".txt");
			if(fileSize[id].value + toWrite.length <= Constant.FILE_SIZE){
				FileOutputStream fos = new FileOutputStream(logFile,true);
				fos.write(toWrite);
				fos.close();
				fileSize[id].value = fileSize[id].value + toWrite.length;
			}else{
				fileIndex[id].value++;
				fileSize[id].value = 0;
				logFile = new File(Constant.LOG_DIR+File.separator + pid + "_" + id+ "_" + fileIndex[id].value + ".txt");
				FileOutputStream fos = new FileOutputStream(logFile,true);
				fos.write(toWrite);
				fos.close();
				fileSize[id].value = fileSize[id].value + toWrite.length;
				if(fileIndex[id].value>Constant.LOG_FILE_NUMBER_PER_THREAD){
					(new File(Constant.LOG_DIR+File.separator + pid + "_"
						+ id +"_"+(fileIndex[id].value-Constant.LOG_FILE_NUMBER_PER_THREAD) + ".txt")).delete();
				}
			}
			toWrite = null;
		}catch(Exception e){
			Log.e("LOGGING", e.toString());
		}
	}
	
	public byte[] transferLongArraytoBytesWithTimeStamp(long[] temp, int lastPos, boolean isCorrupted, long timeStamp){
		byte[] b = null;
		int index = 0;
		
		//we use 8 bytes to mark whether the log cache is corrupted
		//if the starting 8 bytes are 0, then it is not;
		//if the starting 8 bytes are -1, then it is corrupted.					
		if(!isCorrupted){
			b = new byte[8*(lastPos+2)];
			for(;index<8; index++){
				b[index] = 0;
			}
			
			for(;index<16;){
				b[index++] = ((byte) (timeStamp >>> 56));
				b[index++] = ((byte) (timeStamp >>> 48));
				b[index++] = ((byte) (timeStamp >>> 40));
				b[index++] = ((byte) (timeStamp >>> 32));
				b[index++] = ((byte) (timeStamp >>> 24));
				b[index++] = ((byte) (timeStamp >>> 16));
				b[index++] = ((byte) (timeStamp >>> 8));
				b[index++] = ((byte) (timeStamp >>> 0));
			}
			
			for(int i=0; i!=lastPos; i++){
				long first = temp[i];
				b[index++] = ((byte) (first >>> 56));
				b[index++] = ((byte) (first >>> 48));
				b[index++] = ((byte) (first >>> 40));
				b[index++] = ((byte) (first >>> 32));
				b[index++] = ((byte) (first >>> 24));
				b[index++] = ((byte) (first >>> 16));
				b[index++] = ((byte) (first >>> 8));
				b[index++] = ((byte) (first >>> 0));
			}
		}else{
			b = new byte[8*(temp.length+2)];
			for(;index<8; index++){
				b[index] = -1;
			}
			for(;index<16;){
				b[index++] = ((byte) (timeStamp >>> 56));
				b[index++] = ((byte) (timeStamp >>> 48));
				b[index++] = ((byte) (timeStamp >>> 40));
				b[index++] = ((byte) (timeStamp >>> 32));
				b[index++] = ((byte) (timeStamp >>> 24));
				b[index++] = ((byte) (timeStamp >>> 16));
				b[index++] = ((byte) (timeStamp >>> 8));
				b[index++] = ((byte) (timeStamp >>> 0));
			}

			for(int i=0; i < temp.length; i++){
				long l = temp[(i+lastPos)%(temp.length)];
				b[index++] = ((byte) (l >>> 56));
				b[index++] = ((byte) (l >>> 48));
				b[index++] = ((byte) (l >>> 40));
				b[index++] = ((byte) (l >>> 32));
				b[index++] = ((byte) (l >>> 24));
				b[index++] = ((byte) (l >>> 16));
				b[index++] = ((byte) (l >>> 8));
				b[index++] = ((byte) (l >>> 0));
			}
		}
		return b;
	}
	
	public void exportAll(long timestamp){
		logLock.lock();	
		try{
			for(int i=0; i<LoggingClassCompress.getThreadCaches().length; i++){						
				ThreadBuffer tBuffer = LoggingClassCompress.getThreadCaches()[i];						
				if(tBuffer.buffer!=null){
					ThreadBuffer copy = null;
					synchronized(tBuffer){
						if(tBuffer.lastPos!=0 || tBuffer.isCorrupted){
							copy = (ThreadBuffer)tBuffer.clone();														
							tBuffer.flush();
						}
					}
					if(copy!=null){
						logToFileWithTimeStamp(copy,timestamp);
						copy.clear();
						copy = null;
					}						
				}
			}
			
			File logDir = new File(Constant.LOG_DIR);
			SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			String dateStr = dateFormatter.format(timestamp);			
			File backupDir = new File(Constant.LOG_DIR + File.separator + dateStr);
			backupDir.mkdirs();
			
			int pid = android.os.Process.myPid();
			//bakcup the execution trace in a directory
			for (File subFile : logDir.listFiles()) {
				if (!subFile.isDirectory() && subFile.getName().startsWith(pid+"_") && subFile.getName().endsWith(".txt")) {
					FileChannel inputChannel = null;
					FileChannel outputChannel = null;
					try {
						inputChannel = new FileInputStream(subFile).getChannel();
						outputChannel = new FileOutputStream(
								backupDir.getAbsolutePath() + File.separator + subFile.getName()).getChannel();
						outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
					} catch (Exception e) {
						Log.e("LOGGING", e.toString());
					} finally {
						if (inputChannel != null) {
							inputChannel.close();
						}
						if (outputChannel != null) {
							outputChannel.close();
						}
					}
				}
			}
			clearProcessTraceFile(pid);
		}catch(Exception e){
			Log.e("LOGGING", e.toString());
		}			
		logLock.unlock();	
	}
	
	public void clearProcessTraceFile(int pid, long checkPointTime){
		File logDir = new File(Constant.LOG_DIR + File.separator);
		try{
			for(File subFile:logDir.listFiles()){
				if(subFile.getName().startsWith(pid+"_")
						&& subFile.getName().endsWith(".txt")){
					try{
						long interval = checkPointTime - subFile.lastModified();
						if(interval>=Constant.TIME_INTERVAL_CLEAN_LOG){
							subFile.delete();
						}
					}catch(Exception e){
						
					}
				}
			}
		}catch(Exception e){
			Log.e("LOGGING", "cannot clean logged files");
		}
	}
	
	
	public void clearProcessTraceFile(int pid){
		File logDir = new File(Constant.LOG_DIR + File.separator);
		try{
			for(File subFile:logDir.listFiles()){
				if(subFile.getName().startsWith(pid+"_")
						&& subFile.getName().endsWith(".txt")){
					try{
						subFile.delete();
					}catch(Exception e){
						
					}
				}
			}
		}catch(Exception e){
			Log.e("LOGGING", "cannot clean logged files");
		}
		
	}
}