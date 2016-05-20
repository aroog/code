package edu.wayne.metrics;

import org.eclipse.jdt.core.dom.ASTNode;

public class UserProblemKey {

	private ASTNode node = null;

	private ICrystalAnalysis analysis = null;

	private String problemDescription = null;

	public UserProblemKey(ASTNode node, ICrystalAnalysis analysis, String problemDescription) {
		super();

		this.node = node;
		this.analysis = analysis;
		this.problemDescription = problemDescription;
	}

	// Implement value equality
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (!(o instanceof UserProblemKey)) {
			return false;
		}

		UserProblemKey key = (UserProblemKey) o;

		return (key.node == node) /*&& (key.analysis == analysis) && (key.problemDescription == problemDescription ) */;
	}

	// Always override hashcode when you override equals
	public int hashCode() {
		int result = 17;

		result = 37 * result + (node == null ? 0 : node.hashCode());
/*		result = 37 * result + (analysis == null ? 0 : analysis.hashCode());
		result = 37 * result + (problemDescription == null ? 0 : problemDescription.hashCode());

*/		return result;
	}

	public ICrystalAnalysis getAnalysis() {
		return analysis;
	}

	public ASTNode getNode() {
		return node;
	}

	public String getProblemDescription() {
		return problemDescription;
	}
}
