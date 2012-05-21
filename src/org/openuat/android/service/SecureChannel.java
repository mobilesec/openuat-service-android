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

import org.openuat.android.Constants;
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

    private Client client;

    private RemoteCallbackList<IReceiverCallback> callbacks = null;
    private boolean isValid = false;

    private BufferedInputStream inStream = null;
    private BufferedOutputStream outStream = null;

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
			Thread.sleep(Constants.POLLING_INTERVALL);
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
		    boolean result = DiscoverService.oob_key
			    .equalsIgnoreCase(Hash.getHexString(oobKey));

		    if (result) {
			verificationTrigger = null;
			try {
			    DHwithVerificationHelper.getInstance()
				    .verificationSuccess(remoteConnection,
					    null, client.getId().toString());
			} catch (IOException e) {
			    e.printStackTrace();
			}
		    } else {
			try {
			    DHwithVerificationHelper
				    .getInstance()
				    .verificationFailure(true,
					    remoteConnection, null,
					    client.getId().toString(),
					    new Exception(), "invalid OOB code");
			} catch (IOException e) {
			    e.printStackTrace();
			}
		    }
		    DiscoverService.oob_key = null;
		    verificationTrigger = null;
		}
		try {
		    Thread.sleep(Constants.POLLING_INTERVALL);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }

	}
    });

    private volatile Thread keepAliveTrigger = null;
    private final Thread keepAliveThread = new Thread(new Runnable() {
	Thread thisThread = null;

	@Override
	public void run() {
	    thisThread = Thread.currentThread();
	    while (keepAliveTrigger == thisThread) {
		// TODO keepalive / check
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
    public SecureChannel(final RemoteConnection toRemote, Client client)
	    throws IOException {
	this.remoteConnection = toRemote;
	callbacks = new RemoteCallbackList<IReceiverCallback>();
	this.client = client;
	isValid = false;
    }

    @Override
    protected void finalize() {
	try {
	    close();
	    super.finalize();
	} catch (Throwable e) {
	    e.printStackTrace();
	}
    }

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
	    if (len > 0) {
		Log.i(this.toString(), "receiving bytes: " + len);
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

    @Override
    public void registerReceiveHandler(final IReceiverCallback receiver)
	    throws RemoteException {
	callbacks.register(receiver);
    }

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

    @Override
    public void unregisterReceiveHandler(final IReceiverCallback receiver)
	    throws RemoteException {
	callbacks.unregister(receiver);
    }

    public void setSessionKey(byte[] sessionKey) {
	sessionkey = sessionKey;
    }

    public void setOobKey(byte[] oobKey) {
	this.oobKey = oobKey;
    }

    public boolean isValid() {
	return isValid;
    }

    public void setValid(boolean status) {
	isValid = status;
	if (isValid) {
	    setVerificationPolling(false);
	    setReceivePolling(true);
	} else {
	    setVerificationPolling(false);
	    setReceivePolling(false);
	    oobKey = null;
	    sessionkey = null;
	}
    }

    public void startVerification(byte[] sharedAuthenticationKey) {
	oobKey = sharedAuthenticationKey;
	setVerificationPolling(true);
    }

    private void close() throws IOException {
	if (inStream != null) {
	    inStream.close();
	}
	if (outStream != null) {
	    outStream.close();
	}
	remoteConnection = null;
	verificationTrigger = null;
	receiveTrigger = null;
	keepAliveTrigger = null;
	isValid = false;
    }

}
