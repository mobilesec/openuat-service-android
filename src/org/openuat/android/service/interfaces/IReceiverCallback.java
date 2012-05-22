/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\Hannes\\SkyDrive\\Documents\\FH\\Sem5\\OpenUAT\\openuat-service-android\\src\\org\\openuat\\android\\service\\interfaces\\IReceiverCallback.aidl
 */
package org.openuat.android.service.interfaces;
@SuppressWarnings("all")
public interface IReceiverCallback extends android.os.IInterface {
    /** Local-side IPC implementation stub class. */
    public static abstract class Stub extends android.os.Binder implements
	    org.openuat.android.service.interfaces.IReceiverCallback {
	private static final java.lang.String DESCRIPTOR = "org.openuat.android.service.interfaces.IReceiverCallback";

	/** Construct the stub at attach it to the interface. */
	public Stub() {
	    this.attachInterface(this, DESCRIPTOR);
	}

	/**
	 * Cast an IBinder object into an
	 * org.openuat.android.service.interfaces.IReceiverCallback interface,
	 * generating a proxy if needed.
	 */
	public static org.openuat.android.service.interfaces.IReceiverCallback asInterface(
		android.os.IBinder obj) {
	    if ((obj == null)) {
		return null;
	    }
	    android.os.IInterface iin = (android.os.IInterface) obj
		    .queryLocalInterface(DESCRIPTOR);
	    if (((iin != null) && (iin instanceof org.openuat.android.service.interfaces.IReceiverCallback))) {
		return ((org.openuat.android.service.interfaces.IReceiverCallback) iin);
	    }
	    return new org.openuat.android.service.interfaces.IReceiverCallback.Stub.Proxy(
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
	    case TRANSACTION_receive: {
		data.enforceInterface(DESCRIPTOR);
		byte[] _arg0;
		_arg0 = data.createByteArray();
		this.receive(_arg0);
		return true;
	    }
	    }
	    return super.onTransact(code, data, reply, flags);
	}

	private static class Proxy implements
		org.openuat.android.service.interfaces.IReceiverCallback {
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

	    public void receive(byte[] data) throws android.os.RemoteException {
		android.os.Parcel _data = android.os.Parcel.obtain();
		try {
		    _data.writeInterfaceToken(DESCRIPTOR);
		    _data.writeByteArray(data);
		    mRemote.transact(Stub.TRANSACTION_receive, _data, null,
			    android.os.IBinder.FLAG_ONEWAY);
		} finally {
		    _data.recycle();
		}
	    }
	}

	static final int TRANSACTION_receive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    }

    public void receive(byte[] data) throws android.os.RemoteException;
}
