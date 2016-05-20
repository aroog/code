package edu.wayne.metrics.datamodel;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import edu.wayne.metrics.utils.CSVOutputUtils;
import edu.wayne.metrics.utils.ObjectsUtils;

public class FlatGraphMetricReport {
	public static final String METRIC_HEADER = "#FO,%LLFO,HR,MF";
	private static final String OPT_HEADER = " flat graph covered by OGraph, OGraph covered by flat Graph";
	private static final String FLAT_METRIC_TABLEHEADER = "System,"+METRIC_HEADER+","+OPT_HEADER;
	private static final String FLAT_OGRAPH_TABLEHEADER = "System,Flat Object,depth,isLLO,OObject Type,depth,#OObjects"+"\n";
	private static final String OGRAPH_FLAT_TABLEHEADER = "System,Flat Object,depth,isLLO,OObject Type,depth,#OObjects"+"\n";
	private static final String FLAT_TABLEHEADER = "System,type,depth,inDegree,outDegree"+"\n";
	private Hashtable<String, FlatGraphMetricItem> flatGraphObjects;
	private Hashtable<String, GraphMetricItem> ographObjects;
	public Hashtable<String, Double> flatGraphMetrics;
	private IProject currentProject;
	private IPath currentPath;
	private String projectName;
	
	//Metrics
	private int upLevelOObjects;
	private int upLevelFlatObjects;
	private int upLevelOGraph;
	private int upLevelFlatGraph;
	private int lowLeveFlatObjects;
	private int TLO;
	/**
     * @param flatGraphObjects
     * @param ographObjects
     */
    public FlatGraphMetricReport(Hashtable<String, FlatGraphMetricItem> flatGraphObjects,
            Hashtable<String, GraphMetricItem> ographObjects, IProject currentProject) {
	    this.flatGraphObjects = flatGraphObjects;
	    this.ographObjects = ographObjects;
	    this.currentProject = currentProject;
	    currentPath = currentProject.getLocation();
    	projectName = currentProject.getName();
    	upLevelFlatGraph = 0;
    	upLevelFlatObjects = 0;
    	upLevelOGraph = 0;
    	upLevelOObjects = 0;
    	lowLeveFlatObjects = 0;
    }
	
    public void matchFlatGraphToOGraph(String fileName) throws IOException{
    	String filePath = currentPath.append(projectName + fileName).toOSString();    	
    	CSVOutputUtils.writeTableHeaderToFile(filePath, FLAT_OGRAPH_TABLEHEADER);
    	for(FlatGraphMetricItem fgmi:flatGraphObjects.values()){
    		StringBuffer rowBuffer = new StringBuffer(); 
    		boolean isLowLevelFlatObject = ObjectsUtils.isLowLevelFlatObject(fgmi.label);
    		if (!isLowLevelFlatObject) upLevelFlatGraph++; 
    		else lowLeveFlatObjects++;
    		
			rowBuffer.append(fgmi.label+","+fgmi.depth+","+isLowLevelFlatObject+",");
    		int noOfOObjects = 0;
    		for(GraphMetricItem gmi:ographObjects.values()){
    			//we can compare the types as they are, but Womble do not include generics
    			//if (fgmi.getLabel().equals(gmi.getTypeName())){
    			//HACK: we do not always have qualified type name from flat graphs.
    			boolean matches = matchByName(fgmi, gmi);
    			
				if (matches){
    				if (noOfOObjects==0){
    					rowBuffer.append("\"");
    					rowBuffer.append(gmi.getTypeName());
    					rowBuffer.append("\",");
    					rowBuffer.append(gmi.getDepth());
    					rowBuffer.append(",");
    				}
    				noOfOObjects++;
    			}
    		}
    		if (noOfOObjects>0)
    			rowBuffer.append(noOfOObjects+",");
    		else{
    			rowBuffer.append(",,"+noOfOObjects+",");
    			if (!isLowLevelFlatObject) upLevelFlatObjects++;
    		}
    		CSVOutputUtils.appendRowToFile(filePath, projectName, rowBuffer.toString());
    	}
    }

	/**
     * @param fgmi
     * @param gmi
     * @return true if the names are equal, if one ends with the name of the other.
     */
    private boolean matchByName(FlatGraphMetricItem fgmi, GraphMetricItem gmi) {
    	//HACK:we can compare the types as they are, but Womble do not include generics
		//if (fgmi.getLabel().equals(gmi.getTypeName())){
    	
    	String[] extractGenerics = gmi.getTypeName().split("<");
	    boolean matches = fgmi.getLabel().equals(gmi.getTypeName());
	    if (!matches) matches = fgmi.getLabel().equals(extractGenerics[0]);
	    if (!matches) matches = extractGenerics[0].endsWith("."+fgmi.getLabel());
	    return matches;
    }
    
    public void matchOGraphToFlatGraph(String fileName) throws IOException{
    	String filePath = currentPath.append(projectName + fileName).toOSString();    	
    	CSVOutputUtils.writeTableHeaderToFile(filePath, OGRAPH_FLAT_TABLEHEADER);
    	for(GraphMetricItem gmi:ographObjects.values()){
    		StringBuffer rowBuffer = new StringBuffer(); 
    		boolean isLowLevelObject = ObjectsUtils.isLowLevelObject(gmi.getTypeName());
    		if (!isLowLevelObject) upLevelOGraph++;
    		if (gmi.getDepth() == 2) TLO++;
			rowBuffer.append("\""+gmi.getTypeName()+"\","+gmi.getDepth()+","+isLowLevelObject+",");
    		int noOfOObjects = 0;
    		for(FlatGraphMetricItem fgmi:flatGraphObjects.values()){    			
    			boolean matches = matchByName(fgmi, gmi);
				if (matches){
    				if (noOfOObjects==0){
    					rowBuffer.append(fgmi.label);
    					rowBuffer.append(",");
    					rowBuffer.append(fgmi.depth);
    					rowBuffer.append(",");
    				}
    				noOfOObjects++;
    			}
    		}
    		if (noOfOObjects>0)
    			rowBuffer.append(noOfOObjects+",");
    		else{
    			rowBuffer.append(",,"+noOfOObjects+",");
    			if (!isLowLevelObject)
    				upLevelOObjects++;
    		}
    		CSVOutputUtils.appendRowToFile(filePath, projectName, rowBuffer.toString());
    	}
    }

    public void collectAllData() throws IOException{
    	 saveFlatGraphStats("_OOGMetrics_FlatGraph.csv");
		 matchFlatGraphToOGraph("_OOGMetrics_Flat_vs_OGraph.csv");
		 matchOGraphToFlatGraph("_OOGMetrics_OGraph_vs_Flat.csv");
		 saveFlatMetrics("_OOGMetrics_FlatGraphTable.csv");
    }
    public void saveFlatGraphStats(String fileName) throws IOException{
    	
    	String filePath = currentPath.append(projectName + fileName).toOSString();    	
    	CSVOutputUtils.writeTableHeaderToFile(filePath, FLAT_TABLEHEADER);
    	for (FlatGraphMetricItem fgmi:flatGraphObjects.values()){
    		String row = fgmi.label+","+fgmi.depth+","+fgmi.inDegree+","+fgmi.outDegree;
    		CSVOutputUtils.appendRowToFile(filePath, projectName, row);
    	}
    }
    /**
     *   call matchers before calling this method for correct results
     * */
    public void saveFlatMetrics(String fileName) throws IOException{
    	
    	String filePath = currentPath.append(projectName + fileName).toOSString(); 
    	CSVOutputUtils.writeTableHeaderToFile(filePath, FLAT_METRIC_TABLEHEADER+"\n");
    	DecimalFormat twoDForm = new DecimalFormat("#.##");
    	StringBuffer rowBuffer = new StringBuffer();
    	
    	//#FO
    	rowBuffer.append(flatGraphObjects.values().size()+",");
    	
    	//#LLFO
    	double llfo = lowLeveFlatObjects*1.0/flatGraphObjects.values().size();
		rowBuffer.append(llfo+",");
//    	rowBuffer.append(upLevelFlatGraph*1.0/TLO+",");

    	//Hierarchical reduction
    	double hr = 0;
    	if (TLO>0) {
	        hr = flatGraphObjects.values().size()*1.0/TLO;
	        rowBuffer.append(twoDForm.format(hr)+",");
        }
        else 
    		rowBuffer.append(",");

    	//Merging factor
    	double mf = 0;
    	if (upLevelFlatObjects>0) {
	        mf = upLevelOObjects*1.0/upLevelFlatObjects;
	        rowBuffer.append(twoDForm.format(mf)+",");
        }
        else 
    		rowBuffer.append(",");

    	//
    	if (upLevelFlatGraph>0)
    		rowBuffer.append(twoDForm.format((upLevelFlatGraph-upLevelFlatObjects)*1.0/upLevelFlatGraph)+",");
    	else 
    		rowBuffer.append(",");
    	if (upLevelOGraph>0)
    		rowBuffer.append(twoDForm.format((upLevelOGraph-upLevelOObjects)*1.0/upLevelOGraph)+",");
    	else 
    		rowBuffer.append(",");
    	CSVOutputUtils.appendRowToFile(filePath, projectName, rowBuffer.toString());
    	
    	flatGraphMetrics = new Hashtable<String, Double>();
    	flatGraphMetrics.put("#FO",new Double(flatGraphObjects.values().size()));
    	flatGraphMetrics.put("%LLFO", new Double(llfo));
    	flatGraphMetrics.put("%HR", new Double(hr));
    	flatGraphMetrics.put("MF", new Double(mf));
    }
    
    public String saveLineMetrics(){
    	DecimalFormat twoDForm = new DecimalFormat("#.##");
    	
    	return  twoDForm.format(flatGraphMetrics.get("#FO")) + "," +
    			twoDForm.format(flatGraphMetrics.get("%LLFO")) + "," +
    			twoDForm.format(flatGraphMetrics.get("%HR")) + "," +
    			twoDForm.format(flatGraphMetrics.get("MF")) + ",";
    }
}
