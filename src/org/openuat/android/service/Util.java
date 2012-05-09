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

import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

/**
 * The Class Util.
 * 
 * 
 * @author Hannes Markschlaeger
 */
public class Util {

    /**
     * Concat.
     * 
     * @param prefix
     *            the prefix
     * @param data
     *            the data
     * @return the byte[]
     */
    public static byte[] concat(final byte[] prefix, final byte[] data) {
	if (prefix == null) {
	    if (data == null) {
		return null;
	    }
	    return data;
	}
	if (data == null) {
	    return prefix;
	}
	final byte[] result = Arrays
		.copyOf(prefix, prefix.length + data.length);
	System.arraycopy(data, 0, result, prefix.length, data.length);
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
}
