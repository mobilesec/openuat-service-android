///**
// * Copyright Hannes Markschlaeger
// * File created 13.03.2012
// * 
// * This program is free software; you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation; either version 2 of the License, or
// * (at your option) any later version. 
// */
//package org.openuat.android.service;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.List;
//
//import android.util.Log;
//
///**
// * The Class ReceiveEventWrapper.
// * 
// * 
// * @author Hannes Markschlaeger
// */
//public class ReceiveEventWrapper {
//
//    /**
//     * The Interface InputStreamEventHandler.
//     * 
//     * 
//     * @author Hannes Markschlaeger
//     */
//    public interface InputStreamEventHandler {
// 
//	/**
//	 * On data received.
//	 * 
//	 * @param data
//	 *            the data
//	 */
//	void onDataReceived(byte[] data);
//    }
//
//    /** The Constant UPDATE_INTERVALL. */
//    private static final int UPDATE_INTERVALL = 100;
//
//    /** The m eventhandler. */
//    private List<InputStreamEventHandler> mEventhandler = null;
//
//    /** The m runner. */
//    private final Thread mRunner = new Thread(new Runnable() {
//	private int singleByte;
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see java.lang.Runnable#run()
//	 */
//	@Override
//	public void run() {
//	    // TODO unnecessary because chopping files into chunks can only be done by the app itself
//	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//	    byte[] data = null;
//	    while (mStream != null) {
//		try {
//		    singleByte = mStream.read();
//
//		    if (singleByte != -1) {
//			Log.d(this.toString(), "data received");
//			data = new byte[1024];
//			data[0] = (byte) singleByte;
//			final int numBytes = mStream.read(data, 1,
//				data.length - 1);
//			buffer.write(data);
//
//		    } else {
//			for (final InputStreamEventHandler e : mEventhandler) {
//			    e.onDataReceived(buffer.toByteArray());
//			}
//			buffer = new ByteArrayOutputStream();
//		    }
//		    Thread.sleep(ReceiveEventWrapper.UPDATE_INTERVALL);
//		} catch (final IOException e) {
//		    e.printStackTrace();
//		} catch (final InterruptedException e) {
//		    e.printStackTrace();
//		}
//	    }
//	}
//    });
//
//    /** The m stream. */
//    private InputStream mStream = null;
//
//    /**
//     * Instantiates a new receive event wrapper.
//     * 
//     * @param stream
//     *            the stream
//     */
//    public ReceiveEventWrapper(final InputStream stream) {
//	mStream = stream;
//	mEventhandler = new ArrayList<InputStreamEventHandler>();
//	mRunner.run();
//    }
//
//    /**
//     * Adds the input stream event handler.
//     * 
//     * @param handler
//     *            the handler
//     */
//    public void addInputStreamEventHandler(final InputStreamEventHandler handler) {
//	mEventhandler.add(handler);
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see java.lang.Object#finalize()
//     */
//    @Override
//    protected void finalize() throws Throwable {
//	mStream = null;
//	super.finalize();
//    }
//
//    /**
//     * Removes the input stream event handler.
//     * 
//     * @param handler
//     *            the handler
//     */
//    public void removeInputStreamEventHandler(
//	    final InputStreamEventHandler handler) {
//	mEventhandler.remove(handler);
//    }
//}
