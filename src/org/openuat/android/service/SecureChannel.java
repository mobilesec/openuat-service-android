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

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.openuat.android.Constants;
import org.openuat.android.service.interfaces.IReceiverCallback;
import org.openuat.android.service.interfaces.ISecureChannel.Stub;
import org.openuat.channel.main.RemoteConnection;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

/**
 * The {@link SecureChannel} class represents a connection to a remote
 * {@link SecureChannel} - usually owned by a {@link Client}.
 * 
 * @author Hannes Markschläger
 * 
 */
public class SecureChannel extends Stub {

	/**
	 * The receiver-callbacks.
	 */
	private RemoteCallbackList<IReceiverCallback> receiveCallbacks = null;
	/**
	 * Flag used to synchronize access to receiveCallback.
	 */
	boolean callbackInUse = false;

	private BufferedInputStream inStream = null;
	private BufferedOutputStream outStream = null;

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
					// Wait until data is received.
					data = receive();
					synchronized (receiveCallbacks) {
						// Wait until receiveCallbacks is available
						while (callbackInUse) {
							try {
								receiveCallbacks.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						// obtain lock on callbacks.
						callbackInUse = true;
						// Get number of registered callbacks.
						n = receiveCallbacks.beginBroadcast();
						// Terminate thread if there are no callbacks.
						if (n == 0) {
							setReceivePolling(false);
						}
						for (int i = 0; i < n; i++) {
							Log.d(this.toString(), "broadcasting.. "
									+ data.length + " bytes");
							// Broadcast the received data to the callbacks.
							receiveCallbacks.getBroadcastItem(i).receive(data);
						}
						receiveCallbacks.finishBroadcast();
						// Release lock on callbacks.
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

	// buffer for the header
	byte incHeader[] = new byte[Constants.HEADER_LENGTH];

	@Override
	public byte[] receive() throws RemoteException {

		int bytesReceived = 0;
		try {
			if (inStream == null) {
				inStream = new BufferedInputStream(
						remoteConnection.getInputStream());
			}

			// receive all bytes of the header
			while ((bytesReceived += inStream.read(incHeader, bytesReceived,
					Constants.HEADER_LENGTH - bytesReceived)) < Constants.HEADER_LENGTH) {
			}

			// parse the received header --> size of the incoming paket.
			int paket_size = Integer.parseInt(new String(incHeader, 0,
					Constants.HEADER_LENGTH));
			Log.i(this.toString(), "paket size " + paket_size);

			bytesReceived = 0;
			// array where the incoming data will be stored.
			byte[] incData = new byte[paket_size];
			while ((bytesReceived += inStream.read(incData, bytesReceived,
					paket_size - bytesReceived)) < paket_size) {
			}
			Log.i(this.toString(),
					"ciphertext: "
							+ new String(incData, 0, Math.min(incData.length,
									35)));
			// decrypt and return the received data
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
		// Done in a thread to avoid blocking while waiting for callbacks.
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (receiveCallbacks) {
					// wait until callbacks are available
					while (callbackInUse) {
						try {
							receiveCallbacks.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					// obtain lock, register callback, release lock.
					callbackInUse = true;
					receiveCallbacks.register(receiver);
					callbackInUse = false;
					receiveCallbacks.notify();
					// start receiver-thread
					setReceivePolling(true);
				}
			}
		});
		t.run();
	}

	@Override
	public boolean send(final byte[] data) throws RemoteException {
		boolean result = false;
		if (remoteConnection != null) {
			try {
				if (outStream == null) {
					outStream = new BufferedOutputStream(
							remoteConnection.getOutputStream(),
							Constants.CHUNK_SIZE);
				}
				Log.d(this.toString(), "sending..");
				// encrypt and send the data.
				byte[] ciphertext = AESGCM.SimpleEncrypt(data, sessionkey);
				outStream.write(Util.makePacket(ciphertext));
				outStream.flush();
				result = true;
			} catch (final IOException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (InvalidCipherTextException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * Starts or stops the receiver-thread.
	 * 
	 * @param status
	 *            True to turn the thread on, false otherwise.
	 */
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
		// Done in a thread to avoid blocking while waiting for callbacks.
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (receiveCallbacks) {
					// wait until callbacks are available
					while (callbackInUse) {
						try {
							receiveCallbacks.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					// obtain lock, unregister callback, release lock
					callbackInUse = true;
					receiveCallbacks.unregister(receiver);
					callbackInUse = false;
					receiveCallbacks.notify();
				}
			}
		});
		t.run();
	}

	/**
	 * Sets the key to be used for cryptographic operations by this channel
	 * 
	 * @param sessionKey
	 *            The key to be used.
	 */
	public void setSessionKey(byte[] sessionKey) {
		sessionkey = sessionKey;
	}

	/**
	 * Closes all opened streams, terminates the receiver-thread.
	 * 
	 * @throws IOException
	 */
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
