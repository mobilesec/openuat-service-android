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
import org.openuat.authentication.AuthenticationProgressHandler;
import org.openuat.authentication.DHWithVerification;
import org.openuat.authentication.HostProtocolHandler;
import org.openuat.channel.main.RemoteConnection;
import org.openuat.channel.main.ip.RemoteTCPConnection;
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
public class Client {

    private RemoteConnection remote = null;
    private SecureChannel secureChannel = null;

    public AuthenticationProgressHandler getAuthenticationHandler() {
	return authenticationHandler;
    }

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
    public final AuthenticationProgressHandler authenticationHandler = new AuthenticationProgressHandler() {

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

	    // TCPPortServerHandler.getInstance().dhhelper.startVerificationAsync(
	    // sharedOObMsg, null, Client.this.remote);
	    // TCPPortServerHandler.getInstance().dhhelper.verificationSuccess(
	    // Client.this.remote, null, id.getApp().getLocalId()
	    // .toString());
	    // TODO get from partner when verification was succesfull!
	    // secureChannel
	    // .setVerificationStatus(VERIFICATION_STATUS.VERIFICATION_PENDING);
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
    public SecureChannel establishConnection() throws IOException {
	Log.d(this.toString(), "openConnection");
	// Log.d(this.toString(), remote.toString());
	//
	// if ((secureChannel != null)
	// && secureChannel.getVerificationStatus() ==
	// VERIFICATION_STATUS.VERIFICATION_SUCCESS) {
	// return secureChannel;
	// }
	// Log.d(this.toString(), "opening sockets");
	//
	// secureChannel = new SecureChannel(remote);
	// HostProtocolHandler.startAuthenticationWith(remote,
	// authenticationHandler, -1, true, RegisteredAppManager
	// .getServiceOfClient(this).getName(), true);
	// return secureChannel;

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
	
//	HostProtocolHandler.startAuthenticationWith(getRemoteObject(),
//		authenticationHandler, Constants.PROTOCOL_TIMEOUT,
//		Constants.KEEP_CONNECTED, id.getApp().getLocalId().toString(),
//		Constants.USE_JSSE);
//
	
	TCPPortServerHandler.getInstance().dhhelper.startAuthentication(
		getRemoteObject(), Constants.PROTOCOL_TIMEOUT, id.getApp()
			.getLocalId().toString());
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

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append(id);
	return builder.toString();
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

}
