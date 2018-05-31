package it.innove;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import com.facebook.react.bridge.*;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

public class LegacyScanManager extends ScanManager {

    private static final String LOG_TAG = "LegacyScanManager";

	LegacyScanManager(ReactApplicationContext reactContext, BleManager bleManager) {
		super(reactContext, bleManager);
	}

	@Override
	protected void stopScanInternal() {
		getBluetoothAdapter().stopLeScan(mScanCallbackInternal);
	}

	@Override
	protected void scanInternal(ReadableArray serviceUUIDs, ReadableMap options) {
		if (serviceUUIDs.size() > 0) {
			Log.d(LOG_TAG, "Filter is not working in pre-lollipop devices");
		}
		getBluetoothAdapter().startLeScan(mScanCallbackInternal);
	}

	private BluetoothAdapter.LeScanCallback mScanCallbackInternal = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecordBytes) {
			onDeviceScanned(device, rssi, scanRecordBytes);
		}
	};
}
