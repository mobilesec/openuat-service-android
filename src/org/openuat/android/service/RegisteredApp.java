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

import java.net.InetAddress;
import java.util.ArrayList;

import org.openuat.android.service.connectiontype.IConnectionType;

import android.util.Log;

/**
 * The Class RegisteredApp.
 * 
 * 
 * @author Hannes Markschlaeger
 */
public class RegisteredApp {

    /** The is discovering. */
    private boolean isDiscovering = true;

    /** The m clients. */
    private ArrayList<Client> mClients = null;

    /** The m connection. */
    private final IConnectionType.CONNECTION_TYPE mConnection;

    /** The m name. */
    private String mName = null;

    /**
     * Instantiates a new registered app.
     * 
     * @param service
     *            the service
     * @param connection
     *            the connection
     */
    public RegisteredApp(final String service,
	    final IConnectionType.CONNECTION_TYPE connection) {
	mName = service;
	mClients = new ArrayList<Client>();
	mConnection = connection;
    }

    /**
     * Adds the client.
     * 
     * @param client
     *            the client
     */
    public void addClient(final Client client) {
	if (!mClients.contains(client)) {
	    mClients.add(client);
	}
    }

    public ArrayList<Client> getClients() {
	return mClients;
    }

    /**
     * Gets the connection.
     * 
     * @return the connection
     */
    public IConnectionType.CONNECTION_TYPE getConnection() {
	return mConnection;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
	return mName;
    }

    /**
     * Gets the number of clients.
     * 
     * @return the number of clients
     */
    public int getNumberOfClients() {
	return mClients.size();
    }

    /**
     * Checks if is discovering.
     * 
     * @return true, if is discovering
     */
    public boolean isDiscovering() {
	return isDiscovering;
    }

    // /**
    // * On message received.
    // *
    // * @param recControl
    // * the rec control
    // * @param recMessage
    // * the rec message
    // */
    // public void onMessageReceived(final String recControl,
    // final String recMessage) {
    //
    // }
    public Client getClientByAdress(final InetAddress adress) {
	Log.d(this.toString(), "getClientbyIp " + adress.toString());
	for (final Client c : mClients) {
	    if (c.getAdress().equals(adress)) {
		Log.d(this.toString(), "client found " + c.toString());
		return c;
	    }
	}
	Log.d(this.toString(), "no client found");
	return null;
    }

    /**
     * Sets the discovering.
     * 
     * @param isDiscovering
     *            the new discovering
     */
    public void setDiscovering(final boolean isDiscovering) {
	if (isDiscovering) {
	    mClients.clear();
	}
	this.isDiscovering = isDiscovering;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return mName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (isDiscovering ? 1231 : 1237);
	result = prime * result
		+ ((mClients == null) ? 0 : mClients.hashCode());
	result = prime * result
		+ ((mConnection == null) ? 0 : mConnection.hashCode());
	result = prime * result + ((mName == null) ? 0 : mName.hashCode());
	return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof RegisteredApp)) {
	    return false;
	}
	RegisteredApp other = (RegisteredApp) obj;
	if (isDiscovering != other.isDiscovering) {
	    return false;
	}
	if (mClients == null) {
	    if (other.mClients != null) {
		return false;
	    }
	} else if (!mClients.equals(other.mClients)) {
	    return false;
	}
	if (mConnection != other.mConnection) {
	    return false;
	}
	if (mName == null) {
	    if (other.mName != null) {
		return false;
	    }
	} else if (!mName.equals(other.mName)) {
	    return false;
	}
	return true;
    }

}
