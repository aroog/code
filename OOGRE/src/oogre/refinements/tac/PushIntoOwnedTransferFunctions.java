
package oogre.refinements.tac;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.internal.WorkspaceUtilities;
import edu.cmu.cs.crystal.simple.AbstractingTransferFunction;
import edu.cmu.cs.crystal.tac.model.CastInstruction;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariable;
import edu.cmu.cs.crystal.tac.model.SourceVariableDeclaration;
import edu.cmu.cs.crystal.tac.model.SourceVariableReadInstruction;
import edu.cmu.cs.crystal.tac.model.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.model.TACInstruction;
import edu.cmu.cs.crystal.tac.model.TempVariable;
import edu.cmu.cs.crystal.tac.model.ThisVariable;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.cmu.cs.crystal.util.Option;

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
 */
public class PushIntoOwnedTransferFunctions extends AbstractingTransferFunction<OOGContext> {
	
	private RefinementAnalysis analysis;
	
	private TM tm;
	
	private IOperation opr;
	
	private static final boolean DISABLE_CREATION_IN_PD = false;
	private static final boolean ENABLE_THIS_RECEIVER_FOR_OWNED_METHOD = true;
	private static final boolean ENABLE_PASSING_OWNED_TO_PUBLIC_METHOD = false;
	private static final boolean ENABLE_REMOVE_FIELD_OWNED_RECEIVER_THIS = true;
	
	
	
	
	// XXX. opr can be null, during post-processing
	public PushIntoOwnedTransferFunctions(RefinementAnalysis analysis, TM tm, IOperation opr) {
		this.analysis = analysis;
		this.tm = tm;
		this.opr = opr;
	}

	// XXX. parameter should become an ITypeBinding
	private Set<OType> createLibraryTypingSet(String type) {
		Set<OType> libraryTypingSet = new HashSet<OType>();

		OType oType = null;
		// Handle generic/parameterized types
		if (type.contains("<")) {
			oType = new OType("owner", "owner", "p");
			libraryTypingSet.add(oType);
			oType = new OType("owner", "p", "p");
			libraryTypingSet.add(oType);
			oType = new OType("p", "p", "p");
			libraryTypingSet.add(oType);
		}
		// Special case: String
		else if (type.equals("java.lang.String") ) {
			oType = new OType("shared","shared");
			libraryTypingSet.add(oType);
		}
		else {
			oType = new OType("owner","p");
			libraryTypingSet.add(oType);
			oType = new OType("p","p");
			libraryTypingSet.add(oType);
		}
		return libraryTypingSet;
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
		
		ASTNode node = instr.getNode();
		String encClassName = findEnclosingClassName(node);
		boolean isMainClass = encClassName.equals(Config.MAINCLASS);
		String currentReceiverName = value.getCurrentReceiverName();
		
		Variable leftHandSide = instr.getTarget();
		Set<OType> leftHandSideOType = this.tm.getTypeMapping(leftHandSide);
		if(leftHandSideOType == null){
			leftHandSideOType = this.tm.initTypeMapping(isMainClass,leftHandSide);
			this.tm.putTypeMapping(leftHandSide, leftHandSideOType);
		}
		Set<OType> newleftHandSideOType = new HashSet<OType>(leftHandSideOType);
		
		Variable rightHandSide = instr.getOperand();
		Set<OType> rightHandSideOType = this.tm.getTypeMapping(rightHandSide);
		if(rightHandSideOType == null){
			rightHandSideOType = this.tm.initTypeMapping(isMainClass,rightHandSide);
			this.tm.putTypeMapping(rightHandSide, rightHandSideOType);
		}
		else{
			if(!currentReceiverName.equals("new")){
				createChangingQualifiers(currentReceiverName, leftHandSideOType, rightHandSideOType, newleftHandSideOType);
			}
			// if the right hand side of the copy construction is a new expression
			else{
				rightHandSideOType.addAll(newleftHandSideOType);
			}
		}

		newleftHandSideOType.retainAll(rightHandSideOType);
		this.tm.putTypeMapping(leftHandSide, newleftHandSideOType);
		this.tm.putTypeMapping(rightHandSide, newleftHandSideOType);
		
		if(newleftHandSideOType.size()==0){
			emptySetAction(instr, "The left hand side and right hand side of the assignment statement: ");
			return value;
		}

		return value;
    }

	@Override
	public OOGContext transfer(CastInstruction instr, OOGContext value) {
		
		//Finding the enclosing method of this instruction
		ASTNode node = instr.getNode();
		String encClassName = findEnclosingClassName(node);
		boolean isMainClass = encClassName.equals(Config.MAINCLASS);
		
		Variable operand = instr.getOperand();
		Set<OType> operandOType = this.tm.getTypeMapping(operand);
		if(operandOType == null){
			operandOType = this.tm.initTypeMapping(isMainClass,operand);
			this.tm.putTypeMapping(operand, operandOType);
		}
		
		Variable target = instr.getTarget();
		Set<OType> targetOType = this.tm.getTypeMapping(target);
		if(targetOType == null){
			targetOType = this.tm.initTypeMapping(isMainClass,target);
			this.tm.putTypeMapping(target, targetOType);
		}
		
		operandOType.retainAll(targetOType);
		this.tm.putTypeMapping(operand, operandOType);
		this.tm.putTypeMapping(target, operandOType);
		if(operandOType.size()==0){
			emptySetAction(instr, "The left hand side and right hand side of Cast : ");
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
	    boolean isPossible = true; // Is Adaptation needed, i.e., receiver is not 'this'? 
	    
	    IMethodBinding rBinding = instr.resolveBinding();
	    
	    ASTNode node = instr.getNode();
		String encClassName = findEnclosingClassName(node);
		
		value.addACalledMethod(rBinding);
		
		Variable receiver = instr.getReceiverOperand();
		Set<OType> receiverAUTypings = null;
		if(receiver instanceof ThisVariable){
			isPossible = false;
		}
		else{
			receiverAUTypings = this.tm.getTypeMapping(receiver);
			if(receiverAUTypings==null){
				boolean isMainClass = encClassName.equals(Config.MAINCLASS);
				receiverAUTypings = this.tm.initTypeMapping(isMainClass,receiver);
				this.tm.putTypeMapping(receiver, receiverAUTypings);
			}
		}
		
		List<Variable> argOperands = instr.getArgOperands();
		for (Variable arg : argOperands) {
			if(!arg.resolveType().isPrimitive()){
				//To handle if the field is an inherited field
				String argEnclosingClass = value.getFieldEnclosingType(arg);
				Set<OType> argeTypingSet = this.tm.getTypeMapping(arg);
				if(argeTypingSet==null){
					boolean isMainClass = argEnclosingClass.equals(Config.MAINCLASS);
					argeTypingSet = this.tm.initTypeMapping(isMainClass,arg);
					this.tm.putTypeMapping(arg, argeTypingSet);
				}
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
		
		Set<OType> methodAUTypings = null;
		List<Set<OType>> methodParsListTyping = new ArrayList<Set<OType>>();

		List<Variable> parametersVarList = new ArrayList<Variable>();
		IMethod javaElement = (IMethod) rBinding.getJavaElement();
		boolean isFromSource = rBinding.getDeclaringClass().isFromSource();
		if(isFromSource){
			extractParametersFromSource(parametersVarList, javaElement);
		}
		// Else, this is library code.
		else {
			libraryMethodAndParams(rBinding, methodAUTypings, methodParsListTyping);
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
			
			//To handle if the field is an inherited field
			String lhsEnclosingClass = value.getFieldEnclosingType(leftHandSide);
			
			Set<OType> lhsTypingSet = this.tm.getTypeMapping(leftHandSide);
			if(lhsTypingSet==null){
				boolean isMainClass = lhsEnclosingClass.equals(Config.MAINCLASS);
				lhsTypingSet = this.tm.initTypeMapping(isMainClass,leftHandSide);
				this.tm.putTypeMapping(leftHandSide, lhsTypingSet);
			}
		}
		
		// methodVar == y_m
		// methodAUTypings == S[y_m] 
		if(methodVar!=null){
			methodAUTypings = this.tm.getTypeMapping(methodVar);
		}
		
		Set<OType> leftHandSideTypings = null;
		if(leftHandSide!=null){
			leftHandSideTypings = this.tm.getTypeMapping(leftHandSide);
		}
		
		if(parametersVarList!=null){
			for (Variable param : parametersVarList) {
				Set<OType> paramAUTypings = new HashSet<OType>(this.tm.getTypeMapping(param));
				methodParsListTyping.add(paramAUTypings);
			}
		}
		List<Set<OType>> methodArgsListTyping = new ArrayList<Set<OType>>();
		for (Variable arg : argOperands) {
			if(!arg.resolveType().isPrimitive()){
				Set<OType> argAUTypings = new HashSet<OType>(this.tm.getTypeMapping(arg));
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
		
		// Adaptation is needed; receiver is not this
		if(isPossible){
			int i=0;
			if(methodParsListTyping.size()>0 && methodArgsListTyping.size()==methodParsListTyping.size()){
				for (Set<OType> parTypingSet : methodParsListTyping) {
					
					// TOEBI: We need this constraint. If receiver is not this, cannot pass owned to public method. Breaks private domain.
					
					// Applying type system specific constraints
					if(ENABLE_PASSING_OWNED_TO_PUBLIC_METHOD){
						TypeConstraints.tCall_PassingOwnedToPublicMethod(rBinding, methodArgsListTyping.get(i));
					}
					
					// x_m \in parTypingSet
					parTypingSet = substThisWithThat(parTypingSet);
					
					// Adapt-out
					Set<OType> szi = Adaptation.adaptOutSet(parTypingSet, receiverAUTypings, receiver);
					// szi == Q1o
					if(parametersVarList.size()>0){
						checkFinalInner(parametersVarList.get(i),receiver);
					}
					
					Set<OType> methodArgumentSet = new HashSet<OType>(methodArgsListTyping.get(i));
					createChangingQualifiers(receiverName, methodArgsListTyping.get(i), szi, methodArgumentSet);
					// szi == Qz
					szi.retainAll(methodArgumentSet);
					
					// spi == Q1i
					Set<OType> spi = Adaptation.adaptInSet(szi, receiverAUTypings, receiver);
					spi.retainAll(parTypingSet);
					// spi == Qxm
					
					Set<OType> sx = null;
					
					if(!rBinding.getReturnType().isPrimitive()){
						// Updating S[y_m]
						methodAUTypings = substThisWithThat(methodAUTypings);
						
						if(leftHandSideTypings!=null){
							sx = Adaptation.adaptOutSet(methodAUTypings, receiverAUTypings, receiver);
							// sx == Q2o
							checkFinalInner(methodVar,receiver);
							
							Set<OType> newLeftHandSideTypings = new HashSet<OType>(leftHandSideTypings);
							createChangingQualifiers(receiverName, leftHandSideTypings, sx, newLeftHandSideTypings);
							sx.retainAll(newLeftHandSideTypings);
							// sx == Qx
						}
						else{
							sx = Adaptation.adaptOutSet(methodAUTypings, receiverAUTypings, receiver);
							checkFinalInner(methodVar,receiver);
						}
						
						// sm == Q2i
						Set<OType> sm = Adaptation.adaptInSet(sx, receiverAUTypings, receiver);
						sm.retainAll(methodAUTypings);
						// sm == Qym
						
						// sy1 = Q1r
						Set<OType> sy1 = Adaptation.adaptRcvSet(szi,spi, receiver,encClassName);
						
						// sy2 == Q2r
						Set<OType> sy2 = Adaptation.adaptRcvSet(sx, sm, receiver,encClassName);
						
						sy1.retainAll(sy2);
						sy1.retainAll(receiverAUTypings);
						// sy1 = Qy
						
						//Substitute that with this for method the parameter
						spi = substThatWithReceiver(spi, "this");

						//Substitute that with this for the method return
						sm = substThatWithReceiver(sm, "this");
						
						this.tm.putTypeMapping(argOperands.get(i), szi);
						this.tm.putTypeMapping(leftHandSide, sx);
						this.tm.putTypeMapping(receiver, sy1);
						
						
						if(isFromSource){
							this.tm.putTypeMapping(parametersVarList.get(i), spi);
							this.tm.putTypeMapping(methodVar, sm);
						}
						
						if(sy1.size()==0 || sm.size()==0 || sx.size()==0 || spi.size()==0 || szi.size()==0){
							emptySetAction(instr, "Adaptation failed for the method invocation: ");
							return value;
						}
					}
					else{
						Set<OType> sy1 = Adaptation.adaptRcvSet(szi, spi, receiver,encClassName);
						sy1.retainAll(receiverAUTypings);
						
						//Substitute that with receiver for the argument
						szi = substThatWithReceiver(szi, receiverName);
						//Substitute that with this for method the parameter
						spi = substThatWithReceiver(spi, "this");
						
						this.tm.putTypeMapping(argOperands.get(i), szi);
						this.tm.putTypeMapping(receiver, sy1);
						if(isFromSource){
							this.tm.putTypeMapping(parametersVarList.get(i), spi);
						}
						if(sy1.size()==0 || spi.size()==0 || szi.size()==0){
							emptySetAction(instr, "Adaptation failed for the method invocation: ");
							return value;
						}
					}
					i++;
				}
			}
			else{
				// XXX. Get rid of the GD "java.lang"...
				if(!rBinding.getReturnType().isPrimitive() && methodAUTypings!=null){
					Set<OType> sx = null;
					methodAUTypings = substThisWithThat(methodAUTypings);
					if(leftHandSideTypings!=null){
						sx = Adaptation.adaptOutSet(methodAUTypings, receiverAUTypings, receiver);
						//Check if the receiver should be final
						checkFinalInner(methodVar,receiver);
						Set<OType> newLeftHandSideTypings = new HashSet<OType>(leftHandSideTypings);
						createChangingQualifiers(receiverName, leftHandSideTypings, sx, newLeftHandSideTypings);
						sx.retainAll(newLeftHandSideTypings);
					}
					else{
						sx = Adaptation.adaptOutSet(methodAUTypings, receiverAUTypings, receiver);
						checkFinalInner(methodVar,receiver);
					}
					
					
					Set<OType> sm = Adaptation.adaptInSet(sx, receiverAUTypings, receiver);
					sm.retainAll(methodAUTypings);
					
					Set<OType> sy2 = Adaptation.adaptRcvSet(sx, sm, receiver,encClassName);
					sy2.retainAll(receiverAUTypings);
					
					//Substitute that with receiver for left hand side
					sx = substThatWithReceiver(sx, receiverName);
					//Substitute that with this for method return
					sm = substThatWithReceiver(sm, "this");
					
					
					this.tm.putTypeMapping(leftHandSide, sx);
					this.tm.putTypeMapping(receiver, sy2);
					if(isFromSource){
						this.tm.putTypeMapping(methodVar, sm);
					}
					if(sx.size()==0 || sm.size()==0 || sy2.size()==0){
						emptySetAction(instr, "Adaptation failed for the method invocation: ");
						return value;
					}
				}
			}
		}
		else{
			if(receiver instanceof ThisVariable){
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
							}
							if(parTypingSet.size()==0 || argTypingSet.size()==0 || leftHandSideTypings.size()==0 || methodAUTypings.size()==0){
								emptySetAction(instr, "Adaptation failed for the method invocation: ");
								return value;
							}
						}
						else{
							this.tm.putTypeMapping(argOperands.get(i), argTypingSet);
							if(isFromSource){
								this.tm.putTypeMapping(parametersVarList.get(i), parTypingSet);
							}
							if(parTypingSet.size()==0 || argTypingSet.size()==0){
								emptySetAction(instr, "Adaptation failed for the method invocation: ");
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
							}
							if(leftHandSideTypings.size()==0 || methodAUTypings.size()==0){
								emptySetAction(instr, "Adaptation failed for the method invocation: ");
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
						}
						if(leftHandSideTypings.size()==0 || methodAUTypings.size()==0){
							emptySetAction(instr, "Adaptation failed for the method invocation: ");
							return value;
						}
					}
				}
			}
		}
	    return value;
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

	private void libraryMethodAndParams(IMethodBinding rBinding, Set<OType> methodAUTypings, List<Set<OType>> methodParsListTyping) {
		ITypeBinding mehtodReturnType = rBinding.getReturnType();
		if(!mehtodReturnType.isPrimitive() && !Modifier.isStatic(rBinding.getModifiers()) && methodAUTypings!=null){
			methodAUTypings = createLibraryTypingSet(mehtodReturnType.getQualifiedName());
		}
		for (ITypeBinding paramType : rBinding.getParameterTypes()) {
			if(!paramType.isPrimitive()){
				Set<OType> libraryMethodAUTypings = createLibraryTypingSet(paramType.getQualifiedName());
				methodParsListTyping.add(libraryMethodAUTypings);
			}
		}
	}

	private void extractParametersFromSource(List<Variable> parametersVarList,
			IMethod javaElement) {
		Option<MethodDeclaration> mDecl = WorkspaceUtilities.getMethodDeclFromModel(javaElement);
		MethodDeclaration mmDecl = mDecl.unwrap();

		List<SingleVariableDeclaration> parameters = mmDecl.parameters();
		for (SingleVariableDeclaration param : parameters) {
			IVariableBinding paramBinding = param.resolveBinding();
			if(!paramBinding.getType().isPrimitive()){
				Variable paramVariable = this.tm.getVariableFromBindingKey(paramBinding.getKey());
				if(paramVariable!=null){
					parametersVarList.add(paramVariable);
				}
			}
		}
	}

	@Override
    public OOGContext transfer(LoadFieldInstruction instr, OOGContext value) {
	    super.transfer(instr, value);
	    
	    boolean isPossible = true; // Is adaptation needed, i.e., is the receiver not 'this'
	    
	    ASTNode node = instr.getNode();
		String encClassName = findEnclosingClassName(node);
		
		Variable leftHandSide = instr.getTarget();
		Set<OType> lhsTypingSet = this.tm.getTypeMapping(leftHandSide);
		if(lhsTypingSet==null){
			boolean isMainClass = encClassName.equals(Config.MAINCLASS);
			lhsTypingSet = this.tm.initTypeMapping(isMainClass,leftHandSide);
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
			// Strip "this." prefix from the receiver "this.x" in "this.x.f"
			receiverName = receiver.getSourceString();
			if(receiverName.contains("this")){
				receiverName=receiverName.substring(receiverName.indexOf('.')+1);
			}
			
			receiverEnclosingClass = value.getFieldEnclosingType(receiver);
			receiverTypings = this.tm.getTypeMapping(receiver);
			if(receiverTypings==null){
				boolean isMainClass = receiverEnclosingClass.equals(Config.MAINCLASS);
				receiverTypings = this.tm.initTypeMapping(isMainClass,receiver);
				this.tm.putTypeMapping(receiver, receiverTypings);
			}
		}
		
		value.setCurrentReceiverName(receiverName);
	
		Set<OType> fieldTypings = null;
		IVariableBinding instrFieldBinding = instr.resolveFieldBinding();
		
		Variable fieldVariable = this.tm.getVarBindingMap(instrFieldBinding);
		if(fieldVariable!=null){
			fieldTypings = this.tm.getTypeMapping(fieldVariable);
		}
		else{
			isPossible = false;
		}

		// TODO: inline
		Set<OType> leftHandSideTypings = lhsTypingSet;
		
		if(isPossible){
			// fieldTypings == S[f]
			fieldTypings  = substThisWithThat(fieldTypings);
			
			// sSecond == Qo
			Set<OType> sSecond = Adaptation.adaptOutSet(fieldTypings, receiverTypings, receiver);
			
			checkFinalInner(fieldVariable, receiver);
			
			Set<OType> newLeftHandSideTypings = new HashSet<OType>(leftHandSideTypings);
			if(leftHandSideTypings !=null && leftHandSideTypings.size()>0){
				createChangingQualifiers(receiverName, leftHandSideTypings,	sSecond, newLeftHandSideTypings);
				sSecond.retainAll(newLeftHandSideTypings);
			}
			// sSecond == Qy
			
			// sCapital == Qi
			Set<OType> sCapital = Adaptation.adaptInSet(sSecond, receiverTypings, receiver);
			sCapital.retainAll(fieldTypings);
			// sCapital == Qf
			
			// sPrime == Qr
			Set<OType> sPrime = Adaptation.adaptRcvSet(sSecond, sCapital, receiver, receiverEnclosingClass);
			sPrime.retainAll(receiverTypings);
			// sPrime == Qx
			
			//Substitute that with this for field
			sCapital = substThatWithReceiver(sCapital, "this");
			
			this.tm.putTypeMapping(leftHandSide, sSecond);
			this.tm.putTypeMapping(fieldVariable, sCapital);
			this.tm.putTypeMapping(receiver, sPrime);
			
			if(sPrime.size()==0 || sCapital.size()==0 || sSecond.size()==0){
				emptySetAction(instr, "Adaptation failed for the field read: ");
				return value;
			}
		}
		else{
			if(receiver instanceof ThisVariable){
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
						emptySetAction(instr, "Adaptation failed for the field read: ");
						return value;
					}
				}
			}
		}
		return value;
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
			if((containsReceiver(adaptedTypes.getOwner(),receiverName) && !adaptedTypes.getOwner().contains(".any")) || (containsReceiver(adaptedTypes.getAlpha(),receiverName) && !adaptedTypes.getAlpha().contains(".any"))){
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
	    
	    boolean isPossible = true; // Is adaptation needed, i.e., is receiver not 'this'?
	    
	    ASTNode node = instr.getNode();
		String encClassName = findEnclosingClassName(node);
		boolean isMainClass = encClassName.equals(Config.MAINCLASS);
		
		IVariableBinding resolveFieldBinding = instr.resolveFieldBinding();
		Variable fieldVariable = this.tm.getVarBindingMap(resolveFieldBinding);
		Set<OType> fieldTypings = null;
		if(fieldVariable!=null){
			fieldTypings = this.tm.getTypeMapping(fieldVariable);
		}
		else{
			isPossible = false;
		}

		// receiver == x
		Variable receiver = instr.getAccessedObjectOperand();
		String receiverName = "";
		Set<OType> receiverTypings = null;
		if(receiver instanceof ThisVariable){
			isPossible = false;
		}
		else{
			// Strip "this." prefix from the receiver "this.x" in "this.x.f = y"
			receiverName = receiver.getSourceString();
			if(receiverName.contains("this")){
				receiverName=receiverName.substring(receiverName.indexOf('.')+1);
			}
			
			receiverTypings = this.tm.getTypeMapping(receiver);
			if(receiverTypings==null){
				receiverTypings = this.tm.initTypeMapping(isMainClass,receiver);
				this.tm.putTypeMapping(receiver, receiverTypings);
			}
		}
		value.setCurrentReceiverName(receiverName);

		// rightHandSide == y
		Variable rightHandSide = instr.getSourceOperand();
		ITypeBinding rhsType = rightHandSide.resolveType();
		
		Set<OType> rightHandSideTypings = null;
		if(rhsType.isPrimitive() || rhsType.isNullType()){
			isPossible = false;
		}
		else{
			rightHandSideTypings = this.tm.getTypeMapping(rightHandSide);
			if(rightHandSideTypings==null){
				rightHandSideTypings = this.tm.initTypeMapping(isMainClass,rightHandSide);
				this.tm.putTypeMapping(rightHandSide, rightHandSideTypings);
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
				sSecond = Adaptation.adaptOutSet(fieldTypings, receiverTypings, receiver);
				checkFinalInner(fieldVariable, receiver);
				
				Set<OType> newrightHandSideTypings = new HashSet<OType>(rightHandSideTypings);
				createChangingQualifiers(receiverName, rightHandSideTypings, sSecond, newrightHandSideTypings);
				
				sSecond.retainAll(newrightHandSideTypings);
				// sSecond == Qy
			}
			else{
				sSecond = Adaptation.adaptOutSet(fieldTypings, receiverTypings, receiver);
				checkFinalInner(fieldVariable, receiver);
			}
			
			// sCapital == Qi
			Set<OType> sCapital = Adaptation.adaptInSet(sSecond, receiverTypings, receiver);
			sCapital.retainAll(fieldTypings);
			// sCapital == Qf
			
			Set<OType> sPrime = Adaptation.adaptRcvSet(sSecond, sCapital, receiver, encClassName);
			sPrime.retainAll(receiverTypings);
			// sPrime == Qr
			
			//Substitute that with this for field
			sCapital = substThatWithReceiver(sCapital, "this");
			
			
			this.tm.putTypeMapping(rightHandSide, sSecond);
			this.tm.putTypeMapping(fieldVariable, sCapital);
			this.tm.putTypeMapping(receiver, sPrime);
			
			if(sPrime.size()==0 || sCapital.size()==0 || sSecond.size()==0){
				emptySetAction(instr, "Adaptation failed for the field write: ");
				return value;
			}
		}
		else{
			// this.f = y
			if(receiver instanceof ThisVariable){
				if(fieldTypings!=null && fieldTypings.size()>0 && rightHandSideTypings!=null && rightHandSideTypings.size()>0 && !fieldVariable.equals(rightHandSide)){
					fieldTypings.retainAll(rightHandSideTypings);
					rightHandSideTypings.retainAll(fieldTypings);
					
					this.tm.putTypeMapping(fieldVariable, fieldTypings);
					this.tm.putTypeMapping(rightHandSide, rightHandSideTypings);
					
					if(fieldTypings.size()==0 || rightHandSideTypings.size()==0){
						emptySetAction(instr, "Adaptation failed for the field write: ");
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
		
		ASTNode node = instr.getNode();
		String enclosingClassName = findEnclosingClassName(node);
		
		value.setCurrentReceiverName("new");
		
		Variable leftHandSide = instr.getTarget();
		String lhsName = leftHandSide.getSourceString();
		Set<OType> lhsTypingSet = this.tm.getTypeMapping(leftHandSide);
		if(lhsTypingSet==null){
			boolean isMainClass = enclosingClassName.equals(Config.MAINCLASS);
			lhsTypingSet = this.tm.initTypeMapping(isMainClass,leftHandSide);
			this.tm.putTypeMapping(leftHandSide, lhsTypingSet);
		}
		
		Set<OType> retianedLHSSet = null;
		List<Variable> argOperands = instr.getArgOperands();
		for (Variable arg : argOperands) {
			if(!arg.resolveType().isPrimitive()){
				//To handle if the field is an inherited field
				String argEnclosingClass = value.getFieldEnclosingType(arg);
				Set<OType> argTypingSet = this.tm.getTypeMapping(arg);
				if(argTypingSet==null){
					boolean isMainClass = argEnclosingClass.equals(Config.MAINCLASS);
					argTypingSet = this.tm.initTypeMapping(isMainClass,arg);
					this.tm.putTypeMapping(arg, argTypingSet);
				}
			}
		}
		
		// Finding the qualifiers for the constructor parameters
		List<Set<OType>> methodParsListTyping = new ArrayList<Set<OType>>();
		IMethodBinding instrIMethodBinding = instr.resolveBinding();
		List<Variable> parametersVarList = new ArrayList<Variable>();
		IMethod javaElement = (IMethod) instrIMethodBinding.getJavaElement();
		boolean isFromSource = instrIMethodBinding.getDeclaringClass().isFromSource();
		if (isFromSource) {
			extractParametersFromSource(parametersVarList, javaElement);
		}
		else {
			libraryMethodAndParams(instrIMethodBinding, null,methodParsListTyping);
		}

		// XXX. After getting rid of Set<OType>, make it a simple assignment
		Set<OType> lhsTypings = this.tm.getTypeMapping(leftHandSide);
		if(parametersVarList!=null){
			for (Variable param : parametersVarList) {
				Set<OType> paramAUTypings = new HashSet<OType>(this.tm.getTypeMapping(param));
				methodParsListTyping.add(paramAUTypings);
			}
		}
		List<Set<OType>> methodArgsListTypings = new ArrayList<Set<OType>>();
		for (Variable arg : argOperands) {
			if(!arg.resolveType().isPrimitive()){
				Set<OType> argAUTypings = new HashSet<OType>(this.tm.getTypeMapping(arg));
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
					paramAUTypeMappings = createLibraryTypingSet(parametersVarList.get(i).resolveType().getQualifiedName());
					this.tm.putTypeMapping(parametersVarList.get(i), paramAUTypeMappings);
				}

				paramAUTypeMappings = substThisWithThat(paramAUTypeMappings);
				// lhsTypings == recv
				// szi == Q1o
				Set<OType> szi = Adaptation.adaptOutSet(paramAUTypeMappings, lhsTypings, leftHandSide);
				// Warn if expecting final but it's not
				checkFinalInner(parametersVarList.get(i),leftHandSide);
				
				Set<OType> methodArgumentSet = new HashSet<OType>(methodArgsListTypings.get(i)); 
				createChangingQualifiers(lhsName, methodArgsListTypings.get(i), szi, methodArgumentSet);
				
				// szi becomes Qz
				szi.retainAll(methodArgumentSet);

				// spi == Q1i
				Set<OType> spi = Adaptation.adaptInSet(szi, lhsTypings, leftHandSide);
				
				// spi == Qxm (formal)
				spi.retainAll(paramAUTypeMappings);

				// slhs == Q1r
				Set<OType> slhs = Adaptation.adaptRcvSet(szi, spi, leftHandSide, enclosingClassName);
				sLhsList.add(slhs);

				// paramAUTypeMappings.retainAll(methodArgsListTypings.get(i));
				// methodArgsListTypings.get(i).retainAll(paramAUTypeMappings);
				if(isFromSource){
					this.tm.putTypeMapping(parametersVarList.get(i), spi);
				}
				this.tm.putTypeMapping(argOperands.get(i), szi);
			
				// Empty set => Discard TM
				if(spi.size()==0 || szi.size()==0){
					emptySetAction(instr, "Adaptation failed for the object creation: ");
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
				emptySetAction(instr, "Adaptation failed for the object creation: ");
				return value;
			}
		}
		
		return value;
	}

	@Override
	public OOGContext transfer(edu.cmu.cs.crystal.tac.model.ReturnInstruction instr, OOGContext value) {
		super.transfer(instr, value);
		
		ASTNode node = instr.getNode();
		String enclosingClassName = findEnclosingClassName(node);
		
		//Finding the enclosing method this instruction
		node = instr.getNode();
		while(!(node instanceof MethodDeclaration)){
			node=node.getParent();
		}
		MethodDeclaration methDecl = (MethodDeclaration)node;
		
		// Finding returned variable
		Variable returnedVariable = instr.getReturnedVariable();
		
		Set<OType> returnedVariableTypeMapping = this.tm.getTypeMapping(returnedVariable);
		if(returnedVariableTypeMapping==null){
			boolean isMainClass = enclosingClassName.equals(Config.MAINCLASS);
			returnedVariableTypeMapping = this.tm.initTypeMapping(isMainClass,returnedVariable);
			this.tm.putTypeMapping(returnedVariable, returnedVariableTypeMapping);
		}
		
		//Finding AU of the method that returns the returned variable
		IMethodBinding methodBinding = methDecl.resolveBinding();
		Variable methodVar = null;
		if(!methodBinding.getReturnType().isPrimitive()){
			methodVar = this.tm.getVarBindingMap(methodBinding);
		}
		
		Set<OType> methodTypeMapping = null;
		if(methodVar!=null){
			methodTypeMapping = this.tm.getTypeMapping(methodVar);
		}
		
		if(methodTypeMapping!=null && returnedVariableTypeMapping!=null){
			// Do the intersection between set of typings of methodAU and returned variable
			returnedVariableTypeMapping.retainAll(methodTypeMapping);
			methodTypeMapping.retainAll(returnedVariableTypeMapping);
			this.tm.putTypeMapping(returnedVariable, returnedVariableTypeMapping);
			this.tm.putTypeMapping(methodVar, methodTypeMapping);
			if(returnedVariableTypeMapping.size()==0 || methodTypeMapping.size()==0){
				emptySetAction(instr, "Adaptation failed for the method return: ");
				return value;
			}
		}
		

		return value;
	}

	private Set<OType> substThisWithThat(Set<OType> typingSet) {
		Set<OType> substitutedSet = new HashSet<OType>();
		for (OType oType : typingSet) {
			OType substitutedType = new OType(ThatThisSubst.substThisWithThat(oType.getOwner()),ThatThisSubst.substThisWithThat(oType.getAlpha()));
			substitutedSet.add(substitutedType);
		}
		return substitutedSet;
	}
	
	private Set<OType> substThatWithReceiver(Set<OType> typingSet, String rcvName) {
		Set<OType> substitutedSet = new HashSet<OType>();
		for (OType oType : typingSet) {
			OType substitutedType = new OType(ThatThisSubst.substThatWithRec(oType.getOwner(),rcvName),ThatThisSubst.substThatWithRec(oType.getAlpha(),rcvName), ThatThisSubst.substThatWithRec(oType.getInner(),rcvName));
			substitutedSet.add(substitutedType);
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

	private void emptySetAction(TACInstruction instr, String errMsg) {
		tm.isDiscarded = true;
		StringBuilder reason = new StringBuilder();
		reason.append(errMsg);
		reason.append(instr);
		reason.append(" do not have compatible typings");
		tm.discardReason = reason.toString();
		tm.discardExpression = instr.getNode().toString();
	}
	

	/**
	 * 
	 * @param node: ASTNode corresponding to the TAC instruction that is being analyzed
	 * @return The qualified name of the enclosing class of the TAC instruction that is being analyzed
	 */
	private String findEnclosingClassName(ASTNode node) {
		while(!(node instanceof TypeDeclaration)){
			node=node.getParent();
		}
		TypeDeclaration enclosingType = (TypeDeclaration)node;
		ITypeBinding enclosingClass = enclosingType.resolveBinding();
		String encClassName = enclosingClass.getQualifiedName();
		return encClassName;
	}
}

