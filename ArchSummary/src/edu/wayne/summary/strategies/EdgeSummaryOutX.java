package edu.wayne.summary.strategies;
/**
 * Sub-Strategy to count only outgoing edges for MIC and MIRC but excludes self edges
 * @author Andrew
 *
 */
public class EdgeSummaryOutX extends EdgeSummaryOut {

	@Override
	protected boolean includeSelfEdges() {
		return false;
	}

}
