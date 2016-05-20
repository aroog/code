package edu.wayne.metrics;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;

// TODO: Customize for ArchMetrics
public class Config {

	private static final String CONFIG_PROPERTIES = "archMetrics.properties";
	public static String[] FILTER_IMPORTS = { "java.*", "javax.*", "org.*", "edu.cmu.cs.aliasjava.*" };
	public static String[] FILTER_DECL = { "java.*", "javax.*", "org.*" };
	public static boolean USE_QUALIFIED_TYPENAME = true;
	public static boolean DEBUG_OUTPUT_PATHS = false;
	
	private Config() {
	}

	public static void loadConfig(IPath path) throws IOException {
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(path.append(CONFIG_PROPERTIES).toOSString()));
			String property = props.getProperty("filter.imports");
			if (property != null)
				FILTER_IMPORTS = property.split(";");
			String property2 = props.getProperty("filter.decl");
			if (property2 != null)
				FILTER_DECL = property2.split(";");
//            TODO LOW: use the property file to decide if fully qualified name is to be used
//			  Requires several changes				
//			String property3 = props.getProperty("use.qualified.typename");
//			if (property3 != null)
//				USE_QUALIFIED_TYPENAME = property3.compareToIgnoreCase("true")==0;
//			String property4 = props.getProperty("debug.output.paths");
//			if (property4 != null)
//				DEBUG_OUTPUT_PATHS = property4.compareToIgnoreCase("true")==0;
		} catch (IOException ex) {
			throw ex;
		}
	}
}
