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

import android.R;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

/**
 * TODO: add class comment.
 * 
 * @author Hannes Markschlaeger
 */
public class DHwithVerificationHelper extends DHWithVerification {

    // int tcpPort;
    // int numResetHookCalled = 0;
    // int numSucceededHookCalled = 0;
    // int numFailedHardHookCalled = 0;
    // int numFailedSoftHookCalled = 0;
    // int numProgressHookCalled = 0;
    // int numStartedHookCalled = 0;
    // byte[] sharedAuthKey = null;
    // byte[] sharedSessKey = null;
    // String param;
    //
    // Object optVerifyIdIn = null, optVerifyIdOut = null;
    // String optParamIn = null, optParamOut = null;

    /**
     * @param server
     * @param keepConnectedOnSuccess
     * @param keepConnectedOnFailure
     * @param concurrentVerificationSupported
     * @param instanceId
     * @param useJSSE
     */
    public DHwithVerificationHelper(HostAuthenticationServer server,
	    boolean keepConnectedOnSuccess, boolean keepConnectedOnFailure,
	    boolean concurrentVerificationSupported, String instanceId,
	    boolean useJSSE) {
	super(server, keepConnectedOnSuccess, keepConnectedOnFailure,
		concurrentVerificationSupported, instanceId, useJSSE);
	Log.i(this.toString(), "DHwithVerificationHelper");
    }

    @Override
    public void startVerificationAsync(byte[] sharedAuthenticationKey,
	    String optionalParam, RemoteConnection toRemote) {
	
	// ### TODO
	SecureChannel chan = null;

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

	DiscoverService.mNotificationManager.notify(
		Constants.NOTIF_VERIFICATION_RESPONSE, notif);

	// final RegisteredApp app = RegisteredAppManager
	// .getServiceByName(optionalParam);

	OpenUAT_ID id = OpenUAT_ID.parseToken(optionalParam);
	Client c = id.getApp().getClientById(id);
	try {

	    // c = app.getClientByRemoteObject(toRemote);
	    if (c == null) {
		c = new Client(id);
	    }
	    chan = new SecureChannel(toRemote, c);
	    c.setRemote(toRemote);
	    c.setSecureChannel(chan);
	    // TODO #####
	    // chan.setVerificationStatus(VERIFICATION_STATUS.VERIFICATION_PENDING);
	    chan.startVerification(sharedAuthenticationKey);
	    // chan.setOobKey(sharedAuthenticationKey);
	    // chan.setProtocol(dhhelper);
	    // final Client tempClient = c;
	    // TODO check why verification code null
	    // chan.setVerificationStatusListener(new
	    // IVerificationStatusListener() {
	    // @Override
	    // public void onVerificationStatusChanged(
	    // VERIFICATION_STATUS newStatus) {
	    // switch (newStatus) {
	    // case VERIFICATION_FAILED:
	    // verificationFailure(true,
	    // tempClient.getRemoteObject(),
	    // tempClient.getId().toString(),
	    // tempClient.getId().toString(),
	    // new Exception(), "verification failed");
	    // break;
	    // case VERIFICATION_PENDING:
	    // break;
	    // case VERIFICATION_SUCCESS:
	    // verificationSuccess(
	    // tempClient.getRemoteObject(),
	    // tempClient.getId().toString(),
	    // tempClient.getId().toString());
	    // break;
	    // default:
	    // break;
	    // }
	    // }
	    // });

	} catch (final IOException e) {
	    // AuthenticationFailure(sender, remote, e, e.getMessage());
	    chan = null;
	    c = null;
	}
	if (c != null) {
	    // app.addClient(c);
	}

    }

    // public void startVerificationAsync(byte[] sharedAuthenticationKey,
    // String optionalParam, RemoteConnection toRemote) {
    // startVerificationAsync(sharedAuthenticationKey, optionalParam, toRemote);
    // }

    @Override
    protected void resetHook(RemoteConnection remote) {
	Log.i(this.toString(), "resetHook");
    }

    @Override
    protected void protocolSucceededHook(RemoteConnection remote,
	    Object optionalVerificationId, String optionalParameterFromRemote,
	    byte[] sharedSessionKey) {

	OpenUAT_ID id = OpenUAT_ID.parseToken(optionalParameterFromRemote);
	Client c = id.getApp().getClientById(id);
	c.getSecureChannel().setSessionKey(sharedSessionKey);
	c.getSecureChannel().setValid(true);
	Log.i(this.toString(), "protocolSucceededHook");
    }

    @Override
    protected void protocolFailedHook(boolean failedHard,
	    RemoteConnection remote, Object optionalVerificationId,
	    Exception e, String message) {
	Log.i(this.toString(), "protocolFailedHook");
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

    public void verificationSuccess(RemoteConnection remote,
	    Object optionalVerificationId, String optionalParameterToRemote) {
	Log.i(this.toString(), "verificationSuccess");
	super.verificationSuccess(remote, optionalVerificationId,
		optionalParameterToRemote);
    }

    public void verificationFailure(boolean failHard, RemoteConnection remote,
	    Object optionalVerificationId, String optionalParameterToRemote,
	    Exception e, String msg) {
	Log.i(this.toString(), "verificationFailure");
	super.verificationFailure(failHard, remote, optionalVerificationId,
		optionalParameterToRemote, e, msg);
    }

}
