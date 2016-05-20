package edu.wayne.metrics;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IProgressMonitor;

import edu.wayne.metrics.internal.AbstractCrystalPlugin;

/**
 * The eclipse plugin that will launch and maintain the analysis. This is where you must instantiate and register your
 * analyses in order for them to be run.
 * 
 * @author David Dickey
 * @author Jonathan Aldrich
 */
public class CrystalPlugin extends AbstractCrystalPlugin {

	// The shared instance.
	private static CrystalPlugin plugin;

	// Resource bundle.
	private ResourceBundle resourceBundle;

	/**
	 * The constructor.
	 */
	public CrystalPlugin() {
		super();
		CrystalPlugin.plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("codeview.CodeviewPluginResources");
		}
		catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Modify this method to instantiate and register the analyses you want the framework to run.
	 */
	public void setupCrystalAnalyses(Crystal crystal) {
	}

	/**
	 * Returns the shared instance.
	 */
	public static CrystalPlugin getDefault() {
		return CrystalPlugin.plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = CrystalPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		}
		catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public static String getPluginId() {
		return null;
	}


	/**
	 * @return
	 */
	public IProgressMonitor getProgressMonitor() {
		return null;
	}

}
