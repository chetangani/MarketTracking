package com.tvd.markettracking;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tvd.markettracking.posting.SendingData;
import com.tvd.markettracking.values.FunctionsCall;
import com.tvd.markettracking.values.GetSetValues;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.tvd.markettracking.values.ConstantValues.LOGIN_FAILURE;
import static com.tvd.markettracking.values.ConstantValues.LOGIN_SUCCESS;
import static com.tvd.markettracking.values.ConstantValues.SHARED_PREFS_NAME;

public class LoginActivity extends AppCompatActivity {

    private static final int RequestPermissionCode = 1;

    EditText et_login_id, et_password;
    Button sign_in_btn;
    String Login_ID="", Login_Password="";
    ProgressDialog progressDialog;

    GetSetValues getSetValues;
    FunctionsCall functionsCall;

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    private final Handler mHandler;
    {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case LOGIN_SUCCESS:
                        progressDialog.dismiss();
                        editor.putString("MTP_ID", Login_ID);
                        editor.putString("Login", "Yes");
                        editor.commit();
                        clearLoginDetails();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;

                    case LOGIN_FAILURE:
                        break;
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        setContentView(R.layout.activity_login);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkPermissionsMandAbove();
            }
        }, 1000);

        sign_in_btn = (Button) findViewById(R.id.sign_in_btn);
        et_login_id = (EditText) findViewById(R.id.et_login_id);
        et_password = (EditText) findViewById(R.id.et_password);

        getSetValues = new GetSetValues();
        functionsCall = new FunctionsCall();

        settings = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        editor = settings.edit();
        editor.apply();

        if (settings.getString("Login", "").equals("Yes")) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        sign_in_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logindetails();
            }
        });

        et_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                    logindetails();
                return false;
            }
        });
    }

    @TargetApi(23)
    private void checkPermissionsMandAbove() {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 23) {
            if (!checkPermission()) {
                requestPermission();
            }
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]
                {
                        CAMERA,
                        READ_PHONE_STATE,
                        ACCESS_FINE_LOCATION,
                        WRITE_EXTERNAL_STORAGE
                }, RequestPermissionCode);
    }

    private boolean checkPermission() {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);
        int FourthPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED &&
                FourthPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean ReadCameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadPhoneStatePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadLocationPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean ReadStoragePermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    if (!ReadCameraPermission && !ReadPhoneStatePermission && !ReadLocationPermission &&!ReadStoragePermission)
                        finish();
                }
                break;
        }
    }

    private void logindetails() {
        Login_ID = et_login_id.getText().toString();
        if (functionsCall.checkEditTextValue(Login_ID, et_login_id, "Enter Login ID")) {
            Login_Password = et_password.getText().toString();
            if (functionsCall.checkEditTextValue(Login_Password, et_password, "Enter Password")) {
                progressDialog = ProgressDialog.show(LoginActivity.this, "", "Logging In please wait..", true);
                SendingData sendingData = new SendingData();
                SendingData.Login_Details loginDetails = sendingData.new Login_Details(mHandler, getSetValues);
                loginDetails.execute(Login_ID, Login_Password);
            }
        }
        /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();*/
    }

    private void clearLoginDetails() {
        et_login_id.setText("");
        et_password.setText("");
    }
}
