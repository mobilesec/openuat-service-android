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
import org.openuat.android.service.interfaces.IConnectionCallback;
import org.openuat.android.service.interfaces.IDeviceAuthenticator;
import org.openuat.android.service.interfaces.ISecureChannel;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * The Class DiscoverService.
 * 
 * 
 * @author Hannes Markschlaeger
 */
public class DiscoverService extends Service {

    public static Context context = null;
    public static NotificationManager mNotificationManager;
    public static String oob_key = null;

    public DiscoverService() {
	Log.i("DiscoverService", "ctor");

	try {
	    Thread.sleep(5000);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	TCPPortServerHandler tcpHandler = TCPPortServerHandler.getInstance();
    }

    /** The device authenticator. */
    private final IDeviceAuthenticator.Stub deviceAuthenticator = new IDeviceAuthenticator.Stub() {
	@Override
	public ISecureChannel authenticate(final String serviceId,
		final String device) throws RemoteException {
	    Log.i(this.toString(), "authenticate" + device);

	    // final String[] s = device.split("/");
	    //
	    // InetAddress ad = null;
	    // try {
	    // ad = InetAddress.getByName(s[s.length - 1]);
	    // Log.d(this.toString(), ad.toString());
	    // } catch (final UnknownHostException e1) {
	    // e1.printStackTrace();
	    // }
	    //
	    // if (ad.equals(Util.getipAddress())) {
	    // throw new RemoteException();
	    // }

	    OpenUAT_ID id = OpenUAT_ID.parseToken(device);

	    Client c = id.getApp().getClientById(id);
	    try {
		return c.openConnection();
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	    // SecureChannel chan = null;
	    // try {
	    // chan = RegisteredAppManager.getServiceByName(serviceId)
	    // .getClientById(adress).openConnection();
	    //
	    // } catch (final IOException e) {
	    // throw new RemoteException();
	    // }
	    return null;
	}

	@Override
	public String[] getAvailableDevices(final String serviceId)
		throws RemoteException {
	    Log.i(this.toString(), " getDevices");
	    final List<Client> list = RegisteredAppManager.getServiceByName(
		    serviceId).getClients();
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
	public String[] getPairedDevices() throws RemoteException {
	    return null;
	}

	@Override
	public String[] getSupportedAuthenticationMethods()
		throws RemoteException {
	    return null;
	}

	@Override
	public void register(final String serviceId,
		IConnectionCallback connectionCallback) throws RemoteException {

	    final RegisteredApp app = new RegisteredApp(serviceId,
		    IConnectionType.CONNECTION_TYPE.WIFI);
	    RegisteredAppManager.registerService(app);

	    // try {
	    // app.addClient(new Client(new RemoteTCPConnection(new Socket(
	    // Util.getipAddress(), Constants.TCP_PORT)),
	    // connectionCallback));
	    // } catch (IOException e) {
	    // e.printStackTrace();
	    // }
	    Log.i(this.toString(), app + " registered");
	}

	@Override
	public ISecureChannel selectAndAuthenticate(final String serviceId)
		throws RemoteException {
	    // TODO: show available devices
	    return null;
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
	context = getApplicationContext();
	mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	return deviceAuthenticator;
    }
}