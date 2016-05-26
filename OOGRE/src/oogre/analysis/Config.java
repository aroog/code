package oogre.analysis;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import oogre.refinements.tac.InferOptions;
import oogre.refinements.tac.InferOptions.InferTypes;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * XXX. Convert this to a proper singleton instead of using static fields 
 *
 * XXX. Extract constants
 */
public class Config {

	private static final String CONFIG_PROPERTIES = "oogre.properties";
	public static String MAINCLASS = "Main";
	public static String MAINMETHOD = "main";
	
	/**
	 * Save annotations to code after each refinements
	 */
	public static boolean SAVE_ANNOTATIONS = true;
	
	/**
	 * Save changed AUs in CSV file (slows things down a bit)
	 */
	public static boolean SAVE_DIFFS = false;

	private Config() {
	}

	public static void loadConfig(IPath path) {
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(path.append(CONFIG_PROPERTIES).toOSString()));
			MAINCLASS = props.getProperty("mainclass");
			MAINMETHOD = props.getProperty("mainmethod");
			
			String property3 = props.getProperty("saveAnnotations");
			SAVE_ANNOTATIONS = property3!=null && property3.equals("true");
			
			String property32 = props.getProperty("saveDiffs");
			SAVE_DIFFS = property32!=null && property32.equals("true");
			
			String property4 = props.getProperty("enableHeuristicsOwned");
			OptionsForHeuristics.enableHeuristicsOwned = property4!=null && property4.equals("true");

			String property5 = props.getProperty("enableHeuristicsPD");
			OptionsForHeuristics.enableHeuristicsPD = property5!=null && property5.equals("true");

			String property6 = props.getProperty("turnOFFowned");
			InferOptions.getInstance().setTurnOFFowned(property6 != null && property6.equals("true"));			
			
			// The default is InferTypes.OD; read the default from InferOptions.
			String property7 = props.getProperty("inferOT");
			InferOptions.getInstance().setInferStyle( property7 != null && property7.equals("true") ? InferTypes.OT : InferTypes.OD);
		} 
		catch (IOException ex) {
			System.err.println("Configuration file not found. Create config.properites for current project");
		}
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
