package oog.ui.content.wrappers;

import java.util.List;

import util.TraceabilityEntry;

public class TraceabilityNode implements InfoWrapper{

	TraceabilityEntry data;
	TraceabilityNode next;
	
	public TraceabilityNode(TraceabilityEntry trace) {
		this.data = trace;
	}
	public TraceabilityEntry getData() {
		return data;
	}
	public TraceabilityNode getNext() {
		return next;
	}
	public void setData(TraceabilityEntry data) {
		this.data = data;
	}
	public void setNext(TraceabilityNode next) {
		this.next = next;
	}
	@Override
	public Object[] toArray() {
		return new Object[]{this};

	}
	@Override
	public String toString() {
		return data.getSecond().toString();
	}
	
	
	
	public static TraceabilityNode createNodeList(List<TraceabilityEntry> rawList){
		TraceabilityNode head;
		TraceabilityNode last;
		head = last = new TraceabilityNode(null);
		for(TraceabilityEntry trace: rawList){
			last.setNext(new TraceabilityNode(trace));
			last = last.getNext();
		}
		return head;
		
	}
	
}
