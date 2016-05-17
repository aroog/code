package edu.wayne.flowgraph;

public class FlowGraphEdge{


	FlowGraphNode src;
	FlowGraphNode dst;
	FlowAnnot annot;
	
	public FlowGraphEdge(FlowGraphNode src, FlowGraphNode dst, FlowAnnot annot) {
		this.src = src;
		this.dst = dst;
		this.annot = annot;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dst == null) ? 0 : dst.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FlowGraphEdge other = (FlowGraphEdge) obj;
		if (dst == null) {
			if (other.dst != null)
				return false;
		} else if (!dst.equals(other.dst))
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}


	public FlowGraphNode getDst() {
		return dst;
	}


	public FlowGraphNode getSrc() {
		return src;
	}


	@Override
	public String toString() {
		return "[" + src + "-> " + dst + ", " + annot + "]";
	}


	public boolean isCall() {
		return annot.value == FlowAnnotType.CALL;
	}

	public boolean isReturn() {
		return annot.value == FlowAnnotType.RETURN;
	}

	public boolean hasEmptyAnnot() {
		return annot.value == FlowAnnotType.EMPTY;
	}


	public boolean isStar() {		
		return annot.value == FlowAnnotType.STAR;
	}


	public String getLabel() {		
		return annot.toString();
	}



}
