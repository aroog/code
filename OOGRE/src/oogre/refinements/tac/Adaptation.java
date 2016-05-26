package oogre.refinements.tac;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import oogre.analysis.Config;
import oogre.analysis.OOGContext;
import oogre.utils.Utils;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

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
	public static OType adaptOut(OType in, OType receiver, Variable receiverVar){
		
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
				typing.append(receiverVar.getSourceString());
				typing.append(".");
				typing.append("any");
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
				typing.append(receiverVar.getSourceString());
				typing.append(".");
				typing.append("any");
			}
		}
		
		//Typing that is used when the receiver contains 'any'
		StringBuilder anyTyping = new StringBuilder();
		anyTyping.append(receiverVar.getSourceString());
		anyTyping.append(".");
		anyTyping.append("any");
		
		
		OType retVal = null;
		
		// Extract type binding of receiver to check for generics
		ITypeBinding receiverTypebinding = receiverVar.resolveType();
		
		// If the type of the receiver is a generic type
		if(receiverTypebinding.isParameterizedType()){
			if(in.getOwner().equals("p") && in.getAlpha()==null){
				retVal = new OType(receiver.getAlpha(), receiver.getInner());
			}
		}
		else{
			// 6 basic rules from Huang and Milanova's work
			// Adapt-O-O
			if(in.getOwner().equals("owner") && in.getAlpha().equals("owner")){
				retVal = new OType(receiver.getOwner(), receiver.getOwner());
			}
			// Adapt-O-A
			else if(in.getOwner().equals("owner") && in.getAlpha().equals("p")){
				retVal = new OType(receiver.getOwner(), receiver.getAlpha());
			}
			// XXX. New set of adaptation cases
			else if(in.getOwner().equals("p") && in.getAlpha().equals("owner")){
				retVal = new OType(receiver.getAlpha(), receiver.getOwner());
			}
			// end New set of adaptation cases
			// Adapt-A-A
			else if(in.getOwner().equals("p") && in.getAlpha().equals("p")){
				retVal = new OType(receiver.getAlpha(), receiver.getAlpha());
			}
			// Adapt-O-S
			else if(in.getOwner().equals("owner") && in.getAlpha().equals("shared")){
				retVal = new OType(receiver.getOwner(), "shared");
			}
			// Adapt-A-S
			else if(in.getOwner().equals("p") && in.getAlpha().equals("shared")){
				retVal = new OType(receiver.getAlpha(), "shared");
			}
			// Adapt-S-S
			else if(in.getOwner().equals("shared") && in.getAlpha().equals("shared"))
				retVal = new OType("shared", "shared");
			//Extended rules for adapt
			
			// In the following, D == D1 or D == D2 where D1 == n.PD and D2 ==n.any
			// Adapt-D-D 
			else if(in.getOwner().contains("that.PD") && in.getAlpha().contains("that.PD")){
				retVal = new OType(typing.toString(), typing.toString());
			}
			// Adapt-D-O 
			else if(in.getOwner().contains("that.PD") && in.getAlpha().equals("owner")){
				retVal = new OType(typing.toString(), receiver.getOwner());
			}
			// Adapt-D-A
			else if(in.getOwner().contains("that.PD") && in.getAlpha().equals("p")){
				retVal = new OType(typing.toString(), receiver.getAlpha());
			}
			// Adapt-D-S
			else if(in.getOwner().contains("that.PD") && in.getAlpha().equals("shared")){
				retVal = new OType(typing.toString(), "shared");
			}
			// Adapt-O-D
			else if(in.getOwner().equals("owner") && in.getAlpha().contains("that.PD")){
				retVal = new OType(receiver.getOwner(), typing.toString());
			}
			// Adapt-A-D
			else if(in.getOwner().equals("p") && in.getAlpha().contains("that.PD")){
				retVal = new OType(receiver.getAlpha(), typing.toString());
			}
			// Adapt-S-D
			else if(in.getOwner().equals("shared") && in.getAlpha().contains("that.PD")){
				retVal = new OType("shared", typing.toString());
			}
			// Adapt-ndX-ndY rules (inner is n.d)
			// Adapt-ND-ND
			// XXX. Difference with formalization: we always return 'n.any'. We never construct 'n1.n2.PD'
			else if((!in.getOwner().contains("that.") && in.getOwner().contains("PD")) && (!in.getAlpha().contains("that.") && in.getAlpha().contains("PD"))){
				retVal = new OType(anyTyping.toString(), anyTyping.toString());
			}
			// Adapt-ND-D (where D == D1 or D == D2)
			else if((!in.getOwner().contains("that.") && in.getOwner().contains("PD")) && (in.getAlpha().contains("that.PD"))){
				if(Utils.isFinal(receiverbinding)){
					retVal = new OType(anyTyping.toString(), typing.toString());
				}
				else{
					retVal = new OType(anyTyping.toString(), anyTyping.toString());
				}
			}
			// Adapt-D-ND
			else if((in.getOwner().contains("that.PD")) && (!in.getAlpha().contains("that.") && in.getAlpha().contains("PD"))){
				if(Utils.isFinal(receiverbinding)){
					retVal = new OType(typing.toString(), anyTyping.toString());
				}
				else{
					retVal = new OType(anyTyping.toString(), anyTyping.toString());
				}
			}
			// Adapt-ND-O
			else if((!in.getOwner().contains("that.") && in.getOwner().contains("PD")) && in.getAlpha().equals("owner")){
				retVal = new OType(anyTyping.toString(), receiver.getOwner());
			}
			// Adapt-O-ND
			else if(in.getOwner().equals("owner") && (!in.getAlpha().contains("that.") && in.getAlpha().contains("PD"))){
				retVal = new OType(receiver.getOwner(), anyTyping.toString());
			}
			// Adapt-ND-A
			else if((!in.getOwner().contains("that.") && in.getOwner().contains("PD")) && in.getAlpha().equals("p")){
				retVal = new OType(anyTyping.toString(), receiver.getAlpha());
			}
			// Adapt-A-ND
			else if(in.getOwner().equals("p") && (!in.getAlpha().contains("that.") && in.getAlpha().contains("PD"))){
				retVal = new OType(receiver.getAlpha(), anyTyping.toString());
			}
			// Adapt-ND-S 
			else if((!in.getOwner().contains("that.") && in.getOwner().contains("PD")) && in.getAlpha().equals("shared")){
				retVal = new OType(anyTyping.toString(), "shared");
			}
			// Adapt-S-ND 
			else if(in.getOwner().equals("shared") && (!in.getAlpha().contains("that.") && in.getAlpha().contains("PD"))){
				retVal = new OType("shared", anyTyping.toString());
			}
			// When the inner contains n.any (commented out in the formalization)
			// adapt-anyX-anyY rules
			// adapt-any-any rule
			else if(in.getOwner().contains("that.any") && in.getAlpha().contains("that.any")){
				retVal = new OType(anyTyping.toString(), anyTyping.toString());
			}
			//adapt-nd-any rule
			else if((!in.getOwner().contains("that.") && in.getOwner().contains("PD")) && in.getAlpha().contains("that.any")){
				retVal = new OType(anyTyping.toString(), anyTyping.toString());
			}
			//adapt-any-nd rule
			else if(in.getOwner().contains("that.any") && (!in.getAlpha().contains("that.") && in.getAlpha().contains("PD"))){
				retVal = new OType(anyTyping.toString(), anyTyping.toString());
			}
			//adapt-o-any rule
			else if(in.getOwner().equals("owner") && in.getAlpha().contains("that.any")){
				retVal = new OType(receiver.getOwner(), anyTyping.toString());
			}
			//adapt-any-o rule
			else if(in.getOwner().contains("that.any") && in.getAlpha().equals("owner")){
				retVal = new OType(anyTyping.toString(), receiver.getOwner());
			}
			//adapt-a-any rule
			else if(in.getOwner().equals("p") && in.getAlpha().contains("that.any")){
				retVal = new OType(receiver.getAlpha(), anyTyping.toString());
			}
			//adapt-any-a rule
			else if(in.getOwner().contains("that.any") && in.getAlpha().equals("p")){
				retVal = new OType(anyTyping.toString(), receiver.getAlpha());
			}
			//adapt-s-any rule
			else if(in.getOwner().equals("shared") && in.getAlpha().contains("that.any")){
				retVal = new OType("shared", anyTyping.toString());
			}
			//adapt-any-s rule
			else if(in.getOwner().contains("that.any") && in.getAlpha().equals("shared")){
				retVal = new OType(anyTyping.toString(), "shared");
			}
		}
		return retVal;
	}

	/**
	 * Set-level adapt out
	 * @param in: inner set
	 * @param receiver: receiver set:
	 * @param receiverVar: receiver variable
	 * @return outer set
	 */
	public static Set<OType> adaptOutSet(Set<OType> in, Set<OType> receiver, Variable receiverVar){
		Set<OType> retSet = new HashSet<OType>();
		for (OType inOType : in) {
			for (OType recOType : receiver) {
				OType newOType = adaptOut(inOType,recOType, receiverVar);
				if(newOType!=null)
					retSet.add(newOType);
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
	public static Set<OType> adaptIn(OType out, OType receiver, Variable receiverVar){
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
		
		if(receiverType.isParameterizedType()){
			if(out.getOwner().equals(receiver.getAlpha()) && out.getAlpha().equals(receiver.getInner())){
				OType retOType = new OType( "p", null);
				retVal.add(retOType);
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
	public static Set<OType> adaptInSet(Set<OType> out, Set<OType> receiver, Variable receiverVar){
		Set<OType> retSet = new HashSet<OType>();
		for (OType outOType : out) {
			for (OType rcvOType : receiver) {
				Set<OType> newOType = adaptIn(outOType,rcvOType,receiverVar);
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
	public static Set<OType> adaptRcvSet(Set<OType> outSet, Set<OType> inSet, Variable receiver, String enclosingClass){
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
				if(receiverType.isParameterizedType()){
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
						(in.getOwner().equals(".any") && in.getAlpha().contains(".PD"))){
					if(!enclosingClass.equals(Config.MAINCLASS)){
						retVal = new OType("this.owned", "this.owned");
						retSet.add(retVal);
						retVal = new OType("this.owned", "owner");
						retSet.add(retVal);
						retVal = new OType("this.owned", "p");
						retSet.add(retVal);
						retVal = new OType("owner", "owner");
						retSet.add(retVal);
						retVal = new OType("owner", "p");
						retSet.add(retVal);
						retVal = new OType("p", "p");
						retSet.add(retVal);
						retVal = new OType("this.owned", "shared");
						retSet.add(retVal);
						retVal = new OType("owner", "shared");
						retSet.add(retVal);
						retVal = new OType("p", "shared");
						retSet.add(retVal);
						retVal = new OType("shared", "shared");
						retSet.add(retVal);
						//New typings that PD introduces to the set
						retVal = new OType("this.PD", "this.PD");
						retSet.add(retVal);
						retVal = new OType("this.PD", "owner");
						retSet.add(retVal);
						retVal = new OType("this.PD", "p");
						retSet.add(retVal);
						retVal = new OType("this.PD", "shared");
						retSet.add(retVal);
						retVal = new OType("this.owned", "this.PD");
						retSet.add(retVal);
						retVal = new OType("owner", "this.PD");
						retSet.add(retVal);
						retVal = new OType("p", "this.PD");
						retSet.add(retVal);
						retVal = new OType("shared", "this.PD");
						retSet.add(retVal);
						//New typings that any introduces to the set
//						retVal = new OType("that.any", "that.any");
//						retSet.add(retVal);
//						retVal = new OType("that.owned", "that.any");
//						retSet.add(retVal);
//						retVal = new OType("that.PD", "that.any");
//						retSet.add(retVal);
//						retVal = new OType("owner", "that.any");
//						retSet.add(retVal);
//						retVal = new OType("p", "that.any");
//						retSet.add(retVal);
//						retVal = new OType("shared", "that.any");
//						retSet.add(retVal);
//						retVal = new OType("that.any", "that.PD");
//						retSet.add(retVal);
//						retVal = new OType("that.any", "owner");
//						retSet.add(retVal);
//						retVal = new OType("that.any", "p");
//						retSet.add(retVal);
//						retVal = new OType("that.any", "shared");
//						retSet.add(retVal);
					}
					else{
						retVal = new OType("this.owned", "this.owned");
						retSet.add(retVal);
						retVal = new OType("this.owned", "shared");
						retSet.add(retVal);
						retVal = new OType( "shared", "shared");
						retSet.add(retVal);
						//New typings that PD introduces to the set
						retVal = new OType("this.PD", "this.PD");
						retSet.add(retVal);
						retVal = new OType("this.owned", "this.PD");
						retSet.add(retVal);
						retVal = new OType("this.PD", "shared");
						retSet.add(retVal);
						retVal = new OType("shared", "this.PD");
						retSet.add(retVal);
						//New typings that any introduces to the set
//						retVal = new OType("that.any", "that.any");
//						retSet.add(retVal);
//						retVal = new OType("that.owned", "that.any");
//						retSet.add(retVal);
//						retVal = new OType("that.PD", "that.any");
//						retSet.add(retVal);
//						retVal = new OType("shared", "that.any");
//						retSet.add(retVal);
//						retVal = new OType("that.any", "that.PD");
//						retSet.add(retVal);
//						retVal = new OType("that.any", "shared");
//						retSet.add(retVal);
					}
					for (String nPDModifier : nPDModifiers) {
						retVal = new OType("this.owned", nPDModifier);
						retSet.add(retVal);
						retVal = new OType(nPDQualifier, nPDModifier);
						retSet.add(retVal);
						retVal = new OType(nPDModifier, nPDModifier);
						retSet.add(retVal);
						retVal = new OType("owner", nPDModifier);
						retSet.add(retVal);
						retVal = new OType("p", nPDModifier);
						retSet.add(retVal);
						retVal = new OType("this.PD", nPDModifier);
						retSet.add(retVal);
						retVal = new OType("this.any", nPDModifier);
						retSet.add(retVal);
						retVal = new OType("shared", nPDModifier);
						retSet.add(retVal);
						retVal = new OType(nPDModifier, "this.owned");
						retSet.add(retVal);
						retVal = new OType(nPDModifier, nPDQualifier);
						retSet.add(retVal);
						retVal = new OType(nPDModifier, "owner");
						retSet.add(retVal);
						retVal = new OType(nPDModifier, "p");
						retSet.add(retVal);
						retVal = new OType(nPDModifier, "this.PD");
						retSet.add(retVal);
						retVal = new OType(nPDModifier, "this.any");
						retSet.add(retVal);
						retVal = new OType(nPDModifier, "shared");
						retSet.add(retVal);
					}
				}
						// Adapt-D-O1 and Adapt-D-O2
				else if((in.getOwner().contains("that.PD") && in.getAlpha().equals("owner"))
						// Adapt-ND-O OR Adapt-Any-O
						|| (in.getOwner().contains(".PD") && !in.getOwner().contains("that") && in.getAlpha().equals("owner"))
						// Adapt-Any-O 						
						|| (in.getOwner().contains(".any") && in.getAlpha().equals("owner"))){
					
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
						|| (in.getOwner().contains("that.any") && in.getAlpha().equals("p"))){
					
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
				}
			}
		}		
		return retSet;
	}
}
