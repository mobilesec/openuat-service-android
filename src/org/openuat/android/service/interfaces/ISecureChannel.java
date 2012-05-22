/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\Hannes\\SkyDrive\\Documents\\FH\\Sem5\\OpenUAT\\openuat-service-android\\src\\org\\openuat\\android\\service\\interfaces\\ISecureChannel.aidl
 */
package org.openuat.android.service.interfaces;
@SuppressWarnings("all")
public interface ISecureChannel extends android.os.IInterface {
    /** Local-side IPC implementation stub class. */
    public static abstract class Stub extends android.os.Binder implements
	    org.openuat.android.service.interfaces.ISecureChannel {
	private static final java.lang.String DESCRIPTOR = "org.openuat.android.service.interfaces.ISecureChannel";

	/** Construct the stub at attach it to the interface. */
	public Stub() {
	    this.attachInterface(this, DESCRIPTOR);
	}

	/**
	 * Cast an IBinder object into an
	 * org.openuat.android.service.interfaces.ISecureChannel interface,
	 * generating a proxy if needed.
	 */
	public static org.openuat.android.service.interfaces.ISecureChannel asInterface(
		android.os.IBinder obj) {
	    if ((obj == null)) {
		return null;
	    }
	    android.os.IInterface iin = (android.os.IInterface) obj
		    .queryLocalInterface(DESCRIPTOR);
	    if (((iin != null) && (iin instanceof org.openuat.android.service.interfaces.ISecureChannel))) {
		return ((org.openuat.android.service.interfaces.ISecureChannel) iin);
	    }
	    return new org.openuat.android.service.interfaces.ISecureChannel.Stub.Proxy(
		    obj);
	}

	public android.os.IBinder asBinder() {
	    return this;
	}

	@Override
	public boolean onTransact(int code, android.os.Parcel data,
		android.os.Parcel reply, int flags)
		throws android.os.RemoteException {
	    switch (code) {
	    case INTERFACE_TRANSACTION: {
		reply.writeString(DESCRIPTOR);
		return true;
	    }
	    case TRANSACTION_send: {
		data.enforceInterface(DESCRIPTOR);
		byte[] _arg0;
		_arg0 = data.createByteArray();
		boolean _result = this.send(_arg0);
		reply.writeNoException();
		reply.writeInt(((_result) ? (1) : (0)));
		return true;
	    }
	    case TRANSACTION_receive: {
		data.enforceInterface(DESCRIPTOR);
		byte[] _result = this.receive();
		reply.writeNoException();
		reply.writeByteArray(_result);
		return true;
	    }
	    case TRANSACTION_registerReceiveHandler: {
		data.enforceInterface(DESCRIPTOR);
		org.openuat.android.service.interfaces.IReceiverCallback _arg0;
		_arg0 = org.openuat.android.service.interfaces.IReceiverCallback.Stub
			.asInterface(data.readStrongBinder());
		this.registerReceiveHandler(_arg0);
		reply.writeNoException();
		return true;
	    }
	    case TRANSACTION_unregisterReceiveHandler: {
		data.enforceInterface(DESCRIPTOR);
		org.openuat.android.service.interfaces.IReceiverCallback _arg0;
		_arg0 = org.openuat.android.service.interfaces.IReceiverCallback.Stub
			.asInterface(data.readStrongBinder());
		this.unregisterReceiveHandler(_arg0);
		reply.writeNoException();
		return true;
	    }
	    }
	    return super.onTransact(code, data, reply, flags);
	}

	private static class Proxy implements
		org.openuat.android.service.interfaces.ISecureChannel {
	    private android.os.IBinder mRemote;

	    Proxy(android.os.IBinder remote) {
		mRemote = remote;
	    }

	    public android.os.IBinder asBinder() {
		return mRemote;
	    }

	    public java.lang.String getInterfaceDescriptor() {
		return DESCRIPTOR;
	    }

	    /** Send data to other device */
	    public boolean send(byte[] data) throws android.os.RemoteException {
		android.os.Parcel _data = android.os.Parcel.obtain();
		android.os.Parcel _reply = android.os.Parcel.obtain();
		boolean _result;
		try {
		    _data.writeInterfaceToken(DESCRIPTOR);
		    _data.writeByteArray(data);
		    mRemote.transact(Stub.TRANSACTION_send, _data, _reply, 0);
		    _reply.readException();
		    _result = (0 != _reply.readInt());
		} finally {
		    _reply.recycle();
		    _data.recycle();
		}
		return _result;
	    }

	    /** Blocking receive of data from the other device. */
	    public byte[] receive() throws android.os.RemoteException {
		android.os.Parcel _data = android.os.Parcel.obtain();
		android.os.Parcel _reply = android.os.Parcel.obtain();
		byte[] _result;
		try {
		    _data.writeInterfaceToken(DESCRIPTOR);
		    mRemote.transact(Stub.TRANSACTION_receive, _data, _reply, 0);
		    _reply.readException();
		    _result = _reply.createByteArray();
		} finally {
		    _reply.recycle();
		    _data.recycle();
		}
		return _result;
	    }

	    /**
	     * Register for asynchronously receiving data from the other device.
	     */
	    public void registerReceiveHandler(
		    org.openuat.android.service.interfaces.IReceiverCallback receiver)
		    throws android.os.RemoteException {
		android.os.Parcel _data = android.os.Parcel.obtain();
		android.os.Parcel _reply = android.os.Parcel.obtain();
		try {
		    _data.writeInterfaceToken(DESCRIPTOR);
		    _data.writeStrongBinder((((receiver != null)) ? (receiver
			    .asBinder()) : (null)));
		    mRemote.transact(Stub.TRANSACTION_registerReceiveHandler,
			    _data, _reply, 0);
		    _reply.readException();
		} finally {
		    _reply.recycle();
		    _data.recycle();
		}
	    }

	    public void unregisterReceiveHandler(
		    org.openuat.android.service.interfaces.IReceiverCallback receiver)
		    throws android.os.RemoteException {
		android.os.Parcel _data = android.os.Parcel.obtain();
		android.os.Parcel _reply = android.os.Parcel.obtain();
		try {
		    _data.writeInterfaceToken(DESCRIPTOR);
		    _data.writeStrongBinder((((receiver != null)) ? (receiver
			    .asBinder()) : (null)));
		    mRemote.transact(Stub.TRANSACTION_unregisterReceiveHandler,
			    _data, _reply, 0);
		    _reply.readException();
		} finally {
		    _reply.recycle();
		    _data.recycle();
		}
	    }
	}

	static final int TRANSACTION_send = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
	static final int TRANSACTION_receive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
	static final int TRANSACTION_registerReceiveHandler = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
	static final int TRANSACTION_unregisterReceiveHandler = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    }

    /** Send data to other device */
    public boolean send(byte[] data) throws android.os.RemoteException;

    /** Blocking receive of data from the other device. */
    public byte[] receive() throws android.os.RemoteException;

    /** Register for asynchronously receiving data from the other device. */
    public void registerReceiveHandler(
	    org.openuat.android.service.interfaces.IReceiverCallback receiver)
	    throws android.os.RemoteException;

    public void unregisterReceiveHandler(
	    org.openuat.android.service.interfaces.IReceiverCallback receiver)
	    throws android.os.RemoteException;
}
