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

    /** The Constant END_TAG. */
    public static final String END_TAG = "<END>";

    /** The Constant SEPERATOR. */
    public static final String SEPERATOR = "*";

    /** The Constant TCP_PORT. */
    public static final int TCP_PORT = 6968;

    /** The Constant UDP_PORT. */
    public static final int UDP_PORT = 6969;

    public static final int KB_SIZE = (Byte.MAX_VALUE + 1) * 8;
    
    // = 4 kb
    public static final int CHUNK_SIZE = KB_SIZE * 4;

    public static final boolean USE_JSSE = true;
    public static final int PROTOCOL_TIMEOUT = -1;
    public static final boolean KEEP_CONNECTED = true;
    public static final int POLLING_INTERVALL = 250;
}
