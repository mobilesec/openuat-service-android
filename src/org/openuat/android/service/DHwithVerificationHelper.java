/* Copyright Hannes Markschläger
 * File created 07.05.2012
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package org.openuat.android.service;

import java.io.IOException;
import java.net.UnknownHostException;

import org.openuat.android.Constants;
import org.openuat.authentication.DHWithVerification;
import org.openuat.channel.main.HostAuthenticationServer;
import org.openuat.channel.main.RemoteConnection;
import org.openuat.channel.main.ip.RemoteTCPConnection;

/**
 * TODO: add class comment.
 * 
 * @author Hannes Markschlaeger
 */
public abstract class DHwithVerificationHelper extends DHWithVerification {

    int tcpPort;
    int numResetHookCalled = 0;
    int numSucceededHookCalled = 0;
    int numFailedHardHookCalled = 0;
    int numFailedSoftHookCalled = 0;
    int numProgressHookCalled = 0;
    int numStartedHookCalled = 0;
    byte[] sharedAuthKey = null;
    byte[] sharedSessKey = null;
    private boolean succeed;
    private boolean failHard;
    String param;

    Object optVerifyIdIn = null, optVerifyIdOut = null;
    String optParamIn = null, optParamOut = null;

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
    }

//    @Override
//    protected void startVerificationAsync(byte[] sharedAuthenticationKey,
//	    String parm, RemoteConnection remote) {
//	this.param = parm;
//	// need to copy here to retain until after success of failure - the
//	// original will be wiped
//	this.sharedAuthKey = new byte[sharedAuthenticationKey.length];
//	System.arraycopy(sharedAuthenticationKey, 0, this.sharedAuthKey, 0,
//		sharedAuthenticationKey.length);
//
//	if (succeed)
//	    this.verificationSuccess(remote, optVerifyIdIn, optParamIn);
//	else
//	    this.verificationFailure(failHard, remote, optVerifyIdIn,
//		    optParamIn, null, null);
//
//    }

    @Override
    protected void resetHook(RemoteConnection remote) {
    }

    @Override
    protected void protocolSucceededHook(RemoteConnection remote,
	    Object optionalVerificationId, String optionalParameterFromRemote,
	    byte[] sharedSessionKey) {
    }

    @Override
    protected void protocolFailedHook(boolean failedHard,
	    RemoteConnection remote, Object optionalVerificationId,
	    Exception e, String message) {
    }

    @Override
    protected void protocolProgressHook(RemoteConnection remote, int cur,
	    int max, String message) {
    }

    @Override
    protected void protocolStartedHook(RemoteConnection remote) {
    }

}
