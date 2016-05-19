package edu.wayne.summary.strategies;

/**
 * Sub-Strategy of EdgeSummaryAll to exclude self edges 
 * @author Andrew
 *
 */
public class EdgeSummaryAllX extends EdgeSummaryAll {

	@Override
	protected boolean includeSelfEdges() {
		return false;
	}
	
}
