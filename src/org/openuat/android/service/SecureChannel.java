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
import java.util.Arrays;

import org.openuat.android.Constants;
import org.openuat.android.service.interfaces.IReceiverCallback;
import org.openuat.android.service.interfaces.ISecureChannel.Stub;
import org.openuat.channel.main.RemoteConnection;

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

    private RemoteCallbackList<IReceiverCallback> receiveCallbacks = null;

    private BufferedInputStream inStream = null;
    private BufferedOutputStream outStream = null;

    // storage for receive
    private byte[] data = null;

    private byte[] sessionkey = null;

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
		    n = receiveCallbacks.beginBroadcast();
		    for (int i = 0; i < n; i++) {
			data = receive();
			receiveCallbacks.getBroadcastItem(i).receive(data);
		    }
		    receiveCallbacks.finishBroadcast();
		} catch (final RemoteException e) {
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
    public SecureChannel(final RemoteConnection toRemote) {
	this.remoteConnection = toRemote;
	receiveCallbacks = new RemoteCallbackList<IReceiverCallback>();
	data = new byte[Constants.CHUNK_SIZE];
	setReceivePolling(true);
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
	// int len = 0;
	// byte[] data = null;
	// byte[] prefix = new byte[10];
	//
	// try {
	// if (inStream == null) {
	// inStream = new BufferedInputStream(
	// remoteConnection.getInputStream());
	// }
	// inStream.read(prefix);
	// try {
	// len = Integer.parseInt((new String(prefix)).trim());
	// } catch (NumberFormatException e) {
	// return null;
	// }
	// if (len > 0) {
	// Log.i(this.toString(), "receiving bytes: " + len);
	// data = new byte[len];
	// inStream.read(data, 0, data.length);
	// }
	// prefix = new byte[10];
	// return data;
	// } catch (final NumberFormatException e) {
	// e.printStackTrace();
	// } catch (final IOException e) {
	// e.printStackTrace();
	// }
	// return data;
	try {
	    int bytesRead = remoteConnection.getInputStream().read(data);
	    if (bytesRead != -1) {
		return Arrays.copyOf(data, bytesRead);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return null;

    }

    @Override
    public void registerReceiveHandler(final IReceiverCallback receiver)
	    throws RemoteException {
	receiveCallbacks.register(receiver);
    }

    @Override
    public boolean send(final byte[] data) throws RemoteException {
	if (remoteConnection != null) {
	    try {
		if (outStream == null) {
		    outStream = new BufferedOutputStream(
			    remoteConnection.getOutputStream(),
			    Constants.CHUNK_SIZE);
		}
		Log.d(this.toString(), "sending..");
		// final byte[] prefix = java.util.Arrays.copyOfRange(String
		// .valueOf(data.length).getBytes(), 0, 10);
		// outStream.write(Util.concat(prefix, data));
		outStream.write(data);
		outStream.flush();
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

    @Override
    public void unregisterReceiveHandler(final IReceiverCallback receiver)
	    throws RemoteException {
	receiveCallbacks.unregister(receiver);
    }

    public void setSessionKey(byte[] sessionKey) {
	sessionkey = sessionKey;
    }

    private void close() throws IOException {
	if (inStream != null) {
	    inStream.close();
	}
	if (outStream != null) {
	    outStream.close();
	}
	remoteConnection = null;
	receiveTrigger = null;
	keepAliveTrigger = null;
    }

}
