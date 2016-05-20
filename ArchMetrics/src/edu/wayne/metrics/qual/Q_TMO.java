package edu.wayne.metrics.qual;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import ast.Type;
import oog.itf.IObject;
import edu.wayne.metrics.adb.ADBTriplet;
import edu.wayne.metrics.adb.CustomWriter;
import edu.wayne.metrics.adb.Util;
import edu.wayne.metrics.adb.HowManyTypesMergedByObject.MergedTypesInfo;
import edu.wayne.metrics.adb.ObjectInfo;
import edu.wayne.metrics.utils.CSVConst;

// TOSUM: DONE: HIGH The code seems buggy (few outliers classified in TMO of CDB)
public class Q_TMO extends ClassifyUtlis {

	private Writer writer;

	private Set<ObjectInfo> outliers;

	private String shortMetricName;

	// Count of classification of outliers
	private int totalNumberOfOutliers = 0;

	public Q_TMO(Writer writer, Set<ObjectInfo> outliers, String name) {
		this.writer = writer;
		this.outliers = outliers;
		this.shortMetricName = name;
	}

	@Override
	public void visit() {
		for (ObjectInfo oInfo : outliers) {
			if (oInfo instanceof MergedTypesInfo) {
				MergedTypesInfo oInf = (MergedTypesInfo) oInfo;
				ADBTriplet triletObj = oInf.getTriplet();
				IObject objectA = triletObj.getObjectA();
				Type typeA = objectA.getC();
				Set<String> allSuperclassTypeA = new HashSet<String>();
				Util.getSuperClasses(typeA, allSuperclassTypeA, true);
				Type superclassTypeA = typeA.getSuperClass();
				// Implementing the methods from ClassifyUtils
				TMO(typeA, superclassTypeA);

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

		for (ObjectInfo oInfo : outliers) {
			if (oInfo instanceof MergedTypesInfo) {
				MergedTypesInfo oInf = (MergedTypesInfo) oInfo;
				ADBTriplet triletObj = oInf.getTriplet();
				IObject objectA = triletObj.getObjectA();
				Type typeA = objectA.getC();
				Type superclassTypeA = typeA.getSuperClass();
				Set<String> allSuperclassTypeA = new HashSet<String>();
				Util.getSuperClasses(typeA, allSuperclassTypeA, true);

				if (utils.isapplicationType(typeA.getFullyQualifiedName())) {

					if (superclassTypeA != null) {
						if (utils.isAbstractType(superclassTypeA.getFullyQualifiedName())
						        || utils.isInterfaceType(superclassTypeA.getFullyQualifiedName())) {
							writer.append(typeA.getFullyQualifiedName());
							writer.append(CSVConst.COMMA);
							writer.append("Application type <: General type");
							writer.append(CSVConst.NEWLINE);
						}

						else if (utils.isJavaTypes(superclassTypeA.getFullyQualifiedName())
						        && !utils.isAbstractType(superclassTypeA.getFullyQualifiedName())
						        && !utils.isInterfaceType(superclassTypeA.getFullyQualifiedName())) {
							writer.append(typeA.getFullyQualifiedName());
							writer.append(CSVConst.COMMA);
							writer.append("Application type <: Java type");
							writer.append(CSVConst.NEWLINE);
						}
						else if (utils.isapplicationType(superclassTypeA.getFullyQualifiedName())) {
							writer.append(typeA.getFullyQualifiedName());
							writer.append(CSVConst.COMMA);
							writer.append("Application type <: Application type");
							writer.append(CSVConst.NEWLINE);
						}
						else if (utils.isexceptionType(superclassTypeA.getFullyQualifiedName())
						        || allSuperclassTypeA.contains(EXCEPTIONS)) {
							writer.append(typeA.getFullyQualifiedName());
							writer.append(CSVConst.COMMA);
							writer.append("Exception type");
							writer.append(CSVConst.NEWLINE);
						}

						if (utils.isFrameworkType(superclassTypeA.getFullyQualifiedName())
						        && !utils.isAbstractType(superclassTypeA.getFullyQualifiedName())
						        && !utils.isInterfaceType(superclassTypeA.getFullyQualifiedName())) {
							if (!utils.isAbstractType(superclassTypeA.getFullyQualifiedName())
							        || !utils.isInterfaceType(superclassTypeA.getFullyQualifiedName())) {
								writer.append(typeA.getFullyQualifiedName());
								writer.append(CSVConst.COMMA);
								writer.append("Application type <: Framework type");
								writer.append(CSVConst.NEWLINE);
							}
						}
					}

				}
				else if (utils.isFrameworkType(typeA.getFullyQualifiedName())) {
					if (superclassTypeA != null) {
						if (utils.isJavaTypes(superclassTypeA.getFullyQualifiedName())) {
							writer.append(typeA.getFullyQualifiedName());
							writer.append(CSVConst.COMMA);
							writer.append("Framework type <: Java type");
							writer.append(CSVConst.NEWLINE);
						}
						else if (utils.isFrameworkType(superclassTypeA.getFullyQualifiedName())) {
							writer.append(typeA.getFullyQualifiedName());
							writer.append(CSVConst.COMMA);
							writer.append("Framework type <: Framework type");
							writer.append(CSVConst.NEWLINE);
						}
						else if (utils.isexceptionType(superclassTypeA.getFullyQualifiedName())
						        || allSuperclassTypeA.contains(EXCEPTIONS)) {
							writer.append(typeA.getFullyQualifiedName());
							writer.append(CSVConst.COMMA);
							writer.append("Exception type");
							writer.append(CSVConst.NEWLINE);
						}

					}
				}
				else if (utils.isexceptionType(typeA.getFullyQualifiedName())
				        || allSuperclassTypeA.contains(EXCEPTIONS)) {
					writer.append(typeA.getFullyQualifiedName());
					writer.append(CSVConst.COMMA);
					writer.append("Exception type");
					writer.append(CSVConst.NEWLINE);
				}
				else {
					writer.append(typeA.getFullyQualifiedName());
					writer.append(CSVConst.COMMA);
					writer.append("Classification is unknown");
					writer.append(CSVConst.NEWLINE);
				}

			}
		}

		totalNumberOfOutliers = getNumDefaultDefaultGT() + getUnknown() + getNumDefaultappFramewk1()
		        + getNumappFramewk1appFramewk2() + getNumDefaultDefault() + getNumDefaultF1DefaultF2GT()
		        + getNumDefaultDefaultF1DefaultF2GT() + getNumExceptionClass();
		// Sanity Check
		writer.append(CSVConst.NEWLINE);
		writer.append("Total count of the outliers");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.totalNumberOfOutliers).toString());
		writer.append(CSVConst.NEWLINE);

		Writer qualSummaryWrite = new CustomWriter(filePath);

		// writer1.append(CSVConst.NEWLINE);
		// writer1.append("Summary of the Classification");
		// writer1.append(CSVConst.NEWLINE);

		// The total count of Application subtype of general type
		qualSummaryWrite.append("Count of Application type <: General type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumDefaultDefaultGT()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of Exception classes - Classification 2

		qualSummaryWrite.append("Count of Exception types");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumExceptionClass()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of Application types <: Java types

		qualSummaryWrite.append("Count of Application type <: Java type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumDefaultDefaultF1DefaultF2GT()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of Application type <: Application type
		qualSummaryWrite.append("Count of Application type <: Application type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumDefaultDefault()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of application type <: framework type
		qualSummaryWrite.append("Count of Application type <: Framework type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumDefaultappFramewk1()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of Framework types <: Java types
		qualSummaryWrite.append("Count of Framework type <: Java type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumDefaultF1DefaultF2GT()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of framework type <: framework type
		qualSummaryWrite.append("Count of Framework type <: Framework type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumappFramewk1appFramewk2()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// Unknown classification
		qualSummaryWrite.append("Count of Unknown Classification");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getUnknown()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		qualSummaryWrite.flush();
		qualSummaryWrite.close();

	}

}
