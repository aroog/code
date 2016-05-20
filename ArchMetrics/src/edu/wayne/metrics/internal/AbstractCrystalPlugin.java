package edu.wayne.metrics.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.wayne.metrics.Crystal;

/**
 * Provided Crystal plugin functionality
 * 
 * @author David Dickey
 * @author Jonathan Aldrich
 */
public abstract class AbstractCrystalPlugin extends AbstractUIPlugin {
	/**
	 * This method is called upon plug-in activation. Used to initialize the plugin for first time use. Invokes
	 * setupCrystalAnalyses, which is overridden by CrystalPlugin.java to register any necessary analyses with the
	 * framework.
	 */
	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	public abstract void setupCrystalAnalyses(Crystal crystal);
}
