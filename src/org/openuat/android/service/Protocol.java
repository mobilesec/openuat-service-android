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

/**
 * The Class Protocol.
 * 
 * 
 * @author Hannes Markschlaeger
 */
public class Protocol {

    /** The Constant BEGIN_TAG. */
    public static final String BEGIN_TAG = "<BEGIN>";

    /** The Constant CONNECTION_ATTEMPT. */
    public static final String CONNECTION_ATTEMPT = "CONNECTION_ATTEMPT";

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
}
