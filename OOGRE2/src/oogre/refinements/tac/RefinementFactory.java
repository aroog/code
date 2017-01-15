package oogre.refinements.tac;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.core.dom.ITypeBinding;

import oog.itf.IObject;
import oog.re.IHeuristic;
import oog.re.IRefinement;
import oog.re.RefinementState;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

/**
 * Refinement factory:
 * - create OOGRE refinement object from each MOOG oog.re refinement Object
 * 
 * LOW. Should we do the Iterator semantics here? Give me the next refinement? No. Worklist has a queue.
 * 
 */
public class RefinementFactory {

	/**
	 * Create a facade heuristic from the local heuristic object
	 * @param heu
	 * @return
	 */
	public static oog.re.Heuristic createFacadeHeuristic(IHeuristic heu) {
		
		oog.re.Heuristic facadeHeu = new oog.re.Heuristic(heu.getSrcObject(), heu.getDstObject(), heu.getDomainName());
		// XXX. Need to set a ref ID too!
		// facadeHeu.setRefID(refID);
		// facadeHeu.setState(heu.getState());
		return facadeHeu;
	}
	
	// XXX. Gotta handle remaining cases: SplitUp
	// XXX.  Weird that create method is private in a factory!!!
	private static Refinement createRef(IRefinement facadeRef, TM tm) {

		Refinement oogRef = null;
		if (facadeRef instanceof oog.re.PushIntoOwned) {
			oog.re.PushIntoOwned pushIntoOwned = (oog.re.PushIntoOwned) facadeRef;
			IObject isrc = pushIntoOwned.getSrcIObject();
			IObject idst = pushIntoOwned.getDstIObject();
			oogRef = new PushIntoOwned(isrc, idst);
			oogRef.setFacadeOpr(facadeRef);
		}
		else if (facadeRef instanceof oog.re.PushIntoPD) {
			oog.re.PushIntoPD pushIntoPD = (oog.re.PushIntoPD) facadeRef;
			IObject isrc = pushIntoPD.getSrcIObject();
			IObject idst = pushIntoPD.getDstIObject();
			String domainName = pushIntoPD.getDomainName();
			// TOEBI: Leave MOOG untouched; here construct PushIntoPD2!
			oogRef = new PushIntoPD(isrc, idst, domainName);
			oogRef.setFacadeOpr(facadeRef);
		}
		else if (facadeRef instanceof oog.re.PushIntoParam) {
			oog.re.PushIntoParam pushIntoParam = (oog.re.PushIntoParam) facadeRef;
			IObject isrc = pushIntoParam.getSrcIObject();
			IObject idst = pushIntoParam.getDstIObject();
			oogRef = new PushIntoParam(isrc, idst);
			oogRef.setFacadeOpr(facadeRef);
		}
		else if (facadeRef instanceof oog.re.SplitUp) {
			oog.re.SplitUp splitUp = (oog.re.SplitUp) facadeRef;
			String auName = splitUp.getName();
			String aukind = splitUp.getKind();
			String auType = splitUp.getType();
			String auEncMthd = splitUp.getEnclosingMethod();
			String auEncType = splitUp.getEnclosingType();
			
			// TODO: Cleanup: remove eKind
			// TODO: Stop relying on var names. Just use the var type, encl. method, encl. class
			AnnotateUnitEnum eKind = null;
			if(aukind.equals("f")){
				eKind = AnnotateUnitEnum.f;
			}
			else if(aukind.equals("v")){
				eKind = AnnotateUnitEnum.v;
			}
			else if(aukind.equals("p")){
				eKind = AnnotateUnitEnum.p;
			}
			else {
				eKind = AnnotateUnitEnum.r;
			}
			Set<Variable> targetVars = new HashSet<Variable>();
			ITypeBinding targetVarsTypeBinding = null;
			Variable au = null;
			// XXX. For local variables as the target variables of SplitUp the look up does not work
			// because auName is empty string, so au is null, so refinement is null
			// We end up with NPE
			// Lookup variable in TM
			// TODO: Performance bottleneck: iterating over entire TM.
			for(Entry<Variable, Set<OType>> entry : tm.entrySet() ) {
				Variable var = entry.getKey();
				if(var instanceof SourceVariable) {
					SourceVariable srcVar = (SourceVariable)var;
					// TODO: Remove comparing name
					if(srcVar.getBinding().getType().getQualifiedName().equals(auType) && 
							// TODO: split into method and class..
							srcVar.getBinding().getDeclaringMethod().getName().equals(auEncMthd) &&
							srcVar.getBinding().getDeclaringMethod().getDeclaringClass().getQualifiedName().equals(auEncType)) {
						au = srcVar;
						targetVarsTypeBinding = au.resolveType();
						targetVars.add(au);
					}
				}
				else if(var instanceof TACVariable) {
					TACVariable srcVar = (TACVariable)var;
					if(srcVar.getSourceString().equals(auName) &&
							srcVar.getVarDecl().getType().getQualifiedName().equals(auType) && 
							srcVar.getVarDecl().getDeclaringClass().getQualifiedName().equals(auEncType)) {
						au = srcVar;
						targetVarsTypeBinding = au.resolveType();
						targetVars.add(au);
					}
				}
			}
			
			if(!targetVars.isEmpty()) {
				String destinationType = splitUp.getDstIObject().getC().fullyQualifiedName;
				String domainName = splitUp.getDomainName();
				IObject isrc = splitUp.getSrcIObject();
				IObject idst = splitUp.getDstIObject();
				// XXX. AU has to become a TAC variable
				oogRef = new SplitUp(isrc, idst,domainName, targetVars, targetVarsTypeBinding, destinationType);
				oogRef.setFacadeOpr(facadeRef);
			}
			else {
				System.err.println("Cannot find Variable");
			}
		}
		return oogRef;
	}
	
	/**
	 * Create OOGRE's copy of the Facade refinement objects, so we can clear the Facade
	 */
	public static Queue<Refinement> populateRefinements(List<IRefinement> facadeRefs, TM tm) {
		Queue<Refinement> refinements = new LinkedList<Refinement>();

		for (IRefinement facadeRef : facadeRefs) {
			// Here, just consider refinements that are still pending...
			
			// Skip over "implicit" refinements
			if(facadeRef.isImplicit()) {
				continue;
			}
			
			if (facadeRef.getState() == RefinementState.Pending) {
				Refinement oogreRef = createRef(facadeRef, tm);
				refinements.add(oogreRef);
			}
		}
		return refinements;
	}
}
