package edu.wayne.metrics.qual;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import ast.Type;
import edu.wayne.metrics.adb.DFEdgePrecision.DFEdgePrecisionInfo;
import edu.wayne.metrics.adb.CustomWriter;
import edu.wayne.metrics.adb.EdgeInfo;
import edu.wayne.metrics.utils.CSVConst;

public class Q_DFEP extends ClassifyUtlis {

	private int totalNumberOfOutliers = 0;

	private Writer writer;

	private Set<EdgeInfo> outliers;

	private String shortMetricName;

	public Q_DFEP(Writer writer, Set<EdgeInfo> outliers, String shortMetricName) {
		this.writer = writer;
		this.outliers = outliers;
		this.shortMetricName = shortMetricName;
	}

	@Override
	public void visit() {

		/*
		 * 1) We need LIST of Abstract classes and Interfaces 2) We need LIST of JAVA Framework/Library: Added in as a
		 * list for now 3) The fully qualified type of the field declaration of TYPEA
		 */

		QualUtils utils = QualUtils.getInstance();
		String fieldName = null;
		// String to store all the type names of the outliers
		String enclType = null;
		Type refType = null;

		// For every EdgeInfo in the outliers, pick the field of TYPEB

		for (EdgeInfo eg : outliers) {
			DFEdgePrecisionInfo epi = (DFEdgePrecisionInfo) eg;

			refType = epi.getTypeA();
			// Call the method from the classifyUtils
			if (refType != null) {
				DFEP(refType);
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
		String fieldName = null;
		Type enclType = null;

		// - This will identify cases where our visitors are unable to process
		// the outliers
		writer.append(CSVConst.NEWLINE);
		writer.append("Results of Quantitative analysis");
		writer.append(CSVConst.NEWLINE);

		for (EdgeInfo eg : outliers) {
			DFEdgePrecisionInfo epi = (DFEdgePrecisionInfo) eg;
			// Populating the fields and types
			// fieldName = epi.getFieldName();
			enclType = epi.getTypeA();
			if (utils.isContainerOfGeneralType(enclType.getFullyQualifiedName())) {
				// Write classification 2 of the outliers
				writer.append(epi.getTypeA().getFullyQualifiedName());
				writer.append(CSVConst.COMMA);
				writer.append("Containter of elements of a General Type");
				writer.append(CSVConst.NEWLINE);
			}

			else if (utils.isFieldOfAtLeastOneGeneralType(enclType.getFullyQualifiedName())) {
				// Write classification 1 of the outliers
				writer.append(epi.getTypeA().getFullyQualifiedName());
				writer.append(CSVConst.COMMA);
				writer.append("Field of General Type");
				writer.append(CSVConst.NEWLINE);
			}
			else if (utils.isContainerType(enclType.getFullyQualifiedName())) {
				writer.append(epi.getTypeA().getFullyQualifiedName());
				writer.append(CSVConst.COMMA);
				writer.append("Container of a Type");
				writer.append(CSVConst.NEWLINE);
			}

			else {
				writer.append(CSVConst.COMMA);
				writer.append("The classification is unknown");
				writer.append(CSVConst.NEWLINE);
			}

		}

		// The numbers from ClassifyUtils
		totalNumberOfOutliers = getNumFieldOfGeneralTypes() + getNumContainerOfInterfaces() + getNumContainerOfType()
		        + getUnknown();

		// Sanity Check
		writer.append(CSVConst.NEWLINE);
		writer.append("Total count of the outliers");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.totalNumberOfOutliers).toString());
		writer.append(CSVConst.NEWLINE);

		Writer qualSummaryWrite = new CustomWriter(filePath);

		/*qualSummaryWrite.append(CSVConst.NEWLINE);
		qualSummaryWrite.append("Summary of the Classification");
		qualSummaryWrite.append(CSVConst.NEWLINE);*/

		// The total count of Container of General Type - Classification 1
		qualSummaryWrite.append("Count of Container elements of a General Type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumContainerOfInterfaces()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of Field of a General Type - Classification 2

		qualSummaryWrite.append("Count of Field of a general Type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumFieldOfGeneralTypes()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of Field of a GT - Classification 3
		qualSummaryWrite.append("Count of container of a Type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumContainerOfType()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of unknown classification
		qualSummaryWrite.append("Count of unknown classification");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getUnknown()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		qualSummaryWrite.flush();
		qualSummaryWrite.close();

	}
}
