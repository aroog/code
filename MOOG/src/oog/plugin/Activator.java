package oog.plugin;

import java.util.Hashtable;

import oog.common.MotherFacade;
import oog.common.OGraphFacade;

import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;


/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends org.eclipse.core.runtime.Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "MOOG";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
		super();
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bc) throws Exception {
		super.start(bc);
		
		// registerService(bc);
		// trackService(bc);
}

	private void trackService(BundleContext bc) {
		ServiceTracker serviceTracker = new ServiceTracker(bc, "oog.common.OGraphFacade", null);
		// Be sure to call open() before getService()
		serviceTracker.open();

		Object service = serviceTracker.getService();
		if (service instanceof OGraphFacade) {
		// MotherFacade.setInstance((OGraphFacade) service);
		}
	}

//	private void registerService(BundleContext bc) {
//	    Hashtable props = new Hashtable();
//		OGraphFacade instance = MotherFacade.getInstance();
//		bc.registerService("oog.common.OGraphFacade", instance , props);
//    }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bc) throws Exception {
		super.stop(bc);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return Activator.plugin;
	}
}
