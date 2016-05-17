package edu.wayne.pointsto.plugin;

import oog.common.OGraphFacade;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "PointsTo";

	// The shared instance
	private static Activator plugin;

	private OGraphFacade facade;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
//		registerService(context);
		trackService(context);
	}
	


	private void trackService(BundleContext bc) {
		ServiceTracker serviceTracker = new ServiceTracker(bc, "oog.common.OGraphFacade", null);
		// Be sure to call open() before getService()
		serviceTracker.open();
		
		Object service = serviceTracker.getService();
		if(service instanceof OGraphFacade) {
			facade = (OGraphFacade) service;
		}		
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public OGraphFacade getMotherFacade() {
	    return facade;
    }
}
