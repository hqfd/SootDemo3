package com.example.myplugin.casper.android.instrument.util;

public class UnCaughtExceptionHandler implements
		Thread.UncaughtExceptionHandler {
	@Override
	/*
	 * Handling the thread that this exception happens
	 * 
	 * */
	public void uncaughtException(Thread t, Throwable e) {
		LoggingThreadCompress.loggingThread.exportAll(System.currentTimeMillis());		
	}
	
}