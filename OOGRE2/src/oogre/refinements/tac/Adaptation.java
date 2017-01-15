package oogre.refinements.tac;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import oogre.analysis.OOGContext;
import oogre.utils.Utils;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

// TODO: Weird that only adptRecvSet takes a TM;
// To match the formalization, there is one judgment form for adaptation.
// We need common/same signature for adaptation functions.
// Or make the TM a field; pass as constructor; and no longer use static methods
public class Adaptation {
	
	private static OOGContext context = OOGContext.getInstance();
	
	/**
	 * Qualifier-level adapt-out
	 * 
	 * @param in
	 * @param receiver
	 * @param receiverVar The receiver variable to retrieve the 'n', check if final, etc.
	 * @return
	 */
	public static Set<OType> adaptOut(OType in, OType receiver, Variable receiverVar, ITypeBinding innerType){
		
		IVariableBinding receiverbinding = null;
		if(receiverVar instanceof SourceVariable){
			SourceVariable srcVariable = (SourceVariable)receiverVar;
			receiverbinding = srcVariable.getBinding();
		}
		else if(receiverVar instanceof TACVariable){
			TACVariable srcVariable = (TACVariable)receiverVar;
			receiverbinding = srcVariable.getVarDecl();
		}
		// temporary variables, they can be final too is it is a field access

		StringBuilder typing = new StringBuilder();
		if(receiverbinding!=null){ // If receiver is not a temporary variable
			//When there is a final field to be used in the annotation
			if(Utils.isFinal(receiverbinding)){
				typing.append(receiverVar.getSourceString());
				typing.append(".");
				typing.append("PD");
				// Keep track of all the modifiers in form of n.PD
				context.addNPDModifier(typing.toString());
			}
			//If there is no final field the result is 'any'
			else{
				// typing.append(receiverVar.getSourceString());
				// typing.append(".");
				// typing.append("any");
				typing.append("lent");
			}
		}
		else { // Receiver is temporary variable
			if(context.isTempVariableFinal(receiverVar)){
				typing.append(receiverVar.getSourceString());
				typing.append(".");
				typing.append("PD");
				// Keep track of all the modifiers in form of n.PD
				context.addNPDModifier(typing.toString());
			}
			else{
				// typing.append(receiverVar.getSourceString());
				// typing.append(".");
				// typing.append("any");
				typing.append("lent");
			}
		}
		
		//Typing that is used when the receiver contains 'any'
		StringBuilder anyTyping = new StringBuilder();
//		anyTyping.append(receiverVar.getSourceString());
//		anyTyping.append(".");
//		anyTyping.append("any");
		anyTyping.append("lent");
	
		
		Set<OType> retValSet = new HashSet<OType>();
		OType retVal = null;
		
		// Extract type binding of receiver to check for generics
		ITypeBinding receiverTypebinding = receiverVar.resolveType();
		
		if(innerType.isParameterizedType() || receiverTypebinding.isParameterizedType() || (receiverTypebinding.getSuperclass()!=null && receiverTypebinding.getSuperclass().isParameterizedType())){
			if(innerType.isParameterizedType()){
				if(in.getOwner().equals("owner") && in.getAlpha().equals("owner")){
					if(in.getInner()!= null ){
						// Rule #1
						if(in.getInner().equals("owner")){
							retVal = new OType(receiver.getOwner(), receiver.getOwner() ,receiver.getOwner());
							retValSet.add(retVal);
						}
						// Rule #2
						else if(in.getInner().equals("p")){
							retVal = new OType(receiver.getOwner(), receiver.getOwner() ,receiver.getAlpha());
							retValSet.add(retVal);
						}
						// Rule #3
						else if(in.getInner().equals("shared")){
							retVal = new OType(receiver.getOwner(), receiver.getOwner() ,"shared");
							retValSet.add(retVal);
						}
					}
					else{
						// Not going to happen...
					}
				}
				else if(in.getOwner().equals("owner") && in.getAlpha().equals("p")){
					if(in.getInner() != null){
						// Rule #4
						if(in.getInner().equals("owner")){
							retVal = new OType(receiver.getOwner(), receiver.getAlpha() ,receiver.getOwner());
							retValSet.add(retVal);
						}
						// Rule #5
						if(in.getInner().equals("p")){
							retVal = new OType(receiver.getOwner(), receiver.getAlpha() ,receiver.getAlpha());
							retValSet.add(retVal);
						}
						// Rule #6
						if(in.getInner().equals("shared")){
							retVal = new OType(receiver.getOwner(), receiver.getAlpha() ,"shared");
							retValSet.add(retVal);
						}
					}
					else{
						if(receiverTypebinding.isParameterizedType()){
							ITypeBinding[] recTypeArguments = receiverTypebinding.getTypeArguments();
							if(recTypeArguments[0].isTypeVariable()){
								retVal = new OType(receiver.getOwner(), receiver.getAlpha());
								retValSet.add(retVal);
							}
							else{
								retVal = new OType(receiver.getOwner(), receiver.getAlpha(), receiver.getInner());
								retValSet.add(retVal);
							}
						}
					}
				}
				else if(in.getOwner().equals("owner") && in.getAlpha().equals("that.PD")){
					if(in.getInner() != null){
						// Rule #7
						if(in.getInner().equals("that.PD")){	
							retVal = new OType(receiver.getOwner(), typing.toString() , typing.toString());
							retValSet.add(retVal);
						}
						// Rule #8
						if(in.getInner().equals("owner")){	
							retVal = new OType(receiver.getOwner(), typing.toString() , receiver.getOwner());
							retValSet.add(retVal);
						}
						// Rule #9
						if(in.getInner().equals("p")){	
							retVal = new OType(receiver.getOwner(), typing.toString() , receiver.getAlpha());
							retValSet.add(retVal);
						}
						// Rule #10
						if(in.getInner().equals("shared")){	
							retVal = new OType(receiver.getOwner(), typing.toString() , "shared");
							retValSet.add(retVal);
						}
					}
					else{
						// Not going to happen
					}
				}
				else if(in.getOwner().equals("p") && in.getAlpha().equals("p")){
					if(in.getInner() != null){
						// Rule #17
						if(in.getInner().equals("p")){
							retVal = new OType(receiver.getAlpha(), receiver.getAlpha() ,receiver.getAlpha());
							retValSet.add(retVal);
						}
					}
					else{
						if(receiverTypebinding.isParameterizedType()){
							ITypeBinding[] recTypeArguments = receiverTypebinding.getTypeArguments();
							if(recTypeArguments[0].isTypeVariable()){
								retVal = new OType(receiver.getAlpha(), receiver.getAlpha());
								retValSet.add(retVal);
							}
							else{
								retVal = new OType(receiver.getAlpha(), receiver.getAlpha(), receiver.getInner());
								retValSet.add(retVal);
							}
						}
					}
				}
				else if(in.getOwner().equals("owner") && in.getAlpha().equals("shared")){
					if(in.getInner() != null){
						// Rule #52
						if(in.getInner().equals("shared")){
							retVal = new OType(receiver.getOwner(), "shared" ,"shared");
							retValSet.add(retVal);
						}
					}
					else{
						// Not going to happen...
					}
				}
				else if(in.getOwner().equals("p") && in.getAlpha().equals("shared")){
					if(in.getInner() != null){
						// Rule #19
						if(in.getInner().equals("shared")){
							retVal = new OType(receiver.getAlpha(), "shared" ,"shared");
							retValSet.add(retVal);
						}
					}
					else{
						// Not going to happen...
					}
				}
				else if(in.getOwner().equals("that.PD") && in.getAlpha().equals("shared")){
					if(in.getInner()!=null){
						// Rule #51
						if(in.getInner().equals("shared")){
							retVal = new OType(typing.toString(), "shared" ,"shared");
							retValSet.add(retVal);
						}
					}
					else{
						// Not going to happen...
					}
				}
				// Rule #32
				else if(in.getOwner().equals("shared") && in.getAlpha().equals("shared")){
					if(in.getInner() != null){
						if(in.getInner().equals("shared")){
							retVal = new OType("shared", "shared" ,"shared");
							retValSet.add(retVal);
						}
					}
					else{
						// Not going to happen...
					}
				}
				// lent adaptation cases
				else if(in.getOwner().equals("lent") && in.getAlpha().equals("owner")){
					if(in.getInner() != null){
						// adapt-l-o-o
						if(in.getInner().equals("owner")){
							retVal = new OType("this.owned", receiver.getOwner() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getOwner() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getOwner() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getOwner() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getOwner() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getOwner() , receiver.getOwner());
							retValSet.add(retVal);
						}
						// adapt-l-o-p
						else if(in.getInner().equals("p")){
							retVal = new OType("this.owned", receiver.getOwner() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getOwner() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getOwner() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getOwner() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getOwner() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getOwner() , receiver.getAlpha());
							retValSet.add(retVal);
						}
						// adapt-l-o-d
						else if(in.getInner().equals("that.PD")){
							retVal = new OType("this.owned", receiver.getOwner() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getOwner() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getOwner() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getOwner() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getOwner() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getOwner() , typing.toString());
							retValSet.add(retVal);
						}
						// adapt-l-o-s
						else if(in.getInner().equals("shared")){
							retVal = new OType("this.owned", receiver.getOwner() , "shared");
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getOwner() , "shared");
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getOwner() , "shared");
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getOwner() , "shared");
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getOwner() , "shared");
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getOwner() , "shared");
							retValSet.add(retVal);
						}
					}
					else{
						// Not going to happen...
					}
				}
				else if(in.getOwner().equals("lent") && in.getAlpha().equals("p")){
					if(in.getInner() != null){
						// adapt-l-p-o
						if(in.getInner().equals("owner")){
							retVal = new OType("this.owned", receiver.getAlpha(), receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getAlpha() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getAlpha() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getAlpha() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getAlpha() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getAlpha() , receiver.getOwner());
							retValSet.add(retVal);
						}
						// adapt-l-p-p
						else if(in.getInner().equals("p")){
							retVal = new OType("this.owned", receiver.getAlpha(), receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getAlpha() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getAlpha() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getAlpha() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getAlpha() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getAlpha() , receiver.getAlpha());
							retValSet.add(retVal);
						}
						// adapt-l-p-d
						else if(in.getInner().equals("that.PD")){
							retVal = new OType("this.owned", receiver.getAlpha(), typing.toString());
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getAlpha() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getAlpha() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getAlpha() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getAlpha() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getAlpha() , typing.toString());
							retValSet.add(retVal);
						}
						// adapt-l-p-s
						else if(in.getInner().equals("shared")){
							retVal = new OType("this.owned", receiver.getAlpha(), "shared");
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getAlpha() , "shared");
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getAlpha() , "shared");
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getAlpha() , "shared");
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getAlpha() , "shared");
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getAlpha() , "shared");
							retValSet.add(retVal);
						}
					}
					else{
						if(receiverTypebinding.isParameterizedType()){
							ITypeBinding[] recTypeArguments = receiverTypebinding.getTypeArguments();
							if(recTypeArguments[0].isTypeVariable()){
								retValSet.addAll(doNotCareOwner(receiver.getAlpha()));
							}
							else{
								retValSet.addAll(doNotCareOwnerGeneric(receiver.getAlpha(),receiver.getInner()));
							}
						}
					}
				}
				else if(in.getOwner().equals("lent") && in.getAlpha().equals("that.PD")){
					if(in.getInner() != null){
						// adapt-l-d-o
						if(in.getInner().equals("owner")){
							retVal = new OType("this.owned", typing.toString(), receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("this.PD", typing.toString(), receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("owner", typing.toString(), receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("p", typing.toString(), receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("shared", typing.toString(), receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("lent", typing.toString(), receiver.getOwner());
							retValSet.add(retVal);
						}
						// adapt-l-d-p
						else if(in.getInner().equals("p")){
							retVal = new OType("this.owned", typing.toString(), receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("this.PD", typing.toString(), receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("owner", typing.toString(), receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("p", typing.toString(), receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("shared", typing.toString(), receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("lent", typing.toString(), receiver.getAlpha());
							retValSet.add(retVal);
						}
						// adapt-l-d-d
						else if(in.getInner().equals("that.PD")){
							retVal = new OType("this.owned", typing.toString(), typing.toString());
							retValSet.add(retVal);
							retVal = new OType("this.PD", typing.toString(), typing.toString());
							retValSet.add(retVal);
							retVal = new OType("owner", typing.toString(), typing.toString());
							retValSet.add(retVal);
							retVal = new OType("p", typing.toString(), typing.toString());
							retValSet.add(retVal);
							retVal = new OType("shared", typing.toString(), typing.toString());
							retValSet.add(retVal);
							retVal = new OType("lent", typing.toString(), typing.toString());
							retValSet.add(retVal);
						}
						// adapt-l-d-s
						else if(in.getInner().equals("shared")){
							retVal = new OType("this.owned", typing.toString(), "shared");
							retValSet.add(retVal);
							retVal = new OType("this.PD", typing.toString(), "shared");
							retValSet.add(retVal);
							retVal = new OType("owner", typing.toString(), "shared");
							retValSet.add(retVal);
							retVal = new OType("p", typing.toString(), "shared");
							retValSet.add(retVal);
							retVal = new OType("shared", typing.toString(), "shared");
							retValSet.add(retVal);
							retVal = new OType("lent", typing.toString(), "shared");
							retValSet.add(retVal);
						}
					}
					else{
						// Not going to happen...
					}
				}
				else if(in.getOwner().equals("lent") && in.getAlpha().equals("shared")){
					if(in.getInner()!=null){
						// adapt-l-s-s
						if(in.getInner().equals("shared")){
							retVal = new OType("this.owned", "shared", "shared");
							retValSet.add(retVal);
							retVal = new OType("this.PD", "shared", "shared");
							retValSet.add(retVal);
							retVal = new OType("owner", "shared", "shared");
							retValSet.add(retVal);
							retVal = new OType("p", "shared", "shared");
							retValSet.add(retVal);
							retVal = new OType("shared", "shared", "shared");
							retValSet.add(retVal);
							retVal = new OType("lent", "shared", "shared");
							retValSet.add(retVal);
						}
					}
					else{
						// Not going to happen
					}
				}
				// unique adaptation cases
				else if(in.getOwner().equals("unique") && in.getAlpha().equals("owner")){
					if(in.getInner() != null){
						// adapt-u-o-o
						if(in.getInner().equals("owner")){
							retVal = new OType("this.owned", receiver.getOwner() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getOwner() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getOwner() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getOwner() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getOwner() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getOwner() , receiver.getOwner());
							retValSet.add(retVal);
						}
						// adapt-u-o-p
						else if(in.getInner().equals("p")){
							retVal = new OType("this.owned", receiver.getOwner() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getOwner() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getOwner() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getOwner() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getOwner() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getOwner() , receiver.getAlpha());
							retValSet.add(retVal);
						}
						// adapt-u-o-d
						else if(in.getInner().equals("that.PD")){
							retVal = new OType("this.owned", receiver.getOwner() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getOwner() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getOwner() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getOwner() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getOwner() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getOwner() , typing.toString());
							retValSet.add(retVal);
						}
						// adapt-u-o-s
						else if(in.getInner().equals("shared")){
							retVal = new OType("this.owned", receiver.getOwner() , "shared");
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getOwner() , "shared");
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getOwner() , "shared");
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getOwner() , "shared");
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getOwner() , "shared");
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getOwner() , "shared");
							retValSet.add(retVal);
						}
					}
					else{
						// Not going to happen
					}
				}
				else if(in.getOwner().equals("unique") && in.getAlpha().equals("p")){
					if(in.getInner() != null){
						// adapt-u-p-o
						if(in.getInner().equals("owner")){
							retVal = new OType("this.owned", receiver.getAlpha(), receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getAlpha() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getAlpha() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getAlpha() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getAlpha() , receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getAlpha() , receiver.getOwner());
							retValSet.add(retVal);
						}
						// adapt-u-p-p
						else if(in.getInner().equals("p")){
							retVal = new OType("this.owned", receiver.getAlpha(), receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getAlpha() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getAlpha() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getAlpha() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getAlpha() , receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getAlpha() , receiver.getAlpha());
							retValSet.add(retVal);
						}
						// adapt-u-p-d
						else if(in.getInner().equals("that.PD")){
							retVal = new OType("this.owned", receiver.getAlpha(), typing.toString());
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getAlpha() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getAlpha() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getAlpha() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getAlpha() , typing.toString());
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getAlpha() , typing.toString());
							retValSet.add(retVal);
						}
						// adapt-u-p-s
						else if(in.getInner().equals("shared")){
							retVal = new OType("this.owned", receiver.getAlpha(), "shared");
							retValSet.add(retVal);
							retVal = new OType("this.PD", receiver.getAlpha() , "shared");
							retValSet.add(retVal);
							retVal = new OType("owner", receiver.getAlpha() , "shared");
							retValSet.add(retVal);
							retVal = new OType("p", receiver.getAlpha() , "shared");
							retValSet.add(retVal);
							retVal = new OType("shared", receiver.getAlpha() , "shared");
							retValSet.add(retVal);
							retVal = new OType("lent", receiver.getAlpha() , "shared");
							retValSet.add(retVal);
						}
					}
					else{
						if(receiverTypebinding.isParameterizedType()){
							ITypeBinding[] recTypeArguments = receiverTypebinding.getTypeArguments();
							if(recTypeArguments[0].isTypeVariable()){
								retValSet.addAll(doNotCareOwner(receiver.getAlpha()));
							}
							else{
								retValSet.addAll(doNotCareOwnerGeneric(receiver.getAlpha(),receiver.getInner()));
							}
						}
					}
				}
				else if(in.getOwner().equals("unique") && in.getAlpha().equals("that.PD")){
					if(in.getInner() != null){
						// adapt-u-d-o
						if(in.getInner().equals("owner")){
							retVal = new OType("this.owned", typing.toString(), receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("this.PD", typing.toString(), receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("owner", typing.toString(), receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("p", typing.toString(), receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("shared", typing.toString(), receiver.getOwner());
							retValSet.add(retVal);
							retVal = new OType("lent", typing.toString(), receiver.getOwner());
							retValSet.add(retVal);
						}
						// adapt-u-d-p
						else if(in.getInner().equals("p")){
							retVal = new OType("this.owned", typing.toString(), receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("this.PD", typing.toString(), receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("owner", typing.toString(), receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("p", typing.toString(), receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("shared", typing.toString(), receiver.getAlpha());
							retValSet.add(retVal);
							retVal = new OType("lent", typing.toString(), receiver.getAlpha());
							retValSet.add(retVal);
						}
						// adapt-u-d-d
						else if(in.getInner().equals("that.PD")){
							retVal = new OType("this.owned", typing.toString(), typing.toString());
							retValSet.add(retVal);
							retVal = new OType("this.PD", typing.toString(), typing.toString());
							retValSet.add(retVal);
							retVal = new OType("owner", typing.toString(), typing.toString());
							retValSet.add(retVal);
							retVal = new OType("p", typing.toString(), typing.toString());
							retValSet.add(retVal);
							retVal = new OType("shared", typing.toString(), typing.toString());
							retValSet.add(retVal);
							retVal = new OType("lent", typing.toString(), typing.toString());
							retValSet.add(retVal);
						}
						// adapt-u-d-s
						else if(in.getInner().equals("shared")){
							retVal = new OType("this.owned", typing.toString(), "shared");
							retValSet.add(retVal);
							retVal = new OType("this.PD", typing.toString(), "shared");
							retValSet.add(retVal);
							retVal = new OType("owner", typing.toString(), "shared");
							retValSet.add(retVal);
							retVal = new OType("p", typing.toString(), "shared");
							retValSet.add(retVal);
							retVal = new OType("shared", typing.toString(), "shared");
							retValSet.add(retVal);
							retVal = new OType("lent", typing.toString(), "shared");
							retValSet.add(retVal);
						}
					}
					else{
						// Not going to happen...
					}
				}
				else if(in.getOwner().equals("unique") && in.getAlpha().equals("shared")){
					if(in.getInner()!=null){
						// adapt-u-s-s
						if(in.getInner().equals("shared")){
							retVal = new OType("this.owned", "shared", "shared");
							retValSet.add(retVal);
							retVal = new OType("this.PD", "shared", "shared");
							retValSet.add(retVal);
							retVal = new OType("owner", "shared", "shared");
							retValSet.add(retVal);
							retVal = new OType("p", "shared", "shared");
							retValSet.add(retVal);
							retVal = new OType("shared", "shared", "shared");
							retValSet.add(retVal);
							retVal = new OType("lent", "shared", "shared");
							retValSet.add(retVal);
						}
					}
					else{
						// Not going to happen...
					}
				}
			}
			// If the type of the receiver is a generic type
			if((receiverTypebinding.isParameterizedType() || ( receiverTypebinding.getSuperclass()!=null && receiverTypebinding.getSuperclass().isParameterizedType())) && !innerType.isParameterizedType()){
				boolean isTypeVariable = false;
				ITypeBinding[] typeArguments = receiverVar.resolveType().getTypeArguments();
				if(typeArguments.length >0){
					isTypeVariable = typeArguments[0].isTypeVariable();
				}
				if(isTypeVariable){
					if(in.getOwner().equals("p") && in.getAlpha()==null){
						retVal = new OType(receiver.getAlpha(), null);
						retValSet.add(retVal);
					}
				}
				else{
					if(in.getOwner().equals("p") && in.getAlpha()==null){
						retVal = new OType(receiver.getAlpha(), receiver.getInner());
						retValSet.add(retVal);
					}
					else if(in.getOwner().equals("shared") && in.getAlpha().equals("shared")){
						retVal = new OType("shared", "shared");
						retValSet.add(retVal);
					}
					// lent adaptation cases
					// adapt-l-o
					else if(in.getOwner().equals("lent") && in.getAlpha().equals("owner")){
						retValSet.addAll(doNotCareOwner(receiver.getOwner()));
						
					}
					// adapt-l-p
					else if(in.getOwner().equals("lent") && in.getAlpha().equals("p")){
						retValSet.addAll(doNotCareOwner(receiver.getAlpha()));
						retValSet.addAll(doNotCareOwnerGeneric(receiver.getAlpha(), receiver.getInner()));
					}
					// adapt-l-d
					else if(in.getOwner().equals("lent") && in.getAlpha().equals("that.PD")){
						retValSet.addAll(doNotCareOwner(typing.toString()));
					}
					// adapt-l-s
					else if(in.getOwner().equals("lent") && in.getAlpha().equals("shared")){
						retValSet.addAll(doNotCareOwner("shared"));
					}
					// unique adaptation cases
					// adapt-l-o
					else if(in.getOwner().equals("unique") && in.getAlpha().equals("owner")){
						retValSet.addAll(doNotCareOwner(receiver.getOwner()));
					}
					// adapt-l-p
					else if(in.getOwner().equals("unique") && in.getAlpha().equals("p")){
						retValSet.addAll(doNotCareOwner(receiver.getAlpha()));
					}
					// adapt-l-d
					else if(in.getOwner().equals("unique") && in.getAlpha().equals("that.PD")){
						retValSet.addAll(doNotCareOwner(typing.toString()));
					}
					// adapt-l-s
					else if(in.getOwner().equals("unique") && in.getAlpha().equals("shared")){
						retValSet.addAll(doNotCareOwner("shared"));
					}
					// Adapt-O-O
					else if(in.getOwner().equals("owner") && in.getAlpha().equals("owner")){
						retVal = new OType(receiver.getOwner(), receiver.getOwner());
						retValSet.add(retVal);
					}
					// Adapt-O-A
					else if(in.getOwner().equals("owner") && in.getAlpha().equals("p")){
						retVal = new OType(receiver.getOwner(), receiver.getAlpha());
						retValSet.add(retVal);
					}
					// XXX. New set of adaptation cases
					else if(in.getOwner().equals("p") && in.getAlpha().equals("owner")){
						retVal = new OType(receiver.getAlpha(), receiver.getOwner());
						retValSet.add(retVal);
					}
					// end New set of adaptation cases
					// Adapt-A-A
					else if(in.getOwner().equals("p") && in.getAlpha().equals("p")){
						retVal = new OType(receiver.getAlpha(), receiver.getAlpha());
						retValSet.add(retVal);
						retVal = new OType(receiver.getAlpha(), receiver.getAlpha(), receiver.getInner());
						retValSet.add(retVal);
					}
					// Adapt-O-S
					else if(in.getOwner().equals("owner") && in.getAlpha().equals("shared")){
						retVal = new OType(receiver.getOwner(), "shared");
						retValSet.add(retVal);
					}
					// Adapt-A-S
					else if(in.getOwner().equals("p") && in.getAlpha().equals("shared")){
						retVal = new OType(receiver.getAlpha(), "shared");
						retValSet.add(retVal);
					}
					// Adapt-S-S
					else if(in.getOwner().equals("shared") && in.getAlpha().equals("shared")){
						retVal = new OType("shared", "shared");
						retValSet.add(retVal);
					}
				}
			}
		}
		else{
			// 6 basic rules from Huang and Milanova's work
			// Adapt-O-O
			if(in.getOwner().equals("owner") && in.getAlpha().equals("owner")){
				retVal = new OType(receiver.getOwner(), receiver.getOwner());
				retValSet.add(retVal);
			}
			// Adapt-O-A
			else if(in.getOwner().equals("owner") && in.getAlpha().equals("p")){
				retVal = new OType(receiver.getOwner(), receiver.getAlpha());
				retValSet.add(retVal);
			}
			// XXX. New set of adaptation cases
			else if(in.getOwner().equals("p") && in.getAlpha().equals("owner")){
				retVal = new OType(receiver.getAlpha(), receiver.getOwner());
				retValSet.add(retVal);
			}
			// end New set of adaptation cases
			// Adapt-A-A
			else if(in.getOwner().equals("p") && in.getAlpha().equals("p")){
				retVal = new OType(receiver.getAlpha(), receiver.getAlpha());
				retValSet.add(retVal);
			}
			// Adapt-O-S
			else if(in.getOwner().equals("owner") && in.getAlpha().equals("shared")){
				retVal = new OType(receiver.getOwner(), "shared");
				retValSet.add(retVal);
			}
			// Adapt-A-S
			else if(in.getOwner().equals("p") && in.getAlpha().equals("shared")){
				retVal = new OType(receiver.getAlpha(), "shared");
				retValSet.add(retVal);
			}
			// Adapt-S-S
			else if(in.getOwner().equals("shared") && in.getAlpha().equals("shared")){
				retVal = new OType("shared", "shared");
				retValSet.add(retVal);
			}
			//Extended rules for adapt
			
			// In the following, D == D1 or D == D2 where D1 == n.PD and D2 ==n.any
			// Adapt-D-D 
			else if(in.getOwner().contains("that.PD") && in.getAlpha().contains("that.PD")){
				retVal = new OType(typing.toString(), typing.toString());
				retValSet.add(retVal);
			}
			// Adapt-D-O 
			else if(in.getOwner().contains("that.PD") && in.getAlpha().equals("owner")){
				retVal = new OType(typing.toString(), receiver.getOwner());
				retValSet.add(retVal);
			}
			// Adapt-D-A
			else if(in.getOwner().contains("that.PD") && in.getAlpha().equals("p")){
				retVal = new OType(typing.toString(), receiver.getAlpha());
				retValSet.add(retVal);
			}
			// Adapt-D-S
			else if(in.getOwner().contains("that.PD") && in.getAlpha().equals("shared")){
				retVal = new OType(typing.toString(), "shared");
				retValSet.add(retVal);
			}
			// Adapt-O-D
			else if(in.getOwner().equals("owner") && in.getAlpha().contains("that.PD")){
				retVal = new OType(receiver.getOwner(), typing.toString());
				retValSet.add(retVal);
			}
			// Adapt-A-D
			else if(in.getOwner().equals("p") && in.getAlpha().contains("that.PD")){
				retVal = new OType(receiver.getAlpha(), typing.toString());
				retValSet.add(retVal);
			}
			// Adapt-S-D
			else if(in.getOwner().equals("shared") && in.getAlpha().contains("that.PD")){
				retVal = new OType("shared", typing.toString());
				retValSet.add(retVal);
			}
			// Adapt-ndX-ndY rules (inner is n.d)
			// Adapt-ND-ND
			// XXX. Difference with formalization: we always return 'n.any'. We never construct 'n1.n2.PD'
			else if((!in.getOwner().contains("that.") && in.getOwner().contains("PD")) && (!in.getAlpha().contains("that.") && in.getAlpha().contains("PD"))){
				retVal = new OType(anyTyping.toString(), anyTyping.toString());
				retValSet.add(retVal);
			}
			// Adapt-ND-D (where D == D1 or D == D2)
			else if((!in.getOwner().contains("that.") && in.getOwner().contains("PD")) && (in.getAlpha().contains("that.PD"))){
				if(Utils.isFinal(receiverbinding)){
					retVal = new OType(anyTyping.toString(), typing.toString());
					retValSet.add(retVal);
				}
				else{
					retVal = new OType(anyTyping.toString(), anyTyping.toString());
					retValSet.add(retVal);
				}
			}
			// Adapt-D-ND
			else if((in.getOwner().contains("that.PD")) && (!in.getAlpha().contains("that.") && in.getAlpha().contains("PD"))){
				if(Utils.isFinal(receiverbinding)){
					retVal = new OType(typing.toString(), anyTyping.toString());
					retValSet.add(retVal);
				}
				else{
					retVal = new OType(anyTyping.toString(), anyTyping.toString());
					retValSet.add(retVal);
				}
			}
			// Adapt-ND-O
			else if((!in.getOwner().contains("that.") && in.getOwner().contains("PD")) && in.getAlpha().equals("owner")){
				retVal = new OType(anyTyping.toString(), receiver.getOwner());
				retValSet.add(retVal);
			}
			// Adapt-O-ND
			else if(in.getOwner().equals("owner") && (!in.getAlpha().contains("that.") && in.getAlpha().contains("PD"))){
				retVal = new OType(receiver.getOwner(), anyTyping.toString());
				retValSet.add(retVal);
			}
			// Adapt-ND-A
			else if((!in.getOwner().contains("that.") && in.getOwner().contains("PD")) && in.getAlpha().equals("p")){
				retVal = new OType(anyTyping.toString(), receiver.getAlpha());
				retValSet.add(retVal);
			}
			// Adapt-A-ND
			else if(in.getOwner().equals("p") && (!in.getAlpha().contains("that.") && in.getAlpha().contains("PD"))){
				retVal = new OType(receiver.getAlpha(), anyTyping.toString());
				retValSet.add(retVal);
			}
			// Adapt-ND-S 
			else if((!in.getOwner().contains("that.") && in.getOwner().contains("PD")) && in.getAlpha().equals("shared")){
				retVal = new OType(anyTyping.toString(), "shared");
				retValSet.add(retVal);
			}
			// Adapt-S-ND 
			else if(in.getOwner().equals("shared") && (!in.getAlpha().contains("that.") && in.getAlpha().contains("PD"))){
				retVal = new OType("shared", anyTyping.toString());
				retValSet.add(retVal);
			}
			// When the inner contains n.any (commented out in the formalization)
			// adapt-anyX-anyY rules
			// adapt-any-any rule
			else if(in.getOwner().contains("that.any") && in.getAlpha().contains("that.any")){
				retVal = new OType(anyTyping.toString(), anyTyping.toString());
				retValSet.add(retVal);
			}
			//adapt-nd-any rule
			else if((!in.getOwner().contains("that.") && in.getOwner().contains("PD")) && in.getAlpha().contains("that.any")){
				retVal = new OType(anyTyping.toString(), anyTyping.toString());
				retValSet.add(retVal);
			}
			//adapt-any-nd rule
			else if(in.getOwner().contains("that.any") && (!in.getAlpha().contains("that.") && in.getAlpha().contains("PD"))){
				retVal = new OType(anyTyping.toString(), anyTyping.toString());
				retValSet.add(retVal);
			}
			//adapt-o-any rule
			else if(in.getOwner().equals("owner") && in.getAlpha().contains("that.any")){
				retVal = new OType(receiver.getOwner(), anyTyping.toString());
				retValSet.add(retVal);
			}
			//adapt-any-o rule
			else if(in.getOwner().contains("that.any") && in.getAlpha().equals("owner")){
				retVal = new OType(anyTyping.toString(), receiver.getOwner());
				retValSet.add(retVal);
			}
			//adapt-a-any rule
			else if(in.getOwner().equals("p") && in.getAlpha().contains("that.any")){
				retVal = new OType(receiver.getAlpha(), anyTyping.toString());
				retValSet.add(retVal);
			}
			//adapt-any-a rule
			else if(in.getOwner().contains("that.any") && in.getAlpha().equals("p")){
				retVal = new OType(anyTyping.toString(), receiver.getAlpha());
				retValSet.add(retVal);
			}
			//adapt-s-any rule
			else if(in.getOwner().equals("shared") && in.getAlpha().contains("that.any")){
				retVal = new OType("shared", anyTyping.toString());
				retValSet.add(retVal);
			}
			//adapt-any-s rule
			else if(in.getOwner().contains("that.any") && in.getAlpha().equals("shared")){
				retVal = new OType(anyTyping.toString(), "shared");
				retValSet.add(retVal);
			}
			// lent adaptation cases
			//adapt-l-o rule
			else if(in.getOwner().contains("lent") && in.getAlpha().equals("owner")){
				retValSet.addAll(doNotCareOwner(receiver.getOwner()));
			}
			//adapt-l-p rule
			else if(in.getOwner().contains("lent") && in.getAlpha().equals("p")){
				retValSet.addAll(doNotCareOwner(receiver.getAlpha()));
			}
			//adapt-l-D rule
			else if(in.getOwner().contains("lent") && in.getAlpha().contains("that.PD")){
				retValSet.addAll(doNotCareOwner(typing.toString()));
			}
			//adapt-l-s rule
			else if(in.getOwner().contains("lent") && in.getAlpha().equals("shared")){
				retValSet.addAll(doNotCareOwner("shared"));
			}
			// unique adaptation cases
			//adapt-u-o rule
			else if(in.getOwner().contains("unique") && in.getAlpha().equals("owner")){
				retValSet.addAll(doNotCareOwner(receiver.getOwner()));
			}
			//adapt-u-p rule
			else if(in.getOwner().contains("unique") && in.getAlpha().equals("p")){
				retValSet.addAll(doNotCareOwner(receiver.getAlpha()));
			}
			//adapt-u-D rule
			else if(in.getOwner().contains("unique") && in.getAlpha().contains("that.PD")){
				retValSet.addAll(doNotCareOwner(typing.toString()));
			}
			//adapt-u-s rule
			else if(in.getOwner().contains("unique") && in.getAlpha().equals("shared")){
				retValSet.addAll(doNotCareOwner("shared"));
			}
		}
		return retValSet;
	}

	/**
	 * Set-level adapt out
	 * @param in: inner set
	 * @param receiver: receiver set:
	 * @param receiverVar: receiver variable
	 * @return outer set
	 */
	public static Set<OType> adaptOutSet(Set<OType> in, Set<OType> receiver, Variable receiverVar, ITypeBinding innerType){
		Set<OType> retSet = new HashSet<OType>();
		for (OType inOType : in) {
			for (OType recOType : receiver) {
				Set<OType> newOType = adaptOut(inOType,recOType, receiverVar, innerType);
				if(!newOType.isEmpty())
					retSet.addAll(newOType);
			}
		}
		return retSet;
	}
	
	/**
	 * Qualifier-level adapt-in
	 * 
	 * @param out
	 * @param receiver
	 * @param receiverVar The receiver variable to retrieve the 'n', check if final, etc.
	 * @return
	 */
	public static Set<OType> adaptIn(OType out, OType receiver, Variable receiverVar, ITypeBinding innerType){
		Set<OType> retVal = new HashSet<OType>();
		
		ITypeBinding receiverType = receiverVar.resolveType();
		String receiverName = receiverVar.getSourceString();
		
		// Build n.PD where n is the receiver
		StringBuilder nPD = new StringBuilder();
		nPD.append(receiverName);
		nPD.append(".");
		nPD.append("PD");
		String nPDQualifier = nPD.toString();
		boolean isFinalReceiver = false;
		IVariableBinding receiverbinding = null;
		if(receiverVar instanceof SourceVariable){
			SourceVariable srcVariable = (SourceVariable)receiverVar;
			receiverbinding = srcVariable.getBinding();
		}
		else if(receiverVar instanceof TACVariable){
			TACVariable srcVariable = (TACVariable)receiverVar;
			receiverbinding = srcVariable.getVarDecl();
		}
		
		if(receiverbinding!=null){
			//When there is a final field to be used in the annotation
			if(Utils.isFinal(receiverbinding)){
				isFinalReceiver = true;
			}
		}
		else{
			if(context.isTempVariableFinal(receiverVar)){
				isFinalReceiver = true;
			}
		}
		
		if(receiverType.isParameterizedType() || (receiverType.getSuperclass() != null && receiverType.getSuperclass().isParameterizedType()) || innerType.isParameterizedType()){
			if((receiverType.isParameterizedType() || (receiverType.getSuperclass()!=null && receiverType.getSuperclass().isParameterizedType())) && !innerType.isParameterizedType()){
				boolean isTypeVariable = false;
				ITypeBinding[] typeArguments = receiverVar.resolveType().getTypeArguments();
				if(typeArguments.length >0){
					isTypeVariable = typeArguments[0].isTypeVariable();
				}
				if(isTypeVariable){
					if(out.getOwner().equals(receiver.getAlpha())){
						OType retOType = new OType( "p", null);
						retVal.add(retOType);
					}
				}
				else{
					if(out.getOwner().equals(receiver.getAlpha()) && out.getAlpha().equals(receiver.getInner())){
						OType retOType = new OType( "p", null);
						retVal.add(retOType);
					}
					if(innerType.getQualifiedName().equals("java.lang.String")){
						OType retOType = new OType( "shared", "shared");
						retVal.add(retOType);
					}
					// lent adaptation cases
					// adapt-l-o
					if(out.getAlpha().equals(receiver.getOwner())){
						OType retOType = new OType( "lent", "owner");
						retVal.add(retOType);
					}
					// adapt-l-p
					if(out.getAlpha().equals(receiver.getAlpha())){
						OType retOType = new OType( "lent", "p");
						retVal.add(retOType);
					}
					// adapt-l-p
					if(out.getAlpha().contains(nPDQualifier) ){
						OType retOType = new OType( "lent", "that.PD");
						retVal.add(retOType);
					}
					// adapt-l-s
					if(out.getAlpha().equals("shared") ){
						OType retOType = new OType( "lent", "shared");
						retVal.add(retOType);
					}
					// unique adaptation cases
					// adapt-u-o
					if(out.getAlpha().equals(receiver.getOwner())){
						OType retOType = new OType( "unique", "owner");
						retVal.add(retOType);
					}
					// adapt-u-p
					if(out.getAlpha().equals(receiver.getAlpha())){
						OType retOType = new OType( "unique", "p");
						retVal.add(retOType);
					}
					// adapt-u-p
					if(out.getAlpha().contains(nPDQualifier) ){
						OType retOType = new OType( "unique", "that.PD");
						retVal.add(retOType);
					}
					// adapt-u-s
					if(out.getAlpha().equals("shared") ){
						OType retOType = new OType( "unique", "shared");
						retVal.add(retOType);
					}
					if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().equals(receiver.getOwner())){
						OType retOType = new OType( "owner", "owner");
						retVal.add(retOType);
					}
					// Adapt-O-A			
					if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().equals(receiver.getAlpha())){
						OType retOType = new OType( "owner", "p");
						retVal.add(retOType);
					}
					// Adapt-A-A			
					if(out.getOwner().equals(receiver.getAlpha()) && out.getAlpha().equals(receiver.getAlpha())){
						OType retOType = new OType( "p", "p");
						retVal.add(retOType);
					}
					// Adapt-O-S			
					if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().equals("shared")){
						OType retOType = new OType( "owner", "shared");
						retVal.add(retOType);
					}
					// Adapt-A-S			
					if(out.getOwner().equals(receiver.getAlpha()) && out.getAlpha().equals("shared")){
						OType retOType = new OType( "p", "shared");
						retVal.add(retOType);
					}
					// Adapt-S-S
					if(out.getOwner().equals("shared") && out.getAlpha().equals("shared")){
						OType retOType = new OType( "shared", "shared");
						retVal.add(retOType);
					}
					// XXX. New set of adaptation cases
					if(out.getOwner().equals(receiver.getAlpha()) && out.getAlpha().equals(receiver.getOwner())){
						OType retOType = new OType( "p", "owner");
						retVal.add(retOType);
					}
					//When the inner is lent<p> but p is for a type variable -> Collection.addAll(lent<p> col)
					if(out.getInner()!=null){
						if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().equals(receiver.getAlpha()) && out.getInner().equals(receiver.getInner())){
							OType retOType = new OType( "lent", "p");
							retVal.add(retOType);
							retOType = new OType( "unique", "p");
							retVal.add(retOType);
						}
					}
				}
			}
			else if(innerType.isParameterizedType()){
				if(innerType.isTypeVariable()){
					if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().equals(receiver.getAlpha()) && out.getInner().equals(receiver.getInner())){
						OType retOType = new OType( "owner", "p");
						retVal.add(retOType);
					}
					if(out.getOwner().equals(receiver.getAlpha()) && out.getAlpha().equals(receiver.getAlpha()) && out.getInner().equals(receiver.getInner())){
						OType retOType = new OType( "p", "p");
						retVal.add(retOType);
					}
					if(out.getAlpha().equals(receiver.getAlpha()) && out.getInner().equals(receiver.getInner())){
						OType retOType = new OType( "lent", "p");
						retVal.add(retOType);
						retOType = new OType( "unique", "p");
						retVal.add(retOType);
					}
				}
				else{
					ITypeBinding[] typeArguments = innerType.getTypeArguments();
					if(typeArguments[0].isTypeVariable()){
						if(out.getOwner().equals("unique") && out.getAlpha().equals(receiver.getAlpha())){
							retVal.addAll(doNotCareOwner(out.getAlpha()));
						}
						if(out.getOwner().equals("lent") && out.getAlpha().equals(receiver.getAlpha())){
							retVal.addAll(doNotCareOwner(out.getAlpha()));
						}
						if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().equals(receiver.getAlpha())){
							OType retOType = new OType( "owner", "p");
							retVal.add(retOType);
						}
						if(out.getOwner().equals(receiver.getAlpha()) && out.getAlpha().equals(receiver.getAlpha())){
							OType retOType = new OType( "p", "p");
							retVal.add(retOType);
						}
					}
					else{
					if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().equals(receiver.getOwner())){
						// Rule #1
						if(out.getInner().equals(receiver.getOwner())){
							OType retOType = new OType( "owner", "owner", "owner");
							retVal.add(retOType);
						}
						// Rule #2
						if(out.getInner().equals(receiver.getAlpha())){
							OType retOType = new OType( "owner", "owner", "p");
							retVal.add(retOType);
						}
						// Rule #3
						if(out.getInner().equals("shared")){
							OType retOType = new OType( "owner", "owner", "shared");
							retVal.add(retOType);
						}
						//XXX. what is this condition?
						else{
							OType retOType = new OType( "owner", "owner");
							retVal.add(retOType);
						}
					}
					if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().equals(receiver.getAlpha())){
						// Rule #4
						if(out.getInner().equals(receiver.getOwner())){
							OType retOType = new OType( "owner", "p", "owner");
							retVal.add(retOType);
						}
						// Rule #5
						if(out.getInner().equals(receiver.getAlpha())){
							OType retOType = new OType( "owner", "p", "p");
							retVal.add(retOType);
						}
						// Rule #6
						if(out.getInner().equals("shared")){
							OType retOType = new OType( "owner", "p", "shared");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "owner", "p");
							retVal.add(retOType);
						}
					}
					if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().contains(nPDQualifier)){
						// Rule #7
						if(out.getInner().contains(nPDQualifier)){
							OType retOType = new OType( "owner", "that.PD", "that.PD");
							retVal.add(retOType);
						}
						// Rule #8
						if(out.getInner().equals(receiver.getOwner())){
							OType retOType = new OType( "owner", "that.PD", "owner");
							retVal.add(retOType);
						}
						// Rule #9
						if(out.getInner().equals(receiver.getAlpha())){
							OType retOType = new OType( "owner", "that.PD", "p");
							retVal.add(retOType);
						}
						// Rule #10
						if(out.getInner().equals("shared")){
							OType retOType = new OType( "owner", "that.PD", "shared");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "owner", "that.PD");
							retVal.add(retOType);
						}
					}
					if(out.getOwner().equals(receiver.getAlpha()) && out.getAlpha().equals(receiver.getAlpha())){
						// Rule #17
						if(out.getInner().equals(receiver.getAlpha())){
							OType retOType = new OType( "p", "p", "p");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "p", "p");
							retVal.add(retOType);
						}
					}
					if(out.getOwner().equals(receiver.getAlpha()) && out.getAlpha().equals("shared")){
						// Rule #19
						if(out.getInner().equals("shared")){
							OType retOType = new OType( "p", "shared", "shared");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "p", "shared");
							retVal.add(retOType);
						}
					}
					// Rule #32
					if(out.getOwner().equals("shared") && out.getAlpha().equals("shared")){
						if(out.getInner().equals("shared")){
							OType retOType = new OType( "shared", "shared", "shared");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "shared", "shared");
							retVal.add(retOType);
						}
					}
					// Rule #51
					if(out.getOwner().contains(nPDQualifier) && out.getAlpha().equals("shared")){
						if(out.getInner().equals("shared")){
							OType retOType = new OType( "that.PD", "shared", "shared");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "that.PD", "shared");
							retVal.add(retOType);
						}
					}
					// Rule #52
					if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().equals("shared")){
						if(out.getInner().equals("shared")){
							OType retOType = new OType( "owner", "shared", "shared");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "owner", "shared");
							retVal.add(retOType);
						}
					}
					// lent adaptation cases
					// adapt-l-o-o , adapt-u-o-o  
					if(out.getAlpha().equals(receiver.getOwner())){
						if(out.getInner().equals(receiver.getOwner())){
							OType retOType = new OType( "lent", "owner", "owner");
							retVal.add(retOType);
							retOType = new OType( "unique", "owner", "owner");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "lent", "owner");
							retVal.add(retOType);
							retOType = new OType( "unique", "owner");
							retVal.add(retOType);
						}
					}
					// adapt-l-o-p, adapt-u-o-p
					if(out.getAlpha().equals(receiver.getOwner())){
						if(out.getInner().equals(receiver.getAlpha())){
							OType retOType = new OType( "lent", "owner", "p");
							retVal.add(retOType);
							retOType = new OType( "unique", "owner", "p");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "lent", "owner");
							retVal.add(retOType);
							retOType = new OType( "unique", "owner");
							retVal.add(retOType);
						}
					}
					// adapt-l-o-d, adapt-u-o-d
					if(out.getAlpha().equals(receiver.getOwner())){
						if(out.getInner().contains(nPDQualifier)){
							OType retOType = new OType( "lent", "owner", "that.PD");
							retVal.add(retOType);
							retOType = new OType( "unique", "owner", "that.PD");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "lent", "owner");
							retVal.add(retOType);
							retOType = new OType("unique", "owner");
							retVal.add(retOType);
						}
					}
					// adapt-l-o-s, adapt-u-o-s
					if(out.getAlpha().equals(receiver.getOwner())){
						if(out.getInner().equals("shared")){
							OType retOType = new OType( "lent", "owner", "shared");
							retVal.add(retOType);
							retOType = new OType( "unique", "owner", "shared");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "lent", "owner");
							retVal.add(retOType);
							retOType = new OType( "unique", "owner");
							retVal.add(retOType);
						}
					}
					// adapt-l-p-o, adapt-u-p-o
					if(out.getAlpha().equals(receiver.getAlpha())){
						if(out.getInner().equals(receiver.getOwner())){
							OType retOType = new OType( "lent", "p", "owner");
							retVal.add(retOType);
							retOType = new OType( "unique", "p", "owner");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "lent", "p");
							retVal.add(retOType);
							retOType = new OType( "unique", "p");
							retVal.add(retOType);
						}
					}
					// adapt-l-p-p, adapt-u-p-p
					if(out.getAlpha().equals(receiver.getAlpha())){
						if(out.getInner().equals(receiver.getAlpha())){
							OType retOType = new OType( "lent", "p", "p");
							retVal.add(retOType);
							retOType = new OType( "unique", "p", "p");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "lent", "p");
							retVal.add(retOType);
							retOType = new OType( "unique", "p");
							retVal.add(retOType);
						}
					}
					// adapt-l-p-d, adapt-u-p-d
					if(out.getAlpha().equals(receiver.getAlpha())){
						if(out.getInner().contains(nPDQualifier)){
							OType retOType = new OType( "lent", "p", "that.PD");
							retVal.add(retOType);
							retOType = new OType( "unique", "p", "that.PD");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "lent", "p");
							retVal.add(retOType);
							retOType = new OType( "unique", "p");
							retVal.add(retOType);
						}
					}
					// adapt-l-p-s, adapt-u-p-s
					if(out.getAlpha().equals(receiver.getAlpha())){
						if(out.getInner().equals("shared")){
							OType retOType = new OType( "lent", "p", "shared");
							retVal.add(retOType);
							retOType = new OType( "unique", "p", "shared");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "lent", "p");
							retVal.add(retOType);
							retOType = new OType( "unique", "p");
							retVal.add(retOType);
						}
					}
					// adapt-l-d-o, adapt-u-d-o
					if(out.getAlpha().contains(nPDQualifier)){
						if(out.getInner().equals(receiver.getOwner())){
							OType retOType = new OType( "lent", "that.PD", "owner");
							retVal.add(retOType);
							retOType = new OType( "unique", "that.PD", "owner");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "lent", "that.PD");
							retVal.add(retOType);
							retOType = new OType( "unique", "that.PD");
							retVal.add(retOType);
						}
					}
					// adapt-l-d-p, adapt-u-d-p
					if(out.getAlpha().contains(nPDQualifier)){
						if(out.getInner().equals(receiver.getAlpha())){
							OType retOType = new OType( "lent", "that.PD", "p");
							retVal.add(retOType);
							retOType = new OType( "unique", "that.PD", "p");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "lent", "that.PD");
							retVal.add(retOType);
							retOType = new OType( "unique", "that.PD");
							retVal.add(retOType);
						}
					}
					// adapt-l-d-d, adapt-u-d-d
					if(out.getAlpha().contains(nPDQualifier)){
						if(out.getInner().contains(nPDQualifier)){
							OType retOType = new OType( "lent", "that.PD", "that.PD");
							retVal.add(retOType);
							retOType = new OType( "unique", "that.PD", "that.PD");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "lent", "that.PD");
							retVal.add(retOType);
							retOType = new OType( "unique", "that.PD");
							retVal.add(retOType);
						}
					}
					// adapt-l-d-s, adapt-u-d-s
					if(out.getAlpha().contains(nPDQualifier)){
						if(out.getInner().equals("shared")){
							OType retOType = new OType( "lent", "that.PD", "shared");
							retVal.add(retOType);
							retOType = new OType( "unique", "that.PD", "shared");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "lent", "that.PD");
							retVal.add(retOType);
							retOType = new OType( "unique", "that.PD");
							retVal.add(retOType);
						}
					}
					// adapt-l-s-s, adapt-u-s-s
					if(out.getAlpha().equals("shared")){
						if(out.getInner().equals("shared")){
							OType retOType = new OType( "lent", "shared", "shared");
							retVal.add(retOType);
							retOType = new OType( "unique", "shared", "shared");
							retVal.add(retOType);
						}
						else{
							OType retOType = new OType( "lent", "shared");
							retVal.add(retOType);
							retOType = new OType( "unique", "shared");
							retVal.add(retOType);
						}
					}
				}
				}
			}
		}
		else {
			// 6 basic rules from Huang and Milanova's work
			// Adapt-O-O
			if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().equals(receiver.getOwner())){
				OType retOType = new OType( "owner", "owner");
				retVal.add(retOType);
			}
			// Adapt-O-A			
			if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().equals(receiver.getAlpha())){
				OType retOType = new OType( "owner", "p");
				retVal.add(retOType);
			}
			// Adapt-A-A			
			if(out.getOwner().equals(receiver.getAlpha()) && out.getAlpha().equals(receiver.getAlpha())){
				OType retOType = new OType( "p", "p");
				retVal.add(retOType);
			}
			// Adapt-O-S			
			if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().equals("shared")){
				OType retOType = new OType( "owner", "shared");
				retVal.add(retOType);
			}
			// Adapt-A-S			
			if(out.getOwner().equals(receiver.getAlpha()) && out.getAlpha().equals("shared")){
				OType retOType = new OType( "p", "shared");
				retVal.add(retOType);
			}
			// Adapt-S-S
			if(out.getOwner().equals("shared") && out.getAlpha().equals("shared")){
				OType retOType = new OType( "shared", "shared");
				retVal.add(retOType);
			}
			// XXX. New set of adaptation cases
			if(out.getOwner().equals(receiver.getAlpha()) && out.getAlpha().equals(receiver.getOwner())){
				OType retOType = new OType( "p", "owner");
				retVal.add(retOType);
			}
			// end New set of adaptation cases

			//Extended rules
			// Adapt-D-D1
			if(out.getOwner().contains(nPDQualifier)  && out.getAlpha().contains(nPDQualifier)){
				OType retOType = new OType( "that.PD", "that.PD");
				retVal.add(retOType);
			}
			// Adapt-D-D2 or adapt-nd-nd
			if(out.getOwner().contains(".any") && out.getAlpha().contains(".any")){
				//adapt-d-d2
				if(!isFinalReceiver){
					OType retOType = new OType( "that.PD", "that.PD");
					retVal.add(retOType);
				}
				else{
					// XXX. Code for 'any'; not part of the formalization yet
					Map<String,Set<String>> finalVars = context.getFinalFields(receiverType.getQualifiedName());
					Map<String, String> rcvsFinFlds = context.getRcvsAsFinFlds(receiverType.getQualifiedName());
					if(finalVars!=null && rcvsFinFlds!=null){
						if(finalVars.size()>0 &&  rcvsFinFlds.size()>0){
							for (Entry<String, Set<String>> finalVar : finalVars.entrySet()) {
								for (Entry<String, String> rcvEntry : rcvsFinFlds.entrySet()) {
									StringBuilder retTypePD = new StringBuilder();
									retTypePD.append(rcvEntry.getValue());
									retTypePD.append(".");
									retTypePD.append("PD");
									StringBuilder retTypeAny = new StringBuilder();
									retTypeAny.append(rcvEntry.getValue());
									retTypeAny.append(".");
									retTypeAny.append("any");
									if(rcvEntry.getKey().equals(finalVar.getKey()) && finalVar.getValue().contains(rcvEntry.getValue())){
										//adapt-nd-nd
										OType retOType = new OType( retTypePD.toString(), retTypePD.toString());
										retVal.add(retOType);
										//adapt-nd-d2
										retOType = new OType( retTypePD.toString(), "that.PD");
										retVal.add(retOType);
										//adapt-d-nd2
										retOType = new OType( "that.PD", retTypePD.toString());
										retVal.add(retOType);
										//adapt-nd-any
										retOType = new OType( retTypePD.toString(), retTypeAny.toString());
										retVal.add(retOType);
										//adapt-any-any
										retOType = new OType( retTypeAny.toString(), retTypePD.toString());
										retVal.add(retOType);
									}
									else{
										//adapt-any-any
										OType retOType = new OType( retTypeAny.toString(), retTypeAny.toString());
										retVal.add(retOType);
									}
								}
							}
						}
						else{
							if(finalVars.size()>0 &&  rcvsFinFlds.size()==0){
								//adapt-nd-nd
								OType retOType = new OType( "that.PD", "that.PD");
								retVal.add(retOType);
								//adapt-nd-d2
								retOType = new OType( "that.PD", "that.PD");
								retVal.add(retOType);
								//adapt-d-nd2
								retOType = new OType( "that.PD", "that.PD");
								retVal.add(retOType);
								//adapt-nd-any
								retOType = new OType( "that.PD", "that.any");
								retVal.add(retOType);
								//adapt-any-any
								retOType = new OType( "that.any", "that.PD");
								retVal.add(retOType);
							}
							else if(finalVars.size()==0 &&  rcvsFinFlds.size()==0){
								//adapt-any-any
								OType retOType = new OType( "that.any", "that.any");
								retVal.add(retOType);
							}
						}
					}
					OType retOType = new OType( "that.PD", "that.PD");
					retVal.add(retOType);
				}
			}
			// Adapt-ND-D
			// XXX. Code for 'any'; not part of the formalization yet
			if(out.getOwner().contains(".any") && out.getAlpha().contains(nPDQualifier)){
				Map<String,Set<String>> finalVars = context.getFinalFields(receiverType.getQualifiedName());
				Map<String, String> rcvsFinFlds = context.getRcvsAsFinFlds(receiverType.getQualifiedName());
				if(finalVars!=null && rcvsFinFlds!=null){
					if(finalVars.size()>0 &&  rcvsFinFlds.size()>0){
						for (Entry<String, Set<String>> finalVar : finalVars.entrySet()) {
							for (Entry<String, String> rcvEntry : rcvsFinFlds.entrySet()) {
								if(rcvEntry.getKey().equals(finalVar.getKey()) && finalVar.getValue().contains(rcvEntry.getValue())){
									StringBuilder retType = new StringBuilder();
									retType.append(rcvEntry.getValue());
									retType.append(".");
									retType.append("PD");
									OType retOType = new OType( retType.toString(), "that.PD");
									retVal.add(retOType);
								}
							}
						}
					}
					else{
						if(finalVars.size()>0 &&  rcvsFinFlds.size()==0){
							OType retOType = new OType( "that.PD", "that.PD");
							retVal.add(retOType);
						}
						else if(finalVars.size()==0 &&  rcvsFinFlds.size()==0){
							OType retOType = new OType( "that.any", "that.PD");
							retVal.add(retOType);
						}
					}
				}
			}
			//adapt-d-nd1
			if(out.getOwner().contains(nPDQualifier) && out.getAlpha().contains(".any")){
				Map<String,Set<String>> finalVars = context.getFinalFields(receiverType.getQualifiedName());
				Map<String, String> rcvsFinFlds = context.getRcvsAsFinFlds(receiverType.getQualifiedName());
				if(finalVars!=null && rcvsFinFlds!=null){
					if(finalVars!=null && rcvsFinFlds!=null){
						if(finalVars.size()>0 &&  rcvsFinFlds.size()>0){
							for (Entry<String, Set<String>> finalVar : finalVars.entrySet()) {
								for (Entry<String, String> rcvEntry : rcvsFinFlds.entrySet()) {
									if(rcvEntry.getKey().equals(finalVar.getKey()) && finalVar.getValue().contains(rcvEntry.getValue())){
										StringBuilder retType = new StringBuilder();
										retType.append(rcvEntry.getValue());
										retType.append(".");
										retType.append("PD");
										OType retOType = new OType( "that.PD", retType.toString());
										retVal.add(retOType);
									}
								}
							}
						}
						else{
							if(finalVars.size()>0 &&  rcvsFinFlds.size()==0){
								OType retOType = new OType( "that.PD", "that.PD");
								retVal.add(retOType);
							}
							else if(finalVars.size()==0 &&  rcvsFinFlds.size()==0){
								OType retOType = new OType( "that.PD", "that.any");
								retVal.add(retOType);
							}
						}
					}
				}
			}
			// Adapt-D-O1
			if(out.getOwner().contains(nPDQualifier) && out.getAlpha().equals(receiver.getOwner())){
				OType retOType = new OType( "that.PD", "owner");
				retVal.add(retOType);
			}
			//adapt-l-o
			if(out.getOwner().equals("lent") && out.getAlpha().equals(receiver.getOwner())){
				if(!Utils.isFinal(receiverbinding)){
					OType retOType = new OType( "that.PD", "owner");
					retVal.add(retOType);
				}
			}
			//adapt-d-o2 or adapt-nd-o
			if(out.getOwner().contains(".any") && out.getAlpha().equals(receiver.getOwner())){
				//adapt-d-o2
				if(!context.hasFinalField(receiverType.getQualifiedName())){
					OType retOType = new OType( "that.PD", "owner");
					retVal.add(retOType);
				}
				else{
					Map<String,Set<String>> finalVars = context.getFinalFields(receiverType.getQualifiedName());
					Map<String, String> rcvsFinFlds = context.getRcvsAsFinFlds(receiverType.getQualifiedName());
					if(finalVars!=null && rcvsFinFlds!=null){
						if(finalVars.size()>0 &&  rcvsFinFlds.size()>0){
							for (Entry<String, Set<String>> finalVar : finalVars.entrySet()) {
								for (Entry<String, String> rcvEntry : rcvsFinFlds.entrySet()) {
									if(rcvEntry.getKey().equals(finalVar.getKey()) && finalVar.getValue().contains(rcvEntry.getValue())){
										StringBuilder retType = new StringBuilder();
										//adapt-nd-o
										retType.append(rcvEntry.getValue());
										retType.append(".");
										retType.append("PD");
										OType retOType = new OType( retType.toString(), "owner");
										retVal.add(retOType);
									}
									else{
										//adapt-any-o
										StringBuilder retType = new StringBuilder();
										retType.append(rcvEntry.getValue());
										retType.append(".");
										retType.append("any");
										OType retOType = new OType( retType.toString(), "owner");
										retVal.add(retOType);
									}
								}
							}
						}
						else{
							if(finalVars.size()>0 &&  rcvsFinFlds.size()==0){
								//adapt-nd-o
								OType retOType = new OType( "that.PD", "owner");
								retVal.add(retOType);
							}
							else if(finalVars.size()==0 &&  rcvsFinFlds.size()==0){
								//adapt-any-o
								OType retOType = new OType( "that.any", "owner");
								retVal.add(retOType);
							}
						}
					}
					OType retOType = new OType( "that.PD", "owner");
					retVal.add(retOType);
				}
			}
			//adapt-l-a
			if(out.getOwner().equals("lent") && out.getAlpha().equals(receiver.getAlpha())){
				if(!Utils.isFinal(receiverbinding)){
					OType retOType = new OType( "that.PD", "p");
					retVal.add(retOType);
				}
			}
			// Adapt-D-A1
			if(out.getOwner().contains(nPDQualifier) && out.getAlpha().equals(receiver.getAlpha())){
				OType retOType = new OType( "that.PD", "p");
				retVal.add(retOType);
			}
			//adapt-d-a2 or adapt-nd-a
			if(out.getOwner().contains(".any") && out.getAlpha().equals(receiver.getAlpha())){
				//adapt-d-a2
				if(!context.hasFinalField(receiverType.getQualifiedName())){
					OType retOType = new OType( "that.PD", "p");
					retVal.add(retOType);
				}
				else{
					Map<String,Set<String>> finalVars = context.getFinalFields(receiverType.getQualifiedName());
					Map<String, String> rcvsFinFlds = context.getRcvsAsFinFlds(receiverType.getQualifiedName());
					if(finalVars!=null && rcvsFinFlds!=null){
						if(finalVars.size()>0 &&  rcvsFinFlds.size()>0){
							for (Entry<String, Set<String>> finalVar : finalVars.entrySet()) {
								for (Entry<String, String> rcvEntry : rcvsFinFlds.entrySet()) {
									if(rcvEntry.getKey().equals(finalVar.getKey()) && finalVar.getValue().contains(rcvEntry.getValue())){
										StringBuilder retType = new StringBuilder();
										//adapt-nd-a
										retType.append(rcvEntry.getValue());
										retType.append(".");
										retType.append("PD");
										OType retOType = new OType( retType.toString(), "p");
										retVal.add(retOType);
									}
									else{
										//adapt-any-a
										StringBuilder retType = new StringBuilder();
										retType.append(rcvEntry.getValue());
										retType.append(".");
										retType.append("any");
										OType retOType = new OType( retType.toString(), "p");
										retVal.add(retOType);
									}
								}
							}
						}
						else{
							if(finalVars.size()>0 &&  rcvsFinFlds.size()==0){
								//adapt-nd-a
								OType retOType = new OType( "that.PD", "p");
								retVal.add(retOType);
							}
							else if(finalVars.size()==0 &&  rcvsFinFlds.size()==0){
								//adapt-any-a
								OType retOType = new OType( "that.any", "p");
								retVal.add(retOType);
							}
						}
					}
					OType retOType = new OType( "that.PD", "p");
					retVal.add(retOType);
				}
			}
			//adapt-l-s
			if(out.getOwner().equals("lent") && out.getAlpha().equals("shared")){
				if(!Utils.isFinal(receiverbinding)){
					OType retOType = new OType( "that.PD", "shared");
					retVal.add(retOType);
				}
			}
			// Adapt-D-S1
			if(out.getOwner().contains(nPDQualifier) && out.getAlpha().equals("shared")){
				OType retOType = new OType( "that.PD", "shared");
				retVal.add(retOType);
			}
			//adapt-d-s2 or adapt-nd-s
			if(out.getOwner().contains(".any") && out.getAlpha().equals("shared")){
				//adapt-d-s2
				if(!context.hasFinalField(receiverType.getQualifiedName())){
					OType retOType = new OType( "that.PD", "shared");
					retVal.add(retOType);
				}
				else{
					Map<String,Set<String>> finalVars = context.getFinalFields(receiverType.getQualifiedName());
					Map<String, String> rcvsFinFlds = context.getRcvsAsFinFlds(receiverType.getQualifiedName());
					if(finalVars!=null && rcvsFinFlds!=null){
						if(finalVars.size()>0 &&  rcvsFinFlds.size()>0){
							for (Entry<String, Set<String>> finalVar : finalVars.entrySet()) {
								for (Entry<String, String> rcvEntry : rcvsFinFlds.entrySet()) {
									if(rcvEntry.getKey().equals(finalVar.getKey()) && finalVar.getValue().contains(rcvEntry.getValue())){
										StringBuilder retType = new StringBuilder();
										//adapt-nd-s
										retType.append(rcvEntry.getValue());
										retType.append(".");
										retType.append("PD");
										OType retOType = new OType( retType.toString(), "shared");
										retVal.add(retOType);
									}
									else{
										//adapt-any-s
										StringBuilder retType = new StringBuilder();
										retType.append(rcvEntry.getValue());
										retType.append(".");
										retType.append("any");
										OType retOType = new OType( retType.toString(), "shared");
										retVal.add(retOType);
									}
								}
							}
						}
						else{
							if(finalVars.size()>0 &&  rcvsFinFlds.size()==0){
								//adapt-nd-s
								OType retOType = new OType( "that.PD", "shared");
								retVal.add(retOType);
							}
							else if(finalVars.size()==0 &&  rcvsFinFlds.size()==0){
								//adapt-any-s
								OType retOType = new OType( "that.any", "shared");
								retVal.add(retOType);
							}
						}
					}
					OType retOType = new OType( "that.PD", "shared");
					retVal.add(retOType);
				}
			}
			// Adapt-O-D1		
			if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().contains(nPDQualifier)){
				OType retOType = new OType( "owner", "that.PD");
				retVal.add(retOType);
			}
			//adapt-o-d2 or adapt-o-nd
			if(out.getOwner().equals(receiver.getOwner()) && out.getAlpha().contains(".any")){
				//adapt-o-d2
				if(!context.hasFinalField(receiverType.getQualifiedName())){
					OType retOType = new OType( "owner", "that.PD");
					retVal.add(retOType);
				}
				else{
					//XXX. If the is nothing in the rcvsFinFlds, use 'that'.
					Map<String,Set<String>> finalVars = context.getFinalFields(receiverType.getQualifiedName());
					Map<String, String> rcvsFinFlds = context.getRcvsAsFinFlds(receiverType.getQualifiedName());
					if(finalVars!=null && rcvsFinFlds!=null){
						if(finalVars.size()>0 &&  rcvsFinFlds.size()>0){
							for (Entry<String, Set<String>> finalVar : finalVars.entrySet()) {
								for (Entry<String, String> rcvEntry : rcvsFinFlds.entrySet()) {
									if(rcvEntry.getKey().equals(finalVar.getKey()) && finalVar.getValue().contains(rcvEntry.getValue())){
										StringBuilder retType = new StringBuilder();
										//adapt-o-nd
										retType.append(rcvEntry.getValue());
										retType.append(".");
										retType.append("PD");
										OType retOType = new OType( "owner", retType.toString());
										retVal.add(retOType);
									}
									else{
										//adapt-o-any
										StringBuilder retType = new StringBuilder();
										retType.append(rcvEntry.getValue());
										retType.append(".");
										retType.append("any");
										OType retOType = new OType( "owner", retType.toString());
										retVal.add(retOType);
									}
								}
							}
						}
						else{
							if(finalVars.size()>0 &&  rcvsFinFlds.size()==0){
								//adapt-o-nd
								OType retOType = new OType( "owner", "that.PD");
								retVal.add(retOType);
							}
							else if(finalVars.size()==0 &&  rcvsFinFlds.size()==0){
								//adapt-o-any
								OType retOType = new OType( "owner", "that.any");
								retVal.add(retOType);
							}
						}
					}
					OType retOType = new OType( "owner", "that.PD");
					retVal.add(retOType);
				}
			}
			// Adapt-A-D1
			if(out.getOwner().equals(receiver.getAlpha()) && out.getAlpha().contains(nPDQualifier)){
				OType retOType = new OType( "p", "that.PD");
				retVal.add(retOType);
			}
			//adapt-a-d2 or adapt-a-nd
			if(out.getOwner().equals(receiver.getAlpha()) && out.getAlpha().contains(".any")){
				//adapt-a-d2
				if(!context.hasFinalField(receiverType.getQualifiedName())){
					OType retOType = new OType( "p", "that.PD");
					retVal.add(retOType);
				}
				else{
					Map<String,Set<String>> finalVars = context.getFinalFields(receiverType.getQualifiedName());
					Map<String, String> rcvsFinFlds = context.getRcvsAsFinFlds(receiverType.getQualifiedName());
					if(finalVars!=null && rcvsFinFlds!=null){
						if(finalVars.size()>0 &&  rcvsFinFlds.size()>0){
							for (Entry<String, Set<String>> finalVar : finalVars.entrySet()) {
								for (Entry<String, String> rcvEntry : rcvsFinFlds.entrySet()) {
									if(rcvEntry.getKey().equals(finalVar.getKey()) && finalVar.getValue().contains(rcvEntry.getValue())){
										StringBuilder retType = new StringBuilder();
										//adapt-a-nd
										retType.append(rcvEntry.getValue());
										retType.append(".");
										retType.append("PD");
										OType retOType = new OType( "p", retType.toString());
										retVal.add(retOType);
									}
									else{
										//adapt-a-any
										StringBuilder retType = new StringBuilder();
										retType.append(rcvEntry.getValue());
										retType.append(".");
										retType.append("any");
										OType retOType = new OType( "p", retType.toString());
										retVal.add(retOType);
									}
								}
							}
						}
						else{
							if(finalVars.size()>0 &&  rcvsFinFlds.size()==0){
								//adapt-a-nd
								OType retOType = new OType( "p", "that.PD");
								retVal.add(retOType);
							}
							else if(finalVars.size()==0 &&  rcvsFinFlds.size()==0){
								//adapt-a-any
								OType retOType = new OType( "p", "that.any");
								retVal.add(retOType);
							}
						}
					}
					OType retOType = new OType( "p", "that.PD");
					retVal.add(retOType);
				}
			}
			// Adapt-S-D1
			if(out.getOwner().equals("shared") && out.getAlpha().contains(nPDQualifier)){
				OType retOType = new OType( "shared", "that.PD");
				retVal.add(retOType);
			}
			//adapt-s-d2 or adapt-s-nd
			if(out.getOwner().equals("shared") && out.getAlpha().contains(".any")){
				//adapt-s-d2
				if(!context.hasFinalField(receiverType.getQualifiedName())){
					OType retOType = new OType( "shared", "that.PD");
					retVal.add(retOType);
				}
				else{
					Map<String,Set<String>> finalVars = context.getFinalFields(receiverType.getQualifiedName());
					Map<String, String> rcvsFinFlds = context.getRcvsAsFinFlds(receiverType.getQualifiedName());
					if(finalVars!=null && rcvsFinFlds!=null){
						if(finalVars.size()>0 &&  rcvsFinFlds.size()>0){
							for (Entry<String, Set<String>> finalVar : finalVars.entrySet()) {
								for (Entry<String, String> rcvEntry : rcvsFinFlds.entrySet()) {
									if(rcvEntry.getKey().equals(finalVar.getKey()) && finalVar.getValue().contains(rcvEntry.getValue())){
										StringBuilder retType = new StringBuilder();
										//adapt-s-nd
										retType.append(rcvEntry.getValue());
										retType.append(".");
										retType.append("PD");
										OType retOType = new OType( "shared", retType.toString());
										retVal.add(retOType);
									}
									else{
										//adapt-s-any
										StringBuilder retType = new StringBuilder();
										retType.append(rcvEntry.getValue());
										retType.append(".");
										retType.append("any");
										OType retOType = new OType( "shared", retType.toString());
										retVal.add(retOType);
									}
								}
							}
						}
						else{
							if(finalVars.size()>0 &&  rcvsFinFlds.size()==0){
								//adapt-s-nd
								OType retOType = new OType( "shared", "that.PD");
								retVal.add(retOType);
							}
							else if(finalVars.size()==0 &&  rcvsFinFlds.size()==0){
								//adapt-s-any
								OType retOType = new OType( "shared", "that.any");
								retVal.add(retOType);
							}
						}
					}
					OType retOType = new OType( "shared", "that.PD");
					retVal.add(retOType);
				}
			}
			// lent and unique adaptation cases
			// adap-l-o, adap-u-o
			if(out.getAlpha().equals(receiver.getOwner())){
				OType retOType = new OType( "lent", "owner");
				retVal.add(retOType);
				retOType = new OType( "unique", "owner");
				retVal.add(retOType);
			}
			// adap-l-p, adap-u-p
			if(out.getAlpha().equals(receiver.getAlpha())){
				OType retOType = new OType( "lent", "p");
				retVal.add(retOType);
				retOType = new OType( "unique", "p");
				retVal.add(retOType);
			}
			// adap-l-d, adap-u-d
			if(out.getAlpha().equals(nPDQualifier)){
				OType retOType = new OType( "lent", "that.PD");
				retVal.add(retOType);
				retOType = new OType( "unique", "that.PD");
				retVal.add(retOType);
			}
			// adap-l-s, adap-u-s
			if(out.getAlpha().equals("shared")){
				OType retOType = new OType( "lent", "shared");
				retVal.add(retOType);
				retOType = new OType( "unique", "shared");
				retVal.add(retOType);
			}
		}
		return retVal;
	}
	
	/**
	 * Set-level adapt in
	 * @param out: outer set
	 * @param receiver: receiver set:
	 * @param receiverVar: receiver variable
	 * @return inner set
	 */
	public static Set<OType> adaptInSet(Set<OType> out, Set<OType> receiver, Variable receiverVar, ITypeBinding innerType){
		Set<OType> retSet = new HashSet<OType>();
		for (OType outOType : out) {
			for (OType rcvOType : receiver) {
				Set<OType> newOType = adaptIn(outOType,rcvOType,receiverVar, innerType);
				if(newOType.size()>0)
					retSet.addAll(newOType);
			}
		}
		return retSet;
	}
	
	
	/**
	 * Set-level adapt-recv
	 * @param outSet
	 * @param inSet
	 * @param receiver
	 * @return receiver set as a set of OTypes.
	 */
	public static Set<OType> adaptRcvSet(Set<OType> outSet, Set<OType> inSet, Variable receiver, TM tm, ITypeBinding innerType){
		Set<OType> retSet = new HashSet<OType>();
		OType retVal = null;
		
		ITypeBinding receiverType = receiver.resolveType();
		String receiverName = receiver.getSourceString();
		
		// Build n.PD where n is the receiver
		StringBuilder nPD = new StringBuilder();
		nPD.append(receiverName);
		nPD.append(".");
		nPD.append("PD");
		String nPDQualifier = nPD.toString();
		
		Set<String> nPDModifiers = context.getnPDModifiers();
		
		for (OType in : inSet) {
			for (OType out : outSet) {
				if(receiverType.isParameterizedType() || (receiverType.getSuperclass() != null && receiverType.getSuperclass().isParameterizedType()) || innerType.isParameterizedType()){
					if((receiverType.isParameterizedType() || (receiverType.getSuperclass() != null && receiverType.getSuperclass().isParameterizedType())) && !innerType.isParameterizedType()){
						boolean isTypeVariable = false;
						ITypeBinding[] typeArguments = receiverType.getTypeArguments();
						if(typeArguments.length >0){
							isTypeVariable = typeArguments[0].isTypeVariable();
						}
						if(isTypeVariable){
							if(in.getOwner().equals("p") && in.getAlpha() == null && out.getOwner().equals("p") && out.getAlpha() == null){
								retVal = new OType("this.owned" ,out.getOwner());
								retSet.add(retVal);
								retVal = new OType("this.PD" ,out.getOwner());
								retSet.add(retVal);
								retVal = new OType("owner" ,out.getOwner());
								retSet.add(retVal);
								retVal = new OType("p" ,out.getOwner());
								retSet.add(retVal);
								retVal = new OType("shared" ,out.getOwner());
								retSet.add(retVal);
								retVal = new OType("this.any" ,out.getOwner());
								retSet.add(retVal);
								retVal = new OType("lent" ,out.getOwner());
								retSet.add(retVal);
								retVal = new OType("unique" ,out.getOwner());
								retSet.add(retVal);
							}
						}
						if(in.getOwner().equals("p") && in.getAlpha() == null){
							retVal = new OType("this.owned" ,out.getOwner(), out.getAlpha());
							retSet.add(retVal);
							retVal = new OType("this.PD" ,out.getOwner(), out.getAlpha());
							retSet.add(retVal);
							retVal = new OType("owner" ,out.getOwner(), out.getAlpha());
							retSet.add(retVal);
							retVal = new OType("p" ,out.getOwner(), out.getAlpha());
							retSet.add(retVal);
							retVal = new OType("shared" ,out.getOwner(), out.getAlpha());
							retSet.add(retVal);
							retVal = new OType("this.any" ,out.getOwner(), out.getAlpha());
							retSet.add(retVal);
							retVal = new OType("lent" ,out.getOwner(), out.getAlpha());
							retSet.add(retVal);
							retVal = new OType("unique" ,out.getOwner(), out.getAlpha());
							retSet.add(retVal);
						}
						else if(innerType.getQualifiedName().equals("java.lang.String")){
							return tm.getAnalysisResult(receiver);
						}
						// adapt-l-o, adapt-u-o
						else if((in.getOwner().equals("lent") && in.getAlpha().equals("owner")) ||
								(in.getOwner().equals("unique") && in.getAlpha().equals("owner")) ){
							retVal = new OType(out.getAlpha(), "this.owned","this.owned");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "this.owned","this.PD");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "this.owned","owner");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "this.owned","p");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "this.owned","shared");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "this.owned",nPDQualifier);
							retSet.add(retVal);
							
							retVal = new OType(out.getAlpha(), "this.PD","this.owned");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "this.PD","this.PD");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "this.PD","owner");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "this.PD","p");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "this.PD","shared");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "this.PD",nPDQualifier);
							retSet.add(retVal);
							
							retVal = new OType(out.getAlpha(), "this.PD","this.owned");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "owner","this.PD");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "owner","owner");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "owner","p");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "owner","shared");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "owner",nPDQualifier);
							retSet.add(retVal);
							
							retVal = new OType(out.getAlpha(), "this.PD","this.owned");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "p","this.PD");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "p","owner");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "p","p");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "p","shared");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), "p",nPDQualifier);
							retSet.add(retVal);
							
							retVal = new OType(out.getAlpha(), "shared","shared");
							retSet.add(retVal);
							
							retVal = new OType(out.getAlpha(), "this.PD","this.owned");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), nPDQualifier,"this.PD");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), nPDQualifier,"owner");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), nPDQualifier,"p");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), nPDQualifier,"shared");
							retSet.add(retVal);
							retVal = new OType(out.getAlpha(), nPDQualifier,nPDQualifier);
							retSet.add(retVal);
						}
						// adapt-l-p, adapt-u-p
						else if((in.getOwner().equals("lent") && in.getAlpha().equals("p")) ||
								(in.getOwner().equals("unique") && in.getAlpha().equals("p")) ){
							retVal = new OType("this.owned", out.getAlpha(), "this.owned");
							retSet.add(retVal);
							retVal = new OType("this.owned", out.getAlpha(), "this.PD");
							retSet.add(retVal);
							retVal = new OType("this.owned", out.getAlpha(), "owner");
							retSet.add(retVal);
							retVal = new OType("this.owned", out.getAlpha(), "p");
							retSet.add(retVal);
							retVal = new OType("this.owned", out.getAlpha(), "shared");
							retSet.add(retVal);

							retVal = new OType("this.PD", out.getAlpha(), "this.owned");
							retSet.add(retVal);
							retVal = new OType("this.PD", out.getAlpha(), "this.PD");
							retSet.add(retVal);
							retVal = new OType("this.PD", out.getAlpha(), "owner");
							retSet.add(retVal);
							retVal = new OType("this.PD", out.getAlpha(), "p");
							retSet.add(retVal);
							retVal = new OType("this.PD", out.getAlpha(), "shared");
							retSet.add(retVal);

							retVal = new OType("owner", out.getAlpha(), "this.owned");
							retSet.add(retVal);
							retVal = new OType("owner", out.getAlpha(), "this.PD");
							retSet.add(retVal);
							retVal = new OType("owner", out.getAlpha(), "owner");
							retSet.add(retVal);
							retVal = new OType("owner", out.getAlpha(), "p");
							retSet.add(retVal);
							retVal = new OType("owner", out.getAlpha(), "shared");
							retSet.add(retVal);

							retVal = new OType("p", out.getAlpha(), "this.owned");
							retSet.add(retVal);
							retVal = new OType("p", out.getAlpha(), "this.PD");
							retSet.add(retVal);
							retVal = new OType("p", out.getAlpha(), "owner");
							retSet.add(retVal);
							retVal = new OType("p", out.getAlpha(), "p");
							retSet.add(retVal);
							retVal = new OType("p", out.getAlpha(), "shared");
							retSet.add(retVal);

							retVal = new OType("shared", out.getAlpha(), "this.owned");
							retSet.add(retVal);
							retVal = new OType("shared", out.getAlpha(), "this.PD");
							retSet.add(retVal);
							retVal = new OType("shared", out.getAlpha(), "owner");
							retSet.add(retVal);
							retVal = new OType("shared", out.getAlpha(), "p");
							retSet.add(retVal);
							retVal = new OType("shared", out.getAlpha(), "shared");
							retSet.add(retVal);
							
							retVal = new OType("lent", out.getAlpha(), "this.owned");
							retSet.add(retVal);
							retVal = new OType("lent", out.getAlpha(), "this.PD");
							retSet.add(retVal);
							retVal = new OType("lent", out.getAlpha(), "owner");
							retSet.add(retVal);
							retVal = new OType("lent", out.getAlpha(), "p");
							retSet.add(retVal);
							retVal = new OType("lent", out.getAlpha(), "shared");
							retSet.add(retVal);
							
							retVal = new OType("unique", out.getAlpha(), "this.owned");
							retSet.add(retVal);
							retVal = new OType("unique", out.getAlpha(), "this.PD");
							retSet.add(retVal);
							retVal = new OType("unique", out.getAlpha(), "owner");
							retSet.add(retVal);
							retVal = new OType("unique", out.getAlpha(), "p");
							retSet.add(retVal);
							retVal = new OType("unique", out.getAlpha(), "shared");
							retSet.add(retVal);
						}
						// adapt-l-d, adapt-u-d, adapt-l-s, adapt-u-s
						else if((in.getOwner().equals("lent") && in.getAlpha().equals("that.PD")) ||
								(in.getOwner().equals("unique") && in.getAlpha().equals("that.PD")) ||
								(in.getOwner().equals("lent") && in.getAlpha().equals("shared")) ||
								(in.getOwner().equals("unique") && in.getAlpha().equals("shared"))){
							return tm.getAnalysisResult(receiver);
						}
						//////////////////////////////
						// Adapt-O-O
						// receiver.getAlpha() can be anything; so gotta generate all the possibilities
						else if(in.getOwner().equals("owner") && in.getAlpha().equals("owner") 
								&& out.getOwner().equals(out.getAlpha()) ){
							retSet.addAll(doNotCareAlphaAndInnerAndGeneric(out.getOwner()));
						}
						// Adapt-O-A
						else if(in.getOwner().equals("owner") && in.getAlpha().equals("p")){
							// Adapt-O-A
							retSet.addAll(doNotCareInnerGeneric(out.getOwner(), out.getAlpha()));
						}
						// Adapt-A-A
						else if(in.getOwner().equals("p") && in.getAlpha().equals("p") 
								&& out.getOwner().equals(out.getAlpha())) {
							retSet.addAll(doNotCareOwnerAndInnerGeneric(out.getOwner()));
						}
						// XXX. New set of adaptation cases
						// Adapt-A-O
						else if(in.getOwner().equals("p") && in.getAlpha().equals("owner")){
							retSet.addAll(doNotCareInnerGeneric(out.getAlpha(), out.getOwner()));
						}
						// end New set of adaptation cases
						
						// Adapt-O-S
						else if(in.getOwner().equals("owner") && in.getAlpha().equals("shared") &&
								out.getAlpha().equals("shared")){
							retSet.addAll(doNotCareAlphaAndInnerAndGeneric(out.getOwner()));
						}
						// Adapt-A-S
						else if(in.getOwner().equals("p") && in.getAlpha().equals("shared")
							&& out.getAlpha().equals("shared")) {
							retSet.addAll(doNotCareOwnerAndInnerGeneric(out.getOwner()));
						}
						else if(in.getOwner().equals("shared") && in.getAlpha().equals("shared")
								&& out.getOwner().equals("shared") && out.getAlpha().equals("shared")) {
							return tm.getAnalysisResult(receiver);
						}
						//////////////////////////////
					}
					//Rule #1, #3, #7, #8, #10, #52
					else if(innerType.isParameterizedType() && (receiverType.isParameterizedType() || (receiverType.getSuperclass() != null && receiverType.getSuperclass().isParameterizedType()))){
						if(in.getInner()!=null){
							if((in.getOwner().equals("owner") && in.getAlpha().equals("owner") && in.getInner().equals("owner")
									&& out.getOwner().equals(out.getAlpha()) && out.getOwner().equals(out.getInner())) || 
									(in.getOwner().equals("owner") && in.getAlpha().equals("owner") && in.getInner().equals("shared") && out.getOwner().equals(out.getAlpha()) && out.getInner().equals("shared")) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("that.PD") && in.getInner().equals("that.PD") && out.getAlpha().equals(out.getInner())) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("that.PD") && in.getInner().equals("owner") && out.getOwner().equals(out.getInner())) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("that.PD") && in.getInner().equals("shared") && out.getInner().equals("shared")) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("shared") && in.getInner().equals("shared") && out.getAlpha().equals(out.getInner()) && out.getAlpha().equals("shared"))){
								retSet.addAll(doNotCareAlphaAndInnerAndGeneric(out.getOwner()));
								
								retVal = new OType(out.getOwner(), "this.owned",nPDQualifier);
								retSet.add(retVal);
								retVal = new OType(out.getOwner(), "this.PD",nPDQualifier);
								retSet.add(retVal);
								retVal = new OType(out.getOwner(), "owner",nPDQualifier);
								retSet.add(retVal);
								retVal = new OType(out.getOwner(), "p",nPDQualifier);
								retSet.add(retVal);
								retVal = new OType(out.getOwner(), "shared",nPDQualifier);
								retSet.add(retVal);
								retVal = new OType(out.getOwner(), nPDQualifier,"this.PD");
								retSet.add(retVal);
								retVal = new OType(out.getOwner(), nPDQualifier,"owner");
								retSet.add(retVal);
								retVal = new OType(out.getOwner(), nPDQualifier,"p");
								retSet.add(retVal);
								retVal = new OType(out.getOwner(), nPDQualifier,"shared");
								retSet.add(retVal);
								retVal = new OType(out.getOwner(), nPDQualifier,"this.any");
								retSet.add(retVal);
								retVal = new OType(out.getOwner(), nPDQualifier,nPDQualifier);
								retSet.add(retVal);
							}
							// Rule #2, #9
							else if((in.getOwner().equals("owner") && in.getAlpha().equals("owner") && in.getInner().equals("p") && out.getOwner().equals(out.getAlpha())) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("that.PD") && in.getInner().equals("p")	&& out.getOwner().equals(out.getAlpha()))){
								retSet.addAll(doNotCareInnerGeneric(out.getOwner(), out.getInner()));
								retVal = new OType(out.getOwner(), out.getInner(),nPDQualifier);
								retSet.add(retVal);
							}
							// Rule #4, #5, #6
							else if((in.getOwner().equals("owner") && in.getAlpha().equals("p") && in.getInner().equals("owner") && out.getOwner().equals(out.getInner())) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("p") && in.getInner().equals("p") && out.getAlpha().equals(out.getInner())) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("p") && in.getInner().equals("shared") && out.getInner().equals("shared"))){

								retSet.addAll(doNotCareInnerGeneric(out.getOwner(), out.getAlpha()));
								retVal = new OType(out.getOwner(), out.getAlpha(),nPDQualifier);
								retSet.add(retVal);
							}
							// Rule #17
							else if(in.getOwner().equals("p") && in.getAlpha().equals("p") && in.getInner().equals("p") && out.getAlpha().equals(out.getInner()) && out.getOwner().equals(out.getInner())){
								retSet.addAll(doNotCareOwnerAndInnerGeneric(out.getInner()));
								
							}
							// Rule #19
							else if(in.getOwner().equals("p") && in.getAlpha().equals("shared") && in.getInner().equals("shared") && out.getAlpha().equals(out.getInner()) && out.getAlpha().equals("shared")
									// adapt-l-p
									|| (in.getOwner().equals("lent") && in.getAlpha().equals("p"))
									// adapt-u-p
									|| (in.getOwner().equals("unique") && in.getAlpha().equals("p")) ){
								retSet.addAll(doNotCareOwnerAndInnerGeneric(out.getAlpha()));
							}
							// Rule #32, #51
							else if(in.getOwner().equals("shared") && in.getAlpha().equals("shared") && in.getInner().equals("shared") && out.getOwner().equals(out.getInner()) && out.getAlpha().equals(out.getInner()) && out.getAlpha().equals("shared") ||
									(in.getOwner().equals("that.PD") && in.getAlpha().equals("shared") && in.getInner().equals("shared") && out.getAlpha().equals(out.getInner()) && out.getAlpha().equals("shared"))){
								return tm.getAnalysisResult(receiver);
							}
							// lent and unique adaptation cases
							// adapt-l-o-o
							else if((in.getOwner().equals("lent") && in.getAlpha().equals("owner") && in.getInner().equals("owner")) ||
									//adapt-u-o-o
									(in.getOwner().equals("unique") && in.getAlpha().equals("owner") && in.getInner().equals("owner")) ||
									//adapt-l-o-d
									(in.getOwner().equals("lent") && in.getAlpha().equals("owner") && in.getInner().contains(nPDQualifier)) ||
									//adapt-u-o-d
									(in.getOwner().equals("unique") && in.getAlpha().equals("owner") && in.getInner().contains(nPDQualifier)) ||
									//adapt-l-o-s
									(in.getOwner().equals("lent") && in.getAlpha().equals("owner") && in.getInner().equals("shared")) ||
									//adapt-u-o-s
									(in.getOwner().equals("unique") && in.getAlpha().equals("owner") && in.getInner().equals("shared"))){
								retSet.addAll(doNotCareAlphaAndInnerAndGeneric(out.getAlpha()));

								retVal = new OType(out.getAlpha(), "this.owned",nPDQualifier);
								retSet.add(retVal);
								retVal = new OType(out.getAlpha(), "this.PD",nPDQualifier);
								retSet.add(retVal);
								retVal = new OType(out.getAlpha(), "owner",nPDQualifier);
								retSet.add(retVal);
								retVal = new OType(out.getAlpha(), "p",nPDQualifier);
								retSet.add(retVal);
								retVal = new OType(out.getAlpha(), nPDQualifier,"this.PD");
								retSet.add(retVal);
								retVal = new OType(out.getAlpha(), nPDQualifier,"owner");
								retSet.add(retVal);
								retVal = new OType(out.getAlpha(), nPDQualifier,"p");
								retSet.add(retVal);
								retVal = new OType(out.getAlpha(), nPDQualifier,"shared");
								retSet.add(retVal);
								retVal = new OType(out.getAlpha(), nPDQualifier,nPDQualifier);
								retSet.add(retVal);
							}
							// adapt-l-o-p, adapt-u-o-p
							else if((in.getOwner().equals("lent") && in.getAlpha().equals("owner") && in.getInner().equals("p")) ||
									(in.getOwner().equals("unique") && in.getAlpha().equals("owner") && in.getInner().equals("p"))){
								retSet.addAll(doNotCareInnerGeneric(out.getAlpha(), out.getInner()));
								
								retVal = new OType(out.getAlpha(), out.getInner(), nPDQualifier);
								retSet.add(retVal);
							}
							// adapt-l-p-o, adapt-u-p-o
							else if((in.getOwner().equals("lent") && in.getAlpha().equals("p") && in.getInner().equals("owner")) ||
									(in.getOwner().equals("unique") && in.getAlpha().equals("p") && in.getInner().equals("owner"))){
								retSet.addAll(doNotCareInnerGeneric(out.getInner(), out.getAlpha()));
								
								retVal = new OType(out.getInner(), out.getAlpha(), nPDQualifier);
								retSet.add(retVal);
							}
							// adapt-l-p-p
							else if((in.getOwner().equals("lent") && in.getAlpha().equals("p") && in.getInner().equals("p")) ||
									//adapt-u-p-p
									(in.getOwner().equals("unique") && in.getAlpha().equals("p") && in.getInner().equals("p")) ||
									//adapt-l-p-d
									(in.getOwner().equals("lent") && in.getAlpha().equals("p") && in.getInner().contains(nPDQualifier)) ||
									//adapt-u-p-d
									(in.getOwner().equals("unique") && in.getAlpha().equals("p") && in.getInner().contains(nPDQualifier)) ||
									//adapt-l-p-s
									(in.getOwner().equals("lent") && in.getAlpha().equals("p") && in.getInner().equals("shared")) ||
									//adapt-u-p-s
									(in.getOwner().equals("unique") && in.getAlpha().equals("p") && in.getInner().equals("shared"))){
								retSet.addAll(doNotCareOwnerAndInnerGeneric(out.getAlpha()));
							}
							// adapt-l-d-o, adapt-u-d-o 
							else if((in.getOwner().equals("lent") && in.getAlpha().equals("that.PD") && in.getInner().equals("owner")) ||
									(in.getOwner().equals("unique") && in.getAlpha().equals("that.PD") && in.getInner().equals("owner"))){
								retSet.addAll(doNotCareAlphaAndInnerAndGeneric(out.getInner()));
								
								retVal = new OType(out.getInner(), "this.owned",nPDQualifier);
								retSet.add(retVal);
								retVal = new OType(out.getInner(), "this.PD",nPDQualifier);
								retSet.add(retVal);
								retVal = new OType(out.getInner(), "owner",nPDQualifier);
								retSet.add(retVal);
								retVal = new OType(out.getInner(), "p",nPDQualifier);
								retSet.add(retVal);
								retVal = new OType(out.getInner(), nPDQualifier,"this.PD");
								retSet.add(retVal);
								retVal = new OType(out.getInner(), nPDQualifier,"owner");
								retSet.add(retVal);
								retVal = new OType(out.getInner(), nPDQualifier,"p");
								retSet.add(retVal);
								retVal = new OType(out.getInner(), nPDQualifier,"shared");
								retSet.add(retVal);
								retVal = new OType(out.getInner(), nPDQualifier,nPDQualifier);
								retSet.add(retVal);
							}
							// adapt-l-d-p, adapt-u-d-p 
							else if((in.getOwner().equals("lent") && in.getAlpha().equals("that.PD") && in.getInner().equals("p")) ||
									(in.getOwner().equals("unique") && in.getAlpha().equals("that.PD") && in.getInner().equals("p"))){
								retSet.addAll(doNotCareOwnerAndInnerGeneric(out.getInner()));
							}
							// adapt-l-d-d
							else if((in.getOwner().equals("lent") && in.getAlpha().equals("that.PD") && in.getInner().equals("that.PD")) ||
									// adapt-u-d-d
									(in.getOwner().equals("unique") && in.getAlpha().equals("that.PD") && in.getInner().equals("that.PD")) ||
									// adapt-l-d-s
									(in.getOwner().equals("lent") && in.getAlpha().equals("that.PD") && in.getInner().equals("shared")) ||
									// adapt-u-d-s
									(in.getOwner().equals("unique") && in.getAlpha().equals("that.PD") && in.getInner().equals("shared")) ||
									// adapt-l-s-s
									(in.getOwner().equals("lent") && in.getAlpha().equals("shared") && in.getInner().equals("shared")) ||
									// adapt-u-s-s
									(in.getOwner().equals("unique") && in.getAlpha().equals("shared") && in.getInner().equals("shared"))){
								return tm.getAnalysisResult(receiver);
							}
						}
						// in.getInner == null. Collection<T>
						else{
							ITypeBinding[] recTypeArguments = receiverType.getTypeArguments();
							if(!recTypeArguments[0].isTypeVariable()){
								if(in.getOwner().equals("owner") && in.getAlpha().equals("p") && out.getOwner().equals(out.getInner())){

									retVal = new OType(out.getOwner(), out.getAlpha(),out.getInner());
									retSet.add(retVal);
								}
								// Rule #17
								else if(in.getOwner().equals("p") && in.getAlpha().equals("p") && out.getOwner().equals(out.getAlpha())){
									retSet.addAll(doNotCareOwnerGeneric(out.getAlpha(), out.getInner()));
								}
								// lent and unique adaptation cases
								// adapt-l-p, adapt-u-p-o
								else if((in.getOwner().equals("lent") && in.getAlpha().equals("p")) ||
										(in.getOwner().equals("unique") && in.getAlpha().equals("p"))){
									retSet.addAll(doNotCareOwnerGeneric(out.getAlpha(), out.getInner()));
								}
							}
							else{
								if(in.getOwner().equals("owner") && in.getAlpha().equals("p")){
									retVal = new OType(out.getOwner(), out.getAlpha());
									retSet.add(retVal);
								}
								else if(in.getOwner().equals("lent") && in.getAlpha().equals("p")){
									retSet.addAll(doNotCareOwner(out.getAlpha()));
								}
								if(in.getOwner().equals("p") && in.getAlpha().equals("p")){
									retVal = new OType(out.getAlpha(), out.getAlpha());
									retSet.add(retVal);
								}
							}
						}
					}
					else if(innerType.isParameterizedType() && !(receiverType.isParameterizedType() || (receiverType.getSuperclass() != null && receiverType.getSuperclass().isParameterizedType()))){
						if(in.getInner()!=null){
							// Rule #1, #3, #7, #8, #10, #52
							if((in.getOwner().equals("owner") && in.getAlpha().equals("owner") && in.getInner().equals("owner") && out.getOwner().equals(out.getAlpha()) && out.getOwner().equals(out.getInner())) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("owner") && in.getInner().equals("shared") && out.getOwner().equals(out.getAlpha())) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("this.PD") && in.getInner().equals("this.PD") && out.getAlpha().equals(out.getInner())) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("this.PD") && in.getInner().equals("owner") && out.getOwner().equals(out.getInner())) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("this.PD") && in.getInner().equals("shared") && out.getInner().equals("shared")) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("shared") && in.getInner().equals("shared") && out.getAlpha().equals("shared") && out.getInner().equals("shared"))){
								retSet.addAll(doNotCareAlpha(out.getOwner()));
							}
							// Rule #2, #9
							if(in.getOwner().equals("owner") && in.getAlpha().equals("owner") && in.getInner().equals("p") && out.getOwner().equals(out.getAlpha()) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("this.PD") && in.getInner().equals("p") && out.getAlpha().contains(nPDQualifier))){
								retVal = new OType(out.getOwner(), out.getInner());
								retSet.add(retVal);
							}
							// Rule #4, #5, #6
							else if(in.getOwner().equals("owner") && in.getAlpha().equals("p") && in.getInner().equals("owner") && out.getOwner().equals(out.getInner()) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("p") && in.getInner().equals("p") && out.getAlpha().equals(out.getInner())) ||
									(in.getOwner().equals("owner") && in.getAlpha().equals("p") && in.getInner().equals("shared") && out.getInner().equals("shared"))){
								retVal = new OType(out.getOwner(), out.getAlpha());
								retSet.add(retVal);
							}
							// Rule #17, #19
							else if(in.getOwner().equals("p") && in.getAlpha().equals("p") && in.getInner().equals("p") && out.getOwner().equals(out.getAlpha()) && out.getOwner().equals(out.getInner()) ||
									(in.getOwner().equals("p") && in.getAlpha().equals("p") && in.getInner().equals("shared") && out.getOwner().equals(out.getAlpha()) && out.getInner().equals("shared"))){
								retSet.addAll(doNotCareOwner(out.getInner()));
							}
							// Rule #32, #51
							else if(in.getOwner().equals("shared") && in.getAlpha().equals("shared") && in.getInner().equals("shared") && out.getOwner().equals("shared") && out.getAlpha().equals("shared") && out.getInner().equals("shared") ||
									(in.getOwner().equals("that.PD") && in.getAlpha().equals("shared") && in.getInner().equals("shared") && out.getAlpha().equals("shared") && out.getInner().equals("shared"))){
								return tm.getAnalysisResult(receiver);
							}
							// lent and unique adaptation cases
							// adapt-l-o-o
							else if((in.getOwner().equals("lent") && in.getAlpha().equals("owner") && in.getInner().equals("owner")) ||
									// adapt-u-o-o
									(in.getOwner().equals("unique") && in.getAlpha().equals("owner") && in.getInner().equals("owner")) ||
									// adapt-l-o-d
									(in.getOwner().equals("lent") && in.getAlpha().equals("owner") && in.getInner().equals("that.PD")) ||
									// adapt-u-o-d
									(in.getOwner().equals("unique") && in.getAlpha().equals("owner") && in.getInner().equals("that.PD")) ||
									// adapt-l-o-s
									(in.getOwner().equals("lent") && in.getAlpha().equals("owner") && in.getInner().equals("shared")) ||
									// adapt-u-o-s
									(in.getOwner().equals("unique") && in.getAlpha().equals("owner") && in.getInner().equals("shared"))){
								retSet.addAll(doNotCareAlpha(out.getAlpha()));
							}
							// adapt-l-o-p, adapt-u-o-p
							else if((in.getOwner().equals("lent") && in.getAlpha().equals("owner") && in.getInner().equals("p")) ||
									(in.getOwner().equals("unique") && in.getAlpha().equals("owner") && in.getInner().equals("p"))){
								retVal = new OType(out.getAlpha(), out.getInner());
								retSet.add(retVal);
							}
							// adapt-l-p-o, adapt-u-p-o
							else if((in.getOwner().equals("lent") && in.getAlpha().equals("p") && in.getInner().equals("owner")) ||
									(in.getOwner().equals("unique") && in.getAlpha().equals("p") && in.getInner().equals("owner"))){
								retVal = new OType(out.getInner(), out.getAlpha());
								retSet.add(retVal);
							}
							// adapt-l-p-p
							else if((in.getOwner().equals("lent") && in.getAlpha().equals("p") && in.getInner().equals("p")) ||
									// adapt-u-p-p
									(in.getOwner().equals("unique") && in.getAlpha().equals("p") && in.getInner().equals("p")) ||
									// adapt-l-p-d
									(in.getOwner().equals("lent") && in.getAlpha().equals("p") && in.getInner().equals("that.PD")) ||
									// adapt-u-p-d
									(in.getOwner().equals("unique") && in.getAlpha().equals("p") && in.getInner().equals("that.PD")) ||
									// adapt-l-p-s
									(in.getOwner().equals("lent") && in.getAlpha().equals("p") && in.getInner().equals("shared")) ||
									// adapt-u-p-s
									(in.getOwner().equals("unique") && in.getAlpha().equals("p") && in.getInner().equals("shared"))){
								retSet.addAll(doNotCareOwner(out.getAlpha()));
							}
							// adapt-l-d-o, adapt-u-d-o
							else if((in.getOwner().equals("lent") && in.getAlpha().equals("that.PD") && in.getInner().equals("owner")) ||
									(in.getOwner().equals("unique") && in.getAlpha().equals("that.PD") && in.getInner().equals("owner"))){
								retSet.addAll(doNotCareAlpha(out.getInner()));
							}
							// adapt-l-d-p, adapt-u-d-p
							else if((in.getOwner().equals("lent") && in.getAlpha().equals("that.PD") && in.getInner().equals("p")) ||
									(in.getOwner().equals("unique") && in.getAlpha().equals("that.PD") && in.getInner().equals("p"))){
								retSet.addAll(doNotCareOwner(out.getInner()));
							}
							// adapt-l-d-d
							else if((in.getOwner().equals("lent") && in.getAlpha().equals("that.PD") && in.getInner().equals("that.PD")) ||
									// adapt-u-d-d
									(in.getOwner().equals("unique") && in.getAlpha().equals("that.PD") && in.getInner().equals("that.PD")) ||
									// adapt-l-s-s
									(in.getOwner().equals("unique") && in.getAlpha().equals("shared") && in.getInner().equals("shared")) ||
									// adapt-u-s-s
									(in.getOwner().equals("unique") && in.getAlpha().equals("shared") && in.getInner().equals("shared"))){
								return tm.getAnalysisResult(receiver);
							}
						}
						// InnerType.getInner is null. Collection<T> 
						else{
							// Not going to happen...
						}
						
					}
				}
				// Adapt-O-O
				// receiver.getAlpha() can be anything; so gotta generate all the possibilities
				else if(in.getOwner().equals("owner") && in.getAlpha().equals("owner") 
						&& out.getOwner().equals(out.getAlpha()) ){
					// Create qualifiers with all the possible n'.PD modifiers 
					for (String nPDModifier : nPDModifiers) {
						if(!nPDModifier.equals(nPDQualifier)){
							retVal = new OType(out.getOwner(), nPDModifier);
							retSet.add(retVal);
						}
					}
					// XXX. XXX. This has to go... when we remove <owned,owned>
					// Except for top-level class.
					retVal = new OType(out.getOwner(), "this.owned");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), nPDQualifier);
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "owner");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "p");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "shared");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "this.PD");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "this.any");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "shared");
					retSet.add(retVal);
				}
				// Adapt-O-A
				else if(in.getOwner().equals("owner") && in.getAlpha().equals("p")){
					// Adapt-O-A
					retVal = new OType(out.getOwner(), out.getAlpha());
					retSet.add(retVal);
				}
				// Adapt-A-A
				else if(in.getOwner().equals("p") && in.getAlpha().equals("p") 
						&& out.getOwner().equals(out.getAlpha())) {

					// Create qualifiers with all the possible n'.PD modifiers 
					for (String nPDModifier : nPDModifiers) {
						if(!nPDModifier.equals(nPDQualifier)){
							retVal = new OType(nPDModifier, out.getOwner());
							retSet.add(retVal);
						}
					}
					retVal = new OType("this.owned", out.getOwner());
					retSet.add(retVal);
					retVal = new OType(nPDQualifier, out.getOwner());
					retSet.add(retVal);
					retVal = new OType("owner", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("p", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("this.PD", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("this.any", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("shared", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("lent", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("unique", out.getOwner());
					retSet.add(retVal);
				}
				// XXX. New set of adaptation cases
				// Adapt-A-O
				else if(in.getOwner().equals("p") && in.getAlpha().equals("owner")){
					retVal = new OType(out.getAlpha(), out.getOwner());
					retSet.add(retVal);
				}
				// end New set of adaptation cases
				
				// Adapt-O-S
				else if(in.getOwner().equals("owner") && in.getAlpha().equals("shared") &&
						out.getAlpha().equals("shared")){
					// Create qualifiers with all the possible n'.PD modifiers 
					for (String nPDModifier : nPDModifiers) {
						if(!nPDModifier.equals(nPDQualifier)){
							retVal = new OType(out.getOwner(), nPDModifier);
							retSet.add(retVal);
						}
					}
					retVal = new OType(out.getOwner(), "this.owned");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), nPDQualifier);
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "owner");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "p");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "shared");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "this.PD");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "this.any");
					retSet.add(retVal);
				}
				// Adapt-A-S
				else if(in.getOwner().equals("p") && in.getAlpha().equals("shared")
					&& out.getAlpha().equals("shared")) {
					
					// Create qualifiers with all the possible n'.PD modifiers 
					for (String nPDModifier : nPDModifiers) {
						if(!nPDModifier.equals(nPDQualifier)){
							retVal = new OType(nPDModifier, out.getOwner());
							retSet.add(retVal);
						}
					}
					retVal = new OType("this.owned", out.getOwner());
					retSet.add(retVal);
					retVal = new OType(nPDQualifier, out.getOwner());
					retSet.add(retVal);
					retVal = new OType("owner", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("p", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("this.PD", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("this.any", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("shared", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("lent", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("unique", out.getOwner());
					retSet.add(retVal);
				}
				
				// Adapt-S-S 
				else if((in.getOwner().equals("shared") && in.getAlpha().equals("shared") && out.getOwner().equals("shared") && out.getAlpha().equals("shared")) ||
						//adapt-d-d1 and adapt-d-d2
						((in.getOwner().contains("that.PD") && in.getAlpha().contains("that.PD")) && out.getOwner().equals(out.getAlpha())) ||
						//adapt-nd-nd
						((in.getOwner().contains(".PD") && !in.getOwner().contains("that.") && in.getAlpha().contains(".PD") && !in.getAlpha().contains("that.")) && out.getOwner().equals(out.getAlpha())) ||
						//adapt-nd-d1 and adapt-nd-d2
						(in.getOwner().equals(".PD") && !in.getOwner().contains("that.") && in.getAlpha().equals("that.PD")) || 
						//adapt-d-nd1 and adapt-d-nd2
						(in.getOwner().equals("that.PD") && in.getAlpha().equals(".PD") && !in.getAlpha().contains("that.")) ||
						//adapt-d-s1 and adapt-d-s2
						((in.getOwner().contains("that.PD") && in.getAlpha().equals("shared")) && out.getAlpha().equals("shared")) ||
						//adapt-nd-s
						((in.getOwner().contains(".PD") && !in.getOwner().contains("that.") && in.getAlpha().equals("shared")) && out.getAlpha().equals("shared")) || 
						//adapt-any-s
						((in.getOwner().contains(".any") && !in.getOwner().contains("that.") && in.getAlpha().equals("shared")) && out.getAlpha().equals("shared")) ||
						//adapt-s-d1 and adapt-s-d2
						((in.getOwner().equals("shared") && in.getAlpha().contains("that.PD")) && out.getOwner().equals("shared")) || 
						//adapt-s-nd
						((in.getOwner().equals("shared") && in.getAlpha().contains(".PD") && !in.getAlpha().contains("that.")) && out.getOwner().equals("shared")) || 
						//adapt-any-any
						((in.getOwner().contains(".any") && !in.getOwner().contains("that.") && in.getAlpha().contains(".any") && !in.getAlpha().contains("that.")) && out.getOwner().equals(out.getAlpha())) || 
						//adapt-s-any
						((in.getOwner().equals("shared") && in.getAlpha().contains("that.any")) && out.getOwner().equals("shared")) ||
						//adapt-nd-any
						(in.getOwner().equals(".PD") && !in.getOwner().contains("that.") && in.getAlpha().contains(".any") && !in.getAlpha().contains("that.")) || 
						//adapt-any-nd
						(in.getOwner().equals(".any") && in.getAlpha().contains(".PD")) ||
						// Adapt-l-nd
						(in.getOwner().equals("lent") && in.getAlpha().equals(".PD") && !in.getAlpha().contains("that.")) ||
						// Adapt-l-s
						(in.getOwner().equals("lent") && in.getAlpha().equals("shared")) ||
						// Adapt-u-nd
						(in.getOwner().equals("unique") && in.getAlpha().equals(".PD") && !in.getAlpha().contains("that.")) ||
						// Adapt-u-s
						(in.getOwner().equals("unique") && in.getAlpha().equals("shared"))){
				
					return tm.getAnalysisResult(receiver);
				}
						// Adapt-D-O1 and Adapt-D-O2
				else if((in.getOwner().contains("that.PD") && in.getAlpha().equals("owner"))
						// Adapt-ND-O OR Adapt-Any-O
						|| (in.getOwner().contains(".PD") && !in.getOwner().contains("that") && in.getAlpha().equals("owner"))
						// Adapt-Any-O 						
						|| (in.getOwner().contains(".any") && in.getAlpha().equals("owner"))
						// Adapt-l-o
						|| (in.getOwner().equals("lent") && in.getAlpha().equals("owner"))
						// Adapt-u-o
						|| (in.getOwner().equals("unique") && in.getAlpha().equals("owner"))){
					
					// Create qualifiers with all the possible n'.PD modifiers 
					// Create qualifiers with all the possible n'.PD modifiers 
					for (String nPDModifier : nPDModifiers) {
						if(!nPDModifier.equals(nPDQualifier)){
							retVal = new OType(out.getAlpha(), nPDModifier);
							retSet.add(retVal);
						}
					}
					
					retVal = new OType(out.getAlpha(), "this.owned");
					retSet.add(retVal);
					retVal = new OType(out.getAlpha(), nPDQualifier);
					retSet.add(retVal);
					retVal = new OType(out.getAlpha(), "owner");
					retSet.add(retVal);
					retVal = new OType(out.getAlpha(), "p");
					retSet.add(retVal);
					retVal = new OType(out.getAlpha(), "shared");
					retSet.add(retVal);
					retVal = new OType(out.getAlpha(), "this.PD");
					retSet.add(retVal);
					retVal = new OType(out.getAlpha(), "this.any");
					retSet.add(retVal);
					retVal = new OType(out.getAlpha(), "shared");
					retSet.add(retVal);
				}
				
					//	Adapt-D-A1 and Adapt-D-A2
				else if((in.getOwner().contains("that.PD") && in.getAlpha().equals("p"))
						// Adapt-ND-A
						|| ((in.getOwner().contains(".PD") && !in.getOwner().contains("that") && in.getAlpha().equals("p")))
						// Adapt-any-A
						|| (in.getOwner().contains("that.any") && in.getAlpha().equals("p"))
						// Adapt-l-p
						|| (in.getOwner().equals("lent") && in.getAlpha().equals("p"))
						// Adapt-u-p
						|| (in.getOwner().equals("unique") && in.getAlpha().equals("p"))){
					
					// Create qualifiers with all the possible n'.PD modifiers 
					for (String nPDModifier : nPDModifiers) {
						if(!nPDModifier.equals(nPDQualifier)){
							retVal = new OType(nPDModifier, out.getAlpha());
							retSet.add(retVal);
						}
					}
					retVal = new OType("this.owned", out.getAlpha());
					retSet.add(retVal);
					retVal = new OType(nPDQualifier, out.getAlpha());
					retSet.add(retVal);
					retVal = new OType("owner", out.getAlpha());
					retSet.add(retVal);
					retVal = new OType("p", out.getAlpha());
					retSet.add(retVal);
					retVal = new OType("this.PD", out.getAlpha());
					retSet.add(retVal);
					retVal = new OType("this.any", out.getAlpha());
					retSet.add(retVal);
					retVal = new OType("shared", out.getAlpha());
					retSet.add(retVal);
					retVal = new OType("lent", out.getAlpha());
					retSet.add(retVal);
					retVal = new OType("unique", out.getAlpha());
					retSet.add(retVal);
				}
				
					// Adapt-O-D1 and Adapt-O-D2
				else if((in.getOwner().equals("owner") && in.getAlpha().contains("that.PD"))
						//  Adapt-O-ND
						|| (in.getOwner().equals("owner") && in.getAlpha().contains(".PD") && !in.getAlpha().contains("that"))
						//  Adapt-O-ANY
						|| (in.getOwner().equals("owner") && in.getAlpha().contains("that.any"))){
					
					// Create qualifiers with all the possible n'.PD modifiers 
					for (String nPDModifier : nPDModifiers) {
						if(!nPDModifier.equals(nPDQualifier)){
							retVal = new OType(out.getOwner(), nPDModifier);
							retSet.add(retVal);
						}
					}
					retVal = new OType(out.getOwner(), nPDQualifier);
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "this.owned");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "owner");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "p");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "shared");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "this.PD");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "this.any");
					retSet.add(retVal);
					retVal = new OType(out.getOwner(), "shared");
					retSet.add(retVal);
				}
				
					// Adapt-A-D1 and Adapt-A-D2
				else if((in.getOwner().equals("p") && in.getAlpha().contains("that.PD")) 
						// Adapt-A-ND 
						|| (in.getOwner().equals("p") && in.getAlpha().contains(".PD"))
						// Adapt-A-ANY
						|| (in.getOwner().equals("p") && in.getAlpha().contains("that.any"))){
					// Create qualifiers with all the possible n'.PD modifiers 
					for (String nPDModifier : nPDModifiers) {
						if(!nPDModifier.equals(nPDQualifier)){
							retVal = new OType(nPDModifier, out.getOwner());
							retSet.add(retVal);
						}
					}
					retVal = new OType("this.owned", out.getOwner());
					retSet.add(retVal);
					retVal = new OType(nPDQualifier, out.getOwner());
					retSet.add(retVal);
					retVal = new OType("owner", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("p", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("this.PD", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("this.any", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("shared", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("lent", out.getOwner());
					retSet.add(retVal);
					retVal = new OType("unique", out.getOwner());
					retSet.add(retVal);
				}
			}
		}		
		return retSet;
	}
	
	
	/**
	 * Produces qualifiers in the form of <X,alpha,Y>
	 * X is don't care and can be any qualifiers including lent and unique
	 * Y is don't care and can be any qualifiers except lent and unique
	 * @param alpha
	 * @return a set of qualifiers contain <X,alpha,Y>
	 */
	private static Set<OType> doNotCareOwnerAndInnerGeneric(String alpha) {
		Set<OType> returnSet = new HashSet<OType>();
		OType retVal = new OType("this.owned", alpha, "this.owned");
		returnSet.add(retVal);
		retVal = new OType("this.owned", alpha, "this.PD");
		returnSet.add(retVal);
		retVal = new OType("this.owned", alpha, "owner");
		returnSet.add(retVal);
		retVal = new OType("this.owned", alpha, "p");
		returnSet.add(retVal);
		retVal = new OType("this.owned", alpha, "shared");
		returnSet.add(retVal);
		retVal = new OType("this.owned", alpha, "this.any");
		returnSet.add(retVal);

		retVal = new OType("this.PD", alpha, "this.owned");
		returnSet.add(retVal);
		retVal = new OType("this.PD", alpha, "this.PD");
		returnSet.add(retVal);
		retVal = new OType("this.PD", alpha, "owner");
		returnSet.add(retVal);
		retVal = new OType("this.PD", alpha, "p");
		returnSet.add(retVal);
		retVal = new OType("this.PD", alpha, "shared");
		returnSet.add(retVal);
		retVal = new OType("this.PD", alpha, "this.any");
		returnSet.add(retVal);

		retVal = new OType("owner", alpha, "this.owned");
		returnSet.add(retVal);
		retVal = new OType("owner", alpha, "this.PD");
		returnSet.add(retVal);
		retVal = new OType("owner", alpha, "owner");
		returnSet.add(retVal);
		retVal = new OType("owner", alpha, "p");
		returnSet.add(retVal);
		retVal = new OType("owner", alpha, "shared");
		returnSet.add(retVal);
		retVal = new OType("owner", alpha, "this.any");
		returnSet.add(retVal);

		retVal = new OType("p", alpha, "this.owned");
		returnSet.add(retVal);
		retVal = new OType("p", alpha, "this.PD");
		returnSet.add(retVal);
		retVal = new OType("p", alpha, "owner");
		returnSet.add(retVal);
		retVal = new OType("p", alpha, "p");
		returnSet.add(retVal);
		retVal = new OType("p", alpha, "shared");
		returnSet.add(retVal);
		retVal = new OType("p", alpha, "this.any");
		returnSet.add(retVal);

		retVal = new OType("shared", alpha, "this.owned");
		returnSet.add(retVal);
		retVal = new OType("shared", alpha, "this.PD");
		returnSet.add(retVal);
		retVal = new OType("shared", alpha, "owner");
		returnSet.add(retVal);
		retVal = new OType("shared", alpha, "p");
		returnSet.add(retVal);
		retVal = new OType("shared", alpha, "shared");
		returnSet.add(retVal);
		retVal = new OType("shared", alpha, "this.any");
		returnSet.add(retVal);

		retVal = new OType("lent", alpha, "this.owned");
		returnSet.add(retVal);
		retVal = new OType("lent", alpha, "this.PD");
		returnSet.add(retVal);
		retVal = new OType("lent", alpha, "owner");
		returnSet.add(retVal);
		retVal = new OType("lent", alpha, "p");
		returnSet.add(retVal);
		retVal = new OType("lent", alpha, "shared");
		returnSet.add(retVal);
		retVal = new OType("lent", alpha, "this.any");
		returnSet.add(retVal);

		retVal = new OType("unique", alpha, "this.owned");
		returnSet.add(retVal);
		retVal = new OType("unique", alpha, "this.PD");
		returnSet.add(retVal);
		retVal = new OType("unique", alpha, "owner");
		returnSet.add(retVal);
		retVal = new OType("unique", alpha, "p");
		returnSet.add(retVal);
		retVal = new OType("unique", alpha, "shared");
		returnSet.add(retVal);
		retVal = new OType("unique", alpha, "this.any");
		returnSet.add(retVal);
		
		return returnSet;
	}
	
	/**
	 * Produces qualifiers in the form of <owner,X,Y>
	 * X is don't care and can be any qualifiers except lent and unique
	 * Y is don't care and can be any qualifiers except lent and unique
	 * @param owner
	 * @return a set of qualifiers contain <owner,X,Y>
	 */
	private static Set<OType> doNotCareAlphaAndInnerAndGeneric(String owner) {
		Set<OType> returnSet = new HashSet<OType>();
		OType retVal = new OType(owner, "this.owned","this.owned");
		returnSet.add(retVal);
		retVal = new OType(owner, "this.owned","this.PD");
		returnSet.add(retVal);
		retVal = new OType(owner, "this.owned","owner");
		returnSet.add(retVal);
		retVal = new OType(owner, "this.owned","p");
		returnSet.add(retVal);
		retVal = new OType(owner, "this.owned","shared");
		returnSet.add(retVal);
		
		retVal = new OType(owner, "this.PD","this.owned");
		returnSet.add(retVal);
		retVal = new OType(owner, "this.PD","this.PD");
		returnSet.add(retVal);
		retVal = new OType(owner, "this.PD","owner");
		returnSet.add(retVal);
		retVal = new OType(owner, "this.PD","p");
		returnSet.add(retVal);
		retVal = new OType(owner, "this.PD","shared");
		returnSet.add(retVal);
		
		retVal = new OType(owner, "owner","this.owned");
		returnSet.add(retVal);
		retVal = new OType(owner, "owner","this.PD");
		returnSet.add(retVal);
		retVal = new OType(owner, "owner","owner");
		returnSet.add(retVal);
		retVal = new OType(owner, "owner","p");
		returnSet.add(retVal);
		retVal = new OType(owner, "owner","shared");
		returnSet.add(retVal);
		
		retVal = new OType(owner, "p","this.owned");
		returnSet.add(retVal);
		retVal = new OType(owner, "p","this.PD");
		returnSet.add(retVal);
		retVal = new OType(owner, "p","owner");
		returnSet.add(retVal);
		retVal = new OType(owner, "p","p");
		returnSet.add(retVal);
		retVal = new OType(owner, "p","shared");
		returnSet.add(retVal);
		
		retVal = new OType(owner, "shared","this.owned");
		returnSet.add(retVal);
		retVal = new OType(owner, "shared","this.PD");
		returnSet.add(retVal);
		retVal = new OType(owner, "shared","owner");
		returnSet.add(retVal);
		retVal = new OType(owner, "shared","p");
		returnSet.add(retVal);
		retVal = new OType(owner, "shared","shared");
		returnSet.add(retVal);
		
		return returnSet;
	}
	
	/**
	 * Produces qualifiers in the form of <owner,alpha,X>. 
	 * X is don't care and can be any qualifiers except lent and unique
	 * @param owner, alpha
	 * @return a set of qualifiers contain <X, alpha>
	 */
	private static Set<OType> doNotCareInnerGeneric(String owner, String alpha) {
		Set<OType> returnSet = new HashSet<OType>();
		OType retVal = new OType(owner, alpha, "this.owned");
		returnSet.add(retVal);
		retVal = new OType(owner, alpha, "this.PD");
		returnSet.add(retVal);
		retVal = new OType(owner, alpha, "owner");
		returnSet.add(retVal);
		retVal = new OType(owner, alpha, "p");
		returnSet.add(retVal);
		retVal = new OType(owner, alpha, "shared");
		returnSet.add(retVal);
		return returnSet;
	}
	
	/**
	 * Produces qualifiers in the form of <X, alpha, inner>. 
	 * X is don't care and can be any qualifiers including lent and unique
	 * @param alpha, inner
	 * @return a set of qualifiers contain <X, alpha, inner>
	 */
	private static Set<OType> doNotCareOwnerGeneric(String alpha, String inner) {
		Set<OType> returnSet = new HashSet<OType>();
		OType retVal = new OType("this.owned", alpha, inner);
		returnSet.add(retVal);
		retVal = new OType("this.PD", alpha, inner);
		returnSet.add(retVal);
		retVal = new OType("owner", alpha, inner);
		returnSet.add(retVal);
		retVal = new OType("p", alpha, inner);
		returnSet.add(retVal);
		retVal = new OType("shared", alpha, inner);
		returnSet.add(retVal);
		retVal = new OType("lent", alpha, inner);
		returnSet.add(retVal);
		retVal = new OType("unique", alpha, inner);
		returnSet.add(retVal);
		return returnSet;
	}

	/**
	 * Produces qualifiers in the form of <X, alpha>. X is don't care and can be any qualifiers including lent and unique
	 * @param alpha
	 * @return a set of qualifiers contain <X, alpha>
	 */
	private static Set<OType> doNotCareOwner(String alpha) {
		Set<OType> returnSet = new HashSet<OType>();
		OType retVal = new OType("this.owned", alpha);
		returnSet.add(retVal);
		retVal = new OType("this.PD", alpha);
		returnSet.add(retVal);
		retVal = new OType("owner", alpha);
		returnSet.add(retVal);
		retVal = new OType("p", alpha);
		returnSet.add(retVal);
		retVal = new OType("lent", alpha);
		returnSet.add(retVal);
		retVal = new OType("unique", alpha);
		returnSet.add(retVal);
		retVal = new OType("shared", alpha);
		returnSet.add(retVal);
		return returnSet;
	}
	
	/**
	 * Produces qualifiers in the form of <owenr, X>. 
	 * X is don't care and can be any qualifiers except lent and unique
	 * @param owner
	 * @return a set of qualifiers contain <X, alpha>
	 */
	private static Set<OType> doNotCareAlpha(String owner) {
		Set<OType> returnSet = new HashSet<OType>();
		OType retVal = new OType(owner, "this.owned");
		returnSet.add(retVal);
		retVal = new OType(owner, "this.PD");
		returnSet.add(retVal);
		retVal = new OType(owner, "owner");
		returnSet.add(retVal);
		retVal = new OType(owner, "p");
		returnSet.add(retVal);
		retVal = new OType(owner, "shared");
		returnSet.add(retVal);
		return returnSet;
	}

}
