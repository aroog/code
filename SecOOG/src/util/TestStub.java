package util;

import java.net.URISyntaxException;
import java.net.URL;

import org.junit.After;
import org.junit.Before;

import edu.wayne.ograph.OGraph;
import edu.wayne.ograph.OOGUtils;
import junit.framework.Assert;
import secoog.CopyOGraphVisitor;
import secoog.SecGraph;
import secoog.SecurityAnalysis;

/**
 * Extend this class to write a constraint as a unit test 
 * available references: 
 * secGraph - OOG + security properties + queries
 * scoria - reference to the analysis, to display warnings   
 * */
public class TestStub {
	protected SecGraph secGraph;
	protected OGraph graph;
	protected SecurityAnalysis scoria = SecurityAnalysis.getInstance();

	@Before
	public void setup() throws URISyntaxException {
		URL url = getClass().getResource("/OOG.xml.gz");
		Assert.assertNotNull(url);
		graph = OOGUtils.loadGZIP(url.getPath());

		// Create SecGraph
		CopyOGraphVisitor copyVisitor = new CopyOGraphVisitor();
		graph.accept(copyVisitor);
		secGraph = SecGraph.getInstance();
	}
	
	@After
	public void clean(){
		graph.clear();
		secGraph.clear();
	}

}