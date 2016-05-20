package oog.ui;

import java.util.Hashtable;

import oog.common.MotherFacade;
import oog.common.OGraphFacade;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "OOG.UI";

	// The shared instance
	private static Activator plugin;
	
	private OGraphFacade facade;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	private void registerService(BundleContext bc) {
	    Hashtable props = new Hashtable();
		facade = MotherFacade.getInstance();
		bc.registerService("oog.common.OGraphFacade", facade , props);
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
		// Service not yet registered, so register it!
		else  {
			registerService(bc);
		}

    }
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		
		// TODO: Unregister service
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public OGraphFacade getMotherFacade() {
	    return facade;
    }
}
