package edu.wayne.summary.strategies;
/**
 * Sub-Strategy to count only incoming edges for MIC and MIRC but excludes self edges 
 * @author Andrew
 *
 */
public class EdgeSummaryInX extends EdgeSummaryIn {

	@Override
	protected boolean includeSelfEdges() {
		return false;
	}

}
