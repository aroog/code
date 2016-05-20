package edu.wayne.metrics.datamodel;

public class FlatGraphMetricItem {
 String id;
 String label;
 double depth;
 int inDegree;
 int outDegree;
 public FlatGraphMetricItem(String id2, String label2, double height, int inDegree, int outDegree) {
	 this.id = id2;
	 this.label = label2;
	 this.depth = height;
	 this.inDegree = inDegree;
	 this.outDegree = outDegree;
 }
public String getLabel() {
	return label;
}
 
}
