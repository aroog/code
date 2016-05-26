package oogre.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

// TODO: Rename: Remove -> Erase
public class RemoveAnnotations extends ChangeAnnotationsBase  {

	protected void analyzeCompilationUnit(CompilationUnit unit, ICompilationUnit compilationUnit) {

		unit.recordModifications();
		IBuffer buffer = null;
		try {
			buffer = compilationUnit.getBuffer();
		}
		catch (JavaModelException e) {
			e.printStackTrace();
		}
		if (buffer == null) {
			return;
		}

		char[] bufferContents = buffer.getCharacters();
		String generatedSource = new String(bufferContents);
		Document document = new Document(generatedSource);

		ASTRewrite rewrite = ASTRewrite.create(unit.getAST());
		
		ListRewrite idListRewrite = rewrite.getListRewrite(unit, CompilationUnit.IMPORTS_PROPERTY);
		for(Iterator it = unit.imports().iterator();it.hasNext(); ) {
			ImportDeclaration importDeclaration = (ImportDeclaration) it.next();
			if ( importDeclaration.getName().toString().startsWith("edu.cmu.cs.aliasjava")) {
				idListRewrite.replace(importDeclaration, rewrite.getAST().newLineComment(), null);
			}
		}

		LocationVariableVisitor visitor = new LocationVariableVisitor(rewrite);
		unit.accept(visitor);

		List types = unit.types();
		for (Iterator iter = types.iterator(); iter.hasNext();) {

			Object next = iter.next();
			if (next instanceof TypeDeclaration) {
				TypeDeclaration declaration = (TypeDeclaration) next;
				reorderStuff(rewrite, declaration);
			}
		}

		TextEdit edits = rewrite.rewriteAST(document, null);
		try {
			edits.apply(document, TextEdit.NONE);
		}
		catch (MalformedTreeException e) {
			e.printStackTrace();
		}
		catch (org.eclipse.jface.text.BadLocationException e) {
			e.printStackTrace();
		}
		generatedSource = document.get();
		buffer.setContents(generatedSource);
		try {
	        buffer.save(null, true);
        }
        catch (JavaModelException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
		buffer.close();		
	}
	
	private ASTNode hasTypeAnnotation(List paramModifiers, String annotation) {
		ASTNode found = null;
		for (Iterator itParamModifiers = paramModifiers.iterator(); itParamModifiers.hasNext();) {
			Object o = itParamModifiers.next();
			if (o instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation annot = (SingleMemberAnnotation) o;
				String name = annot.getTypeName().toString();
				if (name.compareTo(annotation) == 0) {
					found = annot;
				}
			}
		}
		return found;
	}
	

	private void setParameterAnnotation(ASTRewrite rewrite, SingleVariableDeclaration param, String annotation) {
		SingleMemberAnnotation newParamAnnotation = param.getAST().newSingleMemberAnnotation();
		newParamAnnotation.setTypeName(rewrite.getAST().newSimpleName("Domain"));
		StringLiteral newStringLiteral = rewrite.getAST().newStringLiteral();
		newStringLiteral.setLiteralValue(annotation);
		newParamAnnotation.setValue(newStringLiteral);
		ListRewrite paramRewrite = rewrite.getListRewrite(param, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
		paramRewrite.insertFirst(newParamAnnotation, null);
	}

	private void reorderStuff(ASTRewrite rewrite, TypeDeclaration declaration) {

		TypeDeclaration[] nestedTypes = declaration.getTypes();
		for (int i = 0; i < nestedTypes.length; i++) {
			TypeDeclaration nestedType = nestedTypes[i];
			reorderStuff(rewrite, nestedType);
		}
		
		ASTNode domainsNode = hasTypeAnnotation(declaration.modifiers(), "Domains"); 
		if ( domainsNode != null ) {
			ListRewrite paramRewrite = rewrite.getListRewrite(declaration, TypeDeclaration.MODIFIERS2_PROPERTY);
			paramRewrite.replace(domainsNode, rewrite.getAST().newLineComment(), null);
		}

		ASTNode domainParamsNode = hasTypeAnnotation(declaration.modifiers(), "DomainParams");
		if ( domainParamsNode != null ) {
			ListRewrite paramRewrite = rewrite.getListRewrite(declaration, TypeDeclaration.MODIFIERS2_PROPERTY);
			paramRewrite.replace(domainParamsNode, rewrite.getAST().newLineComment(), null);
		}
		
		ASTNode domainInheritsNode = hasTypeAnnotation(declaration.modifiers(), "DomainInherits");
		if ( domainInheritsNode != null ) {
			ListRewrite paramRewrite = rewrite.getListRewrite(declaration, TypeDeclaration.MODIFIERS2_PROPERTY);
			paramRewrite.replace(domainInheritsNode, rewrite.getAST().newLineComment(), null);
		}
		
		ASTNode domainLinksNode = hasTypeAnnotation(declaration.modifiers(), "DomainLinks");
		if ( domainLinksNode != null ) {
			ListRewrite paramRewrite = rewrite.getListRewrite(declaration, TypeDeclaration.MODIFIERS2_PROPERTY);
			paramRewrite.replace(domainLinksNode, rewrite.getAST().newLineComment(), null);
		}

		ASTNode domainAssumesNode = hasTypeAnnotation(declaration.modifiers(), "DomainAssumes");
		if ( domainAssumesNode != null ) {
			ListRewrite paramRewrite = rewrite.getListRewrite(declaration, TypeDeclaration.MODIFIERS2_PROPERTY);
			paramRewrite.replace(domainAssumesNode, rewrite.getAST().newLineComment(), null);
		}

		MethodDeclaration[] methods = declaration.getMethods();
		for (int i = 0; i < methods.length; i++) {
			MethodDeclaration methodDeclaration = methods[i];

			Type returnType2 = methodDeclaration.getReturnType2();
			if (returnType2 != null) {
				ASTNode annotation = getAnnotation(methodDeclaration.modifiers(), "Domain");
				if (annotation != null) {
					ListRewrite paramRewrite = rewrite.getListRewrite(methodDeclaration,
					        MethodDeclaration.MODIFIERS2_PROPERTY);
					paramRewrite.remove(annotation, null);
				}
				ASTNode receiverAnnotation = getAnnotation(methodDeclaration.modifiers(), "DomainReceiver");
				if (receiverAnnotation != null) {
					ListRewrite paramRewrite = rewrite.getListRewrite(methodDeclaration,
					        MethodDeclaration.MODIFIERS2_PROPERTY);
					paramRewrite.replace(receiverAnnotation, rewrite.getAST().newLineComment(), null);
				}
				ASTNode methodParamsAnnotation = getAnnotation(methodDeclaration.modifiers(), "DomainParams");
				if (methodParamsAnnotation != null) {
					ListRewrite paramRewrite = rewrite.getListRewrite(methodDeclaration,
					        MethodDeclaration.MODIFIERS2_PROPERTY);
					paramRewrite.replace(methodParamsAnnotation, rewrite.getAST().newLineComment(), null);
				}
			}

			List parameters = methodDeclaration.parameters();
			for (Iterator itParams = parameters.iterator(); itParams.hasNext();) {
				SingleVariableDeclaration param = (SingleVariableDeclaration) itParams.next();

				ASTNode annotation2 = getAnnotation(param.modifiers(), "Domain");
				if (annotation2 != null) {
					ListRewrite paramRewrite = rewrite.getListRewrite(param,
					        SingleVariableDeclaration.MODIFIERS2_PROPERTY);
					paramRewrite.remove(annotation2, null);
				}
			}
			
			RemoveAnnotationsVisitor visitor = new RemoveAnnotationsVisitor();
			visitor.rewrite = rewrite;
			Block body = methodDeclaration.getBody();
			if ( body != null ) {
				body.accept(visitor);
			}
		}
		FieldDeclaration[] fieldDeclarations = declaration.getFields();
		for (int i = 0; i < fieldDeclarations.length; i++) {
			FieldDeclaration fieldDeclaration = fieldDeclarations[i];
			ASTNode annotation = getAnnotation(fieldDeclaration.modifiers(), "Domain");
			if (annotation != null) {
				ListRewrite paramRewrite = rewrite.getListRewrite(fieldDeclaration,
				        FieldDeclaration.MODIFIERS2_PROPERTY);
				paramRewrite.remove(annotation, null);
			}
		}
	}

	private void setFieldAnnotation(ASTRewrite rewrite, FieldDeclaration fieldDeclaration, String annotation) {
		SingleMemberAnnotation newFieldAnnotation = fieldDeclaration.getAST().newSingleMemberAnnotation();
		newFieldAnnotation.setTypeName(rewrite.getAST().newSimpleName("Domain"));
		StringLiteral newStringLiteral = rewrite.getAST().newStringLiteral();
		newStringLiteral.setLiteralValue(annotation);
		newFieldAnnotation.setValue(newStringLiteral);
		ASTNode modifier = getModifier(fieldDeclaration.modifiers());
		if (modifier != null) {
			ListRewrite paramRewrite = rewrite.getListRewrite(fieldDeclaration, FieldDeclaration.MODIFIERS2_PROPERTY);
			paramRewrite.insertAfter(newFieldAnnotation, modifier, null);
		}
		else {
			ListRewrite fieldRewrite = rewrite.getListRewrite(fieldDeclaration, FieldDeclaration.MODIFIERS2_PROPERTY);
			fieldRewrite.insertFirst(newFieldAnnotation, null);
		}

	}

	private void setReturnTypeAnnotation(ASTRewrite rewrite, MethodDeclaration methodDeclaration, String annotation) {
		SingleMemberAnnotation newParamAnnotation = methodDeclaration.getAST().newSingleMemberAnnotation();
		newParamAnnotation.setTypeName(rewrite.getAST().newSimpleName("Domain"));
		StringLiteral newStringLiteral = rewrite.getAST().newStringLiteral();
		newStringLiteral.setLiteralValue(annotation);
		newParamAnnotation.setValue(newStringLiteral);

		ASTNode modifier = getModifier(methodDeclaration.modifiers());
		if (modifier != null) {
			ListRewrite paramRewrite = rewrite.getListRewrite(methodDeclaration, MethodDeclaration.MODIFIERS2_PROPERTY);
			paramRewrite.insertAfter(newParamAnnotation, modifier, null);
		}
		else {
			ListRewrite paramRewrite = rewrite.getListRewrite(methodDeclaration, MethodDeclaration.MODIFIERS2_PROPERTY);
			paramRewrite.insertFirst(newParamAnnotation, null);
		}

	}

	private boolean hasAnnotation(List paramModifiers) {
		boolean found = false;
		for (Iterator itParamModifiers = paramModifiers.iterator(); itParamModifiers.hasNext();) {
			Object o = itParamModifiers.next();
			if (o instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation annot = (SingleMemberAnnotation) o;
				String name = annot.getTypeName().toString();
				if (name.compareTo("Domain") == 0) {
					found = true;
				}
			}
		}
		return found;
	}

	private ASTNode getAnnotation(List paramModifiers, String annotation) {
		ASTNode found = null;
		for (Iterator itParamModifiers = paramModifiers.iterator(); itParamModifiers.hasNext();) {
			IExtendedModifier o = (IExtendedModifier) itParamModifiers.next();
			if (o instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation annot = (SingleMemberAnnotation) o;

				String name = annot.getTypeName().toString();
				if (name.compareTo(annotation) == 0) {
					found = annot;
					break;
				}
			}
		}
		return found;
	}

	private ASTNode getModifier(List paramModifiers) {

		List<IExtendedModifier> copy = new ArrayList<IExtendedModifier>(paramModifiers);
		Collections.copy(copy, paramModifiers);

		Collections.sort(copy, new Comparator<IExtendedModifier>() {
			public int compare(IExtendedModifier o0, IExtendedModifier o1) {
				if (o0.isModifier() && o1.isAnnotation()) {
					return -1; // Modifier before annotation
				}
				else if (o0.isAnnotation() && o1.isModifier()) {
					return +1;
				}

				return 0;
			}
		});

		ASTNode found = null;
		int index = 0;
		for (Iterator itParamModifiers = copy.iterator(); itParamModifiers.hasNext();) {
			IExtendedModifier o = (IExtendedModifier) itParamModifiers.next();
			if (o.isAnnotation()) {
				break;
			}
			index++;
		}
		index--;
		if ((index >= 0) && (index < paramModifiers.size())) {
			found = (ASTNode) copy.get(index);
		}

		return found;
	}

	private class LocationVariableVisitor extends ASTVisitor {
		private ASTRewrite rewrite;

		public LocationVariableVisitor(ASTRewrite rewrite) {
			super(false); // don't visit ancestors
			this.rewrite = rewrite;
		}

		@Override
        public boolean visit(VariableDeclarationFragment param) {

			ASTNode parent = param.getParent();
			if (parent instanceof VariableDeclarationStatement) {
				VariableDeclarationStatement statement = (VariableDeclarationStatement) parent;

				ASTNode annotation = getAnnotation(statement.modifiers(), "Domain");
				if (annotation != null) {
					ListRewrite paramRewrite = rewrite.getListRewrite(statement,
					        VariableDeclarationStatement.MODIFIERS2_PROPERTY);
					paramRewrite.remove(annotation, null);
				}
			}
			return super.visit(param);
		}
	}

}
