/**************************************************************************************************
 * Copyright (c) 2011 Mihail Atanassov. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.rap;

import java.net.MalformedURLException;
import java.net.URL;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.equinox.security.auth.ILoginContext;
import org.eclipse.equinox.security.auth.LoginContextFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.osgi.framework.BundleContext;

import com.quui.sinist.XmlDb;

import de.uni_koeln.ub.drc.ui.DrcUiActivator;
import de.uni_koeln.ub.drc.ui.facades.SessionContextSingleton;
import de.uni_koeln.ub.drc.ui.views.SearchView;

/**
 * @author Mihail Atanassov (matana)
 */
public class EntryPoint implements IEntryPoint {

	/**
	 * The class ID
	 */
	public static final String ID = EntryPoint.class.getName().toLowerCase();
	private XmlDb db;
	private SearchView searchView;
	private final String JAAS_CONFIG_FILE = "jaas_config"; //$NON-NLS-1$
	private String configName = "SIMPLE"; //$NON-NLS-1$

	@Override
	public int createUI() {
		Display display = PlatformUI.createDisplay();
		WorkbenchAdvisor advisor = new ApplicationWorkbenchAdvisor();
		BundleContext bundleContext = DrcUiActivator.getDefault().getBundle()
				.getBundleContext();
		login(bundleContext);
		RWT.getSessionStore().addSessionStoreListener(
				new SessionStoreListener() {

					@Override
					public void beforeDestroy(SessionStoreEvent event) {
						logout();
						redirect();
					}

					private void redirect() {
						final Display display = Display.getCurrent();
						RWT.getLifeCycle().addPhaseListener(
								new PhaseListener() {

									private static final long serialVersionUID = 1L;

									@Override
									public void afterPhase(PhaseEvent event) {
										if (Display.getCurrent() == null
												|| display == Display
														.getCurrent()) {
											// Uses a non-public API, but
											// currently this is the only
											// solution
											JavaScriptResponseWriter writer = ContextProvider
													.getStateInfo()
													.getResponseWriter();
											String url = "http://www.crestomazia.ch/application/users"; //$NON-NLS-1$
											writer.write("window.location.href=\"" + url + "\";"); //$NON-NLS-1$ //$NON-NLS-2$
											RWT.getRequest().getSession()
													.setMaxInactiveInterval(1);
											RWT.getLifeCycle()
													.removePhaseListener(this);
										}
									}

									@Override
									public PhaseId getPhaseId() {
										return PhaseId.ANY;
									}

									@Override
									public void beforePhase(PhaseEvent event) {
									};

								});
					}

					private void logout() {
						try {
							SessionContextSingleton.getInstance()
									.getLoginContext().logout();
						} catch (LoginException e) {
							e.printStackTrace();
						}
					}
				});
		return PlatformUI.createAndRunWorkbench(display, advisor);
	}

	private void login(BundleContext bundleContext) {
		URL configUrl = bundleContext.getBundle().getEntry(JAAS_CONFIG_FILE);
		ILoginContext loginContext = LoginContextFactory.createContext(
				configName, configUrl);
		try {
			loginContext.login();
			SessionContextSingleton.getInstance().setLoginContext(loginContext);
		} catch (LoginException e) {
			e.printStackTrace();
		}
	}

	// /**
	// * @return The data DB we are using
	// */
	// public XmlDb db() {
	// if (db == null) {
	// db = Index.LocalDb().isAvailable() ? Index.LocalDb()
	// : SessionContextSingleton.getInstance().getCurrentUser()
	// .db();
	// if (!db.isAvailable()) {
	// throw new IllegalStateException(
	//						"Could not connect to DB: " + db); //$NON-NLS-1$
	// }
	// }
	// return db;
	// }
	//
	// /**
	// * @return The users DB we are using
	// */
	// public XmlDb userDb() {
	// return Index.LocalDb().isAvailable() ? Index.LocalDb() : new XmlDb(
	//				"bob.spinfo.uni-koeln.de", 8080, "drc", "crd"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	// }

	// /**
	// * @return The user that is currently logged in
	// */
	// public User currentUser() {
	// User user = null;
	// try {
	// ILoginContext context = getLoginContext();
	// user = (User) context.getSubject().getPrivateCredentials()
	// .iterator().next();
	// } catch (LoginException e) {
	// e.printStackTrace();
	// }
	// return user;
	// }

	/**
	 * @return The context for the logged in user.
	 */
	public ILoginContext getLoginContext() {
		return SessionContextSingleton.getInstance().getLoginContext();
	}

	/**
	 * @param location
	 *            The bundle path of the image to load
	 * @return The loaded image
	 */
	public Image loadImage(String location) {
		// IPath path = new Path(location);
		URL url;
		ImageDescriptor desc = ImageDescriptor.createFromImage(new Image(
				Display.getCurrent(), location));
		try {
			url = FileLocator.find(new URL(location));
			desc = ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return desc.createImage();
	}

	/**
	 * @param loginContext
	 *            The ILoginContext
	 */
	public void setLoginContext(ILoginContext loginContext) {
		SessionContextSingleton.getInstance().setLoginContext(loginContext);
		this.searchView.setInput();
		this.searchView.select();
	}

	/**
	 * @param searchView
	 *            The SearchView
	 */
	public void register(SearchView searchView) {
		this.searchView = searchView;
	}

	/**
	 * @param clazz
	 *            The class literal of the view to find (needs an ID field)
	 * @return The view corresponding to the given class literal, or null
	 */
	public static <T extends IViewPart> T find(Class<T> clazz) {
		try {
			String id = (String) clazz.getField("ID").get(null); //$NON-NLS-1$
			@SuppressWarnings("unchecked")
			// safe because class is passed
			T view = (T) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().findView(id);
			return view;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		return null;
	}

}
