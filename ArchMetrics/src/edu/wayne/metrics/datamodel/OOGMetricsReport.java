package edu.wayne.metrics.datamodel;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import edu.wayne.metrics.utils.CSVOutputUtils;

public class OOGMetricsReport {

	private static final String GRAPH_METRICS = "COOG,MXD,AVGD,PTLO,#LLOTLD,LLOD,AVG PDO,MIN PDO,MAX PDO,STDEV PDO, AVG PrDO,MIN PrDO,MAX PrDO,STDEV PrDO,AVG IDTLD,MIN IDTLD,MAX IDTLD,STDEV IDTLD,AVG IDPD,MIN IDPD,MAX IDPD,STDEV IDPD,AVG IDPrD,MIN IDPrD,MAX IDPrD,STDEV IDPrD,#OC,AVG DOC,AOD,MIN OD,MAX OD,STDEV OD,AVG TCO,MIN TCO,MAX TCO,STDEV TCO,AVG TCE,AOS,MIN OS,MAX OS,STDEV OS,CTO,MIN CTO, MAX CTO, STDEV CTO,ABTF,TLABTF,ABHTF"; // if

	// change
	// AVG
	// OD
	// -
	// change
	// in
	// the
	// OOGMetrics
	// as
	// well.
	private static final String GRAPH_METRIC_HEADER = "Node,NodeType,Depth,inPtDegree,outPtDegree,inOusidePtDegree,inOwnDegree,outOwnDegree,noPublicDoms,noPrivateDoms,isInPublicDom,qualifiedTypeName,noTraceabilityLinks,simpleTypeName,noUniqueFilesInTraceabilityLinks";
	
	private static final String GRAPH_SIZE_METRIC = "#O,#TLO,#OPD,#OPrD,#D,#TLD,#PD,#PrD,#PtE";

	public static final String TABLE_HEADER_DGRAPH = "System," + GRAPH_SIZE_METRIC + "," + GRAPH_METRICS;

	public Hashtable<String, Double> oogResults;

	public Hashtable<String, Long> graphMetrics;

	private FileWriter writer;

	private IProject currentProject;

	private String oogTablePath;

	private String oogMetricsPath;

	private Hashtable<String, GraphMetricItem> listOfObjects;

	private Hashtable<String, GraphMetricItem> listOfDomains;

	private long edgesSize;

	private long totalTCE;

	private long treeDepth;

	public double abtf;

	public double tlabtf;

	public long tlo;

	public SummaryStatistics objectsInDomains;

	public double abhtf;

	public OOGMetricsReport(IProject currentProject, Hashtable<String, GraphMetricItem> listOfObjects,
	        Hashtable<String, GraphMetricItem> listOfDomains, long noOfPtEdges,
	        long totalTCE) {
		this.currentProject = currentProject;
		this.listOfObjects = listOfObjects;
		this.listOfDomains = listOfDomains;
		
		// TODO: HIGH. XXX. Careful: 
		// This latest version of "runtime model" now has DF edges in addition to PT Edges.
		// The previous metrics code (LNCS) dealt with only PT edges.
		// Need to filter out by Edge Type from the set of edges.
		this.edgesSize = noOfPtEdges;
		this.totalTCE = totalTCE;
		treeDepth = getMaxTreeDepth();
	}

	public void oObjectsReport(String oogMetrics, String oogMetricsTable)
	        throws IOException {

		String projectName = currentProject.getName();
		IPath location = currentProject.getLocation();
		
		oogMetricsPath = location.append(projectName + oogMetrics).toOSString();
		oogTablePath = location.append(projectName + oogMetricsTable).toOSString();
		
		CSVOutputUtils.writeTableHeaderToFile(oogTablePath, TABLE_HEADER_DGRAPH + "\n");
		writer = new FileWriter(oogMetricsPath);
		writer.append(GRAPH_METRIC_HEADER);

		for (GraphMetricItem str : listOfObjects.values()) {
			writer.append('\n');
			writer.append(str.toString());
			writer.append(",\"");
			writer.append(str.declaredTypeName);
			writer.append("\"," + str.noTrLinks);
			writer.append(",\"");
			writer.append(str.displayTypeName + "\"");
			writer.append("," + str.objectScattering);
		}
		for (GraphMetricItem str : listOfDomains.values()) {
			writer.append('\n');
			writer.append(str.toString());
		}
		writer.flush();
		writer.close();

		CSVOutputUtils.appendRowToFile(oogTablePath, currentProject.getName(), computeStats());
	}
	

	private String computeStats() {
		graphMetrics = new Hashtable<String, Long>();
		oogResults = new Hashtable<String, Double>();

		int size_O = listOfObjects.size();
		tlo = noOfObjectsDepth(2);
		long noOfTLO = tlo;
		int size_D = listOfDomains.size();
		long noOfTLD = noOfDomainsDepth(2);
		long noOfPublicD = noOfDomainsType(NodeType.PD) - noOfTLD;
		int noOfPrivateD = noOfDomainsType(NodeType.PrD);
		//long noOfOPD = objectsIDPublicD().getN();
		long noOfOPD = noOfOinD(true);
		
		
		//long noOfOPrD = objectsIDPrivateD().getN();
		long noOfOPrD = noOfOinD(false);

		// "#O,#TLO,#D,#TLD,#public d,#private d,#PtE,";
		String[] token = GRAPH_SIZE_METRIC.split(",");
		int i = 0;
		graphMetrics.put(token[i++], new Long(size_O));
		graphMetrics.put(token[i++], new Long(noOfTLO));
		graphMetrics.put(token[i++], new Long(noOfOPD));
		graphMetrics.put(token[i++], new Long(noOfOPrD));
		graphMetrics.put(token[i++], new Long(size_D));
		graphMetrics.put(token[i++], new Long(noOfTLD));
		graphMetrics.put(token[i++], new Long(noOfPublicD));
		graphMetrics.put(token[i++], new Long(noOfPrivateD));
		graphMetrics.put(token[i++], new Long(edgesSize));

		int noOfAllObjects = listOfObjects.size();
		int noOfDomains = listOfDomains.size();

		double percentTLO = (tlo * 1.0) / noOfAllObjects;
		int tlllo = getTLLLO();
		double avgDepthOfLLO = 0;
		int noOfLLO = noOfObjectsType(NodeType.LLO);
		SummaryStatistics depthLLO = getDepth(NodeType.LLO);
		if (noOfLLO > 0) {
			double avgDepth = depthLLO.getMean();
			double minDepth = depthLLO.getMin();
			double maxDepth = depthLLO.getMax();
			double stddev = Math.sqrt(depthLLO.getVariance());
			avgDepthOfLLO = avgDepth;
		}
		int noPublicDomains = noOfDomainsType(NodeType.PD);

		// AVG PDO
		SummaryStatistics objectsInPublicD = getPublicDInObjects();
		SummaryStatistics objectsInPrivateD = getPrivateDInObjects();

		// AVG IDPD AVG IDPrD
		SummaryStatistics inDegree = objectsIDPublicD();
		SummaryStatistics inDegreePrD = objectsIDPrivateD();
		SummaryStatistics inDegreeTLD = objectsIDTLD();

		double noCOOG = 0;
		if (noOfAllObjects > 1) {
			noCOOG = (edgesSize * 100.0) / (noOfAllObjects * (noOfAllObjects - 1));
		}

		int noOwnCyclesDomains = getNoOwnCyclesDomains();

		double depthOfOwnershipCycles = 0;
		if (noOwnCyclesDomains > 0) {
			depthOfOwnershipCycles = (sumDepthCycles() * 1.0) / (noOwnCyclesDomains);
		}

		double avgDepthPerMaxDepth = (sumDepth() * 1.0) / (noOfAllObjects);

		objectsInDomains = getNoObjectsInDomains();
		double avgObjectsPerDomain = objectsInDomains.getMean();

		// average trace code to object
		// double avgTCO = getTotalTCO()*1.0/noOfAllObjects;
		SummaryStatistics avgTCO = getTCO();

		double avgTCE = 0;
		if (edgesSize > 0)
			avgTCE = totalTCE * 1.0 / edgesSize;

		SummaryStatistics classToObject = getClassToObject();

		SummaryStatistics scattering = getScattering();

		if (abtf > 0)
			abtf = 1 - (avgObjectsPerDomain / abtf);
		if (tlabtf > 0)
			tlabtf = 1 - (tlo * 1.0 / tlabtf);
		if (abhtf > 0)
			abhtf = 1 - (tlo * 1.0 / abhtf);

		// "%TLO,#LLOTLD,AVG LLO Depth/Max Depth,AVG PDO,AVG PrDO,AVG IDP,%COOG,#OC,AVG DOC/Max Depth,AVG Depth/Max Depth,AVG O/D";
		String[] mToken = GRAPH_METRICS.split(",");
		i = 0;
		oogResults.put(mToken[i++], new Double(noCOOG));
		oogResults.put(mToken[i++], new Double(treeDepth));
		oogResults.put(mToken[i++], new Double(avgDepthPerMaxDepth));
		oogResults.put(mToken[i++], new Double(percentTLO));
		oogResults.put(mToken[i++], new Double(tlllo));
		oogResults.put(mToken[i++], new Double(avgDepthOfLLO));
		oogResults.put(mToken[i++], new Double(objectsInPublicD.getMean()));
		oogResults.put(mToken[i++], new Double(objectsInPublicD.getMin()));
		oogResults.put(mToken[i++], new Double(objectsInPublicD.getMax()));
		oogResults.put(mToken[i++], new Double(Math.sqrt(objectsInPublicD.getVariance())));
		oogResults.put(mToken[i++], new Double(objectsInPrivateD.getMean()));
		oogResults.put(mToken[i++], new Double(objectsInPrivateD.getMin()));
		oogResults.put(mToken[i++], new Double(objectsInPrivateD.getMax()));
		oogResults.put(mToken[i++], new Double(Math.sqrt(objectsInPrivateD.getVariance())));
		oogResults.put(mToken[i++], new Double(inDegreeTLD.getMean()));
		oogResults.put(mToken[i++], new Double(inDegreeTLD.getMin()));
		oogResults.put(mToken[i++], new Double(inDegreeTLD.getMax()));
		oogResults.put(mToken[i++], new Double(Math.sqrt(inDegreeTLD.getVariance())));
		oogResults.put(mToken[i++], new Double(inDegree.getMean()));
		oogResults.put(mToken[i++], new Double(inDegree.getMin()));
		oogResults.put(mToken[i++], new Double(inDegree.getMax()));
		oogResults.put(mToken[i++], new Double(Math.sqrt(inDegree.getVariance())));
		oogResults.put(mToken[i++], new Double(inDegreePrD.getMean()));
		oogResults.put(mToken[i++], new Double(inDegreePrD.getMin()));
		oogResults.put(mToken[i++], new Double(inDegreePrD.getMax()));
		oogResults.put(mToken[i++], new Double(Math.sqrt(inDegreePrD.getVariance())));

		oogResults.put(mToken[i++], new Double(noOwnCyclesDomains));
		oogResults.put(mToken[i++], new Double(depthOfOwnershipCycles));

		// AOD
		oogResults.put(mToken[i++], new Double(avgObjectsPerDomain));
		oogResults.put(mToken[i++], new Double(objectsInDomains.getMin()));
		oogResults.put(mToken[i++], new Double(objectsInDomains.getMax()));
		oogResults.put(mToken[i++], new Double(Math.sqrt(objectsInDomains.getVariance())));
		
		oogResults.put(mToken[i++], new Double(avgTCO.getMean()));
		oogResults.put(mToken[i++], new Double(avgTCO.getMin()));
		oogResults.put(mToken[i++], new Double(avgTCO.getMax()));
		oogResults.put(mToken[i++], new Double(avgTCO.getStandardDeviation()));
		oogResults.put(mToken[i++], new Double(avgTCE));
		oogResults.put(mToken[i++], new Double(scattering.getMean()));
		oogResults.put(mToken[i++], new Double(scattering.getMin()));
		oogResults.put(mToken[i++], new Double(scattering.getMax()));
		oogResults.put(mToken[i++], new Double(scattering.getStandardDeviation()));
		oogResults.put(mToken[i++], new Double(classToObject.getMean()));
		oogResults.put(mToken[i++], new Double(classToObject.getMin()));
		oogResults.put(mToken[i++], new Double(classToObject.getMax()));
		oogResults.put(mToken[i++], new Double(classToObject.getStandardDeviation()));
		oogResults.put(mToken[i++], new Double(abtf));
		oogResults.put(mToken[i++], new Double(tlabtf));
		oogResults.put(mToken[i++], new Double(abhtf));

		return saveTableLine();
	}

	private SummaryStatistics getTCO() {
		SummaryStatistics summary = new SummaryStatistics();
		for (GraphMetricItem gmi : listOfObjects.values()) {
			summary.addValue(gmi.noTrLinks);
		}
		return summary;
	}

	private SummaryStatistics getScattering() {
		SummaryStatistics summary = new SummaryStatistics();
		for (GraphMetricItem gmi : listOfObjects.values()) {
			summary.addValue(gmi.objectScattering);
		}
		return summary;
	}

	private SummaryStatistics getClassToObject() {
		SummaryStatistics summary = new SummaryStatistics();
		HashSet<String> classNameSet = new HashSet<String>();
		for (GraphMetricItem gmi : listOfObjects.values()) {
			// HACK: displayTypeName is not uniquely identified
			// HACK: using gmi.declaredTypeName is a workaround, but some
			// declared types are imprecise. See Hashtable<K, List<V>> in PX
			classNameSet.add(gmi.declaredTypeName);
		}
		
		// System.out.println("classes that have more than one representative in the object graph");

		for (String className : classNameSet) {
			long count = 0;
			for (GraphMetricItem gmj : listOfObjects.values()) {
				if (gmj.declaredTypeName.equals(className)){
					count++;
//					if (count>1)
//					 System.out.println(gmj.declaredTypeName);
				}
			}
			summary.addValue(count);
		}
		return summary;
	}

	private long getTotalTCO() {
		long total = 0;
		for (GraphMetricItem gmi : listOfObjects.values()) {
			total += gmi.noTrLinks;
		}
		return total;
	}

	/**
	 * @param token
	 * @param mToken
	 * @return
	 */
	public String saveTableLine() {
		String[] token = GRAPH_SIZE_METRIC.split(",");
		String[] mToken = GRAPH_METRICS.split(",");
		StringBuffer percentageLine = new StringBuffer();
		DecimalFormat twoDForm = new DecimalFormat("#.##");

		for (int i = 0; i < token.length; i++) {
			percentageLine.append(twoDForm.format(graphMetrics.get(token[i])));
			percentageLine.append(",");
		}
		// do not save ABTF
		for (int i = 0; i < mToken.length - 3; i++) {
			percentageLine.append(twoDForm.format(oogResults.get(mToken[i])));
			percentageLine.append(",");
		}
		return percentageLine.toString();
	}

	
	private long noOfOinD(boolean isPublic) {
		long noOfOPD = 0;
		for (GraphMetricItem gmi : listOfDomains.values()) {
			if(gmi.getType() == NodeType.SHARED) {
				// skip shared
				continue;
			}
				
			if ( gmi.isInPublicDomain() == isPublic) {
				noOfOPD += gmi.getOutOwnDegree();
			}
		}
	    return noOfOPD;
    }
	
	private long getTotalNoObjectsInDomains() {
		long sum = 0;
		for (GraphMetricItem gmi : listOfDomains.values()) {
			sum += gmi.getOutOwnDegree();
		}
		return sum;
	}

	private SummaryStatistics getNoObjectsInDomains() {
		SummaryStatistics summary = new SummaryStatistics();
		for (GraphMetricItem gmi : listOfDomains.values()) {
			summary.addValue(gmi.getOutOwnDegree());
		}
		return summary;
	}

	private long getNoObjectsInPD() {
		long number = 0;
		for (GraphMetricItem mi : listOfObjects.values()) {
			if (mi.isInPublicDomain())
				number++;
		}
		return number;
	}

	private SummaryStatistics getPublicDInObjects() {
		SummaryStatistics summary = new SummaryStatistics();
		for (GraphMetricItem mi : listOfObjects.values()) {
			if (mi.getType() != NodeType.ORoot)
				summary.addValue(mi.noPublicDomains);
		}
		return summary;
	}

	private SummaryStatistics getPrivateDInObjects() {
		SummaryStatistics summary = new SummaryStatistics();
		for (GraphMetricItem mi : listOfObjects.values()) {
			if (mi.getType() != NodeType.ORoot)
				summary.addValue(mi.noPrivateDomains);
		}
		return summary;
	}

	private long getNoObjectsInPrD() {
		long number = 0;
		for (GraphMetricItem mi : listOfObjects.values()) {
			if (!mi.isInPublicDomain())
				number++;
		}
		return number;
	}

	private long noOfDomainsDepth(int depth) {
		long noOfDomains = 0;
		for (GraphMetricItem mi : listOfDomains.values()) {
			if (mi.getDepth() == 2)
				noOfDomains++;
		}
		return noOfDomains;
	}

	private long noOfObjectsDepth(int depth) {
		long noOfObjects = 0;
		for (GraphMetricItem mi : listOfObjects.values()) {
			if (mi.getDepth() == 2)
				noOfObjects++;
		}
		return noOfObjects;
	}

	private int getTLLLO() {
		int noTL_LLO = 0;
		for (GraphMetricItem mi : listOfObjects.values()) {
			if (mi.getType().equals(NodeType.LLO) && (mi.getDepth() == 2))
				noTL_LLO++;
		}
		return noTL_LLO;
	}

	private long sumDepth() {
		long sum = 0;
		for (GraphMetricItem mi : listOfObjects.values()) {
			sum += mi.getDepth();
		}
		return sum;
	}

	private int getNoOwnCyclesDomains() {
		int cycles = 0;
		for (GraphMetricItem mi : listOfDomains.values()) {
			if (mi.getInOwnDegree() > 1)
				cycles += mi.getInOwnDegree() - 1;
		}
		return cycles;
	}

	private double sumDepthCycles() {
		int sumDepth = 0;
		for (GraphMetricItem mi : listOfDomains.values()) {
			if (mi.getInOwnDegree() > 1)
				sumDepth += mi.getDepth();
		}
		return sumDepth;
	}

	private int noOfDomainsType(NodeType type) {
		int tlos = 0;
		for (GraphMetricItem mi : listOfDomains.values()) {
			if (mi.getType().equals(type))
				tlos++;
		}
		return tlos;
	}

	private int noOfObjectsType(NodeType type) {
		int tlos = 0;
		for (GraphMetricItem mi : listOfObjects.values()) {
			if (mi.getType().equals(type))
				tlos++;
		}
		return tlos;
	}

	private long inPublicDomainsDegree() {
		long inDegree = 0;
		for (GraphMetricItem mi : listOfObjects.values()) {
			if (mi.isInPublicDomain())
				inDegree += mi.getInPtDegree();
		}
		return inDegree;
	}

	private long inPrivateDomainsDegree() {
		long inDegree = 0;
		for (GraphMetricItem mi : listOfObjects.values()) {
			if (!mi.isInPublicDomain())
				inDegree += mi.getInPtDegree();
		}
		return inDegree;
	}

	private SummaryStatistics objectsIDTLD() {
		SummaryStatistics summary = new SummaryStatistics();
		for (GraphMetricItem mi : listOfObjects.values()) {
			if (mi.getDepth() == 2)
				summary.addValue(mi.inOutsidePtDegree);
		}
		return summary;
	}

	private SummaryStatistics objectsIDPublicD() {
		SummaryStatistics summary = new SummaryStatistics();
		for (GraphMetricItem mi : listOfObjects.values()) {
			if (mi.isInPublicDomain() && (mi.getDepth() != 2))
				summary.addValue(mi.inOutsidePtDegree);
		}
		return summary;
	}

	private SummaryStatistics objectsIDPrivateD() {
		SummaryStatistics summary = new SummaryStatistics();
		for (GraphMetricItem mi : listOfObjects.values()) {
			if (!mi.isInPublicDomain())
				summary.addValue(mi.inOutsidePtDegree);
		}
		return summary;
	}

	private SummaryStatistics getDepth(NodeType type) {
		SummaryStatistics d = new SummaryStatistics();
		for (GraphMetricItem mi : listOfObjects.values()) {
			if (mi.getType().equals(type))
				d.addValue(mi.getDepth());
		}
		return d;

	}

	private long getMaxTreeDepth() {
		long treeODepth = getMaxDepth(NodeType.O);
		long rootDepth = getMaxDepth(NodeType.ORoot);
		return Math.max(treeODepth, rootDepth);

	}

	private long getMaxDepth(NodeType type) {
		long d = 0;
		for (GraphMetricItem mi : listOfObjects.values()) {
			if (mi.getType().equals(type))
				if (mi.getDepth() >= d)
					d = mi.getDepth();
		}
		return d;
	}
}