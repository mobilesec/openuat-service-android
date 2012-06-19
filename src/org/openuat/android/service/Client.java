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

import org.openuat.android.Constants;
import org.openuat.android.OpenUAT_ID;
import org.openuat.android.service.connectiontype.IConnectionType;
import org.openuat.android.service.interfaces.IConnectionCallback;
import org.openuat.android.service.interfaces.IDeviceAuthenticator;
import org.openuat.channel.main.RemoteConnection;
import org.openuat.util.Hash;

import android.os.RemoteException;
import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * This class represents a client. A client is identified by an
 * {@link OpenUAT_ID} and has the corresponding remote-connection and streams.
 * 
 * @author Hannes Markschläger
 * 
 */
public class Client {

	/** The remote-connection. */
	private RemoteConnection remote = null;

	/** The secure channel. */
	private SecureChannel secureChannel = null;

	/**
	 * Flag indicating that this instance is the local client. Prevents
	 * publishing this client by
	 * {@link IDeviceAuthenticator#getAvailableDevices(String)}
	 */
	private boolean isLocalClient = false;

	/**
	 * The id identifying this client.
	 */
	private OpenUAT_ID id;

	/**
	 * The key used to verify the connection using an out-of-bound channel.
	 */
	private byte[] oobKey = null;

	/**
	 * Gets the {@link OpenUAT_ID} identifying this {@link Client}.
	 * 
	 * @return The {@link OpenUAT_ID} of this {@link Client}
	 */
	public final OpenUAT_ID getId() {
		return id;
	}

	/**
	 * Creates a new instance of {@link Client}.
	 * 
	 * @param id
	 *            the {@link OpenUAT_ID} identifying this {@link Client}
	 */
	public Client(OpenUAT_ID id) {
		this.id = id;
	}

	/**
	 * Gets the {@link RemoteConnection} of this {@link Client}.
	 * 
	 * @return The {@link RemoteConnection}.
	 */
	public RemoteConnection getRemoteConnection() {
		return remote;
	}

	/**
	 * Tries to open a connection to a remote {@link Client}. If there is an
	 * existing valid connection that one will be used. Otherwise a new
	 * connection will be established and verified.
	 * 
	 * In case of an existing valid connection or a new one has been established
	 * successfully it can be received by
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 *             {@link IConnectionCallback#connectionIncoming(org.openuat.android.service.interfaces.ISecureChannel, String)}
	 * @see DHwithVerificationImpl#startAuthentication(RemoteConnection, int,
	 *      String)
	 */
	public void establishConnection() throws IOException {

		Log.d(this.toString(), "openConnection");
		if (secureChannel != null) {
			// TODO return existing channel
			Log.d(this.toString(), "valid channel present");
		}
		Log.d(this.toString(), "no channel found - creating new one");
		setRemoteConnection(IConnectionType.newConnection(id));

		// TODO
		DHwithVerificationImpl.getInstance().startAuthentication(remote,
				Constants.PROTOCOL_TIMEOUT, id.getApp().getLocalId().toToken());
		// String token = id.toString() + Constants.TOKEN_SEPARATOR
		// + id.getApp().getLocalId().toString();
		//
		// DHwithVerificationImpl.getInstance().startAuthentication(
		// getRemoteConnection(), Constants.PROTOCOL_TIMEOUT, token);
	}

	/**
	 * Sets the remote connection.
	 * 
	 * @param adress
	 *            the new remote connection
	 */
	public void setRemoteConnection(final RemoteConnection adress) {
		this.remote = adress;
	}

	/**
	 * Sets the secure channel and publishes it.
	 * 
	 * @param channel
	 *            the new secure channel
	 * @throws RemoteException
	 *             the remote exception
	 * @see RegisteredApp#publishChannel(Client)
	 */
	public void setSecureChannel(final SecureChannel channel)
			throws RemoteException {
		secureChannel = channel;
		id.getApp().publishChannel(this);
	}

	/**
	 * Gets the secure channel.
	 * 
	 * @return the secure channel
	 */
	public final SecureChannel getSecureChannel() {
		return secureChannel;
	}

	/**
	 * Flag indicating that this instance is the local client. Prevents
	 * publishing this client by
	 * {@link IDeviceAuthenticator#getAvailableDevices(String)}
	 * 
	 * @return true if this is a local {@link Client}, false otherwise.
	 */
	public boolean isLocalClient() {
		return isLocalClient;
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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((remote == null) ? 0 : remote.hashCode());
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
		if (!(obj instanceof Client))
			return false;
		Client other = (Client) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (remote == null) {
			if (other.remote != null)
				return false;
		} else if (!remote.equals(other.remote))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return id.toToken();
	}

	/**
	 * Sets the oob key.
	 * 
	 * @param sharedAuthenticationKey
	 *            the new oob key
	 */
	public void setOobKey(byte[] sharedAuthenticationKey) {
		oobKey = sharedAuthenticationKey;
	}

	/**
	 * Creates the local client.
	 * 
	 * @param localId
	 *            The {@link OpenUAT_ID} of the local client.
	 * @return An instance of {@link Client} which is marked as local client.
	 * @see #isLocalClient()
	 */
	public static Client createLocalClient(OpenUAT_ID localId) {
		Client client = new Client(localId);
		client.isLocalClient = true;
		return client;
	}

	/**
	 * This method compares the stored oob-key with the one passed as parameter
	 * (in general the key received via the OOB-channel). According to the
	 * result the current verification process will succeed or fail.
	 * 
	 * @param key
	 *            The key to be compared with the stored one.
	 * @see {@link #checkKeys(String)}
	 * @see DHwithVerificationImpl#verificationSuccess(RemoteConnection, Object,
	 *      String)
	 * @see DHwithVerificationImpl#verificationFailure(boolean,
	 *      RemoteConnection, Object, String, Exception, String)
	 */
	public void checkKeys(String key) {
		boolean result = key.equalsIgnoreCase(Hash.getHexString(oobKey));

		if (result) {
			try {
				DHwithVerificationImpl.getInstance().verificationSuccess(
						remote, this, id.getApp().getLocalId().toToken());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				DHwithVerificationImpl.getInstance().verificationFailure(true,
						remote, this, id.getApp().getLocalId().toToken(),
						new Exception(), "invalid OOB code");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
