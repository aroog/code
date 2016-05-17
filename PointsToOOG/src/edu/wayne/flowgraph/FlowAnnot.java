package edu.wayne.flowgraph;
/**
 * represents a flow annotation
 * (i - method i is invoked
 * )i - method i returns
 * \bullet - empty annotation
 * 
 * */
public class FlowAnnot {
	final long i;
	FlowAnnotType value;
	private static FlowAnnot empty;
	private static FlowAnnot star;
	private static FlowAnnot call;
	private static FlowAnnot ncall;

	public FlowAnnot(long l, FlowAnnotType value) {
		this.i = l;
		this.value = value;
	}

	public static FlowAnnot getEmpty(){
		if (empty==null){
			empty =  new FlowAnnot(-1,FlowAnnotType.EMPTY);
			return empty;
		}
		return empty;
	}

	@Override
	public String toString() {
		String string = value.name();
		if (i>0) string +="_" + i + "";
		return string;
	}

	public static FlowAnnot getSTAR() {
		if (star==null){
			star =  new FlowAnnot(-2,FlowAnnotType.STAR);
			return star;
		}
		return star;
	}

	public static FlowAnnot getCall() {
		if (call==null){
			call =  new FlowAnnot(-3,FlowAnnotType.CALL);
			return call;
		}
		return call;
	}

	public static FlowAnnot getnCall() {
		if (ncall==null){
			ncall =  new FlowAnnot(-4,FlowAnnotType.NCALL);
			return ncall;
		}
		return ncall;
	}
}
