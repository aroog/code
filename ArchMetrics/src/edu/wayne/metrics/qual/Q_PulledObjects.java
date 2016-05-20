package edu.wayne.metrics.qual;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import ast.Type;

import edu.wayne.metrics.adb.ObjectInfo;
import edu.wayne.metrics.adb.PulledObjects.PulledObjectInfo;
import edu.wayne.metrics.utils.CSVConst;

public class Q_PulledObjects extends Q_Base {
	
	private int numFrameworktoFramework = 0;
	private int numFrameworktoApplication = 0;
	private int numApplicationtoApplication = 0;
	private int unknown = 0;
	private int totalNumberOfOutliers = 0;

	private Writer writer;
	private Set<ObjectInfo> allPulledObjs;

	public Q_PulledObjects(Writer writer, Set<ObjectInfo> allPulledObjs) {
		// TODO Auto-generated constructor stub
		this.writer = writer;
		this.allPulledObjs = allPulledObjs;
	}

	@Override
	public void visit() {	
		QualUtils utils = QualUtils.getInstance();
		// TODO Auto-generated method stub
		for (ObjectInfo eachPulledObj : allPulledObjs) {			
			if (eachPulledObj instanceof PulledObjectInfo) {
				PulledObjectInfo pulledObjInfo = (PulledObjectInfo) eachPulledObj;				
				Type pulledType = pulledObjInfo.getPulledintoType();
				Type objectType = pulledObjInfo.getObjectType();
				if (utils.isFrameworkType(
						objectType.getFullyQualifiedName())) {
					if (utils.isapplicationType(
							pulledType.getFullyQualifiedName())) {
						numFrameworktoApplication++;
					}
					if (utils.isFrameworkType(
							pulledType.getFullyQualifiedName())) {
						numFrameworktoFramework++;
					}
				}
				if (utils.isapplicationType(
						objectType.getFullyQualifiedName())) {
					if (utils.isapplicationType(
							pulledType.getFullyQualifiedName())) {
						numApplicationtoApplication++;
					}
				}

			}

		}
	}

	@Override
	public void display() throws IOException {
		// TODO Auto-generated method stub

		QualUtils utils = QualUtils.getInstance();

		writer.append(CSVConst.NEWLINE);
		writer.append("Results of Quantitative analysis");
		writer.append(CSVConst.NEWLINE);

		for (ObjectInfo eachPulledObj : allPulledObjs) {
			if (eachPulledObj instanceof PulledObjectInfo) {
				PulledObjectInfo pulledObjInfo = (PulledObjectInfo) eachPulledObj;
				Type declaredType = pulledObjInfo.getDeclaredType();
				Type pulledType = pulledObjInfo.getPulledintoType();
				Type objectType = pulledObjInfo.getObjectType();
				if (utils.isFrameworkType(
						objectType.getFullyQualifiedName())) {
					if (utils.isapplicationType(
							pulledType.getFullyQualifiedName())) {
						writer.append(objectType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(declaredType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(pulledType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append("Frame work type pulled into application type");
						writer.append(CSVConst.NEWLINE);
					}
					if (utils.isFrameworkType(
							pulledType.getFullyQualifiedName())) {
						writer.append(objectType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(declaredType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(pulledType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append("Frame work type pulled into frame work type");
						writer.append(CSVConst.NEWLINE);
					}
				}
				if (utils.isapplicationType(
						objectType.getFullyQualifiedName())) {
					if (utils.isapplicationType(
							pulledType.getFullyQualifiedName())) {
						writer.append(objectType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(declaredType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append(pulledType.getFullyQualifiedName());
						writer.append(CSVConst.COMMA);
						writer.append("Application type pulled into application type");
						writer.append(CSVConst.NEWLINE);
					}

				}

			}
		}
		writer.append(CSVConst.NEWLINE);
		writer.append("Summary of the Classification");
		writer.append(CSVConst.NEWLINE);

		// The total count of Frame work(Application specific) to Frame
		// work(General Java) pulled objects link - Classification 1
		writer.append("Total count of Frame work type pulled into other framework type");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numFrameworktoFramework).toString());
		writer.append(CSVConst.NEWLINE);

		// The total count of Frame work(Application specific) to another
		// Application class pulled objects - Classification 2

		writer.append("Total count of framework type pulled into application type");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numFrameworktoApplication)
				.toString());
		writer.append(CSVConst.NEWLINE);

		// The total count of Application specific classes that link to another
		// application specific classes - Classification 3

		writer.append("Total count of Application type pulled into application type");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.numApplicationtoApplication)
				.toString());
		writer.append(CSVConst.NEWLINE);
		
		
		// Total count of unknow classifiation

		writer.append("Total count of unknown classification");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.unknown)
						.toString());
		writer.append(CSVConst.NEWLINE);
		
		totalNumberOfOutliers = numApplicationtoApplication + numFrameworktoApplication + numFrameworktoFramework + unknown;

		// Sanity Check
		writer.append(CSVConst.NEWLINE);
		writer.append("Total count of the outliers");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.totalNumberOfOutliers).toString());
		writer.append(CSVConst.NEWLINE);

	}

}
