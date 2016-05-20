package edu.wayne.metrics.qual;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import edu.wayne.metrics.adb.Util;
import ast.Type;
import ast.TypeInfo;

public class ClassifyUtlis extends Classify_Base {

	// Concrete class for all implementations of the classification with
	// annotations and also without annotations
	// Pick all the ITypebindings and Types from outliers or the adapter and
	// classify them into
	/*
	 * Which_A - Container of a general type, Field of a general type, Container of a type, Data types, Exceptions,
	 * Fields of a application framework type Which_A_In_Which_B - Container of a general type, Field of a general type,
	 * Container of a type, Data types, Fields of a final type Which_A_In_Which_B - Container of a general type, Field
	 * of a general type, Container of a type, Data types, Exceptions, Fields of a application framework type SO - Field
	 * of a general type, Fields of a application framework type, Exceptions, Data types PTEP - Field of a general type
	 */

	/*
	 * To classify , we use the helper methods from QualUtils.
	 */
	QualUtils utils = QualUtils.getInstance();

	TypeInfo typeInfo = TypeInfo.getInstance();

	// Private variables for the number of classification - All the metrics use
	// the same variables

	private int numFieldOfGeneralTypes = 0;

	private int numContainerOfType = 0;

	private int numContainerOfInterfaces = 0;

	private int numofAppFrameworkTypes = 0;

	private int numofAppTypes = 0;

	private int numofFrameworkTypes = 0;

	private int numExceptionType = 0;

	private int numFieldOfFinal = 0;

	private int numDataType = 0;

	private int unknown = 0;

	// The number of classifications for TMO
	private int numDefaultDefaultGT;

	private int numDefaultDefaultF1DefaultF2GT;

	private int numDefaultDefault;

	private int numDefaultappFramewk1;

	private int numDefaultF1DefaultF2GT;

	private int numappFramewk1appFramewk2;

	ITypeBinding enclosingTypeVar;

	// Getters and setters for the private number of classification
	public int getNumContainerOfInterfaces() {
		return numContainerOfInterfaces;
	}

	public int getNumFieldOfGeneralTypes() {
		return numFieldOfGeneralTypes;
	}

	public int getNumContainerOfType() {
		return numContainerOfType;
	}

	public int getNumFieldOfFinal() {
		return numFieldOfFinal;
	}

	public int getNumExceptionClass() {
		return numExceptionType;
	}

	/*
	 * public int getNumFieldofAppFrameworkTypes() { return numofAppFrameworkTypes; }
	 */

	public int getNumDataClass() {
		return numDataType;
	}

	public int getUnknown() {
		return unknown;
	}

	public int getNumDefaultDefaultGT() {
		return numDefaultDefaultGT;
	}

	public int getNumDefaultDefaultF1DefaultF2GT() {
		return numDefaultDefaultF1DefaultF2GT;
	}

	public int getNumDefaultDefault() {
		return numDefaultDefault;
	}

	public int getNumDefaultappFramewk1() {
		return numDefaultappFramewk1;
	}

	public int getNumDefaultF1DefaultF2GT() {
		return numDefaultF1DefaultF2GT;
	}

	public int getNumappFramewk1appFramewk2() {
		return numappFramewk1appFramewk2;
	}

	public int getNumofAppFrameworkTypes() {
		return numofAppFrameworkTypes;
	}

	public int getNumofFrameworkTypes() {
		return numofFrameworkTypes;
	}

	public int getNumofAppTypes() {
		return numofAppTypes;
	}

	public void setNumofAppTypes(int numofAppTypes) {
		this.numofAppTypes = numofAppTypes;
	}

	public void setNumofFrameworkTypes(int numofFrameworkTypes) {
		this.numofFrameworkTypes = numofFrameworkTypes;
	}

	public void setNumofAppFrameworkTypes(int numofAppFrameworkTypes) {
		this.numofAppFrameworkTypes = numofAppFrameworkTypes;
	}

	public void setNumFieldOfGeneralTypes(int numFieldOfGeneralTypes) {
		this.numFieldOfGeneralTypes = numFieldOfGeneralTypes;
	}

	public void setNumContainerOfType(int numContainerOfType) {
		this.numContainerOfType = numContainerOfType;
	}

	public void setNumFieldOfFinal(int numFieldOfFinal) {
		this.numFieldOfFinal = numFieldOfFinal;
	}

	public void setNumExceptionClass(int numExceptionClass) {
		this.numExceptionType = numExceptionClass;
	}

	/*
	 * public void setNumFieldofAppFrameworkTypes(int numFieldofAppFrameworkTypes) { this.numofAppFrameworkTypes =
	 * numFieldofAppFrameworkTypes; }
	 */

	public void setNumDataClass(int numDataClass) {
		this.numDataType = numDataClass;
	}

	public void setUnknown(int unknown) {
		this.unknown = unknown;
	}

	public void setNumContainerOfInterfaces(int numContainerOfInterfaces) {
		this.numContainerOfInterfaces = numContainerOfInterfaces;
	}

	public void setNumDefaultDefaultGT(int numDefaultDefaultGT) {
		this.numDefaultDefaultGT = numDefaultDefaultGT;
	}

	public void setNumDefaultDefaultF1DefaultF2GT(int numDefaultDefaultF1DefaultF2GT) {
		this.numDefaultDefaultF1DefaultF2GT = numDefaultDefaultF1DefaultF2GT;
	}

	public void setNumDefaultDefault(int numDefaultDefault) {
		this.numDefaultDefault = numDefaultDefault;
	}

	public void setNumDefaultappFramewk1(int numDefaultappFramewk1) {
		this.numDefaultappFramewk1 = numDefaultappFramewk1;
	}

	public void setNumDefaultF1DefaultF2GT(int numDefaultF1DefaultF2GT) {
		this.numDefaultF1DefaultF2GT = numDefaultF1DefaultF2GT;
	}

	public void setNumappFramewk1appFramewk2(int numappFramewk1appFramewk2) {
		this.numappFramewk1appFramewk2 = numappFramewk1appFramewk2;
	}

	// Implementation of the methods
	@Override
	public void Which_A(Type objType) {

		if (objType != null) {
			Set<String> allSuperclassTypeObj = new HashSet<String>();
			Util.getSuperClasses(objType, allSuperclassTypeObj, true);
			if (utils.isContainerOfGeneralType(objType.getFullyQualifiedName())) {
				numContainerOfInterfaces++;
			}
			else if (utils.isContainerType(objType.getFullyQualifiedName())) {
				numContainerOfType++;
			}
			else if (utils.isFrameworkType(objType.getFullyQualifiedName())) {
				numofAppFrameworkTypes++;
			}
			else if (utils.isapplicationType(objType.getFullyQualifiedName())) {
				numofAppTypes++;
			}
			else if (utils.isJavaTypes(objType.getFullyQualifiedName())) {
				numofFrameworkTypes++;
			}
			else if (utils.isexceptionType(objType.getFullyQualifiedName())) {
				numExceptionType++;
			}
			else {
				unknown++;
			}
		}
	}

	@Override
	public void Which_A_In_B(Type firstObjType, Type secObjType) {

		if (firstObjType != null && secObjType != null) {

			if (utils.isContainerOfGeneralType(firstObjType.getFullyQualifiedName())
			        && utils.isContainerOfGeneralType(secObjType.getFullyQualifiedName())) {
				numContainerOfInterfaces++;
			}
			else if (utils.isContainerType(firstObjType.getFullyQualifiedName())
			        || utils.isContainerType(secObjType.getFullyQualifiedName())) {
				numContainerOfType++;
			}
			else if (utils.isFrameworkType(firstObjType.getFullyQualifiedName())
			        && utils.isFrameworkType(secObjType.getFullyQualifiedName())) {
				numofAppFrameworkTypes++;
			}
			else if (utils.isapplicationType(firstObjType.getFullyQualifiedName())
			        && utils.isapplicationType(secObjType.getFullyQualifiedName())) {
				numofAppTypes++;
			}
			else if (utils.isJavaTypes(firstObjType.getFullyQualifiedName())
			        && utils.isJavaTypes(secObjType.getFullyQualifiedName())) {
				numofFrameworkTypes++;
			}
			else if (utils.isexceptionType(firstObjType.getFullyQualifiedName())
			        && utils.isexceptionType(secObjType.getFullyQualifiedName())) {
				numExceptionType++;
			}
			else {
				unknown++;
			}
		}
	}

	@Override
	public void Which_A_In_Which_B(Type firstObjType, Type secObjType) {

		if (firstObjType != null && secObjType != null) {

			if (utils.isContainerOfGeneralType(firstObjType.getFullyQualifiedName())
			        && utils.isContainerOfGeneralType(secObjType.getFullyQualifiedName())) {
				numContainerOfInterfaces++;
			}
			else if (utils.isContainerType(firstObjType.getFullyQualifiedName())
			        && utils.isContainerType(secObjType.getFullyQualifiedName())) {
				numContainerOfType++;
			}
			else if (utils.isFrameworkType(firstObjType.getFullyQualifiedName())
			        && utils.isFrameworkType(secObjType.getFullyQualifiedName())) {
				numofAppFrameworkTypes++;
			}
			else if (utils.isapplicationType(firstObjType.getFullyQualifiedName())
			        && utils.isapplicationType(secObjType.getFullyQualifiedName())) {
				numofAppTypes++;
			}
			else if (utils.isJavaTypes(firstObjType.getFullyQualifiedName())
			        && utils.isJavaTypes(secObjType.getFullyQualifiedName())) {
				numofFrameworkTypes++;
			}
			else if (utils.isexceptionType(firstObjType.getFullyQualifiedName())
			        && utils.isexceptionType(firstObjType.getFullyQualifiedName())) {
				numExceptionType++;
			}
			else {
				unknown++;
			}
		}

	}

	@Override
	public void SO(Type ObjType) {

		// In cases of systems with annotations get the Types from the IObjects
		if (utils.isexceptionType((ObjType.getFullyQualifiedName()))) {
			numExceptionType++;
		}
		else if (utils.isdataType(ObjType.getFullyQualifiedName())) {
			numDataType++;
		}
		else if (utils.isFrameworkType(ObjType.getFullyQualifiedName())) {
			numofAppFrameworkTypes++;
		}
		else {
			unknown++;
		}

	}

	@Override
	public void HMO(Type objType) {
		if (objType != null && utils.isContainerOfGeneralType(objType.getFullyQualifiedName())) {
			numContainerOfInterfaces++;
		}
		else if (objType != null && utils.isContainerType(objType.getFullyQualifiedName())) {
			numContainerOfType++;
		}
		else if (objType != null && utils.isFrameworkType(objType.getFullyQualifiedName())) {
			numofAppFrameworkTypes++;
		}
		else if (objType != null && utils.isdataType(objType.getFullyQualifiedName())) {
			numDataType++;
		}
		else if (objType != null && utils.isexceptionType(objType.getFullyQualifiedName())) {
			numExceptionType++;
		}
		else {
			unknown++;
		}

	}

	@Override
	public void PTEP(ITypeBinding fieldType) {

		// For systems with annotation, pick the ITypebinding for fields
		// associated with each edge

		if (utils.isFieldOfAtLeastOneGeneralType(fieldType.getQualifiedName())) {
			numFieldOfGeneralTypes++;

		}
		else if (utils.isContainerOfGeneralType(fieldType.getQualifiedName())) {
			numContainerOfInterfaces++;

		}
		else if (utils.isContainerType(fieldType.getQualifiedName())) {
			numContainerOfType++;
		}
		else {
			unknown++;
		}

	}

	@Override
	public void DFEP(Type fieldType) {

		// Pick the type of the field
		if (utils.isFieldOfAtLeastOneGeneralType(fieldType.getFullyQualifiedName())) {
			numFieldOfGeneralTypes++;

		}
		else if (utils.isContainerOfGeneralType(fieldType.getFullyQualifiedName())) {
			numContainerOfInterfaces++;

		}
		else if (utils.isContainerType(fieldType.getFullyQualifiedName())) {
			numContainerOfType++;
		}
		else {
			unknown++;
		}

	}

	@Override
	public void TMO(Type ObjType, Type superType) {

		superType = ObjType.getSuperClass();
		Set<String> allSuperclassTypeA = new HashSet<String>();
		Util.getSuperClasses(ObjType, allSuperclassTypeA, true);
		if (utils.isapplicationType(ObjType.getFullyQualifiedName())) {

			if (superType != null) {
				if (utils.isAbstractType(superType.getFullyQualifiedName())
				        || utils.isInterfaceType(superType.getFullyQualifiedName())) {
					numDefaultDefaultGT++;
				}
				else if (utils.isJavaTypes(superType.getFullyQualifiedName())
				        && !utils.isAbstractType(superType.getFullyQualifiedName())
				        && !utils.isInterfaceType(superType.getFullyQualifiedName())) {
					numDefaultDefaultF1DefaultF2GT++;
				}
				else if (utils.isapplicationType(superType.getFullyQualifiedName())) {
					numDefaultDefault++;
				}
				else if (utils.isexceptionType(superType.getFullyQualifiedName())
				        || allSuperclassTypeA.contains(EXCEPTIONS)) {
					numExceptionType++;
				}

				else if (utils.isFrameworkType(superType.getFullyQualifiedName())
				        && !utils.isAbstractType(superType.getFullyQualifiedName())
				        && !utils.isInterfaceType(superType.getFullyQualifiedName())) {
					if (!utils.isAbstractType(superType.getFullyQualifiedName())
					        || !utils.isInterfaceType(superType.getFullyQualifiedName())) {
						numDefaultappFramewk1++;
					}
				}
			}

		}
		else if (utils.isFrameworkType(ObjType.getFullyQualifiedName())) {
			if (superType != null) {
				if (utils.isJavaTypes(superType.getFullyQualifiedName())) {
					numDefaultF1DefaultF2GT++;
				}
				if (utils.isFrameworkType(superType.getFullyQualifiedName())) {
					numappFramewk1appFramewk2++;
				}
				if (utils.isexceptionType(superType.getFullyQualifiedName()) || allSuperclassTypeA.contains(EXCEPTIONS)) {
					numExceptionType++;
				}
			}
		}// Supporting custom exception
		else if (utils.isexceptionType(ObjType.getFullyQualifiedName()) || allSuperclassTypeA.contains(EXCEPTIONS)) {
			numExceptionType++;

		}
		else {
			unknown++;
		}

	}

	// The methods that are used by NoAnnotatMetric - I need to implement them but later

	@Override
	public void noAnnotateVisitor(ITypeBinding objType) {
		if (objType != null) {
			if (utils.isContainerOfGeneralType(objType.getQualifiedName())) {
				numContainerOfInterfaces++;
			}
			else if (utils.isContainerType(objType.getQualifiedName())) {
				numContainerOfType++;
			}
			else if (utils.isFieldOfAtLeastOneGeneralType(objType.getQualifiedName())) {
				numFieldOfGeneralTypes++;
			}
			else if (utils.isFrameworkType(objType.getQualifiedName())) {
				numofAppFrameworkTypes++;
			}
			else if (utils.isapplicationType(objType.getQualifiedName())) {
				numofAppTypes++;
			}
			else if (utils.isJavaTypes(objType.getQualifiedName())) {
				numofFrameworkTypes++;
			}
			else if (Modifier.isFinal(objType.getModifiers())) {
				numFieldOfFinal++;
			}
			else if (utils.isdataType(objType.getQualifiedName())) {
				numDataType++;
			}
			else if (utils.isexceptionType(objType.getQualifiedName())) {
				numExceptionType++;
			}
			else {
				unknown++;
			}
		}
	}

	// Overridden methods from Q_Base
	@Override
	public void display() throws IOException {
	}

	@Override
	public void visit() {
	}

}
