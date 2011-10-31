package org.openuat.service;

import org.openuat.service.IReceiverCallback;

interface ISecureChannel {
    /** Send data to other device */
    boolean send(in byte[] data);
    
    /** Blocking receive of data from the other device. */
    byte[] receive();
    
    /** Register for asynchronously receiving data from the other device. */
    void registerReceiveHandler(IReceiverCallback receiver);
}
