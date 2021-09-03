package com.example.myplugin.casper.android.instrument.util;

public class Stack{
	private int size = 32;
	private int[] elements = null; 
//			new int[size];
	private int index = -1;
	
	public void push(int element){
		try{
			index++;
			elements[index] = element;			
		}catch(ArrayIndexOutOfBoundsException e){
			int newSize = size * 2;
			int[] newElements = new int[newSize];
			for(int i=0;i<size; i++){
				newElements[i] = elements[i];
			}
			size = newSize;
			elements = newElements;
		}catch(NullPointerException e){
			elements = new int[size];
			elements[index] = element;
		}
	}
	
	public int peek(){		
		return elements[index];		
	}
	
	public void pop(){
		if(index>=0){
			index--;
		}		
	}
	
}
