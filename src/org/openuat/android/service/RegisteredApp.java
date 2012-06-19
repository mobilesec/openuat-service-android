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

import org.openuat.android.OpenUAT_ID;
import org.openuat.android.service.connectiontype.IConnectionType;
import org.openuat.android.service.interfaces.IConnectionCallback;
import org.openuat.android.service.interfaces.ISecureChannel;
import org.openuat.channel.main.RemoteConnection;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.util.Log;

/**
 * This class represents an application bond to the service.
 * 
 * @author Hannes Markschlaeger
 */
public class RegisteredApp {

	private Client localClient = null;
	private RemoteCallbackList<IConnectionCallback> connectionCallbacks = null;

	/** The is discovering. */
	private boolean isDiscovering = true;

	/**
	 * The clients registered for this application.
	 */
	private ArrayList<Client> mClients = null;

	/**
	 * The type of connection used by this application.
	 */
	private final IConnectionType.CONNECTION_TYPE mConnection;

	/**
	 * The name and unique identifier of this application.
	 */
	private String mName = null;

	/**
	 * Instantiates a new registered app.
	 * 
	 * @param service
	 *            The unique identifier of this application
	 * @param connection
	 *            The type of connection used by this application.
	 */
	public RegisteredApp(final String service,
			final IConnectionType.CONNECTION_TYPE connection) {
		mName = service;
		mClients = new ArrayList<Client>();
		mConnection = connection;
		connectionCallbacks = new RemoteCallbackList<IConnectionCallback>();

		localClient = Client.createLocalClient(new OpenUAT_ID(this, Secure
				.getString(OpenUATService.context.getContentResolver(),
						Secure.ANDROID_ID)));
		addClient(localClient);
	}

	/**
	 * Adds a client to this application. Does not create duplicates.
	 * 
	 * @param client
	 *            the client to add.
	 */
	public void addClient(final Client client) {
		if (!mClients.contains(client)) {
			mClients.add(client);
		}
	}

	/**
	 * Gets the list of registered clients.
	 * 
	 * @return A list containing all registered clients.
	 */
	public ArrayList<Client> getClients() {
		return mClients;
	}

	/**
	 * Gets the connection-type used by this application.
	 * 
	 * @return the type of connection used by this application.
	 */
	public IConnectionType.CONNECTION_TYPE getConnection() {
		return mConnection;
	}

	/**
	 * Gets the name/identifier of this application.
	 * 
	 * @return the name/identifier
	 */
	public String getName() {
		return mName;
	}

	/**
	 * Gets the number of clients.
	 * 
	 * @return the number of clients
	 */
	public int getNumberOfClients() {
		return mClients.size();
	}

	/**
	 * Checks if is discovering.
	 * 
	 * @return true, if is discovering
	 */
	public boolean isDiscovering() {
		return isDiscovering;
	}

	/**
	 * Gets the client with the given id.
	 * 
	 * @param id
	 *            The id of the client to be looked for.
	 * @return The client if it could be found, null otherwise.
	 */
	public Client getClientById(final OpenUAT_ID id) {
		for (final Client c : mClients) {
			if (c.getId().equals(id)) {
				return c;
			}
		}
		Log.d(this.toString(), "no client found");
		return null;
	}

	/**
	 * Sets the discovering.
	 * 
	 * @param isDiscovering
	 *            the new discovering
	 */
	public void setDiscovering(final boolean isDiscovering) {
		if (isDiscovering) {
			mClients.clear();
		}
		this.isDiscovering = isDiscovering;
	}

	@Override
	public String toString() {
		return mName + ", " + mConnection.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((mConnection == null) ? 0 : mConnection.hashCode());
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RegisteredApp))
			return false;
		RegisteredApp other = (RegisteredApp) obj;
		if (mConnection != other.mConnection)
			return false;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		return true;
	}

	public String toToken() {
		// TODO move separator to constants
		return mName + "_" + mConnection.toString();
	}

	/**
	 * Gets the local client.
	 * 
	 * @return the local client.
	 */
	public Client getLocalClient() {
		return localClient;
	}

	/**
	 * Registers an {@link IConnectionCallback}
	 * 
	 * @param connectionCallback
	 *            the callback to register.
	 */
	public void setConnectionCallback(IConnectionCallback connectionCallback) {
		connectionCallbacks.register(connectionCallback);
	}

	/**
	 * Publishes an instance of {@link ISecureChannel} to the according clients.
	 * TODO: check behavior if more than 2 clients are registered.
	 * 
	 * @param client
	 */
	public synchronized void publishChannel(Client client)
			throws RemoteException {
		Log.i(this.toString(), "publishing channel: " + client.toString());

		try {
			int n = connectionCallbacks.beginBroadcast();
			for (int i = 0; i < n; i++) {
				Log.i(this.toString(), "TROLOLO");
				connectionCallbacks.getBroadcastItem(i).connectionIncoming(
						client.getSecureChannel(), client.getId().toToken());
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(this.toString(), e.getMessage());
		} finally {
			connectionCallbacks.finishBroadcast();
		}

	}

	/**
	 * Gets the id of the local client.
	 * 
	 * @return The id of the local client.
	 */
	public OpenUAT_ID getLocalId() {
		OpenUAT_ID id = null;
		if (localClient != null) {
			id = localClient.getId();
		}
		return id;
	}

	public Client getClientByRemote(RemoteConnection toRemote)
			throws IOException {
		OpenUAT_ID id = IConnectionType.getIdByRemote(toRemote);
		return id.getApp().getClientById(id);
	}
}
