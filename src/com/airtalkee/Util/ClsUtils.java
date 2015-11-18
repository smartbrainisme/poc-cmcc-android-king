package com.airtalkee.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class ClsUtils
{
	public static BluetoothDevice remoteDevice = null;
	/**
	 * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static public boolean createBond(Class btClass, BluetoothDevice btDevice)
			throws Exception
	{
		Method createBondMethod = btClass.getMethod("createBond");
		Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
		Log.e("kill","createBond===="+returnValue);
		return returnValue.booleanValue();
	}
	/**
	 * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public boolean removeBond(Class btClass, BluetoothDevice btDevice)
			throws Exception
	{
		Method removeBondMethod = btClass.getMethod("removeBond");
		Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static public boolean setPin(Class btClass, BluetoothDevice btDevice,
			String str) throws Exception
	{
		try
		{
			Method setPin = btClass.getDeclaredMethod("setPin", new Class[] {byte[].class});
			
			Boolean returnValue = (Boolean) setPin.invoke(btDevice, new Object[] {str.getBytes()});
			
			
			Log.e("kill", "SetPin ==" + returnValue);
		}
		catch (SecurityException e)
		{
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static public boolean cancelPairingUserInput(Class btClass,
			BluetoothDevice device)

	throws Exception
	{
		Method cancelPairingUserInput = btClass.getMethod("cancelPairingUserInput");
		// cancelBondProcess()
		Boolean returnValue = (Boolean) cancelPairingUserInput.invoke(device);
		return returnValue.booleanValue();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	static public boolean cancelBondProcess(Class btClass, BluetoothDevice device)

	throws Exception
	{
		Method cancelBondProcess = btClass.getMethod("cancelBondProcess");
		Boolean returnValue = (Boolean) cancelBondProcess.invoke(device);
		return returnValue.booleanValue();
	}

	/**
	 * 
	 * @param clsShow
	 */
	@SuppressWarnings("rawtypes")
	static public void printAllInform(Class clsShow)
	{
		try
		{
			Method[] hideMethod = clsShow.getMethods();
			int i = 0;
			for (; i < hideMethod.length; i++)
			{
				Log.e("method name", hideMethod[i].getName() + ";and the i is:"
						+ i);
			}
			Field[] allFields = clsShow.getFields();
			for (i = 0; i < allFields.length; i++)
			{
				Log.e("Field name", allFields[i].getName());
			}
		}
		catch (SecurityException e)
		{
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean pair(String strAddr, String strPsw) {
		boolean result = false;
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		bluetoothAdapter.cancelDiscovery();
		if (!bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.enable();
		}
		if (!BluetoothAdapter.checkBluetoothAddress(strAddr)) {
			Log.d("mylog", "devAdd un effient!");
		}
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(strAddr);
		if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
			try {
				ClsUtils.setPin(device.getClass(), device, strPsw);
				ClsUtils.createBond(device.getClass(), device);
				remoteDevice = device;
				result = true;
			} catch (Exception e) {
				Log.d("mylog", "setPiN failed!----" + e.toString());
			}
		} else {
			remoteDevice = device;
			result = true;
		}
		return result;
	}
}