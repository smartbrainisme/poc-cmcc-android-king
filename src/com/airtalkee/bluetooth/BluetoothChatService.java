/*
 * Copyright (C) 2009 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.airtalkee.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import com.airtalkee.sdk.util.Log;
/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothChatService
{

	// Name for the SDP record when creating server socket
	private static final String NAME_SECURE = "BluetoothChatSecure";

	// Unique UUID for this application
	private static final UUID SERIAL_PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Member fields
	private final BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private AcceptThread mSecureAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
	// connections
	public static final int STATE_CONNECTING = 2; // now initiating an
	// outgoing connection
	public static final int STATE_CONNECTED = 3; // now connected to a
	// remote device
	public static final int STATE_CONNECT_FAILED = 4; // 
	public static final int STATE_DISCONNECT = 5; // 
	
	public static final int STATE_ACCEPT = 0;
	public static final int STATE_END = 1;
	public static final int STATE_ERROR = 2;
	public int state = STATE_END;

	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 * 
	 * @param context
	 *            The UI Activity Context
	 * @param handler
	 *            A Handler to send messages back to the UI Activity
	 */
	public BluetoothChatService(Context context, Handler handler)
	{
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;
	}

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	private synchronized void setState(int state)
	{
		Log.d(BluetoothChatService.class, "setState() " + mState + " -> " + state);
		mState = state;

		// Give the new state to the Handler so the UI Activity can
		// update
		mHandler.obtainMessage(BluetoothManager.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState()
	{
		return mState;
	}

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume()
	 */
	public synchronized void start()
	{
		Log.d(BluetoothChatService.class, "start");

		// Cancel any thread attempting to make a connection
		if (mConnectThread != null)
		{
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null)
		{
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		setState(STATE_LISTEN);

//		if (mSecureAcceptThread == null)
//		{
//			mSecureAcceptThread = new AcceptThread();
//			mSecureAcceptThread.start();
//		}
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 * @param secure
	 *            Socket Security type - Secure (true) , Insecure
	 *            (false)
	 */
	public synchronized void connect(BluetoothDevice device)
	{
		Log.d(BluetoothChatService.class, "connect to: " + device);

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING)
		{
			if (mConnectThread != null)
			{
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null)
		{
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(final BluetoothSocket socket, final BluetoothDevice device, final String socketType)
	{
		Log.d(BluetoothChatService.class, "connected, Socket Type:" + socketType);

		// Cancel the thread that completed the connection
		if (mConnectThread != null)
		{
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null)
		{
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		
		if(mSecureAcceptThread != null)
		{
			mSecureAcceptThread.cancel(null);
			mSecureAcceptThread=null;
		}
		mConnectedThread = new ConnectedThread(socket, socketType);
		mConnectedThread.start();
		setState(STATE_CONNECTED);

	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop()
	{
		Log.d(BluetoothChatService.class, "bluetooth:  stop");

		if (mConnectThread != null)
		{
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null)
		{
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mSecureAcceptThread != null)
		{
			mSecureAcceptThread.cancel(null);
			mSecureAcceptThread = null;
		}
		setState(STATE_NONE);
		state =STATE_END;
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out)
	{
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this)
		{
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(out);
	}

	public interface OnAcceptThreadEndListener
	{
		public void onAcceptThreadEnd(boolean isOk);
	}

	/**
	 * This thread runs while listening for incoming connections. It behaves
	 * like a server-side client. It runs until a connection is accepted
	 * (or until cancelled).
	 */
	private class AcceptThread extends Thread
	{
		// The local server socket
		private OnAcceptThreadEndListener listener;

		private final BluetoothServerSocket mmServerSocket;

		@SuppressWarnings("unused")
		public AcceptThread()
		{
			mState = STATE_END;
			BluetoothServerSocket tmp = null;

			// Create a new listening server socket
			try
			{
				tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, SERIAL_PORT_UUID);
			}
			catch (IOException e)
			{
				Log.e(BluetoothChatService.class, "bluetooth:  Socket Type: secure listen() failed"+ e.toString());
			}
			mmServerSocket = tmp;
		}
		

		public void run()
		{
			Log.d(BluetoothChatService.class, "bluetooth: Socket Type: secureBEGIN mAcceptThread" + this);
			setName("AcceptThread  secure");

			BluetoothSocket socket = null;
			mState = STATE_END;
			// Listen to the server socket if we're not connected
			while (mState != STATE_CONNECTED)
			{
				state = STATE_ACCEPT;
				try
				{
					// This is a blocking call and will only
					// return on a
					// successful connection or an exception
					if(mmServerSocket !=null)
						socket = mmServerSocket.accept();
				}
				catch (IOException e)
				{
					Log.e(BluetoothChatService.class, "bluetooth:  Socket Type:secure accept() failed"+e.toString());
					//setState(STATE_CONNECT_FAILED);
					state = STATE_ERROR;
					break;
				}

				// If a connection was accepted
				if (socket != null)
				{
					synchronized (BluetoothChatService.this)
					{

						Log.i(BluetoothChatService.class, "bluetooth:  mState:" + mState);
						switch (mState)
						{
							case STATE_LISTEN:
							case STATE_CONNECTING:
								// Situation
								// normal. Start
								// the connected
								// thread.
								connected(socket, socket.getRemoteDevice(), "secure");
								break;
							case STATE_NONE:
							case STATE_CONNECTED:
								// Either not
								// ready or
								// already
								// connected.
								// Terminate new
								// socket.
								try
								{
									socket.close();
								}
								catch (IOException e)
								{
									Log.e(BluetoothChatService.class, "bluetooth: Could not close unwanted socket"+ e.toString()); 
								}
								break;
						}
					}
				}

			}
			if (state != STATE_ERROR)
				state = STATE_END;
			Log.i(BluetoothChatService.class, "bluetooth:  END mAcceptThread, socket Type:  secure");

			if (this.listener != null )
			{
				this.listener.onAcceptThreadEnd(state != STATE_ERROR);
			}
		}

		public void cancel(OnAcceptThreadEndListener l)
		{
			Log.d(BluetoothChatService.class, "bluetooth: Socket Type secure cancel " + this);
			try
			{
				mmServerSocket.close();
			}
			catch (IOException e)
			{
				Log.e(BluetoothChatService.class, "bluetooth: Socket Type secureclose() of server failed"+e.toString());
			}

			if (l != null )
			{
				if(state == STATE_END)
					l.onAcceptThreadEnd(true);
				else
				{
					this.listener = l;
				}
			}

		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection
	 * with a device. It runs straight through; the connection either
	 * succeeds or fails.
	 */
	private class ConnectThread extends Thread
	{
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private String mSocketType;

		public ConnectThread(BluetoothDevice device)
		{
			mmDevice = device;
			BluetoothSocket tmp = null;
			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try
			{
				tmp = device.createRfcommSocketToServiceRecord(SERIAL_PORT_UUID);
			}
			catch (IOException e)
			{
				Log.e(BluetoothChatService.class, "bluetooth:  Socket Type: " + mSocketType + "create() failed"+e.toString());
			}
			mmSocket = tmp;
		}

		public void run()
		{
			Log.i(BluetoothChatService.class, "bluetooth: BEGIN mConnectThread SocketType:" + mSocketType);
			setName("ConnectThread" + mSocketType);

			// Always cancel discovery because it will slow down a
			// connection
			mAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try
			{
				// This is a blocking call and will only return
				// on a
				// successful connection or an exception
				mmSocket.connect();
			}
			catch (IOException e)
			{
				// Close the socket
				try
				{
					mmSocket.close();
				}
				catch (IOException e2)
				{
					Log.e(BluetoothChatService.class, "bluetooth:  unable to close() " + mSocketType + " socket during connection failure"+e2.toString());
				}
//				connectionFailed();
				setState(STATE_CONNECT_FAILED);
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothChatService.this)
			{
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice, mSocketType);
		}

		public void cancel()
		{
			try
			{
				mmSocket.close();
			}
			catch (IOException e)
			{
				Log.e(BluetoothChatService.class, "bluetooth:  close() of connect " + mSocketType + " socket failed"+e.toString());
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device.
	 * It handles all incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread
	{
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket, String socketType)
		{
			Log.d(BluetoothChatService.class, "bluetooth:  create ConnectedThread: " + socketType);
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try
			{
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			}
			catch (IOException e)
			{
				Log.e(BluetoothChatService.class, "bluetooth: temp sockets not created"+e.toString());
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run()
		{
			Log.i(BluetoothChatService.class, "bluetooth: BEGIN mConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true)
			{
				try
				{
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					// Send the obtained bytes to the UI
					// Activity
					mHandler.obtainMessage(BluetoothManager.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
				}
				catch (IOException e)
				{
					Log.e(BluetoothChatService.class, "bluetooth:  disconnected"+e.toString());
//					connectionLost();
					setState(STATE_DISCONNECT);
					// Start the service over to restart
					// listening mode
//					BluetoothChatService.this.start();
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer)
		{
			try
			{
				mmOutStream.write(buffer);
				// Share the sent message back to the UI
				// Activity
			}
			catch (IOException e)
			{
				Log.e(BluetoothChatService.class, "bluetooth:  Exception during write"+e.toString());
			}
		}

		public void cancel()
		{
			try
			{
				mmSocket.close();
			}
			catch (IOException e)
			{
				Log.e(BluetoothChatService.class, "bluetooth:  close() of connect socket failed"+e.toString());
			}
		}
	}
}
