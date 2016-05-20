package edu.wayne.metrics.qual;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import ast.Type;
import oog.itf.IObject;
import edu.wayne.metrics.adb.CustomWriter;
import edu.wayne.metrics.adb.Util;
import edu.wayne.metrics.utils.CSVConst;

// Now - Q_Which_A points to ClassifyUtils
public class Q_Which_A extends ClassifyUtlis {

	private Writer writer;

	private Set<Collection<IObject>> outliers;

	private String shortMetricName;

	// TODO: Convert this to use a Hashtable...
	// Make the output code a bit more flexible
	// Don't have to modify output code each time we want to add something
	// Share common output code in Q_Base

	// TODO: Include additional information?
	// - is Field? or isVariable (does it matter?)
	// - yes, so we can compute if it is a field of a general type
	// - but we are also interested in local variables, for DF edges

	// TODO: Compute the total number of outliers that we handle/classify vs.
	// outliers.size()

	private int totalNumberOfOutliers = 0;

	public Q_Which_A(Writer writer, Set<Collection<IObject>> outliers, String shortMetricName) {
		this.writer = writer;
		this.outliers = outliers;
		this.shortMetricName = shortMetricName;
	}

	// DONE. Extract method to make it easier to convert the code into a visitor
	// that goes against AST
	@Override
	public void visit() {

		for (Collection<IObject> objects : outliers) {

			for (IObject object : objects) {
				if (object != null) {
					Type typeObject = object.getC();
					Which_A(typeObject);
				}
			}
		}

	}

	@Override
	public void display() throws IOException {

		// TODO: Output both total number of outliers (outliers.size()) and the
		// ones we handle/classify
		// - This will identify cases where our visitors are unable to process
		// the outliers

		QualUtils utils = QualUtils.getInstance();

		String path = utils.getPath();
		IProject project = utils.getProject();
		// Path to write Qual summary data
		String filePath = path + "\\" + CSVConst.R_Qual + project.getName() + "_" + shortMetricName + ".csv";

		writer.append(CSVConst.NEWLINE);
		writer.append("Results of Quantitative analysis");
		writer.append(CSVConst.NEWLINE);

		// TODO: SUM: Refactor the display.
		// Write all the outliers in the Data File
		for (Collection<IObject> objects : outliers) {

			for (IObject object : objects) {
				if (object != null) {
					Type typeObject = object.getC();
					Set<String> allSuperclassTypeObj = new HashSet<String>();
					Util.getSuperClasses(typeObject, allSuperclassTypeObj, true);
					if (utils.isContainerOfGeneralType(typeObject.getFullyQualifiedName())) {
						writer.append(typeObject.getFullyQualifiedName().toString());
						writer.append(CSVConst.COMMA);
						writer.append(object.getInstanceDisplayName());
						writer.append(CSVConst.COMMA);
						writer.append("Container of a General Type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isContainerType(typeObject.getFullyQualifiedName())) {
						writer.append(typeObject.getFullyQualifiedName().toString());
						writer.append(CSVConst.COMMA);
						writer.append(object.getInstanceDisplayName());
						writer.append(CSVConst.COMMA);
						writer.append("Container of a Type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isFrameworkType(typeObject.getFullyQualifiedName())) {
						writer.append(typeObject.getFullyQualifiedName().toString());
						writer.append(CSVConst.COMMA);
						writer.append(object.getInstanceDisplayName());
						writer.append(CSVConst.COMMA);
						writer.append("Application framework type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isapplicationType(typeObject.getFullyQualifiedName())) {
						writer.append(typeObject.getFullyQualifiedName().toString());
						writer.append(CSVConst.COMMA);
						writer.append(object.getInstanceDisplayName());
						writer.append(CSVConst.COMMA);
						writer.append("Application default type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isJavaTypes(typeObject.getFullyQualifiedName())
					        || allSuperclassTypeObj.contains(EXCEPTIONS)) {
						writer.append(typeObject.getFullyQualifiedName().toString());
						writer.append(CSVConst.COMMA);
						writer.append(object.getInstanceDisplayName());
						writer.append(CSVConst.COMMA);
						writer.append("Java framework type");
						writer.append(CSVConst.NEWLINE);
					}
					else if (utils.isexceptionType(typeObject.getFullyQualifiedName())
					        || allSuperclassTypeObj.contains(EXCEPTIONS)) {
						writer.append(typeObject.getFullyQualifiedName().toString());
						writer.append(CSVConst.COMMA);
						writer.append(object.getInstanceDisplayName());
						writer.append(CSVConst.COMMA);
						writer.append("Exception type");
						writer.append(CSVConst.NEWLINE);
					}
					else {
						writer.append(object.getC().getFullyQualifiedName().toString());
						writer.append(CSVConst.COMMA);
						writer.append(object.getInstanceDisplayName());
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
