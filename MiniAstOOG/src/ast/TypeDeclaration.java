package ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Transient;

import adapter.Adapter;
import adapter.TraceabilityFactory;

// TODO: MED. Handle nested classes?
// In that case, a TypeDeclaration can have a parent TypeDeclaration?
// TODO: HIGH. Inner classes have implications for domain parameters as well!
// TODO: HIGH. Superclass has implications for domain parameters as well.
public class TypeDeclaration extends BodyDeclaration {

	// Connect Type to TypeDeclaration?
	// TODO: HIGH. XXX. This field does not make sense here.
	// It should be the other way around; a Type knows its TypeDeclaration!
	// A TypeDeclaration has a fullyQualifiedName
	// Make fullyQualifiedName a required field. Cannot create a TypeDeclaration without a fullyQualifiedName
	@Element(required=false)
	public Type type;
	
	@ElementList(required=false)
	public List<FieldDeclaration> fields;

	// Or just have a String for the fully qualified type name
	
	// TODO: HIGH. XXX. Better to store as C::d here. Or just assume that the Strings are always of the form C::d,
	// including null::SHARED.
	// If using qualified domains, still give ability to return an unqualified domain, e.g., getRawDomains or something.
	/**
	 * List of locally declared domains; maintain invariant of non-null
	 * 
	 * TODO: HIGH. XXX. Distinguish between public and private domains!!!
	 */
	@ElementList(required=false)
	private List<String> domains = new ArrayList<String>();

	/**
	 * List of domain parameters; maintain invariant of NonNull
	 */
	@ElementList(required=false)
	protected List<String> parameters = new ArrayList<String>();
	
	// TODO: HIGH. Have a TypeDeclaration point to its super TypeDeclaration
	// TODO: HIGH. Have a TypeDeclaration point to its outer TypeDeclaration
	// TODO: HIGH. Do we need this inheritParams???
	private List<String> inheritParams = new ArrayList<String>();
	
	/**
	 * Serialization requires a default constructor, which can be protected.
	 */
	protected TypeDeclaration() {
	    super();
	    
		// Register the TypeDeclaration with the class table
	    // NOTE: the object may not have been fully initialized yet; some fields are still null!
		ClassTable.getInstance().addTypeDeclaration(this);	    
    }
	
	static ast.TypeDeclaration create(){
		return new TypeDeclaration();
	}
	
	public static ast.TypeDeclaration createFrom(ASTNode node){
		ast.TypeDeclaration retNode = null;
		if(node instanceof org.eclipse.jdt.core.dom.TypeDeclaration){
			org.eclipse.jdt.core.dom.TypeDeclaration td = 
					(org.eclipse.jdt.core.dom.TypeDeclaration)node;
			
			Adapter factory = Adapter.getInstance();
			AstNode astNode = factory.get(node);
			if(astNode instanceof ast.TypeDeclaration){
				retNode = (ast.TypeDeclaration)astNode;
			}else{
				retNode = ast.TypeDeclaration.create();
				ITypeBinding typeBinding = ((org.eclipse.jdt.core.dom.TypeDeclaration) node).resolveBinding();
				retNode.type = TraceabilityFactory.getType(typeBinding);
				factory.map(node, retNode);
				factory.mapTypeDeclaration(retNode);
				retNode.fields = TraceabilityFactory.getDeclaredFields(td);
			}
		}else{
			throw new IllegalArgumentException();
		}
		return retNode;
	}
	
	public static ast.TypeDeclaration createFrom(ITypeBinding typeBinding){
		ast.TypeDeclaration retNode = null;
		
		Adapter factory = Adapter.getInstance();
		AstNode astNode = factory.get(typeBinding);
		if(astNode instanceof ast.TypeDeclaration){
			retNode = (ast.TypeDeclaration)astNode;
		}else{
			retNode = ast.TypeDeclaration.create();
			retNode.type = TraceabilityFactory.getType(typeBinding);
			retNode.fields = TraceabilityFactory.getDeclaredFields(typeBinding);
			factory.map(typeBinding, retNode);
			// typeBinding.getQualifiedName()
			// XXX. Has the name been set yet?
			factory.mapTypeDeclaration(retNode);
		}
		return retNode;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(type);
		// TODO: Fix me ...to deal with domains and domain parameters.
		// Ideally, generate FDJ code!
		return buffer.toString();
	}

	/**
	 * Return list of domains locally declared on this class.
	 */
	public List<String> getDomains() {
		return Collections.unmodifiableList(domains);
	}

	public void setDomains(List<String> domains) {
		// Maintain non-null invariant
		this.domains.clear();
		this.domains.addAll(domains);
	}
	
	public void setDomains(String[] domains) {
		List<String> domList = new ArrayList<String>();
		domList.addAll(Arrays.asList(domains));
		
		setDomains(domList);
	}
	
	/**
	 * Return list of domain parameters on this class.
	 * TODO: Rename: parameters -> domainParameters or domainParams
	 */
	public List<String> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	public void setParameters(List<String> parameters) {
		// Maintain non-null invariant		
		this.parameters.clear();
		this.parameters.addAll(parameters);
	}

	public void setParameters(String[] domainParams) {
		this.parameters.clear();
		for(String domainParam : domainParams ) {
			this.parameters.add(domainParam);
		}
	}
	
	public boolean hasDomain(String domainName) {
		return domains.contains(domainName);
    }
	
	@Transient
	// Delegate method.
	public String getFullyQualifiedName(){
		return type == null ? "" : type.getFullyQualifiedName();
	}

	public boolean hasDomainParam(String formalD) {
	    return parameters.contains(formalD);
    }
	
}
