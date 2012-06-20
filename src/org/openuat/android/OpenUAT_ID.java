/* Copyright Hannes Markschläger
 * File created 12.05.2012
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package org.openuat.android;

import org.openuat.android.service.RegisteredApp;
import org.openuat.android.service.RegisteredAppManager;
import org.openuat.android.service.connectiontype.IConnectionType;
import org.openuat.authentication.exceptions.InternalApplicationException;
import org.openuat.util.Hash;

/**
 * This class represents the OpenUAT-ID which is a unique identifier for all
 * clients connected to the service. It consists of the Android-ID (unique to an
 * android-device), the application bound to the service and the type of the
 * connection used to communicate with other applications bound to the service.
 * 
 * @author Hannes Markschläger
 */
public class OpenUAT_ID {

	private RegisteredApp app = null;
	private String androidId = null;
	private String hash = null;

	/**
	 * Instantiates a new OpenUAT-ID
	 * 
	 * @param connection_type
	 *            The type of the connection to be used.
	 * @param app
	 *            The application bound to the service.
	 * @param androidId
	 *            The android-id of the device.
	 */
	public OpenUAT_ID(RegisteredApp app, String androidId) {
		this.app = app;
		this.androidId = androidId;
		hash = getHash();
	}

	public String getHash() {
		if (hash == null) {
			try {
				hash = Hash.getHexString(Hash.SHA256(serialize().getBytes(),
						true));
			} catch (InternalApplicationException e) {
				e.printStackTrace();
			}
		}
		return hash;
	}

	/**
	 * Parses a serialized {@link String} generated by
	 * {@link OpenUAT_ID#serialize()} into an {@link OpenUAT_ID}-object.
	 * 
	 * @param serializedObject
	 *            The serialized object to be parsed
	 * @return The object according to the serialized string or null if parsing
	 *         has been unsuccessful.
	 */
	public static OpenUAT_ID deserialize(String serializedObject) {

		String[] content = serializedObject.split("_");

		if (content.length != 3) {
			return null;
		}

		int index = 0;
		String androidId = content[index++];
		RegisteredApp app = RegisteredAppManager.getServiceByNameAndConnType(
				content[index++],
				IConnectionType.CONNECTION_TYPE.valueOf(content[index++]));

		if (androidId == "" || app == null) {
			return null;
		}
		return new OpenUAT_ID(app, androidId);
	}

	@Override
	public String toString() {
		return androidId + ", " + app.toString();
	}

	/**
	 * Serializes the object to a string.
	 * 
	 * @return A string representing the object.
	 */
	public String serialize() {
		return androidId + "_" + app.getName() + "_"
				+ app.getConnection().toString();
	}

	/**
	 * Gets the {@link RegisteredApp} associated with this {@link OpenUAT_ID}
	 * 
	 * @return The {@link RegisteredApp} of this object.
	 */
	public final RegisteredApp getApp() {
		return app;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((androidId == null) ? 0 : androidId.hashCode());
		result = prime * result + ((app == null) ? 0 : app.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof OpenUAT_ID))
			return false;
		OpenUAT_ID other = (OpenUAT_ID) obj;
		if (androidId == null) {
			if (other.androidId != null)
				return false;
		} else if (!androidId.equals(other.androidId))
			return false;
		if (app == null) {
			if (other.app != null)
				return false;
		} else if (!app.equals(other.app))
			return false;
		return true;
	}

}
