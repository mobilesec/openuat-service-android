/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: C:\\Users\\Hannes\\SkyDrive\\Documents\\FH\\Sem5\\OpenUAT\\openuat-service-android\\src\\org\\openuat\\android\\service\\interfaces\\IDeviceAuthenticator.aidl
 */
package org.openuat.android.service.interfaces;

/**
 * Main interface for the OpenUAT authentication service. It allows applications
 * to establish and use secure, authenticated channels to other devices over any
 * supported communication channel (e.g. WLAN, Bluetooth, or XMPP).
 */
@SuppressWarnings("all")
public interface IDeviceAuthenticator extends android.os.IInterface {
	/** Local-side IPC implementation stub class. */
	public static abstract class Stub extends android.os.Binder implements
			org.openuat.android.service.interfaces.IDeviceAuthenticator {
		private static final java.lang.String DESCRIPTOR = "org.openuat.android.service.interfaces.IDeviceAuthenticator";

		/** Construct the stub at attach it to the interface. */
		public Stub() {
			this.attachInterface(this, DESCRIPTOR);
		}

		/**
		 * Cast an IBinder object into an
		 * org.openuat.android.service.interfaces.IDeviceAuthenticator
		 * interface, generating a proxy if needed.
		 */
		public static org.openuat.android.service.interfaces.IDeviceAuthenticator asInterface(
				android.os.IBinder obj) {
			if ((obj == null)) {
				return null;
			}
			android.os.IInterface iin = (android.os.IInterface) obj
					.queryLocalInterface(DESCRIPTOR);
			if (((iin != null) && (iin instanceof org.openuat.android.service.interfaces.IDeviceAuthenticator))) {
				return ((org.openuat.android.service.interfaces.IDeviceAuthenticator) iin);
			}
			return new org.openuat.android.service.interfaces.IDeviceAuthenticator.Stub.Proxy(
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
			case TRANSACTION_getAvailableDevices: {
				data.enforceInterface(DESCRIPTOR);
				java.lang.String _arg0;
				_arg0 = data.readString();
				java.lang.String[] _result = this.getAvailableDevices(_arg0);
				reply.writeNoException();
				reply.writeStringArray(_result);
				return true;
			}
			case TRANSACTION_register: {
				data.enforceInterface(DESCRIPTOR);
				java.lang.String _arg0;
				_arg0 = data.readString();
				org.openuat.android.service.interfaces.IConnectionCallback _arg1;
				_arg1 = org.openuat.android.service.interfaces.IConnectionCallback.Stub
						.asInterface(data.readStrongBinder());
				this.register(_arg0, _arg1);
				reply.writeNoException();
				return true;
			}
			case TRANSACTION_authenticate: {
				data.enforceInterface(DESCRIPTOR);
				java.lang.String _arg0;
				_arg0 = data.readString();
				java.lang.String _arg1;
				_arg1 = data.readString();
				this.authenticate(_arg0, _arg1);
				reply.writeNoException();
				return true;
			}
			}
			return super.onTransact(code, data, reply, flags);
		}

		private static class Proxy implements
				org.openuat.android.service.interfaces.IDeviceAuthenticator {
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

			/**
			 * Returns a list of devices currently available for a connection.
			 * 
			 * @param _serviceId
			 *            The identifier of the application of interest.
			 * @return A list of available devices to connect to.
			 */
			public java.lang.String[] getAvailableDevices(
					java.lang.String _serviceId)
					throws android.os.RemoteException {
				android.os.Parcel _data = android.os.Parcel.obtain();
				android.os.Parcel _reply = android.os.Parcel.obtain();
				java.lang.String[] _result;
				try {
					_data.writeInterfaceToken(DESCRIPTOR);
					_data.writeString(_serviceId);
					mRemote.transact(Stub.TRANSACTION_getAvailableDevices,
							_data, _reply, 0);
					_reply.readException();
					_result = _reply.createStringArray();
				} finally {
					_reply.recycle();
					_data.recycle();
				}
				return _result;
			}

			/**
			 * Registers the calling application for incoming requests for
			 * secure communication. It is recommended to either use a UUID in
			 * String format or e.g. the unique package name of the application
			 * for registering the service.
			 * 
			 * @param serviceId
			 *            A unique string identifying the application (e.g. the
			 *            package name)
			 * @param connectionCallback
			 *            The interface to be called when requested or new
			 *            SecureChannels are incoming.
			 */
			public void register(
					java.lang.String serviceId,
					org.openuat.android.service.interfaces.IConnectionCallback connectionCallback)
					throws android.os.RemoteException {
				android.os.Parcel _data = android.os.Parcel.obtain();
				android.os.Parcel _reply = android.os.Parcel.obtain();
				try {
					_data.writeInterfaceToken(DESCRIPTOR);
					_data.writeString(serviceId);
					_data.writeStrongBinder((((connectionCallback != null)) ? (connectionCallback
							.asBinder()) : (null)));
					mRemote.transact(Stub.TRANSACTION_register, _data, _reply,
							0);
					_reply.readException();
				} finally {
					_reply.recycle();
					_data.recycle();
				}
			}

			/**
			 * Asks the service to initiate a secure (encrypted and
			 * authenticated) connection to the device and service on this
			 * device as specified by the parameters. If this device was not
			 * previously authenticated, then the service will perform
			 * human-assisted device authentication. Once the connection has
			 * been established and verified it will be sent to the
			 * IConnectionCallback.
			 * 
			 * @param serviceID
			 *            The application
			 * @param device
			 *            The remote device to connect to. Use only strings
			 *            returned by getAvailableDevices!
			 */
			public void authenticate(java.lang.String serviceId,
					java.lang.String device) throws android.os.RemoteException {
				android.os.Parcel _data = android.os.Parcel.obtain();
				android.os.Parcel _reply = android.os.Parcel.obtain();
				try {
					_data.writeInterfaceToken(DESCRIPTOR);
					_data.writeString(serviceId);
					_data.writeString(device);
					mRemote.transact(Stub.TRANSACTION_authenticate, _data,
							_reply, 0);
					_reply.readException();
				} finally {
					_reply.recycle();
					_data.recycle();
				}
			}
		}

		static final int TRANSACTION_getAvailableDevices = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
		static final int TRANSACTION_register = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
		static final int TRANSACTION_authenticate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
	}

	/**
	 * Returns a list of devices currently available for a connection.
	 * 
	 * @param _serviceId
	 *            The identifier of the application of interest.
	 * @return A list of available devices to connect to.
	 */
	public java.lang.String[] getAvailableDevices(java.lang.String _serviceId)
			throws android.os.RemoteException;

	/**
	 * Registers the calling application for incoming requests for secure
	 * communication. It is recommended to either use a UUID in String format or
	 * e.g. the unique package name of the application for registering the
	 * service.
	 * 
	 * @param serviceId
	 *            A unique string identifying the application (e.g. the package
	 *            name)
	 * @param connectionCallback
	 *            The interface to be called when requested or new
	 *            SecureChannels are incoming.
	 */
	public void register(
			java.lang.String serviceId,
			org.openuat.android.service.interfaces.IConnectionCallback connectionCallback)
			throws android.os.RemoteException;

	/**
	 * Asks the service to initiate a secure (encrypted and authenticated)
	 * connection to the device and service on this device as specified by the
	 * parameters. If this device was not previously authenticated, then the
	 * service will perform human-assisted device authentication. Once the
	 * connection has been established and verified it will be sent to the
	 * IConnectionCallback.
	 * 
	 * @param serviceID
	 *            The application
	 * @param device
	 *            The remote device to connect to. Use only strings returned by
	 *            getAvailableDevices!
	 */
	public void authenticate(java.lang.String serviceId, java.lang.String device)
			throws android.os.RemoteException;
}
