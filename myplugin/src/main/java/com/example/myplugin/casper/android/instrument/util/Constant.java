package com.example.myplugin.casper.android.instrument.util;

import android.os.Environment;

import java.io.File;

public class Constant {
	public static String PROPERTY_FILE_PATH = Environment.getExternalStorageDirectory() + File.separator + "casper_config.properties";
	public static String LOG_DIR = Environment.getExternalStorageDirectory() + File.separator + "LOG";
//	public static String LOG_DIR =  "LOG";
	public static int BUFFER_SIZE = 16*1024;
	public static int THREAD_COUNT = 4;
	public static long FILE_SIZE = 1024 * 1024;
//	public static long FILE_SIZE = 16;
	public static int LOG_FILE_NUMBER_PER_THREAD = 5;
//	public static int INITIALIZE_BUFFER_POOL_SIZE = 1024;
	public static long IO_FLUSH_TIME = 100;
	public static long TIME_INTERVAL_CLEAN_LOG = 5*60*1000; //5 minutes
//	public static int QUEUE_CAPACITY = 512;
//	public static int QUEUE_CAPACITY = 16 * 1024;
}