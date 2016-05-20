package edu.wayne.metrics.qual;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;

import oog.itf.IElement;
import ast.FieldDeclaration;
import ast.Type;
import ast.TypeDeclaration;
import edu.wayne.metrics.adb.CustomWriter;
import edu.wayne.metrics.adb.EdgeInfo;
import edu.wayne.metrics.adb.HowManyEdgesToFieldDecl.HowManyEdgesInfo;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.ograph.OPTEdge;
import edu.wayne.metrics.adb.Util;

public class Q_1FnE extends Q_Base {
	private int numInheritanceType1 = 0;

	private int numInheritanceType2 = 0;

	private int numComposition = 0;

	private int unknown = 0;

	private int totalNumberOfOutliers = 0;

	private Writer writer;

	private Set<EdgeInfo> outliers;

	private String shortMetricName;

	private HashMap<String, String> fieldTypeSource = new HashMap<String, String>();

	public Q_1FnE(Writer writer, Set<EdgeInfo> outliers, String shortMetricName) {
		this.writer = writer;
		this.outliers = outliers;
		this.shortMetricName = shortMetricName;
	}

	// Collect all the sources and fieldTypes

	public HashMap<String, String> getAllFieldTypeSource() {
		for (EdgeInfo eginfo : outliers) {
			if (eginfo instanceof HowManyEdgesInfo) {
				HowManyEdgesInfo egInfo = (HowManyEdgesInfo) eginfo;
				FieldDeclaration fieldDeclr = egInfo.getFieldDeclaration();
				Type fieldType = fieldDeclr.fieldType;
				Set<IElement> setEdges = egInfo.getElems();
				Type sourceType = null;
				for (IElement eachEdge : setEdges) {
					if (eachEdge instanceof OPTEdge) {
						sourceType = ((OPTEdge) eachEdge).getOsrc().getC();
						fieldTypeSource.put(fieldType.getFullyQualifiedName(), sourceType.getFullyQualifiedName());

					}
				}
			}
		}
		return fieldTypeSource;
	}

	@Override
	public void visit() {
		QualUtils utils = QualUtils.getInstance();
		// Traversing through the outliers
		for (EdgeInfo eginfo : outliers) {

			if (eginfo instanceof HowManyEdgesInfo) {
				HowManyEdgesInfo egInfo = (HowManyEdgesInfo) eginfo;
				FieldDeclaration fieldDeclr = egInfo.getFieldDeclaration();
				TypeDeclaration enclosingTypeDeclr = fieldDeclr.enclosingType;
				Type enclosingType = enclosingTypeDeclr.type;
				Type fieldType = fieldDeclr.fieldType;
				String destinationType = null;

				Set<String> superclassDestType = new HashSet<String>();
				Set<String> superclassSourceType = new HashSet<String>();
				Set<String> superclassEnclosingType = new HashSet<String>();
				Set<String> superclassFieldType = new HashSet<String>();
				Set<String> subclassEnclosingType = new HashSet<String>();

				Type sourceType = null;
				Type destType = null;
				Set<IElement> setEdges = egInfo.getElems();
				for (IElement eachEdge : setEdges) {
					if (eachEdge instanceof OPTEdge) {
						sourceType = ((OPTEdge) eachEdge).getOsrc().getC();
						destType = ((OPTEdge) eachEdge).getOdst().getC();
						destinationType = destType.getFullyQualifiedName();
						Util.getSuperClasses(sourceType, superclassSourceType, true);
						Util.getSuperClasses(destType, superclassDestType, true);
						Util.getSuperClasses(enclosingType, superclassEnclosingType, true);
						Util.getSuperClasses(fieldType, superclassFieldType, true);
						Util.getSubClasses(enclosingType, subclassEnclosingType);

						if (utils.isFieldOfAtLeastOneGeneralType(fieldType.getFullyQualifiedName())) {
							// Inheritance 1
							if (superclassDestType.contains(destType.getFullyQualifiedName())
							        && sourceType == enclosingType) {
								numInheritanceType1++;

							}
							// Inheritance 2
							else if (subclassEnclosingType.contains(sourceType.getFullyQualifiedName())
							        && superclassDestType.contains(destType.getFullyQualifiedName())) {
								numInheritanceType2++;
							}

						}

						else if (fieldType == destType
						        || superclassDestType.contains(fieldType.getFullyQualifiedName())) {
							for (Map.Entry eachFieldforSource : getAllFieldTypeSource().entrySet()) {
								if (destinationType.equals(eachFieldforSource.getValue())) {
									Object otherFields = eachFieldforSource.getKey();
									if (utils.isFieldOfAtLeastOneGeneralType(otherFields.toString())) {
										numComposition++;
									}
								}
							}

						}
						else {
							unknown++;
						}
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

		for (EdgeInfo eginfo : outliers) {

			if (eginfo instanceof HowManyEdgesInfo) {
				HowManyEdgesInfo egInfo = (HowManyEdgesInfo) eginfo;
				FieldDeclaration fieldDeclr = egInfo.getFieldDeclaration();
				TypeDeclaration enclosingTypeDeclr = fieldDeclr.enclosingType;
				Type enclosingType = enclosingTypeDeclr.type;
				Type fieldType = fieldDeclr.fieldType;
				String destinationType = null;
				Set<String> superclassDestType = new HashSet<String>();
				Set<String> superclassSourceType = new HashSet<String>();
				Set<String> superclassEnclosingType = new HashSet<String>();
				Set<String> superclassFieldType = new HashSet<String>();
				Set<String> subclassEnclosingType = new HashSet<String>();

				Type sourceType = null;
				Type destType = null;
				Set<IElement> setEdges = egInfo.getElems();
				for (IElement eachEdge : setEdges) {
					if (eachEdge instanceof OPTEdge) {
						sourceType = ((OPTEdge) eachEdge).getOsrc().getC();
						destType = ((OPTEdge) eachEdge).getOdst().getC();
						destinationType = destType.getFullyQualifiedName();
						Util.getSuperClasses(sourceType, superclassSourceType, true);
						Util.getSuperClasses(destType, superclassDestType, true);
						Util.getSuperClasses(enclosingType, superclassEnclosingType, true);
						Util.getSuperClasses(fieldType, superclassFieldType, true);
						Util.getSubClasses(enclosingType, subclassEnclosingType);

						if (utils.isFieldOfAtLeastOneGeneralType(fieldType.getFullyQualifiedName())) {
							// Inheritance 1
							if (superclassDestType.contains(destType.getFullyQualifiedName())
							        && sourceType == enclosingType) {
								enclosingType = enclosingTypeDeclr.type;
								fieldType = fieldDeclr.fieldType;
								writer.append(enclosingType.getFullyQualifiedName());
								writer.append(CSVConst.COMMA);
								writer.append(destType.getFullyQualifiedName());
								writer.append(CSVConst.COMMA);
								writer.append(fieldType.getFullyQualifiedName());
								writer.append(CSVConst.COMMA);
								writer.append("Inheritance Type 1");
								writer.append(CSVConst.NEWLINE);
							}
							// Inheritance 2
							else if (subclassEnclosingType.contains(sourceType.getFullyQualifiedName())
							        && superclassDestType.contains(destType.getFullyQualifiedName())) {
								enclosingType = enclosingTypeDeclr.type;
								fieldType = fieldDeclr.fieldType;

								writer.append(enclosingType.getFullyQualifiedName());
								writer.append(CSVConst.COMMA);
								writer.append(destType.getFullyQualifiedName());
								writer.append(CSVConst.COMMA);
								writer.append(fieldType.getFullyQualifiedName());
								writer.append(CSVConst.COMMA);
								writer.append("Inheritance Type 2");
								writer.append(CSVConst.NEWLINE);
							}

						}
						else if (fieldType == destType
						        || superclassDestType.contains(fieldType.getFullyQualifiedName())) {
							for (Map.Entry entry : getAllFieldTypeSource().entrySet()) {
								if (destinationType.equals(entry.getValue())) {
									Object eachFieldforSource = entry.getKey();
									if (utils.isFieldOfAtLeastOneGeneralType(eachFieldforSource.toString())) {
										writer.append(enclosingType.getFullyQualifiedName());
										writer.append(CSVConst.COMMA);
										writer.append(destType.getFullyQualifiedName());
										writer.append(CSVConst.COMMA);
										writer.append(fieldType.getFullyQualifiedName());
										writer.append(CSVConst.COMMA);
										writer.append("Composition");
										writer.append(CSVConst.NEWLINE);
									}

								}
							}

						}
						else {
							enclosingType = enclosingTypeDeclr.type;
							fieldType = fieldDeclr.fieldType;

							writer.append(enclosingType.getFullyQualifiedName());
							writer.append(CSVConst.COMMA);
							writer.append(destType.getFullyQualifiedName());
							writer.append(CSVConst.COMMA);
							writer.append(fieldType.getFullyQualifiedName());
							writer.append(CSVConst.COMMA);
							writer.append("Unknown Classification");
							writer.append(CSVConst.NEWLINE);
						}

					}
				}
			}

		}
		// EdgeInfo einfo = null;

		totalNumberOfOutliers = numComposition + numInheritanceType1 + numInheritanceType2 + unknown;

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

		// Total Count of composition - Classification 3
		qualSummaryWrite.append("Count of composition");
		qualSummaryWrite.append(CSVConst.COMMA);
		qualSummaryWrite.append(Integer.valueOf(this.numComposition).toString());
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
