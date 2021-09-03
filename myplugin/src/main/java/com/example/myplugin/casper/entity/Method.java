package com.example.myplugin.casper.entity;

public class Method {
	/**
	 * method name which is full qualified name
	 * the format of method name is the same as the 
	 * the format in the bytecode
	 * e.g. [Ljava/lang/Object instead of java.lang.Object[] 
	 */
	private String methodName = "";
	/**
	 * whether the method is a method in library
	 */
	private boolean isLibrary = false;
	/**
	 * whether the method is an abstract method
	 */
	private boolean isAbstract = false;
	/**
	 *whether the method is a native method
	 */
	private boolean isNative = false;
	
	public boolean isNative() {
		return isNative;
	}
	public void setNative(boolean isNative) {
		this.isNative = isNative;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public boolean isLibrary() {
		return isLibrary;
	}
	public void setLibrary(boolean isLibrary) {
		this.isLibrary = isLibrary;
	}
	public boolean isAbstract() {
		return isAbstract;
	}
	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((methodName == null) ? 0 : methodName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Method other = (Method) obj;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Method [methodName=" + methodName + ", isLibrary=" + isLibrary
				+ ", isAbstract=" + isAbstract + ", isNative=" + isNative + "]";
	}
	
	
	
}
