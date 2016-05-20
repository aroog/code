package edu.wayne.metrics.actions;



import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import edu.cmu.cs.aliasjava.AnnotationInfo;
import edu.cmu.cs.aliasjava.parser.DomainParams;
import edu.wayne.metrics.Crystal;
import edu.wayne.metrics.adb.ADBTripletSimple;
import edu.wayne.metrics.datamodel.ClassMetricItem;
import edu.wayne.metrics.datamodel.ObjectMetricItem;
import edu.wayne.metrics.internal.WorkspaceUtilities;
import edu.wayne.metrics.utils.AnnotationsUtils;
import edu.wayne.metrics.utils.CSVOutputUtils;

public class AnnotatMetrics implements IWorkbenchWindowActionDelegate {
	private static final String allDomainsTableHeader = "Key,Name,Type,Annotation,EnclosingType,IsStatic,NodeType,ASTNode,isArray,isEnum,isParametrizedType,isDomain,isDomainParams, isObjectPublicDomain";

	private static final String objectMetricHeader = "%lent,%unique,%shared,%private d_i,%public d_i,%p_i,%ff.d_i, other, total,";

	
	private static final String classMetricHeader = "AVG private d_i,AVG public d_i,AVG p_i,AVG inherits,AVG links,AVG assumes,#classes,";
	
	private static final String annosTableHeader = " Table 1,Domain statistics" + "\n" +
	"System,Fields,,,,,,,,,,Local Variables,,,,,,,,,,Method Parameters,,,,,,,,,,Method Return Values,,,,,,,,,,,Class Metrics" + "\n" +
	","+objectMetricHeader+objectMetricHeader+objectMetricHeader+objectMetricHeader+"%void," + classMetricHeader +"\n";

	private static final String classTableHeader = " Table 1,Domain statistics" + "\n" +
	"System,Key,Name,PublicDomains,,PrivateDomains,,DomainParams,,DomainInherits,,DomainLinks,,DomainAssumes,"+"\n";

	private static final String annosGlobalTableHeader = "System,"+objectMetricHeader + "\n";
	
	/**
	 * Set to true to display the paths of this compilation units (slows things down!)
	 */
	private final boolean DEBUG_OUTPUT_PATHS = false;

	private String currentProjectPath;

	private String annosMetricPath;
	
	private String classTablePath;
	
	private String srcTripletsPath;
	
	private String metricFilePath;

	private String globalDomainsStatFilePath;

	private String fieldsDomainsStatFilePath;

	private String localMethodDomainsStatFilePath;

	private String returnTypeMethodDomainsStatFilePath;
	
	private String annosGlobalMetricPath;

	private Hashtable<String, ObjectMetricItem> objectsHashtable = new Hashtable<String, ObjectMetricItem>();
	
	private Hashtable<String, ClassMetricItem> classHashtable = new Hashtable<String, ClassMetricItem>();
	
	private IWorkbenchWindow window = null;
	
	private int noVoidReturnTypes = 0;
	private int noMethods = 0;
	private int noClasses = 0;
	// [ privatedomains, publicdomains, domainParams, domainLinks, domainInherits, domainAssumes]
	private int[] classMetrics = {0,0,0,0,0,0};

	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 */
	public void run(IAction action) {
		// TODO: remove if you don't need
		AnnotationsUtils.classInfo.clear();

		objectsHashtable.clear();
		classHashtable.clear();

		noClasses = 0;
		noMethods = 0;
		noVoidReturnTypes = 0;

		for (int i = 0; i < classMetrics.length; i++) {
	        classMetrics[i] = 0;
        }
		
		IProject currentProject = WorkspaceUtilities.getCurrentProject(window);

		currentProjectPath = currentProject.getLocation().toOSString();
		
		// Annotations Metrics in the project
		String projectName = currentProject.getName();
		
		annosMetricPath = currentProjectPath + "\\" + projectName + "_ObjectMetrics.csv";
		
		// Class Annotations in the project for testing purposes
		
		classTablePath = currentProjectPath + "\\" + projectName + "_ClassMetrics.csv";
		
		
		
		// Compute simple triplets on the annotations
		
		srcTripletsPath = currentProjectPath + "\\" + projectName + "_SrcTriplets.csv";
		
		// All references in the system and their domains
		metricFilePath = currentProjectPath  + "\\" + projectName + "_AllReferences.csv";
		
		// Global domain metrics
		globalDomainsStatFilePath = currentProjectPath + "\\" + projectName + "_GlobalDomains.csv";
		
		// Fields and their domains
		fieldsDomainsStatFilePath = currentProjectPath  + "\\" + projectName + "_FieldDomains.csv";

		//Method local variables and parameters
		localMethodDomainsStatFilePath = currentProjectPath + "\\" + projectName +  "_VariableDomains.csv";
		
		//Method return value domains 
		returnTypeMethodDomainsStatFilePath = currentProjectPath + "\\" + projectName +  "_ReturnValueDomains.csv";

		//global annotation metrics
		annosGlobalMetricPath = currentProjectPath + "\\" + projectName +  "_GlobalAnnosMetrics.csv";
		
		if (currentProject != null) {
			IJavaProject javaProject = WorkspaceUtilities.getJavaProject(currentProject.getName());
			Crystal.getInstance().setJavaProject(javaProject);
		}
		else {
			MessageBox box = new MessageBox(window.getShell(), SWT.ICON_INFORMATION);
			box.setMessage("Please open a Java file in the project to analyze");
			box.open();
			return;
		}
		Crystal crystal = Crystal.getInstance();
		PrintWriter output = crystal.userOut();
		crystal.scanWorkspace();
		Iterator<ICompilationUnit> unitIterator = crystal.getCompilationUnitIterator();
		ICompilationUnit compUnit = null;
		for (; unitIterator.hasNext();) {
			compUnit = unitIterator.next();
			if (compUnit == null) {
				output.println("AbstractCompilationUnitAnalysis: null CompilationUnit");
				continue;
			}
			// Retrieve the path of this compilation unit, and output it
			if (DEBUG_OUTPUT_PATHS) {
				try {
					IResource resource = compUnit.getCorrespondingResource();
					if (resource != null) {
						IPath path = resource.getLocation();
						if (path != null) {
							output.println(path.toPortableString());
						}
					}
				}
				catch (JavaModelException e) {
					output.println("AbstractCompilationUnitAnalysis: Unable to retrieve path of CompilationUnit"
					        + compUnit.getElementName());
				}
			}
			// Obtain the AST for this CompilationUnit and analyze it
			ASTNode node = crystal.getASTNodeFromCompilationUnit(compUnit);
			if ((node != null) && (node instanceof CompilationUnit)) {
				try {
					analyzeCompilationUnit((CompilationUnit) node, compUnit);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				output.println("AbstractCompilationUnitAnalysis: Could not retrieve the ASTNode for CompilationUnit "
				        + compUnit.getElementName());
			}
		}

		try {

			// Write all data into file.
			writeAllStatDataToFile();

			// Find the details statistics and write them to a separate files
			globalDomainsStatistics();
			localMethodDomainStat();
			secondDomainStat(fieldsDomainsStatFilePath, "Field");
			secondDomainStat(returnTypeMethodDomainsStatFilePath, "ReturnType");

			MessageBox msgbox = new MessageBox(window.getShell(), SWT.ICON_INFORMATION);
			msgbox.setText("Annotation Metrics");
			msgbox.setMessage("The annotation statistics were generated successfully into a spreadsheet in the project directory.");
			msgbox.open();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		// Important: Reset all the hashmaps!
		crystal.reset();

	}

	private void analyzeCompilationUnit(CompilationUnit unit, ICompilationUnit compilationUnit) throws IOException {

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
		
		LocationVariableVisitor visitor = new LocationVariableVisitor();
		unit.accept(visitor);

		List types = unit.types();
		for (Iterator iter = types.iterator(); iter.hasNext();) {

			Object next = iter.next();
			if (next instanceof TypeDeclaration) {
				// declaration: Contains one file content at a time.
				TypeDeclaration declaration = (TypeDeclaration) next;
				traverseType(declaration);
			}
		}
	}

	private void traverseType(TypeDeclaration declaration) throws IOException {

		TypeDeclaration[] nestedTypes = declaration.getTypes();
		for (int i = 0; i < nestedTypes.length; i++) {
			traverseType(nestedTypes[i]);
		}
		traverseTypeDecl(declaration);
		traverseMethod(declaration);
		traverseFields(declaration);
	}

	@SuppressWarnings("unchecked")
    private void traverseTypeDecl(TypeDeclaration declaration) {
		noClasses++;
		Hashtable<String,String> annotations = new Hashtable<String, String>();
	   List<String> domains = getAnnotationList(declaration.modifiers(), "Domains");
	   List<String> pDomains = new ArrayList<String>();
	   if (domains.size()>0) {
		   annotations.put("@Domains", domains.toString());
		   if (domains.contains("owned")) pDomains.add("owned");
		   domains.remove("owned");
		   //int prDoms = AnnotationsUtils.extractNoPrivateDomains(domains.toString());
		 //  int allDoms = AnnotationsUtils.extractNoItems(domains.toString());
		   classMetrics[1]+= domains.size();
		   classMetrics[0]+= pDomains.size();
	   }
	   List<String> domainParams = getAnnotationList(declaration.modifiers(), "DomainParams");
	   if (domainParams.size()>0) {
		   annotations.put("@DomainParams", domainParams.toString());
		   //classMetrics[2]+= AnnotationsUtils.extractNoItems(domainParams.toString());
//		   classMetrics[2]+=domainParams.size();
	   }
	   List<String> domainInherits = getAnnotationList(declaration.modifiers(), "DomainInherits");
	   if (domainInherits.size()>0) {
		   HashSet<DomainParams> alpha = new HashSet<DomainParams>();
		   for(String di:domainInherits){
			   AnnotationInfo ai = AnnotationInfo.parseAnnotation(di);
			   alpha.addAll(ai.getParameters());
		   }
		   if (domainParams.size() - alpha.size()>=0){
			   classMetrics[2]+= domainParams.size() - alpha.size();
			   classMetrics[3]+= alpha.size();
		   }
		   else{
			// HACK: Why should this ever be smaller than 0?
			   int debug = 0; debug++;
			   System.out.println("|beta|>|alpha| should not happen!");
		   }
		   
//		   annotations.put("@DomainInherits", domainInherits.toString());
		   
		   //classMetrics[3]+= AnnotationsUtils.extractNoItems(domainInherits.toString());
//		   classMetrics[3]+=domainInherits.size();
	   }
	   else {
		   classMetrics[2]+=domainParams.size();
	   }
	   
	   //compute |\alpha| and |\beta|\
	   
	   List<String> domainLinks = getAnnotationList(declaration.modifiers(), "DomainLinks");
	   if (domainLinks.size()>0) {
		   annotations.put("@DomainLinks", domainLinks.toString());
		   //classMetrics[4]+= AnnotationsUtils.extractNoItems(domainLinks.toString());
		   classMetrics[4]+=domainLinks.size();
	   }
	   List<String> domainAssumes = getAnnotationList(declaration.modifiers(), "DomainAssumes");
	   if (domainAssumes.size()>0) {
		   annotations.put("@DomainAssumes", domainAssumes.toString());
		   //classMetrics[5]+= AnnotationsUtils.extractNoItems(domainAssumes.toString());
		   classMetrics[5]+=domainAssumes.size();
	   }
	   ClassMetricItem cmi = new ClassMetricItem(declaration.resolveBinding().getKey(),
			   declaration.resolveBinding().getQualifiedName().toString(),
			   "", //empty for now
			   annotations,
			   declaration.getAST().toString(),
			   domains,
			   pDomains,
			   domainParams,
			   domainInherits,
			   domainLinks,
			   domainAssumes
			   );
	   classHashtable.put(declaration.resolveBinding().getKey(), cmi);
	   AnnotationsUtils.classInfo.put(declaration.resolveBinding().getKey(), cmi);
		   
    }

	// Analyze methods
	private void traverseMethod(TypeDeclaration declaration) throws IOException {
		MethodDeclaration[] methods = declaration.getMethods();
		for (int i = 0; i < methods.length; i++) {
			MethodDeclaration methodDeclaration = methods[i];
			traverseMethodReturn(methodDeclaration);
			traverseMethodParams(methodDeclaration);
		}
	}

	// Analyze the list of parameters
	private void traverseMethodParams(MethodDeclaration methodDeclaration) throws IOException {
		List parameters = methodDeclaration.parameters();
		for (Iterator itParams = parameters.iterator(); itParams.hasNext();) {
			SingleVariableDeclaration param = (SingleVariableDeclaration) itParams.next();

			ITypeBinding type = param.resolveBinding().getType();
			if (hasAnnotation(param.modifiers()) && !type.isPrimitive()) {
				SingleMemberAnnotation annotation = getAnnotation(param.modifiers());
				Expression value = annotation.getValue();
				if (value instanceof StringLiteral) { //@Domain("D")
					StringLiteral annotValue = (StringLiteral)value;
					String parserInput = annotValue.getLiteralValue();
					AnnotationInfo annotInfo = AnnotationInfo.parseAnnotation(parserInput); 
					DomainParams annot = annotInfo.getAnnotation();	
				boolean isDom = isDomain(methodDeclaration.resolveBinding().getDeclaringClass(), annot);
				boolean isDomPars = isDomainParams(methodDeclaration.resolveBinding().getDeclaringClass(), annot);
				
				ObjectMetricItem archMetricItem = new ObjectMetricItem(param.resolveBinding().getKey(),
					        param.getName().getFullyQualifiedName(),
					        param.getType().resolveBinding().getQualifiedName(),
					        parserInput,
					        methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName(),
					        param.toString(),
					        Modifier.isStatic(param.getModifiers()),
					        "MethodParams",
					        param.resolveBinding().getType().isArray(),
					        param.resolveBinding().getType().isEnum(),
					        param.resolveBinding().getType().isParameterizedType(),
					        isDom,
					        isDomPars,
					        annot.isObjectPublicDomain());
				if (!objectsHashtable.containsKey(archMetricItem.toString())) {
					objectsHashtable.put(archMetricItem.toString(), archMetricItem);
					}
					// TODO: src.triplets for Method Params
				}
			}
		}
	}
	// Analyze the method return type
	private void traverseMethodReturn(MethodDeclaration methodDeclaration) throws IOException {
		Type returnType2 = methodDeclaration.getReturnType2();
		if (returnType2 != null) {
			ITypeBinding resolveBinding = returnType2.resolveBinding();
			noMethods++;
			if (resolveBinding.getName().equals("void")) noVoidReturnTypes++;
			if (hasAnnotation(methodDeclaration.modifiers()) && !resolveBinding.isPrimitive()) {

				SingleMemberAnnotation annotation = getAnnotation(methodDeclaration.modifiers());
				Expression value = annotation.getValue();
				if (value instanceof StringLiteral) { //@Domain("D")
					StringLiteral annotValue = (StringLiteral)value;
					String parserInput = annotValue.getLiteralValue();
					AnnotationInfo annotInfo = AnnotationInfo.parseAnnotation(parserInput); 
					DomainParams annot = annotInfo.getAnnotation();	

					boolean isDom = isDomain(methodDeclaration.resolveBinding().getDeclaringClass(), annot);
					boolean isDomPars = isDomainParams(methodDeclaration.resolveBinding().getDeclaringClass(), annot);


					ObjectMetricItem archMetricItem = new ObjectMetricItem(methodDeclaration.resolveBinding().getKey(),
							methodDeclaration.getName().getFullyQualifiedName(),
							resolveBinding.getQualifiedName(),
							parserInput,
							methodDeclaration.resolveBinding().getDeclaringClass().getQualifiedName(),
							returnType2.toString(),
							Modifier.isStatic(methodDeclaration.getModifiers()),
							"ReturnType",
							returnType2.resolveBinding().isArray(),
							returnType2.resolveBinding().isEnum(),
							returnType2.resolveBinding().isParameterizedType(),
							isDom,
							isDomPars,
							annot.isObjectPublicDomain());
					if (!objectsHashtable.containsKey(archMetricItem.toString())) {
						objectsHashtable.put(archMetricItem.toString(), archMetricItem);
					}
					// TODO: src.triplets for MethodReturn type					
				}
			}
		}
	}

	// Analyze fields.
	private void traverseFields(TypeDeclaration declaration) {
		FieldDeclaration[] fieldDeclarations = declaration.getFields();
		for (int i = 0; i < fieldDeclarations.length; i++) {
			FieldDeclaration fieldDeclaration = fieldDeclarations[i];
			ITypeBinding type = fieldDeclaration.getType().resolveBinding();

			if (hasAnnotation(fieldDeclaration.modifiers()) && !type.isPrimitive()) {

				List ff = fieldDeclaration.fragments();
				for (Object ff1 : ff) {
					VariableDeclaration V = (VariableDeclaration) ff1;
					String key = V.resolveBinding().getKey();
					String name = V.getName().getIdentifier();
					String objectType = fieldDeclaration.getType().resolveBinding().getQualifiedName();
					
					String enclosingType = declaration.resolveBinding().getQualifiedName();
				
					String ast = fieldDeclaration.getAST().toString();
					boolean isStatic = Modifier.isStatic(fieldDeclaration.getModifiers());
					//					
					SingleMemberAnnotation annotation = getAnnotation(fieldDeclaration.modifiers());
					Expression value = annotation.getValue();
					if (value instanceof StringLiteral) { //@Domain("D")
						StringLiteral annotValue = (StringLiteral)value;
						String parserInput = annotValue.getLiteralValue();
						AnnotationInfo annotInfo = AnnotationInfo.parseAnnotation(parserInput); 
						DomainParams annot = annotInfo.getAnnotation();	
					
						boolean isDomainParam = isDomainParams(declaration.resolveBinding(), annot);
						boolean isDomain = isDomain(declaration.resolveBinding(), annot);
						ObjectMetricItem archMetricItem = new ObjectMetricItem(key,name,
								objectType,
								parserInput,
								enclosingType,
								ast,
								isStatic,
								"Field",
								type.isArray(),
								type.isEnum(),
								type.isParameterizedType(),
								isDomain,
								isDomainParam,
								annot.isObjectPublicDomain()
						);
						// TODO: Do we need the following two lines?
						archMetricItem.setDomainParams(isDomainParam);
						archMetricItem.setDomain(isDomain);
						if (!objectsHashtable.containsKey(archMetricItem.toString())) {							
							objectsHashtable.put(archMetricItem.toString(), archMetricItem);
						}
						
						// TODO: src.triplets for Field
					}

					else
					{
						//this should not happen
						System.out.println("Error in field declaration: "+ value);
					}
				}
			}
		}
	}

	public String extractDomain(String annotation) {
		String tempStr = annotation.substring(9);

		int leftquote = tempStr.indexOf("\""); // -1 if not exists
		int leftAngle = tempStr.indexOf("<");
		int leftSQ = tempStr.indexOf("[");

		int leftEnd = leftquote;

		if (leftAngle > 0 && leftAngle < leftEnd) {
			leftEnd = leftAngle;
		}

		if (leftSQ > 0 && leftSQ < leftEnd) {
			leftEnd = leftSQ;
		}
		return (tempStr.substring(0, leftEnd)).trim();
	}

	private boolean hasAnnotation(List<IExtendedModifier> paramModifiers) {
		boolean found = false;
		for (Iterator<IExtendedModifier> itParamModifiers = paramModifiers.iterator(); itParamModifiers.hasNext();) {
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

	/**
     * @param domain
     * @param ann finds a domain in annotation binding
     */
    private boolean hasDomain(DomainParams domain, IAnnotationBinding ann) {
	    IMemberValuePairBinding[] pairs = ann.getDeclaredMemberValuePairs();
	    for (int i = 0; i < pairs.length; i++) {
	    	if (pairs[i].getValue() instanceof String) {
	    		String dname = (String)pairs[i].getValue();
	    		if (dname.equals(domain.getDomainName())) return true;
	    	}
	    	if (pairs[i].getValue() instanceof Object[]){
	    		Object[] objs = (Object[])pairs[i].getValue();
	    		for (int j = 0; j < objs.length; j++) {
	    			String dName = objs[j].toString();
	    			if (dName.equals(domain.getDomainName())) return true;
	    		}
	    	}
	    }
	    return false;
    }

    
	public boolean isDomain(ITypeBinding iTypeBinding, DomainParams domain) {
		if (domain.isLent()||domain.isUnique()||domain.isShared())
			return true;

		IAnnotationBinding[] annotations = iTypeBinding.getAnnotations();
		for (IAnnotationBinding ann : annotations) {
			if (ann.getName().equals("Domains")) {
				if (hasDomain(domain, ann)) return true;
			}
		}
		return false;
	}
    
	public boolean isDomainParams(ITypeBinding iTypeBinding, DomainParams domain) {
		if (domain.isLent() || domain.isUnique() || domain.isShared() || domain.isOwned()) return false;
		if (domain.isOwner()) return true;

		IAnnotationBinding[] annotations = iTypeBinding.getAnnotations();
		for (IAnnotationBinding ann : annotations) {
			if (ann.getName().equals("DomainParams")) {
				if (hasDomain(domain, ann)) return true;				
			}
		}
		return false;
	}

	
	public List<String> getAnnotationList(List<IExtendedModifier> paramModifiers, String annotation) {	
		List<String> found = new ArrayList<String>();
		
		for (IExtendedModifier o:paramModifiers){
			if (o instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation annot = (SingleMemberAnnotation) o;
				if (annot.getTypeName().toString().equals(annotation)) {

					Expression value = annot.getValue();
					if (value instanceof ArrayInitializer) { //@Domains( {"D", "U"})
						ArrayInitializer annotValueArray = (ArrayInitializer)value;
						found = new ArrayList<String>();
						for (Object s:annotValueArray.expressions()){
							if (s instanceof StringLiteral) {
								StringLiteral sLiteral = (StringLiteral)s;
								found.add(sLiteral.getLiteralValue());
							}
						}
						return found;
					}
					if (value instanceof StringLiteral) { //@Domains ("D")
						StringLiteral annotValue = (StringLiteral)value;
						String parserInput = annotValue.getLiteralValue();
						found = new ArrayList<String>();
						found.add(parserInput);
						return found;
					}
				}
			}
		}
		return found;
	}
	
	private SingleMemberAnnotation getAnnotation(List<IExtendedModifier> paramModifiers) {
		SingleMemberAnnotation found = null;
		for (Iterator<IExtendedModifier> itParamModifiers = paramModifiers.iterator(); itParamModifiers.hasNext();) {
			IExtendedModifier o = itParamModifiers.next();
			if ((o instanceof SingleMemberAnnotation) ) {
				found = (SingleMemberAnnotation) o;
				if (found.getTypeName().toString().equals("Domain"))
					break;
			}
		}
		

		return found;
	}

	private class LocationVariableVisitor extends ASTVisitor {

		TypeDeclaration currentType;	
		
		@Override
        public boolean visit(Initializer node) {
//			System.out.println("this is initializer " + node.toString());
	        // TODO Auto-generated method stub
	        return super.visit(node);
        }


		@Override
        public boolean visit(TypeDeclaration node) {
			currentType = node;
	        return super.visit(node);
        }


		// Local variables
		@Override
		public boolean visit(VariableDeclarationStatement param) {
			
			ITypeBinding type = param.getType().resolveBinding();
			if (hasAnnotation(param.modifiers()) && !type.isPrimitive()) {
				List<VariableDeclarationFragment> L = param.fragments();
				for (VariableDeclarationFragment oo : L) {

					SingleMemberAnnotation annotation = getAnnotation(param.modifiers());
					Expression value = annotation.getValue();
					if (value instanceof StringLiteral) { //@Domain("D")
						StringLiteral annotValue = (StringLiteral)value;
						String parserInput = annotValue.getLiteralValue();
						AnnotationInfo annotInfo = AnnotationInfo.parseAnnotation(parserInput); 
						DomainParams annot = annotInfo.getAnnotation();	
						boolean b = annot.isObjectPublicDomain();
						if (b)
						{
							b = true;
						}


						boolean isDom = false;
						boolean isDomPars = false;
						String qualifiedName = "";
				
//						if (oo.resolveBinding().getDeclaringMethod() != null) {
//							qualifiedName = oo.resolveBinding().getDeclaringMethod().getDeclaringClass().getQualifiedName();
//
//							isDom = isDomain(oo.resolveBinding().getDeclaringMethod().getDeclaringClass(), annot);
//							isDomPars = isDomainParams(oo.resolveBinding().getDeclaringMethod().getDeclaringClass(),annot);
//
//
//						}
//						else 
						{

							qualifiedName = currentType.resolveBinding().getQualifiedName();

							isDom = isDomain(currentType.resolveBinding(), annot);
							isDomPars = isDomainParams(currentType.resolveBinding() , annot);		

						}
						
						ObjectMetricItem archMetricItem = new ObjectMetricItem(oo.resolveBinding().getKey(),oo.resolveBinding().getName().toString(),
								param.getType().resolveBinding().getQualifiedName().toString(),
								parserInput,
								qualifiedName,
								oo.toString(),
								Modifier.isStatic(param.getModifiers()),
								"LocalVariable",
								type.isArray(),
								type.isEnum(),
								type.isParameterizedType(),
								isDom,
								isDomPars,
								annot.isObjectPublicDomain()
							);
						
						
							
						if (!objectsHashtable.containsKey(archMetricItem.toString())) {
							objectsHashtable.put(archMetricItem.toString(), archMetricItem);
						}
						
						// TODO: src.triplets for Local variables
					}
				}
			}
				return super.visit(param);
		}
	}

	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}

	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 */
	public void dispose() {

	}

	/**
	 * required by the IWorkbenchWindowActionDelegate interface
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void localMethodDomainStat() throws IOException {
		int iii = 0;
		Hashtable<String, Double> counts = new Hashtable<String, Double>();

		for (ObjectMetricItem arch : objectsHashtable.values()) {
			if (arch.getNodeType().compareTo("LocalVariable") == 0 || arch.getNodeType().compareTo("MethodParams") == 0) {
				iii++;
				Double ddd = 1.0;
				String domain = arch.extractDomain().getDomain();
				if (arch.isDomain()){
					domain = "Domain," + domain;
				} else if (arch.isDomainParams()){
					domain = "DomainParams," + domain;
				}
				else
					domain = "," + domain;
					
				if (counts.containsKey(domain)) {
					ddd = counts.get(domain) + 1;
				}
				counts.put(domain, ddd);
			}
		}
		writeTableToFile(localMethodDomainsStatFilePath, iii, counts);
	}

	public void secondDomainStat(String fileName, String NodeType) throws IOException {
		int iii = 0;
		Hashtable<String, Double> counts = new Hashtable<String, Double>();

		for (ObjectMetricItem arch : objectsHashtable.values()) {
			if (arch.getNodeType().compareTo(NodeType) == 0) {
				iii++;
				Double ddd = 1.0;
				String domain = arch.extractDomain().getDomain();
				if (arch.isDomain()){
					domain = "Domain," + domain;
				} else if (arch.isDomainParams()){
					domain = "DomainParams," + domain;
				}
				else
					domain = "," + domain;
				
				if (counts.containsKey(domain)) {
					ddd = counts.get(domain) + 1;
				}
				counts.put(domain, ddd);
			}
		}
		writeTableToFile(fileName, iii, counts);
	}

	public void globalDomainsStatistics() throws IOException {
		Hashtable<String, Double> counts = new Hashtable<String, Double>();

		for (ObjectMetricItem arch : objectsHashtable.values()) {
			Double ddd = 1.0;
			String domain = arch.extractDomain().getDomain();
			if (arch.isDomain()){
				domain = "Domain," + domain;
			} else if (arch.isDomainParams()){
				domain = "DomainParams," + domain;
			}
			else
				domain = "," + domain;
			
			if (counts.containsKey(domain)) {
				ddd = counts.get(domain) + 1;
			}
			counts.put(domain, ddd);
		}
		writeTableToFile(globalDomainsStatFilePath, objectsHashtable.size(), counts);
	}


	private void writeTableToFile(String fileName, int iii, Hashtable<String, Double> counts) throws IOException {
		FileWriter writer = new FileWriter(fileName);
		writer.append(",Annotation,Frequency,Percent");
		writer.append('\n');

		DecimalFormat twoDForm = new DecimalFormat("#.##");

		for (Entry<String, Double> entry : counts.entrySet()) {
			writer.append(entry.getKey() + "," + entry.getValue() + ","
			        + Double.valueOf(twoDForm.format(((entry.getValue() / iii) * 100))) + "%");
			writer.append('\n');
		}
		writer.append(",Total," + iii + ",100% ");
		writer.flush();
		writer.close();
	}

	public void writeAllStatDataToFile() throws IOException {
		FileWriter writer = new FileWriter(metricFilePath);

		writer.append(allDomainsTableHeader);
		writer.append('\n');

		for (ObjectMetricItem arch : objectsHashtable.values()) {
			writer.append(arch.toString().trim());
			writer.append('\n');
		}
		writer.append("Total domains," + objectsHashtable.size());
		writer.flush();
		writer.close();
		
		CSVOutputUtils.writeTableHeaderToFile(annosMetricPath, annosTableHeader);
		IProject currentProject = WorkspaceUtilities.getCurrentProject(window);
		CSVOutputUtils.appendRowToFile(annosMetricPath, currentProject.getName(), computeStats(false));
		
		CSVOutputUtils.writeTableHeaderToFile(annosGlobalMetricPath, annosGlobalTableHeader);
		CSVOutputUtils.appendRowToFile(annosGlobalMetricPath, currentProject.getName(), computeStats(true));
		
		CSVOutputUtils.writeTableHeaderToFile(classTablePath, classTableHeader);
		for (ClassMetricItem cmi:classHashtable.values()){
			CSVOutputUtils.appendRowToFile(classTablePath, currentProject.getName(), cmi.toString());
		}
		
		// Compute the ADBTriplets on the annotations
		Set<ADBTripletSimple> triplets = new HashSet<ADBTripletSimple>();
		for (ObjectMetricItem arch : objectsHashtable.values()) {
			triplets.add(arch.getTriplet());
		}
		ADBTripletSimple.save(triplets, this.srcTripletsPath);
	}
	
	//CSV related methods - maybe move in a different class
	private String computeStats(boolean global){
		StringBuffer rowBuffer = new StringBuffer();
		StringBuffer rawNumbers = new StringBuffer();
		StringBuffer globalBuffer = new StringBuffer();
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		int noFields = 0;
		int noReturnTypes = 0;
		int noLocalVars = 0;
		int noMethodParams = 0;
		
		//[lent, unique, shared, privatedom, publicdom, domparam, fi.DOM, other]	
		int[] fieldMetric =        {0,0,0,0,0,0,0,0};
		int[] varMetric =          {0,0,0,0,0,0,0,0};
		int[] methodParamsMetric = {0,0,0,0,0,0,0,0};
		int[] returnTypeMetric =   {0,0,0,0,0,0,0,0};
		final String emptySeparator = ",,,,,,,,,";

		
		for (ObjectMetricItem metric:objectsHashtable.values()){
			if (metric.getNodeType().equals("Field")) {
				noFields++;
				getMetrics(fieldMetric, metric);

			}
			else
			if (metric.getNodeType().equals("LocalVariable")) {
				noLocalVars++;
				getMetrics(varMetric, metric);
			}
			else
			if (metric.getNodeType().equals("MethodParams")) {
				noMethodParams++;
				getMetrics(methodParamsMetric, metric);
			}
			else
			if (metric.getNodeType().equals("ReturnType")) {
				noReturnTypes++;
				getMetrics(returnTypeMetric, metric);
			}
			else
				System.out.println("there is something else: "+metric.getNodeType());
		}
		

		double totalPercentage = 0.0;
		int totalRaw = 0;
		
		if (noFields >0){
			for (int i = 0; i<fieldMetric.length; i++){
				rowBuffer.append(twoDForm.format(fieldMetric[i]*1.0/noFields * 100));
				rowBuffer.append(",");
				rawNumbers.append(twoDForm.format(fieldMetric[i]));
				totalRaw+=fieldMetric[i];
				totalPercentage+=fieldMetric[i]*1.0/noFields * 100;
				rawNumbers.append(",");
			}
			rowBuffer.append(twoDForm.format(totalPercentage)+",");
			rawNumbers.append(totalRaw+",");
		}
		else {
			rowBuffer.append(emptySeparator);
			rawNumbers.append(emptySeparator);
		}
		totalPercentage = 0.0;
		totalRaw = 0;
		if (noLocalVars >0){
			for (int i = 0; i<varMetric.length; i++){ 
				rowBuffer.append(twoDForm.format(varMetric[i]*1.0/noLocalVars * 100));
				rowBuffer.append(",");
				rawNumbers.append(twoDForm.format(varMetric[i]));
				totalRaw+=varMetric[i];
				totalPercentage+=varMetric[i]*1.0/noLocalVars * 100;
				rawNumbers.append(",");
			}
			rowBuffer.append(twoDForm.format(totalPercentage)+",");
			rawNumbers.append(totalRaw+",");
		}
		else {
			rowBuffer.append(emptySeparator);
			rawNumbers.append(emptySeparator);
		}
		totalPercentage = 0.0;
		totalRaw = 0;
		if (noMethodParams >0){
			for (int i = 0; i<methodParamsMetric.length; i++){ 
				rowBuffer.append(twoDForm.format(methodParamsMetric[i]*1.0/noMethodParams * 100));
				rowBuffer.append(",");
				rawNumbers.append(twoDForm.format(methodParamsMetric[i]));
				totalRaw+=methodParamsMetric[i];
				totalPercentage+=methodParamsMetric[i]*1.0/noMethodParams * 100;
				rawNumbers.append(",");
			}
			rowBuffer.append(twoDForm.format(totalPercentage)+",");
			rawNumbers.append(totalRaw+",");
		}
		else {
			rowBuffer.append(emptySeparator);
			rawNumbers.append(emptySeparator);
		}
		totalPercentage = 0.0;
		totalRaw = 0;
		if (noReturnTypes >0){ 
			for (int i = 0; i<returnTypeMetric.length; i++){  
				rowBuffer.append(twoDForm.format(returnTypeMetric[i]*1.0/noReturnTypes * 100));
				rowBuffer.append(",");
				rawNumbers.append(twoDForm.format(returnTypeMetric[i]));
				totalRaw+=returnTypeMetric[i];
				totalPercentage+=returnTypeMetric[i]*1.0/noReturnTypes * 100;
				rawNumbers.append(",");
			}
			rowBuffer.append(twoDForm.format(totalPercentage)+",");
			rawNumbers.append(totalRaw+",");
		}
		else {
			rowBuffer.append(emptySeparator);
			rawNumbers.append(emptySeparator);
		}

		if (noMethods>0){
			rowBuffer.append(twoDForm.format(noVoidReturnTypes*1.0/noMethods * 100));
			rowBuffer.append(",");
			rawNumbers.append(noVoidReturnTypes+",");
		}
		else {
			rowBuffer.append(",");
			rawNumbers.append(",");
		}
		if (noClasses > 0) {
			for (int i=0;i<classMetrics.length;i++){
				rowBuffer.append(twoDForm.format(classMetrics[i]*1.0/noClasses));
				rowBuffer.append(",");
				rawNumbers.append(classMetrics[i]+",");
			}
			rowBuffer.append(noClasses+",");		
		}
		else {
			rowBuffer.append(",,,,,,");
			rawNumbers.append(",,,,,,");
		}
		if (!global)
		{
			return rowBuffer.toString()+"\n"+","+rawNumbers.toString();
		}
		else {
			int i = 0;
			long totalLent =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, i++);
			long totalShared =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, i++);
			long totalPrivateD =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, i++);
			long totalPublicD =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, i++);
			long totalUnique =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, i++);
			long totalPi =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, i++);
			long totalFFDi =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, i++);
			long totalOther =	getSumColumn(fieldMetric, varMetric, methodParamsMetric, returnTypeMetric, i++);
			long sumColumn = totalLent + 
							 totalShared + 
							 totalPrivateD + 
							 totalPublicD + 
							 totalUnique + 
							 totalPi + 
							 totalFFDi + 
							 totalOther;
			
			//save to buffer on two separate columns, one for raw numbers, one for percentages			
			globalBuffer.append(totalLent + ","); 
			globalBuffer.append(totalShared + ",");
			globalBuffer.append(totalPrivateD + ",");
			globalBuffer.append(totalPublicD + ",");
			globalBuffer.append(totalUnique + ",");
			globalBuffer.append(totalPi + ",");
			globalBuffer.append(totalFFDi + ",");
			globalBuffer.append(totalOther + ",");
			globalBuffer.append(sumColumn + ",");
			globalBuffer.append("\n");
			if (sumColumn>0){
				globalBuffer.append(",");
				globalBuffer.append(twoDForm.format(totalLent*1.0/sumColumn * 100) + ","); 
				globalBuffer.append(twoDForm.format(totalShared*1.0/sumColumn * 100) + ","); 
				globalBuffer.append(twoDForm.format(totalPrivateD*1.0/sumColumn * 100) + ","); 
				globalBuffer.append(twoDForm.format(totalPublicD*1.0/sumColumn * 100) + ","); 
				globalBuffer.append(twoDForm.format(totalUnique*1.0/sumColumn * 100) + ","); 
				globalBuffer.append(twoDForm.format(totalPi*1.0/sumColumn * 100) + ","); 
				globalBuffer.append(twoDForm.format(totalFFDi*1.0/sumColumn * 100) + ","); 
				globalBuffer.append(twoDForm.format(totalOther*1.0/sumColumn * 100) + ",");
			}
			return globalBuffer.toString();
		}
	}

	/**
     * @param fieldMetric
     * @param varMetric
     * @param methodParamsMetric
     * @param returnTypeMetric
     * @param totalLent
     * @param i
     */
    private long getSumColumn(int[] fieldMetric, int[] varMetric, int[] methodParamsMetric, int[] returnTypeMetric,
            int i) {
    	long totalColumn = 0;
	    totalColumn+=fieldMetric[i];
	    totalColumn+=varMetric[i];
	    totalColumn+=methodParamsMetric[i]; //maybe exclude this one too.
	   // totalColumn+=returnTypeMetric[i]; exclude for now
	    return totalColumn;
    }

	/**
     * @param fieldMetric
     * @param metric
     */
    private void getMetrics(int[] fieldMetric, ObjectMetricItem metric) {
	    if (metric.isDomainParams()) {
	    		fieldMetric[5]++; //domain params (including "owner")
	    }
	    else if(metric.isDomain()){
	    	if (metric.extractDomain().isLent())
	    		fieldMetric[0]++; //lent
	    	else
	    		if (metric.extractDomain().isUnique())
	    			fieldMetric[1]++; //unique
	    		else
	    			if (metric.extractDomain().isShared())
	    				fieldMetric[2]++; //shared
	    			else
	    				if (metric.extractDomain().isOwned()) //I need a method to check if this is a public/private domain
	    					fieldMetric[3]++; // private domain
	    				else
	    					fieldMetric[4]++; // public domain				
	    }
	    else if (metric.isObjectPublicDomain()){
	    	fieldMetric[6]++; //f.DOM
	    }
	    else 	
	    {
	    	fieldMetric[7]++; //other
	    	System.out.println(metric.getAnnotation()+" "+metric.getEnclosingType()+" " + metric.getDeclaredType());
	    }
    }
		
}