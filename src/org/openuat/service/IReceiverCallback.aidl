package org.openuat.service;

// oneway?
interface IReceiverCallback {
	void receive(in byte[] data);
}
