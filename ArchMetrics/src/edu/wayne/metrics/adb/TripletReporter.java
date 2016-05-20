package edu.wayne.metrics.adb;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import oog.itf.IObject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import edu.wayne.metrics.datamodel.CodeStructureInfo;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.ograph.OGraph;

public class TripletReporter {
	
	private static final String TRIPLET_TABLE_HEADER_OGRAPH = "ObjectA,TypeA,TypeA_raw,DomainD,DomainD_raw,ObjectB,TypeB,TypeB_raw";
	
	// TODO: Was ist das?  No need to export this raw data.
	private static final String TRIPLET_TRACEABILITY_TABLE_HEADER_OGRAPH = "ObjectA,TypeA,TypeA_raw,Traceablity information [Object-DeclaredType-DeclaringType-Line],DomainD,DomainD_raw,ObjectB,TypeB,TypeB_raw,Traceablity information [Object-DeclaredType-DeclaringType-Line]";
	
	// TODO: HIGH. Revisit this.
	private static final String EDGE_TRIPLETS_TABLE_HEADER_OGRAPH = "ObjectAsrc,TypeAsrc,DomainDsrc,ObjectBsrc,TypeBsrc,ObjectAdst,TypeAdst,DomainDdst,ObjectBdst,TypeBdst";
	
	private Set<ADBTriplet> allTriplets = new HashSet<ADBTriplet>();
	private Set<IObject> allObjects;

	// TODO: Refactor away.
	private IProject currentProject;
	private String projectName;
	private IPath location;
	
	private OGraph runtimeModel;
	
	// TODO: LOW. Move this option to someplace more prominent, into an Options class, etc.
	private boolean codeInfoEnabled = false;

	private List<MetricInfo> allMetrics = new ArrayList<MetricInfo>();

	
	public TripletReporter(IProject currentProject, OGraph runtimeModel, Set<ADBTriplet> allTriplets, Set<IObject> allObjects) {
		this.currentProject = currentProject;
		this.projectName = currentProject.getName();
		this.location = currentProject.getLocation();
		
		
		this.runtimeModel = runtimeModel;
		this.allTriplets = allTriplets;
		this.allObjects = allObjects;
	}
	
	// TODO: HIGH. Refactor this code some more to reduce duplication.
	public void tripletsReport(String oogMetricsTripletTable) throws IOException {
		// TODO: Maybe use interface instead of abstract base class.
		// NOTE: Right now, AbstractBaseClass is a generic one; so definitely, do not use the raw version
		
		// Exclude this trivial strategy from the summary
		// NOTE: Does not work well with TripletPair. Generates too many TripletPairs!
		// ClusterMetricBase objectTriplets = new ObjectTriplets();
		// objectTriplets.compute(allTriplets);
		// objectTriplets.displayCluster(this.location, this.projectName, oogMetricsTripletTable);
		// allMetrics.add(objectTriplets);

		SameTierDifferentPackage sameTierDifferentPackage = new SameTierDifferentPackage();
		sameTierDifferentPackage.compute(allObjects);
		sameTierDifferentPackage.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(sameTierDifferentPackage);
		
		SamePackageDifferentTier samePackageDifferentTier = new SamePackageDifferentTier();
		samePackageDifferentTier.compute(allObjects);
		samePackageDifferentTier.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(samePackageDifferentTier);

		ClusterMetricBase which_A_In_B_Raw = new Which_A_In_B_Raw();
		which_A_In_B_Raw.compute(allObjects);
		which_A_In_B_Raw.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(which_A_In_B_Raw);
		
		ClusterMetricBase which_A_In_B = new Which_A_In_B();
		which_A_In_B.compute(allObjects);
		which_A_In_B.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(which_A_In_B);
	
		ClusterMetricBase which_A_In_Which_B_Raw = new Which_A_In_Which_B_Raw();
		which_A_In_Which_B_Raw.compute(allObjects);
		which_A_In_Which_B_Raw.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(which_A_In_Which_B_Raw);
		
		ClusterMetricBase which_A_In_Which_B = new Which_A_In_Which_B();
		which_A_In_Which_B.compute(allObjects);
		which_A_In_Which_B.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(which_A_In_Which_B);

		ObjectSetStrategyBase allObjectTypes = new AllObjectTypes();
		allObjectTypes.compute(allObjects);
		allObjectTypes.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(allObjectTypes);
		
		ObjectSetStrategyBase inheritedDomains = new InheritedDomains();
		inheritedDomains.compute(allObjects);
		inheritedDomains.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(inheritedDomains);
		
		// NOTE: The following metrics use allObjects instead of allTriplets
		// TODO: Split into separate method?
		PulledObjects pulledObjects = new PulledObjects();
		pulledObjects.compute(allObjects);
		pulledObjects.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(pulledObjects);
		
		HowManyObjectsToNewC howManyObjectsToNewC = new HowManyObjectsToNewC();
		howManyObjectsToNewC.compute(allObjects);
		howManyObjectsToNewC.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(howManyObjectsToNewC);
		
		HowManyNewCToObject howManyNewCToObject = new HowManyNewCToObject();
		howManyNewCToObject.compute(allObjects);
		howManyNewCToObject.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(howManyNewCToObject);		
		
		// XXX. Changed HowManyNewCToObject to HowManyTypesMergedByObject for calling HowManyTypesMergedByObject()
		HowManyTypesMergedByObject howManyTypesMergedByObject = new HowManyTypesMergedByObject();
		howManyTypesMergedByObject.compute(allObjects);
		howManyTypesMergedByObject.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(howManyTypesMergedByObject);
		
		// CUT: Not using EBO for now
//		ObjectSetStrategyBase weightOfEdgeBetweenTwoObjects = new WeightOfEdgeBetweenTwoObjects(this.runtimeModel.getEdges());
//		weightOfEdgeBetweenTwoObjects.compute(allObjects);
//		weightOfEdgeBetweenTwoObjects.display(location, projectName, oogMetricsTripletTable);
//		allMetrics.add(weightOfEdgeBetweenTwoObjects);
		
		HowManyTypesInObjectSubTree howManyTypesInObjectsSubTree = new HowManyTypesInObjectSubTree();
		howManyTypesInObjectsSubTree.compute(allObjects);
		howManyTypesInObjectsSubTree.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(howManyTypesInObjectsSubTree);
		
		Which_A_Raw which_A_Raw = new Which_A_Raw();
		which_A_Raw.compute(allObjects);
		which_A_Raw.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(which_A_Raw);

		Which_A which_A = new Which_A();
		which_A.compute(allObjects);
		which_A.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(which_A);
		
		// DO NOT use. Deprecated/Hackish.
		// ObjectMetricBase pulledObjectsAlt = new PulledObjectsAlt();
		// pulledObjectsAlt.compute(allObjects);
		// pulledObjectsAlt.displayCluster(location, projectName, oogMetricsTripletTable);
		// allMetrics.add(pulledObjectsAlt);
		
		ScatteringObjects scatteringObjects = new ScatteringObjects();
		scatteringObjects.compute(allObjects);
		scatteringObjects.display(location, projectName, oogMetricsTripletTable);
		allMetrics.add(scatteringObjects);
	}
	
	// TODO: HIGH. This is largely useless. Very wordy/verbose display of information.
	// TODO: HIGH. Move method.
	public void tripletsWithTraceabilityReport(String oogMetricsTripletTable) throws IOException {
		String projectName = currentProject.getName();
		IPath location = currentProject.getLocation();
		
		String oogTripletWithTraceabilityTablePath = location.append(projectName + oogMetricsTripletTable).toOSString();
		displayTripletWithTraceabilitySet(oogTripletWithTraceabilityTablePath, TRIPLET_TRACEABILITY_TABLE_HEADER_OGRAPH, allTriplets);
	}
	
	public void edgeTripletsReport(String oogMetricsEdgeTripletsTable) throws IOException {
		// Exclude trivial metric
		// EdgeMetricBase edgeMetric0 = new EdgeTriplets();
		// edgeMetric0.compute(runtimeModel.getEdges());
		// edgeMetric0.displayEdgeMetric(location, projectName, oogMetricsEdgeTripletsTable);
		// allMetrics.add(edgeMetric0);
		
		EdgeMetricBase edgeMetric0 = new AllEdgeTypes();
		edgeMetric0.compute(runtimeModel.getEdges());
		edgeMetric0.display(location, projectName, oogMetricsEdgeTripletsTable);
		allMetrics.add(edgeMetric0);
		
		EdgeMetricBase edgeMetric1 = new EdgeInheritance();
		edgeMetric1.compute(runtimeModel.getEdges());
		edgeMetric1.display(location, projectName, oogMetricsEdgeTripletsTable);
		allMetrics.add(edgeMetric1);

		EdgeMetricBase edgeMetric2 = new EdgePrecision();
		edgeMetric2.compute(runtimeModel.getEdges());
		edgeMetric2.display(location, projectName, oogMetricsEdgeTripletsTable);
		allMetrics.add(edgeMetric2);

		EdgeMetricBase edgeMetric3 = new HowManyEdgesToFieldDecl();
		edgeMetric3.compute(runtimeModel.getEdges());
		edgeMetric3.display(location, projectName, oogMetricsEdgeTripletsTable);
		allMetrics.add(edgeMetric3);
		
	    // Added DataEdgePrecision to compute DF Edges from the OGraph
		EdgeMetricBase edgeMetric4 = new DFEdgePrecision();
 	    edgeMetric4.compute(runtimeModel.getEdges());
		edgeMetric4.display(location, projectName, oogMetricsEdgeTripletsTable);
		allMetrics.add(edgeMetric4);
		
	    // Metric to compute MInE from the OGraph
		EdgeMetricBase edgeMetric5 = new HowManyEdgesToMethodInvok();
		edgeMetric5.compute(runtimeModel.getEdges());
		edgeMetric5.display(location, projectName, oogMetricsEdgeTripletsTable);
		allMetrics.add(edgeMetric5);
		
		// Metric to compute MInE_RecType from the OGraph
		EdgeMetricBase edgeMetric6 = new HowManyEdgesToMethodInvok_RecType();		
		edgeMetric6.compute(runtimeModel.getEdges());
		edgeMetric6.display(location, projectName, oogMetricsEdgeTripletsTable);
		allMetrics.add(edgeMetric6);
		
	
 	// Metric to compute MInE_RetType from the OGraph
		EdgeMetricBase edgeMetric7 = new HowManyEdgesToMethodInvok_RetType();		
		edgeMetric7.compute(runtimeModel.getEdges());
		edgeMetric7.display(location, projectName, oogMetricsEdgeTripletsTable);
		allMetrics.add(edgeMetric7);
		
		// Metric to compute MInE_ArgType from the OGraph
		EdgeMetricBase edgeMetric8 = new HowManyEdgesToMethodInvok_ArgType();		
		edgeMetric8.compute(runtimeModel.getEdges());
		edgeMetric8.display(location, projectName, oogMetricsEdgeTripletsTable);
		allMetrics.add(edgeMetric8);
		
		
		// Metric to compute FrnE from the OGraph
		EdgeMetricBase edgeMetric9 = new HowManyEdgesToFieldRead();
		edgeMetric9.compute(runtimeModel.getEdges());
		edgeMetric9.display(location, projectName, oogMetricsEdgeTripletsTable);
		allMetrics.add(edgeMetric9);
		
		
		// Metric to compute FrnE from the OGraph
		EdgeMetricBase edgeMetric10 = new HowManyEdgesToFieldRead_RecType();
		edgeMetric10.compute(runtimeModel.getEdges());
		edgeMetric10.display(location, projectName, oogMetricsEdgeTripletsTable);
		allMetrics.add(edgeMetric10);
		
		// Metric to compute FwnE from the OGraph
		EdgeMetricBase edgeMetric11 = new HowManyEdgesToFieldWrite();
		edgeMetric11.compute(runtimeModel.getEdges());
		edgeMetric11.display(location, projectName, oogMetricsEdgeTripletsTable);
		allMetrics.add(edgeMetric11);
		
		// Metric to compute FwnE_RecType from the OGraph
		EdgeMetricBase edgeMetric12 = new HowManyEdgesToFieldWrite_RecType();
		edgeMetric12.compute(runtimeModel.getEdges());
		edgeMetric12.display(location, projectName, oogMetricsEdgeTripletsTable);
		allMetrics.add(edgeMetric12);
	}

	public void writeSummary(String fileExtension)
			throws IOException {
		String summaryPath = location.append(CSVConst.R_PREFIX + projectName + "_"+ CSVConst.ALL_METRICS+ fileExtension).toOSString();
		FileWriter writer = new CustomWriter(summaryPath);
		writer.append("\"System\"");
		CodeStructureInfo codeStructureInfo = CodeStructureInfo.getInstance();
		
		if(codeInfoEnabled){
			//Write the code structure headers first
			codeStructureInfo.writeHeaders(writer);
		}
		// Write all the headers on one line
		for(MetricInfo metricInfo : allMetrics ) {
			metricInfo.writeHeaderToSummary(writer);
		}
	
		writer.append(CSVConst.NEWLINE);
		writer.append(CSVConst.DOUBLE_QUOTES);
		writer.append(projectName);
		writer.append(CSVConst.DOUBLE_QUOTES);
		
		if(codeInfoEnabled){
			//Write code structure metric data
			codeStructureInfo.writeMetrics(writer);
		}
		
		// Traverse the same list a second time, to write out the data
		// Write all the data on one line
		for(MetricInfo metricInfo : allMetrics ) {
			metricInfo.writeDataToSummary(writer);
		}
		
		// Add newline at the end of each row. Otherwise, R complains (warning only).
		writer.append(CSVConst.NEWLINE);
		
		// At the very end.
		writer.flush();
		writer.close();
	}

	// TODO: HIGH. This is largely useless. Very wordy/verbose display of information.
	// TODO: HIGH. Move method.	
	private void displayTripletWithTraceabilitySet(String oogTripletWithTraceabilityTablePath, String header, Set<ADBTriplet> set) throws IOException {		
		Writer writer = new CustomWriter(oogTripletWithTraceabilityTablePath);
		writer.append(header);
		
		if (set != null) {
			for (ADBTriplet triplet : set) {
				writer.append('\n');
				triplet.writeTo(writer);
			}
		}
		
		writer.flush();
		writer.close();
    }
}
