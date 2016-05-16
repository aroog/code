package edu.wayne.ograph;

import java.util.List;

import ast.AstNode;
import ast.BaseTraceability;
import ast.BodyDeclaration;
import ast.ClassInstanceCreation;
import ast.FieldDeclaration;
import ast.MethodDeclaration;
import ast.Type;
import ast.TypeDeclaration;
import ast.VariableDeclaration;

/*
 *  AUString: a String representation for an AU.
 *  
 *  XXX. Should we also store a unique ID, i.e., ASTNode.hashCode()
 *  Since two AUStrings may be identical, but may correspond to different ASTNodes 
 *  
 */
public class AUString {
	private String name;
	private String kind;
	private String type;
	private String enclosingType;
	private String enclosingMethod;
	
	/**
	 * Store the expression as a string
	 */
	private String expression;
	
	public AUString() {
	    super();
    }
	
	public String getName() {
	    return name;
    }
	public void setName(String name) {
	    this.name = name;
    }
	public String getKind() {
	    return kind;
    }
	public void setKind(String kind) {
	    this.kind = kind;
    }
	public String getType() {
	    return type;
    }
	public void setType(String type) {
	    this.type = type;
    }
	public String getEnclosingType() {
	    return enclosingType;
    }
	public void setEnclosingType(String enclosingType) {
	    this.enclosingType = enclosingType;
    }
	public String getEnclosingMethod() {
	    return enclosingMethod;
    }
	public void setEnclosingMethod(String enclosingMethod) {
	    this.enclosingMethod = enclosingMethod;
    }
	

	// XXX. Move some of this code to MiniAST, In particular, MiniAstUtils; might be useful there.
	public static AUString createFrom(BaseTraceability trace) {
		AstNode expr = trace.getExpression();

		AUString au =  new AUString();
		au.expression = expr.toString();
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
				kind = "v";
			}
			else if (enclDecl instanceof FieldDeclaration) {
				FieldDeclaration fieldDecl = (FieldDeclaration) enclDecl;
				name = fieldDecl.fieldName;
				kind = "f";
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
						kind = "f";
						type = fieldDecl.fieldType;
						encMthdSig = "field";
					}
					//typeDecl = ((MethodDeclaration)enclosingDeclaration).enclosingType;
				}
				else {
					int debug = 0; debug++;
					name = varDecl.varName;
					type = varDecl.varType;
					kind = "v";
				}
			}
			
			// XXX. Need to populate the following on SplitUp so OOGRE can look it up:
			//- name;
			//- kind;
			//- type;
			//- enclosingType;			
			au.setName(name);
			au.setKind(kind);
			au.setType(type == null ? "" : type.getFullyQualifiedName());
			au.setEnclosingMethod(encMthdSig);
			au.setEnclosingType(typeDecl == null ? "" : typeDecl.getFullyQualifiedName());

			// XXX. HACK: Avoid this like the plague...
			// ASTNode astNode = MiniAstToEclipseAST.getASTNode(trace);
			// if (astNode != null ) {
			//
			// }
		}
		
		return au;
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

	public String getExpression() {
    	return expression;
    }

	public void setExpression(String expression) {
    	this.expression = expression;
    }
}
