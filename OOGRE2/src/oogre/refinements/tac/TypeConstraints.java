package oogre.refinements.tac;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.Modifier;

import oogre.analysis.Config;
import oogre.analysis.OOGContext;

/**
 * Implement type-system specific-constraints for O.D.
 * 
 * 
 * XXX. Why not remove from set instead of make a copy for a subset?
 * - If the idea is to make the input set smaller, by calling these methods first 
 *
 */
public class TypeConstraints {
	
	private static boolean isPublicMethod(String methodName, String methodEnclosingClass){
		boolean isPublic = false;
		OOGContext context = OOGContext.getInstance();
		List<String> classPublicMethods = context.getClassPublicMethods(methodEnclosingClass);
		if(classPublicMethods != null && classPublicMethods.contains(methodName)){
			isPublic = true;
		}
		return isPublic;
	}
	
	/**
	 * T-Invk: cannot pass an argument with 'owned' in signature to a public method 
	 * 
	 * XXX. Pass receiver too to double-check
	 * 
	 * @param methodName
	 * @param methodEnclosingClass
	 * @param argumentTypingSet
	 * @return
	 */
	public static void tCall_PassingOwnedToPublicMethod(IMethodBinding methodBinding, Set<OType> argumentTypingSet){
		Iterator<OType> it = argumentTypingSet.iterator();
		if(Modifier.isPublic(methodBinding.getModifiers())){
			while (it.hasNext()) {
				OType oType = it.next();
				if(oType.getOwner().contains("owned") || oType.getAlpha().contains("owned")){
					it.remove();
				}
			}
		}
	}
	
	/**
	 * T-Invk: if calling method with 'owned' in signature, then receiver must be 'this'
	 * 
	 * @param receiverName
	 * @param methodTypingSet
	 * @return
	 */
	public static void tCall_ThisReceiverForOwnedMethod(String receiverName, Set<OType> methodTypingSet){
		Iterator<OType> it = methodTypingSet.iterator();
		if(!receiverName.equals("this")){
			while (it.hasNext()) {
				OType oType = it.next();
				if(oType!=null && (oType.getOwner()!= null && oType.getOwner().contains("owned")) || (oType.getAlpha()!= null && oType.getAlpha().contains("owned"))){
					it.remove();
				}
			}
		}
	}
	
	/**
	 * T-Write: 
	 * - if field is 'owned', then receiver must be 'this'
	 * 
	 * @param receiverName
	 * @param fieldTypingSet
	 * @return
	 */
	public static void tWrite(String receiverName, Set<OType> fieldTypingSet){
		Iterator<OType> it = fieldTypingSet.iterator();
		if(!receiverName.equals("this")){
			while (it.hasNext()) {
				OType oType = it.next();
				if(oType.getOwner().contains("owned")){
					it.remove();
				}
			}
		}
	}
	
	/**
	 * T-New: owning domain cannot be 'p'
	 * XXX. This may be problematic and may have to be relaxed
	 * 
	 * @param lhsName
	 * @param lhsTypingSet
	 * @return
	 */
	// Alt. way to rewrite T-New without returning a copy...Let the caller make a copy if needed to avoid CME
	public static void tNew(String lhsName, String enclosingClass, Set<OType> lhsTypingSet) {
		Iterator<OType> it = lhsTypingSet.iterator();
		while (it.hasNext()) {
			OType oType = it.next();
			if (oType.getOwner().equals("p")) {
				it.remove();
			}
			else{
				if(!enclosingClass.equals(Config.MAINCLASS) && oType.getOwner().equals("shared")){
					it.remove();
				}
			}
		}
	}
	
	/**
	 * T-New: 
	 * - An object cannot create itself in the public domain of another object
	 * - If an object cannot be in n.PD, it may be unique
	 * @param lhsTypingSet
	 * @return
	 */
	public static void tNewNoOthersPublicDomain(Set<OType> lhsTypingSet){
		Set<OType> uniqueSet = new SetOType<>();
		Iterator<OType> it = lhsTypingSet.iterator();
		while (it.hasNext()) {
			OType oType = it.next();
			if((oType.getOwner().contains(".PD") && !oType.getOwner().contains("this.PD")) || (oType.getAlpha().contains(".PD") && !oType.getAlpha().contains("this.PD"))){
				if(oType.getOwner().contains(".PD") && !oType.getOwner().contains("this.PD")){
					OType uOType = new OType("unique",oType.getAlpha());
					uniqueSet.add(uOType);
				}
				it.remove();
			}
		}
		lhsTypingSet.addAll(uniqueSet);
	}
	
	/**
	 * A method to check lent does not assign to a variable that needs more specific typing
	 * If left hand side of an assignment does not contain lent, them right hand side of that assignment cannot have lent
	 * @param lhsTypingSet
	 * @param rhsTypingSet
	 */
	public static void checkLentValidity(Set<OType> lhsTypingSet, Set<OType> rhsTypingSet){
		boolean lhsContainsLent = false;
		for (OType lhsOType : lhsTypingSet) {
			if(lhsOType.getOwner().equals("lent")){
				lhsContainsLent = true;
				break;
			}
		}
		if(!lhsContainsLent){
			Iterator<OType> it = rhsTypingSet.iterator();
			while (it.hasNext()) {
				OType rhsOType = it.next();
				if(rhsOType.getOwner().equals("lent")){
					it.remove();
				}
			}
		}
	}

}
