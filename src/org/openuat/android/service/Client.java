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
import org.openuat.util.Hash;

import android.os.RemoteException;
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

    private boolean isLocalClient = false;
    private OpenUAT_ID id;

    private byte[] oobKey = null;

    public final OpenUAT_ID getId() {
	return id;
    }

    public Client() {
    }

    public Client(RemoteConnection adress,
	    IConnectionCallback connectionCallback) {
	this();
	setRemote(adress);
    }

    /**
     * @param id
     */
    public Client(OpenUAT_ID id) {
	this();
	this.id = id;
    }

    public RemoteConnection getRemoteObject() {
	return remote;
    }

    public void establishConnection() throws IOException {

	Log.d(this.toString(), "openConnection");
	InetAddress adress = TCP.getAvailableClients().get(id);
	if (secureChannel != null) {
	    Log.d(this.toString(), "valid channel present");
	}
	Log.d(this.toString(), "no channel found - creating new one");
	RemoteTCPConnection con = new RemoteTCPConnection(new Socket(adress,
		Constants.TCP_PORT));
	setRemote(con);

	// TODO
	DHwithVerificationHelper.getInstance().startAuthentication(
		getRemoteObject(), Constants.PROTOCOL_TIMEOUT, id.toString());
    }

   

    public void setRemote(final RemoteConnection adress) {
	this.remote = adress;
//	checkIfLocal();
    }

    public void setSecureChannel(final SecureChannel channel) throws RemoteException {
	secureChannel = channel;
	id.getApp().publishChannel(this);
    }

    public final SecureChannel getSecureChannel() {
	return secureChannel;
    }

    public boolean isLocalClient() {
	return isLocalClient;
    }

    public void reset() throws RemoteException {
	setRemote(null);
	setSecureChannel(null);
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

    private volatile Thread verificationTrigger = null;
    private final Thread verificationThread = new Thread(new Runnable() {
	Thread thisThread = null;

	@Override
	public void run() {
	    thisThread = Thread.currentThread();
	    while (verificationTrigger == thisThread) {
		if (DiscoverService.oob_key != null) {
		    boolean result = DiscoverService.oob_key
			    .equalsIgnoreCase(Hash.getHexString(oobKey));

		    if (result) {
			verificationTrigger = null;
			try {
			    DHwithVerificationHelper.getInstance()
				    .verificationSuccess(remote, this,
					    getId().toString());
			} catch (IOException e) {
			    e.printStackTrace();
			}
		    } else {
			try {
			    DHwithVerificationHelper
				    .getInstance()
				    .verificationFailure(true, remote, this,
					    getId().toString(),
					    new Exception(), "invalid OOB code");
			} catch (IOException e) {
			    e.printStackTrace();
			}
		    }
		    DiscoverService.oob_key = null;
		    verificationTrigger = null;
		}
		try {
		    Thread.sleep(Constants.POLLING_INTERVALL);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }

	}
    });

    public void startVerification(byte[] sharedAuthenticationKey) {
	oobKey = sharedAuthenticationKey;
	setVerificationPolling(true);
    }

    private void setVerificationPolling(Boolean status) {
	if (status) {
	    verificationTrigger = verificationThread;
	    verificationThread.start();
	} else {
	    verificationTrigger = null;
	}
    }

    /**
     * @param sharedAuthenticationKey
     */
    public void setOobKey(byte[] sharedAuthenticationKey) {
	oobKey = sharedAuthenticationKey;
    }

    /**
     * @param localId
     * @return
     */
    public static Client createLocalClient(OpenUAT_ID localId) {
	Client client = new Client();
	client.id = localId;
	client.isLocalClient = true;
	return client;
    }

}
