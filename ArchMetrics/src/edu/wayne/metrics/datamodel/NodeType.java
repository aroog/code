package edu.wayne.metrics.datamodel;

//TODO: HIGH. Some of these are not in use.

public enum NodeType {
	DRoot, //root domain  - a unique DRoot
	ORoot, //root object - a unique ORoot
//	TLO, //top level object - depth 2
	TLD, //top level domain - depth 2
	PD, // public domain (non top-level)
	PrD, //private domain
	O, // object (non-top-level)
	LLO, // low level object e.g. an insance of ArrayList. It has nothing to do with depth
	SHARED;
	public String toString(){
		switch(this){
		case DRoot: return "DRoot";
		case ORoot: return "ORoot";
//		case TLO: return "TLO";
		case TLD: return "TLD";
		case PD: return "PD";
		case PrD: return "PrD";
		case O: return "O";
		case LLO: return "LLO";
		}
		return "undefined";
	}
} //#all objects  = #TLO +#O + #LLO
//#all domains = #TLD +#PD + #PrD
