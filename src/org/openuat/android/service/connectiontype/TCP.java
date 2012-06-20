/**
 * Copyright Hannes Markschlaeger
 * File created 13.03.2012
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version. 
 */
package org.openuat.android.service.connectiontype;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.openuat.android.Constants;
import org.openuat.android.OpenUAT_ID;
import org.openuat.android.service.Client;
import org.openuat.android.service.RegisteredApp;
import org.openuat.android.service.Util;
import org.openuat.channel.main.MessageListener;
import org.openuat.channel.main.ip.UDPMulticastSocket;

import android.util.Log;

import com.google.common.collect.HashBiMap;

/**
 * The Class TCP.
 * 
 * 
 * @author Hannes Markschlaeger
 */
public final class TCP extends IConnectionType implements MessageListener {
	protected HashBiMap<OpenUAT_ID, InetAddress> availableClients = null;
	/** The m discover runnable. */
	private static Runnable mDiscoverRunnable = new Runnable() {

		@Override
		public void run() {
			final Thread thisThread = Thread.currentThread();
			Log.i(this.toString(), "thread started");

			while (thisThread == TCP.mDiscoverThread) {
				try {
					for (final RegisteredApp service : TCP.mServices) {
						if (service.isDiscovering()) {
							TCP.mUdpMultiSock
									.sendMulticast((service.getName()
											+ Constants.SEPERATOR + Constants.DISCOVER_CHALLENGE)
											.getBytes());
						}
					}
					Thread.sleep(1000);
				} catch (final IOException e) {
					e.printStackTrace();
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	/** The m discover thread. */
	private static Thread mDiscoverThread = null;

	/** The m instance. */
	private static TCP mInstance = null;

	/** The m services. */
	private static List<RegisteredApp> mServices = null;

	/** The m udp multi sock. */
	private static UDPMulticastSocket mUdpMultiSock = null;

	/**
	 * Gets the single instance of TCP.
	 * 
	 * @return single instance of TCP
	 */
	public static TCP getInstance() {
		if (TCP.mInstance == null) {
			TCP.mInstance = new TCP();
		}
		return TCP.mInstance;
	}

	/** The m local ip. */
	private InetAddress mLocalIp = null;

	/**
	 * Instantiates a new tCP.
	 */
	private TCP() {
		try {
			availableClients = HashBiMap.create();
			TCP.mUdpMultiSock = new UDPMulticastSocket(Constants.UDP_PORT,
					Constants.UDP_PORT, "255.255.255.255");
			TCP.mUdpMultiSock.addIncomingMessageListener(this);
			TCP.mUdpMultiSock.startListening();
			TCP.mServices = new ArrayList<RegisteredApp>();
			mLocalIp = Util.getipAddress();
			TCP.mDiscoverThread = new Thread(TCP.mDiscoverRunnable);
			TCP.mDiscoverThread.start();
			Log.i(this.toString(), "start discovering");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void addApp(final RegisteredApp app) {
		if (!TCP.mServices.contains(app)) {
			TCP.mServices.add(app);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openuat.channel.main.MessageListener#handleMessage(byte[], int,
	 * int, java.lang.Object)
	 */
	@Override
	public void handleMessage(final byte[] message, final int offset,
			final int length, final Object sender) {
		synchronized (this) {
			Inet4Address sentFrom = (Inet4Address) ((sender instanceof Inet4Address) ? sender
					: null);

			if (sentFrom.getHostAddress().equals(mLocalIp.getHostAddress())) {
				return;
			}

			if ((message == null) || (length == 0)) {
				Log.i("UDP received", "Message is null!");
				return;
			}

			final String[] received = new String(message, offset, length)
					.split("\\" + Constants.SEPERATOR);

			if (received.length < 2) {
				Log.i("UDP received", "Message not correct!");
				return;
			}

			final String payload = received[0];
			final String command = received[1];

			if (payload == null || payload.length() == 0 || command == null
					|| command.length() == 0) {
				Log.i("UDP received", "Message not correct!");
				return;
			}

			if (command.equalsIgnoreCase(Constants.DISCOVER_CHALLENGE)) {
				try {
					for (RegisteredApp app : TCP.mServices) {
						if (app.getName().equalsIgnoreCase(payload)) {
							TCP.mUdpMultiSock
									.sendTo((app.getLocalId().serialize()
											+ Constants.SEPERATOR + Constants.DISCOVER_RESPOND)
											.getBytes(), sentFrom);
						}
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
			} else if (command.equalsIgnoreCase(Constants.DISCOVER_RESPOND)) {
				OpenUAT_ID id = OpenUAT_ID.deserialize(payload);
				RegisteredApp app = id.getApp();
				Client client = id.getApp().getClientById(id);
				if (client == null) {
					client = new Client(id);
					app.addClient(client);
				}
				if (!availableClients.containsKey(id)) {
					availableClients.put(id, sentFrom);
				}
			}
		}
	}

	public void removeApp(final RegisteredApp app) {
		if (TCP.mServices.contains(app) && (app.getNumberOfClients() == 0)) {
			TCP.mServices.remove(app);
		}
	}
}
