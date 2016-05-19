package edu.wayne.summary.strategies;

import java.util.HashMap;
import java.util.Map;

public class MarkManager {
	
	static MarkManager instance = null;
	
	private Map<String, MarkStatus> map = new HashMap<String,MarkStatus>();
	
	private MarkManager() {
	}

	public static MarkManager getInstance() {
		if(instance == null ) {
			instance = new MarkManager();
		}
		return instance;
	}
	
	public void setMark(String info, MarkStatus mark) {
		map.put(info, mark);
	}
	
	public MarkStatus getMark(String info) {
		return map.get(info);
	}
	
	public void reset() {
		map.clear();
	}
}
