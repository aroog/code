package edu.wayne.metrics.qual;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import ast.Type;
import edu.wayne.metrics.adb.CustomWriter;
import edu.wayne.metrics.adb.Util;
import edu.wayne.metrics.utils.CSVConst;
import oog.itf.IObject;

public class Q_HMO extends ClassifyUtlis {

	private int totalNumberOfOutliers = 0;

	private Writer writer;

	private Set<Collection<IObject>> outliers;

	private String shortMetricName;

	public Q_HMO(Writer writer, Set<Collection<IObject>> outliers, String shortMetricName) {
		this.writer = writer;
		this.outliers = outliers;
		this.shortMetricName = shortMetricName;
	}

	@Override
	public void visit() {
		for (Collection<IObject> objects : outliers) {
			for (IObject object : objects) {

				Type typeObject = object.getC();
				Set<String> allSuperclassTypeObj = new HashSet<String>();
				Util.getSuperClasses(typeObject, allSuperclassTypeObj, true);
				if (object != null) {
					HMO(typeObject);
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

		for (Collection<IObject> objects : outliers) {
			for (IObject object : objects) {

				Type typeObject = object.getC();
				Set<String> allSuperclassTypeObj = new HashSet<String>();
				Util.getSuperClasses(typeObject, allSuperclassTypeObj, true);
				if (object != null) {
					if (utils.isContainerOfGeneralType(typeObject.getFullyQualifiedName())) {
						writer.append(typeObject.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append("Container of a General Type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isContainerType(typeObject.getFullyQualifiedName())) {
						writer.append(typeObject.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append("Container of a Type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isFrameworkType(typeObject.getFullyQualifiedName())) {
						writer.append(typeObject.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append("Application framework type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isdataType(typeObject.getFullyQualifiedName())) {
						writer.append(typeObject.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append("Data type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isexceptionType(typeObject.getFullyQualifiedName())
					        || allSuperclassTypeObj.contains(EXCEPTIONS)) {
						writer.append(typeObject.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append("Exception type");
						writer.append(CSVConst.NEWLINE);
					}
					else {
						writer.append(typeObject.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append("Classification is unknown");
						writer.append(CSVConst.NEWLINE);
					}
				}

			}
		}

		// Use the numbers from ClassifyUtils
		totalNumberOfOutliers = getNumContainerOfInterfaces() + getNumContainerOfType() + getNumofAppFrameworkTypes()
		        + getUnknown() + getNumExceptionClass() + getNumDataClass();

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

		// The total count of App Framework type - Classification 3
		qualSummaryWrite.append("Count of Data type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumDataClass()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of Java type - Classification 6
		qualSummaryWrite.append("Count of Exception type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumExceptionClass()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of unknown classification
		qualSummaryWrite.append("Count of Unknown Classification");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getUnknown()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		qualSummaryWrite.flush();
		qualSummaryWrite.close();

	}

}
