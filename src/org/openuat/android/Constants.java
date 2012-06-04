/* Copyright Hannes Markschläger
 * File created 22 Apr 2012
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package org.openuat.android;

public final class Constants {
	public static final int NOTIF_VERIFICATION_CHALLENGE = 100;
	public static final int NOTIF_VERIFICATION_RESPONSE = 101;

	/** The Constant DISCOVER_CHALLENGE. */
	public static final String DISCOVER_CHALLENGE = "DISCOVER_CHALLENGE";

	/** The Constant DISCOVER_RESPOND. */
	public static final String DISCOVER_RESPOND = "DISCOVER_RESPOND";

	/**
	 * Separator-symbol used by the IP-Discoveryservice.
	 */
	public static final String SEPERATOR = "*";

	/**
	 * TCP-PORT
	 */
	public static final int TCP_PORT = 6968;

	/**
	 * UDP_PORT.
	 */
	public static final int UDP_PORT = 6969;

	/**
	 * Size of one KB
	 */
	public static final int KB_SIZE = (Byte.MAX_VALUE + 1) * 8;

	/**
	 * Size of one chunk in kb
	 */
	public static final int CHUNK_SIZE = KB_SIZE * 64;

	/**
	 * The length of the header
	 */
	public static final int HEADER_LENGTH = String.valueOf(CHUNK_SIZE)
			.getBytes().length;
	
	/**
	 * Format-string to add leading zeros to the header
	 */
	public static final String HEADER_PATTERN = "%0" + HEADER_LENGTH + "d";

	/**
	 * Length of the data field.
	 */
	public static final int DATA_LENGTH = CHUNK_SIZE - HEADER_LENGTH;

	public static final boolean USE_JSSE = true;
	public static final int PROTOCOL_TIMEOUT = -1;
	public static final boolean KEEP_CONNECTED = true;
	
}
