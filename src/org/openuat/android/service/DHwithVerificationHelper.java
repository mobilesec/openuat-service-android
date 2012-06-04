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
import org.openuat.channel.main.RemoteConnection;
import org.openuat.channel.main.ip.TCPPortServer;
import org.openuat.util.Hash;

import android.R;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

/**
 * This class implements {@link DHWithVerification} and manages the
 * authentication and verification process.
 * 
 * @author Hannes Markschläger
 * 
 */
public class DHwithVerificationHelper extends DHWithVerification {

	/**
	 * Flag to enable verification using QR-codes. Set false to verify new
	 * connections immediately.
	 */
	private static final boolean ENABLE_QRVERIFICATION = true;
	private static DHwithVerificationHelper instance = null;

	/**
	 * Gets an instance of {@link DHwithVerificationHelper}. Starts listening
	 * for incoming connections.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static DHwithVerificationHelper getInstance() throws IOException {
		if (DHwithVerificationHelper.instance == null) {
			DHwithVerificationHelper.instance = new DHwithVerificationHelper();
			instance.startListening();
		}
		return DHwithVerificationHelper.instance;
	}

	/**
	 * Private constructor. Creates a new instance.
	 * {@link DHwithVerificationHelper}.
	 */
	private DHwithVerificationHelper() {
		super(new TCPPortServer(Constants.TCP_PORT, Constants.PROTOCOL_TIMEOUT,
				Constants.KEEP_CONNECTED, Constants.USE_JSSE), true, true,
				true, null, Constants.USE_JSSE);
		Log.i(this.toString(), "DHwithVerificationHelper");
	}

	@Override
	protected void startVerificationAsync(byte[] sharedAuthenticationKey,
			String optionalParam, RemoteConnection toRemote) {

		// incoming connection: start scan-procedure - verificationSuccess -
		// wait for success from remote

		OpenUAT_ID id = OpenUAT_ID.parseToken(optionalParam);
		Client c = id.getApp().getClientById(id);
		// If the requesting client passed as optionalParameter is a local
		// client a notification containing an activity to show a QR-code
		// representation of the
		// sharedAuthenticationKey will be started.
		if (c != null && !id.getApp().getLocalId().equals(id)) {
			c.setOobKey(sharedAuthenticationKey);

			// Create an intent starting a new activity to show the QR-code.
			Intent intent = new Intent("com.google.zxing.client.android.ENCODE");
			intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
			intent.putExtra("ENCODE_DATA",
					Hash.getHexString(sharedAuthenticationKey));
			intent.putExtra("ENCODE_SHOW_CONTENTS", true);

			// Create a new notification
			Notification notif = new Notification(R.drawable.ic_dialog_alert,
					"Verification required", System.currentTimeMillis());

			// Create a PendingIntent out of the previously created intent to be
			// able to attach it to the notification.
			PendingIntent pendingIntent = PendingIntent.getActivity(
					DiscoverService.context, 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT
							| PendingIntent.FLAG_ONE_SHOT);

			// Attach the pending intent.
			notif.setLatestEventInfo(DiscoverService.context,
					"OpenUAT - Opening connection",
					"This code has to be verified by the other side",
					pendingIntent);
			notif.flags = Notification.FLAG_NO_CLEAR
					| Notification.FLAG_AUTO_CANCEL;

			// Publish the notification.
			if (ENABLE_QRVERIFICATION) {
				DiscoverService.mNotificationManager.notify(
						Constants.NOTIF_VERIFICATION_CHALLENGE, notif);
			}

			// Due to this is the local client - and verification will be done
			// by
			// the remote client - the code can be verified immediately.
			verificationSuccess(toRemote, c, id.toString());
			return;
		}

		// Due to this isn't the local client it has to verify the connection by
		// scanning the QR-code shown by the remote client.

		// If the Client does not exist locally it has to be created and added
		// to the according application.
		if (c == null) {
			c = new Client(id);
			id.getApp().addClient(c);
		}

		// Set the remoteConnection of the Client.
		c.setRemoteConnection(toRemote);
		c.setOobKey(sharedAuthenticationKey);

		// If the verification has been disabled, verify the connection
		// immediately.
		if (!ENABLE_QRVERIFICATION) {
			verificationSuccess(toRemote, c, id.toString());
			return;
		}

		// Create an intent to start the scan-activity.
		Intent intent = new Intent(DiscoverService.context,
				VerificationQR.class);

		// Create a PendingIntent out of the intent created before.
		PendingIntent pendingIntent = PendingIntent.getActivity(
				DiscoverService.context, 0, intent, PendingIntent.FLAG_ONE_SHOT
						| PendingIntent.FLAG_CANCEL_CURRENT);

		// Create a new notification.
		Notification notif = new Notification(R.drawable.ic_menu_help,
				"verfication required", System.currentTimeMillis());

		// Attach the pendingIntent.
		notif.setLatestEventInfo(DiscoverService.context,
				"OpenUAT - Incoming connection",
				"Please verify the connection", pendingIntent);
		notif.flags = Notification.FLAG_NO_CLEAR
				| Notification.FLAG_AUTO_CANCEL;

		// Publish the notification.
		DiscoverService.mNotificationManager.notify(
				Constants.NOTIF_VERIFICATION_RESPONSE, notif);

		// Start the verification thread and set the oob-key
		c.startVerification();

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
		if (optionalParameterFromRemote == null
				|| optionalParameterFromRemote.isEmpty()) {
			return;
		}

		// If the verification has been successful a new SecureChannel will be
		// created and sent to the clients.
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
			// Delete the secureChannel and notify the clients.
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
