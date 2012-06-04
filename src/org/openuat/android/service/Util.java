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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.openuat.android.Constants;

import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

/**
 * This class provides some useful methods for this service.
 * 
 * @author Hannes Markschlaeger
 */
public class Util {

	/**
	 * Concatenates two arrays
	 * 
	 * @param first
	 *            The first array
	 * @param second
	 *            The second array
	 * @return The concatenated array
	 */
	public static byte[] concatenate(final byte[] first, final byte[] second) {
		if (first == null) {
			if (second == null) {
				return null;
			}
			return second;
		}
		if (second == null) {
			return first;
		}
		final byte[] result = Arrays
				.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	/**
	 * Gets the local IPv4-Address. Use this instead of {@link
	 * InetAddress.getLocalHost()}, because that will return 127.0.0.1
	 * 
	 * @return the local IPv4 address.
	 */
	public static Inet4Address getipAddress() {

		Inet4Address localhost = null;
		try {
			localhost = (Inet4Address) InetAddress.getByName(Formatter
					.formatIpAddress(((WifiManager) DiscoverService.context
							.getSystemService("wifi")).getConnectionInfo()
							.getIpAddress()));
			Log.i("localhost: ", localhost.toString());
		} catch (UnknownHostException e) {
			Log.i("ip by android:", "fail");
			e.printStackTrace();
		}
		return localhost;
	}

	/**
	 * Creates a packet from a byte array by adding the according header.
	 * 
	 * @param data
	 *            The byte array for which the packet will be created.
	 * @return the packet
	 */
	public static byte[] makePacket(byte[] data) {
		return concatenate(String.format(Constants.HEADER_PATTERN, data.length)
				.getBytes(), data);

	}
}
