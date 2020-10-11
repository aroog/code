
package oogre.refinements.tac;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oog.re.IOperation;
import oogre.analysis.Config;
import oogre.analysis.OOGContext;
import oogre.analysis.RefinementAnalysis;
import oogre.analysis.RefinementLatticeOps;
import oogre.utils.Utils;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.simple.AbstractingTransferFunction;
import edu.cmu.cs.crystal.tac.model.CastInstruction;
import edu.cmu.cs.crystal.tac.model.ConstructorCallInstruction;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.SourceVariableDeclaration;
import edu.cmu.cs.crystal.tac.model.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.model.SuperVariable;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.TempVariable;
import edu.cmu.cs.crystal.tac.model.ThisVariable;
import edu.cmu.cs.crystal.tac.model.Variable;

/*
 * TOEBI: This class should tie everything together.... This is the ONLY thing that gets called
 * 
 * 2nd pass: all the logic needs to be in here
 * XXX. Any other logic will not get called.
 * 
 * XXX. Need a 1st pass version that traverses other things. and builds a map
 * 
 * 
 * TODO: MOve back into refinements.tac package
 * 
 * XXX. Why so much duplication for createSuggestedRefinement?
 * - extract methods
 * 
 */
public class PushIntoOwnedTransferFunctions extends AbstractingTransferFunction<OOGContext> {
	
	private RefinementAnalysis analysis;
	
	private TM tm;
	
	private IOperation opr;
	
	private boolean isTypeCheching;
	
	// A map from each analyzed method declaration to its list of NON-conflict instructions
	// Every TF accept(...) adds to this set...if all goes well
	// This way, you can skip them the next time around and focus on the things that did not go well.
	
	// XXX. Make this a set. You are calling contains a lot. That's a linear search!
	private Set<TACInstruction> skipExprs = new HashSet<TACInstruction>();
	
	
	private static final boolean DISABLE_CREATION_IN_PD = true;
	private static final boolean ENABLE_THIS_RECEIVER_FOR_OWNED_METHOD = true;
	private static final boolean ENABLE_PASSING_OWNED_TO_PUBLIC_METHOD = false;
	private static final boolean ENABLE_REMOVE_FIELD_OWNED_RECEIVER_THIS = true;
	
	
	public void resetDoneWithMethod() {
		skipExprs.clear();
	}
	
	// Should you not copy the skipExprs?
	public void setSkipExprs(Set<TACInstruction> skipExprs) {
    	this.skipExprs = skipExprs;
    }

	public Set<TACInstruction> getSkipExprs() {
    	return skipExprs;
    }

	// XXX. opr can be null, during post-processing
	public PushIntoOwnedTransferFunctions(RefinementAnalysis analysis, TM tm, IOperation opr, boolean isTypeCheching) {
		this.analysis = analysis;
		this.tm = tm;
		this.opr = opr;
		this.isTypeCheching = isTypeCheching;
	}

	private void addNonConflictInstruction(TACInstruction instr){
		skipExprs.add(instr);
	}
	
	public IOperation getCurrentOpr() {
		return opr;
	}

	public TM getTM() {
    	return tm;
    }

	@Override
    public ILatticeOperations<OOGContext> getLatticeOperations() {
	    return new RefinementLatticeOps();
    }

	@Override
    public OOGContext createEntryValue(MethodDeclaration method) {
		OOGContext context = OOGContext.getInstance();
		return context;
    }
	
	/**
	 * Compute: lS = lS \cap rS
	 * Update TM: TM' = TM[au -> lS]
	 * @param au
	 * @param lS
	 * @param rS
	 */
	/*void intersectUpdateTM(AnnotatableUnit au, Set<OType> lS, Set<OType> rS) {
		lS.retainAll(rS);
		this.tm.putTypeMapping(au, lS);
	}*/
	
	/**
	 * Compute: lS = lS \cup  rS
	 * Update TM: TM' = TM[au -> lS]
	 * @param au
	 * @param lS
	 * @param rS
 	   XXX. Not used?! How come
	 */
/*	void unionUpdateTM(AnnotatableUnit au, Set<OType> lS, Set<OType> rS) {
		lS.addAll(rS);
		this.tm.putTypeMapping(au, lS);
	}*/
	
	
	/**
	 * x := y
	 */
	@Override
    public OOGContext transfer(CopyInstruction instr, OOGContext value) {
		super.transfer(instr, value);
		
		//Set of variables of the instruction in case of a conflict
		Set<Variable> varSet = new HashSet<Variable>();
		
		// Skip over the ones you do not to re-analyze...
		if(skipExprs.contains(instr)) {
			return super.transfer(instr, value);
		}
		
		ASTNode node = instr.getNode();
		String encClassName = Utils.findEnclosingClassName(node);
		boolean isMainClass = encClassName.equals(Config.MAINCLASS);
		String currentReceiverName = value.getCurrentReceiverName();
		
		Variable leftHandSide = instr.getTarget();
		varSet.add(leftHandSide);
		ITypeBinding lhsType = leftHandSide.resolveType();
		if(!lhsType.isPrimitive()){
			Set<OType> leftHandSideOType = this.tm.getTypeMapping(leftHandSide);
			if(leftHandSideOType == null){
				if(currentReceiverName.equals("new")){
					leftHandSideOType = this.tm.initTypeMapping(isMainClass,leftHandSide, false, true);
				}
				else{
					leftHandSideOType = this.tm.initTypeMapping(isMainClass,leftHandSide, true, true);
				}
				this.tm.putTypeMapping(leftHandSide, leftHandSideOType);
			}
			Set<OType> newleftHandSideOType = new SetOType<OType>(leftHandSideOType);

			Variable rightHandSide = instr.getOperand();
			varSet.add(rightHandSide);
			ITypeBinding rhsType = rightHandSide.resolveType();
			if(!rhsType.isPrimitive() && !rhsType.isNullType()){
				Set<OType> rightHandSideOType = this.tm.getTypeMapping(rightHandSide);
				if(rightHandSideOType == null){
					if(currentReceiverName.equals("new")){
						rightHandSideOType = this.tm.initTypeMapping(isMainClass,rightHandSide, false, true);
					}
					else{
						rightHandSideOType = this.tm.initTypeMapping(isMainClass,rightHandSide, true, true);
					}
					this.tm.putTypeMapping(rightHandSide, rightHandSideOType);
				}
				else{
					if(currentReceiverName!=null){
						if(!currentReceiverName.equals("new") && !currentReceiverName.equals("this")){
							createChangingQualifiers(currentReceiverName, leftHandSideOType, rightHandSideOType, newleftHandSideOType);
						}
						// if the right hand side of the copy construction is a new expression
//						else{
//							rightHandSideOType.addAll(newleftHandSideOType);
//						}
					}
				}
				
				Set<OType> newRightHandSideOType = new SetOType<OType>(rightHandSideOType);

				newleftHandSideOType.retainAll(newRightHandSideOType);
				newRightHandSideOType.retainAll(newleftHandSideOType);
				
				this.tm.putTypeMapping(leftHandSide, newleftHandSideOType);
				this.tm.putTypeMapping(rightHandSide, newRightHandSideOType);

				if(newleftHandSideOType.size()==0){
					emptySetAction(instr, "The left hand side and right hand side of the assignment statement: ",varSet);
					return value;
				}
			}
		}
		return value;
    }

	@Override
	public OOGContext transfer(CastInstruction instr, OOGContext value) {
		
		//Set of variables of the instruction in case of a conflict
		Set<Variable> varSet = new HashSet<Variable>();
				
		// Skip over the ones you do not to re-analyze...
		if(skipExprs.contains(instr)) {
			return super.transfer(instr, value);
		}
		
		//Finding the enclosing method of this instruction
		ASTNode node = instr.getNode();
		String encClassName = Utils.findEnclosingClassName(node);
		boolean isMainClass = encClassName.equals(Config.MAINCLASS);
		
		Variable operand = instr.getOperand();
		varSet.add(operand);
		Set<OType> operandOType = this.tm.getTypeMapping(operand);
		if(operandOType == null){
			operandOType = this.tm.initTypeMapping(isMainClass,operand, true, true);
			this.tm.putTypeMapping(operand, operandOType);
		}
		
		Variable target = instr.getTarget();
		varSet.add(target);
		Set<OType> targetOType = this.tm.getTypeMapping(target);
		if(targetOType == null){
			targetOType = this.tm.initTypeMapping(isMainClass,target, true, true);
			this.tm.putTypeMapping(target, targetOType);
		}
		
		operandOType.retainAll(targetOType);
		targetOType.retainAll(operandOType);
		
		this.tm.putTypeMapping(operand, operandOType);
		this.tm.putTypeMapping(target, targetOType);
		if(operandOType.size()==0){
			emptySetAction(instr, "The left hand side and right hand side of Cast : ",varSet);
			return value;
		}

		
		return value;
	}

	
	/**
	 * We need:
	 * 
	 * MethodCallInstr -> IMethodBinding 
	 * and
	 * List<IVariableBinding>
	 * s.t.
	 * 
	 * arguments/operands (Variable)
	 * 
	 * we need to assign:
	 * 
	 * arg_i -> Variable_i    
	 * param_i -> SrcVariable_i
	 * and then do work
	 * 
	 * param_i := arg_i // Is this here??? Or in the CopyInstruction?
	 * 
	 * Logic:
	 * - m   ->   List<ITypeBinding> for params
	 * 
	 * 
	 * Somewhere else: createEntry?
	 * 
	 * QUESTION: Where do we find the SrcTACVariables for MethodParams?
	 * 	 * Can we do it in one place instead of all over the place?
	 * 
	 * m -> Src
	 *  
	 * IMethodBinding -> MethodDeclaration
	 * 
	 * MethodDeclaration -> IVariableBinding
	 * 
	 * IVariableBinding -> Variable (TAC) 
	 * 
	 *  
	 */
	@Override
    public OOGContext transfer(MethodCallInstruction instr, OOGContext value) {
	    super.transfer(instr, value);
	    
	    //Set of variables of the instruction in case of a conflict
	    Set<Variable> varSet = new HashSet<Variable>();
	    
	    // Skip over the ones you do not to re-analyze...
	    if(skipExprs.contains(instr)) {
	    	return super.transfer(instr, value);
	    }
	    
	    boolean isPossible = true; // Is Adaptation needed, i.e., receiver is not 'this'? 
	    
	    // rBinding needs to always refer to the right method.
	    // If the method being called is a parameterized method,
	    // The method binding with the type substitution does NOT exist in the code!
	    // Always go back to the original (generic) method.
	    IMethodBinding rBinding = instr.resolveBinding().getMethodDeclaration();
	    
	    
	    ASTNode node = instr.getNode();
		String encClassName = Utils.findEnclosingClassName(node);
		
		value.addACalledMethod(rBinding);
		
		Variable receiver = instr.getReceiverOperand();
		Set<OType> receiverAUTypings = null;
		if((receiver instanceof ThisVariable) || (receiver instanceof SuperVariable)){
			isPossible = false;
		}
		else{
			varSet.add(receiver);
			receiverAUTypings = this.tm.getTypeMapping(receiver);
			if(receiverAUTypings==null){
				boolean isMainClass = encClassName.equals(Config.MAINCLASS);
				receiverAUTypings = this.tm.initTypeMapping(isMainClass,receiver, false, false);
				this.tm.putTypeMapping(receiver, receiverAUTypings);
			}
		}
		
		Variable methodVar = this.tm.getVarBindingMap(rBinding);
		// TOEBI: Delete commented out code if no longer needed
		// XXX. Figure me out
		// Related to library methods...
//		if(methodVar!=null && !instrIMethodBinding.getReturnType().isPrimitive()){
//			Set<OType> methodAUTypings = createLibraryTypingSet(instrIMethodBinding.getDeclaringClass().getQualifiedName());
//			this.tm.updateVariableTypingSet(methodVar, methodAUTypings);
//		}
		
		Set<OType> methodAUTypings =  new SetOType<OType>();
		List<Set<OType>> methodParsListTyping = new ArrayList<Set<OType>>();

		List<Variable> parametersVarList = new ArrayList<Variable>();
		IMethod javaElement = (IMethod) rBinding.getJavaElement();
		boolean isFromSource = rBinding.getDeclaringClass().isFromSource();
		if(isFromSource){
			Utils.extractParametersFromSource(parametersVarList, javaElement, this.tm);
		}
		// Else, this is library code.
		else {
			methodAUTypings = Utils.libraryMethod(rBinding);
			methodParsListTyping = Utils.libraryMethodParams(rBinding);
		}
		
		int parIndex = 0;
		List<Variable> argOperands = instr.getArgOperands();
		for (Variable arg : argOperands) {
			if(!arg.resolveType().isPrimitive()){
				if(!arg.resolveType().isNullType()){
					varSet.add(arg);
					//To handle if the field is an inherited field
					String argEnclosingClass = value.getFieldEnclosingType(arg);
					Set<OType> argeTypingSet = this.tm.getTypeMapping(arg);
					if(argeTypingSet==null){
						if(arg instanceof ThisVariable){
							argeTypingSet = this.tm.initThisVariable();
							this.tm.putTypeMapping(arg, argeTypingSet);
						}
						else{
							boolean isMainClass = argEnclosingClass.equals(Config.MAINCLASS);
							argeTypingSet = this.tm.initTypeMapping(isMainClass,arg, true, true);
							this.tm.putTypeMapping(arg, argeTypingSet);
						}
					}
				}
				else{
					if(isFromSource){
						parametersVarList.remove(parIndex);
					}
					else{
						methodParsListTyping.remove(parIndex);
					}
				}
				parIndex++;
			}
		}
		
		// Support Generic types 
		// Substitute an actual type with type parameter T in order to be able to do the lookup
		// XXX. HACK: hard-coded type parameter name "T"; will not work for "K" and "V".
		// XXX. Extract method (static) call it genTypeFtoA (subst. type arg for type param).
		//methodEnclosingClass = typeFtoA(methodDeclaringClass);
		
		//XXX. Returns null fo the add method of the Generic Type
		// Now lookup is problematic. Gets recorded in TraverseType is VectorNet
		// Here when we look the method up is Vector<Net>
		
		// leftHandSide == x
		Variable leftHandSide = null;
		if(!rBinding.getReturnType().isPrimitive()){
			leftHandSide = instr.getTarget();
			varSet.add(leftHandSide);
			//To handle if the field is an inherited field
			String lhsEnclosingClass = value.getFieldEnclosingType(leftHandSide);
			
			Set<OType> lhsTypingSet = this.tm.getTypeMapping(leftHandSide);
			if(lhsTypingSet==null){
				boolean isMainClass = lhsEnclosingClass.equals(Config.MAINCLASS);
				lhsTypingSet = this.tm.initTypeMapping(isMainClass,leftHandSide, true, true);
				this.tm.putTypeMapping(leftHandSide, lhsTypingSet);
			}
		}
		
		// methodVar == y_m
		// methodAUTypings == S[y_m] 
		if(methodVar!=null){
			methodAUTypings = this.tm.getTypeMapping(methodVar);
			varSet.add(methodVar);
		}
		
		Set<OType> leftHandSideTypings = null;
		if(leftHandSide!=null){
			leftHandSideTypings = this.tm.getTypeMapping(leftHandSide);
		}
		
		if(parametersVarList!=null){
			for (Variable param : parametersVarList) {
				varSet.add(param);
				Set<OType> paramAUTypings = new SetOType<OType>(this.tm.getTypeMapping(param));
				methodParsListTyping.add(paramAUTypings);
			}
		}
		List<Set<OType>> methodArgsListTyping = new ArrayList<Set<OType>>();
		for (Variable arg : argOperands) {
			if(!arg.resolveType().isPrimitive() && !arg.resolveType().isNullType()){
				Set<OType> argAUTypings = new SetOType<OType>(this.tm.getTypeMapping(arg));
				methodArgsListTyping.add(argAUTypings);
			}
		}
		
		// Strip "this." prefix from the receiver "this.y" in "this.y.m(z)"
		String receiverName = instr.getReceiverOperand().getSourceString();
		if(receiverName.contains("this")){
			receiverName=receiverName.substring(receiverName.indexOf('.')+1);
		}
		value.setCurrentReceiverName(receiverName);

		// Applying type system specific constraints
		if(methodAUTypings!=null && ENABLE_THIS_RECEIVER_FOR_OWNED_METHOD){
			TypeConstraints.tCall_ThisReceiverForOwnedMethod(receiverName , methodAUTypings);
		}
		
		Set<OType> retianedReceiverSet = null;
		Set<OType> sm = null;
		Set<OType> sx = null;

		// Adaptation is needed; receiver is not this
		if(isPossible){
			int i=0;
			if(methodParsListTyping.size()>0 && methodArgsListTyping.size()==methodParsListTyping.size()){
				List<Set<OType>> sReceiverList = new ArrayList<Set<OType>>(); 
				for (Set<OType> parTypingSet : methodParsListTyping) {
					
					// TOEBI: We need this constraint. If receiver is not this, cannot pass owned to public method. Breaks private domain.
					
					// Applying type system specific constraints
					if(ENABLE_PASSING_OWNED_TO_PUBLIC_METHOD){
						TypeConstraints.tCall_PassingOwnedToPublicMethod(rBinding, methodArgsListTyping.get(i));
					}
					
					ITypeBinding[] paramtType = rBinding.getParameterTypes();
					
					// x_m \in parTypingSet
					parTypingSet = substThisWithThat(parTypingSet);
					
					// Adapt-out
					Set<OType> szi = Adaptation.adaptOutSet(parTypingSet, receiverAUTypings, receiver, paramtType[i]);
					
					// check lent validity
					TypeConstraints.checkLentValidity(parTypingSet, szi);
					
					// szi == Q1o
					if(parametersVarList.size()>0){
						checkFinalInner(parametersVarList.get(i),receiver);
					}
					
					Set<OType> methodArgumentSet = new SetOType<OType>(methodArgsListTyping.get(i));
					createChangingQualifiers(receiverName, methodArgsListTyping.get(i), szi, methodArgumentSet);
					
					if(containsOnlyNPD(szi) && containsOnlyUinque(methodArgumentSet)){
						syncUniqueAndNPD(methodArgumentSet,szi);
						szi=methodArgumentSet;
					}
					else{
					// szi == Qz
						szi.retainAll(methodArgumentSet);
					}
					
					// spi == Q1i
					Set<OType> spi = Adaptation.adaptInSet(szi, receiverAUTypings, receiver, paramtType[i]);
					spi.retainAll(parTypingSet);
					// spi == Qxm
					
					if(!rBinding.getReturnType().isPrimitive() && methodAUTypings!=null){
						// Updating S[y_m]
						methodAUTypings = substThisWithThat(methodAUTypings);
						
						if(leftHandSideTypings!=null){
							sx = Adaptation.adaptOutSet(methodAUTypings, receiverAUTypings, receiver, rBinding.getReturnType());
							// sx == Q2o
							checkFinalInner(methodVar,receiver);
							
							// Check validity of lent
							TypeConstraints.checkLentValidity(methodAUTypings, sx);
							
							Set<OType> newLeftHandSideTypings = new SetOType<OType>(leftHandSideTypings);
							createChangingQualifiers(receiverName, leftHandSideTypings, sx, newLeftHandSideTypings);
							sx.retainAll(newLeftHandSideTypings);
							// sx == Qx
						}
						else{
							sx = Adaptation.adaptOutSet(methodAUTypings, receiverAUTypings, receiver, rBinding.getReturnType());
							checkFinalInner(methodVar,receiver);
						}
						
						// sm == Q2i
						sm = Adaptation.adaptInSet(sx, receiverAUTypings, receiver, rBinding.getReturnType());
						sm.retainAll(methodAUTypings);
						// sm == Qym
						
						// slhs == Q1r
						Set<OType> sReceiver = Adaptation.adaptRcvSet(szi, spi, receiver, this.tm, paramtType[i]);
						sReceiverList.add(sReceiver);
						
						//Substitute that with this for method the parameter
						spi = substThatWithReceiver(spi, "this");

						//Substitute that with this for the method return
						sm = substThatWithReceiver(sm, "this");
						
						this.tm.putTypeMapping(argOperands.get(i), szi);
						this.tm.putTypeMapping(leftHandSide, sx);
						
						
						if(isFromSource){
							this.tm.putTypeMapping(parametersVarList.get(i), spi);
							this.tm.putTypeMapping(methodVar, sm);
							updateOverriddenMethodMapping(rBinding,sm,value);
							updateOverriddenMethodParameterMapping(rBinding,spi,i,value);
						}
						
						if(sm.size()==0 || sx.size()==0 || spi.size()==0 || szi.size()==0){
							emptySetAction(instr, "Adaptation failed for the method invocation: ",varSet);
							return value;
						}
					}
					else{
						// slhs == Q1r
						Set<OType> sReceiver = Adaptation.adaptRcvSet(szi, spi, receiver, this.tm, paramtType[i]);
						sReceiverList.add(sReceiver);
						
						//Substitute that with receiver for the argument
						szi = substThatWithReceiver(szi, receiverName);
						//Substitute that with this for method the parameter
						spi = substThatWithReceiver(spi, "this");
						
						// DEBUG:
						// if(((SourceVariable)argOperands.get(i)).getSourceString().equals("freshVal")){
						// ((SetOType<OType>)szi).setVar(argOperands.get(i));
						// }
						
						this.tm.putTypeMapping(argOperands.get(i), szi);
						if(isFromSource){
							this.tm.putTypeMapping(parametersVarList.get(i), spi);
							updateOverriddenMethodParameterMapping(rBinding,spi,i,value);
						}
						if(spi.size()==0 || szi.size()==0){
							emptySetAction(instr, "Adaptation failed for the method invocation: ",varSet);
							return value;
						}
					}
					i++;
				}
				retianedReceiverSet = sReceiverList.get(0);
				for (Set<OType> typingSet : sReceiverList) {
					retianedReceiverSet.retainAll(typingSet);
				}
				// sy2 == Q2r
				if(sx!=null && sm!=null){
					Set<OType> sy2 = Adaptation.adaptRcvSet(sx, sm, receiver,this.tm, rBinding.getReturnType());
					retianedReceiverSet.retainAll(sy2);
				}
				retianedReceiverSet.retainAll(receiverAUTypings);
				
				this.tm.putTypeMapping(receiver, retianedReceiverSet);
				
				// Empty set => Discard TM
				if(retianedReceiverSet.size()==0){
					emptySetAction(instr, "Adaptation failed for the object creation: ",varSet);
					return value;
				}
			}
			else{
				if(!rBinding.getReturnType().isPrimitive() && methodAUTypings!=null){
					sx = null;
					int j = 0;
					methodAUTypings = substThisWithThat(methodAUTypings);
					if(leftHandSideTypings!=null){
						sx = Adaptation.adaptOutSet(methodAUTypings, receiverAUTypings, receiver, rBinding.getReturnType());
						//Check if the receiver should be final
						checkFinalInner(methodVar,receiver);
						Set<OType> newLeftHandSideTypings = new SetOType<OType>(leftHandSideTypings);
						createChangingQualifiers(receiverName, leftHandSideTypings, sx, newLeftHandSideTypings);
						sx.retainAll(newLeftHandSideTypings);
					}
					else{
						sx = Adaptation.adaptOutSet(methodAUTypings, receiverAUTypings, receiver, rBinding.getReturnType());
						checkFinalInner(methodVar,receiver);
					}
					
					
					sm = Adaptation.adaptInSet(sx, receiverAUTypings, receiver, rBinding.getReturnType());
					sm.retainAll(methodAUTypings);
					
					Set<OType> sy2 = Adaptation.adaptRcvSet(sx, sm, receiver,this.tm, rBinding.getReturnType());
					sy2.retainAll(receiverAUTypings);
					
					//Substitute that with receiver for left hand side
					sx = substThatWithReceiver(sx, receiverName);
					//Substitute that with this for method return
					sm = substThatWithReceiver(sm, "this");
					
					this.tm.putTypeMapping(leftHandSide, sx);
					this.tm.putTypeMapping(receiver, sy2);
					if(isFromSource){
						this.tm.putTypeMapping(methodVar, sm);
						updateOverriddenMethodMapping(rBinding,sm,value);
					}
					if(sx.size()==0 || sm.size()==0 || sy2.size()==0){
						emptySetAction(instr, "Adaptation failed for the method invocation: ",varSet);
						return value;
					}
				}
			}
		}
		else{
			value.setCurrentReceiverName("this");
			if((receiver instanceof ThisVariable) || (receiver instanceof SuperVariable)){
				int i=0;
				if(methodParsListTyping.size()>0 && methodArgsListTyping.size()==methodParsListTyping.size()){
					for (Set<OType> parTypingSet : methodParsListTyping) {
						// argTypingSet == S[z]
						Set<OType> argTypingSet = methodArgsListTyping.get(i);
						// parTypingSet == S[x_m]
						parTypingSet.retainAll(argTypingSet);
						argTypingSet.retainAll(parTypingSet);
						
						if(!rBinding.getReturnType().isPrimitive()){
							// leftHandSideTypings == S[x]
							leftHandSideTypings.retainAll(methodAUTypings);
							methodAUTypings.retainAll(leftHandSideTypings);
							
							this.tm.putTypeMapping(argOperands.get(i), argTypingSet);
							this.tm.putTypeMapping(leftHandSide, leftHandSideTypings);
							
							if(isFromSource){
								this.tm.putTypeMapping(parametersVarList.get(i), parTypingSet);
								this.tm.putTypeMapping(methodVar, methodAUTypings);
								updateOverriddenMethodMapping(rBinding,methodAUTypings,value);
								updateOverriddenMethodParameterMapping(rBinding,parTypingSet,i,value);
							}
							if(parTypingSet.size()==0 || argTypingSet.size()==0 || leftHandSideTypings.size()==0 || methodAUTypings.size()==0){
								emptySetAction(instr, "Adaptation failed for the method invocation: ",varSet);
								return value;
							}
						}
						else{
							this.tm.putTypeMapping(argOperands.get(i), argTypingSet);
							if(isFromSource){
								this.tm.putTypeMapping(parametersVarList.get(i), parTypingSet);
							}
							if(parTypingSet.size()==0 || argTypingSet.size()==0){
								emptySetAction(instr, "Adaptation failed for the method invocation: ",varSet);
								return value;
							}
						}
						i++;
					}
				}
				else{
					ITypeBinding methodReturnType = rBinding.getReturnType();
					if(!methodReturnType.isPrimitive() && methodVar!=null){
						methodAUTypings =  this.tm.getTypeMapping(methodVar);
						if(leftHandSideTypings!=null && methodAUTypings!=null){
							leftHandSideTypings.retainAll(methodAUTypings);
							methodAUTypings.retainAll(leftHandSideTypings);
							
							this.tm.putTypeMapping(leftHandSide, leftHandSideTypings);
							if(isFromSource){
								this.tm.putTypeMapping(methodVar, methodAUTypings);
								updateOverriddenMethodMapping(rBinding,methodAUTypings,value);
							}
							if(leftHandSideTypings.size()==0 || methodAUTypings.size()==0){
								emptySetAction(instr, "Adaptation failed for the method invocation: ",varSet);
								return value;
							}
						}
						// XXX. Figure me out
						// Related to library methods...
//						else {
//							if(methodAUTypings == null && !instrIMethodBinding.getReturnType().isPrimitive() ) {
//								methodAUTypings = createLibraryTypingSet(methodAU.getType());
//								this.tm.putTypeMapping(methodAU, methodAUTypings);
//							}
//						}
					}
				}
			}
			else{
				if(!rBinding.getReturnType().isPrimitive()){
					if(leftHandSideTypings!=null && methodAUTypings!=null){
						leftHandSideTypings.retainAll(methodAUTypings);
						methodAUTypings.retainAll(leftHandSideTypings);
						
						this.tm.putTypeMapping(leftHandSide, leftHandSideTypings);
						if(isFromSource){
							this.tm.putTypeMapping(methodVar, methodAUTypings);
							updateOverriddenMethodMapping(rBinding,methodAUTypings,value);
						}
						if(leftHandSideTypings.size()==0 || methodAUTypings.size()==0){
							emptySetAction(instr, "Adaptation failed for the method invocation: ",varSet);
							return value;
						}
					}
				}
			}
		}
	    return value;
    }

	@Override
	public OOGContext transfer(SourceVariableDeclaration instr, OOGContext value) {
		// Get the source variable that is being declared
		SourceVariable declaredVariable = instr.getDeclaredVariable();
		// Get the set of qualifiers of the variable that is being declared
		Set<OType> declVarSet = this.tm.getAnalysisResult(declaredVariable);
		
		// Check if the declared variable is the left-hand side of a new expression, call T-New for its ser
		// And remove the qualfiers that contain 'n.PD'
		VariableDeclaration node = instr.getNode();
		ASTNode parent = node.getParent();
		if(parent instanceof VariableDeclarationStatement){
			if(node.getInitializer()!=null && node.getInitializer() instanceof ClassInstanceCreation){
				TypeConstraints.tNewNoOthersPublicDomain(declVarSet);
			}
		}
		
		return value;
	}

	@Override
	public OOGContext transfer(ConstructorCallInstruction instr,
			OOGContext value) {
		
		//Set of variables of the instruction in case of a conflict
	    Set<Variable> varSet = new HashSet<Variable>();
		
		int i = 0;
		List<Variable> argOperands = instr.getArgOperands();
		for (Variable arg : argOperands) {
			if(this.tm.containsKey(arg)){
				IMethodBinding constMethBinding = instr.resolveBinding().getMethodDeclaration();
				List<Variable> parametersVarList = new ArrayList<Variable>();
				IMethod javaElement = (IMethod) constMethBinding.getJavaElement();
				List<Set<OType>> methodParsListTyping = new ArrayList<Set<OType>>();
				boolean isFromSource = constMethBinding.getDeclaringClass().isFromSource();
				if(isFromSource){
					Utils.extractParametersFromSource(parametersVarList, javaElement, this.tm);
					Set<OType> parSet = this.tm.getAnalysisResult(parametersVarList.get(i));
					Set<OType> argSet = this.tm.getAnalysisResult(arg);
					varSet.add(parametersVarList.get(i));
					varSet.add(arg);
					argSet.retainAll(parSet);
					tm.putTypeMapping(arg, argSet);
					tm.putTypeMapping(parametersVarList.get(i), argSet);
					if(argSet.size()==0){
						emptySetAction(instr, "Adaptation failed for the supercall: ",varSet);
						return value;
					}
				}
				else {
					methodParsListTyping = Utils.libraryMethodParams(constMethBinding);
					Set<OType> argSet = this.tm.getAnalysisResult(arg);
					argSet.retainAll(methodParsListTyping.get(i));
					tm.putTypeMapping(arg, argSet);
					varSet.add(arg);
					if(argSet.size()==0){
						emptySetAction(instr, "Adaptation failed for the supercall: ",varSet);
						return value;
					}
				}
			}
			i++;
		}
		return super.transfer(instr, value);
	}

	/**
	 * Sets the set of qualifiers of the overriding method for the overridden method in TM
	 */
	private void updateOverriddenMethodMapping(IMethodBinding overriddenMethodBinding,
			Set<OType> overriddenMethodSet, OOGContext value) {

		// top-down---> from overridden to overriding
		Set<IMethodBinding> overridingMethods = value.getOverridingMethods(overriddenMethodBinding);
		if(overridingMethods!=null){
			for (IMethodBinding iMethodBinding : overridingMethods) {
				if(iMethodBinding.isSubsignature(overriddenMethodBinding)){
					Variable overridingMethodVar = this.tm.getVarBindingMap(iMethodBinding);
					if(overridingMethodVar != null){
						this.tm.putTypeMapping(overridingMethodVar, overriddenMethodSet);
					}
				}
			}
		}
		
		// bottom-up ---> from overriding to overridden
		Set<IMethodBinding> overridenMethods = value.getOverridenMethods();
		for (IMethodBinding iMethodBinding : overridenMethods) {
			if(overriddenMethodBinding.isSubsignature(iMethodBinding)){
				Variable overridingMethodVar = this.tm.getVarBindingMap(iMethodBinding);
				if(overridingMethodVar != null){
					this.tm.putTypeMapping(overridingMethodVar, overriddenMethodSet);
				}
			}
		}
		
	}
	
	/**
	 * Sets the set of qualifiers of the overriding method for the overridden method in TM
	 */
	private void updateOverriddenMethodParameterMapping(IMethodBinding overriddenMethodBinding,
			Set<OType> overriddenMethodParameterSet, int paramIndex, OOGContext value) {
		
		Variable overridingVar = null;
		
		// top-down---> from overridden to overriding
		Set<List<IVariableBinding>> overridingMethodsParams = value.getOverridingMethodsParams(overriddenMethodBinding);
		if(overridingMethodsParams!=null){
			for (List<IVariableBinding> overridingParamsList : overridingMethodsParams) {
				IVariableBinding paramBinding = overridingParamsList.get(paramIndex);
				Variable overridingParamVariable = this.tm.getVariableFromBindingKey(paramBinding.getKey());
				if(overridingParamVariable!=null){
					overridingVar = overridingParamVariable;
					Set<OType> overridingSet = this.tm.getAnalysisResult(overridingParamVariable);
					overridingSet.retainAll(overriddenMethodParameterSet);
					this.tm.putTypeMapping(overridingParamVariable, overridingSet);
				}
			}
		}
		
		// bottom-up ---> from overriding to overridden
		Set<IMethodBinding> overriddenMethods = value.getOverridenMethods();
		for (IMethodBinding iMethodBinding : overriddenMethods) {
			if(overriddenMethodBinding.isSubsignature(iMethodBinding)){
				List<IVariableBinding> overriddenMethodParameters = value.getOverriddenMethodParameters(iMethodBinding.getKey());
				Variable overriddenParamVariable = this.tm.getVariableFromBindingKey(overriddenMethodParameters.get(paramIndex).getKey());
				if(overriddenParamVariable!=null && overridingVar!=null){
					Set<OType> overriddenSet = this.tm.getAnalysisResult(overriddenParamVariable);
					Set<OType> overridingSet = this.tm.getAnalysisResult(overridingVar);
					overriddenSet.retainAll(overridingSet);
					overridingSet.retainAll(overriddenSet);
					this.tm.putTypeMapping(overriddenParamVariable, overriddenSet);
					this.tm.putTypeMapping(overridingVar, overridingSet);
				}
			}
		}
	}

	private String finalErrorMessage(IVariableBinding variableBinding) {
		String variableKind = "";
		String encClass = "";
		if(variableBinding.isField()){
			variableKind = " field ";
			encClass = variableBinding.getDeclaringClass().getQualifiedName();
		}
		else if(variableBinding.isParameter()){
			variableKind = " method parameter ";
			encClass = variableBinding.getDeclaringMethod().getDeclaringClass().getQualifiedName();
		}
		else {
			variableKind = " local variable ";
			encClass = variableBinding.getDeclaringMethod().getDeclaringClass().getQualifiedName();
		}
		StringBuilder errorMsg = new StringBuilder();
		errorMsg.append("The ");
		errorMsg.append(variableKind);
		errorMsg.append(variableBinding.getName());
		errorMsg.append(" in the class ");
		errorMsg.append(encClass);
		errorMsg.append(" is not final");
		
		return errorMsg.toString();
	}

	@Override
    public OOGContext transfer(LoadFieldInstruction instr, OOGContext value) {
	    super.transfer(instr, value);
	    
	    //Set of variables of the instruction in case of a conflict
	    Set<Variable> varSet = new HashSet<Variable>();
	    
	    // Skip over the ones you do not to re-analyze...
	    if(skipExprs.contains(instr)) {
	    	return super.transfer(instr, value);
	    }
	    
	    boolean isPossible = true; // Is adaptation needed, i.e., is the receiver not 'this'
	    
	    ASTNode node = instr.getNode();
		String encClassName = Utils.findEnclosingClassName(node);
		
		Variable leftHandSide = instr.getTarget();
		varSet.add(leftHandSide);
		Set<OType> lhsTypingSet = this.tm.getTypeMapping(leftHandSide);
		if(lhsTypingSet==null){
			boolean isMainClass = encClassName.equals(Config.MAINCLASS);
			lhsTypingSet = this.tm.initTypeMapping(isMainClass,leftHandSide, true, true);
			this.tm.putTypeMapping(leftHandSide, lhsTypingSet);
		}
	
//		Variable receiver = instr.getAccessedObjectOperand();
		Variable receiver = instr.getSourceObject();
		String receiverName = "";
		String receiverEnclosingClass = "";
		Set<OType> receiverTypings = null;
		if(receiver instanceof ThisVariable){
			isPossible = false;
		}
		else{
			varSet.add(receiver);
			// Strip "this." prefix from the receiver "this.x" in "this.x.f"
			receiverName = receiver.getSourceString();
			if(receiverName.contains("this")){
				receiverName=receiverName.substring(receiverName.indexOf('.')+1);
			}
			
			receiverEnclosingClass = value.getFieldEnclosingType(receiver);
			receiverTypings = this.tm.getTypeMapping(receiver);
			if(receiverTypings==null){
				boolean isMainClass = receiverEnclosingClass.equals(Config.MAINCLASS);
				receiverTypings = this.tm.initTypeMapping(isMainClass,receiver, true, true);
				this.tm.putTypeMapping(receiver, receiverTypings);
			}
		}
		
		value.setCurrentReceiverName(receiverName);
	
		Set<OType> fieldTypings = null;
		IVariableBinding instrFieldBinding = instr.resolveFieldBinding().getVariableDeclaration();
		Variable fieldVariable = null;
		
		if(instrFieldBinding.getType().isTypeVariable()){
			fieldVariable = initializeTypeVariableVars(value, instrFieldBinding);
		}
		else{
			fieldVariable = this.tm.getVarBindingMap(instrFieldBinding);
			if(fieldVariable!=null){
				varSet.add(fieldVariable);
				fieldTypings = this.tm.getTypeMapping(fieldVariable);
			}
			else{
				isPossible = false;
			}
		}

		// TODO: inline
		Set<OType> leftHandSideTypings = lhsTypingSet;
		
		if(isPossible){
			// fieldTypings == S[f]
			fieldTypings  = substThisWithThat(fieldTypings);
			
			// sSecond == Qo
			Set<OType> sSecond = Adaptation.adaptOutSet(fieldTypings, receiverTypings, receiver, instrFieldBinding.getType());
			
			checkFinalInner(fieldVariable, receiver);
			
			TypeConstraints.checkLentValidity(fieldTypings, sSecond);
			
			Set<OType> newLeftHandSideTypings = new SetOType<OType>(leftHandSideTypings);
			if(leftHandSideTypings !=null && leftHandSideTypings.size()>0){
				createChangingQualifiers(receiverName, leftHandSideTypings,	sSecond, newLeftHandSideTypings);
				sSecond.retainAll(newLeftHandSideTypings);
			}
			// sSecond == Qy
			
			// sCapital == Qi
			Set<OType> sCapital = Adaptation.adaptInSet(sSecond, receiverTypings, receiver, instrFieldBinding.getType());
			sCapital.retainAll(fieldTypings);
			// sCapital == Qf
			
			// sPrime == Qr
			Set<OType> sPrime = Adaptation.adaptRcvSet(sSecond, sCapital, receiver, this.tm, instrFieldBinding.getType());
			sPrime.retainAll(receiverTypings);
			// sPrime == Qx
			
			//Substitute that with this for field
			sCapital = substThatWithReceiver(sCapital, "this");
			
			this.tm.putTypeMapping(leftHandSide, sSecond);
			this.tm.putTypeMapping(fieldVariable, sCapital);
			this.tm.putTypeMapping(receiver, sPrime);
			
			if(sPrime.size()==0 || sCapital.size()==0 || sSecond.size()==0){
				emptySetAction(instr, "Adaptation failed for the field read: ",varSet);
				return value;
			}
		}
		else{
			if(receiver instanceof ThisVariable){
				value.setCurrentReceiverName("this");
				IVariableBinding fieldBinding = ((TACVariable)fieldVariable).getVarDecl();
				if(Modifier.isFinal(fieldBinding.getModifiers())){
					value.putfinalTempVariable(leftHandSide, fieldVariable);
				}
				if(fieldTypings!=null && fieldTypings.size()>0 && leftHandSideTypings!=null && leftHandSideTypings.size()>0 && !fieldVariable.equals(leftHandSide)){
					fieldTypings.retainAll(leftHandSideTypings);
					leftHandSideTypings.retainAll(fieldTypings);
					
					this.tm.putTypeMapping(fieldVariable, fieldTypings);
					this.tm.putTypeMapping(leftHandSide, leftHandSideTypings);
					
					if(fieldTypings.size()==0 || leftHandSideTypings.size()==0){
						emptySetAction(instr, "Adaptation failed for the field read: ",varSet);
						return value;
					}
				}
			}
		}
		return value;
    }
	
	/**
	 * A method to initialize a variable that is being declared with a type variable 
	 * but it being accessed with a type argument
	 * @param value : to add a mapping from the variable binding key to variable itself
	 * @param instrFieldBinding: the binding of a variable that is being initialized
	 */


	private Variable initializeTypeVariableVars(OOGContext value, IVariableBinding instrFieldBinding) {
		final TACVariable fieldVar = new TACVariable(instrFieldBinding);
		value.putAllBindingKeyToVariableMap(instrFieldBinding.getKey(), fieldVar);
		
		Variable fieldBindingInMap = this.tm.getVarBindingMap(instrFieldBinding);
		if(fieldBindingInMap==null){
			this.tm.putVarBindingMap(instrFieldBinding, fieldVar);
		}
		String enclosingTypeName = instrFieldBinding.getDeclaringClass().getQualifiedName();
		Set<OType> fieldTypingSet = this.tm.getTypeMapping(fieldVar);
		
		if(fieldTypingSet==null){
			boolean isMainClass = enclosingTypeName.equals(Config.MAINCLASS);
			fieldTypingSet = this.tm.initTypeMapping(isMainClass,fieldVar, false, false);
			this.tm.putTypeMapping(fieldVar, fieldTypingSet);
		}
		return fieldVar;
	}

	/**
	 * Create set containing n.PD from a set containing this.PD (this.PD are being replaced, not being added to).
	 * If there is an n'.PD, it copies it to the new outer set.
	 * 
	 * @param receiverName is 'n'
	 * @param outerVariableSet the set that contains 'this.PD'
	 * @param adaptationResultSet the resulting set that contains 'n.PD'
	 * @param newOuterVariableSet the set where 'this.PD' is replaced with 'n.PD' (the return)
	 */
	private void createChangingQualifiers(String receiverName, Set<OType> outerVariableSet, Set<OType> adaptationResultSet, Set<OType> newOuterVariableSet) {
		for (OType adaptedTypes : adaptationResultSet) {
			if((containsReceiver(adaptedTypes.getOwner(),receiverName) && !adaptedTypes.getOwner().contains(".any")) || (adaptedTypes.getAlpha()!=null && containsReceiver(adaptedTypes.getAlpha(),receiverName) && !adaptedTypes.getAlpha().contains(".any"))){
				for (OType outOType : outerVariableSet) {
					OType newType = null;
					if(containsReceiver(adaptedTypes.getOwner(),receiverName) && containsReceiver(adaptedTypes.getAlpha(),receiverName)){
						if(outOType.getOwner().equals("this.PD") && outOType.getAlpha().equals("this.PD")){
							newType = new OType(ThatThisSubst.substThisWithRec(outOType.getOwner(), receiverName),ThatThisSubst.substThisWithRec(outOType.getAlpha(), receiverName));
							newOuterVariableSet.add(newType);
							break;
						}
					}
					else if(containsReceiver(adaptedTypes.getOwner(),receiverName) && !containsReceiver(adaptedTypes.getAlpha(),receiverName)){
						if(outOType.getOwner().equals("this.PD")){
							if(outOType.getAlpha().equals(adaptedTypes.getAlpha())){
								newType = new OType(ThatThisSubst.substThisWithRec(outOType.getOwner(), receiverName), ThatThisSubst.substThisWithRec(outOType.getAlpha(),"this"));
								newOuterVariableSet.add(newType);
								break;
							}
							else if(adaptedTypes.getAlpha().contains(".PD") && !adaptedTypes.getAlpha().contains("this.PD") && !adaptedTypes.getAlpha().contains(receiverName)){
								newOuterVariableSet.add(adaptedTypes);
							}
						}
					}
					else if(!containsReceiver(adaptedTypes.getOwner(),receiverName) && containsReceiver(adaptedTypes.getAlpha(),receiverName)){
						if(outOType.getAlpha().equals("this.PD")){
							if(outOType.getOwner().equals(adaptedTypes.getOwner()) ){
								newType = new OType(ThatThisSubst.substThisWithRec(outOType.getOwner(),"this"),ThatThisSubst.substThisWithRec(outOType.getAlpha(), receiverName));
								newOuterVariableSet.add(newType);
								break;
							}
							else if(adaptedTypes.getOwner().contains(".PD") && !adaptedTypes.getOwner().contains("this.PD") && !adaptedTypes.getOwner().contains(receiverName)){
								newOuterVariableSet.add(adaptedTypes);
							}
						}
					}
				}
			}
		}
	}

	private boolean containsReceiver(String owner, String receiverName) {
		return owner.contains(receiverName);
	}

	@Override
    public OOGContext transfer(StoreFieldInstruction instr, OOGContext value) {
	    super.transfer(instr, value);
	    
	    //Set of variables of the instruction in case of a conflict
	    Set<Variable> varSet = new HashSet<Variable>();

	    // Skip over the ones you do not to re-analyze...
	    if(skipExprs.contains(instr)) {
	    	return super.transfer(instr, value);
	    }
	    
	    boolean isPossible = true; // Is adaptation needed, i.e., is receiver not 'this'?
	    
	    ASTNode node = instr.getNode();
		String encClassName = Utils.findEnclosingClassName(node);
		boolean isMainClass = encClassName.equals(Config.MAINCLASS);
		
		IVariableBinding resolveFieldBinding = instr.resolveFieldBinding().getVariableDeclaration();
		Variable fieldVariable = this.tm.getVarBindingMap(resolveFieldBinding);
		Set<OType> fieldTypings = null;
		if(fieldVariable!=null){
			fieldTypings = this.tm.getTypeMapping(fieldVariable);
			varSet.add(fieldVariable);
		}
		else{
			if(resolveFieldBinding.getType().isTypeVariable()){
				fieldVariable = initializeTypeVariableVars(value, resolveFieldBinding);
			}
			else{
				isPossible = false;
			}
		}

		// receiver == x
		Variable receiver = instr.getAccessedObjectOperand();
		String receiverName = "";
		Set<OType> receiverTypings = null;
		if(receiver instanceof ThisVariable){
			isPossible = false;
		}
		else{
			varSet.add(receiver);
			// Strip "this." prefix from the receiver "this.x" in "this.x.f = y"
			receiverName = receiver.getSourceString();
			if(receiverName.contains("this")){
				receiverName=receiverName.substring(receiverName.indexOf('.')+1);
			}
			
			receiverTypings = this.tm.getTypeMapping(receiver);
			if(receiverTypings==null){
				receiverTypings = this.tm.initTypeMapping(isMainClass,receiver, true, true);
				this.tm.putTypeMapping(receiver, receiverTypings);
			}
		}
		value.setCurrentReceiverName(receiverName);

		// rightHandSide == y
		Variable rightHandSide = instr.getSourceOperand();
		varSet.add(rightHandSide);
		ITypeBinding rhsType = rightHandSide.resolveType();
		
		Set<OType> rightHandSideTypings = null;
		if(rhsType.isPrimitive() || rhsType.isNullType()){
			isPossible = false;
		}
		else{
			rightHandSideTypings = this.tm.getTypeMapping(rightHandSide);
			if(rightHandSideTypings==null){
				if(rightHandSide instanceof ThisVariable){
					rightHandSideTypings = this.tm.initThisVariable();
					this.tm.putTypeMapping(rightHandSide, rightHandSideTypings);
				}
				else{
					rightHandSideTypings = this.tm.initTypeMapping(isMainClass,rightHandSide, true, true);
					this.tm.putTypeMapping(rightHandSide, rightHandSideTypings);
				}
			}
		}

		
		if(isPossible){
			// Applying type system specific constraints
			if(ENABLE_REMOVE_FIELD_OWNED_RECEIVER_THIS){
				TypeConstraints.tWrite(receiverName, fieldTypings);
			}
			
			Set<OType> sSecond = null;
			fieldTypings = substThisWithThat(fieldTypings);
			
			if(rightHandSideTypings != null){
				// sSecond == Qo
				sSecond = Adaptation.adaptOutSet(fieldTypings, receiverTypings, receiver, resolveFieldBinding.getType());
				checkFinalInner(fieldVariable, receiver);
				
				// check lent validity
				TypeConstraints.checkLentValidity(fieldTypings, sSecond);
				
				Set<OType> newrightHandSideTypings = new SetOType<OType>(rightHandSideTypings);
				createChangingQualifiers(receiverName, rightHandSideTypings, sSecond, newrightHandSideTypings);
				
				sSecond.retainAll(newrightHandSideTypings);
				// sSecond == Qy
			}
			else{
				sSecond = Adaptation.adaptOutSet(fieldTypings, receiverTypings, receiver, resolveFieldBinding.getType());
				checkFinalInner(fieldVariable, receiver);
				
				// check lent validity
				TypeConstraints.checkLentValidity(fieldTypings, sSecond);
			}
			
			// sCapital == Qi
			Set<OType> sCapital = Adaptation.adaptInSet(sSecond, receiverTypings, receiver, resolveFieldBinding.getType());
			sCapital.retainAll(fieldTypings);
			// sCapital == Qf
			
			Set<OType> sPrime = Adaptation.adaptRcvSet(sSecond, sCapital, receiver, this.tm, resolveFieldBinding.getType());
			sPrime.retainAll(receiverTypings);
			// sPrime == Qr
			
			//Substitute that with this for field
			sCapital = substThatWithReceiver(sCapital, "this");
			
			this.tm.putTypeMapping(rightHandSide, sSecond);
			this.tm.putTypeMapping(fieldVariable, sCapital);
			this.tm.putTypeMapping(receiver, sPrime);
			
			if(sPrime.size()==0 || sCapital.size()==0 || sSecond.size()==0){
				emptySetAction(instr, "Adaptation failed for the field write: ",varSet);
				return value;
			}
		}
		else{
			// this.f = y
			if(receiver instanceof ThisVariable){
				value.setCurrentReceiverName("this");
				if(fieldTypings!=null && fieldTypings.size()>0 && rightHandSideTypings!=null && rightHandSideTypings.size()>0 && !fieldVariable.equals(rightHandSide)){
					fieldTypings.retainAll(rightHandSideTypings);
					rightHandSideTypings.retainAll(fieldTypings);
					
					this.tm.putTypeMapping(fieldVariable, fieldTypings);
					this.tm.putTypeMapping(rightHandSide, rightHandSideTypings);
					
					if(fieldTypings.size()==0 || rightHandSideTypings.size()==0){
						emptySetAction(instr, "Adaptation failed for the field write: ",varSet);
						return value;
					}
				}
			}
		}
	    return value;
    }

	private void checkFinalInner(Variable innerVariable, Variable receiver) {
		if(receiver instanceof SourceVariable){
			SourceVariable srcReceiver = (SourceVariable)receiver;
			IVariableBinding receiverBinding = srcReceiver.getBinding();
			if(!Utils.isFinal(receiverBinding)){
				System.out.println(finalErrorMessage(receiverBinding));
			}
		}
		else if(receiver instanceof TACVariable){
			TACVariable srcReceiver = (TACVariable)receiver;
			IVariableBinding receiverBinding = srcReceiver.getVarDecl();
			if(!Utils.isFinal(receiverBinding)){
				System.out.println(finalErrorMessage(receiverBinding));
			}
		}
	}
	
	@Override
	public OOGContext transfer(NewObjectInstruction instr, OOGContext value) {
		super.transfer(instr, value);
		
		//Set of variables of the instruction in case of a conflict
		Set<Variable> varSet = new HashSet<Variable>();

		// Skip over the ones you do not to re-analyze...
		if(skipExprs.contains(instr)) {
			return super.transfer(instr, value);
		}
		ITypeBinding instantiatedType = instr.resolveInstantiatedType();
		ASTNode node = instr.getNode();
		String enclosingClassName = Utils.findEnclosingClassName(node);
		
		value.setCurrentReceiverName("new");
		
		TACNewExpr newExprVar = null;
		ITypeBinding enclosingClassBinding = Utils.findEnclosingClassBinding(node);
		IMethodBinding enclosingMethodBinding = Utils.findEnclosingMethodBinding(node);
		if(node instanceof ClassInstanceCreation){
			ClassInstanceCreation cicNode = (ClassInstanceCreation)node;
			IMethodBinding constructorBinding = cicNode.resolveConstructorBinding();
			newExprVar = new TACNewExpr(constructorBinding, enclosingClassBinding, enclosingMethodBinding, instantiatedType);
		}
		
		Set<OType> newExprSet = null;
		if(newExprVar!=null){
			varSet.add(newExprVar);
			newExprSet = this.tm.getAnalysisResult(newExprVar);
		}
		
		Variable leftHandSide = instr.getTarget();
		varSet.add(leftHandSide);
		String lhsName = leftHandSide.getSourceString();
		Set<OType> lhsTypingSet = this.tm.getTypeMapping(leftHandSide);
		if(lhsTypingSet==null){
			boolean isMainClass = enclosingClassName.equals(Config.MAINCLASS);
			lhsTypingSet = this.tm.initTypeMapping(isMainClass,leftHandSide, false, true);
			this.tm.putTypeMapping(leftHandSide, lhsTypingSet);
		}
		
		if(newExprSet!=null){
			lhsTypingSet.retainAll(newExprSet);
			newExprSet.retainAll(lhsTypingSet);
			this.tm.putTypeMapping(leftHandSide, lhsTypingSet);
			this.tm.putTypeMapping(newExprVar, newExprSet);
		}
		
		// Finding the qualifiers for the constructor parameters
		List<Set<OType>> methodParsListTyping = new ArrayList<Set<OType>>();
		IMethodBinding instrIMethodBinding = instr.resolveBinding();
		List<Variable> parametersVarList = new ArrayList<Variable>();
		IMethod javaElement = (IMethod) instrIMethodBinding.getJavaElement();
		boolean isFromSource = instrIMethodBinding.getDeclaringClass().isFromSource();
		if (isFromSource) {
			// TODO: TOEBI: getting NPE here due to javaElement == null
			Utils.extractParametersFromSource(parametersVarList, javaElement,this.tm);
		}
		else {
			methodParsListTyping = Utils.libraryMethodParams(instrIMethodBinding);
		}
		
		Set<OType> retianedLHSSet = null;
		List<Variable> argOperands = instr.getArgOperands();
		int parIndex = 0;
		for (Variable arg : argOperands) {
			if(!arg.resolveType().isPrimitive()){
				if(!arg.resolveType().isNullType()){
					//To handle if the field is an inherited field
					String argEnclosingClass = value.getFieldEnclosingType(arg);
					Set<OType> argTypingSet = this.tm.getTypeMapping(arg);
					if(argTypingSet==null){
						if(arg instanceof ThisVariable){
							argTypingSet = this.tm.initThisVariable();
							this.tm.putTypeMapping(arg, argTypingSet);
						}
						else{
							boolean isMainClass = argEnclosingClass.equals(Config.MAINCLASS);
							argTypingSet = this.tm.initTypeMapping(isMainClass,arg, true, true);
							this.tm.putTypeMapping(arg, argTypingSet);
						}
					}
				}
				else{
					if(isFromSource){
						parametersVarList.remove(parIndex);
					}
					else{
						methodParsListTyping.remove(parIndex);
					}
				}
				parIndex++;
			}
		}

		// XXX. After getting rid of Set<OType>, make it a simple assignment
		Set<OType> lhsTypings = lhsTypingSet;
		if(parametersVarList!=null){
			for (Variable param : parametersVarList) {
				varSet.add(param);
				Set<OType> paramAUTypings = new SetOType<OType>(this.tm.getTypeMapping(param));
				methodParsListTyping.add(paramAUTypings);
			}
		}
		List<Set<OType>> methodArgsListTypings = new ArrayList<Set<OType>>();
		for (Variable arg : argOperands) {
			if(!arg.resolveType().isPrimitive() && !arg.resolveType().isNullType()){
				varSet.add(arg);
				Set<OType> argAUTypings = new SetOType<OType>(this.tm.getTypeMapping(arg));
				methodArgsListTypings.add(argAUTypings);
			}
		}

			
		if(DISABLE_CREATION_IN_PD){
			TypeConstraints.tNewNoOthersPublicDomain(lhsTypings);
		}
		int i = 0;
		if(parametersVarList!=null && parametersVarList.size()>0 && methodArgsListTypings.size()==methodParsListTyping.size() && lhsTypings!=null){
			List<Set<OType>> sLhsList = new ArrayList<Set<OType>>(); 
			for (Set<OType> paramAUTypeMappings : methodParsListTyping) {

				// constructor parameter of a library class 
				// HACK: Check for non-primitive type
				// XXX. What's with "contains("."))???
				if(paramAUTypeMappings == null && parametersVarList.get(i).resolveType().getQualifiedName().contains(".") ){
					ITypeBinding paramType = parametersVarList.get(i).resolveType();
					boolean isTypeVariable = instrIMethodBinding.getDeclaringClass().isParameterizedType();
					paramAUTypeMappings = Utils.createLibraryTypingSet(paramType,isTypeVariable);
					this.tm.putTypeMapping(parametersVarList.get(i), paramAUTypeMappings);
				}
				
				ITypeBinding[] parameterTypes = instrIMethodBinding.getParameterTypes();

				paramAUTypeMappings = substThisWithThat(paramAUTypeMappings);
				// lhsTypings == recv
				// szi == Q1o
				Set<OType> szi = Adaptation.adaptOutSet(paramAUTypeMappings, lhsTypings, leftHandSide, parameterTypes[i]);
				// Warn if expecting final but it's not
				checkFinalInner(parametersVarList.get(i),leftHandSide);
				
				// check lent validity
				TypeConstraints.checkLentValidity(paramAUTypeMappings, szi);
				
				Set<OType> methodArgumentSet = new SetOType<OType>(methodArgsListTypings.get(i)); 
				createChangingQualifiers(lhsName, methodArgsListTypings.get(i), szi, methodArgumentSet);
				
				// szi becomes Qz
				szi.retainAll(methodArgumentSet);

				// spi == Q1i
				Set<OType> spi = Adaptation.adaptInSet(szi, lhsTypings, leftHandSide, parameterTypes[i]);
				
				// spi == Qxm (formal)
				spi.retainAll(paramAUTypeMappings);

				// slhs == Q1r
				Set<OType> slhs = Adaptation.adaptRcvSet(szi, spi, leftHandSide, this.tm, parameterTypes[i]);
				sLhsList.add(slhs);

				// paramAUTypeMappings.retainAll(methodArgsListTypings.get(i));
				// methodArgsListTypings.get(i).retainAll(paramAUTypeMappings);
				if(isFromSource){
					this.tm.putTypeMapping(parametersVarList.get(i), spi);
				}
				this.tm.putTypeMapping(argOperands.get(i), szi);
			
				// Empty set => Discard TM
				if(spi.size()==0 || szi.size()==0){
					emptySetAction(instr, "Adaptation failed for the object creation: ",varSet);
					return value;
				}
				i++;
			}
			retianedLHSSet = sLhsList.get(0);
			for (Set<OType> typingSet : sLhsList) {
				retianedLHSSet.retainAll(typingSet);
			}
			retianedLHSSet.retainAll(lhsTypings);
			
			this.tm.putTypeMapping(leftHandSide, retianedLHSSet);
			
			// Empty set => Discard TM
			if(retianedLHSSet.size()==0){
				emptySetAction(instr, "Adaptation failed for the object creation: ",varSet);
				return value;
			}
		}
		
		return value;
	}

	@Override
	public OOGContext transfer(edu.cmu.cs.crystal.tac.model.ReturnInstruction instr, OOGContext value) {
		super.transfer(instr, value);
		
		//Set of variables of the instruction in case of a conflict
		Set<Variable> varSet = new HashSet<Variable>();

		// Skip over the ones you do not to re-analyze...
		if(skipExprs.contains(instr)) {
			return super.transfer(instr, value);
		}
		
		boolean isPossible = true;
		ASTNode node = instr.getNode();
		String enclosingClassName = Utils.findEnclosingClassName(node);
		
		//Finding the enclosing method this instruction
		node = instr.getNode();
		while(!(node instanceof MethodDeclaration)){
			node=node.getParent();
		}
		MethodDeclaration methDecl = (MethodDeclaration)node;
		
		// Finding returned variable
		Variable returnedVariable = instr.getReturnedVariable();
		varSet.add(returnedVariable);
		ITypeBinding returnVariableType = returnedVariable.resolveType();
		Set<OType> returnedVariableTypeMapping = null;
		if(returnVariableType.isPrimitive() || returnVariableType.isNullType()){
			isPossible = false;
		}
		else{
			returnedVariableTypeMapping = this.tm.getTypeMapping(returnedVariable);
			if(returnedVariableTypeMapping==null){
				boolean isMainClass = enclosingClassName.equals(Config.MAINCLASS);
				returnedVariableTypeMapping = this.tm.initTypeMapping(isMainClass,returnedVariable, false, true);
				this.tm.putTypeMapping(returnedVariable, returnedVariableTypeMapping);
			}
		}
		
		//Finding AU of the method that returns the returned variable
		IMethodBinding methodBinding = methDecl.resolveBinding();
		Variable methodVar = null;
		if(!methodBinding.getReturnType().isPrimitive()){
			methodVar = this.tm.getVarBindingMap(methodBinding);
		}
		
		Set<OType> methodTypeMapping = null;
		if(methodVar!=null){
			varSet.add(methodVar);
			methodTypeMapping = this.tm.getTypeMapping(methodVar);
		}
		
		if(methodTypeMapping!=null && returnedVariableTypeMapping!=null && isPossible){
			// Do the intersection between set of typings of methodAU and returned variable
			returnedVariableTypeMapping.retainAll(methodTypeMapping);
			methodTypeMapping.retainAll(returnedVariableTypeMapping);

			this.tm.putTypeMapping(returnedVariable, returnedVariableTypeMapping);
			this.tm.putTypeMapping(methodVar, methodTypeMapping);

			updateOverriddenMethodMapping(methodBinding,methodTypeMapping,value);
			
			if(returnedVariableTypeMapping.size()==0 || methodTypeMapping.size()==0){
				emptySetAction(instr, "Adaptation failed for the method return: ",varSet);
				return value;
			}
		}
		

		return value;
	}

	private Set<OType> substThisWithThat(Set<OType> typingSet) {
		Set<OType> substitutedSet = new SetOType<OType>();
		for (OType oType : typingSet) {
			if(oType.getInner()!=null){
				OType substitutedType = new OType(ThatThisSubst.substThisWithThat(oType.getOwner()),ThatThisSubst.substThisWithThat(oType.getAlpha()),ThatThisSubst.substThisWithThat(oType.getInner()));
				substitutedSet.add(substitutedType);
			}
			else{
				OType substitutedType = new OType(ThatThisSubst.substThisWithThat(oType.getOwner()),ThatThisSubst.substThisWithThat(oType.getAlpha()));
				substitutedSet.add(substitutedType);
			}
			
		}
		return substitutedSet;
	}
	
	private Set<OType> substThatWithReceiver(Set<OType> typingSet, String rcvName) {
		Set<OType> substitutedSet = new SetOType<OType>();
		for (OType oType : typingSet) {
			if(oType.getInner()!=null){
				OType substitutedType = new OType(ThatThisSubst.substThatWithRec(oType.getOwner(),rcvName),ThatThisSubst.substThatWithRec(oType.getAlpha(),rcvName), ThatThisSubst.substThatWithRec(oType.getInner(),rcvName));
				substitutedSet.add(substitutedType);
			}
			else{
				OType substitutedType = new OType(ThatThisSubst.substThatWithRec(oType.getOwner(),rcvName),ThatThisSubst.substThatWithRec(oType.getAlpha(),rcvName));
				substitutedSet.add(substitutedType);
			}
		}
		return substitutedSet;
	}
	
	/**
	 * Perform generic type substitution...
	 * Currently, just uses string substitution.
	 * Could be problematic (e.g., if it constructs invalid types).
	 * 
	 * @return
	 */
	public static String typeFtoA(ITypeBinding typeBinding) {
		String formalType = "";
		if(typeBinding.isParameterizedType()){
			ITypeBinding genericTypeBinding = typeBinding.getTypeDeclaration();
			ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
			ITypeBinding[] typeParameters = genericTypeBinding.getTypeParameters();
			formalType = typeBinding.getQualifiedName();
			int i =0;
			for (ITypeBinding iTypeBinding : typeParameters) {
				String typeParameterName = iTypeBinding.getQualifiedName();
				String typeArgumentName = typeArguments[i].getQualifiedName();
				formalType = formalType.replace(typeArgumentName, typeParameterName);
			}
		}
		else{
			formalType = typeBinding.getQualifiedName();
		}
		return formalType;
	}
	
	/**
	 * Does the actions when the TFs hit empty set
	 * @param instr: The instruction that is being analyzed when the TFs hit empty set
	 */

	private void emptySetAction(TACInstruction instr, String errMsg, Set<Variable> insrtVaribleSet) {
		tm.isDiscarded = true;
		StringBuilder reason = new StringBuilder();
		reason.append(errMsg);
		reason.append(instr);
		reason.append(" do not have compatible typings");
		tm.discardReason = reason.toString();
		tm.discardExpression = instr.getNode().toString();
		
		if(isTypeCheching){
			throw new MoreInformationNeededException(reason.toString(), insrtVaribleSet, instr.getNode().toString(),suggestedRefinements(instr));
		}
	}

	private ArrayList<String> suggestedRefinements(TACInstruction instr) {

		ArrayList<String> suggestedRefinements = new ArrayList<String>();
		
		ASTNode node = instr.getNode();
		String enclosingClassName = Utils.findEnclosingClassName(node);
		
		if(instr instanceof LoadFieldInstruction){
			LoadFieldInstruction fieldReadInstr = (LoadFieldInstruction)instr;
			
			Variable receiver = fieldReadInstr.getSourceObject();
			String receiverType = receiver.resolveType().getQualifiedName();
			
			IVariableBinding instrFieldBinding = fieldReadInstr.resolveFieldBinding();
			Variable fieldVariable = this.tm.getVarBindingMap(instrFieldBinding);
			if(fieldVariable!=null && fieldVariable instanceof TACVariable){
				TACVariable field = (TACVariable)fieldVariable;
				IVariableBinding fieldBinding = field.varDecl;
				String fieldType = fieldBinding.getType().getQualifiedName();
				String suggestedRef = createSuggestedRefinement("field",field.getSourceString(),fieldType, receiverType, "PD");
				suggestedRefinements.add(suggestedRef);
				if(receiver instanceof ThisVariable){
					suggestedRef = createSuggestedRefinement("field",field.getSourceString(),fieldType, receiverType, "owned");
					suggestedRefinements.add(suggestedRef);
				}
			}
			if(!(receiver instanceof ThisVariable)){
				String suggestedRef = createSuggestedRefinement("receiver",receiver.getSourceString(),receiverType, enclosingClassName, "owned");
				suggestedRefinements.add(suggestedRef);
				suggestedRef = createSuggestedRefinement("receiver",receiver.getSourceString(),receiverType, enclosingClassName, "PD");
				suggestedRefinements.add(suggestedRef);
				suggestedRef = createSuggestedRefinement("receiver",receiver.getSourceString(),receiverType, "Main", "shared");
				suggestedRefinements.add(suggestedRef);
			}

		}
		else if(instr instanceof StoreFieldInstruction){
			StoreFieldInstruction fieldWriteInsrt = (StoreFieldInstruction)instr;
			
			Variable receiver = fieldWriteInsrt.getAccessedObjectOperand();
			String receiverType = receiver.resolveType().getQualifiedName();
			
			IVariableBinding resolveFieldBinding = fieldWriteInsrt.resolveFieldBinding();
			Variable fieldVariable = this.tm.getVarBindingMap(resolveFieldBinding);
			if(fieldVariable!=null && fieldVariable instanceof TACVariable){
				TACVariable field = (TACVariable)fieldVariable;
				IVariableBinding fieldBinding = field.varDecl;
				String fieldType = fieldBinding.getType().getQualifiedName();
				String suggestedRef = createSuggestedRefinement("field",field.getSourceString(),fieldType, receiverType, "PD");
				suggestedRefinements.add(suggestedRef);
				if(receiver instanceof ThisVariable){
					suggestedRef = createSuggestedRefinement("field",field.getSourceString(),fieldType, receiverType, "owned");
					suggestedRefinements.add(suggestedRef);
				}
			}
			if(!(receiver instanceof ThisVariable)){
				String suggestedRef = createSuggestedRefinement("receiver",receiver.getSourceString(),receiverType, enclosingClassName, "owned");
				suggestedRefinements.add(suggestedRef);
				suggestedRef = createSuggestedRefinement("receiver",receiver.getSourceString(),receiverType, enclosingClassName, "PD");
				suggestedRefinements.add(suggestedRef);
				suggestedRef = createSuggestedRefinement("receiver",receiver.getSourceString(),receiverType, "Main", "shared");
				suggestedRefinements.add(suggestedRef);
			}
			
			Variable rightHandSide = fieldWriteInsrt.getSourceOperand();
			if(!(rightHandSide instanceof TempVariable)){
				String rhsType = rightHandSide.resolveType().getQualifiedName();
				String suggestedRef = createSuggestedRefinement("right hand side",rightHandSide.getSourceString(),rhsType, enclosingClassName, "owned");
				suggestedRefinements.add(suggestedRef);
				suggestedRef = createSuggestedRefinement("right hand side",rightHandSide.getSourceString(),rhsType, enclosingClassName, "PD");
				suggestedRefinements.add(suggestedRef);
			}
			
		}
		else if(instr instanceof MethodCallInstruction){
			MethodCallInstruction methInvkInstr = (MethodCallInstruction)instr;
			
			IMethodBinding rBinding = methInvkInstr.resolveBinding();
			Variable methodVar = this.tm.getVarBindingMap(rBinding);
			
			Variable receiver = methInvkInstr.getReceiverOperand();
			String receiverType = receiver.resolveType().getQualifiedName();
			if(!(receiver instanceof ThisVariable)){
				String suggestedRef = createSuggestedRefinement("receiver", receiver.getSourceString(),receiverType, enclosingClassName, "owned");
				suggestedRefinements.add(suggestedRef);
				suggestedRef = createSuggestedRefinement("receiver", receiver.getSourceString(),receiverType, enclosingClassName, "PD");
				suggestedRefinements.add(suggestedRef);
				suggestedRef = createSuggestedRefinement("receiver",receiver.getSourceString(),receiverType, "Main", "shared");
				suggestedRefinements.add(suggestedRef);
				if(methodVar!=null && methodVar instanceof TACMethod){
					TACMethod method = (TACMethod)methodVar;
					String methodType = method.methDecl.getReturnType().getQualifiedName();
					suggestedRef = createSuggestedRefinement("method", method.getSourceString(),methodType, receiverType, "PD");
					suggestedRefinements.add(suggestedRef);
					
				}
			}
			else{
				if(methodVar!=null && methodVar instanceof TACMethod){
					TACMethod method = (TACMethod)methodVar;
					String methodType = method.methDecl.getReturnType().getQualifiedName();
					String suggestedRef = createSuggestedRefinement("method", method.getSourceString(),methodType, receiverType, "owned");
					suggestedRefinements.add(suggestedRef);
					suggestedRef = createSuggestedRefinement("method", method.getSourceString(),methodType, receiverType, "PD");
					suggestedRefinements.add(suggestedRef);
				}
			}
			
			List<Variable> argOperands = methInvkInstr.getArgOperands();
			for (Variable arg : argOperands) {
				if(!arg.resolveType().isPrimitive()){
					String argType = arg.resolveType().getQualifiedName();
					String suggestedRef = createSuggestedRefinement("method argument",arg.getSourceString(),argType, enclosingClassName, "PD");
					suggestedRefinements.add(suggestedRef);
					suggestedRef = createSuggestedRefinement("method argument",arg.getSourceString(),argType, enclosingClassName, "owner");
					suggestedRefinements.add(suggestedRef);
					suggestedRef = createSuggestedRefinement("method argument",arg.getSourceString(),argType, "Main", "shared");
					suggestedRefinements.add(suggestedRef);
					if(receiver instanceof ThisVariable){
						suggestedRef = createSuggestedRefinement("method argument",arg.getSourceString(),argType, enclosingClassName, "owned");
						suggestedRefinements.add(suggestedRef);
					}
				}
			}
			
			List<Variable> parametersVarList = new ArrayList<Variable>();
			IMethod javaElement = (IMethod) rBinding.getJavaElement();
			boolean isFromSource = rBinding.getDeclaringClass().isFromSource();
			if(isFromSource){
				Utils.extractParametersFromSource(parametersVarList, javaElement, this.tm);
			}
			for (Variable par : parametersVarList) {
				String parType = par.resolveType().getQualifiedName();
				String suggestedRef = createSuggestedRefinement("method parameter",par.getSourceString(),parType, enclosingClassName, "PD");
				suggestedRefinements.add(suggestedRef);
				suggestedRef = createSuggestedRefinement("method parameter",par.getSourceString(),parType, enclosingClassName, "owner");
				suggestedRefinements.add(suggestedRef);
				suggestedRef = createSuggestedRefinement("method parameter",par.getSourceString(),parType, enclosingClassName, "owned");
				suggestedRefinements.add(suggestedRef);
			}
		}
		else if(instr instanceof NewObjectInstruction){
			NewObjectInstruction newExpr = (NewObjectInstruction)instr;
			
			Variable leftHandSide = newExpr.getTarget();
			String lhsType = leftHandSide.resolveType().getQualifiedName();
			String suggestedRef = createSuggestedRefinement("left hand side",leftHandSide.getSourceString(),lhsType, enclosingClassName, "PD");
			suggestedRefinements.add(suggestedRef);
			suggestedRef = createSuggestedRefinement("left hand side", leftHandSide.getSourceString(),lhsType, enclosingClassName, "owned");
			suggestedRefinements.add(suggestedRef);
			
			List<Variable> argOperands = newExpr.getArgOperands();
			for (Variable arg : argOperands) {
				ITypeBinding argTypeBinding = arg.resolveType();
				if(!argTypeBinding.isPrimitive()){
					String argType = argTypeBinding.getQualifiedName();
					suggestedRef = createSuggestedRefinement("constructor argument", arg.getSourceString(),argType, enclosingClassName, "PD");
					suggestedRefinements.add(suggestedRef);
					suggestedRef = createSuggestedRefinement("constructor argument", arg.getSourceString(),argType, enclosingClassName, "owned");
					suggestedRefinements.add(suggestedRef);
					suggestedRef = createSuggestedRefinement("constructor argument", arg.getSourceString(),argType, enclosingClassName, "owner");
					suggestedRefinements.add(suggestedRef);
					suggestedRef = createSuggestedRefinement("constructor argument",arg.getSourceString(),argType, "Main", "shared");
					suggestedRefinements.add(suggestedRef);
				}
			}
			
			IMethodBinding instrIMethodBinding = newExpr.resolveBinding();
			List<Variable> parametersVarList = new ArrayList<Variable>();
			IMethod javaElement = (IMethod) instrIMethodBinding.getJavaElement();
			boolean isFromSource = instrIMethodBinding.getDeclaringClass().isFromSource();
			if (isFromSource) {
				Utils.extractParametersFromSource(parametersVarList, javaElement,this.tm);
			}
			for (Variable par : parametersVarList) {
				String parType = par.resolveType().getQualifiedName();
				suggestedRef = createSuggestedRefinement("constructor parameter", par.getSourceString(),parType, enclosingClassName, "PD");
				suggestedRefinements.add(suggestedRef);
				suggestedRef = createSuggestedRefinement("constructor parameter", par.getSourceString(),parType, enclosingClassName, "owner");
				suggestedRefinements.add(suggestedRef);
				suggestedRef = createSuggestedRefinement("constructor parameter", par.getSourceString(),parType, enclosingClassName, "owned");
				suggestedRefinements.add(suggestedRef);
			}
		}
		
		return suggestedRefinements;
	}

	private String createSuggestedRefinement(String varKind, String varName, String sourceType,
			String destinationType, String domain) {
		StringBuilder refinement = new StringBuilder();
		refinement.append(varKind);
		refinement.append(": ");
		refinement.append(varName);
		refinement.append(": ");
		refinement.append(sourceType);
		refinement.append(" > ");
		refinement.append(destinationType);
		refinement.append(".");
		refinement.append(domain);
		return refinement.toString();
	}
	/**
	 * A method to check if a set only contains unique qualifiers
	 * @param set
	 * @return
	 */
	private boolean containsOnlyUinque(Set<OType> set){
		if(set.isEmpty()){
			return false;
		}
		for (OType oType : set) {
			if(!oType.getOwner().equals("unique")){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * A method to check if a set only contains n.PD qualifiers
	 * @param set
	 * @return
	 */
	private boolean containsOnlyNPD(Set<OType> set){
		if(set.isEmpty()){
			return false;
		}
		for (OType oType : set) {
			if(!(oType.getOwner().contains(".PD") && !oType.getOwner().contains("this.PD"))){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Synchronizing set of unique qualifiers and n.PD qualifiers
	 * n.PD set is the result of adaptation
	 * unique set is a set for the variable that cannot be n.PD e.g. left hand side of a new expression
	 * This method works like an adaptation functions -> unique can flow to n.PD
	 * @param uSet
	 * @param nPDSet
	 */
	private void syncUniqueAndNPD(Set<OType> uSet, Set<OType> nPDSet){
		uSet.clear();
		for (OType oType : nPDSet) {
			OType uOType = new OType("unique",oType.getAlpha());
			uSet.add(uOType);
		}
	}
}

