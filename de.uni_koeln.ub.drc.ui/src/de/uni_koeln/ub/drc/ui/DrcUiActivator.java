package de.uni_koeln.ub.drc.ui;

import java.net.URL;
import java.util.Collections;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.auth.ILoginContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.BundleContext;

import com.quui.sinist.XmlDb;

import de.uni_koeln.ub.drc.data.Index;
import de.uni_koeln.ub.drc.data.User;
import de.uni_koeln.ub.drc.ui.views.SearchView;

/**
 * The activator class controls the plug-in life cycle
 */
public class DrcUiActivator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "de.uni_koeln.ub.drc.ui"; //$NON-NLS-1$
	// The shared instance
	private static DrcUiActivator plugin;
	private XmlDb db;
	//	private static final String JAAS_CONFIG_FILE = "jaas_config"; //$NON-NLS-1$
	private ILoginContext loginContext;
	private SearchView searchView;

	/**
	 * The user ID of the OCR
	 */
	public static final String OCR_ID = "OCR"; //$NON-NLS-1$

	/**
	 * The portal page root address for a user
	 */
	public static final String PROFILE_ROOT = "http://hydra1.spinfo.uni-koeln.de:9000/application/user?id="; //$NON-NLS-1$

	/**
	 * The constructor
	 */
	public DrcUiActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		// login(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
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
	public static DrcUiActivator getDefault() {
		return plugin;
	}

	// /**
	// * Returns an image descriptor for the image file at the given plug-in
	// * relative path
	// *
	// * @param path
	// * the path
	// * @return the image descriptor
	// */
	// public static ImageDescriptor getImageDescriptor(String path) {
	// return imageDescriptorFromPlugin(PLUGIN_ID, path);
	// }

	/**
	 * @return The data DB we are using
	 */
	public XmlDb db() {
		if (db == null) {
			db = Index.LocalDb().isAvailable() ? Index.LocalDb()
					: currentUser().db();
			if (!db.isAvailable()) {
				throw new IllegalStateException(
						"Could not connect to DB: " + db); //$NON-NLS-1$
			}
			getLog().log(new Status(IStatus.INFO, PLUGIN_ID, "Using DB: " + db)); //$NON-NLS-1$
		}
		return db;
	}

	/**
	 * @return The users DB we are using
	 */
	public XmlDb userDb() {
		return Index.LocalDb().isAvailable() ? Index.LocalDb() : new XmlDb(
				"hydra1.spinfo.uni-koeln.de", 8080, "guest", "guest"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @return The user that is currently logged in
	 */
	// public User currentUser() {
	// return User.withId(Index.DefaultCollection(), db, "matana");
	// }

	public User currentUser() {
		User user = null;
		try {
			ILoginContext context = getLoginContext();
			user = (User) context.getSubject().getPrivateCredentials()
					.iterator().next();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		return user;
	}

	/**
	 * @return The context for the logged in user.
	 */
	public ILoginContext getLoginContext() {
		return loginContext;
	}

	/**
	 * @param location
	 *            The bundle path of the image to load
	 * @return The loaded image
	 */
	public Image loadImage(String location) {
		IPath path = new Path(location);
		URL url = FileLocator.find(this.getBundle(), path,
				Collections.EMPTY_MAP);
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		return desc.createImage();
	}

	// private void login(BundleContext bundleContext) throws Exception {
	//		String configName = "SIMPLE"; //$NON-NLS-1$
	// URL configUrl = bundleContext.getBundle().getEntry(JAAS_CONFIG_FILE);
	// loginContext = LoginContextFactory.createContext(configName, configUrl);
	// try {
	// loginContext.login();
	// } catch (LoginException e) {
	// IStatus status = new Status(IStatus.ERROR,
	//					"de.uni_koeln.ub.drc.ui", "Login failed", e); //$NON-NLS-1$ //$NON-NLS-2$
	// int result = ErrorDialog.openError(null, Messages.Error,
	// Messages.LoginFailed, status);
	// if (result == ErrorDialog.CANCEL) {
	// stop(bundleContext);
	// System.exit(0);
	// } else {
	// login(bundleContext);
	// }
	// }
	// }

	public void setLoginContext(ILoginContext loginContext) {
		this.loginContext = loginContext;
		this.searchView.setInput();
	}

	public void register(SearchView searchView) {
		this.searchView = searchView;
	}

}
