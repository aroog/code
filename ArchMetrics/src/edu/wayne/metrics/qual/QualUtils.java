package edu.wayne.metrics.qual;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Set;
import edu.wayne.metrics.mapping.Model;
import edu.wayne.metrics.mapping.ModelManager;
import edu.wayne.metrics.mapping.PackageType;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.ITypeBinding;
import edu.wayne.metrics.Crystal;

/**
 * Consolidate all the logic used for the qualitative analysis in this class TODO: Avoid duplication with Util TODO: Use
 * static methods or not? TODO: This is a non-util class now. It has all the qualitative analysis logic. Rename:
 * QualAnalyzer Then just move Util methods to another Util class General guidelines: - Convert string to ITypebinding
 * ASAP, to use the functionality on ITypeBinding TOSUM: Renaming and cleaning up misleading names
 */
public class QualUtils {

	private static QualUtils instance = null;

	Model model = new Model();
	
	IProject project;
	private String projectName;
	public IPath location;

	ModelManager m = ModelManager.getInstance();

	private static Crystal crystal = null;

	// TOSUM: DONE: HIGH. Move all these fields to the Model class
	// - Also, move the getters
	// - In order to save them/load them from the XML file
	// TOSUM: DONE: Refactor to use the ModelManager

	
   // Use this to also lazily initialize Crystal
	public static Crystal getCrystal() {
		if (crystal == null) {
			crystal = Crystal.getInstance();
		}
		return crystal;
	}

	public static QualUtils getInstance() {
		if (instance == null) {
			instance = new QualUtils();
		}
		return instance;
	}

	public ITypeBinding getTypeBinding(String fullyQualifiedName) {
		ITypeBinding typeBinding = getCrystal().getTypeBindingFromName(fullyQualifiedName);
		return typeBinding;
	}

	public String getRawTypeName(String genericType) {
		String rawName = org.eclipse.jdt.core.Signature.getTypeErasure(genericType);
		return rawName;
	}

	// Getters and Setters for location of the project
	public void setProject(IProject project) {
		this.project = project;
	}
	
	public IProject getProject() {
		return project;
	}
	
	public String getPath(){
		return project.getLocation().toOSString();
	}

	// Changed the method name to isContainerOfAtLeastOneGeneralType
	public boolean isContainerOfGeneralType(String fullyQualifiedName) {
		boolean oneItf = false;

		model = m.getModel();
		Set<String> containerList = model.getContainerTypes();
		// Get the type of container;
		String containerType = getRawTypeName(fullyQualifiedName);
		if (containerList.contains(containerType)) {
			ITypeBinding typeBinding = getTypeBinding(fullyQualifiedName);
			if (typeBinding != null) {
				ITypeBinding[] typeArguments = typeBinding.getTypeArguments();
				if (typeArguments != null) {
					for (int ii = 0; ii < typeArguments.length; ii++) {
						ITypeBinding typeArgument = typeArguments[ii];
						// Changed this to container of a general type
						// (Interface and Abstract classes)
						if (typeArgument != null && typeArgument.isInterface()
						        || Modifier.isAbstract(typeArgument.getModifiers())
						        || isJavaTypes(typeArgument.getQualifiedName())) {
							// TODO: Are we checking that at least one is an
							// interface; or that all are interfaces?
							oneItf = true;
							break;
						}
					}
				}
			}
		}

		return oneItf;
	}

	// Added a new method to check if the container is of a TYPE
	public boolean isContainerType(String fullyQualifiedName) {
		boolean containerType = false;
		String containerofType = getRawTypeName(fullyQualifiedName);

		model = m.getModel();
		Set<String> containerList = model.getContainerTypes();

		// Check if ArrayList and other containers are containers of TYPE(CLASS)
		if (containerList.contains(containerofType)) {
			ITypeBinding typeBinding = getTypeBinding(fullyQualifiedName);
			if (typeBinding != null) {
				ITypeBinding[] typeArguments = typeBinding.getTypeArguments();

				if (typeArguments != null) {
					for (int ii = 0; ii < typeArguments.length; ii++) {
						ITypeBinding typeArgument = typeArguments[ii];
						if (typeArgument != null && typeArgument.isClass()) {
							containerType = true;
							break;
						}
					}
				}
			}
		}

		return containerType;
	}

	// Check for fields of Interfaces/Abstract/Java Library Types
	public boolean isFieldOfAtLeastOneGeneralType(String fullyQualifiedName) {
		boolean genField = false;

		model = m.getModel();
		Set<String> javaLibList = model.getJavaLibTypes();
		ITypeBinding typeBinding = getTypeBinding(fullyQualifiedName);
		String frameworkType = getRawTypeName(fullyQualifiedName);
		if (typeBinding != null) {
			if (typeBinding.isInterface() || Modifier.isAbstract(typeBinding.getModifiers())
			        || javaLibList.contains(frameworkType)) {
				genField = true;
			}
		}

		return genField;
	}

	// Check for types are Abstract
	public boolean isAbstractType(String fullyQualifiedName) {
		boolean genAbstractType = false;
		ITypeBinding typeBinding = getTypeBinding(fullyQualifiedName);
		if (typeBinding != null) {
			if (Modifier.isAbstract(typeBinding.getModifiers())) {
				genAbstractType = true;
			}
		}
		return genAbstractType;
	}

	// Check for types are Interface
	public boolean isInterfaceType(String fullyQualifiedName) {
		boolean genInterfaceType = false;
		ITypeBinding typeBinding = getTypeBinding(fullyQualifiedName);
		if (typeBinding != null) {
			if (typeBinding.isInterface()) {
				genInterfaceType = true;
			}
		}
		return genInterfaceType;
	}

	// Check if the type/field is a application framework type
	// Renamed the method - Has all the Frame work types (No layering at this point in time)
	public boolean isFrameworkType(String fullyQualifiedName) {
		boolean frameworksType = false;

		model = m.getModel();
		Set<PackageType> frameworkTypeList = model.getFrameworkPackageTypes();
		String frameworkType = getRawTypeName(fullyQualifiedName);
		Iterator<PackageType> iterator = frameworkTypeList.iterator();
		while (iterator.hasNext()) {
			{
				String typeName = iterator.next().getTypeName();

				if (typeName.equals(frameworkType)) {
					frameworksType = true;
				}
			}
		}
		return frameworksType;
	}

	// Check if the type/field is a application type
	// Renamed the method - Has all the application types
	public boolean isapplicationType(String fullyQualifiedName) {
		boolean applicationTypes = false;
		model = m.getModel();
		Set<PackageType> applicationTypeList = model.getApplicationPackageTypes();
		String applicationType = getRawTypeName(fullyQualifiedName);

		Iterator<PackageType> iterator = applicationTypeList.iterator();
		while (iterator.hasNext()) {
			{
				String typeName = iterator.next().getTypeName();
				if (typeName.equals(applicationType)) {
					applicationTypes = true;
				}
			}
		}
		return applicationTypes;
	}

	// Check types/field if they are Java types
	public boolean isJavaTypes(String fullyQualifiedName) {
		boolean javaType = false;

		model = m.getModel();
		Set<String> javaList = model.getJavaLibTypes();
		String libType = getRawTypeName(fullyQualifiedName);
		if (javaList.contains(libType)) {
			javaType = true;
		}
		return javaType;
	}

	// Check if the type/field is data type
	public boolean isdataType(String fullyQualifiedName) {
		boolean datasType = false;

		model = m.getModel();
		Set<String> dataTypeList = model.getDataTypes();
		String dataType = getRawTypeName(fullyQualifiedName);
		if (dataTypeList.contains(dataType)) {
			datasType = true;
		}

		return datasType;
	}

	// Check if the type/field is an exception type
	public boolean isexceptionType(String fullyQualifiedName) {
		boolean exceptionsType = false;

		model = m.getModel();
		Set<String> exceptionTypeList = model.getExceptionTypes();
		String exceptionType = getRawTypeName(fullyQualifiedName);
		if (exceptionTypeList.contains(exceptionType)) {
			exceptionsType = true;
		}

		return exceptionsType;
	}
}