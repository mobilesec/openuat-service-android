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
import java.util.ArrayList;

import org.openuat.android.Constants;
import org.openuat.android.R;
import org.openuat.android.dialogs.VerificationQR;
import org.openuat.android.service.SecureChannel.VERIFICATION_STATUS;
import org.openuat.authentication.AuthenticationProgressHandler;
import org.openuat.channel.main.ip.RemoteTCPConnection;
import org.openuat.channel.main.ip.TCPPortServer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

/**
 * The Class TCPPortServerHandler.
 * 
 * 
 * @author Hannes Markschlaeger
 */
public final class TCPPortServerHandler {

    /** The instance. */
    private static TCPPortServerHandler instance = null;

    /** The tcp port server. */
    private static TCPPortServer tcpPortServer = null;

    private ArrayList<RemoteTCPConnection> attemptingClients = null;

    /**
     * Gets the single instance of TCPPortServerHandler.
     * 
     * @return single instance of TCPPortServerHandler
     */
    public static TCPPortServerHandler getInstance() {
	if (TCPPortServerHandler.instance == null) {
	    TCPPortServerHandler.instance = new TCPPortServerHandler();
	}
	return TCPPortServerHandler.instance;
    }

    // AuthHandler for incoming
    /** The authentication progress handler. */
    private final AuthenticationProgressHandler authenticationProgressHandler = new AuthenticationProgressHandler() {
	private RemoteTCPConnection conn = null;

	@Override
	public void AuthenticationFailure(final Object sender,
		final Object remote, final Exception e, final String msg) {
	    Log.d(this.toString(), "AuthFailure");
	    attemptingClients.remove(remote);
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

	    attemptingClients.add((RemoteTCPConnection) remote);
	    return true;
	}

	@Override
	public void AuthenticationSuccess(final Object sender,
		final Object remote, final Object result) {
	    Log.d(this.toString(), "AuthSuccess");

	    SecureChannel chan = null;
	    RemoteTCPConnection con = null;
	    RegisteredApp app = null;

	    Object[] res = (Object[]) result;
	    byte[] sharedSessionKey = (byte[]) res[0];
	    final byte[] sharedOObMsg = (byte[]) res[1];

	    Log.i("Client", new String(sharedSessionKey));
	    Log.i("Client", new String(sharedOObMsg));

	    Intent intent = new Intent(DiscoverService.context,
		    VerificationQR.class);

	    PendingIntent pendingIntent = PendingIntent.getActivity(
		    DiscoverService.context, 0, intent,
		    PendingIntent.FLAG_ONE_SHOT
			    | PendingIntent.FLAG_CANCEL_CURRENT);

	    Notification notif = new Notification(R.drawable.ic_launcher,
		    "verfication required", System.currentTimeMillis());
	    notif.setLatestEventInfo(DiscoverService.context,
		    "OpenUAT - Incoming connection",
		    "Please verify the connection", pendingIntent);
	    notif.flags = Notification.FLAG_NO_CLEAR
		    | Notification.FLAG_AUTO_CANCEL;

	    DiscoverService.mNotificationManager.notify(
		    Constants.NOTIF_VERIFICATION_RESPONSE, notif);

	    if (res.length > 2) {
		Log.i("TCPPortServerHandler", (String) res[2]);
	    }

	    app = RegisteredAppManager.getServiceByName((String) res[2]);

	    Client c = null;
	    try {
		con = (RemoteTCPConnection) remote;
		chan = new SecureChannel(con);
		c = app.getClientByAdress(con.getSocketReference()
			.getInetAddress());
		if (c == null) {
		    c = new Client();
		    c.setAdress((Inet4Address) con.getSocketReference()
			    .getInetAddress());
		}
		chan.setSessionKey(sharedSessionKey);
		chan.setOobKey(sharedOObMsg);
		chan.setVerificationStatus(VERIFICATION_STATUS.VERIFICATION_PENDING);
		c.setConnection(chan);

	    } catch (final IOException e) {
		AuthenticationFailure(sender, remote, e, e.getMessage());
		chan = null;
		c = null;
	    }
	    if (c != null) {
		app.addClient(c);
	    }
	}
    };

    /**
     * Instantiates a new tCP port server handler.
     */
    private TCPPortServerHandler() {
	Log.i(this.toString(), "starting tcpserver");
	TCPPortServerHandler.tcpPortServer = new TCPPortServer(
		Protocol.TCP_PORT, -1, true, false);
	try {
	    TCPPortServerHandler.tcpPortServer
		    .addAuthenticationProgressHandler(authenticationProgressHandler);
	    TCPPortServerHandler.tcpPortServer.start();
	} catch (final IOException e) {
	    e.printStackTrace();
	}

	attemptingClients = new ArrayList<RemoteTCPConnection>();
    }
}
