package edu.wayne.metrics.qual;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import oog.itf.IObject;
import ast.Type;
import edu.wayne.metrics.adb.ADBTriplet;
import edu.wayne.metrics.adb.CustomWriter;
import edu.wayne.metrics.adb.ObjectInfo;
import edu.wayne.metrics.adb.ScatteringObjects.ScatteredObjectInfo;
import edu.wayne.metrics.utils.CSVConst;

public class Q_SO extends ClassifyUtlis {

	private Writer writer;
	private Set<ObjectInfo> outliers;

	// Total count of classification of outliers
	private int totalNumberOfOutliers = 0;
	
	private String shortMetricName;

	public Q_SO(Writer writer, Set<ObjectInfo> outliers, String shortMetricName) {
		// TODO Auto-generated constructor stub
		this.writer = writer;
		this.outliers = outliers;
		this.shortMetricName = shortMetricName;
	}

	@Override
	public void visit() {
		for (ObjectInfo oInfo : outliers) {
			if (oInfo instanceof ScatteredObjectInfo) {
				ScatteredObjectInfo oInf = (ScatteredObjectInfo) oInfo;
				ADBTriplet triletObj = oInf.getTriplet();
				IObject objectSO = triletObj.getObjectA();
				Type typeSO = objectSO.getC();
				SO(typeSO);

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

		for (ObjectInfo oInfo : outliers) {
			if (oInfo instanceof ScatteredObjectInfo) {
				ScatteredObjectInfo oInf = (ScatteredObjectInfo) oInfo;
				ADBTriplet triletObj = oInf.getTriplet();
				IObject objectSO = triletObj.getObjectA();
				Type typeSO = objectSO.getC();
				if (utils.isexceptionType((typeSO.getFullyQualifiedName()))) {
					writer.append(objectSO.getTypeDisplayName());
					writer.append(CSVConst.COMMA);
					writer.append(typeSO.getFullyQualifiedName());
					writer.append(CSVConst.COMMA);
					writer.append("Exception type");
					writer.append(CSVConst.NEWLINE);
				} else if (utils.isdataType(typeSO.getFullyQualifiedName())) {
					writer.append(objectSO.getTypeDisplayName());
					writer.append(CSVConst.COMMA);
					writer.append(typeSO.getFullyQualifiedName());
					writer.append(CSVConst.COMMA);
					writer.append("Data type");
					writer.append(CSVConst.NEWLINE);
				} else if (utils.isFrameworkType(typeSO
						.getFullyQualifiedName())) {
					writer.append(objectSO.getTypeDisplayName());
					writer.append(CSVConst.COMMA);
					writer.append(typeSO.getFullyQualifiedName());
					writer.append(CSVConst.COMMA);
					writer.append("Application framework type");
					writer.append(CSVConst.NEWLINE);
				} else {
					writer.append(objectSO.getTypeDisplayName());
					writer.append(CSVConst.COMMA);
					writer.append(typeSO.getFullyQualifiedName());
					writer.append(CSVConst.COMMA);
					writer.append("Classification is unknown");
					writer.append(CSVConst.NEWLINE);
				}
			}

		}
		

		totalNumberOfOutliers = getNumExceptionClass() + getNumDataClass()
					+ getNumofAppFrameworkTypes() + getUnknown();

		// Sanity Check
		writer.append(CSVConst.NEWLINE);
		writer.append("Total count of the outliers");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.totalNumberOfOutliers).toString());
		writer.append(CSVConst.NEWLINE);
		

		// The summary report

		Writer qualSummaryWrite = new CustomWriter(filePath);

	/*	// Using getNumber methods from 
		qualSummaryWrite.append(CSVConst.NEWLINE);
		qualSummaryWrite.append("Summary of the Classification");
		qualSummaryWrite.append(CSVConst.NEWLINE);*/

		// The total count of Classification 1
		qualSummaryWrite.append("Count of Exception type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumExceptionClass()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of Classification 2
		writer.append("Count of Data type");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(getNumDataClass()).toString());
		writer.append(CSVConst.NEWLINE);

		// The total count of Classification 3
		qualSummaryWrite.append("Count of Application framework type");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getNumofAppFrameworkTypes())
				.toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// The total count of unknown classification
		qualSummaryWrite.append("Count of Unknown classification");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(getUnknown()).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);
		
		qualSummaryWrite.flush();
		qualSummaryWrite.close();


	}
}
