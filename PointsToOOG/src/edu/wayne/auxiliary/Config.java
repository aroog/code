package edu.wayne.auxiliary;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

public class Config {

	private static final String CONFIG_PROPERTIES = "config.properties";
	public static String MAINCLASS = "Main";
	public static String MAINMETHOD = "main";
	public static boolean EDGEOPTIONS_DF = true;
	public static boolean EDGEOPTIONS_PT = true;
	public static boolean EDGEOPTIONS_CR = false;
	public static boolean EDGEOPTIONS_CF = false;
	public static boolean EXPORT_TO_XML = false;
	public static boolean GZIP = true;
	public static boolean DGML = false;
	public static boolean DOT = true;

	// XXX. IMPORTANT: Gotta use lentunique for DF edges.
	public static boolean HANDLE_LENT_UNIQUE = false;

	private Config() {
	}

	public static void loadConfig(IPath path) {
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(path.append(CONFIG_PROPERTIES).toOSString()));
			MAINCLASS = props.getProperty("mainclass");
			MAINMETHOD = props.getProperty("mainmethod");
			String property = props.getProperty("edgeoptions.df");
			EDGEOPTIONS_DF = property!=null && property.equals("true");
			String property2 = props.getProperty("edgeoptions.pt");
			EDGEOPTIONS_PT = property2!=null && property2.equals("true");
			String property4 = props.getProperty("edgeoptions.cr");
			EDGEOPTIONS_CR = property4!=null && property4.equals("true");
			String property5 = props.getProperty("edgeoptions.cf");
			EDGEOPTIONS_CF = property5!=null && property5.equals("true");
			String property3 = props.getProperty("gzip");
			GZIP = property3!=null && property3.equals("true");
			String property33 = props.getProperty("export.graph");
			EXPORT_TO_XML = property33!=null && property33.equals("true");
			String property6 = props.getProperty("export.dgml");
			DGML = property6!=null &&property6.equals("true");
			String property7 = props.getProperty("export.dot");
			DOT = property7!=null && property7.equals("true");
			String property8 = props.getProperty("option.lentunique");
			HANDLE_LENT_UNIQUE = property8!=null && property8.equals("true");
			
			checkConfig();
		} catch (IOException ex) {
			System.err.println("Configuration file not found. Create config.properites for current project");
		}
	}

	// Sanity check
	private static void checkConfig() {
		if (EDGEOPTIONS_DF && !HANDLE_LENT_UNIQUE) {
			System.err.println("Cannot extract DF edges without handling lent/unique");
		}
	}

	public static String toConfigString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PT= ");
		builder.append(Boolean.toString(EDGEOPTIONS_PT));
		builder.append("; DF= ");
		builder.append(Boolean.toString(EDGEOPTIONS_DF));
		// TODO: add more.
		return builder.toString();
	}
	/**
	 * @param md
	 * @return
	 */
	public static boolean isMainMethod(MethodDeclaration md) {
		IMethodBinding resolveBinding = md.resolveBinding();
		if (resolveBinding == null)
			return false;
		ITypeBinding declClassBinding = resolveBinding.getDeclaringClass();
		if (declClassBinding == null)
			return false;
		String declaringClass = declClassBinding.getQualifiedName();
		SimpleName name = md.getName();
		if (name == null)
			return false;
		String mName = name.toString();
		return declaringClass.equals(MAINCLASS) && mName.equals(MAINMETHOD);
	}
}
