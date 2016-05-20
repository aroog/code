package edu.wayne.metrics.qual;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import oog.itf.IObject;
import ast.Type;
import edu.wayne.metrics.adb.CustomWriter;
import edu.wayne.metrics.adb.TripletPairAlt;
import edu.wayne.metrics.adb.Util;
import edu.wayne.metrics.utils.CSVConst;

public class Q_Which_A_In_B extends ClassifyUtlis {
	private Writer writer;

	private Set<Collection<TripletPairAlt>> outliers;

	private String shortMetricName;

	private int totalNumberOfOutliers = 0;

	public Q_Which_A_In_B(Writer writer, Set<Collection<TripletPairAlt>> outliers, String shortMetricName) {
		this.writer = writer;
		this.outliers = outliers;
		this.shortMetricName = shortMetricName;
	}

	@Override
	public void visit() {
		for (Collection<TripletPairAlt> outlier : outliers) {
			for (TripletPairAlt pair : outlier) {

				// Get the pair of objects from TripletPair
				IObject firstObj = pair.getFirst();
				IObject secondObj = pair.getSecond();

				if (firstObj != null && secondObj != null) {
					// To handle custom exception
					Type firstObjType = firstObj.getC();
					Type secObjType = secondObj.getC();
					Set<String> allSuperclassTypeObj = new HashSet<String>();
					Util.getSuperClasses(firstObjType, allSuperclassTypeObj, true);

					// Calling the method from ClassifyUtils
					Which_A_In_B(firstObjType, secObjType);

				}
			}
		}

	}

	@Override
	public void display() throws IOException {
		// TODO Auto-generated method stub
		QualUtils utils = QualUtils.getInstance();

		String path = utils.getPath();
		IProject project = utils.getProject();
		// Path to write Qual summary data
		String filePath = path + "\\" + CSVConst.R_Qual + project.getName() + "_" + shortMetricName + ".csv";
		writer.append(CSVConst.NEWLINE);
		writer.append("Results of Quantitative analysis");
		writer.append(CSVConst.NEWLINE);

		for (Collection<TripletPairAlt> outlier : outliers) {
			for (TripletPairAlt pair : outlier) {
				// Get the pair of objects from TripletPair
				IObject firstObj = pair.getFirst();
				IObject secondObj = pair.getSecond();

				if (firstObj != null && secondObj != null) {
					// To handle custom exception
					Type firstObjType = firstObj.getC();
					Type secObjType = secondObj.getC();
					Set<String> allSuperclassTypeObj = new HashSet<String>();
					Util.getSuperClasses(firstObjType, allSuperclassTypeObj, true);
					if (utils.isContainerOfGeneralType(firstObjType.getFullyQualifiedName())
					        && utils.isContainerOfGeneralType(secObjType.getFullyQualifiedName())) {
						writer.append(firstObjType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(firstObj.getParent().getD());
						writer.append(CSVConst.COMMA);
						writer.append(secObjType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(secondObj.getParent().getD());
						writer.append(CSVConst.COMMA);
						writer.append("Container of a General Type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isContainerType(firstObjType.getFullyQualifiedName())
					        || utils.isContainerType(secObjType.getFullyQualifiedName())) {
						writer.append(firstObjType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(firstObj.getParent().getD());
						writer.append(CSVConst.COMMA);
						writer.append(secObjType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(secondObj.getParent().getD());
						writer.append(CSVConst.COMMA);
						writer.append("Container of a Type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isFrameworkType(firstObjType.getFullyQualifiedName())
					        && utils.isFrameworkType(secObjType.getFullyQualifiedName())) {
						writer.append(firstObjType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(firstObj.getParent().getD());
						writer.append(CSVConst.COMMA);
						writer.append(secObjType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(secondObj.getParent().getD());
						writer.append(CSVConst.COMMA);
						writer.append("Application framework type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isapplicationType(firstObjType.getFullyQualifiedName())
					        && utils.isapplicationType(secObjType.getFullyQualifiedName())) {
						writer.append(firstObjType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(firstObj.getParent().getD());
						writer.append(CSVConst.COMMA);
						writer.append(secObjType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(secondObj.getParent().getD());
						writer.append(CSVConst.COMMA);
						writer.append("Application default type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isJavaTypes(firstObjType.getFullyQualifiedName())
					        && utils.isJavaTypes(secObjType.getFullyQualifiedName())) {
						writer.append(firstObjType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(firstObj.getParent().getD());
						writer.append(CSVConst.COMMA);
						writer.append(secObjType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(secondObj.getParent().getD());
						writer.append(CSVConst.COMMA);
						writer.append("Java framework type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isexceptionType(firstObjType.getFullyQualifiedName())
					        && utils.isexceptionType(secObjType.getFullyQualifiedName())
					        || allSuperclassTypeObj.contains(EXCEPTIONS)) {
						writer.append(firstObjType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(firstObj.getParent().getD());
						writer.append(CSVConst.COMMA);
						writer.append(secObjType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(secondObj.getParent().getD());
						writer.append(CSVConst.COMMA);
						writer.append("Exception Class");
						writer.append(CSVConst.NEWLINE);
					}
					else {
						writer.append(firstObjType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(firstObj.getParent().getD());
						writer.append(CSVConst.COMMA);
						writer.append(secObjType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(secondObj.getParent().getD());
						writer.append(CSVConst.COMMA);
						writer.append("Classification is unknown");
						writer.append(CSVConst.NEWLINE);
					}
				}
			}
		}

		totalNumberOfOutliers = getNumContainerOfInterfaces() + getNumContainerOfType() + getNumofAppFrameworkTypes()
		        + getNumofAppTypes() + getNumofFrameworkTypes() + getNumExceptionClass() + getUnknown();

		// Sanity Check
		writer.append(CSVConst.NEWLINE);
		writer.append("Total count of the outliers");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.totalNumberOfOutliers).toString());
		writer.append(CSVConst.NEWLINE);

		// The summary report

		Writer qualSummaryWrite = new CustomWriter(filePath);

		/*
		 * // Using the numbers from ClassifyUtils qualSummaryWrite.append(CSVConst.NEWLINE);
		 * qualSummaryWrite.append("Summary of the Classification"); qualSummaryWrite.append(CSVConst.NEWLINE);
		 */
		// The total count of Container of General Type - Classification 1
		qualSummaryWrite.append("Count of container of General type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumContainerOfInterfaces()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of Container of Type - Classification 2
		qualSummaryWrite.append("Count of container of a type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumContainerOfType()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of App Framework type - Classification 3
		qualSummaryWrite.append("Count of Application framework type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumofAppFrameworkTypes()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of App Default type - Classification 4
		qualSummaryWrite.append("Count of Application default type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumofAppTypes()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of Java type - Classification 5
		qualSummaryWrite.append("Count of Java framework type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumofFrameworkTypes()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of Java type - Classification 6
		qualSummaryWrite.append("Count of Exception type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumExceptionClass()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of Unknow classifications
		qualSummaryWrite.append("Count of unknown classification");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getUnknown()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		qualSummaryWrite.flush();
		qualSummaryWrite.close();
	}

}
