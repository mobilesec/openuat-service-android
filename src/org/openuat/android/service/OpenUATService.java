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
import java.util.ArrayList;
import java.util.List;

import org.openuat.android.OpenUAT_ID;
import org.openuat.android.service.connectiontype.IConnectionType;
import org.openuat.android.service.connectiontype.TCP;
import org.openuat.android.service.interfaces.IConnectionCallback;
import org.openuat.android.service.interfaces.IDeviceAuthenticator;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * The Class OpenUATService.
 * 
 * 
 * @author Hannes Markschlaeger
 */
public class OpenUATService extends Service {
	public static Context context = null;
	public static NotificationManager mNotificationManager;
	public static String oob_key = null;

	public OpenUATService() {
		Log.i("OpenUATService", "ctor");
	}

	/** The device authenticator. */
	private final IDeviceAuthenticator.Stub deviceAuthenticator = new IDeviceAuthenticator.Stub() {
		@Override
		public void authenticate(final String serviceId, final String device)
				throws RemoteException {
			Log.i(this.toString(), "authenticate" + device);

			final OpenUAT_ID id = OpenUAT_ID.deserialize(device);

			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					Client c = id.getApp().getClientById(id);
					try {
						c.establishConnection();
					} catch (RemoteException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			t.start();
		}

		@Override
		public String[] getAvailableDevices(final String serviceId)
				throws RemoteException {
			Log.i(this.toString(), " getDevices");
			final List<Client> list = RegisteredAppManager
					.getServiceByNameAndConnType(serviceId,
							IConnectionType.CONNECTION_TYPE.WIFI).getClients();
			final List<String> result = new ArrayList<String>(list.size());

			for (final Client c : list) {
				if (!c.isLocalClient()) {
					result.add(c.toString());
				}
				Log.i(this.toString(), c.toString());
			}
			return result.toArray(new String[result.size()]);
		}

		@Override
		public void register(final String serviceId,
				IConnectionCallback connectionCallback) throws RemoteException {

			if (serviceId == null || serviceId.length() == 0) {
				Log.e(this.toString(), "invalid serviceId");
				throw new RemoteException();
			}

			RegisteredApp app = RegisteredAppManager
					.getServiceByNameAndConnType(serviceId,
							IConnectionType.CONNECTION_TYPE.WIFI);
			if (app == null) {
				app = new RegisteredApp(serviceId,
						IConnectionType.CONNECTION_TYPE.WIFI);
				app.setConnectionCallback(connectionCallback);
				RegisteredAppManager.registerService(app);
			}

			Log.i(this.toString(), app + " registered");
		}

	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public final IBinder onBind(final Intent arg0) {
		Log.i(this.toString(), "binded");
		return deviceAuthenticator;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		IConnectionType.start();
		try {
			DHwithVerificationImpl.getInstance();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		IConnectionType.shutdown();
	}
}