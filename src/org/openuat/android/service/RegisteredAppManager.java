/**
 * Copyright Hannes Markschlaeger
 * File created 13.03.2012
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version. 
 */
package org.openuat.android.service;

import java.util.ArrayList;
import java.util.List;

import org.openuat.android.service.connectiontype.IConnectionType.CONNECTION_TYPE;
import org.openuat.android.service.connectiontype.TCP;

/**
 * This class contains all applications registered to the service.
 * 
 * 
 * @author Hannes Markschlaeger
 */
public final class RegisteredAppManager {

	/**
	 * The list of registered apps.
	 */
	private static List<RegisteredApp> mApps = new ArrayList<RegisteredApp>();

	/**
	 * Instantiates a new registered app manager.
	 */
	private RegisteredAppManager() {
	}

	/**
	 * Gets the service by name.
	 * 
	 * @param name
	 *            the name
	 * @return the service by name
	 */
	public static RegisteredApp getServiceByNameAndConnType(final String name,
			final CONNECTION_TYPE connectionType) {
		for (final RegisteredApp s : mApps) {
			if (s.getName().equalsIgnoreCase(name)
					&& s.getConnection() == connectionType) {
				return s;
			}
		}
		return null;
	}

	/**
	 * Registers a new application. No duplicates will be created.
	 * 
	 * @param app
	 *            the application to register.
	 */
	public static void registerService(final RegisteredApp app) {
		if (!mApps.contains(app)) {
			mApps.add(app);
		}
		switch (app.getConnection()) {
		case WIFI:
			TCP.getInstance().addApp(app);
			break;
		case BLUETOOTH:
			break;
		default:
			break;
		}
	}

	/**
	 * Unregisters a service
	 * 
	 * @param app
	 *            the application to remove.
	 */
	public static void unregisterService(final RegisteredApp app) {
		if ((app.getNumberOfClients() == 0) && !mApps.contains(app)) {
			mApps.remove(app);
			TCP.getInstance().removeApp(app);
		}
	}

}
