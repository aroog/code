package moogrex.analysis;

import java.util.HashSet;
import java.util.Set;

import moogrex.plugin.Activator;
import oog.common.OGraphFacade;
import oogre.analysis.RefinementAnalysis;

import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.cmu.cs.crystal.AbstractCompilationUnitAnalysis;
import edu.cmu.cs.crystal.IAnalysisInput;
import edu.cmu.cs.crystal.IAnalysisReporter;
import edu.wayne.pointsto.PointsToAnalysis;

public class MotherAnalysis extends AbstractCompilationUnitAnalysis {

	public static final String CRYSTAL_NAME = "MOOGREX";

	private Set<ITypeRoot> compUnits = new HashSet<ITypeRoot>();
	
	private IAnalysisReporter savedReporter;
	private IAnalysisInput savedInput;
	
	@Override
    public String getName() {
	    return CRYSTAL_NAME;
    }

	@Override
    public void analyzeCompilationUnit(CompilationUnit d) {
    }
	
	
	@Override
    public void afterAllCompilationUnits() {
		Activator default1 = moogrex.plugin.Activator.getDefault();
		OGraphFacade facade = default1.getMotherFacade();
		facade.sayHello(CRYSTAL_NAME);
		
		// Add annotations
		RunChildAnalysis runOOGRE = new RunChildAnalysis();
		runOOGRE.setCompilationUnits(compUnits);
		runOOGRE.setInput(savedInput);
		runOOGRE.setReporter(savedReporter);
		runOOGRE.run(RefinementAnalysis.CRYSTAL_NAME);
		
		// Extract OOG
		if(facade.isInferenceSuccess()) {
			RunChildAnalysis extractOOG = new RunChildAnalysis();
			extractOOG.setCompilationUnits(compUnits);
			extractOOG.setInput(savedInput);
			extractOOG.setReporter(savedReporter);
			extractOOG.run(PointsToAnalysis.CRYSTAL_NAME);
		}
    }

	@Override
    public void runAnalysis(IAnalysisReporter reporter, IAnalysisInput input, ITypeRoot compUnit,
            CompilationUnit rootNode) {
		this.savedReporter = reporter;
		this.savedInput = input;
		
		// Save the compilation units to analyze; analyze them later
		this.compUnits.add(compUnit);
		
	    super.runAnalysis(reporter, input, compUnit, rootNode);

    }

}
