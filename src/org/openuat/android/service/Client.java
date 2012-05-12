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

import org.openuat.android.Constants;
import org.openuat.android.OpenUAT_ID;
import org.openuat.android.service.SecureChannel.VERIFICATION_STATUS;
import org.openuat.android.service.interfaces.IConnectionCallback;
import org.openuat.authentication.AuthenticationProgressHandler;
import org.openuat.authentication.HostProtocolHandler;
import org.openuat.channel.main.RemoteConnection;
import org.openuat.util.Hash;

import android.R;
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

    private RemoteConnection remote = null;
    private SecureChannel secureChannel = null;
    private IConnectionCallback connectionCallback = null;
    private boolean isLocalClient = false;
    private OpenUAT_ID id;

    public final OpenUAT_ID getId() {
	return id;
    }

    /**
     * Instantiates a new client.
     */
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

	    Notification notif = new Notification(R.drawable.ic_dialog_alert,
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
    public RemoteConnection getRemoteObject() {
	return remote;
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
	Log.d(this.toString(), remote.toString());

	if ((secureChannel != null)
		&& secureChannel.getVerificationStatus() == VERIFICATION_STATUS.VERIFICATION_SUCCESS) {
	    return secureChannel;
	}
	Log.d(this.toString(), "opening sockets");

	secureChannel = new SecureChannel(remote);
	HostProtocolHandler.startAuthenticationWith(remote,
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
    public void setRemote(final RemoteConnection adress) {
	this.remote = adress;
    }

    /**
     * Sets the connection.
     * 
     * @param channel
     *            the new connection
     */
    public void setSecureChannel(final SecureChannel channel) {
	secureChannel = channel;
    }

    public final SecureChannel getSecureChannel() {
	return secureChannel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public final String toString() {
	if (remote == null) {
	    return "";
	}
	try {
	    return remote.getRemoteAddress().toString();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return "";
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
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof Client))
	    return false;
	Client other = (Client) obj;
	if (remote == null) {
	    if (other.remote != null)
		return false;
	} else if (!remote.equals(other.remote))
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
