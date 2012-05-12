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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.openuat.android.service.interfaces.IReceiverCallback;
import org.openuat.android.service.interfaces.ISecureChannel.Stub;
import org.openuat.channel.main.RemoteConnection;
import org.openuat.util.Hash;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

/**
 * The Class SecureChannel.
 * 
 * 
 * @author Hannes Markschlaeger
 */
public class SecureChannel extends Stub {

    public static enum VERIFICATION_STATUS {
	VERIFICATION_FAILED, VERIFICATION_PENDING, VERIFICATION_SUCCESS
    };

    private static final int UPDATE_INTERVALL = 250;
    private RemoteCallbackList<IReceiverCallback> callbacks = null;

    private BufferedInputStream inStream = null;
    private BufferedOutputStream outStream = null;

    private VERIFICATION_STATUS verificationStatus = VERIFICATION_STATUS.VERIFICATION_PENDING;
    private ArrayList<IVerificationStatusListener> verificationStatusListener = null;

    private byte[] sessionkey = null;
    private byte[] oobKey = null;

    private RemoteConnection remoteConnection = null;

    private volatile Thread receiveTrigger = null;
    private final Thread receiveThread = new Thread(new Runnable() {
	private byte[] data = null;
	private int n = 0;
	Thread thisThread = null;

	@Override
	public void run() {
	    thisThread = Thread.currentThread();
	    while (receiveTrigger == thisThread) {
		try {
		    data = receive();
		    if (data != null) {
			n = callbacks.beginBroadcast();
			for (int i = 0; i < n; i++) {
			    callbacks.getBroadcastItem(i).receive(data);
			}
			callbacks.finishBroadcast();
		    } else {
			Thread.sleep(UPDATE_INTERVALL);
		    }
		} catch (final RemoteException e) {
		    e.printStackTrace();
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}
    });

    private volatile Thread verificationTrigger = null;
    private final Thread verificationThread = new Thread(new Runnable() {
	Thread thisThread = null;

	@Override
	public void run() {
	    thisThread = Thread.currentThread();
	    while (verificationTrigger == thisThread) {
		if (DiscoverService.oob_key != null) {
		    VERIFICATION_STATUS result = DiscoverService.oob_key
			    .equalsIgnoreCase(Hash.getHexString(oobKey)) ? VERIFICATION_STATUS.VERIFICATION_SUCCESS
			    : VERIFICATION_STATUS.VERIFICATION_FAILED;

		    setVerificationStatus(result);
		    DiscoverService.oob_key = null;
		    verificationTrigger = null;
		}
		try {
		    Thread.sleep(UPDATE_INTERVALL);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }

	}
    });

    /**
     * Instantiates a new secure channel.
     * 
     * @param toRemote
     *            the remote tcp connection
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public SecureChannel(final RemoteConnection toRemote) throws IOException {
	this.remoteConnection = toRemote;
	callbacks = new RemoteCallbackList<IReceiverCallback>();
	verificationStatusListener = new ArrayList<IVerificationStatusListener>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.os.Binder#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
	if (inStream != null) {
	    inStream.close();
	}
	if (outStream != null) {
	    outStream.close();
	}
	verificationTrigger = null;
	receiveTrigger = null;
	super.finalize();
    }

    public VERIFICATION_STATUS getVerificationStatus() {
	return verificationStatus;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openuat.service.ISecureChannel#receive()
     */
    @Override
    public byte[] receive() throws RemoteException {
	int len = 0;
	byte[] data = null;
	byte[] prefix = new byte[10];

	try {
	    if (inStream == null) {
		inStream = new BufferedInputStream(
			remoteConnection.getInputStream());
	    }
	    inStream.read(prefix);
	    try {
		len = Integer.parseInt((new String(prefix)).trim());
	    } catch (NumberFormatException e) {
		return null;
	    }
	    Log.d(this.toString(), "receiving ");
	    if (len > 0) {
		data = new byte[len];
		inStream.read(data, 0, data.length);
	    }
	    prefix = new byte[10];
	    return data;
	} catch (final NumberFormatException e) {
	    e.printStackTrace();
	} catch (final IOException e) {
	    e.printStackTrace();
	}
	return data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openuat.service.ISecureChannel#registerReceiveHandler(org.openuat
     * .service.IReceiverCallback)
     */
    @Override
    public void registerReceiveHandler(final IReceiverCallback receiver)
	    throws RemoteException {
	callbacks.register(receiver);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openuat.service.ISecureChannel#send(byte[])
     */
    @Override
    public boolean send(final byte[] data) throws RemoteException {
	if (remoteConnection != null) {
	    try {
		if (outStream == null) {
		    outStream = new BufferedOutputStream(
			    remoteConnection.getOutputStream());
		}
		Log.d(this.toString(), "sending..");
		final byte[] prefix = java.util.Arrays.copyOfRange(String
			.valueOf(data.length).getBytes(), 0, 10);
		outStream.write(Util.concat(prefix, data));
	    } catch (final IOException e) {
		e.printStackTrace();
		return false;
	    }
	}
	return true;
    }

    /**
     * Sets the auth succeeded.
     * 
     * @param authSucceeded
     *            the new auth succeeded
     */
    public void setVerificationStatus(final VERIFICATION_STATUS veriStatus) {
	this.verificationStatus = veriStatus;
	if (verificationStatus == VERIFICATION_STATUS.VERIFICATION_SUCCESS) {
	    setReceivePolling(true);
	    setVerificationPolling(false);
	} else if (verificationStatus == VERIFICATION_STATUS.VERIFICATION_PENDING) {
	    setVerificationPolling(true);
	} else {
	    setReceivePolling(false);
	    setVerificationPolling(false);
	    remoteConnection = null;
	}

	for (IVerificationStatusListener listener : verificationStatusListener) {
	    listener.onVerificationStatusChanged(verificationStatus);
	}

    }

    private void setReceivePolling(Boolean status) {
	if (status) {
	    receiveTrigger = receiveThread;
	    receiveThread.start();
	} else {
	    receiveTrigger = null;
	}

    }

    private void setVerificationPolling(Boolean status) {
	if (status) {
	    verificationTrigger = verificationThread;
	    verificationThread.start();
	} else {
	    verificationTrigger = null;
	}

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openuat.service.ISecureChannel#unregisterReceiveHandler(org.openuat
     * .service.IReceiverCallback)
     */
    @Override
    public void unregisterReceiveHandler(final IReceiverCallback receiver)
	    throws RemoteException {
	callbacks.unregister(receiver);
    }

    /**
     * @param iAuthenticationStatusListener
     */
    public void setVerificationStatusListener(
	    IVerificationStatusListener listener) {
	verificationStatusListener.add(listener);
    }

    public void setSessionKey(byte[] sessionKey) {
	sessionkey = sessionKey;
    }

    /**
     * @param oobKey
     */
    public void setOobKey(byte[] oobKey) {
	this.oobKey = oobKey;
    }

}
