package moogrex.analysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.ITypeRoot;

import edu.cmu.cs.crystal.IAnalysisInput;
import edu.cmu.cs.crystal.IAnalysisReporter;
import edu.cmu.cs.crystal.IRunCrystalCommand;
import edu.cmu.cs.crystal.internal.AbstractCrystalPlugin;
import edu.cmu.cs.crystal.internal.Crystal;

public class RunChildAnalysis  {

	private IAnalysisReporter reporter;
	
	private IAnalysisInput input;
	
	private Collection<ITypeRoot> compilationUnits;

	public IAnalysisReporter getReporter() {
    	return reporter;
    }

	public void setReporter(IAnalysisReporter reporter) {
    	this.reporter = reporter;
    }

	public IAnalysisInput getInput() {
    	return input;
    }

	public void setInput(IAnalysisInput input) {
    	this.input = input;
    }

	public Collection<ITypeRoot> getCompilationUnits() {
    	return compilationUnits;
    }

	public void setCompilationUnits(Collection<ITypeRoot> compilationUnits) {
    	this.compilationUnits = compilationUnits;
    }
	
	public void run(final String analysis_to_run) {
		Crystal crystal = AbstractCrystalPlugin.getCrystalInstance();

		crystal.runAnalyses(new IRunCrystalCommand() {

			@Override
			public IAnalysisReporter reporter() {
				return reporter;
			}
			
			@Override
			public Collection<? extends ITypeRoot> compilationUnits() {
				return compilationUnits;
			}
			
			@Override
			public Set<String> analyses() {
				Set<String> hashSet = new HashSet<String>();
				hashSet.add(analysis_to_run);
				return hashSet;
			}
		}, null);
	}
	
	
}
