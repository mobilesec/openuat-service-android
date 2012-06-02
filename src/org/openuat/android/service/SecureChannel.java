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

import org.bouncycastle.crypto.InvalidCipherTextException;
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
	boolean callbackInUse = false;

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
					data = receive();
					synchronized (receiveCallbacks) {
						while (callbackInUse) {
							try {
								receiveCallbacks.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						callbackInUse = true;
						n = receiveCallbacks.beginBroadcast();
						if (n == 0) {
							setReceivePolling(false);
						}
						for (int i = 0; i < n; i++) {
							Log.d(this.toString(), "broadcasting.. "
									+ data.length + " bytes");
							receiveCallbacks.getBroadcastItem(i).receive(data);
						}
						receiveCallbacks.finishBroadcast();
						callbackInUse = false;
						receiveCallbacks.notify();
					}
				} catch (final RemoteException e) {
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
	public SecureChannel(final RemoteConnection toRemote) {
		this.remoteConnection = toRemote;
		receiveCallbacks = new RemoteCallbackList<IReceiverCallback>();
		data = new byte[Constants.CHUNK_SIZE];
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

	byte incHeader[] = new byte[Constants.HEADER_LENGTH];

	@Override
	public byte[] receive() throws RemoteException {

		int bytesReceived = 0;
		try {
			if (inStream == null) {
				inStream = new BufferedInputStream(
						remoteConnection.getInputStream());
			}

			while ((bytesReceived += inStream.read(incHeader, bytesReceived,
					Constants.HEADER_LENGTH - bytesReceived)) < Constants.HEADER_LENGTH) {
			}

			int paket_size = Integer.parseInt(new String(incHeader, 0,
					Constants.HEADER_LENGTH));
			Log.i(this.toString(), "paket size " + paket_size);

			bytesReceived = 0;
			byte[] incData = new byte[paket_size];
			while ((bytesReceived += inStream.read(incData, bytesReceived,
					paket_size - bytesReceived)) < paket_size) {
			}
			Log.i(this.toString(),
					"ciphertext: "
							+ new String(incData, 0, Math.min(incData.length,
									35)));
			byte[] plaintext = AESGCM.SimpleDecrypt(incData, sessionkey);
			Log.i(this.toString(), "plaintext: "
					+ new String(plaintext, 0, Math.min(plaintext.length, 35)));
			return plaintext;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (InvalidCipherTextException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public void registerReceiveHandler(final IReceiverCallback receiver)
			throws RemoteException {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (receiveCallbacks) {
					while (callbackInUse) {
						try {
							receiveCallbacks.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					callbackInUse = true;
					receiveCallbacks.register(receiver);
					callbackInUse = false;
					receiveCallbacks.notify();
					setReceivePolling(true);
				}
			}
		});
		t.run();
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
				byte[] ciphertext = AESGCM.SimpleEncrypt(data, sessionkey);
				outStream.write(Util.makeParcel(ciphertext));
				outStream.flush();
			} catch (final IOException e) {
				e.printStackTrace();
				return false;
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (InvalidCipherTextException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
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
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (receiveCallbacks) {
					while (callbackInUse) {
						try {
							receiveCallbacks.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					callbackInUse = true;
					receiveCallbacks.unregister(receiver);
					callbackInUse = false;
					receiveCallbacks.notify();
				}
			}
		});
		t.run();
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
	}

}
