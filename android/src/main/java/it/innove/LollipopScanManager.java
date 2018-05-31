package it.innove;


import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class LollipopScanManager extends ScanManager {

    private static final String LOG_TAG = "LollipopScanManager";

    public LollipopScanManager(ReactApplicationContext reactContext, BleManager bleManager) {
		super(reactContext, bleManager);
	}

	@Override
	public void stopScanInternal() {
		getBluetoothAdapter().getBluetoothLeScanner().stopScan(mScanCallbackInternal);
	}

    @Override
    public void scanInternal(ReadableArray serviceUUIDs, ReadableMap options) {

        ScanSettings.Builder scanSettingsBuilder = new ScanSettings.Builder();

        List<ScanFilter> filters = new ArrayList<>();

        scanSettingsBuilder.setScanMode(options.getInt("scanMode"));
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scanSettingsBuilder.setNumOfMatches(options.getInt("numberOfMatches"));
            scanSettingsBuilder.setMatchMode(options.getInt("matchMode"));
        }
        
        if (serviceUUIDs.size() > 0) {
            for(int i = 0; i < serviceUUIDs.size(); i++){
				ScanFilter filter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUIDHelper.uuidFromString(serviceUUIDs.getString(i)))).build();
                filters.add(filter);
                Log.d(LOG_TAG, "Filter service: " + serviceUUIDs.getString(i));
            }
        }
        
        getBluetoothAdapter().getBluetoothLeScanner().startScan(filters, scanSettingsBuilder.build(), mScanCallbackInternal);
    }

	private ScanCallback mScanCallbackInternal = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            ScanRecord scanRecord = result.getScanRecord();
            byte[] scanRecordBytes = scanRecord != null ? scanRecord.getBytes() : new byte[0];
            onDeviceScanned(device, rssi, scanRecordBytes);
    	}

		@Override
		public void onBatchScanResults(final List<ScanResult> results) {
            for (ScanResult result : results) {
                onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, result);
            }
		}

		@Override
		public void onScanFailed(final int errorCode) {
            stopScan(callback);
		}
	};
}
