package edu.wayne.metrics.adb;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;

import edu.wayne.metrics.adb.OutputOptions.OutputEnum;
import edu.wayne.metrics.utils.CSVConst;

public abstract class MetricInfoBase implements MetricInfo {

	/**
	 * Have the subclasses initialize this protected field
	 */
	protected String shortName;

	/**
	 * By default, it is false
	 */
	protected boolean generateShortOutput = false;

	@Override
	public boolean generateShortOutput() {
		return generateShortOutput;
	}

	@Override
	public String getMetricName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getMetricShortName() {
		return shortName;
	}

	@Override
    public OutputOptions getOutputOptions() {
	    return new OutputOptions(OutputEnum.VERBOSE);
    }

	@Override
    public OutputOptions getOutputOptionsShort() {
	    return new OutputOptions(OutputEnum.SHORT);
    }

	@Override
	public String getFilePath(IPath location, String projectName, String postfix) {
		return location.append(projectName + "_" + getMetricName() + postfix).toOSString();
	}
	
	@Override
	public String getFilePathShort(IPath location, String projectName, String postfix) {
		return location.append(CSVConst.R_PREFIX + projectName + "_" + getMetricShortName() + postfix).toOSString();
	}

	public abstract void display(String tablePath) throws IOException;
	
	public abstract void displayShort(String tablePath) throws IOException;
	
	@Override
	public void display(IPath location, String projectName, String postfix) throws IOException{
		// Display the information as usual
		display(getFilePath(location, projectName, postfix));
		
		if (generateShortOutput() ) {
			displayShort(getFilePathShort(location, projectName, postfix));
		}
	}
}
