package oogre.analysis;

// TODO: Convert this to proper singleton
// TODO: Set these from user interface
// TODO: Move to MOOG, so ArchDoc can manipulate them?
// TODO: Why not combine this with Config?
public class OptionsForHeuristics {

	// These options are used inside the visitor to infer owned
	public static boolean enableHeuristicsOwnedFields = true;
	public static boolean enableHeuristicsOwnedLocals = true;
	
	public static boolean enableNearEncapsulationWarnings= false;
	
	public static boolean enableHeuristicsOwned = false;
	public static boolean enableHeuristicsPD = false;
}
