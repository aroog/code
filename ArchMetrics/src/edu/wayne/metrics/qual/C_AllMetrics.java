package edu.wayne.metrics.qual;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import ast.TypeInfo;
import ast.Type;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.CSVOutputUtils;

public class C_AllMetrics extends Q_Base {

	private Writer writer;

	TypeInfo typeInfo = TypeInfo.getInstance();

	private int numFieldOfGeneralTypes = 0;

	private int numContainerOfType = 0;

	private int numContainerOfInterfaces = 0;

	private int numofAppFrameworkTypes = 0;

	private int numofAppTypes = 0;

	private int numofFrameworkTypes = 0;

	private int numExceptionType = 0;

	private int numFieldOfFinal = 0;

	private int numDataType = 0;

	private int numinheritanceType1 = 0;

	private int numinheritanceType2 = 0;

	private int numcomposition = 0;

	private int unknown = 0;

	private ArrayList<IVariableBinding> variables = new ArrayList<IVariableBinding>();

	private int totalNumberOfClassification = 0;

	private ITypeBinding enclosingTypeField;

	ITypeBinding enclosingTypeVar;

	private ITypeBinding fieldTypeBinding;

	private String fieldName;

	public C_AllMetrics(Writer writer, ArrayList<IVariableBinding> var) {
		this.writer = writer;
		this.variables = var;
	}

	// method to collect types of all fields
	public Set<String> getAllFields() {
		Set<String> allFieldsType = new HashSet<String>();
		for (IVariableBinding varfield : variables) {
			fieldTypeBinding = varfield.getType();
			if (fieldTypeBinding != null) {
				String type = fieldTypeBinding.getQualifiedName();
				allFieldsType.add(type);
			}
		}
		return allFieldsType;
	}

	private Type getEnclosingFieldType(IVariableBinding typeBindingFeild) {

		Type declaringType = null;
		if (typeBindingFeild != null) {
			enclosingTypeField = typeBindingFeild.getDeclaringClass();
			if (enclosingTypeField != null) {
				declaringType = typeInfo.getType(enclosingTypeField.getQualifiedName());

			}
		}
		return declaringType;
	}

	private Type getEnclosingTypeVar(IMethodBinding typeBindingVar) {

		Type declaringType = null;
		if (typeBindingVar != null) {
			enclosingTypeVar = typeBindingVar.getDeclaringClass();
			if (enclosingTypeVar != null) {
				declaringType = typeInfo.getType(enclosingTypeVar.getQualifiedName());
			}
		}
		return declaringType;
	}

	@Override
	public void visit() {
		QualUtils utils = QualUtils.getInstance();

		for (IVariableBinding eachVarField : variables) {
			IMethodBinding declaringMethod = eachVarField.getDeclaringMethod();
			Type enclosingType1 = getEnclosingFieldType(eachVarField);
			Type enclosingType2 = getEnclosingTypeVar(declaringMethod);
			Set<Type> subClassesFieldType = null;
			fieldTypeBinding = eachVarField.getType();
			Type fieldType = typeInfo.getType(fieldTypeBinding.getQualifiedName());

			// System.out.println("the field type: " +fieldType);

			if (fieldType != null) {
				subClassesFieldType = fieldType.getSubClasses();
			}
			fieldName = eachVarField.getName();
			Set<Type> subClassesDeclaringType = null;

			// Get subclasses
			if (enclosingType1 != null) {
				subClassesDeclaringType = enclosingType1.getSubClasses();
			}
			else if (enclosingType2 != null) {
				subClassesDeclaringType = enclosingType2.getSubClasses();
			}

			if (fieldTypeBinding != null) {
				if (utils.isContainerOfGeneralType(fieldTypeBinding.getQualifiedName())) {
					numContainerOfInterfaces++;
				}
				else if (utils.isContainerType(fieldTypeBinding.getQualifiedName())) {
					numContainerOfType++;
				}
				else if (utils.isFieldOfAtLeastOneGeneralType(fieldTypeBinding.getQualifiedName())
				        && !utils.isJavaTypes(fieldTypeBinding.getQualifiedName())) {
					if (subClassesFieldType != null && subClassesFieldType.size() > 0
					        && subClassesDeclaringType != null && subClassesDeclaringType.size() > 0) {
						numinheritanceType2++;
					}
					else if (subClassesFieldType != null && subClassesFieldType.size() > 0) {
						numinheritanceType1++;
					}					
					else {
						numFieldOfGeneralTypes++;
					}
				}
				else if (enclosingType1 != null) {
					if (getAllFields().contains(enclosingType1.getFullyQualifiedName())) {					
						numcomposition++;
					}
				}

				
				else if (utils.isFrameworkType(fieldTypeBinding.getQualifiedName())) {
					numofAppFrameworkTypes++;
				}
				else if (utils.isapplicationType(fieldTypeBinding.getQualifiedName())) {
					numofAppTypes++;
				}
				else if (utils.isJavaTypes(fieldTypeBinding.getQualifiedName())) {
					numofFrameworkTypes++;
				}
				else if (Modifier.isFinal(fieldTypeBinding.getModifiers())) {
					numFieldOfFinal++;
				}
				else if (utils.isdataType(fieldTypeBinding.getQualifiedName())) {
					numDataType++;
				}
				else if (utils.isexceptionType(fieldTypeBinding.getQualifiedName())) {
					numExceptionType++;
				}
				else {
					unknown++;
				}
			}
		}
	}

	@Override
	public void display() throws IOException {

		QualUtils utils = QualUtils.getInstance();
		writer.append(CSVConst.NEWLINE);
		writer.append("Results of Quantitative analysis");
		writer.append(CSVConst.NEWLINE);

		// Header of the CSV
		writer.append(CSVConst.NEWLINE);
		writer.append("Field Type");
		writer.append(CSVConst.COMMA);
		writer.append("Field Name");
		writer.append(CSVConst.COMMA);
		writer.append("Enclosing Type");
		writer.append(CSVConst.COMMA);
		writer.append("Classification");
		writer.append(CSVConst.COMMA);
		writer.append(CSVConst.NEWLINE);

		for (IVariableBinding eachVarField : variables) {
			IMethodBinding declaringMethod = eachVarField.getDeclaringMethod();
			Type enclosingType1 = getEnclosingFieldType(eachVarField);
			Type enclosingType2 = getEnclosingTypeVar(declaringMethod);
			String declaringClass;
			Set<Type> subClassesFieldType = null;

			fieldTypeBinding = eachVarField.getType();
			Type fieldType = typeInfo.getType(fieldTypeBinding.getQualifiedName());

			if (fieldType != null) {
				subClassesFieldType = fieldType.getSubClasses();

			}

			if (enclosingType1 != null) {
				declaringClass = enclosingType1.getFullyQualifiedName();
			}
			else if (enclosingType2 != null) {
				declaringClass = enclosingType2.getFullyQualifiedName();
			}
			else {
				declaringClass = "";
			}

			fieldTypeBinding = eachVarField.getType();
			fieldName = eachVarField.getName();
			Set<Type> subClassesDeclaringType = null;

			// Get subclasses
			if (enclosingType1 != null) {
				subClassesDeclaringType = enclosingType1.getSubClasses();
			}
			else if (enclosingType2 != null) {
				subClassesDeclaringType = enclosingType2.getSubClasses();
			}

			if (fieldTypeBinding != null) {
				if (utils.isContainerOfGeneralType(fieldTypeBinding.getQualifiedName())) {
					writer.append(CSVOutputUtils.sanitize((fieldTypeBinding.getQualifiedName())));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(fieldName));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(declaringClass));
					writer.append(CSVConst.COMMA);
					writer.append("Container of a General Type");
					writer.append(CSVConst.NEWLINE);
				}
				else if (utils.isContainerType(fieldTypeBinding.getQualifiedName())) {
					writer.append(CSVOutputUtils.sanitize((fieldTypeBinding.getQualifiedName())));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(fieldName));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(declaringClass));
					writer.append(CSVConst.COMMA);
					writer.append("Container of a Type");
					writer.append(CSVConst.NEWLINE);
				}
				else if (utils.isFieldOfAtLeastOneGeneralType(fieldTypeBinding.getQualifiedName())
				        && !utils.isJavaTypes(fieldTypeBinding.getQualifiedName())) {
					if (subClassesFieldType != null && subClassesFieldType.size() > 0
					        && subClassesDeclaringType != null && subClassesDeclaringType.size() > 0) {
						writer.append(CSVOutputUtils.sanitize((fieldTypeBinding.getQualifiedName())));
						writer.append(CSVConst.COMMA);
						writer.append(CSVOutputUtils.sanitize(fieldName));
						writer.append(CSVConst.COMMA);
						writer.append(CSVOutputUtils.sanitize(declaringClass));
						writer.append(CSVConst.COMMA);
						writer.append("Inheritance Type 2");
						writer.append(CSVConst.NEWLINE);
					}
					else if (subClassesFieldType != null && subClassesFieldType.size() > 0) {
						writer.append(CSVOutputUtils.sanitize((fieldTypeBinding.getQualifiedName())));
						writer.append(CSVConst.COMMA);
						writer.append(CSVOutputUtils.sanitize(fieldName));
						writer.append(CSVConst.COMMA);
						writer.append(CSVOutputUtils.sanitize(declaringClass));
						writer.append(CSVConst.COMMA);
						writer.append("Inheritance Type 1");
						writer.append(CSVConst.NEWLINE);
					}			
					else {
						writer.append(CSVOutputUtils.sanitize((fieldTypeBinding.getQualifiedName())));
						writer.append(CSVConst.COMMA);
						writer.append(CSVOutputUtils.sanitize(fieldName));
						writer.append(CSVConst.COMMA);
						writer.append(CSVOutputUtils.sanitize(declaringClass));
						writer.append(CSVConst.COMMA);
						writer.append("Field of a general Type");
						writer.append(CSVConst.NEWLINE);
					}
				}

				else if (enclosingType1 != null) {
					if (getAllFields().contains(enclosingType1.getFullyQualifiedName())) {
						writer.append(CSVOutputUtils.sanitize((fieldTypeBinding.getQualifiedName())));
						writer.append(CSVConst.COMMA);
						writer.append(CSVOutputUtils.sanitize(fieldName));
						writer.append(CSVConst.COMMA);
						writer.append(CSVOutputUtils.sanitize(declaringClass));
						writer.append(CSVConst.COMMA);
						writer.append("Composition");
						writer.append(CSVConst.NEWLINE);
					}
				}
				else if (utils.isFrameworkType(fieldTypeBinding.getQualifiedName())) {
					writer.append(CSVOutputUtils.sanitize((fieldTypeBinding.getQualifiedName())));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(fieldName));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(declaringClass));
					writer.append(CSVConst.COMMA);
					writer.append("Application framework type");
				}
				else if (utils.isapplicationType(fieldTypeBinding.getQualifiedName())) {
					writer.append(CSVOutputUtils.sanitize((fieldTypeBinding.getQualifiedName())));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(fieldName));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(declaringClass));
					writer.append(CSVConst.COMMA);
					writer.append("Application type");
					writer.append(CSVConst.NEWLINE);
				}
				else if (utils.isJavaTypes(fieldTypeBinding.getQualifiedName())) {
					writer.append(CSVOutputUtils.sanitize((fieldTypeBinding.getQualifiedName())));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(fieldName));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(declaringClass));
					writer.append(CSVConst.COMMA);
					writer.append("Java framework type");
					writer.append(CSVConst.NEWLINE);
				}
				else if (Modifier.isFinal(fieldTypeBinding.getModifiers())) {
					writer.append(CSVOutputUtils.sanitize((fieldTypeBinding.getQualifiedName())));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(fieldName));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(declaringClass));
					writer.append(CSVConst.COMMA);
					writer.append("Application final type");
					writer.append(CSVConst.NEWLINE);
				}
				else if (utils.isdataType(fieldTypeBinding.getQualifiedName())) {
					writer.append(CSVOutputUtils.sanitize((fieldTypeBinding.getQualifiedName())));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(fieldName));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(declaringClass));
					writer.append(CSVConst.COMMA);
					writer.append("Data type");
					writer.append(CSVConst.NEWLINE);
				}
				else if (utils.isexceptionType(fieldTypeBinding.getQualifiedName())) {
					writer.append(CSVOutputUtils.sanitize((fieldTypeBinding.getQualifiedName())));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(fieldName));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(declaringClass));
					writer.append(CSVConst.COMMA);
					writer.append("Exception type");
					writer.append(CSVConst.NEWLINE);
				}
				else {
					writer.append(CSVOutputUtils.sanitize((fieldTypeBinding.getQualifiedName())));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(fieldName));
					writer.append(CSVConst.COMMA);
					writer.append(CSVOutputUtils.sanitize(declaringClass));
					writer.append(CSVConst.COMMA);
					writer.append("Unknown");
					writer.append(CSVConst.NEWLINE);
				}

			}
		}
		writer.append(CSVConst.NEWLINE);
		writer.append("Summary of the Classification");
		writer.append(CSVConst.NEWLINE);

		// The total count of Container of General Types
		writer.append("Count of container of General type");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numContainerOfInterfaces).toString());
		writer.append(CSVConst.NEWLINE);

		// The total count of Container of Types
		writer.append("Count of container of a type");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numContainerOfType).toString());
		writer.append(CSVConst.NEWLINE);

		// The total count of Inheritance Type 2
		writer.append("Count of inheritance type 2");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numinheritanceType2).toString());
		writer.append(CSVConst.NEWLINE);

		// The total count of Inheritance Type 1
		writer.append("Count of inheritance type 1");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numinheritanceType1).toString());
		writer.append(CSVConst.NEWLINE);

		// The total count of Field of General Types
		writer.append("Count of field of a general type");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numFieldOfGeneralTypes).toString());
		writer.append(CSVConst.NEWLINE);

		// The total count of Composition
		writer.append("Count of composition");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numcomposition).toString());
		writer.append(CSVConst.NEWLINE);

		// The total count Application framework types
		writer.append("Count of Application framework type");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numofAppFrameworkTypes).toString());
		writer.append(CSVConst.NEWLINE);

		// The total count Application types
		writer.append("Count of Application default type");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numofAppTypes).toString());
		writer.append(CSVConst.NEWLINE);

		// The total count Java types
		writer.append("Count of Java framework type");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numofFrameworkTypes).toString());
		writer.append(CSVConst.NEWLINE);

		// The total count of Field of a Final types
		writer.append("Count of final type");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numFieldOfFinal).toString());
		writer.append(CSVConst.NEWLINE);

		// The total count of Data types
		writer.append("Count of Data type");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numDataType).toString());
		writer.append(CSVConst.NEWLINE);

		// The total count of Exception classification - Classification 6
		writer.append("Count of Exception type");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numExceptionType).toString());
		writer.append(CSVConst.NEWLINE);

		// The total count of Unknow classifications
		writer.append("Count of unknown classification");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.unknown).toString());
		writer.append(CSVConst.NEWLINE);

		totalNumberOfClassification = numContainerOfInterfaces + numContainerOfType + numDataType + numExceptionType
		        + numFieldOfFinal + numFieldOfGeneralTypes + numofAppFrameworkTypes + numofAppTypes
		        + numofFrameworkTypes + numinheritanceType2 + numinheritanceType1 + numcomposition;

		// Sanity Check
		writer.append(CSVConst.NEWLINE);
		writer.append("Total count of classified fields");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.totalNumberOfClassification).toString());
		writer.append(CSVConst.NEWLINE);
	}

}
