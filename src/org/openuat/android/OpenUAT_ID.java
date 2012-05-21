/* Copyright Hannes Markschläger
 * File created 12.05.2012
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package org.openuat.android;

import org.openuat.android.service.RegisteredApp;
import org.openuat.android.service.RegisteredAppManager;
import org.openuat.android.service.connectiontype.IConnectionType.CONNECTION_TYPE;
import org.openuat.authentication.exceptions.InternalApplicationException;
import org.openuat.util.Hash;

/**
 * TODO: add class comment.
 * 
 * @author Hannes Markschlaeger
 */
public class OpenUAT_ID {

    private CONNECTION_TYPE connection_type;
    private RegisteredApp app = null;
    private String androidId = null;
    private String hash = null;

    /**
     * @param connection_type
     * @param app
     * @param androidId
     */
    public OpenUAT_ID(CONNECTION_TYPE connection_type, RegisteredApp app,
	    String androidId) {
	this.connection_type = connection_type;
	this.app = app;
	this.androidId = androidId;
	hash = getHash();
    }

    public String getHash() {
	if (hash == null) {
	    try {
		hash = Hash.getHexString(Hash.SHA256(toString().getBytes(),
			true));
	    } catch (InternalApplicationException e) {
		e.printStackTrace();
	    }
	}
	return hash;
    }

    public static OpenUAT_ID parseToken(String token) {

	String[] tokens = token.split("_");

	if (tokens.length != 3) {
	    return null;
	}

	int index = 0;
	String androidId = tokens[index++];
	RegisteredApp app = RegisteredAppManager
		.getServiceByName(tokens[index++]);
	CONNECTION_TYPE connection_type = CONNECTION_TYPE
		.valueOf(tokens[index++]);

	if (androidId == "" || app == null || connection_type == null) {
	    return null;
	}
	return new OpenUAT_ID(connection_type, app, androidId);
    }

    @Override
    public String toString() {
	StringBuilder strb = new StringBuilder();
	strb.append(androidId);
	strb.append("_");
	strb.append(app.toString());
	strb.append("_");
	strb.append(connection_type.toString());
	return strb.toString();
    }

    public final RegisteredApp getApp() {
	return app;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result
		+ ((androidId == null) ? 0 : androidId.hashCode());
	result = prime * result + ((app == null) ? 0 : app.hashCode());
	result = prime * result
		+ ((connection_type == null) ? 0 : connection_type.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof OpenUAT_ID))
	    return false;
	OpenUAT_ID other = (OpenUAT_ID) obj;
	if (androidId == null) {
	    if (other.androidId != null)
		return false;
	} else if (!androidId.equals(other.androidId))
	    return false;
	if (app == null) {
	    if (other.app != null)
		return false;
	} else if (!app.equals(other.app))
	    return false;
	if (connection_type != other.connection_type)
	    return false;
	return true;
    }

}
