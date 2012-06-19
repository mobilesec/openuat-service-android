/**
 * Copyright Hannes Markschlaeger
 * File created 13.03.2012
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version. 
 */
package org.openuat.android.service.connectiontype;

import java.io.IOException;
import java.net.Socket;

import org.openuat.android.Constants;
import org.openuat.android.OpenUAT_ID;
import org.openuat.android.service.Client;
import org.openuat.android.service.RegisteredApp;
import org.openuat.channel.main.RemoteConnection;
import org.openuat.channel.main.ip.RemoteTCPConnection;

/**
 * The Interface IConnectionType.
 * 
 * 
 * @author Hannes Markschlaeger
 */
public abstract class IConnectionType {

	/**
	 * The Enum CONNECTION_TYPE.
	 * 
	 * 
	 * @author Hannes Markschlaeger
	 */
	public static enum CONNECTION_TYPE {

		/** The BLUETOOTH. */
		BLUETOOTH,
		/** The WIFI. */
		WIFI
	};

	/**
	 * Adds the app.
	 * 
	 * @param app
	 *            the app
	 */
	public static void addRegisteredApp(RegisteredApp app) {
		switch (app.getConnection()) {
		case WIFI:
			TCP.getInstance().addApp(app);
			break;
		}
	}

	protected abstract void addApp(RegisteredApp app);

	/**
	 * Removes the app.
	 * 
	 * @param app
	 *            the app
	 */
	public static void removeRegisteredApp(RegisteredApp app) {
		switch (app.getConnection()) {
		case WIFI:
			TCP.getInstance().removeApp(app);
			break;
		}
	}

	protected abstract void removeApp(RegisteredApp app);

	public static RemoteConnection newConnection(OpenUAT_ID id)
			throws IOException {
		switch (id.getApp().getConnection()) {
		case WIFI:
			return new RemoteTCPConnection(new Socket(
					TCP.getInstance().availableClients.get(id),
					Constants.TCP_PORT));
		}
		return null;
	}

	public static OpenUAT_ID getIdByRemote(RemoteConnection remote)
			throws IOException {
		if (remote instanceof RemoteTCPConnection) {
			return TCP.getInstance().availableClients.inverse().get(remote);
		}
		return null;
	}

	public static Client getClientByRemote(RemoteConnection remote)
			throws IOException {
		OpenUAT_ID id = getIdByRemote(remote);
		if (id == null) {
			return null;
		}

		return id.getApp().getClientById(id);
	}
}
