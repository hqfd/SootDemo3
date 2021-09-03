package com.example.myplugin.casper.android.instrument.util;

/**
 * Buffer to cache log data
 * It is an implementation of ring buffer cache
 * 
 * @author rongxin wu
 *
 */
public class ThreadBuffer implements Cloneable{	
	int lastPos = 0;
	boolean isCorrupted = false;
	long[] buffer = null;
	int size = 0;	
	public long tid = Long.MAX_VALUE;	
	
	
	
	public int getLastPos() {
		return lastPos;
	}

	public void setLastPos(int lastPos) {
		this.lastPos = lastPos;
	}

//	public int getStartPos() {
//		return startPos;
//	}
//
//	public void setStartPos(int startPos) {
//		this.startPos = startPos;
//	}

	public long[] getBuffer() {
		return buffer;
	}

	public boolean isCorrupted() {
		return isCorrupted;
	}

	public void setCorrupted(boolean isCorrupted) {
		this.isCorrupted = isCorrupted;
	}

	public ThreadBuffer(int initSize) {
		size = initSize;
		lastPos = 0;
	}
	
	public void add(long ID) {		
		synchronized (this) {
			try{
				buffer[lastPos] = ID;
				++lastPos;
				if(lastPos>=size){
					isCorrupted = true;
					lastPos=lastPos%size;
				}
			}catch(NullPointerException e){
				buffer = new long[size];
				buffer[lastPos] = ID;
				++lastPos;
				if(lastPos>=size){
					isCorrupted = true;
					lastPos=lastPos%size;
				}
			}
		}		
	}
	
	public void flush(){		
		lastPos = 0;
		isCorrupted = false;
		buffer = null;
//		buffer = new long[size];
	}
	
	public void clear(){
		buffer = null;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {		
		return super.clone();
	}
	
}