package com.example.myplugin.casper.entity;
public class EdgeInCallSiteCFG {
	/**
	 * the source of the edge in call site cfg
	 */
	private CallSite predecessor = null;
	/**
	 * the target of the edge in call site cfg
	 */
	private CallSite successor = null;
	public CallSite getPredecessor() {
		return predecessor;
	}
	public void setPredecessor(CallSite predecessor) {
		this.predecessor = predecessor;
	}
	public CallSite getSuccessor() {
		return successor;
	}
	public void setSuccessor(CallSite successor) {
		this.successor = successor;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((predecessor == null) ? 0 : predecessor.hashCode());
		result = prime * result
				+ ((successor == null) ? 0 : successor.hashCode());
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
		EdgeInCallSiteCFG other = (EdgeInCallSiteCFG) obj;
		if (predecessor == null) {
			if (other.predecessor != null)
				return false;
		} else if (!predecessor.equals(other.predecessor))
			return false;
		if (successor == null) {
			if (other.successor != null)
				return false;
		} else if (!successor.equals(other.successor))
			return false;
		return true;
	}
}