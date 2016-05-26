package oogre.actions;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.cmu.cs.crystal.internal.WorkspaceUtilities;

public abstract class ChangeAnnotationsBase {

	public void run() {
		List<ICompilationUnit> allCompilationUnits = WorkspaceUtilities.scanForCompilationUnits();
		for (ICompilationUnit compUnit : allCompilationUnits) {
			if (compUnit == null) {
				System.err.println("AbstractCompilationUnitAnalysis: null CompilationUnit");
				continue;
			}
			// Obtain the AST for this CompilationUnit and analyze it
			ASTNode node = WorkspaceUtilities.getASTNodeFromCompilationUnit(compUnit);
			if ((node != null) && (node instanceof CompilationUnit)) {
				analyzeCompilationUnit((CompilationUnit) node, compUnit);
			}
			else {
				System.err.println("AbstractCompilationUnitAnalysis: Could not retrieve the ASTNode for CompilationUnit "
				        + compUnit.getElementName());
			}
		}
	}

	abstract void analyzeCompilationUnit(CompilationUnit node, ICompilationUnit compUnit);
}
