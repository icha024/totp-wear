package com.clianz.totp_wear;

import android.content.Context;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.clianz.totp_wear.util.TokenCodeValidator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends WearableActivity {

    private static final String CODE_FILE = "code";
    private static final String TAG = "MainActivity";
    private static final String AB = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int SECRET_SIZE = 16;
    private static final int OUTPUT_CODE_LENGTH = 6;

    private boolean confirmSeed = false;

    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mDecriptionView;
    private TextView mButtonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mDecriptionView = (TextView) findViewById(R.id.description);
        mButtonView = (TextView) findViewById(R.id.button);

        mContainerView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_dark, getTheme()));
        mTextView.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
        mButtonView.setTextColor(getResources().getColor(android.R.color.white, getTheme()));
        mDecriptionView.setTextColor(getResources().getColor(android.R.color.white, getTheme()));

        if (fileExistance(CODE_FILE)) {
            Log.i(TAG, "FILE EXIST");
            renderCode();
        } else {
            Log.i(TAG, "NO FILE");
            renderSeed();
        }
    }

    public void onClick(View v) {
        // Perform action on click
//        Log.i(TAG, "onClick: BUTTON CLICKED");
        if (fileExistance(CODE_FILE) && !confirmSeed) {
            // reset it
            deleteFile(CODE_FILE);
            renderSeed();
            confirmSeed = true;
        } else {
            renderCode();
            confirmSeed = false;
        }
    }

    private void renderSeed() {
        String code = createFile();
        mDecriptionView.setText(R.string.save_seed);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        mTextView.setText(code);
        mButtonView.setText(R.string.button_continue);
    }

    private void renderCode() {
        String code = readFile();
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
        mDecriptionView.setText(R.string.your_code);
        mButtonView.setText(R.string.button_reset);

        try {
            mTextView.setText(new TokenCodeValidator().generateTOTP(code, OUTPUT_CODE_LENGTH));
        } catch (GeneralSecurityException e) {
            mTextView.setText("TOTP Error");
            Log.e(TAG, "onCreate: ", e);
        }
    }

    private String readFile() {
        FileInputStream fis = null;
        try {
            fis = openFileInput(CODE_FILE);
            byte[] buf = new byte[SECRET_SIZE];
            fis.read(buf);
            String code = new String(buf, Charset.forName("UTF8"));
//            Log.i(TAG, "CODE READ: " + code);
            return code;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error: ", e);
        } catch (IOException e) {
            Log.e(TAG, "Error: ", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error: ", e);
                }
            }
        }
        return null;
    }

    // Ref: http://stackoverflow.com/questions/10576930/trying-to-check-if-a-file-exists-in-internal-storage
    private boolean fileExistance(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    private String createFile() {
        FileOutputStream fos = null;
        String code = generateRandom(SECRET_SIZE);
        try {
            fos = openFileOutput(CODE_FILE, Context.MODE_PRIVATE);
            fos.write(code.getBytes(Charset.forName("UTF8")));
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error: ", e);
        } catch (IOException e) {
            Log.e(TAG, "Error: ", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error: ", e);
                }
            }
        }
        return code;
    }

    private String generateRandom(int len) {
        // REF: http://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }
}
