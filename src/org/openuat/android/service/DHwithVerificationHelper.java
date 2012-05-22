/* Copyright Hannes Markschläger
 * File created 07.05.2012
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package org.openuat.android.service;

import java.io.IOException;

import org.openuat.android.Constants;
import org.openuat.android.OpenUAT_ID;
import org.openuat.android.dialogs.VerificationQR;
import org.openuat.authentication.DHWithVerification;
import org.openuat.channel.main.HostAuthenticationServer;
import org.openuat.channel.main.RemoteConnection;
import org.openuat.channel.main.ip.TCPPortServer;
import org.openuat.util.Hash;

import android.R;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

public class DHwithVerificationHelper extends DHWithVerification {

    private static DHwithVerificationHelper instance = null;

    public static DHwithVerificationHelper getInstance() throws IOException {
	if (DHwithVerificationHelper.instance == null) {
	    DHwithVerificationHelper.instance = new DHwithVerificationHelper(
		    new TCPPortServer(Constants.TCP_PORT,
			    Constants.PROTOCOL_TIMEOUT,
			    Constants.KEEP_CONNECTED, Constants.USE_JSSE),
		    true, true, true, null, Constants.USE_JSSE);
	    instance.startListening();
	}
	return DHwithVerificationHelper.instance;
    }

    /**
     * @param server
     * @param keepConnectedOnSuccess
     * @param keepConnectedOnFailure
     * @param concurrentVerificationSupported
     * @param instanceId
     * @param useJSSE
     */
    private DHwithVerificationHelper(HostAuthenticationServer server,
	    boolean keepConnectedOnSuccess, boolean keepConnectedOnFailure,
	    boolean concurrentVerificationSupported, String instanceId,
	    boolean useJSSE) {
	super(server, keepConnectedOnSuccess, keepConnectedOnFailure,
		concurrentVerificationSupported, instanceId, useJSSE);
	Log.i(this.toString(), "DHwithVerificationHelper");
    }

    @Override
    protected void startVerificationAsync(byte[] sharedAuthenticationKey,
	    String optionalParam, RemoteConnection toRemote) {

	// incoming connection: start scan-procedure - verificationSuccess -
	// wait for success from remote

	OpenUAT_ID id = OpenUAT_ID.parseToken(optionalParam);
	Client c = id.getApp().getClientById(id);
	if (c != null && !id.getApp().getLocalId().equals(id)) {
	    c.setOobKey(sharedAuthenticationKey);

	    Intent intent = new Intent("com.google.zxing.client.android.ENCODE");
	    intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
	    intent.putExtra("ENCODE_DATA",
		    Hash.getHexString(sharedAuthenticationKey));
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

	    // DiscoverService.mNotificationManager.notify(
	    // Constants.NOTIF_VERIFICATION_CHALLENGE, notif);

	    verificationSuccess(toRemote, c, id.toString());
	    return;
	}

	Intent intent = new Intent(DiscoverService.context,
		VerificationQR.class);

	PendingIntent pendingIntent = PendingIntent.getActivity(
		DiscoverService.context, 0, intent, PendingIntent.FLAG_ONE_SHOT
			| PendingIntent.FLAG_CANCEL_CURRENT);

	Notification notif = new Notification(R.drawable.ic_menu_help,
		"verfication required", System.currentTimeMillis());
	notif.setLatestEventInfo(DiscoverService.context,
		"OpenUAT - Incoming connection",
		"Please verify the connection", pendingIntent);
	notif.flags = Notification.FLAG_NO_CLEAR
		| Notification.FLAG_AUTO_CANCEL;

	// DiscoverService.mNotificationManager.notify(
	// Constants.NOTIF_VERIFICATION_RESPONSE, notif);

	if (c == null) {
	    c = new Client(id);
	    id.getApp().addClient(c);
	}
	// channel = new SecureChannel(toRemote, c);
	c.setRemote(toRemote);
	// c.setSecureChannel(channel);
	// c.startVerification(sharedAuthenticationKey);
	verificationSuccess(toRemote, c, id.toString());

    }

    @Override
    protected void resetHook(RemoteConnection remote) {
	Log.i(this.toString(), "resetHook");
    }

    @Override
    protected void protocolSucceededHook(RemoteConnection remote,
	    Object optionalVerificationId, String optionalParameterFromRemote,
	    byte[] sharedSessionKey) {
	Log.i(this.toString(), "protocolSucceededHook");
	// if (optionalVerificationId != null
	// && optionalVerificationId instanceof Client) {
	// Client c = (Client) optionalVerificationId;
	// SecureChannel channel = new SecureChannel(remote);
	// channel.setSessionKey(sharedSessionKey);
	// try {
	// c.setSecureChannel(channel);
	// } catch (RemoteException e) {
	// channel = null;
	// e.printStackTrace();
	// }
	// }
	if (optionalParameterFromRemote == null
		|| optionalParameterFromRemote.isEmpty()) {
	    return;
	}
	OpenUAT_ID id = OpenUAT_ID.parseToken(optionalParameterFromRemote);
	Client client = id.getApp().getClientById(id);
	SecureChannel channel = new SecureChannel(remote);
	channel.setSessionKey(sharedSessionKey);
	try {
	    client.setSecureChannel(channel);
	} catch (RemoteException e) {
	    e.printStackTrace();
	}

    }

    @Override
    protected void protocolFailedHook(boolean failedHard,
	    RemoteConnection remote, Object optionalVerificationId,
	    Exception e, String message) {
	Log.i(this.toString(), "protocolFailedHook");

	// where is optionalParameterFromRemote?
	if (optionalVerificationId != null
		&& optionalVerificationId instanceof Client) {
	    Client c = (Client) optionalVerificationId;
	    try {
		c.setSecureChannel(null);
	    } catch (RemoteException e1) {
		e1.printStackTrace();
	    }
	}
    }

    @Override
    protected void protocolProgressHook(RemoteConnection remote, int cur,
	    int max, String message) {
	Log.i(this.toString(), "protocolProgressHook");
    }

    @Override
    protected void protocolStartedHook(RemoteConnection remote) {
	Log.i(this.toString(), "protocolStartedHook");
    }

    @Override
    public void verificationSuccess(RemoteConnection remote,
	    Object optionalVerificationId, String optionalParameterToRemote) {
	Log.i(this.toString(), "verificationSuccess");
	super.verificationSuccess(remote, optionalVerificationId,
		optionalParameterToRemote);
    }

    @Override
    public void verificationFailure(boolean failHard, RemoteConnection remote,
	    Object optionalVerificationId, String optionalParameterToRemote,
	    Exception e, String msg) {
	Log.i(this.toString(), "verificationFailure");
	super.verificationFailure(failHard, remote, optionalVerificationId,
		optionalParameterToRemote, e, msg);
    }
}
