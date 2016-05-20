package oog.ui.views;

import java.util.List;
import java.util.Set;

import oog.re.SplitUp;
import oogre.refinements.tac.AnnotateUnitEnum;

import org.eclipse.jdt.core.dom.ASTNode;

import ast.AstNode;
import ast.BaseTraceability;
import ast.BodyDeclaration;
import ast.ClassInstanceCreation;
import ast.FieldDeclaration;
import ast.MethodDeclaration;
import ast.Type;
import ast.TypeDeclaration;
import ast.VariableDeclaration;
import edu.wayne.tracing.actions.DeclarationVisitor;
import edu.wayne.tracing.actions.TraceToCodeUIAction;

/**
 * This is a DIRTY hack, just to pass data onto OOGRE 
 *
 */
public class MiniAstToEclipseAST {
	
	/**
	 * Use this method to convert from MiniAst to Eclipse AST
	 * 
	 * HACK: this method is horrible...just horrible...don't use it.
	 * Scans entire workspace. at each call.
	 * @param trace
	 * @return
	 */
	public static ASTNode getASTNode(BaseTraceability trace) {
		ASTNode retNode = null;
		
		TraceToCodeUIAction action = new TraceToCodeUIAction();
		TraceToCodeUIAction.setHighlightCode(false);
		action.setTraceability(trace);
		action.run(null);
		//Get the ArchTrace Enclosing Declaration visitor 
		//Finds an enclosing declaration of a BaseTraceability 
		DeclarationVisitor visitor = action.getVisitor();

		if (visitor != null) {
			ASTNode enclosingDeclaration = visitor.getEnclosingDeclaration();
			if (enclosingDeclaration != null) {

				//mapAST.put(enclosingDeclaration, trace.enclosingDeclaration);
				//TOAND: Do we really need to remember this?
			//TOAND:TODO we only filter out only the expressions MethodInvokations, FieldWrite, FieldRead, ClassInstanceCreation
			//TOAND: Can create sub strategies for dealing with different type of expressions
			Set<ASTNode> astNodes = visitor.getExpressions();
			
			if (astNodes != null) {
				for (ASTNode node : astNodes) {
					// Return just the first one
					retNode = node;
					break;
				}
			}
			visitor.getExpressions().clear();
			visitor = null;
			}
		}

		return retNode;
	}
	
	// XXX. Move some of this code to MiniAST, In particular, MiniAstUtils; might be useful there.
	public static void populateSplitUp(BaseTraceability trace, SplitUp splitUp) {
		AstNode expr = trace.getExpression();
		
		if (expr instanceof ClassInstanceCreation) {
			ClassInstanceCreation newExpr = (ClassInstanceCreation)expr;
			Type type = newExpr.type;
			BodyDeclaration enclDecl = newExpr.enclosingDeclaration;
			
			TypeDeclaration typeDecl = null;
			String name = "";
			String kind = "";
			String encMthdSig = "";
			
			if (enclDecl instanceof MethodDeclaration) {
				MethodDeclaration methDecl = (MethodDeclaration) enclDecl;
				typeDecl = methDecl.enclosingType;
				
    			encMthdSig = getSignature(methDecl);
				
				// XXX. How to figure out the name?
				// XXX. How to figure out the kind? could be V or R
				kind = AnnotateUnitEnum.v.toString();
			}
			else if (enclDecl instanceof FieldDeclaration) {
				FieldDeclaration fieldDecl = (FieldDeclaration) enclDecl;
				name = fieldDecl.fieldName;
				kind = AnnotateUnitEnum.f.toString();
				type = fieldDecl.fieldType;
				typeDecl = fieldDecl.enclosingType;
				encMthdSig = "field";
			}
			else if (enclDecl instanceof TypeDeclaration) {
				typeDecl = (TypeDeclaration) enclDecl;
			}
			else if (enclDecl instanceof VariableDeclaration) {
				VariableDeclaration varDecl = (VariableDeclaration) enclDecl;
				
				// Get enclosing type
				BodyDeclaration enclosingDeclaration = varDecl.enclosingDeclaration;
				if(enclosingDeclaration instanceof MethodDeclaration ) {
					typeDecl = ((MethodDeclaration)enclosingDeclaration).enclosingType;
					MethodDeclaration mthdDecl = (MethodDeclaration)enclosingDeclaration;
					encMthdSig = getSignature(mthdDecl);
				}
				if(enclosingDeclaration instanceof FieldDeclaration ) {
					FieldDeclaration fieldDecl = (FieldDeclaration)enclosingDeclaration;
					BodyDeclaration fEnclosingDeclaration = fieldDecl.enclosingDeclaration;
					if(fEnclosingDeclaration instanceof TypeDeclaration){
						typeDecl = (TypeDeclaration)fEnclosingDeclaration;
						name = fieldDecl.fieldName;
						kind = AnnotateUnitEnum.f.toString();
						type = fieldDecl.fieldType;
						encMthdSig = "field";
					}
					//typeDecl = ((MethodDeclaration)enclosingDeclaration).enclosingType;
				}
				else {
					int debug = 0; debug++;
					name = varDecl.varName;
					type = varDecl.varType;
					kind = AnnotateUnitEnum.v.toString();
				}
				
				
			}
			
			// XXX. Need to populate the following on SplitUp so OOGRE can look it up:
			//- name;
			//- kind;
			//- type;
			//- enclosingType;			
			splitUp.setName(name);
			splitUp.setKind(kind);
			splitUp.setType(type == null ? "" : type.getFullyQualifiedName());
			splitUp.setEnclosingMethod(encMthdSig);
			splitUp.setEnclosingType(typeDecl == null ? "" : typeDecl.getFullyQualifiedName());

			// XXX. HACK: Avoid this like the plague...
			// ASTNode astNode = MiniAstToEclipseAST.getASTNode(trace);
			// if (astNode != null ) {
			//
			// }
		}
		
	}

	// XXX. There is duplication with the above populateSplitUp. Move to AstUtils.
	// XXX. Maybe this should become the new BaseTraceability.toString()
    public static String toString(BaseTraceability trace) {
    	StringBuilder builder = new StringBuilder();
    	
    	AstNode expr = trace.getExpression();
    	
    	if (expr instanceof ClassInstanceCreation) {
    		ClassInstanceCreation newExpr = (ClassInstanceCreation)expr;
    		Type type = newExpr.type;
    		BodyDeclaration enclDecl = newExpr.enclosingDeclaration;
    		
    		TypeDeclaration typeDecl = null;
    		String name = "";
    		String kind = "";
    		
    		if (enclDecl instanceof MethodDeclaration) {
    			MethodDeclaration methDecl = (MethodDeclaration) enclDecl;
    			typeDecl = methDecl.enclosingType;
    			
    			// XXX. How to figure out the name?
    			// XXX. How to figure out the kind? could be V or R
    			kind = AnnotateUnitEnum.v.toString();
    		}
    		else if (enclDecl instanceof FieldDeclaration) {
    			FieldDeclaration fieldDecl = (FieldDeclaration) enclDecl;
    			name = fieldDecl.fieldName;
    			kind = AnnotateUnitEnum.f.toString();
    			type = fieldDecl.fieldType;
    			typeDecl = fieldDecl.enclosingType;
    			
    		}
    		else if (enclDecl instanceof TypeDeclaration) {
    			typeDecl = (TypeDeclaration) enclDecl;
    		}
    		else if (enclDecl instanceof VariableDeclaration) {
    			VariableDeclaration varDecl = (VariableDeclaration) enclDecl;
    			
    			// Get enclosing type
    			BodyDeclaration enclosingDeclaration = varDecl.enclosingDeclaration;
    			if(enclosingDeclaration instanceof MethodDeclaration ) {
    				typeDecl = ((MethodDeclaration)enclosingDeclaration).enclosingType;
    			}
    			if(enclosingDeclaration instanceof FieldDeclaration ) {
    				FieldDeclaration fieldDecl = (FieldDeclaration)enclosingDeclaration;
    				BodyDeclaration fEnclosingDeclaration = fieldDecl.enclosingDeclaration;
    				if(fEnclosingDeclaration instanceof TypeDeclaration){
    					typeDecl = (TypeDeclaration)fEnclosingDeclaration;
    					name = fieldDecl.fieldName;
    					kind = AnnotateUnitEnum.f.toString();
    					type = fieldDecl.fieldType;
    				}

    			}
    			else {
    				int debug = 0; debug++;
    				name = varDecl.varName;
    				type = varDecl.varType;
    				kind = AnnotateUnitEnum.v.toString();
    			}
    			
    		}

			builder.append(kind);
			builder.append(": ");
			builder.append(name);
			builder.append(":= ");
			builder.append(newExpr.toString());
    	}
    	
    	return builder.toString();
    }
    
    // XXX. This method duplicates code in OOGRE. Careful about changing...
	public static String getSignature(MethodDeclaration methodDeclaration) {
		StringBuilder builder = new StringBuilder();
		builder.append(methodDeclaration.methodName);
		builder.append(".");
		List<VariableDeclaration> parameters = methodDeclaration.parameters;
		for (VariableDeclaration singleVariableDeclaration : parameters) {
			builder.append(singleVariableDeclaration.varType.fullyQualifiedName);
			builder.append(".");
		}
		builder.deleteCharAt(builder.length()-1);
		return builder.toString();
	}
}
