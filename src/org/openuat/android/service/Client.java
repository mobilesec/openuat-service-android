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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.openuat.android.Constants;
import org.openuat.android.OpenUAT_ID;
import org.openuat.android.service.connectiontype.TCP;
import org.openuat.android.service.interfaces.IConnectionCallback;
import org.openuat.channel.main.RemoteConnection;
import org.openuat.channel.main.ip.RemoteTCPConnection;

import android.util.Log;

/**
 * The Class Client.
 * 
 * 
 * @author Hannes Markschlaeger
 */
public class Client {

    private RemoteConnection remote = null;
    private SecureChannel secureChannel = null;

    private IConnectionCallback connectionCallback = null;
    private boolean isLocalClient = false;
    private OpenUAT_ID id;

    public final OpenUAT_ID getId() {
	return id;
    }

    public Client() {
    }

    public Client(RemoteConnection adress,
	    IConnectionCallback connectionCallback) {
	this.remote = adress;
	this.connectionCallback = connectionCallback;
	try {
	    isLocalClient = ((InetAddress) adress.getRemoteAddress())
		    .toString().equalsIgnoreCase(
			    Util.getipAddress().getHostAddress());
	} catch (IOException e) {
	    isLocalClient = false;
	    e.printStackTrace();
	}

    }

    /**
     * @param id
     */
    public Client(OpenUAT_ID id) {
	this.id = id;
    }

    public RemoteConnection getRemoteObject() {
	return remote;
    }

    public SecureChannel establishConnection() throws IOException {

	Log.d(this.toString(), "openConnection");
	InetAddress adress = TCP.getAvailableClients().get(id);
	if ((secureChannel != null) && secureChannel.isValid()) {
	    Log.d(this.toString(), "valid channel present");
	    return secureChannel;
	}
	Log.d(this.toString(), "no channel found - creating new one");
	RemoteTCPConnection con = new RemoteTCPConnection(new Socket(adress,
		Constants.TCP_PORT));
	setRemote(con);
	setSecureChannel(new SecureChannel(con, this));

	// TODO
	DHwithVerificationHelper.getInstance().startAuthentication(
		getRemoteObject(), Constants.PROTOCOL_TIMEOUT, id.toString());
	return secureChannel;

    }

    public void setRemote(final RemoteConnection adress) {
	this.remote = adress;
    }

    public void setSecureChannel(final SecureChannel channel) {
	secureChannel = channel;
    }

    public final SecureChannel getSecureChannel() {
	return secureChannel;
    }

    public IConnectionCallback getConnectionCallback() {
	return connectionCallback;
    }

    public void setConnectionCallback(IConnectionCallback connectionCallback) {
	this.connectionCallback = connectionCallback;
    }

    public boolean isLocalClient() {
	return isLocalClient;
    }

    public void reset() {
	remote = null;
	secureChannel = null;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((id == null) ? 0 : id.hashCode());
	result = prime * result + ((remote == null) ? 0 : remote.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof Client))
	    return false;
	Client other = (Client) obj;
	if (id == null) {
	    if (other.id != null)
		return false;
	} else if (!id.equals(other.id))
	    return false;
	if (remote == null) {
	    if (other.remote != null)
		return false;
	} else if (!remote.equals(other.remote))
	    return false;
	return true;
    }

    @Override
    public String toString() {
	return id.toString();
    }

}
