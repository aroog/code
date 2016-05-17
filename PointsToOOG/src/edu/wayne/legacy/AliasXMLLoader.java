package edu.wayne.legacy;

import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.cmu.cs.aliasxml.AliasXMLWrapper;
import edu.cmu.cs.crystal.AbstractCompilationUnitAnalysis;

public class AliasXMLLoader extends AbstractCompilationUnitAnalysis {

	@Override
	/**
	 * AliasXMLWrapper.reset() must be called at the very end, 
	 * once all analyses are completed, not just this one.
	 * Since this is a CompilationUnitAnalysis, using afterAllCompilationUnits will work.
	 */
    public void afterAllCompilationUnits() {
		// default does nothing
	    super.afterAllCompilationUnits();
	    
	    AliasXMLWrapper.reset();
    }

	@Override
    public void beforeAllCompilationUnits() {
	    super.beforeAllCompilationUnits();
	   
	    AliasXMLWrapper.loadAliasXML();
    }

	@Override
	public String getName() {
		return "AliasXML Loader";
	}

	@Override
	public void analyzeCompilationUnit(CompilationUnit compUnit) {
	}
}
