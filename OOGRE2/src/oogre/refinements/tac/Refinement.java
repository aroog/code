package oogre.refinements.tac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oog.itf.IObject;
import oog.re.IOperation;

import org.eclipse.jdt.core.dom.ITypeBinding;

import edu.cmu.cs.crystal.tac.model.Variable;

public abstract class Refinement extends BaseOperation implements oog.re.IRefinement {
	
    private IObject srcIObject;
	
	private IObject dstIObject;
	
	protected String dstDomain;
	
	// List of implicit refinements
	// NOTE: Store as IOperation objects since they are facade objects only
	private List<IOperation> implicits = new ArrayList<IOperation>();
	
	public Refinement(IObject src, IObject dst, String domainName) {
		this.srcIObject = src;

		this.dstIObject  = dst;
		
		this.dstDomain = domainName;
	}
	
	// XXX. Pull up to interface IOperation
	public abstract boolean refine(TM tm, TMSolutionType solIndex);
	
	@Override
    public String getSrcObject() {
		return srcIObject == null ?  "" : srcIObject.getTypeDisplayName();
    }

	@Override
    public String getDstObject() {
		return dstIObject == null ? "" : dstIObject.getTypeDisplayName();
    }

	@Override
    public String getDomainName() {
	    return this.dstDomain;
    }

	/**
	 * Resolve the string object id to a live object reference in the current graph
	 * @return
	 */
	protected IObject getSrcIObject() {
	    return srcIObject;
    }
	
	/**
	 * Resolve the string object id to a live object reference  in the current graph
	 * @return
	 */
	protected IObject getDstIObject() {
	    return dstIObject;
    }

	
	// TODO: Use to create a Facade refinement from local Unit test refinements.
	// TODO: Rename: getFacadeObject
	public abstract oog.re.Refinement getReal();
	
	
    @Override
    // NOTE: Do not use "commas" here. Otherwise, will need to be escaped.
    public String toString() {
	    return "Refinement [type=" + getClass().getSimpleName() + "  srcObject=" + getSrcObject() + "  dstObject=" + getDstObject() + "  dstDomain=" + dstDomain + "]";
    }
    
    // NOTE: Do not use "commas" here. Otherwise, will need to be escaped.
    public String toDisplayName() {
    	return getClass().getSimpleName() + "__srcObject=" + getSrcObject() + ";dstObject=" + getDstObject() + ";dstDomain=" + dstDomain + "";
    }

	/**
	 * Careful: these IOperations are only facade objects to minimize duplication
	 * @param opr
	 */
	public void addImplicit(IOperation opr) {
		this.implicits.add(opr);
	}
	
	public Collection<IOperation> getImplicits() {
		return Collections.unmodifiableList(implicits);
	}
	
	public void clearImplicits() {
		this.implicits.clear();
	}
	
	public abstract ITypeBinding srcObjectType();
}
