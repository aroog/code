package edu.wayne.ograph.internal;

import org.junit.Test;

import edu.wayne.ograph.OOGUtils;


public class TestOGraph {

	@Test
	public void test(){
		String path = "C:\\Temp\\OOG.xml";
		String loadpath = "C:\\Users\\CRISTINA\\Desktop\\radu_work\\code\\OoDaFATest\\Test_QuadTree\\OOG.xml";
		// OGraph graph = getOGraph();
		OOGContext context = new OOGContext(null);
		edu.wayne.ograph.OGraph graph = OOGUtils.load(loadpath);
		
//		OOGUtils.save(graph, path);
	}
}
