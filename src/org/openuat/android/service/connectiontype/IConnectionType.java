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
	 * Adds a {@link RegisteredApp} to its fitting instance of
	 * {@link IConnectionType}.
	 * 
	 * @param app
	 *            the {@link RegisteredApp} to add.
	 */
	public static void addRegisteredApp(RegisteredApp app) {
		switch (app.getConnection()) {
		case WIFI:
			TCP.getInstance().addApp(app);
			break;
		}
	}

	/**
	 * Adds a {@link RegisteredApp} to this instance of {@link IConnectionType}
	 * 
	 * @param app
	 *            The {@link RegisteredApp} to add.
	 */
	protected abstract void addApp(RegisteredApp app);

	/**
	 * Removes a {@link RegisteredApp} from its fitting instance of
	 * {@link IConnectionType}
	 * 
	 * @param app
	 *            the {@link RegisteredApp} to add.
	 */
	public static void removeRegisteredApp(RegisteredApp app) {
		switch (app.getConnection()) {
		case WIFI:
			TCP.getInstance().removeApp(app);
			break;
		}
	}

	/**
	 * Removes a {@link RegisteredApp} from this instance of
	 * {@link IConnectionType}.
	 * 
	 * @param app
	 *            The {@link RegisteredApp} to remove.
	 */
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

	/**
	 * Tries to get the {@link OpenUAT_ID} according to the given
	 * {@link RemoteConnection}.
	 * 
	 * @param remote
	 *            The {@link RemoteConnection} where the {@link OpenUAT_ID} has
	 *            to be found for.
	 * @return The {@link OpenUAT_ID} mapped to the given
	 *         {@link RemoteConnection} or <code>null</code> if it could not be
	 *         found.
	 * @throws IOException
	 */
	public static OpenUAT_ID getIdByRemote(RemoteConnection remote)
			throws IOException {
		// WiFi?
		if (remote instanceof RemoteTCPConnection) {
			return TCP.getInstance().availableClients.inverse().get(
					remote.getRemoteAddress());
		}
		return null;
	}

	/**
	 * Tries to get the {@link Client} according to the given
	 * {@link RemoteConnection}.
	 * 
	 * @param remote
	 *            The {@link RemoteConnection} where the {@link Client} has to
	 *            be found for.
	 * @return The {@link Client} mapped to the given {@link RemoteConnection}
	 *         or <code>null</code> if nothing could be found.
	 * @throws IOException
	 * @see {@link IConnectionType#getIdByRemote(RemoteConnection)}
	 */
	public static Client getClientByRemote(RemoteConnection remote)
			throws IOException {
		OpenUAT_ID id = getIdByRemote(remote);
		if (id == null) {
			return null;
		}

		return id.getApp().getClientById(id);
	}

	public static void shutdown() {
		TCP.getInstance().close();
	}
	
	public static void start() {
		TCP.getInstance().open();
	}
	
	protected abstract void close();
	protected abstract void open();
	
}
