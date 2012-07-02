package de.uni_koeln.ub.drc.ui.facades;

import org.eclipse.equinox.security.auth.ILoginContext;
import org.eclipse.swt.graphics.Image;

import com.quui.sinist.XmlDb;

import de.uni_koeln.ub.drc.data.User;

/**
 * @author matanassov
 * 
 */
public interface ISessionContextSingletonProvider {

	/**
	 * @return Implementation
	 */
	Object getInstanceInternal();

	/**
	 * @param loginContext
	 *            The session context for the logged in user.
	 */
	public void setLoginContext(ILoginContext loginContext);

	/**
	 * @return Login context
	 */
	public ILoginContext getLoginContext();

	/**
	 * @return The user that is currently logged in
	 */
	public User getCurrentUser();

	/**
	 * @return The users DB we are using
	 */
	public XmlDb getUserDb();

	/**
	 * @return The data DB we are using
	 */
	public XmlDb db();

	/**
	 * @param location
	 *            The bundle path of the image to load
	 * @return The loaded image
	 */
	public Image loadImage(String location);

}
