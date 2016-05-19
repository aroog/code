package ast;

import java.io.File;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.CycleStrategy;
import org.simpleframework.xml.strategy.Strategy;

public class TestUtils {

	/*
	 * See documentation for Simple at http://simple.sourceforge.net/
	 */
	public static void save(BaseTraceability model, String path) {
		Strategy strategy = new CycleStrategy("id", "ref");
		Serializer serializer = new Persister(strategy);
		File result = new File(path);

		try {
			serializer.write(model, result);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static BaseTraceability load(String filename) {
		BaseTraceability read = null;
		Strategy strategy = new CycleStrategy("id", "ref");
		Serializer serializer = new Persister(strategy);
		File source = new File(filename);

		try {
			read = serializer.read(BaseTraceability.class, source);
		}
		catch (Exception e) {
			System.err.println("Exception when loading file:"+ filename);
			e.printStackTrace();
		}

		return read;
	}
	
	
	public static BaseTraceability getFile(int selection){
		String filename = null;
		switch (selection) {
		
		case 1:
			filename = "C:\\temp\\FindFieldDeclarationEclipseAST.xml";
			break;
			
		case 2:
			filename = "C:\\temp\\FindMethodDeclarationEclipseAST.xml";
			break;
			
		case 3:
			filename = "C:\\temp\\FindTypeDeclarationEclipseAST.xml";
			break;
		case 4:
			filename = "C:\\temp\\FindDefaultPackageFieldDeclarationEclipseAST.xml";
			break;
		case 5:
			filename = "C:\\temp\\FindInneClassFieldDeclarationEclipseAST.xml";
			break;
		case 6:
			filename = "C:\\temp\\FindClassInstanceCreationEclipseAST.xml";
			break;
		case 7:
			filename = "C:\\temp\\FindFieldWriteEclipseAST.xml";
			break;
		case 8:
			filename = "C:\\temp\\GenericTypeEclipseAST.xml";
			break;
			
		}
		
		BaseTraceability baseTraceabilityObject = null;
		if(filename!= null)
			baseTraceabilityObject = TestUtils.load(filename);
		return baseTraceabilityObject;
	}
}
