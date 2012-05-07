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

import org.openuat.android.service.connectiontype.TCP;

/**
 * The Class RegisteredAppManager.
 * 
 * 
 * @author Hannes Markschlaeger
 */
public final class RegisteredAppManager {

    /** The m apps. */
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
    public static RegisteredApp getServiceByName(final String name) {
	for (final RegisteredApp s : mApps) {
	    if (s.getName().equalsIgnoreCase(name)) {
		return s;
	    }
	}
	return null;
    }

    /**
     * Gets the service of client.
     * 
     * @param client
     *            the client
     * @return the service of client
     */
    public static RegisteredApp getServiceOfClient(final Client client) {
	for (final RegisteredApp app : mApps) {
	    if (app.getClients().contains(client)) {
		return app;
	    }
	}
	return null;
    }

    /**
     * Register service.
     * 
     * @param app
     *            the app
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
     * Unregister service.
     * 
     * @param app
     *            the app
     */
    public static void unregisterService(final RegisteredApp app) {
	if ((app.getNumberOfClients() == 0) && !mApps.contains(app)) {
	    mApps.remove(app);
	    TCP.getInstance().removeApp(app);
	}
    }

}
