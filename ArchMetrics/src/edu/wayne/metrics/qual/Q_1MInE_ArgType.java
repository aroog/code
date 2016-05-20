package edu.wayne.metrics.qual;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import oog.itf.IElement;
import ast.MethodInvocation;
import ast.Type;
import edu.wayne.metrics.adb.CustomWriter;
import edu.wayne.metrics.adb.EdgeInfo;
import edu.wayne.metrics.adb.HowManyEdgesToMethodInvok_ArgType.HowManyEdges_MIArgType;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.CSVOutputUtils;
import edu.wayne.ograph.ODFEdge;

public class Q_1MInE_ArgType extends Q_Base {

	/*
	 * Brief on the classifications - Name the classifications the same across all metrics 1) Outliers when destination
	 * OObjects are many 2) Outliers when source OObjects are many 3) Outliers when both source OObjects and destination
	 * OObjects are many 4) Outliers when flow OObjects are many in different domains 5) Outliers when flow OObjects are
	 * many in same domain
	 */
	private int numInheritanceType1 = 0;

	private int numInheritanceType2 = 0;

	private int numInheritanceType3 = 0;

	private int numInheritanceType4 = 0;

	private int numInheritanceType5 = 0;

	private int unknown = 0;

	private int totalNumberOfOutliers = 0;

	private Writer writer;

	private Set<EdgeInfo> outliers;

	private String shortMetricName;

	public Q_1MInE_ArgType(Writer writer, Set<EdgeInfo> outliers, String shortMetricName) {
		this.writer = writer;
		this.outliers = outliers;
		this.shortMetricName = shortMetricName;
	}

	@Override
	public void visit() {

		Set<Type> sourceOObjects = new HashSet<Type>();
		Set<Type> destinationOObjects = new HashSet<Type>();
		Set<Type> flowOObjects = new HashSet<Type>();
		Set<List<String>> argDomains = new HashSet<List<String>>();
		// Traversing through the outliers
		for (EdgeInfo edgeInfo : outliers) {
			if (edgeInfo instanceof HowManyEdges_MIArgType) {
				HowManyEdges_MIArgType MI_edgeInfo = (HowManyEdges_MIArgType) edgeInfo;
				MethodInvocation methodInvocation = MI_edgeInfo.getMethodInvocation();
				Set<IElement> setEdges = MI_edgeInfo.getElems();
				for (IElement eachEdge : setEdges) {
					if (eachEdge instanceof ODFEdge) {
						Type sourceType = ((ODFEdge) eachEdge).getOsrc().getC();
						Type destType = ((ODFEdge) eachEdge).getOdst().getC();
						Type flowType = ((ODFEdge) eachEdge).getFlow().getC();
						// Collecting Source, Destination, Flow OObjects type
						sourceOObjects.add(sourceType);
						destinationOObjects.add(destType);
						flowOObjects.add(flowType);
						// Collecting the Argument annotations
						List<String> argumentAnnotations = methodInvocation.argumentAnnotations;
						argDomains.add(argumentAnnotations);
					}
				}
				// The classifications
				// Source OObjects <: receiver type of MI and destination OObjects <: enclosing type
				if (sourceOObjects.size() > 1 && destinationOObjects.size() > 1) {
					numInheritanceType3++;
				}
				// Destination OObjects <: enclosing type
				else if (sourceOObjects.size() == 1 && destinationOObjects.size() > 1) {
					numInheritanceType1++;
				}
				// Source OObjects <: receiver type
				else if (sourceOObjects.size() > 1 && destinationOObjects.size() == 1) {
					numInheritanceType2++;
				}
				// Flow OObjects <: return type when in different domains and same domains
				else if (flowOObjects.size() > 1) {
					numInheritanceType4++;
				}
				else if (flowOObjects.size() == 1 && argDomains.size() == 1) {
					numInheritanceType5++;
				}

				// Unknown classification
				else {
					unknown++;
				}
				sourceOObjects.clear();
				destinationOObjects.clear();
				flowOObjects.clear();

			}
		}

	}

	@Override
	public void display() throws IOException {

		// TOSUM: I am not outputting the edges for now.

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
		Set<Type> flowOObjects = new HashSet<Type>();
		Set<List<String>> argDomains = new HashSet<List<String>>();
		// Traversing through the outliers
		for (EdgeInfo edgeInfo : outliers) {
			if (edgeInfo instanceof HowManyEdges_MIArgType) {
				HowManyEdges_MIArgType MI_edgeInfo = (HowManyEdges_MIArgType) edgeInfo;
				MethodInvocation methodInvocation = MI_edgeInfo.getMethodInvocation();
				Set<IElement> setEdges = MI_edgeInfo.getElems();
				for (IElement eachEdge : setEdges) {
					if (eachEdge instanceof ODFEdge) {
						Type sourceType = ((ODFEdge) eachEdge).getOsrc().getC();
						Type destType = ((ODFEdge) eachEdge).getOdst().getC();
						Type flowType = ((ODFEdge) eachEdge).getFlow().getC();
						sourceOObjects.add(sourceType);
						destinationOObjects.add(destType);
						flowOObjects.add(flowType);
						List<String> argumentAnnotations = methodInvocation.argumentAnnotations;
						argDomains.add(argumentAnnotations);
					}
				}
				// Source OObjects <: receiver type of MI and destination OObjects <: enclosing type
				if (sourceOObjects.size() > 1 && destinationOObjects.size() > 1) {
					writer.append(CSVOutputUtils.sanitize(methodInvocation.toString()));
					writer.append(CSVConst.COMMA);
					writer.append("Inheritance type 3");
					writer.append(CSVConst.NEWLINE);
				}
				// Destination OObjects <: enclosing type
				else if (sourceOObjects.size() == 1 && destinationOObjects.size() > 1) {
					writer.append(CSVOutputUtils.sanitize(methodInvocation.toString()));
					writer.append(CSVConst.COMMA);
					writer.append("Inheritance type 1");
					writer.append(CSVConst.NEWLINE);
				}
				// Source OObjects <: receiver type
				else if (sourceOObjects.size() > 1 && destinationOObjects.size() == 1) {
					writer.append(CSVOutputUtils.sanitize(methodInvocation.toString()));
					writer.append(CSVConst.COMMA);
					writer.append("Inheritance type 2");
					writer.append(CSVConst.NEWLINE);
				}
				// Flow OObjects <: return type when in different domains and same domains
				else if (flowOObjects.size() > 1) {
					writer.append(CSVOutputUtils.sanitize(methodInvocation.toString()));
					writer.append(CSVConst.COMMA);
					writer.append("Inheritance type 4");
					writer.append(CSVConst.NEWLINE);
				}
				else if (flowOObjects.size() == 1 && argDomains.size() == 1) {
					writer.append(CSVOutputUtils.sanitize(methodInvocation.toString()));
					writer.append(CSVConst.COMMA);
					writer.append("Inheritance type 5");
					writer.append(CSVConst.NEWLINE);
				}

				// Unknown classification
				else {
					writer.append(CSVOutputUtils.sanitize(methodInvocation.toString()));
					writer.append(CSVConst.COMMA);
					writer.append("Unknown");
					writer.append(CSVConst.NEWLINE);
				}
				sourceOObjects.clear();
				destinationOObjects.clear();
				flowOObjects.clear();
				// argDomains.clear();
			}
		}

		totalNumberOfOutliers = numInheritanceType1 + numInheritanceType2 + numInheritanceType3 + numInheritanceType4
		        + numInheritanceType5 + unknown;

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

		// Total Count of Inheritance4- Classification 4
		qualSummaryWrite.append("Count of Inheritance Type 4");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(this.numInheritanceType4).toString());
		qualSummaryWrite.append(CSVConst.NEWLINE);

		// Total Count of Inheritance5- Classification 5
		qualSummaryWrite.append("Count of Inheritance Type 5");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(this.numInheritanceType5).toString());
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
