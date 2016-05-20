package edu.wayne.metrics;

/**
 * Presents the interface for an analysis.
 * 
 * @author David Dickey
 * @author Jonathan Aldrich
 */
public interface ICrystalAnalysis extends INamed {
	/**
	 * The starting point of an anlaysis. Invoked whenever the project is (fully or partially) built.
	 */
	public void runAnalysis();
}
