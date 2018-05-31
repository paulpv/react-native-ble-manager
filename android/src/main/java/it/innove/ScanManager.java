package it.innove;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.util.concurrent.atomic.AtomicInteger;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

public abstract class ScanManager {

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

		if (scanSeconds > 0) {
			new Thread() {

				@Override
				public void run() {

					final int currentScanSession = scanSessionId.incrementAndGet();

					try {
						Thread.sleep(scanSeconds * 1000);
					} catch (InterruptedException ignored) {
					}

					runOnUiThread(new Runnable() {
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

	protected void onDeviceScanned(final BluetoothDevice device, final int rssi, final byte[] scanRecordBytes) {
		final String address = device.getAddress();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//Log.i(LOG_TAG, "DiscoverPeripheral: " + device.getName());
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
