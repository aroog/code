package edu.wayne.metrics.qual;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import oog.itf.IElement;
import ast.MethodInvocation;
import ast.Type;
import edu.wayne.metrics.adb.CustomWriter;
import edu.wayne.metrics.adb.EdgeInfo;
import edu.wayne.metrics.adb.HowManyEdgesToMethodInvok_RecType.HowManyEdges_MIRecType;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.CSVOutputUtils;
import edu.wayne.ograph.ODFEdge;

public class Q_1MInE_RecType extends Q_Base {
	
	/*
	 * Brief on the classifications - Name the classifications the same across all metrics 
	 * 1) Outliers when destination OObjects are many 
	 * 2) Outliers when source OObjects are many 
	 * 3) Outliers when both source OObjects and destination OObjects are many 
	 * 4) Outliers when flow OObjects are many
	 * 
	 */
	
	private int numInheritanceType1 = 0;

	private int numInheritanceType2 = 0;

	private int numInheritanceType3 = 0;

	private int unknown = 0;

	private int totalNumberOfOutliers = 0;

	private Writer writer;

	private Set<EdgeInfo> outliers;
	
	private String shortMetricName;

	public Q_1MInE_RecType(Writer writer, Set<EdgeInfo> outliers, String shortMetricName) {
		this.writer = writer;
		this.outliers = outliers;
		this.shortMetricName = shortMetricName;
	}

	@Override
	public void visit() {

		Set<Type> sourceOObjects = new HashSet<Type>();
		Set<Type> destinationOObjects = new HashSet<Type>();
		// Traversing through the outliers
		for (EdgeInfo edgeInfo : outliers) {
			if (edgeInfo instanceof HowManyEdges_MIRecType) {
				HowManyEdges_MIRecType MI_edgeInfo = (HowManyEdges_MIRecType) edgeInfo;				
				Set<IElement> setEdges = MI_edgeInfo.getElems();
				for (IElement eachEdge : setEdges) {
					if (eachEdge instanceof ODFEdge) {
						Type sourceType = ((ODFEdge) eachEdge).getOsrc().getC();
						Type destType = ((ODFEdge) eachEdge).getOdst().getC();
						sourceOObjects.add(sourceType);
						destinationOObjects.add(destType);
					}
				}
				// Source OObjects <: enclosing type
				if (sourceOObjects.size() > 1 && destinationOObjects.size() == 1) {
					numInheritanceType2++;					
				}
				// Source OObjects <: enclosing type && destination OObjects <: receiver
				else if (sourceOObjects.size() > 1 && destinationOObjects.size() > 1) {
					numInheritanceType3++;					
				}
				// Destination OObjects <: receiver
				else if (sourceOObjects.size() == 1 && destinationOObjects.size() > 1) {
					numInheritanceType1++;					
				}
				else {
					unknown++;					
				}				
				sourceOObjects.clear();
				destinationOObjects.clear();
			}			
		}
	}

	@Override
	public void display() throws IOException {
		
		//TOSUM: I am not outputting the edges. I understand the output but will I be able to explain it to others
		//without the number of edges?
		/*
		 * The number of edges associated with 1MInE_RecType for DL is about say 600 but the Qual classification says only 33 
		 * Because the number of MIs are only 33. This is correct!
		 */

		QualUtils utils = QualUtils.getInstance();

		String path = utils.getPath();
		IProject project = utils.getProject();
		// Path to write Qual summary data
		String filePath = path + "\\" + CSVConst.R_Qual + project.getName() + "_" + shortMetricName + ".csv";
		
		writer.append(CSVConst.NEWLINE);
		writer.append("Results of Quantitative analysis");
		writer.append(CSVConst.NEWLINE);

		Set<Type> sourceOObjects = new HashSet<Type>();
		Set<Type> destinationOObjects = new HashSet<Type>();
		// Traversing through the outliers
		for (EdgeInfo edgeInfo : outliers) {
			if (edgeInfo instanceof HowManyEdges_MIRecType) {
				HowManyEdges_MIRecType MI_edgeInfo = (HowManyEdges_MIRecType) edgeInfo;
				MethodInvocation methodInvocation = MI_edgeInfo.getMethodInvocation();
				Set<IElement> setEdges = MI_edgeInfo.getElems();
				for (IElement eachEdge : setEdges) {
					if (eachEdge instanceof ODFEdge) {
						Type sourceType = ((ODFEdge) eachEdge).getOsrc().getC();
						Type destType = ((ODFEdge) eachEdge).getOdst().getC();
						sourceOObjects.add(sourceType);
						destinationOObjects.add(destType);
					}
				}				
				
				if (sourceOObjects.size() > 1 && destinationOObjects.size() == 1) {
					writer.append(CSVOutputUtils.sanitize(methodInvocation.toString()));
					writer.append(CSVConst.COMMA);
					writer.append("Inheritance type 2");
					writer.append(CSVConst.NEWLINE);
				}
				else if (sourceOObjects.size() > 1 && destinationOObjects.size() > 1) {
					writer.append(CSVOutputUtils.sanitize(methodInvocation.toString()));
					writer.append(CSVConst.COMMA);
					writer.append("Inheritance type 3");
					writer.append(CSVConst.NEWLINE);
				}
				else if (sourceOObjects.size() == 1 && destinationOObjects.size() > 1) {
					writer.append(CSVOutputUtils.sanitize(methodInvocation.toString()));
					writer.append(CSVConst.COMMA);
					writer.append("Inheritance type 1");
					writer.append(CSVConst.NEWLINE);					
				}
				else {
					writer.append(CSVOutputUtils.sanitize(methodInvocation.toString()));
					writer.append(CSVConst.COMMA);
					writer.append("Unknown");
					writer.append(CSVConst.NEWLINE);
				}
				sourceOObjects.clear();
				destinationOObjects.clear();
			}
		}

		totalNumberOfOutliers = numInheritanceType1 + numInheritanceType2 + numInheritanceType3 + unknown;

		// Sanity Check
		writer.append(CSVConst.NEWLINE);
		writer.append("Total count of the outliers");
		writer.append(CSVConst.COMMA);
		writer.append(Integer.valueOf(this.totalNumberOfOutliers).toString());
		writer.append(CSVConst.NEWLINE);
		
		// The summary report

		Writer qualSummaryWrite = new CustomWriter(filePath);
		
		/*
		qualSummaryWrite.append(CSVConst.NEWLINE);
		qualSummaryWrite.append("Summary of the Classification");
		qualSummaryWrite.append(CSVConst.NEWLINE);*/

		// Total Count of Inheritance1 - Classification 1
		qualSummaryWrite.append("Count of Inheritance Type 1");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(this.numInheritanceType1).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// Total Count of Inheritance2- Classification 2
		qualSummaryWrite.append("Count of Inheritance Type 2");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(this.numInheritanceType2).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// Total Count of Inheritance3- Classification 3
		qualSummaryWrite.append("Count of Inheritance Type 3");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(this.numInheritanceType3).toString());
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
