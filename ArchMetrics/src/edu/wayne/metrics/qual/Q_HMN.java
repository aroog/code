package edu.wayne.metrics.qual;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import edu.wayne.metrics.adb.CustomWriter;
import edu.wayne.metrics.adb.Util;
import edu.wayne.metrics.utils.CSVConst;
import ast.ClassInstanceCreation;
import ast.Type;

public class Q_HMN extends Q_Base {

	private Set<Collection<ClassInstanceCreation>> outliers;

	private String shortMetricName;

	private int numExceptionType = 0;

	private int numContainerOfInterfaces = 0;

	private int numDataType = 0;

	private int numofAppFrameworkTypes = 0;

	private int numofFrameworkTypes = 0;

	private int numHashMapNewExp = 0;

	private int unknown = 0;

	private int totalNumberOfOutliers = 0;

	private Writer writer;

	public Q_HMN(Writer writer, Set<Collection<ClassInstanceCreation>> outliers, String shortMetricName) {
		this.writer = writer;
		this.outliers = outliers;
		this.shortMetricName = shortMetricName;
	}

	@Override
	public void visit() {
		QualUtils utils = QualUtils.getInstance();

		/*
		 * We need the following for classification of the outliers 1) Just need the New expression associated with
		 * ClassInstanceCreation
		 */

		for (Collection<ClassInstanceCreation> tt1 : outliers) {
			for (ClassInstanceCreation expr : tt1) {
				if (expr != null) {
					Type typeClassInstance = expr.typeDeclaration.type;

					Set<String> allSuperclassTypeObj = new HashSet<String>();
					Util.getSuperClasses(typeClassInstance, allSuperclassTypeObj, true);
					if (utils.isexceptionType(typeClassInstance.getFullyQualifiedName())
					        || allSuperclassTypeObj.contains(EXCEPTIONS)) {
						numExceptionType++;
					}
					else if (utils.isFrameworkType(typeClassInstance.getFullyQualifiedName())) {
						numofAppFrameworkTypes++;
					}
					else if (utils.isJavaTypes(typeClassInstance.getFullyQualifiedName())) {
						numofFrameworkTypes++;
					}
					else if (utils.isContainerOfGeneralType(typeClassInstance.getFullyQualifiedName())) {
						numContainerOfInterfaces++;
					}
					else if (utils.isdataType(typeClassInstance.getFullyQualifiedName())) {
						numDataType++;
					}
					else {
						unknown++;
					}
				}

			}
		}
	}

	@Override
	public void display() throws IOException {
		QualUtils utils = QualUtils.getInstance();

		String path = utils.getPath();
		IProject project = utils.getProject();
		// Path to write Qual summary data
		String filePath = path + "\\" + CSVConst.R_Qual + project.getName() + "_" + shortMetricName + ".csv";

		writer.append(CSVConst.NEWLINE);
		writer.append("Results of Quantitative analysis");
		writer.append(CSVConst.NEWLINE);

		String type = null;
		for (Collection<ClassInstanceCreation> tt1 : outliers) {
			for (ClassInstanceCreation expr : tt1) {
				if (expr != null) {
					type = expr.typeDeclaration.getFullyQualifiedName();
					Type typeClassInstance = expr.typeDeclaration.type;
					Set<String> allSuperclassTypeObj = new HashSet<String>();
					Util.getSuperClasses(typeClassInstance, allSuperclassTypeObj, true);
					if (utils.isexceptionType(type) || allSuperclassTypeObj.contains(EXCEPTIONS)) {
						type = expr.typeDeclaration.getFullyQualifiedName();
						writer.append(type);
						writer.append(CSVConst.COMMA);
						writer.append("Exception type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isFrameworkType(type)) {
						type = expr.typeDeclaration.getFullyQualifiedName();
						writer.append(type);
						writer.append(CSVConst.COMMA);
						writer.append("Application framework type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isContainerOfGeneralType(type)) {
						type = expr.typeDeclaration.getFullyQualifiedName();
						writer.append(type);
						writer.append(CSVConst.COMMA);
						writer.append("Container of general type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isJavaTypes(type)) {
						type = expr.typeDeclaration.getFullyQualifiedName();
						writer.append(type);
						writer.append(CSVConst.COMMA);
						writer.append("Java framework type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isdataType(type)) {
						type = expr.typeDeclaration.getFullyQualifiedName();
						writer.append(type);
						writer.append(CSVConst.COMMA);
						writer.append("Data type");
						writer.append(CSVConst.NEWLINE);
					}
					else {
						type = expr.typeDeclaration.getFullyQualifiedName();
						writer.append(type);
						writer.append(CSVConst.COMMA);
						writer.append("Classification is unknown");
						writer.append(CSVConst.NEWLINE);
					}
				}

				else {
					System.out.println("The expression is null!");
				}
			}
		}

		totalNumberOfOutliers = numDataType + numExceptionType + numHashMapNewExp + +numofAppFrameworkTypes
		        + numofFrameworkTypes + unknown + numContainerOfInterfaces;
		// Sanity Check
		writer.append(CSVConst.NEWLINE);
		writer.append("Total count of the outliers");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.totalNumberOfOutliers).toString());
		writer.append(CSVConst.NEWLINE);

		// The summary report

		Writer qualSummaryWrite = new CustomWriter(filePath);

		/*
		 * qualSummaryWrite.append(CSVConst.NEWLINE); qualSummaryWrite.append("Summary of the Classification");
		 * qualSummaryWrite.append(CSVConst.NEWLINE);
		 */

		// Total Count of creation of Exception - Classification 1
		qualSummaryWrite.append("Count of Exception type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(this.numExceptionType).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// Total Count of creation of App Framework types- Classification 2

		qualSummaryWrite.append("Count of Application framework type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(this.numofAppFrameworkTypes).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// Total Count of creation of App Framework types- Classification 2

		qualSummaryWrite.append("Count of Container of general type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(this.numContainerOfInterfaces).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// Total Count of creation of Framework types- Classification 3

		qualSummaryWrite.append("Count of Java framework type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(this.numofFrameworkTypes).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		qualSummaryWrite.append("Count of data type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(this.numDataType).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// Total Count of creation of keys of Containers - Classification 3
		qualSummaryWrite.append("Count of creation of keys of Containers");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(this.numHashMapNewExp).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of unknown classification

		qualSummaryWrite.append("Count of unknown classification");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(this.unknown).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		qualSummaryWrite.flush();
		qualSummaryWrite.close();

	}
}
