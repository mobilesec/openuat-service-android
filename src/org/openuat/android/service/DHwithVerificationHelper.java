/* Copyright Hannes Markschläger
 * File created 07.05.2012
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package org.openuat.android.service;

import org.openuat.authentication.DHWithVerification;
import org.openuat.channel.main.HostAuthenticationServer;
import org.openuat.channel.main.RemoteConnection;

/**
 * TODO: add class comment.
 *
 * @author Hannes Markschlaeger
 */
public abstract class DHwithVerificationHelper extends DHWithVerification {

    /**
     * @param server
     * @param keepConnectedOnSuccess
     * @param keepConnectedOnFailure
     * @param concurrentVerificationSupported
     * @param instanceId
     * @param useJSSE
     */
    public DHwithVerificationHelper(HostAuthenticationServer server,
	    boolean keepConnectedOnSuccess, boolean keepConnectedOnFailure,
	    boolean concurrentVerificationSupported, String instanceId,
	    boolean useJSSE) {
	super(server, keepConnectedOnSuccess, keepConnectedOnFailure,
		concurrentVerificationSupported, instanceId, useJSSE);
	// TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.openuat.authentication.DHWithVerification#startVerificationAsync(byte[], java.lang.String, org.openuat.channel.main.RemoteConnection)
     */
    @Override
    protected void startVerificationAsync(byte[] sharedAuthenticationKey,
	    String optionalParam, RemoteConnection toRemote) {
	// TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openuat.authentication.DHWithVerification#resetHook(org.openuat.channel.main.RemoteConnection)
     */
    @Override
    protected void resetHook(RemoteConnection remote) {
	// TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openuat.authentication.DHWithVerification#protocolSucceededHook(org.openuat.channel.main.RemoteConnection, java.lang.Object, java.lang.String, byte[])
     */
    @Override
    protected void protocolSucceededHook(RemoteConnection remote,
	    Object optionalVerificationId, String optionalParameterFromRemote,
	    byte[] sharedSessionKey) {
	// TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openuat.authentication.DHWithVerification#protocolFailedHook(boolean, org.openuat.channel.main.RemoteConnection, java.lang.Object, java.lang.Exception, java.lang.String)
     */
    @Override
    protected void protocolFailedHook(boolean failHard,
	    RemoteConnection remote, Object optionalVerificationId,
	    Exception e, String message) {
	// TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openuat.authentication.DHWithVerification#protocolProgressHook(org.openuat.channel.main.RemoteConnection, int, int, java.lang.String)
     */
    @Override
    protected void protocolProgressHook(RemoteConnection remote, int cur,
	    int max, String message) {
	// TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.openuat.authentication.DHWithVerification#protocolStartedHook(org.openuat.channel.main.RemoteConnection)
     */
    @Override
    protected void protocolStartedHook(RemoteConnection remote) {
	// TODO Auto-generated method stub

    }

}
