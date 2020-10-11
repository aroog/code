package oogre.refinements.tac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oogre.utils.Utils;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cmu.cs.crystal.tac.model.CastInstruction;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.ReturnInstruction;
import edu.cmu.cs.crystal.tac.model.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.ThisVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

public class ConflictResolution {


	/**
	 * Finds different permutations of singleton sets for the variables of a conflict instruction
	 * @param instr: The conflict instruction
	 * @param newTM: The TM that contains the set of feasible qualifiers for the variables of a conflict instruction
	 * @return permutationTMs: A set TMs each contains a singleton set of different permutations for the variables of a conflict instruction
	 * permutationTMs is sorted from the highest ranked permutation to the lowest one
	 */
	public static List<TM> findPermutationsOfConflictedVariables(TACInstruction instr, TM newTM) {
		List<TM> permutationTMs = new ArrayList<TM>();
		if(instr instanceof CastInstruction){
			CastInstruction concreteInstr = (CastInstruction)instr;
			Variable operand = concreteInstr.getOperand();
			Variable target = concreteInstr.getTarget();
			permutationTMs = twoVariablePermutations(operand, target, newTM);
		}
		else if(instr instanceof CopyInstruction){
			CopyInstruction concreteInstr = (CopyInstruction)instr;
			Variable operand = concreteInstr.getOperand();
			Variable target = concreteInstr.getTarget();
			permutationTMs = twoVariablePermutations(operand, target, newTM);
		}
		else if(instr instanceof ReturnInstruction){
			ReturnInstruction concreteInstr = (ReturnInstruction)instr;
			Variable returnedVariable = concreteInstr.getReturnedVariable();

			//Finding the enclosing method this instruction
			ASTNode node = concreteInstr.getNode();
			while(!(node instanceof MethodDeclaration)){
				node=node.getParent();
			}
			MethodDeclaration methDecl = (MethodDeclaration)node;
			// Extracting the method variable from TM
			IMethodBinding methodBinding = methDecl.resolveBinding();
			Variable methodVar = null;
			if(!methodBinding.getReturnType().isPrimitive()){
				methodVar = newTM.getVarBindingMap(methodBinding);
			}
			if(methodVar!=null){
				permutationTMs = twoVariablePermutations(returnedVariable, methodVar, newTM);
			}
		}
		else if(instr instanceof LoadFieldInstruction){
			LoadFieldInstruction concreteInstr = (LoadFieldInstruction)instr;
			Variable accessedObjectOperand = concreteInstr.getSourceObject();
			Variable target = concreteInstr.getTarget();

			// Extracting field variable
			IVariableBinding instrFieldBinding = concreteInstr.resolveFieldBinding();
			Variable fieldVariable = newTM.getVarBindingMap(instrFieldBinding);
			if(fieldVariable!=null){
				if(accessedObjectOperand instanceof ThisVariable){
					permutationTMs = twoVariablePermutations(target, fieldVariable, newTM);
				}
				else{
					permutationTMs = threeVariablePermutations(accessedObjectOperand, target, fieldVariable, newTM);
				}
			}
		}
		else if(instr instanceof MethodCallInstruction){
			MethodCallInstruction concreteInstr = (MethodCallInstruction)instr;
			IMethodBinding methodBinding = concreteInstr.resolveBinding();
			List<Variable> argOperands = concreteInstr.getArgOperands();
			Variable receiverVar = concreteInstr.getReceiverOperand();
			Set<OType> methodAUTypings = null;
			List<Set<OType>> methodParsListTyping = null;
			List<Variable> parametersVarList = new ArrayList<Variable>();
			IMethod javaElement = (IMethod) methodBinding.getJavaElement();
			boolean isFromSource = methodBinding.getDeclaringClass().isFromSource();
			if(isFromSource){
				Utils.extractParametersFromSource(parametersVarList, javaElement, newTM);
			}
			// Else, this is library code.
			else {
				methodAUTypings = Utils.libraryMethod(methodBinding);
				methodParsListTyping = Utils.libraryMethodParams(methodBinding);
			}
			if(argOperands.size()>0){
				if(methodBinding.getReturnType().isPrimitive()){
					if(isFromSource){
						if(!(receiverVar instanceof ThisVariable)){
							permutationTMs = methInvoc1(receiverVar, argOperands, parametersVarList, newTM);
						}
						else{
							permutationTMs = methInvoc1This(argOperands, parametersVarList, newTM);
						}
					}
					else{
						if(!(receiverVar instanceof ThisVariable)){
							permutationTMs = methInvoc2(receiverVar, argOperands, methodParsListTyping, newTM);
						}
						else{
							permutationTMs = methInvoc2This(argOperands, methodParsListTyping, newTM);
						}
					}
				}
				else{
					Variable leftHandSideVar = concreteInstr.getTarget();
					if(isFromSource){
						Variable methodVar = newTM.getVarBindingMap(methodBinding);
						if(!(receiverVar instanceof ThisVariable)){
							permutationTMs = methInvoc3(receiverVar, leftHandSideVar, methodVar, argOperands, parametersVarList, newTM);
						}
						else{
							permutationTMs = methInvoc3This(leftHandSideVar, methodVar, argOperands, parametersVarList, newTM);
						}
					}
					else{
						if(!(receiverVar instanceof ThisVariable)){
							permutationTMs = methInvoc4(receiverVar, leftHandSideVar, methodAUTypings, argOperands, methodParsListTyping, newTM);
						}
						else{
							permutationTMs = methInvoc4This(leftHandSideVar, methodAUTypings, argOperands, methodParsListTyping, newTM);
						}
					}
					
				}
			}
			else{
				if(!methodBinding.getReturnType().isPrimitive() ){
					Variable leftHandSideVar = concreteInstr.getTarget();
					if(isFromSource){
						Variable methodVar = newTM.getVarBindingMap(methodBinding);
						if(!(receiverVar instanceof ThisVariable)){
							permutationTMs = threeVariablePermutations(receiverVar, leftHandSideVar, methodVar, newTM);
						}
						else{
							permutationTMs = twoVariablePermutations(leftHandSideVar, methodVar, newTM);
						}
					}
					else{
						if(!(receiverVar instanceof ThisVariable)){
							permutationTMs = methInvoc5(receiverVar, leftHandSideVar, methodAUTypings, newTM);
						}
						else{
							permutationTMs = methInvoc5This(leftHandSideVar, methodAUTypings, newTM);
						}
					}
				}
			}
		}
		else if(instr instanceof NewObjectInstruction){
			NewObjectInstruction concreteInstr = (NewObjectInstruction)instr;
			Variable target = concreteInstr.getTarget();
			List<Variable> argOperands = concreteInstr.getArgOperands();
			permutationTMs = newExpressionPermutations(target, argOperands, newTM);
		}
		else if(instr instanceof StoreFieldInstruction){
			StoreFieldInstruction concreteInstr = (StoreFieldInstruction)instr;
			Variable accessedObjectOperand = concreteInstr.getAccessedObjectOperand();
			Variable sourceOperand = concreteInstr.getSourceOperand();
			// Extracting field variable
			IVariableBinding resolveFieldBinding = concreteInstr.resolveFieldBinding();
			Variable fieldVariable = newTM.getVarBindingMap(resolveFieldBinding);
			if(fieldVariable!=null){
				if(accessedObjectOperand instanceof ThisVariable){
					permutationTMs = twoVariablePermutations(sourceOperand, fieldVariable, newTM);
				}
				else{
					permutationTMs = threeVariablePermutations(accessedObjectOperand, sourceOperand, fieldVariable, newTM);
				}
			}
		}
		return permutationTMs;
	}

	private static List<TM> methInvoc5This(Variable leftHandSideVar,
			Set<OType> methodAUTypings, TM newTM) {
		RankingStrategy ranking = RankingStrategy.getInstance();
		List<TM> permutationTMs = new ArrayList<TM>();
		Set<OType> leftHandSideVarSet = newTM.getAnalysisResult(leftHandSideVar);
		Set<OType> copyLeftHandSideVarSet = new SetOType<OType>(leftHandSideVarSet);
		Set<OType> copymethodVarSet = new SetOType<OType>(methodAUTypings);

		// XXX: loop variable not being used?
		for (OType lhsVarQualifier : leftHandSideVarSet) {
			OType lhsVarHighest = ranking.pickFromSet(copyLeftHandSideVarSet, null);
			Set<OType> newLhsVarSet = new SetOType<OType>();
			newLhsVarSet.add(lhsVarHighest);
			copyLeftHandSideVarSet.remove(lhsVarHighest);
			copymethodVarSet.addAll(methodAUTypings);
			for (OType thirdVarQualifier : methodAUTypings) {
				OType thirdVarHighest = ranking.pickFromSet(copymethodVarSet, null);
				Set<OType> newThirdVarSet = new SetOType<OType>();
				newThirdVarSet.add(thirdVarHighest);
				copymethodVarSet.remove(thirdVarHighest);

				TM aNewTM = newTM.copyTypeMapping(newTM.getKey());
				aNewTM.putTypeMapping(leftHandSideVar, newLhsVarSet);
				permutationTMs.add(aNewTM);
			}
		}

		return permutationTMs;
	}

	/**
	 * Finds different permutations of singleton sets for a method invocation in its complete form
	 * x = this.m(\ob(z))
	 * The return of the method is not a primitive type, but the method is from library code so the
	 * return and parameter variables are not available, and the method accepts their set of qualifiers 
	 * @param leftHandSideVar
	 * @param methodAUTypings
	 * @param argOperands
	 * @param methodParsListTyping
	 * @param newTM
	 * @return
	 */
	private static List<TM> methInvoc4This(Variable leftHandSideVar,
			Set<OType> methodAUTypings, List<Variable> argOperands,
			List<Set<OType>> methodParsListTyping, TM newTM) {
		RankingStrategy ranking = RankingStrategy.getInstance();
		List<TM> permutationTMs = new ArrayList<TM>();
		
		Set<OType> leftHandSideVarSet = newTM.getAnalysisResult(leftHandSideVar);
		Set<OType> copyLeftHandSideVarSet = new SetOType<OType>(leftHandSideVarSet);
		
		Set<OType> copyMethodVarSet = new SetOType<OType>();
		
		List<Set<OType>> listArgVarSets = new ArrayList<Set<OType>>();
		for (Variable argVar : argOperands) {
			Set<OType> argVarSet = newTM.getAnalysisResult(argVar);
			listArgVarSets.add(argVarSet);
		}
		List<List<Set<OType>>> newListArgVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> argSet : listArgVarSets) {
			Set<OType> copyArgVarSet = new SetOType<OType>(argSet);
			List<Set<OType>> argVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : argSet) {
				OType argVarHighest = ranking.pickFromSet(copyArgVarSet, null);
				Set<OType> newArgVarSet = new SetOType<OType>();
				newArgVarSet.add(argVarHighest);
				copyArgVarSet.remove(argVarHighest);
				argVarPermutation.add(newArgVarSet);
			}
			newListArgVarSets.add(argVarPermutation);
		}
		List<List<Set<OType>>> newListParVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> parSet : methodParsListTyping) {
			Set<OType> copyParVarSet = new SetOType<OType>(parSet);
			List<Set<OType>> parVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : parSet) {
				OType parVarHighest = ranking.pickFromSet(copyParVarSet, null);
				Set<OType> newParVarSet = new SetOType<OType>();
				newParVarSet.add(parVarHighest);
				copyParVarSet.remove(parVarHighest);
				parVarPermutation.add(newParVarSet);
			}
			newListParVarSets.add(parVarPermutation);
		}
		for (OType leftHandSideVarQualifier : leftHandSideVarSet) {
			OType leftHandSideVarHighest = ranking.pickFromSet(copyLeftHandSideVarSet, null);
			Set<OType> newLeftHandSideVarSet = new SetOType<OType>();
			newLeftHandSideVarSet.add(leftHandSideVarHighest);
			copyLeftHandSideVarSet.remove(leftHandSideVarHighest);
			for (OType methodVarQualifier : methodAUTypings) {
				OType methodVarHighest = ranking.pickFromSet(copyMethodVarSet, null);
				Set<OType> newMethodVarSet = new SetOType<OType>();
				newMethodVarSet.add(methodVarHighest);
				copyMethodVarSet.remove(methodVarHighest);
				int i = 0;
				//XXX. This way we do not consider all the combinations
				for (List<Set<OType>> oType1 : newListArgVarSets) {
					for (Set<OType> set1 : oType1) {
						for (List<Set<OType>> oType2 : newListParVarSets) {
							for (Set<OType> set2 : oType2) {
								TM aNewTM = newTM.copyTypeMapping(newTM.getKey());
								aNewTM.putTypeMapping(leftHandSideVar, newLeftHandSideVarSet);
								aNewTM.putTypeMapping(argOperands.get(i), set1);
								permutationTMs.add(aNewTM);
							}
						}
					}
					i++;
				}
			}
		}
		return permutationTMs;
	}

	/**
	 * Finds different permutations of singleton sets for a method invocation in its complete form
	 * x = this.m(\ob(z))
	 * The return of the method is not a primitive type and the method is not from library code 
	 * @param leftHandSideVar
	 * @param methodVar
	 * @param argOperands
	 * @param parametersVarList
	 * @param newTM
	 * @return
	 */
	private static List<TM> methInvoc3This(Variable leftHandSideVar,
			Variable methodVar, List<Variable> argOperands,
			List<Variable> parametersVarList, TM newTM) {
		RankingStrategy ranking = RankingStrategy.getInstance();
		List<TM> permutationTMs = new ArrayList<TM>();
		
		Set<OType> leftHandSideVarSet = newTM.getAnalysisResult(leftHandSideVar);
		Set<OType> copyLeftHandSideVarSet = new SetOType<OType>(leftHandSideVarSet);
		
		Set<OType> methodVarSet = newTM.getAnalysisResult(methodVar);
		Set<OType> copyMethodVarSet = new SetOType<OType>(methodVarSet);
		
		List<Set<OType>> listArgVarSets = new ArrayList<Set<OType>>();
		for (Variable argVar : argOperands) {
			Set<OType> argVarSet = newTM.getAnalysisResult(argVar);
			listArgVarSets.add(argVarSet);
		}
		List<List<Set<OType>>> newListArgVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> argSet : listArgVarSets) {
			Set<OType> copyArgVarSet = new SetOType<OType>(argSet);
			List<Set<OType>> argVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : argSet) {
				OType argVarHighest = ranking.pickFromSet(copyArgVarSet, null);
				Set<OType> newArgVarSet = new SetOType<OType>();
				newArgVarSet.add(argVarHighest);
				copyArgVarSet.remove(argVarHighest);
				argVarPermutation.add(newArgVarSet);
			}
			newListArgVarSets.add(argVarPermutation);
		}
		List<Set<OType>> listParVarSets = new ArrayList<Set<OType>>();
		for (Variable parVar : parametersVarList) {
			Set<OType> parVarSet = newTM.getAnalysisResult(parVar);
			listParVarSets.add(parVarSet);
		}
		List<List<Set<OType>>> newListParVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> parSet : listParVarSets) {
			Set<OType> copyParVarSet = new SetOType<OType>(parSet);
			List<Set<OType>> parVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : parSet) {
				OType parVarHighest = ranking.pickFromSet(copyParVarSet, null);
				Set<OType> newParVarSet = new SetOType<OType>();
				newParVarSet.add(parVarHighest);
				copyParVarSet.remove(parVarHighest);
				parVarPermutation.add(newParVarSet);
			}
			newListParVarSets.add(parVarPermutation);
		}
		for (OType leftHandSideVarQualifier : leftHandSideVarSet) {
			OType leftHandSideVarHighest = ranking.pickFromSet(copyLeftHandSideVarSet, null);
			Set<OType> newLeftHandSideVarSet = new SetOType<OType>();
			newLeftHandSideVarSet.add(leftHandSideVarHighest);
			copyLeftHandSideVarSet.remove(leftHandSideVarHighest);
			for (OType methodVarQualifier : methodVarSet) {
				OType methodVarHighest = ranking.pickFromSet(copyMethodVarSet, null);
				Set<OType> newMethodVarSet = new SetOType<OType>();
				newMethodVarSet.add(methodVarHighest);
				copyMethodVarSet.remove(methodVarHighest);
				int i = 0;
				//XXX. This way we do not consider all the combinations
				for (List<Set<OType>> oType1 : newListArgVarSets) {
					for (Set<OType> set1 : oType1) {
						for (List<Set<OType>> oType2 : newListParVarSets) {
							for (Set<OType> set2 : oType2) {
								TM aNewTM = newTM.copyTypeMapping(newTM.getKey());
								aNewTM.putTypeMapping(leftHandSideVar, newLeftHandSideVarSet);
								aNewTM.putTypeMapping(methodVar, newMethodVarSet);
								aNewTM.putTypeMapping(argOperands.get(i), set1);
								aNewTM.putTypeMapping(parametersVarList.get(i), set2);
								permutationTMs.add(aNewTM);
							}
						}
					}
					i++;
				}
			}
		}
		return permutationTMs;
	}

	/**
	 * The difference of this method with methInvoc2 is it is the receiver of this method 
	 * is 'this'
	 * @param argOperands: List of arguments of the method invocation
	 * @param methodParsListTyping: List of parameters of the method invocation
	 * @param newTM: A TM that contains the original set of qualifiers of the variables of the conflict instruction
	 * @return permutationTMs: an ordered list of TMs in which the the variables of the conflict instruction
	 * mapped to a singleton set. Different TMs of the list represent different permutations that the qualifiers
	 * of the variables of the conflict instruction can generate.
	 * The order of the list is based on our ranking on the qualifiers
	 */
	private static List<TM> methInvoc2This(List<Variable> argOperands,
			List<Set<OType>> methodParsListTyping, TM newTM) {
		RankingStrategy ranking = RankingStrategy.getInstance();
		List<TM> permutationTMs = new ArrayList<TM>();
		
		List<Set<OType>> listArgVarSets = new ArrayList<Set<OType>>();
		for (Variable argVar : argOperands) {
			Set<OType> argVarSet = newTM.getAnalysisResult(argVar);
			listArgVarSets.add(argVarSet);
		}
		List<List<Set<OType>>> newListArgVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> argSet : listArgVarSets) {
			Set<OType> copyArgVarSet = new SetOType<OType>(argSet);
			List<Set<OType>> argVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : argSet) {
				OType argVarHighest = ranking.pickFromSet(copyArgVarSet, null);
				Set<OType> newArgVarSet = new SetOType<OType>();
				newArgVarSet.add(argVarHighest);
				copyArgVarSet.remove(argVarHighest);
				argVarPermutation.add(newArgVarSet);
			}
			newListArgVarSets.add(argVarPermutation);
		}
		int i = 0;
		//XXX. This way we do not consider all the combinations
		for (List<Set<OType>> oType1 : newListArgVarSets) {
			for (Set<OType> set1 : oType1) {
				TM aNewTM = newTM.copyTypeMapping(newTM.getKey());
				aNewTM.putTypeMapping(argOperands.get(i), set1);
				permutationTMs.add(aNewTM);
			}
			i++;
		}
		return permutationTMs;
	}

	/**
	 * Finds different permutations of singleton sets when the conflict method invocation instruction
	 * with the receiver 'this' and when there are parameters and arguments for the method and the return type of the method is primitive
	 * @param argOperands: List of argument variables of the method invocation
	 * @param parametersVarListL List of parameters of the method invocation
	 * @param newTM: A TM that contains the original set of qualifiers of the variables of the conflict instruction
	 * @return permutationTMs: an ordered list of TMs in which the the variables of the conflict instruction
	 * mapped to a singleton set. Different TMs of the list represent different permutations that the qualifiers
	 * of the variables of the conflict instruction can generate.
	 * The order of the list is based on our ranking on the qualifiers
	 */
	private static List<TM> methInvoc1This(List<Variable> argOperands,
			List<Variable> parametersVarList, TM newTM) {
		RankingStrategy ranking = RankingStrategy.getInstance();
		List<TM> permutationTMs = new ArrayList<TM>();
		
		List<Set<OType>> listArgVarSets = new ArrayList<Set<OType>>();
		for (Variable argVar : argOperands) {
			Set<OType> argVarSet = newTM.getAnalysisResult(argVar);
			listArgVarSets.add(argVarSet);
		}
		List<List<Set<OType>>> newListArgVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> argSet : listArgVarSets) {
			Set<OType> copyArgVarSet = new SetOType<OType>(argSet);
			List<Set<OType>> argVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : argSet) {
				OType argVarHighest = ranking.pickFromSet(copyArgVarSet, null);
				Set<OType> newArgVarSet = new SetOType<OType>();
				newArgVarSet.add(argVarHighest);
				copyArgVarSet.remove(argVarHighest);
				argVarPermutation.add(newArgVarSet);
			}
			newListArgVarSets.add(argVarPermutation);
		}
		List<Set<OType>> listParVarSets = new ArrayList<Set<OType>>();
		for (Variable parVar : parametersVarList) {
			Set<OType> parVarSet = newTM.getAnalysisResult(parVar);
			listParVarSets.add(parVarSet);
		}
		List<List<Set<OType>>> newListParVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> parSet : listParVarSets) {
			Set<OType> copyParVarSet = new SetOType<OType>(parSet);
			List<Set<OType>> parVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : parSet) {
				OType parVarHighest = ranking.pickFromSet(copyParVarSet, null);
				Set<OType> newParVarSet = new SetOType<OType>();
				newParVarSet.add(parVarHighest);
				copyParVarSet.remove(parVarHighest);
				parVarPermutation.add(newParVarSet);
			}
			newListParVarSets.add(parVarPermutation);
		}
		int i = 0;
		//XXX. This way we do not consider all the combinations
		for (List<Set<OType>> oType1 : newListArgVarSets) {
			for (Set<OType> set1 : oType1) {
				for (List<Set<OType>> oType2 : newListParVarSets) {
					for (Set<OType> set2 : oType2) {
						TM aNewTM = newTM.copyTypeMapping(newTM.getKey());
						aNewTM.putTypeMapping(argOperands.get(i), set1);
						aNewTM.putTypeMapping(parametersVarList.get(i), set2);
						permutationTMs.add(aNewTM);
					}
				}
			}
			i++;
		}
		return permutationTMs;
	}

	/**
	 * Finds different permutations of singleton sets for a method that does not have method parameters and
	 * is from library code. The return of the method is not a primitive type.
	 * @param receiverVar
	 * @param leftHandSideVar
	 * @param methodAUTypings
	 * @param newTM
	 * @return
	 */
	private static List<TM> methInvoc5(Variable receiverVar,
			Variable leftHandSideVar, Set<OType> methodAUTypings, TM newTM) {
		RankingStrategy ranking = RankingStrategy.getInstance();
		List<TM> permutationTMs = new ArrayList<TM>();
		Set<OType> leftHandSideVarSet = newTM.getAnalysisResult(leftHandSideVar);
		Set<OType> copyLeftHandSideVarSet = new SetOType<OType>(leftHandSideVarSet);
		Set<OType> receiverVarSet = newTM.getAnalysisResult(receiverVar);
		Set<OType> copyReceiverVarSet = new SetOType<OType>(receiverVarSet);
		Set<OType> copymethodVarSet = new SetOType<OType>(methodAUTypings);

		// XXX: loop variable not being used?
		for (OType lhsVarQualifier : leftHandSideVarSet) {
			OType lhsVarHighest = ranking.pickFromSet(copyLeftHandSideVarSet, null);
			Set<OType> newLhsVarSet = new SetOType<OType>();
			newLhsVarSet.add(lhsVarHighest);
			copyLeftHandSideVarSet.remove(lhsVarHighest);
			copyReceiverVarSet.addAll(receiverVarSet);
			// XXX: loop variable not being used?
			for (OType receiverVarQualifier : receiverVarSet) {
				OType receiverVarHighest = ranking.pickFromSet(copyReceiverVarSet, null);
				Set<OType> newReceiverVarSet = new SetOType<OType>();
				newReceiverVarSet.add(receiverVarHighest);
				copyReceiverVarSet.remove(receiverVarHighest);
				copymethodVarSet.addAll(methodAUTypings);
				// XXX: loop variable not being used?
				for (OType thirdVarQualifier : methodAUTypings) {
					OType thirdVarHighest = ranking.pickFromSet(copymethodVarSet, null);
					Set<OType> newThirdVarSet = new SetOType<OType>();
					newThirdVarSet.add(thirdVarHighest);
					copymethodVarSet.remove(thirdVarHighest);

					TM aNewTM = newTM.copyTypeMapping(newTM.getKey());
					aNewTM.putTypeMapping(leftHandSideVar, newLhsVarSet);
					aNewTM.putTypeMapping(receiverVar, newReceiverVarSet);
					permutationTMs.add(aNewTM);
				}
			}
		}

		return permutationTMs;
	}

	/**
	 * Finds different permutations of singleton sets for a method invocation in its complete form
	 * x = y.m(\ob(z))
	 * The return of the method is not a primitive type, but the method is from library code so the
	 * return and parameter variables are not available, and the method accepts their set of qualifiers 
	 * @param receiverVar
	 * @param leftHandSideVar
	 * @param methodAUTypings
	 * @param argOperands
	 * @param methodParsListTyping
	 * @param newTM
	 * @return
	 */
	private static List<TM> methInvoc4(Variable receiverVar,
			Variable leftHandSideVar, Set<OType> methodAUTypings,
			List<Variable> argOperands, List<Set<OType>> methodParsListTyping,
			TM newTM) {
		RankingStrategy ranking = RankingStrategy.getInstance();
		List<TM> permutationTMs = new ArrayList<TM>();
		
		Set<OType> leftHandSideVarSet = newTM.getAnalysisResult(leftHandSideVar);
		Set<OType> copyLeftHandSideVarSet = new SetOType<OType>(leftHandSideVarSet);
		
		Set<OType> copyMethodVarSet = new SetOType<OType>();
		
		Set<OType> receiverVarSet = newTM.getAnalysisResult(receiverVar);
		Set<OType> copyReceiverVarSet = new SetOType<OType>(receiverVarSet);
		
		List<Set<OType>> listArgVarSets = new ArrayList<Set<OType>>();
		for (Variable argVar : argOperands) {
			Set<OType> argVarSet = newTM.getAnalysisResult(argVar);
			listArgVarSets.add(argVarSet);
		}
		List<List<Set<OType>>> newListArgVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> argSet : listArgVarSets) {
			Set<OType> copyArgVarSet = new SetOType<OType>(argSet);
			List<Set<OType>> argVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : argSet) {
				OType argVarHighest = ranking.pickFromSet(copyArgVarSet, null);
				Set<OType> newArgVarSet = new SetOType<OType>();
				newArgVarSet.add(argVarHighest);
				copyArgVarSet.remove(argVarHighest);
				argVarPermutation.add(newArgVarSet);
			}
			newListArgVarSets.add(argVarPermutation);
		}
		List<List<Set<OType>>> newListParVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> parSet : methodParsListTyping) {
			Set<OType> copyParVarSet = new SetOType<OType>(parSet);
			List<Set<OType>> parVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : parSet) {
				OType parVarHighest = ranking.pickFromSet(copyParVarSet, null);
				Set<OType> newParVarSet = new SetOType<OType>();
				newParVarSet.add(parVarHighest);
				copyParVarSet.remove(parVarHighest);
				parVarPermutation.add(newParVarSet);
			}
			newListParVarSets.add(parVarPermutation);
		}
		for (OType receiverVarQualifier : receiverVarSet) {
			OType receiverVarHighest = ranking.pickFromSet(copyReceiverVarSet, null);
			Set<OType> newReceiverVarSet = new SetOType<OType>();
			newReceiverVarSet.add(receiverVarHighest);
			copyReceiverVarSet.remove(receiverVarHighest);
			for (OType leftHandSideVarQualifier : leftHandSideVarSet) {
				OType leftHandSideVarHighest = ranking.pickFromSet(copyLeftHandSideVarSet, null);
				Set<OType> newLeftHandSideVarSet = new SetOType<OType>();
				newLeftHandSideVarSet.add(leftHandSideVarHighest);
				copyLeftHandSideVarSet.remove(leftHandSideVarHighest);
				for (OType methodVarQualifier : methodAUTypings) {
					OType methodVarHighest = ranking.pickFromSet(copyMethodVarSet, null);
					Set<OType> newMethodVarSet = new SetOType<OType>();
					newMethodVarSet.add(methodVarHighest);
					copyMethodVarSet.remove(methodVarHighest);
					int i = 0;
					//XXX. This way we do not consider all the combinations
					for (List<Set<OType>> oType1 : newListArgVarSets) {
						for (Set<OType> set1 : oType1) {
							for (List<Set<OType>> oType2 : newListParVarSets) {
								for (Set<OType> set2 : oType2) {
									TM aNewTM = newTM.copyTypeMapping(newTM.getKey());
									aNewTM.putTypeMapping(receiverVar, newReceiverVarSet);
									aNewTM.putTypeMapping(leftHandSideVar, newLeftHandSideVarSet);
									aNewTM.putTypeMapping(argOperands.get(i), set1);
									permutationTMs.add(aNewTM);
								}
							}
						}
						i++;
					}
				}
			}
		}
		return permutationTMs;
	}

	/**
	 * Finds different permutations of singleton sets for a method invocation in its complete form
	 * x = y.m(\ob(z))
	 * The return of the method is not a primitive type and the method is not from library code 
	 * @param receiverVar
	 * @param leftHandSideVar
	 * @param methodVar
	 * @param argOperands
	 * @param parametersVarList
	 * @param newTM
	 * @return
	 */
	private static List<TM> methInvoc3(Variable receiverVar,
			Variable leftHandSideVar, Variable methodVar,
			List<Variable> argOperands, List<Variable> parametersVarList,
			TM newTM) {
		RankingStrategy ranking = RankingStrategy.getInstance();
		List<TM> permutationTMs = new ArrayList<TM>();
		
		Set<OType> leftHandSideVarSet = newTM.getAnalysisResult(leftHandSideVar);
		Set<OType> copyLeftHandSideVarSet = new SetOType<OType>(leftHandSideVarSet);
		
		Set<OType> methodVarSet = newTM.getAnalysisResult(methodVar);
		Set<OType> copyMethodVarSet = new SetOType<OType>(methodVarSet);
		
		Set<OType> receiverVarSet = newTM.getAnalysisResult(receiverVar);
		Set<OType> copyReceiverVarSet = new SetOType<OType>(receiverVarSet);
		
		List<Set<OType>> listArgVarSets = new ArrayList<Set<OType>>();
		for (Variable argVar : argOperands) {
			Set<OType> argVarSet = newTM.getAnalysisResult(argVar);
			listArgVarSets.add(argVarSet);
		}
		List<List<Set<OType>>> newListArgVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> argSet : listArgVarSets) {
			Set<OType> copyArgVarSet = new SetOType<OType>(argSet);
			List<Set<OType>> argVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : argSet) {
				OType argVarHighest = ranking.pickFromSet(copyArgVarSet, null);
				Set<OType> newArgVarSet = new SetOType<OType>();
				newArgVarSet.add(argVarHighest);
				copyArgVarSet.remove(argVarHighest);
				argVarPermutation.add(newArgVarSet);
			}
			newListArgVarSets.add(argVarPermutation);
		}
		List<Set<OType>> listParVarSets = new ArrayList<Set<OType>>();
		for (Variable parVar : parametersVarList) {
			Set<OType> parVarSet = newTM.getAnalysisResult(parVar);
			listParVarSets.add(parVarSet);
		}
		List<List<Set<OType>>> newListParVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> parSet : listParVarSets) {
			Set<OType> copyParVarSet = new SetOType<OType>(parSet);
			List<Set<OType>> parVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : parSet) {
				OType parVarHighest = ranking.pickFromSet(copyParVarSet, null);
				Set<OType> newParVarSet = new SetOType<OType>();
				newParVarSet.add(parVarHighest);
				copyParVarSet.remove(parVarHighest);
				parVarPermutation.add(newParVarSet);
			}
			newListParVarSets.add(parVarPermutation);
		}
		for (OType receiverVarQualifier : receiverVarSet) {
			OType receiverVarHighest = ranking.pickFromSet(copyReceiverVarSet, null);
			Set<OType> newReceiverVarSet = new SetOType<OType>();
			newReceiverVarSet.add(receiverVarHighest);
			copyReceiverVarSet.remove(receiverVarHighest);
			for (OType leftHandSideVarQualifier : leftHandSideVarSet) {
				OType leftHandSideVarHighest = ranking.pickFromSet(copyLeftHandSideVarSet, null);
				Set<OType> newLeftHandSideVarSet = new SetOType<OType>();
				newLeftHandSideVarSet.add(leftHandSideVarHighest);
				copyLeftHandSideVarSet.remove(leftHandSideVarHighest);
				for (OType methodVarQualifier : methodVarSet) {
					OType methodVarHighest = ranking.pickFromSet(copyMethodVarSet, null);
					Set<OType> newMethodVarSet = new SetOType<OType>();
					newMethodVarSet.add(methodVarHighest);
					copyMethodVarSet.remove(methodVarHighest);
					int i = 0;
					//XXX. This way we do not consider all the combinations
					for (List<Set<OType>> oType1 : newListArgVarSets) {
						for (Set<OType> set1 : oType1) {
							for (List<Set<OType>> oType2 : newListParVarSets) {
								for (Set<OType> set2 : oType2) {
									TM aNewTM = newTM.copyTypeMapping(newTM.getKey());
									aNewTM.putTypeMapping(receiverVar, newReceiverVarSet);
									aNewTM.putTypeMapping(leftHandSideVar, newLeftHandSideVarSet);
									aNewTM.putTypeMapping(methodVar, newMethodVarSet);
									aNewTM.putTypeMapping(argOperands.get(i), set1);
									aNewTM.putTypeMapping(parametersVarList.get(i), set2);
									permutationTMs.add(aNewTM);
								}
							}
						}
						i++;
					}
				}
			}
		}
		return permutationTMs;
	}

	/**
	 * The difference of this method with methInvoc1 is it is from library, so there is not
	 * list of parameters as parameters, only a list of set of qualifiers of parameters of the 
	 * method is available. The return type of the method is primitive
	 * @param receiverVar
	 * @param argOperands
	 * @param methodParsListTyping
	 * @param newTM
	 * @return
	 */
	private static List<TM> methInvoc2(Variable receiverVar,
			List<Variable> argOperands, List<Set<OType>> methodParsListTyping,
			TM newTM) {
		RankingStrategy ranking = RankingStrategy.getInstance();
		List<TM> permutationTMs = new ArrayList<TM>();
		
		Set<OType> receiverVarSet = newTM.getAnalysisResult(receiverVar);
		Set<OType> copyReceiverVarSet = new SetOType<OType>(receiverVarSet);
		List<Set<OType>> listArgVarSets = new ArrayList<Set<OType>>();
		for (Variable argVar : argOperands) {
			Set<OType> argVarSet = newTM.getAnalysisResult(argVar);
			listArgVarSets.add(argVarSet);
		}
		List<List<Set<OType>>> newListArgVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> argSet : listArgVarSets) {
			Set<OType> copyArgVarSet = new SetOType<OType>(argSet);
			List<Set<OType>> argVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : argSet) {
				OType argVarHighest = ranking.pickFromSet(copyArgVarSet, null);
				Set<OType> newArgVarSet = new SetOType<OType>();
				newArgVarSet.add(argVarHighest);
				copyArgVarSet.remove(argVarHighest);
				argVarPermutation.add(newArgVarSet);
			}
			newListArgVarSets.add(argVarPermutation);
		}
		for (OType receiverVarQualifier : receiverVarSet) {
			OType receiverVarHighest = ranking.pickFromSet(copyReceiverVarSet, null);
			Set<OType> newReceiverVarSet = new SetOType<OType>();
			newReceiverVarSet.add(receiverVarHighest);
			copyReceiverVarSet.remove(receiverVarHighest);
			int i = 0;
			//XXX. This way we do not consider all the combinations
			for (List<Set<OType>> oType1 : newListArgVarSets) {
				for (Set<OType> set1 : oType1) {
					TM aNewTM = newTM.copyTypeMapping(newTM.getKey());
					aNewTM.putTypeMapping(receiverVar, newReceiverVarSet);
					aNewTM.putTypeMapping(argOperands.get(i), set1);
					permutationTMs.add(aNewTM);
				}
				i++;
			}
		}
		return permutationTMs;
	}

	/**
	 * Finds different permutations of singleton sets when the conflict method invocation instruction
	 * when there are parameters and arguments for the method and the return type of the method is primitive
	 * @param receiverVar: receiver variable of the method invocation
	 * @param argOperands: List of argument variables of the method invocation
	 * @param parametersVarListL List of parameters of the method invocation
	 * @param newTM: A TM that contains the original set of qualifiers of the variables of the conflict instruction
	 * @return permutationTMs: an ordered list of TMs in which the the variables of the conflict instruction
	 * mapped to a singleton set. Different TMs of the list represent different permutations that the qualifiers
	 * of the variables of the conflict instruction can generate.
	 * The order of the list is based on our ranking on the qualifiers
	 */
	private static List<TM> methInvoc1(Variable receiverVar,
			List<Variable> argOperands, List<Variable> parametersVarList,
			TM newTM) {
		RankingStrategy ranking = RankingStrategy.getInstance();
		List<TM> permutationTMs = new ArrayList<TM>();
		
		Set<OType> receiverVarSet = newTM.getAnalysisResult(receiverVar);
		Set<OType> copyReceiverVarSet = new SetOType<OType>(receiverVarSet);
		List<Set<OType>> listArgVarSets = new ArrayList<Set<OType>>();
		for (Variable argVar : argOperands) {
			Set<OType> argVarSet = newTM.getAnalysisResult(argVar);
			listArgVarSets.add(argVarSet);
		}
		List<List<Set<OType>>> newListArgVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> argSet : listArgVarSets) {
			Set<OType> copyArgVarSet = new SetOType<OType>(argSet);
			List<Set<OType>> argVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : argSet) {
				OType argVarHighest = ranking.pickFromSet(copyArgVarSet, null);
				Set<OType> newArgVarSet = new SetOType<OType>();
				newArgVarSet.add(argVarHighest);
				copyArgVarSet.remove(argVarHighest);
				argVarPermutation.add(newArgVarSet);
			}
			newListArgVarSets.add(argVarPermutation);
		}
		List<Set<OType>> listParVarSets = new ArrayList<Set<OType>>();
		for (Variable parVar : parametersVarList) {
			Set<OType> parVarSet = newTM.getAnalysisResult(parVar);
			listParVarSets.add(parVarSet);
		}
		List<List<Set<OType>>> newListParVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> parSet : listParVarSets) {
			Set<OType> copyParVarSet = new SetOType<OType>(parSet);
			List<Set<OType>> parVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : parSet) {
				OType parVarHighest = ranking.pickFromSet(copyParVarSet, null);
				Set<OType> newParVarSet = new SetOType<OType>();
				newParVarSet.add(parVarHighest);
				copyParVarSet.remove(parVarHighest);
				parVarPermutation.add(newParVarSet);
			}
			newListParVarSets.add(parVarPermutation);
		}
		for (OType receiverVarQualifier : receiverVarSet) {
			OType receiverVarHighest = ranking.pickFromSet(copyReceiverVarSet, null);
			Set<OType> newReceiverVarSet = new SetOType<OType>();
			newReceiverVarSet.add(receiverVarHighest);
			copyReceiverVarSet.remove(receiverVarHighest);
			int i = 0;
			//XXX. This way we do not consider all the combinations
			for (List<Set<OType>> oType1 : newListArgVarSets) {
				for (Set<OType> set1 : oType1) {
					for (List<Set<OType>> oType2 : newListParVarSets) {
						for (Set<OType> set2 : oType2) {
							TM aNewTM = newTM.copyTypeMapping(newTM.getKey());
							aNewTM.putTypeMapping(receiverVar, newReceiverVarSet);
							aNewTM.putTypeMapping(argOperands.get(i), set1);
							aNewTM.putTypeMapping(parametersVarList.get(i), set2);
							permutationTMs.add(aNewTM);
						}
					}
				}
				i++;
			}
		}
		return permutationTMs;
	}

	/**
	 * Finds different permutations of singleton sets when the conflict instruction contains Three variables
	 * @param leftHandSideVar : Left hand side variable of the new expression
	 * @param argOperands: List of argument variables of the new expression
	 * @param newTM: A TM that contains the original set of qualifiers of the variables of the conflict instruction
	 * @return permutationTMs: an ordered list of TMs in which the the variables of the conflict instruction
	 * mapped to a singleton set. Different TMs of the list represent different permutations that the qualfiers
	 * of the variables of the conflict instruction can generate.
	 * The order of the list is based on our ranking on the qualifiers
	 */
	private static List<TM> newExpressionPermutations(Variable leftHandSideVar,
			List<Variable> argOperands, TM newTM) {
		RankingStrategy ranking = RankingStrategy.getInstance();
		List<TM> permutationTMs = new ArrayList<TM>();
		Set<OType> lhsVarSet = newTM.getAnalysisResult(leftHandSideVar);
		Set<OType> copyLhsVarSet = new SetOType<OType>(lhsVarSet);
		List<Set<OType>> listArgVarSets = new ArrayList<Set<OType>>();
		for (Variable argVar : argOperands) {
			Set<OType> argVarSet = newTM.getAnalysisResult(argVar);
			listArgVarSets.add(argVarSet);
		}
		List<List<Set<OType>>> newListArgVarSets = new ArrayList<List<Set<OType>>>();
		for (Set<OType> argSet : listArgVarSets) {
			Set<OType> copyArgVarSet = new SetOType<OType>(argSet);
			List<Set<OType>> argVarPermutation = new ArrayList<Set<OType>>();
			for (OType oType : argSet) {
				OType argVarHighest = ranking.pickFromSet(copyArgVarSet, null);
				Set<OType> newArgVarSet = new SetOType<OType>();
				newArgVarSet.add(argVarHighest);
				copyArgVarSet.remove(argVarHighest);
				argVarPermutation.add(newArgVarSet);
			}
			newListArgVarSets.add(argVarPermutation);
		}
		for (OType lhsVarQualifier : lhsVarSet) {
			OType leftHandSideVarHighest = ranking.pickFromSet(copyLhsVarSet, null);
			Set<OType> newLhsVarSet = new SetOType<OType>();
			newLhsVarSet.add(leftHandSideVarHighest);
			copyLhsVarSet.remove(leftHandSideVarHighest);
			int i = 0;
			//XXX. This way we do not consider all the combinations
			for (List<Set<OType>> oType : newListArgVarSets) {
				for (Set<OType> set : oType) {
					TM aNewTM = newTM.copyTypeMapping(newTM.getKey());
					aNewTM.putTypeMapping(leftHandSideVar, newLhsVarSet);
					aNewTM.putTypeMapping(argOperands.get(i), set);
					permutationTMs.add(aNewTM);
				}
				i++;
			}
		}
		return permutationTMs;
	}

	/**
	 * Finds different permutations of singleton sets when the conflict instruction contains Three variables
	 * @param firstVar: First variable of the conflict instruction
	 * @param secondVar: Second variable of the conflict instruction
	 * @param thirdVar: Third variable of the conflict instruction
	 * @param newTM: A TM that contains the original set of qualifiers of the variables of the conflict instruction
	 * @return permutationTMs: an ordered list of TMs in which the the variables of the conflict instruction
	 * mapped to a singleton set. Different TMs of the list represent different permutations that the qualfiers
	 * of the variables of the conflict instruction can generate.
	 * The order of the list is based on our ranking on the qualifiers
	 */
	private static List<TM> threeVariablePermutations(Variable firstVar,
			Variable secondVar, Variable thirdVar, TM newTM) {
		RankingStrategy ranking = RankingStrategy.getInstance();
		List<TM> permutationTMs = new ArrayList<TM>();
		Set<OType> firstVarSet = newTM.getAnalysisResult(firstVar);
		Set<OType> copyFirstVarSet = new SetOType<OType>(firstVarSet);
		Set<OType> secondVarSet = newTM.getAnalysisResult(secondVar);
		Set<OType> copySecondVarSet = new SetOType<OType>(secondVarSet);
		Set<OType> thirdVarSet = newTM.getAnalysisResult(thirdVar);
		Set<OType> copyThirdVarSet = new SetOType<OType>(thirdVarSet);

		// XXX: loop variable not being used?
		for (OType firstVarQualifier : firstVarSet) {
			OType firstVarHighest = ranking.pickFromSet(copyFirstVarSet, null);
			Set<OType> newFirstVarSet = new SetOType<OType>();
			newFirstVarSet.add(firstVarHighest);
			copyFirstVarSet.remove(firstVarHighest);
			copySecondVarSet.addAll(secondVarSet);
			// XXX: loop variable not being used?
			for (OType secondVarQualifier : secondVarSet) {
				OType secondVarHighest = ranking.pickFromSet(copySecondVarSet, null);
				Set<OType> newSecondVarSet = new SetOType<OType>();
				newSecondVarSet.add(secondVarHighest);
				copySecondVarSet.remove(secondVarHighest);
				copyThirdVarSet.addAll(thirdVarSet);
				// XXX: loop variable not being used?
				for (OType thirdVarQualifier : thirdVarSet) {
					OType thirdVarHighest = ranking.pickFromSet(copyThirdVarSet, null);
					Set<OType> newThirdVarSet = new SetOType<OType>();
					newThirdVarSet.add(thirdVarHighest);
					copyThirdVarSet.remove(thirdVarHighest);

					TM aNewTM = newTM.copyTypeMapping(newTM.getKey());
					aNewTM.putTypeMapping(firstVar, newFirstVarSet);
					aNewTM.putTypeMapping(secondVar, newSecondVarSet);
					aNewTM.putTypeMapping(thirdVar, newThirdVarSet);
					permutationTMs.add(aNewTM);
				}
			}
		}

		return permutationTMs;
	}

	/**
	 * Finds different permutations of singleton sets when the conflict instruction contains two variables
	 * @param firstVar: First variable of the conflict instruction
	 * @param secondVar: Second variable of the conflict instruction
	 * @param newTM: A TM that contains the original set of qualifiers of the variables of the conflict instruction 
	 * @return permutationTMs: an ordered list of TMs in which the the variables of the conflict instruction
	 * mapped to a singleton set. Different TMs of the list represent different permutations that the qualfiers
	 * of the variables of the conflict instruction can generate.
	 * The order of the list is based on our ranking on the qualifiers
	 */
	private static List<TM> twoVariablePermutations(Variable firstVar, Variable secondVar, TM newTM) {
		RankingStrategy ranking = RankingStrategy.getInstance();
		List<TM> permutationTMs = new ArrayList<TM>();
		Set<OType> firstVarSet = newTM.getAnalysisResult(firstVar);
		Set<OType> copyFirstVarSet = new SetOType<OType>(firstVarSet);
		Set<OType> secondVarSet = newTM.getAnalysisResult(secondVar);
		Set<OType> copySecondVarSet = new SetOType<OType>(secondVarSet);

		// Build All the permutations of the qualifiers of the two variables as a pair, from the highest
		//  ranked of the first one to the lowest ranked on the second one
		// Build Singleton sets for the variables
		// Create a fresh instance of TM with the singleton sets
		// Store the fressh TM in the permutationTMs
		// XXX: loop variable not being used?
		// Can just delete the for loop?		
		for (OType firstVarQualifier : firstVarSet) {
			OType firstVarHighest = ranking.pickFromSet(copyFirstVarSet, null);
			Set<OType> newFirstVarSet = new SetOType<OType>();
			newFirstVarSet.add(firstVarHighest);
			copyFirstVarSet.remove(firstVarHighest);
			copySecondVarSet.addAll(secondVarSet);
			// XXX: loop variable not being used?
			// Can just delete the for loop?
			for (OType secondVarQualifier : secondVarSet) {
				OType secondVarHighest = ranking.pickFromSet(copySecondVarSet, null);
				Set<OType> newSecondVarSet = new SetOType<OType>();
				newSecondVarSet.add(secondVarHighest);
				copySecondVarSet.remove(secondVarHighest);

				TM aNewTM = newTM.copyTypeMapping(newTM.getKey());
				aNewTM.putTypeMapping(firstVar, newFirstVarSet);
				aNewTM.putTypeMapping(secondVar, newSecondVarSet);
				permutationTMs.add(aNewTM);
			}
		}
		return permutationTMs;
	}
	
	public static Map<Variable, List<Set<OType>>> conflicVariablesAndSets(TACInstruction instr, TM newTM){
		Map<Variable, List<Set<OType>>> varsToSets = new HashMap<Variable, List<Set<OType>>>();
		RankingStrategy ranking = RankingStrategy.getInstance();
		if(instr instanceof CastInstruction){
			CastInstruction concreteInstr = (CastInstruction)instr;
			Variable operand = concreteInstr.getOperand();
			Set<OType> operandSet = newTM.getAnalysisResult(operand);
			List<Set<OType>> operandPermList = new ArrayList<Set<OType>>();
			for (int i = 0; i < operandSet.size(); i++) {
				OType highestQualifier = ranking.pickFromSet(operandSet, null);
				operandSet.remove(highestQualifier);
				Set<OType> singletonSet = new SetOType<OType>();
				singletonSet.add(highestQualifier);
				operandPermList.add(singletonSet);
			}
			varsToSets.put(operand, operandPermList);
			
			Variable target = concreteInstr.getTarget();
			Set<OType> targetSet = newTM.getAnalysisResult(target);
			List<Set<OType>> targetPermList = new ArrayList<Set<OType>>();
			for (int i = 0; i < targetSet.size(); i++) {
				OType highestQualifier = ranking.pickFromSet(targetSet, null);
				targetSet.remove(highestQualifier);
				Set<OType> singletonSet = new SetOType<OType>();
				singletonSet.add(highestQualifier);
				targetPermList.add(singletonSet);
			}
			varsToSets.put(target, targetPermList);
		}
		else if(instr instanceof CopyInstruction){
			CopyInstruction concreteInstr = (CopyInstruction)instr;
			Variable operand = concreteInstr.getOperand();
			Set<OType> operandSet = newTM.getAnalysisResult(operand);
			List<Set<OType>> operandPermList = new ArrayList<Set<OType>>();
			for (int i = 0; i < operandSet.size(); i++) {
				OType highestQualifier = ranking.pickFromSet(operandSet, null);
				operandSet.remove(highestQualifier);
				Set<OType> singletonSet = new SetOType<OType>();
				singletonSet.add(highestQualifier);
				operandPermList.add(singletonSet);
			}
			varsToSets.put(operand, operandPermList);
			
			Variable target = concreteInstr.getTarget();
			Set<OType> targetSet = newTM.getAnalysisResult(target);
			List<Set<OType>> targetPermList = new ArrayList<Set<OType>>();
			for (int i = 0; i < targetSet.size(); i++) {
				OType highestQualifier = ranking.pickFromSet(targetSet, null);
				targetSet.remove(highestQualifier);
				Set<OType> singletonSet = new SetOType<OType>();
				singletonSet.add(highestQualifier);
				targetPermList.add(singletonSet);
			}
			varsToSets.put(target, targetPermList);
		}
		else if(instr instanceof ReturnInstruction){
			ReturnInstruction concreteInstr = (ReturnInstruction)instr;
			Variable returnedVar = concreteInstr.getReturnedVariable();
			Set<OType> returnedVarSet = newTM.getAnalysisResult(returnedVar);
			List<Set<OType>> returnedVarPermList = new ArrayList<Set<OType>>();
			for (int i = 0; i < returnedVarSet.size(); i++) {
				OType highestQualifier = ranking.pickFromSet(returnedVarSet, null);
				returnedVarSet.remove(highestQualifier);
				Set<OType> singletonSet = new SetOType<OType>();
				singletonSet.add(highestQualifier);
				returnedVarPermList.add(singletonSet);
			}
			varsToSets.put(returnedVar, returnedVarPermList);

			//Finding the enclosing method this instruction
			ASTNode node = concreteInstr.getNode();
			while(!(node instanceof MethodDeclaration)){
				node=node.getParent();
			}
			MethodDeclaration methDecl = (MethodDeclaration)node;
			// Extracting the method variable from TM
			IMethodBinding methodBinding = methDecl.resolveBinding();
			Variable methodVar = null;
			if(!methodBinding.getReturnType().isPrimitive()){
				methodVar = newTM.getVarBindingMap(methodBinding);
				if(methodVar!=null){
					Set<OType> methodVarSet = newTM.getAnalysisResult(methodVar);
					List<Set<OType>> methodVarPermList = new ArrayList<Set<OType>>();
					for (int i = 0; i < methodVarSet.size(); i++) {
						OType highestQualifier = ranking.pickFromSet(methodVarSet, null);
						methodVarSet.remove(highestQualifier);
						Set<OType> singletonSet = new SetOType<OType>();
						singletonSet.add(highestQualifier);
						methodVarPermList.add(singletonSet);
					}
					varsToSets.put(methodVar, methodVarPermList);
				}
			}
		}
		else if(instr instanceof LoadFieldInstruction){
			LoadFieldInstruction concreteInstr = (LoadFieldInstruction)instr;
			Variable receiverVar = concreteInstr.getSourceObject();
			if(!(receiverVar instanceof ThisVariable)){
				Set<OType> receiverVarSet = newTM.getAnalysisResult(receiverVar);
				List<Set<OType>> receiverVarPermList = new ArrayList<Set<OType>>();
				for (int i = 0; i < receiverVarSet.size(); i++) {
					OType highestQualifier = ranking.pickFromSet(receiverVarSet, null);
					receiverVarSet.remove(highestQualifier);
					Set<OType> singletonSet = new SetOType<OType>();
					singletonSet.add(highestQualifier);
					receiverVarPermList.add(singletonSet);
				}
				varsToSets.put(receiverVar, receiverVarPermList);
			}	
			
			Variable target = concreteInstr.getTarget();
			Set<OType> targetSet = newTM.getAnalysisResult(target);
			List<Set<OType>> targetPermList = new ArrayList<Set<OType>>();
			for (int i = 0; i < targetSet.size(); i++) {
				OType highestQualifier = ranking.pickFromSet(targetSet, null);
				targetSet.remove(highestQualifier);
				Set<OType> singletonSet = new SetOType<OType>();
				singletonSet.add(highestQualifier);
				targetPermList.add(singletonSet);
			}
			varsToSets.put(target, targetPermList);

			// Extracting field variable
			IVariableBinding instrFieldBinding = concreteInstr.resolveFieldBinding();
			Variable fieldVar = newTM.getVarBindingMap(instrFieldBinding);
			if(fieldVar!=null){
				Set<OType> fieldVarSet = newTM.getAnalysisResult(fieldVar);
				List<Set<OType>> fieldVarPermList = new ArrayList<Set<OType>>();
				for (int i = 0; i < fieldVarSet.size(); i++) {
					OType highestQualifier = ranking.pickFromSet(fieldVarSet, null);
					fieldVarSet.remove(highestQualifier);
					Set<OType> singletonSet = new SetOType<OType>();
					singletonSet.add(highestQualifier);
					fieldVarPermList.add(singletonSet);
				}
				varsToSets.put(fieldVar, fieldVarPermList);
			}
		}
		else if(instr instanceof MethodCallInstruction){
			MethodCallInstruction concreteInstr = (MethodCallInstruction)instr;
			IMethodBinding methodBinding = concreteInstr.resolveBinding();
			List<Variable> argOperands = concreteInstr.getArgOperands();
			for (Variable argVar : argOperands) {
				Set<OType> argVarSet = newTM.getAnalysisResult(argVar);
				List<Set<OType>> argVarPermList = new ArrayList<Set<OType>>();
				for (int i = 0; i < argVarSet.size(); i++) {
					OType highestQualifier = ranking.pickFromSet(argVarSet, null);
					argVarSet.remove(highestQualifier);
					Set<OType> singletonSet = new SetOType<OType>();
					singletonSet.add(highestQualifier);
					argVarPermList.add(singletonSet);
				}
				varsToSets.put(argVar, argVarPermList);
			}
			
			Variable receiverVar = concreteInstr.getReceiverOperand();
			if(!(receiverVar instanceof ThisVariable)){
				Set<OType> receiverVarSet = newTM.getAnalysisResult(receiverVar);
				List<Set<OType>> receiverVarPermList = new ArrayList<Set<OType>>();
				for (int i = 0; i < receiverVarSet.size(); i++) {
					OType highestQualifier = ranking.pickFromSet(receiverVarSet, null);
					receiverVarSet.remove(highestQualifier);
					Set<OType> singletonSet = new SetOType<OType>();
					singletonSet.add(highestQualifier);
					receiverVarPermList.add(singletonSet);
				}
				varsToSets.put(receiverVar, receiverVarPermList);
			}
			
			if(!methodBinding.getReturnType().isPrimitive()){
				Variable target = concreteInstr.getTarget();
				Set<OType> targetSet = newTM.getAnalysisResult(target);
				List<Set<OType>> targetPermList = new ArrayList<Set<OType>>();
				for (int i = 0; i < targetSet.size(); i++) {
					OType highestQualifier = ranking.pickFromSet(targetSet, null);
					targetSet.remove(highestQualifier);
					Set<OType> singletonSet = new SetOType<OType>();
					singletonSet.add(highestQualifier);
					targetPermList.add(singletonSet);
				}
				varsToSets.put(target, targetPermList);
			}
			
			List<Variable> parametersVarList = new ArrayList<Variable>();
			IMethod javaElement = (IMethod) methodBinding.getJavaElement();
			boolean isFromSource = methodBinding.getDeclaringClass().isFromSource();
			if(isFromSource){
				Utils.extractParametersFromSource(parametersVarList, javaElement, newTM);
				for (Variable parVar : parametersVarList) {
					Set<OType> parVarSet = newTM.getAnalysisResult(parVar);
					List<Set<OType>> parVarPermList = new ArrayList<Set<OType>>();
					for (int i = 0; i < parVarSet.size(); i++) {
						OType highestQualifier = ranking.pickFromSet(parVarSet, null);
						parVarSet.remove(highestQualifier);
						Set<OType> singletonSet = new SetOType<OType>();
						singletonSet.add(highestQualifier);
						parVarPermList.add(singletonSet);
					}
					varsToSets.put(parVar, parVarPermList);
				}
				if(!methodBinding.getReturnType().isPrimitive()){
					Variable methodVar = newTM.getVarBindingMap(methodBinding);
					if(methodVar!=null){
						Set<OType> methodVarSet = newTM.getAnalysisResult(methodVar);
						List<Set<OType>> methodVarPermList = new ArrayList<Set<OType>>();
						for (int i = 0; i < methodVarSet.size(); i++) {
							OType highestQualifier = ranking.pickFromSet(methodVarSet, null);
							methodVarSet.remove(highestQualifier);
							Set<OType> singletonSet = new SetOType<OType>();
							singletonSet.add(highestQualifier);
							methodVarPermList.add(singletonSet);
						}
						varsToSets.put(methodVar, methodVarPermList);
					}
				}
			}
			// Else, this is library code.
			else {
				if(!methodBinding.getReturnType().isPrimitive()){
					Set<OType> methodVarSet = Utils.libraryMethod(methodBinding);
					List<Set<OType>> methodVarPermList = new ArrayList<Set<OType>>();
					for (int i = 0; i < methodVarSet.size(); i++) {
						OType highestQualifier = ranking.pickFromSet(methodVarSet, null);
						methodVarSet.remove(highestQualifier);
						Set<OType> singletonSet = new SetOType<OType>();
						singletonSet.add(highestQualifier);
						methodVarPermList.add(singletonSet);
					}
					// Create a syntactic variable for a method form library
					Variable methodVar = new TACMethod(null);
					varsToSets.put(methodVar , methodVarPermList);
				}
				List<Set<OType>> methodParsListTyping = Utils.libraryMethodParams(methodBinding);
				for (Set<OType> set : methodParsListTyping) {
					List<Set<OType>> methodVarPermList = new ArrayList<Set<OType>>();
					for (int i = 0; i < set.size(); i++) {
						OType highestQualifier = ranking.pickFromSet(set, null);
						set.remove(highestQualifier);
						Set<OType> singletonSet = new SetOType<OType>();
						singletonSet.add(highestQualifier);
						methodVarPermList.add(singletonSet);
					}
					// Create a syntactic variable for a parameter of a method form library
					Variable parVar = new TACVariable(null);
					varsToSets.put(parVar, methodVarPermList);
				}
			}
		}
		else if(instr instanceof NewObjectInstruction){
			NewObjectInstruction concreteInstr = (NewObjectInstruction)instr;
			Variable target = concreteInstr.getTarget();
			Set<OType> targetSet = newTM.getAnalysisResult(target);
			List<Set<OType>> targetPermList = new ArrayList<Set<OType>>();
			for (int i = 0; i < targetSet.size(); i++) {
				OType highestQualifier = ranking.pickFromSet(targetSet, null);
				targetSet.remove(highestQualifier);
				Set<OType> singletonSet = new SetOType<OType>();
				singletonSet.add(highestQualifier);
				targetPermList.add(singletonSet);
			}
			varsToSets.put(target, targetPermList);
			List<Variable> argOperands = concreteInstr.getArgOperands();
			for (Variable argVar : argOperands) {
				Set<OType> argVarSet = newTM.getAnalysisResult(argVar);
				List<Set<OType>> argVarPermList = new ArrayList<Set<OType>>();
				for (int i = 0; i < argVarSet.size(); i++) {
					OType highestQualifier = ranking.pickFromSet(argVarSet, null);
					argVarSet.remove(highestQualifier);
					Set<OType> singletonSet = new SetOType<OType>();
					singletonSet.add(highestQualifier);
					argVarPermList.add(singletonSet);
				}
				varsToSets.put(argVar, argVarPermList);
			}
		}
		else if(instr instanceof StoreFieldInstruction){
			StoreFieldInstruction concreteInstr = (StoreFieldInstruction)instr;
			Variable receiverVar = concreteInstr.getAccessedObjectOperand();
			if(!(receiverVar instanceof ThisVariable)){
				Set<OType> receiverVarSet = newTM.getAnalysisResult(receiverVar);
				List<Set<OType>> receiverVarPermList = new ArrayList<Set<OType>>();
				for (int i = 0; i < receiverVarSet.size(); i++) {
					OType highestQualifier = ranking.pickFromSet(receiverVarSet, null);
					receiverVarSet.remove(highestQualifier);
					Set<OType> singletonSet = new SetOType<OType>();
					singletonSet.add(highestQualifier);
					receiverVarPermList.add(singletonSet);
				}
				varsToSets.put(receiverVar, receiverVarPermList);
			}
			
			Variable rhsVar = concreteInstr.getSourceOperand();
			Set<OType> rhsVarSet = newTM.getAnalysisResult(rhsVar);
			List<Set<OType>> rhsVarPermList = new ArrayList<Set<OType>>();
			for (int i = 0; i < rhsVarSet.size(); i++) {
				OType highestQualifier = ranking.pickFromSet(rhsVarSet, null);
				rhsVarSet.remove(highestQualifier);
				Set<OType> singletonSet = new SetOType<OType>();
				singletonSet.add(highestQualifier);
				rhsVarPermList.add(singletonSet);
			}
			varsToSets.put(rhsVar, rhsVarPermList);
			
			// Extracting field variable
			IVariableBinding resolveFieldBinding = concreteInstr.resolveFieldBinding();
			Variable fieldVar = newTM.getVarBindingMap(resolveFieldBinding);
			if(fieldVar!=null){
				Set<OType> fieldVarSet = newTM.getAnalysisResult(fieldVar);
				List<Set<OType>> fieldVarPermList = new ArrayList<Set<OType>>();
				for (int i = 0; i < fieldVarSet.size(); i++) {
					OType highestQualifier = ranking.pickFromSet(fieldVarSet, null);
					fieldVarSet.remove(highestQualifier);
					Set<OType> singletonSet = new SetOType<OType>();
					singletonSet.add(highestQualifier);
					fieldVarPermList.add(singletonSet);
				}
				varsToSets.put(fieldVar, fieldVarPermList);
			}
		}
		return varsToSets;
	}

}
