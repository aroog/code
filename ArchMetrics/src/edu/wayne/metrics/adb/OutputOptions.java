package edu.wayne.metrics.adb;

// TODO: Add multiple getters: show outliers, show statistics, etc.
public class OutputOptions {
	
	enum OutputEnum {VERBOSE, SHORT};
	
	private OutputEnum output = OutputEnum.VERBOSE;

	public OutputOptions(OutputEnum output) {
	    super();
	    this.output = output;
    }

}
