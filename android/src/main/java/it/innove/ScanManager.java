package it.innove;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.UiThreadUtil;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class ScanManager {

	private static final String TAG = "ScanManager";

	protected BluetoothAdapter bluetoothAdapter;
	protected Context context;
	protected ReactContext reactContext;
	protected BleManager bleManager;
	protected AtomicInteger scanSessionId = new AtomicInteger();
	protected Callback callback;

	public ScanManager(ReactApplicationContext reactContext, BleManager bleManager) {
		context = reactContext;
		this.reactContext = reactContext;
		this.bleManager = bleManager;
	}

	protected BluetoothAdapter getBluetoothAdapter() {
		if (bluetoothAdapter == null) {
			android.bluetooth.BluetoothManager manager = (android.bluetooth.BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
			bluetoothAdapter = manager.getAdapter();
		}
		return bluetoothAdapter;
	}

	public void stopScan(Callback callback) {
		// update scanSessionId to prevent stopping next scan by running timeout thread
		scanSessionId.incrementAndGet();

		BluetoothAdapter btAdapter = getBluetoothAdapter();

		if (btAdapter.getState() == BluetoothAdapter.STATE_ON) {
			stopScanInternal();
		}

		WritableMap map = Arguments.createMap();
		bleManager.sendEvent("BleManagerStopScan", map);

		if (callback != null) {
			callback.invoke();
		}
	}

	protected abstract void stopScanInternal();

	public void scan(ReadableArray serviceUUIDs, final int scanSeconds, ReadableMap options, Callback callback) {

		this.callback = callback;

		scanInternal(serviceUUIDs, options);

		// TODO:(pv) Scan on for X seconds, scan off for Y seconds, repeat...

		if (scanSeconds > 0) {
			new Thread() {

				@Override
				public void run() {

					final int currentScanSession = scanSessionId.incrementAndGet();

					try {
						Thread.sleep(scanSeconds * 1000);
					} catch (InterruptedException ignored) {
					}

					UiThreadUtil.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// check current scan session was not stopped
							if (scanSessionId.intValue() == currentScanSession) {
								stopScan(ScanManager.this.callback);
							}
						}
					});
				}
			}.start();
		}
		callback.invoke();
	}

	protected abstract void scanInternal(ReadableArray serviceUUIDs, ReadableMap options);

	public static boolean VERBOSE_LOG = BuildConfig.DEBUG;
	public static final Set<String> DEBUG_DEVICE_ADDRESS_FILTER;
	public static boolean DEBUG_LOG_IGNORED_DEVICE;

	static {
		DEBUG_DEVICE_ADDRESS_FILTER = initializeDebugBluetoothDeviceAddressFilterForDevelopmentMobileDevices();
		DEBUG_LOG_IGNORED_DEVICE = BuildConfig.DEBUG && DEBUG_DEVICE_ADDRESS_FILTER.size() > 0;
	}

	@NonNull
	private static Set<String> initializeDebugBluetoothDeviceAddressFilterForDevelopmentMobileDevices() {
		final Set<String> DEBUG_DEVICE_ADDRESS_FILTER = new LinkedHashSet<>();
		if (BuildConfig.DEBUG) {
			//
			// NOTE:(pv) Add here any BluetoothDevice.getAddress() values that you want to exclusively work with.
			//  All other mac addresses will be ignored, WITHOUT EVEN ANY LOGGING!
			//
			DEBUG_DEVICE_ADDRESS_FILTER.add("0E:0A:A0:02:05:B0"); // pv's Honey 2018.05
		}
		return DEBUG_DEVICE_ADDRESS_FILTER;
	}

	protected void onDeviceScanned(final BluetoothDevice device, final int rssi, final byte[] scanRecordBytes) {
		final String address = device.getAddress();
		if (DEBUG_DEVICE_ADDRESS_FILTER.size() > 0 && !DEBUG_DEVICE_ADDRESS_FILTER.contains(address)) {
			if (DEBUG_LOG_IGNORED_DEVICE) {
				Log.w(TAG, "onDeviceScanned: " + address + " not in DEBUG_DEVICE_ADDRESS_FILTER; ignoring");
			}
			return;
		}

		if (VERBOSE_LOG) {
			Log.v(TAG, "onDeviceScanned(" + device + ", rssi=" + rssi + ", scanRecordBytes=" + scanRecordBytes + ')');
		}

		UiThreadUtil.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//Log.i(TAG, "onDeviceScanned: " + device.getName());
				Peripheral peripheral;
				if (!bleManager.peripherals.containsKey(address)) {
					peripheral = new Peripheral(device, rssi, scanRecordBytes, reactContext);
					bleManager.peripherals.put(address, peripheral);
				} else {
					peripheral = bleManager.peripherals.get(address);
					peripheral.updateRssi(rssi);
					peripheral.updateData(scanRecordBytes);
				}
				WritableMap map = peripheral.asWritableMap();
				bleManager.sendEvent("BleManagerDiscoverPeripheral", map);
			}
		});
	}
}
