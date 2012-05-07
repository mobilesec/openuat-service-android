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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;

import org.openuat.android.Constants;
import org.openuat.android.R;
import org.openuat.android.service.SecureChannel.VERIFICATION_STATUS;
import org.openuat.android.service.interfaces.IConnectionCallback;
import org.openuat.authentication.AuthenticationProgressHandler;
import org.openuat.authentication.HostProtocolHandler;
import org.openuat.channel.main.ip.RemoteTCPConnection;
import org.openuat.util.Hash;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

/**
 * The Class Client.
 * 
 * 
 * @author Hannes Markschlaeger
 */
public class Client implements IVerificationStatusListener {

    private Inet4Address adress = null;
    private SecureChannel secureChannel = null;
    private IConnectionCallback connectionCallback = null;
    private boolean isLocalClient = false;

    /**
     * Instantiates a new client.
     */
    public Client() {
    }

    public Client(Inet4Address adress, IConnectionCallback connectionCallback) {
	this.adress = adress;
	this.connectionCallback = connectionCallback;
	isLocalClient = adress.getHostAddress().equalsIgnoreCase(
		Util.getipAddress().getHostAddress());

    }

    /** The authentication handler. */
    private final AuthenticationProgressHandler authenticationHandler = new AuthenticationProgressHandler() {

	@Override
	public void AuthenticationFailure(final Object sender,
		final Object remote, final Exception e, final String msg) {
	    Log.d(this.toString(), "AuthFailure");
	    secureChannel = null;
	}

	@Override
	public void AuthenticationProgress(final Object sender,
		final Object remote, final int cur, final int max,
		final String msg) {
	    Log.d(this.toString(), "AuthProgress");

	}

	@Override
	public boolean AuthenticationStarted(final Object sender,
		final Object remote) {
	    Log.d(this.toString(), "AuthStarted");
	    return true;
	}

	@Override
	public void AuthenticationSuccess(final Object sender,
		final Object remote, final Object result) {
	    Log.d(this.toString(), "AuthSuccess");

	    Object[] res = (Object[]) result;
	    byte[] sharedSessionKey = (byte[]) res[0];
	    byte[] sharedOObMsg = (byte[]) res[1];

	    secureChannel.setSessionKey(sharedSessionKey);
	    secureChannel.setOobKey(sharedOObMsg);

	    Intent intent = new Intent("com.google.zxing.client.android.ENCODE");
	    intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
	    intent.putExtra("ENCODE_DATA", Hash.getHexString(sharedOObMsg));
	    intent.putExtra("ENCODE_SHOW_CONTENTS", true);

	    Notification notif = new Notification(R.drawable.ic_launcher,
		    "Verification required", System.currentTimeMillis());

	    PendingIntent pendingIntent = PendingIntent.getActivity(
		    DiscoverService.context, 0, intent,
		    PendingIntent.FLAG_CANCEL_CURRENT
			    | PendingIntent.FLAG_ONE_SHOT);

	    notif.setLatestEventInfo(DiscoverService.context,
		    "OpenUAT - Opening connection",
		    "This code has to be verified by the other side",
		    pendingIntent);
	    notif.flags = Notification.FLAG_NO_CLEAR
		    | Notification.FLAG_AUTO_CANCEL;

	    DiscoverService.mNotificationManager.notify(
		    Constants.NOTIF_VERIFICATION_CHALLENGE, notif);

	    
	    // TODO get from partner when verification was succesfull!
	    secureChannel
		    .setVerificationStatus(VERIFICATION_STATUS.VERIFICATION_PENDING);
	}
    };

    /**
     * Gets the adress.
     * 
     * @return the adress
     */
    public InetAddress getAdress() {
	return adress;
    }

    /**
     * Open connection.
     * 
     * @return the secure channel
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public SecureChannel openConnection() throws IOException {
	Log.d(this.toString(), "openConnection");
	Log.d(this.toString(), adress.toString());

	if ((secureChannel != null)
		&& secureChannel.getVerificationStatus() == VERIFICATION_STATUS.VERIFICATION_SUCCESS) {
	    return secureChannel;
	}
	RemoteTCPConnection remoteTcp = null;

	Log.d(this.toString(), "opening sockets");
	remoteTcp = new RemoteTCPConnection(new Socket(adress,
		Protocol.TCP_PORT));

	secureChannel = new SecureChannel(remoteTcp);
	HostProtocolHandler.startAuthenticationWith(remoteTcp,
		authenticationHandler, -1, true, RegisteredAppManager
			.getServiceOfClient(this).getName(), true);
	return secureChannel;

    }

    /**
     * Sets the adress.
     * 
     * @param adress
     *            the new adress
     */
    public void setAdress(final Inet4Address adress) {
	this.adress = adress;
    }

    /**
     * Sets the connection.
     * 
     * @param channel
     *            the new connection
     */
    public void setConnection(final SecureChannel channel) {
	secureChannel = channel;
	secureChannel.setVerificationStatusListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {
	if (adress == null) {
	    return "";
	}
	return adress.getHostAddress();
    }

    /**
     * @return the connectionCallback
     */
    public IConnectionCallback getConnectionCallback() {
	return connectionCallback;
    }

    /**
     * @param connectionCallback
     *            the connectionCallback to set
     */
    public void setConnectionCallback(IConnectionCallback connectionCallback) {
	this.connectionCallback = connectionCallback;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((adress == null) ? 0 : adress.hashCode());
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
	if (adress == null) {
	    if (other.adress != null)
		return false;
	} else if (!adress.equals(other.adress))
	    return false;
	return true;
    }

    /**
     * @return
     */
    public boolean isLocalClient() {
	return isLocalClient;
    }

    @Override
    public void onVerificationStatusChanged(VERIFICATION_STATUS newStatus) {
	switch (newStatus) {
	case VERIFICATION_FAILED:
	    // TODO inform partner when verification was successful!
	    Log.i("Client", "VERIFICATION_FAILED");
	    secureChannel = null;
	    break;
	case VERIFICATION_PENDING:
	    break;
	case VERIFICATION_SUCCESS:
	    break;
	default:
	    break;
	}

    }
}
