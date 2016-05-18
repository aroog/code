package moogrex.plugin;

import java.util.Hashtable;

import oog.common.MotherFacade;
import oog.common.OGraphFacade;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "MOOGREX";

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
		
		trackService(context);
		// registerService(context);
		// XXX. If using the TrackService, we don't need to add the Service lister.
		// addServiceListener(context);
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
	
	private void registerService(BundleContext bc) {
		Hashtable props = new Hashtable();
		facade = MotherFacade.getInstance();
		bc.registerService("oog.common.OGraphFacade", facade , props);
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

	public OGraphFacade getMotherFacade() {
	    return facade;
    }

	public void addServiceListener(final BundleContext bc) {
		ServiceListener sl = new ServiceListener() {
			public void serviceChanged(ServiceEvent ev) {
				ServiceReference sr = ev.getServiceReference();
				switch (ev.getType()) {
				case ServiceEvent.REGISTERED:
					Object service = bc.getService(sr);
					if(service instanceof OGraphFacade) {
						facade = (OGraphFacade) service;
					}
					break;
				default:
					break;
				}
			}
		};

		String filter = "(objectclass=" + "oog.common.OGraphFacade"+ ")";
		try {
			bc.addServiceListener(sl, filter);
		}
		catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
			
		ServiceReference[] srl = null;
        try {
	        srl = bc.getServiceReferences((String)null, filter);
        }
        catch (InvalidSyntaxException e) {
	        e.printStackTrace();
        }
		for (int i = 0; srl != null && i < srl.length; i++) {
			sl.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, srl[i]));
		}		
	}
}
