package edu.cmu.cs.aliasjava.test;
import java.util.List;

import junit.framework.TestCase;
import org.junit.Test;

import edu.cmu.cs.aliasjava.AnnotationInfo;
import edu.cmu.cs.aliasjava.Constants;
import edu.cmu.cs.aliasjava.parser.DomainParams;


public class AnnotationParserTest extends TestCase {

	@Test
	public void testParser() {
		AnnotationInfo parsedAnnotation = AnnotationInfo.parseAnnotation("lent<owned>");
		DomainParams annotation = parsedAnnotation.getAnnotation();
		assertEquals(annotation.getDomain(), Constants.LENT );
		List<DomainParams> params = parsedAnnotation.getParameters();
		assertEquals(params.size(), 1);
		
		if (params.size() >= 1 ) {
			DomainParams param0 = params.get(0);
			assertEquals(param0.getDomain(), Constants.OWNED);
		}
	}

	
	@Test
	public void testParser0() {
		AnnotationInfo parsedAnnotation = AnnotationInfo.parseAnnotation("shared");
		DomainParams annotation = parsedAnnotation.getAnnotation();
		assertEquals(annotation.getDomain(), Constants.SHARED );
		List<DomainParams> params = parsedAnnotation.getParameters();
		assertEquals(params.size(), 0);	
	}

}
