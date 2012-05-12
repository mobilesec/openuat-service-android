/**
 * Copyright Hannes Markschlaeger
 * File created 13.03.2012
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version. 
 */
package org.openuat.android.service.connectiontype;

import org.openuat.android.service.RegisteredApp;

/**
 * The Interface IConnectionType.
 * 
 * 
 * @author Hannes Markschlaeger
 */
public interface IConnectionType {

    /**
     * The Enum CONNECTION_TYPE.
     * 
     * 
     * @author Hannes Markschlaeger
     */
    public static enum CONNECTION_TYPE {

	/** The BLUETOOTH. */
	BLUETOOTH,
	/** The WIFI. */
	WIFI
    };
    
    public String toString();
    /**
     * Adds the app.
     * 
     * @param app
     *            the app
     */
    void addApp(RegisteredApp app);

    /**
     * Removes the app.
     * 
     * @param app
     *            the app
     */
    void removeApp(RegisteredApp app);
}
