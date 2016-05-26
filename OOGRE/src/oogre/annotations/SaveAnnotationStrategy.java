package oogre.annotations;

import oogre.refinements.tac.TM;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

/**
 * TODO: HIGH. Use something better than String. But ultimately, need to save a "p<p1,p2,...>" annotation.
 * 
 * TODO: HIGH. May need to overload some of these methods to handle different AST Nodes:
 * - VariableDeclarationStatement, VariableDeclarationFragment, etc.
 */
public interface SaveAnnotationStrategy {

	String getAnnotationForFieldDeclaration(FieldDeclaration fieldDeclaration);
	
	String getAnnotationForMethodParameter(MethodDeclaration methDecl, SingleVariableDeclaration param);
	
	String getAnnotationForMethodReturn(MethodDeclaration methDecl);
	
	String getAnnotationForLocalVariable(VariableDeclarationFragment varDecl);

	// The TM from which we are saving annotations
	TM getTM();
	
}
