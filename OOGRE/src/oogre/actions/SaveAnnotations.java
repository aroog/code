package oogre.actions;

import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import oog.common.OGraphFacade;
import oog.re.CreateDomain;
import oog.re.IOtherRefinement;
import oog.re.RefinementModel;
import oog.re.RefinementState;
import oogre.analysis.Config;
import oogre.analysis.RefinementAnalysis;
import oogre.annotations.SaveAnnotationStrategy;
import oogre.refinements.tac.Refinement;
import oogre.utils.Utils;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

/**
 * This class is a bit big; move helpers to other utility classes.
 *
 * TODO: Use some adapters from metrics to output more context info about the AU
 * 
 *  NOTE: AU == every variable of a reference type that may take an annotation
 *  
 * TODO: Split tracking changes, diffs, metrics to separate class
 * XXX. The ordering is getting messed up...
 * XXX. Some method params are unused.
 * XXX. Add some breaks inside loops to speed things up
 *  
 */
public class SaveAnnotations extends ChangeAnnotationsBase {

	private static final boolean HARD_CODE_STATICS = false;
	
	private SaveAnnotationStrategy strategy;
	
	// XXX. In order to track changes, cannot call RemoveAnnotations first!
	// The previous annotation will be empty! This class must update the annotations in place.
	private static boolean TRACK_CHANGES = true;
	
	
	private static final String HEADER = "AU,OldAnnotation,NewAnnotation,HasChanged,AUType";
	
	private CustomWriter writer = null;

	private Refinement lastRefinement;
	
	private int numChanged = 0;

	private String path;

	private DecimalFormat twoDForm = new DecimalFormat("#");

	public SaveAnnotations(SaveAnnotationStrategy strategy) {
		super();
		
		this.strategy = strategy;
	}
	
	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 * @param lastRefinement 
	 */
	public void run(String path, Refinement lastRefinement) {
		this.path = path;
		this.lastRefinement = lastRefinement;
		
		// Call the superclass
		// XXX. Careful: this scans all the compilation units
		super.run();
		
		// The super class will call back on the analyzeCompilationUnit(...)
	}
	
	public void finish() {
		if (writer != null ) {
			writeFooter();
			writer.close();
		}
	}

	private void writeHeader() {
        // Add the header once
        writer.append(HEADER);
        writer.append(CSVConst.NEWLINE);
    }
	
	private void writeFooter() {
		writer.append(CSVConst.NEWLINE);
		writer.append(CSVConst.NEWLINE);
		
	    // DONE. Add the refinement being saved to the file header
	    if (lastRefinement != null ) {
	    	writer.append(CSVConst.NEWLINE);
	    	writer.append(lastRefinement.toString());
	    	writer.append(CSVConst.NEWLINE);
	    	writer.append(CSVConst.NEWLINE);
	    }
		
		// Store the total number of AUs:
		writer.append("AUs");
		writer.append(CSVConst.COMMA);
		int numAUs = strategy.getTM().getSize();
		writer.append(Integer.toString(numAUs));
		writer.append(CSVConst.NEWLINE);
		
		writer.append("AUsChanged");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.toString(numChanged));
		writer.append(CSVConst.NEWLINE);
		
		writer.append("AUsChangedPct");
		writer.append(CSVConst.COMMA);
		float pct = ((float)numChanged/(float)numAUs)*100;
		writer.append(twoDForm.format(pct));
		writer.append(CSVConst.NEWLINE);
    }

	// XXX. Extract constants
	private String getFileName() {
		StringBuilder builder = new StringBuilder();
		if (lastRefinement == null ) {
			builder.append(path);
			builder.append(Path.SEPARATOR);
			builder.append("REF_NONE.csv");	
		}
		else {
			// XXX. Remove hard-coded constants
			// String simpleName = lastRefinement.getClass().getSimpleName();
			builder.append(path);
			// XXX. Maybe path an IPath object to get the correct concatenation
			builder.append(Path.SEPARATOR);
			builder.append("REF_");
			//builder.append(simpleName);
			// DONE. Add hashcode, kinda like refinement.toString()
			// XXX. Add lastRefinement.toString() INSIDE the file, just to be clear
			// DONE. Avoid hashcode. Unlikely that the .toDisplayName representation is the same!
			// builder.append(lastRefinement.hashCode());
			// builder.append("_");
			// XXX. Get rid of this cast...after moving up to interface. Requires changes to MOOG.
			if(lastRefinement instanceof Refinement) {
				builder.append(((Refinement)lastRefinement).toDisplayName());
			}
			builder.append(".csv");
		}
		
		return builder.toString();
	}

	private CustomWriter getWriter() {
		if(writer == null ) {
			writer = new CustomWriter(getFileName());

	        writeHeader();
		}
		return writer;
	}

	@Override
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
		// generatedSource contains the source code of the classes
		String generatedSource = new String(bufferContents);
		Document document = new Document(generatedSource);

		ASTRewrite rewrite = ASTRewrite.create(unit.getAST());

		boolean hasImport = false;
		hasImport = hasImportDeclaration(unit);
		if (!hasImport) {
			ImportDeclaration newImportDeclaration = getImportDeclaration(rewrite);
			ListRewrite idListRewrite = rewrite.getListRewrite(unit, CompilationUnit.IMPORTS_PROPERTY);
			idListRewrite.insertLast(newImportDeclaration, null);
		}

		LocationVariableVisitor visitor = new LocationVariableVisitor(rewrite);
		unit.accept(visitor);

		List types = unit.types();
		for (Iterator iter = types.iterator(); iter.hasNext();) {

			Object next = iter.next();
			if (next instanceof TypeDeclaration) {
				// declaration: Contains one file content at a time.
				TypeDeclaration declaration = (TypeDeclaration) next;
				traverseType(rewrite, declaration);
				// reorderStuff(rewrite, declaration);
			}
		}

		// edit: contains all the insertion information (Added annotations)
		TextEdit edits = rewrite.rewriteAST(document, null);
		try {
			edits.apply(document, TextEdit.UPDATE_REGIONS);
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

	// XXX. Not enough to check this:
	// Will not handle situation of:
	// there is an import: edu.cmu.cs.aliasjava.annotations.DomainParams;
	// but need to add import: edu.cmu.cs.aliasjava.annotations.Domains;
	private boolean hasImportDeclaration(CompilationUnit unit) {
	    List imports = unit.imports();
		for(Iterator iter = imports.iterator(); iter.hasNext() ; ) {
			Object next = iter.next();
			if (next instanceof ImportDeclaration ) {
				ImportDeclaration importDecl = (ImportDeclaration)next;
				Name name = importDecl.getName();
				if (name instanceof QualifiedName ) {
					QualifiedName qName = (QualifiedName)name;
					String qNameString = qName.getFullyQualifiedName();
					if (qNameString.startsWith("edu.cmu.cs.aliasjava.annotations") ) {
						return true;
					}
				}
			}
		}
	    return false;
    }

	private ImportDeclaration getImportDeclaration(ASTRewrite rewrite) {
	    ImportDeclaration newImportDeclaration = rewrite.getAST().newImportDeclaration();
		newImportDeclaration.setOnDemand(true);
		newImportDeclaration.setName(rewrite.getAST().newName(new String[] { "edu", "cmu", "cs", "aliasjava", "annotations" }));
	    return newImportDeclaration;
    }

	private void traverseType(ASTRewrite rewrite, TypeDeclaration declaration) {

		TypeDeclaration[] nestedTypes = declaration.getTypes();
		for (int i = 0; i < nestedTypes.length; i++) {
			TypeDeclaration nestedType = nestedTypes[i];
			traverseType(rewrite, nestedType);
		}

		ASTNode domainInheritsNode = hasTypeAnnotation(declaration.modifiers(), "DomainInherits");
		if (domainInheritsNode == null) {
			setTypeAnnotationInherits(rewrite, declaration, "DomainInherits", "p", null);
		}
		else {
			updateTypeAnnotationInherits(rewrite, declaration, "DomainInherits", "p", domainInheritsNode);
		}

		ASTNode domainsNode = hasTypeAnnotation(declaration.modifiers(), "Domains");
		// XXX. do not generate @Domains annotations on interfaces!
//		if (!declaration.isInterface()) {
			if ( domainsNode == null ) { 
				setTypeAnnotationDomains(rewrite, declaration, "Domains", "owned", null);
			}
			else {
				updateTypeAnnotationDomains(rewrite, declaration, "Domains", "owned", null, domainsNode);
			}
//		}

		if (!declaration.resolveBinding().getQualifiedName().equals(Config.MAINCLASS)) {
			ASTNode domainParamsNode = hasTypeAnnotation(declaration.modifiers(), "DomainParams");
			if (domainParamsNode == null) {
				setTypeAnnotationParams(rewrite, declaration, "DomainParams", "p", domainsNode);
			}
			else {
				updateTypeAnnotationParams(rewrite, declaration, "DomainParams", "p", domainsNode, domainParamsNode);
			}
		}
		
		/*if (!declaration.resolveBinding().getQualifiedName().equals(Config.MAINCLASS)) {
			ASTNode domainAssumesNode = hasTypeAnnotation(declaration.modifiers(), "DomainAssumes");
			if (domainAssumesNode == null) {
				setTypeAnnotationDomainAssumes(rewrite, declaration, "DomainAssumes", "owner->p", null);
			}
			else {
				updateTypeAnnotationDomainAssumes(rewrite, declaration, "DomainAssumes", "owner->p", null, domainAssumesNode);
			}
		}
		else{
			ASTNode domainAssumesNode = hasTypeAnnotation(declaration.modifiers(), "DomainAssumes");
			if (domainAssumesNode == null) {
				setTypeAnnotationDomainAssumes(rewrite, declaration, "DomainAssumes", "owner->owner", null);
			}
			else {
				updateTypeAnnotationDomainAssumes(rewrite, declaration, "DomainAssumes", "owner->owner", null, domainAssumesNode);
			}
		}*/
		
		annotateMethods(rewrite, declaration);
		annotateFieldDeclarations(rewrite, declaration);
	}

	/**
	 * @param rewrite
	 * @param declaration
	 */
	private void annotateMethods(ASTRewrite rewrite, TypeDeclaration declaration) {
		MethodDeclaration[] methods = declaration.getMethods();
		for (int i = 0; i < methods.length; i++) {
			MethodDeclaration methodDeclaration = methods[i];

			annotateMethodReturnType(rewrite, methodDeclaration);
			annotateMethodParameters(rewrite, methodDeclaration);

			DefaultingVisitor visitor = new DefaultingVisitor();
			visitor.rewrite = rewrite;
			Block body = methodDeclaration.getBody();
			if (body != null) {
				body.accept(visitor);
			}
		}
	}

	/**
	 * @param rewrite
	 * @param methodDeclaration
	 */
	private void annotateMethodParameters(ASTRewrite rewrite, MethodDeclaration methodDeclaration) {
		
		// Method parameters
		List parameters = methodDeclaration.parameters();
		for (Iterator itParams = parameters.iterator(); itParams.hasNext();) {
			SingleVariableDeclaration param = (SingleVariableDeclaration) itParams.next();

			ITypeBinding type = param.resolveBinding().getType();
			if (!type.isPrimitive()) {
				
				// Use default
				String annotation = "";
				
				if (HARD_CODE_STATICS && Modifier.isStatic(methodDeclaration.getModifiers())) {
					String paramType = param.getType().resolveBinding().getQualifiedName();
					// HACK to handle main method
					if( paramType.equals("java.lang.String[]")) {
							annotation = "lent[shared]";
					}
					else 
						annotation = "shared<shared>";
				}
				else {
					annotation = strategy.getAnnotationForMethodParameter(methodDeclaration, param);
					
					// HACK: HACK to handle String[] for javad (until we get past the initial hurdles)
					String paramType = param.getType().resolveBinding().getQualifiedName();
					if( paramType.equals("java.lang.String[]") && param.getName().toString().equals("args")) {
							annotation = "lent[shared]";
					}

				}
				
				SingleMemberAnnotation currentAnnotation = getCurrentAnnotation(param.modifiers()); 
				if(currentAnnotation == null ) {
					setParameterAnnotation(rewrite, param, annotation);
				}
				else {
					updateParameterAnnotation(rewrite, param, annotation, currentAnnotation);
				}		
				
				String srcType = type.getQualifiedName();
				String dstType = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName();
				// XXX. Come up with a better key
				trackChanges(param.toString(), annotation, getValue(currentAnnotation), CSVConst.METHOD_PARAM, srcType, dstType);
			}
		}
	}

	/**
	 * @param rewrite
	 * @param methodDeclaration
	 */
	private void annotateMethodReturnType(ASTRewrite rewrite, MethodDeclaration methodDeclaration) {
		Type returnType2 = methodDeclaration.getReturnType2();
		if (returnType2 != null) {
			ITypeBinding resolveBinding = returnType2.resolveBinding();
			if ( !resolveBinding.isPrimitive()) {
				String annotation = "";
				if (HARD_CODE_STATICS && Modifier.isStatic(methodDeclaration.getModifiers())) {
					annotation = "shared<shared>"; // only thing a static can be
				}
				else {
					annotation = strategy.getAnnotationForMethodReturn(methodDeclaration);
				}
				
				SingleMemberAnnotation currentAnnotation = getCurrentAnnotation(methodDeclaration.modifiers());
				if(currentAnnotation == null ) {
					setReturnTypeAnnotation(rewrite, methodDeclaration, annotation);
				}
				else {
					updateReturnTypeAnnotation(rewrite, methodDeclaration, annotation, currentAnnotation);
				}		
				
				String srcType = resolveBinding.getQualifiedName();
				String dstType = methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName();
				// XXX. Come up with a better key
				trackChanges(returnType2.toString(), annotation, getValue(currentAnnotation), CSVConst.METHOD_RETURN, srcType, dstType);
			}
		}
	}

	private String getValue(SingleMemberAnnotation currentAnnotation) {
	    String value = "";
	    if (currentAnnotation != null ) {
	    	Expression value2 = currentAnnotation.getValue();
	    	if (value2 instanceof StringLiteral ) {
	    		return ((StringLiteral)value2).getLiteralValue();
	    	}
	    }
	    return value;
    }

	private void annotateFieldDeclarations(ASTRewrite rewrite, TypeDeclaration declaration) {

		FieldDeclaration[] fieldDeclarations = declaration.getFields();
		for (int i = 0; i < fieldDeclarations.length; i++) {
			FieldDeclaration fieldDeclaration = fieldDeclarations[i];
			ITypeBinding type = fieldDeclaration.getType().resolveBinding();

			if (!type.isPrimitive()) {
				String annotation = "";

				if (HARD_CODE_STATICS && Modifier.isStatic(fieldDeclaration.getModifiers())) {
					// XXX. Avoid unique fields. Use shared. 
					annotation = "shared<shared>";
				}
				else {
					annotation = strategy.getAnnotationForFieldDeclaration(fieldDeclaration);
				}

				SingleMemberAnnotation currentAnnotation = getCurrentAnnotation(fieldDeclaration.modifiers());
				if (currentAnnotation == null) {
					setFieldAnnotation(rewrite, fieldDeclaration, annotation);
				}
				else {
					updateFieldAnnotation(rewrite, fieldDeclaration, annotation, currentAnnotation);
				}		
				
				String srcType = type.getQualifiedName();
				String dstType = declaration.getName().getFullyQualifiedName();
				// XXX. Come up with a better key
				trackChanges(fieldDeclaration.toString(), annotation, getValue(currentAnnotation), CSVConst.FIELD, srcType, dstType);
			}
		}
	}

	private SingleMemberAnnotation setParameterAnnotation(ASTRewrite rewrite, SingleVariableDeclaration param, String annotation) {
		SingleMemberAnnotation newParamAnnotation = getParameterAnnotation(rewrite, param, annotation);
		ListRewrite paramRewrite = rewrite.getListRewrite(param, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
		paramRewrite.insertFirst(newParamAnnotation, null);
		
		return newParamAnnotation;
	}
	
	private void updateParameterAnnotation(ASTRewrite rewrite, SingleVariableDeclaration param, String annotation, SingleMemberAnnotation currentAnnotation) {
		SingleMemberAnnotation newParamAnnotation = getParameterAnnotation(rewrite, param, annotation);
		ListRewrite paramRewrite = rewrite.getListRewrite(param, SingleVariableDeclaration.MODIFIERS2_PROPERTY);
		paramRewrite.replace(currentAnnotation, newParamAnnotation, null);
	}

	private SingleMemberAnnotation getParameterAnnotation(ASTRewrite rewrite, SingleVariableDeclaration param,
            String annotation) {
	    SingleMemberAnnotation newParamAnnotation = param.getAST().newSingleMemberAnnotation();
		newParamAnnotation.setTypeName(rewrite.getAST().newSimpleName("Domain"));
		StringLiteral newStringLiteral = rewrite.getAST().newStringLiteral();
		newStringLiteral.setLiteralValue(annotation);
		newParamAnnotation.setValue(newStringLiteral);
	    return newParamAnnotation;
    }	

	private void reorderStuff(ASTRewrite rewrite, TypeDeclaration declaration) {
		TypeDeclaration[] nestedTypes = declaration.getTypes();
		for (int i = 0; i < nestedTypes.length; i++) {
			TypeDeclaration nestedType = nestedTypes[i];
			reorderStuff(rewrite, nestedType);
		}
		MethodDeclaration[] methods = declaration.getMethods();
		for (int i = 0; i < methods.length; i++) {
			MethodDeclaration methodDeclaration = methods[i];
			Type returnType2 = methodDeclaration.getReturnType2();
			if (returnType2 != null) {
				ASTNode annotation = getAnnotation(methodDeclaration.modifiers());
				ASTNode modifier = getModifier(methodDeclaration.modifiers());
				if ((annotation != null) && (modifier != null)) {
					ListRewrite paramRewrite = rewrite.getListRewrite(methodDeclaration,
					        MethodDeclaration.MODIFIERS2_PROPERTY);
					paramRewrite.remove(annotation, null);
					paramRewrite.insertAfter(annotation, modifier, null);
				}
			}
		}
		FieldDeclaration[] fieldDeclarations = declaration.getFields();
		for (int i = 0; i < fieldDeclarations.length; i++) {
			FieldDeclaration fieldDeclaration = fieldDeclarations[i];
			ASTNode annotation = getAnnotation(fieldDeclaration.modifiers());
			ASTNode modifier = getModifier(fieldDeclaration.modifiers());
			if ((annotation != null) && (modifier != null)) {
				ListRewrite paramRewrite = rewrite.getListRewrite(fieldDeclaration,
				        FieldDeclaration.MODIFIERS2_PROPERTY);
				paramRewrite.remove(annotation, null);
				paramRewrite.insertAfter(annotation, modifier, null);
			}
		}
	}

	private SingleMemberAnnotation setFieldAnnotation(ASTRewrite rewrite, FieldDeclaration fieldDeclaration, String annotation) {
		SingleMemberAnnotation newFieldAnnotation = getFieldAnnotation(rewrite, fieldDeclaration, annotation);
		ASTNode modifier = getModifier(fieldDeclaration.modifiers());
		if (modifier != null) {
			ListRewrite paramRewrite = rewrite.getListRewrite(fieldDeclaration, FieldDeclaration.MODIFIERS2_PROPERTY);
			paramRewrite.insertAfter(newFieldAnnotation, modifier, null);
		}
		else {
			ListRewrite fieldRewrite = rewrite.getListRewrite(fieldDeclaration, FieldDeclaration.MODIFIERS2_PROPERTY);
			fieldRewrite.insertFirst(newFieldAnnotation, null);
		}
		
		return newFieldAnnotation;
	}

	private void updateFieldAnnotation(ASTRewrite rewrite, FieldDeclaration fieldDeclaration, String annotation, SingleMemberAnnotation currentAnnotation) {
		SingleMemberAnnotation newFieldAnnotation = getFieldAnnotation(rewrite, fieldDeclaration, annotation);
		ASTNode modifier = getModifier(fieldDeclaration.modifiers());
		if (modifier != null) {
			ListRewrite paramRewrite = rewrite.getListRewrite(fieldDeclaration, FieldDeclaration.MODIFIERS2_PROPERTY);
			paramRewrite.replace(currentAnnotation, newFieldAnnotation, null);
		}
		else {
			ListRewrite fieldRewrite = rewrite.getListRewrite(fieldDeclaration, FieldDeclaration.MODIFIERS2_PROPERTY);
			fieldRewrite.replace(currentAnnotation, newFieldAnnotation, null);
		}
	}

	private SingleMemberAnnotation getFieldAnnotation(ASTRewrite rewrite, FieldDeclaration fieldDeclaration,
	        String annotation) {
		SingleMemberAnnotation newFieldAnnotation = fieldDeclaration.getAST().newSingleMemberAnnotation();
		newFieldAnnotation.setTypeName(rewrite.getAST().newSimpleName("Domain"));
		StringLiteral newStringLiteral = rewrite.getAST().newStringLiteral();
		newStringLiteral.setLiteralValue(annotation);
		newFieldAnnotation.setValue(newStringLiteral);
		return newFieldAnnotation;
	}

	private void setTypeAnnotationDomains(ASTRewrite rewrite, TypeDeclaration typeDeclaration, String annotation,
	        String domainName, ASTNode after) {

		SingleMemberAnnotation newParamAnnotation = getTypeAnnotationDomain(rewrite,
		        typeDeclaration,
		        annotation,
		        domainName);

		ListRewrite paramRewrite = rewrite.getListRewrite(typeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY);
		if (after == null) {
			paramRewrite.insertFirst(newParamAnnotation, null);
		}
		else {
			paramRewrite.insertAfter(newParamAnnotation, after, null);
		}
	}
	
	private void setTypeAnnotationDomainAssumes(ASTRewrite rewrite, TypeDeclaration typeDeclaration, String annotation, String domainName, ASTNode after) {

		SingleMemberAnnotation newParamAnnotation = getTypeAnnotationDomainAssumes(rewrite,typeDeclaration, annotation, domainName);

		ListRewrite paramRewrite = rewrite.getListRewrite(typeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY);
		if (after == null) {
			paramRewrite.insertFirst(newParamAnnotation, null);
		}
		else {
			paramRewrite.insertAfter(newParamAnnotation, after, null);
		}
	}
	
	
	private void updateTypeAnnotationDomains(ASTRewrite rewrite, TypeDeclaration typeDeclaration, String annotation,
	        String domainName, ASTNode after, ASTNode current) {

		SingleMemberAnnotation newParamAnnotation = getTypeAnnotationDomain(rewrite, typeDeclaration, annotation, domainName);

		ListRewrite paramRewrite = rewrite.getListRewrite(typeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY);
		paramRewrite.replace(current, newParamAnnotation, null);
	}
	
	private void updateTypeAnnotationDomainAssumes(ASTRewrite rewrite, TypeDeclaration typeDeclaration, String annotation,
	        String domainName, ASTNode after, ASTNode current) {

		SingleMemberAnnotation newParamAnnotation = getTypeAnnotationDomainAssumes(rewrite, typeDeclaration, annotation, domainName);

		ListRewrite paramRewrite = rewrite.getListRewrite(typeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY);
		paramRewrite.replace(current, newParamAnnotation, null);
	}

	// XXX. Do this more efficiently...
	// - Hash the other refinements: Type -> {Refinements}
	// XXX. Check we are not declaring the same domain multiple times!
	// XXX. Decouple this; return an array of strings, then modify AST elsewhere...
	// XXX. Need to set the status to Completed...and process only Pending...or do we need to re-apply this?
	private void declareOtherDomains(ASTRewrite rewrite, ListRewrite listRewrite, TypeDeclaration typeDeclaration) {
		String qualifiedName = typeDeclaration.resolveBinding().getQualifiedName();
		
		OGraphFacade facade = RefinementAnalysis.getFacade();
		RefinementModel refinementModel = facade.getRefinementModel();
		List<IOtherRefinement> otherRefinements = refinementModel.getOtherRefinements();
		for(IOtherRefinement otherRefinement : otherRefinements ) {
			if (otherRefinement instanceof CreateDomain ) {
				CreateDomain createDomain = (CreateDomain)otherRefinement;
				String fullyQualifiedName = createDomain.getSrcIObject().getC().getFullyQualifiedName();
				if (fullyQualifiedName.equals(qualifiedName)) {
					 StringLiteral newStringLiteralPARAM = rewrite.getAST().newStringLiteral();
					 newStringLiteralPARAM.setLiteralValue(createDomain.getDstDomain());
					 listRewrite.insertLast(newStringLiteralPARAM, null);
				}
			}
		}
				
	}
	private SingleMemberAnnotation getTypeAnnotationDomain(ASTRewrite rewrite, TypeDeclaration typeDeclaration,
            String annotation, String domainName) {
	    ArrayInitializer initializer = rewrite.getAST().newArrayInitializer();

		if (domainName != null) {
			ListRewrite listRewrite = rewrite.getListRewrite(initializer, ArrayInitializer.EXPRESSIONS_PROPERTY);
			
			StringLiteral newStringLiteral = rewrite.getAST().newStringLiteral();
			newStringLiteral.setLiteralValue(domainName);
			listRewrite.insertFirst(newStringLiteral, null);

			// XXX. HACK: Add hard-coded public domain, PD
			StringLiteral newStringLiteralPD = rewrite.getAST().newStringLiteral();
			newStringLiteralPD.setLiteralValue("PD");
			listRewrite.insertLast(newStringLiteralPD, null);

			// Add other domains that are created using other refinements
			declareOtherDomains(rewrite, listRewrite, typeDeclaration);
			
			// XXX. HACK: Add another hard-coded domain, PARAM. NO. 
			// StringLiteral newStringLiteralPARAM = rewrite.getAST().newStringLiteral();
			// newStringLiteralPARAM.setLiteralValue("PARAM");
			// listRewrite.insertLast(newStringLiteralPARAM, null);
		}

		SingleMemberAnnotation newParamAnnotation = typeDeclaration.getAST().newSingleMemberAnnotation();
		newParamAnnotation.setTypeName(rewrite.getAST().newSimpleName(annotation));
		newParamAnnotation.setValue(initializer);
	    return newParamAnnotation;
    }
	
	private SingleMemberAnnotation getTypeAnnotationDomainAssumes(ASTRewrite rewrite, TypeDeclaration typeDeclaration, String annotation, String domainName) {
	    ArrayInitializer initializer = rewrite.getAST().newArrayInitializer();

		if (domainName != null) {
			ListRewrite listRewrite = rewrite.getListRewrite(initializer, ArrayInitializer.EXPRESSIONS_PROPERTY);
			
			StringLiteral newStringLiteral = rewrite.getAST().newStringLiteral();
			newStringLiteral.setLiteralValue(domainName);
			listRewrite.insertFirst(newStringLiteral, null);
			
			declareOtherDomains(rewrite, listRewrite, typeDeclaration);
		}

		SingleMemberAnnotation newParamAnnotation = typeDeclaration.getAST().newSingleMemberAnnotation();
		newParamAnnotation.setTypeName(rewrite.getAST().newSimpleName(annotation));
		newParamAnnotation.setValue(initializer);
	    return newParamAnnotation;
    }
	
	// XXX. Do not call on the MainClass
	private void setTypeAnnotationParams(ASTRewrite rewrite, TypeDeclaration typeDeclaration, String annotation,
	        String domainName, ASTNode after) {

		SingleMemberAnnotation newAnnotation = getTypeAnnotationParams(rewrite, typeDeclaration, annotation);

		ListRewrite paramRewrite = rewrite.getListRewrite(typeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY);
		if (after == null) {
			paramRewrite.insertFirst(newAnnotation, null);
		}
		else {
			paramRewrite.insertAfter(newAnnotation, after, null);
		}
	}
	
	private void updateTypeAnnotationParams(ASTRewrite rewrite, TypeDeclaration typeDeclaration, String annotation,
	        String domainName, ASTNode after, ASTNode current) {

		SingleMemberAnnotation newAnnotation = getTypeAnnotationParams(rewrite, typeDeclaration, annotation);

		ListRewrite paramRewrite = rewrite.getListRewrite(typeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY);
		paramRewrite.replace(current, newAnnotation, null);
	}

	private SingleMemberAnnotation getTypeAnnotationParams(ASTRewrite rewrite, TypeDeclaration typeDeclaration,
            String annotation) {
	    ArrayInitializer initializer = rewrite.getAST().newArrayInitializer();

		if (!typeDeclaration.resolveBinding().getQualifiedName().equals(Config.MAINCLASS)) {
			StringLiteral newStringLiteral = rewrite.getAST().newStringLiteral();
			newStringLiteral.setLiteralValue("p");

			ListRewrite listRewrite = rewrite.getListRewrite(initializer, ArrayInitializer.EXPRESSIONS_PROPERTY);
			listRewrite.insertFirst(newStringLiteral, null);
		}
		SingleMemberAnnotation newParamAnnotation = typeDeclaration.getAST().newSingleMemberAnnotation();
		newParamAnnotation.setTypeName(rewrite.getAST().newSimpleName(annotation));
		newParamAnnotation.setValue(initializer);
	    return newParamAnnotation;
    }
	
	private void setTypeAnnotationInherits(ASTRewrite rewrite, TypeDeclaration typeDeclaration, String annotation,
	        String domainName, ASTNode after) {

		SingleMemberAnnotation newParamAnnotation = getTypeAnnotationInherits(rewrite, typeDeclaration, annotation, domainName);

		if (newParamAnnotation != null ) {
			ListRewrite paramRewrite = rewrite.getListRewrite(typeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY);
			if (after == null) {
				paramRewrite.insertFirst(newParamAnnotation, null);
			}
			else {
				paramRewrite.insertAfter(newParamAnnotation, after, null);
			}
		}
	}

	private void updateTypeAnnotationInherits(ASTRewrite rewrite, TypeDeclaration typeDeclaration, String annotation,
	        String domainName, ASTNode after) {

		SingleMemberAnnotation newAnnotation = getTypeAnnotationInherits(rewrite, typeDeclaration, annotation, domainName);

		if (newAnnotation != null ) {
			ListRewrite paramRewrite = rewrite.getListRewrite(typeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY);
			paramRewrite.replace(after, newAnnotation, null);
		}
	}
	private SingleMemberAnnotation getTypeAnnotationInherits(ASTRewrite rewrite, TypeDeclaration typeDeclaration,
            String annotation, String domainName) {
		boolean display = false;
	    ArrayInitializer initializer = rewrite.getAST().newArrayInitializer();
		ListRewrite listRewrite = rewrite.getListRewrite(initializer, ArrayInitializer.EXPRESSIONS_PROPERTY);
		List localSuperClasses = getAllSuperTypes(typeDeclaration);
		if (!localSuperClasses.isEmpty()) {
			int i = 0;
			for (Iterator iterSuperClass = localSuperClasses.iterator(); iterSuperClass.hasNext();) {
				Type tt = (Type) iterSuperClass.next();
				if (tt != null) {
					StringBuilder builder = new StringBuilder();
					// Use simple name rather than fully qualified type name. Typechecker does not expect qualified name.
					builder.append(Signature.getSimpleName(tt.toString()));
					builder.append("<");
					builder.append(domainName);
					builder.append(">");

					StringLiteral newStringLiteral = rewrite.getAST().newStringLiteral();
					newStringLiteral.setLiteralValue(builder.toString());

					listRewrite.insertAt(newStringLiteral, i, null);
					i++;
					
					display = true;
				}
			}
		}
		SingleMemberAnnotation newParamAnnotation = typeDeclaration.getAST().newSingleMemberAnnotation();
		newParamAnnotation.setTypeName(rewrite.getAST().newSimpleName(annotation));
		newParamAnnotation.setValue(initializer);
		
		if (display) {
			return newParamAnnotation;
		}
		return null;
    }
	
	public List getAllSuperTypes(TypeDeclaration typeDeclaration) {
		List list = new ArrayList();

		Type superclassType = typeDeclaration.getSuperclassType();
		if (superclassType != null) {
			list.add(superclassType);
		}

		List superInterfaceTypes = typeDeclaration.superInterfaceTypes();

		for (Iterator itSuperInterfacesIterator = superInterfaceTypes.iterator(); itSuperInterfacesIterator.hasNext();) {
			Object next = itSuperInterfacesIterator.next();
			if (next instanceof SimpleType) {
				list.add(next);
			}
		}
		return list;
	}

	private SingleMemberAnnotation setReturnTypeAnnotation(ASTRewrite rewrite, MethodDeclaration methodDeclaration, String annotation) {
		SingleMemberAnnotation newReturnAnnotation = getReturnTypeAnnotation(rewrite, methodDeclaration, annotation);

		ASTNode modifier = getModifier(methodDeclaration.modifiers());
		if (modifier != null) {
			ListRewrite paramRewrite = rewrite.getListRewrite(methodDeclaration, MethodDeclaration.MODIFIERS2_PROPERTY);
			paramRewrite.insertAfter(newReturnAnnotation, modifier, null);
		}
		else {
			ListRewrite paramRewrite = rewrite.getListRewrite(methodDeclaration, MethodDeclaration.MODIFIERS2_PROPERTY);
			paramRewrite.insertFirst(newReturnAnnotation, null);
		}
		
		return newReturnAnnotation;
	}
	
	private void updateReturnTypeAnnotation(ASTRewrite rewrite, MethodDeclaration methodDeclaration, String annotation, SingleMemberAnnotation currentAnnotation) {
		SingleMemberAnnotation newReturnAnnotation = getReturnTypeAnnotation(rewrite, methodDeclaration, annotation);

		ListRewrite paramRewrite = rewrite.getListRewrite(methodDeclaration, MethodDeclaration.MODIFIERS2_PROPERTY);
		paramRewrite.replace(currentAnnotation, newReturnAnnotation, null);
	}

	private SingleMemberAnnotation getReturnTypeAnnotation(ASTRewrite rewrite, MethodDeclaration methodDeclaration,
            String annotation) {
	    SingleMemberAnnotation newReturnAnnotation = methodDeclaration.getAST().newSingleMemberAnnotation();
		newReturnAnnotation.setTypeName(rewrite.getAST().newSimpleName("Domain"));
		StringLiteral newStringLiteral = rewrite.getAST().newStringLiteral();
		newStringLiteral.setLiteralValue(annotation);
		newReturnAnnotation.setValue(newStringLiteral);
	    return newReturnAnnotation;
    }


	private boolean hasAnnotation(List<IExtendedModifier> paramModifiers) {
		SingleMemberAnnotation currentAnnotation = getCurrentAnnotation(paramModifiers);
		return currentAnnotation != null;
	}
	
	// XXX. Consolidate with the other hasAnnotation...
	private SingleMemberAnnotation getCurrentAnnotation(List<IExtendedModifier> paramModifiers) {
		SingleMemberAnnotation currentAnnotation = null;
		if(paramModifiers != null)
		for (Iterator<IExtendedModifier> itParamModifiers = paramModifiers.iterator(); itParamModifiers.hasNext();) {
			Object o = itParamModifiers.next();
			if (o instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation annot = (SingleMemberAnnotation) o;
				if (annot.getTypeName().toString().compareTo("Domain") == 0) {
					currentAnnotation = annot;
					break;
				}
			}
		}
		return currentAnnotation;
	}

	private ASTNode hasTypeAnnotation(List<IExtendedModifier> paramModifiers, String annotation) {
		ASTNode found = null;
		for (Iterator<IExtendedModifier> itParamModifiers = paramModifiers.iterator(); itParamModifiers.hasNext();) {
			Object o = itParamModifiers.next();
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

	private ASTNode getAnnotation(List<IExtendedModifier> paramModifiers) {
		ASTNode found = null;
		for (Iterator<IExtendedModifier> itParamModifiers = paramModifiers.iterator(); itParamModifiers.hasNext();) {
			IExtendedModifier o = itParamModifiers.next();
			if (o.isAnnotation()) {
				found = (ASTNode) o;
				break;
			}
		}
		return found;
	}

	private ASTNode getModifier(List<IExtendedModifier> paramModifiers) {
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
		for (Iterator<IExtendedModifier> itParamModifiers = copy.iterator(); itParamModifiers.hasNext();) {
			IExtendedModifier o = itParamModifiers.next();
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

		private boolean isStaticScope(ASTNode varDecl) {
			ASTNode parent = varDecl.getParent();
			while (parent != null && !(parent instanceof MethodDeclaration) && (!(parent instanceof TypeDeclaration))) {
				parent = parent.getParent();
			}
			
			if(parent instanceof MethodDeclaration) {
				MethodDeclaration enclMethod = null;
				enclMethod = (MethodDeclaration)parent;
				// Not the main method!
				if(!enclMethod.getName().toString().contains("main")) {
					return Modifier.isStatic(enclMethod.getModifiers());
				}
			}
			
			if(parent instanceof TypeDeclaration) {
				TypeDeclaration enclType = (TypeDeclaration)parent;
				return Modifier.isStatic(enclType.getModifiers());
			}

			return false;
		}
		
		@Override
		/**
		 * Annotate local variables by visiting the enclosing statement.
		 * 
		 * Handles both VariableDeclarationStatement and VariableDeclarationExpression
		 */
		public boolean visit(VariableDeclarationFragment varDecl) {
			ASTNode parent = varDecl.getParent();
			Type declaredType = null;
			List parentMods = null;
			if(parent instanceof VariableDeclarationStatement ) {
				VariableDeclarationStatement varDeclStmt = (VariableDeclarationStatement)parent;
				parent = varDeclStmt;
				declaredType = varDeclStmt.getType();
				parentMods = varDeclStmt.modifiers();
			}
			else if (parent instanceof VariableDeclarationExpression ) {
				VariableDeclarationExpression varDeclExpr = (VariableDeclarationExpression)parent;
				parent = varDeclExpr;
				declaredType = varDeclExpr.getType();
				// Not allowed to call varExpr.modifiers();
				parentMods = null;
			}
			else {
				parent = null;
				// We handle FieldDeclaration elsewhere...
				return false;
			}
			
			ITypeBinding type = declaredType.resolveBinding();
			// XXX. Why would type be null here? Need to investigate.
			if (type != null && !type.isPrimitive()) {
				
				String annotation = "lent"; // XXX. Bad hard-coded default. What about <p>?
				if (HARD_CODE_STATICS && isStaticScope(parent)) {
					annotation = "shared<shared>";
				}
				else {
					annotation = strategy.getAnnotationForLocalVariable(varDecl);
				}
				
				ChildListPropertyDescriptor property = parent instanceof VariableDeclarationStatement ? 
							VariableDeclarationStatement.MODIFIERS2_PROPERTY
							: VariableDeclarationExpression.MODIFIERS2_PROPERTY;
				
				SingleMemberAnnotation currentAnnotation = getCurrentAnnotation(parentMods);
				if (currentAnnotation == null) {
					setVariableAnnotation2(parent, annotation, property);
				}
				else {
					updateVariableAnnotation2(parent, annotation, currentAnnotation, property);
				}		
				
				
				// XXX. Lookup enclosing type declaration from local variable
				String srcType = type.getQualifiedName();
				String dstType = Utils.getEnclosingTypeName(varDecl);
				// XXX. Come up with a better key
				trackChanges(varDecl.toString(), annotation, getValue(currentAnnotation), CSVConst.VARIABLE, srcType, dstType);
				
			}
			return super.visit(varDecl);
		}


		private SingleMemberAnnotation setVariableAnnotation2(ASTNode varDecl, String annotation, ChildListPropertyDescriptor property) {
			SingleMemberAnnotation newParamAnnotation = getVariableAnnotation(varDecl, annotation);
			ListRewrite paramRewrite = rewrite.getListRewrite(varDecl, property);
			paramRewrite.insertFirst(newParamAnnotation, null);
			return newParamAnnotation;
		}
		
		private void updateVariableAnnotation2(ASTNode varDecl, String annotation, SingleMemberAnnotation currentAnnotation, ChildListPropertyDescriptor property) {
			SingleMemberAnnotation newParamAnnotation = getVariableAnnotation(varDecl, annotation);
			ListRewrite paramRewrite = rewrite.getListRewrite(varDecl, property);
			paramRewrite.replace(currentAnnotation, newParamAnnotation, null);
		}

		private SingleMemberAnnotation getVariableAnnotation(ASTNode varDecl, String annotation) {
	        SingleMemberAnnotation newParamAnnotation = varDecl.getAST().newSingleMemberAnnotation();
			newParamAnnotation.setTypeName(rewrite.getAST().newSimpleName("Domain"));
			StringLiteral newStringLiteral = rewrite.getAST().newStringLiteral();
			newStringLiteral.setLiteralValue(annotation);
			newParamAnnotation.setValue(newStringLiteral);
	        return newParamAnnotation;
        }		
	}
	
	/**
	 * XXX. Track owner and alpha separately
	 * 
	 */
	private void trackChanges(String key, String annotation, String currentAnnotation, String auType, String srcType, String dstType) {
        if (TRACK_CHANGES) {
        	boolean changed = false;
        	if ( currentAnnotation != null ) {
        		changed = currentAnnotation.compareTo(annotation) != 0;
        	}
        	else {
        		// Was previously null; but now it isn't
        		changed = (annotation != null);
        	}
        	if (changed )  {
        		numChanged++;
        	}
        	
        	// The owner was changed
        	String curOwner = CSVConst.getOwner(currentAnnotation);
        	String newOwner = CSVConst.getOwner(annotation);
        	if(!curOwner.equals(newOwner)) {
        		// Use unqualified name. We don't replay these!
				// srcType = Signature.getSignatureSimpleName(srcType);
				// dstType = Signature.getSignatureSimpleName(dstType);
        		// Create an implicit refinement
        		createImplicitRefinement(curOwner, newOwner, srcType, dstType);
        	}
        	
        	
        	// DONE. Escape the key. May contain quotes, commas, newline, etc.
        	getWriter().append(CSVConst.sanitize(key));
        	getWriter().append(CSVConst.COMMA);
        	// DONE. Escape the annotation. May contain commas, etc.        	
        	getWriter().append(CSVConst.sanitize(currentAnnotation == null ? "" : currentAnnotation));
        	getWriter().append(CSVConst.COMMA);
        	getWriter().append(annotation);
        	getWriter().append(CSVConst.COMMA);
        	getWriter().append(Boolean.toString(changed));
        	getWriter().append(CSVConst.COMMA);
        	getWriter().append(auType);
        	getWriter().append(CSVConst.NEWLINE);
        }
    }


	/**
	 * Create an implicit refinement on the facade only.
	 * Important:  Do not add this to the set of "completedRefinements".
	 * 
	 * Otherwise, during TMPP (respect Previous Refinements), we will consider these implict refinements are provided by the user.
	 */
	private void createImplicitRefinement(String curAnnot, String newAnnot, String srcType, String dstType) {
		OGraphFacade facade = RefinementAnalysis.getFacade();
		RefinementModel refinementModel = facade.getRefinementModel();
		
    	// The owner was changed
    	String curOwner = CSVConst.getOwner(curAnnot);
    	String newOwner = CSVConst.getOwner(newAnnot);
    	if(!curOwner.equals(newOwner)) {
    		// Create an implicit refinement
    		// XXX. What if newOwner == "owned"
    		oog.re.Refinement ref = null;
    		if (newOwner.equals("owned")) {
    			ref = new oog.re.PushIntoOwned(srcType, dstType, newOwner);
    		}
    		else if (newOwner.equals("PD")) {
    			ref = new oog.re.PushIntoPD(srcType, dstType, newOwner);
    		}
    		if(ref != null ) {
    			// XXX. In the RefID, connect the "Auto" refinement to the original refinement...
    			// Where is refID assigned?
    			// NOTE: user-specified refinements (such as lastRefinement) will have a Facade object.
    			String lastRefId = "";
    			if ( lastRefinement != null ) {
    				lastRefId = " (for #" + lastRefinement.getFacadeOpr().getRefID() + ")";
    			}
    			else { 
    				// Result of heuristics
    				// For heuristics, we do this once at the end, so cannot tell which is which
    				lastRefId = " (for heus)";
    			}
    			
    			ref.setRefID("Auto" + lastRefId);
    			ref.setImplicit(true);
    			ref.setState(RefinementState.Completed);
    			refinementModel.add(ref);
    			
    			// Have every refinement also remember the implicit refinements it creates
    			if(lastRefinement != null ) {
    				lastRefinement.addImplicit(ref);
    			}
    		}
    	}
    }
}
