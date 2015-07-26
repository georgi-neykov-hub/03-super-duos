package it.jaschke.alexandria.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.Arrays;

import it.jaschke.alexandria.R;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanBookActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    public static final String TAG = ScanBookActivity.class.getSimpleName();
    public static final String EXTRA_ISBN_CODE = "ScanBookActivity.EXTRA_ISBN_CODE";

    private static final BarcodeFormat[] SUPPORTED_FORMATS = {BarcodeFormat.EAN_13};
    private ZXingScannerView mScannerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        mScannerView = (ZXingScannerView) findViewById(R.id.scannerView);
        mScannerView.setFormats(Arrays.asList(SUPPORTED_FORMATS));
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        String bookCode = rawResult.getText();
        Intent resultIntent = new Intent().putExtra(EXTRA_ISBN_CODE, bookCode);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
