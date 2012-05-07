/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\hannes.m\\Desktop\\bac\\openuat_service\\src\\org\\openuat\\android\\service\\interfaces\\IConnectionCallback.aidl
 */
package org.openuat.android.service.interfaces;
@SuppressWarnings("all")
public interface IConnectionCallback extends android.os.IInterface {
    /** Local-side IPC implementation stub class. */
    public static abstract class Stub extends android.os.Binder implements
	    org.openuat.android.service.interfaces.IConnectionCallback {
	private static final java.lang.String DESCRIPTOR = "org.openuat.android.service.interfaces.IConnectionCallback";

	/** Construct the stub at attach it to the interface. */
	public Stub() {
	    this.attachInterface(this, DESCRIPTOR);
	}

	/**
	 * Cast an IBinder object into an
	 * org.openuat.android.service.interfaces.IConnectionCallback interface,
	 * generating a proxy if needed.
	 */
	public static org.openuat.android.service.interfaces.IConnectionCallback asInterface(
		android.os.IBinder obj) {
	    if ((obj == null)) {
		return null;
	    }
	    android.os.IInterface iin = (android.os.IInterface) obj
		    .queryLocalInterface(DESCRIPTOR);
	    if (((iin != null) && (iin instanceof org.openuat.android.service.interfaces.IConnectionCallback))) {
		return ((org.openuat.android.service.interfaces.IConnectionCallback) iin);
	    }
	    return new org.openuat.android.service.interfaces.IConnectionCallback.Stub.Proxy(
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
	    case TRANSACTION_connectionIncoming: {
		data.enforceInterface(DESCRIPTOR);
		org.openuat.android.service.interfaces.ISecureChannel _arg0;
		_arg0 = org.openuat.android.service.interfaces.ISecureChannel.Stub
			.asInterface(data.readStrongBinder());
		this.connectionIncoming(_arg0);
		reply.writeNoException();
		return true;
	    }
	    }
	    return super.onTransact(code, data, reply, flags);
	}

	private static class Proxy implements
		org.openuat.android.service.interfaces.IConnectionCallback {
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

	    public void connectionIncoming(
		    org.openuat.android.service.interfaces.ISecureChannel connection)
		    throws android.os.RemoteException {
		android.os.Parcel _data = android.os.Parcel.obtain();
		android.os.Parcel _reply = android.os.Parcel.obtain();
		try {
		    _data.writeInterfaceToken(DESCRIPTOR);
		    _data.writeStrongBinder((((connection != null)) ? (connection
			    .asBinder()) : (null)));
		    mRemote.transact(Stub.TRANSACTION_connectionIncoming,
			    _data, _reply, 0);
		    _reply.readException();
		} finally {
		    _reply.recycle();
		    _data.recycle();
		}
	    }
	}

	static final int TRANSACTION_connectionIncoming = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    }

    public void connectionIncoming(
	    org.openuat.android.service.interfaces.ISecureChannel connection)
	    throws android.os.RemoteException;
}
