package com.tvd.markettracking.fragment;


import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.Toast;

import com.tvd.markettracking.MainActivity;
import com.tvd.markettracking.R;
import com.tvd.markettracking.values.FunctionsCall;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.tvd.markettracking.values.ConstantValues.DETAILS_FAILURE;
import static com.tvd.markettracking.values.ConstantValues.DETAILS_SUCCESS;

public class Details_Fragment extends Fragment implements AdapterView.OnItemClickListener {
    View view;

    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int MEDIA_TYPE_IMAGE = 1;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyAx1zMcEuGD4lk77coCmdbnCQzsoV8p04s";

    private static Uri pic_1_fileUri, pic_2_fileUri; // file url to store image/video
    static File pic_1_mediaFile, pic_2_mediaFile;
    Bitmap pic_1, pic_2;

    EditText et_name, et_address, et_remarks, et_email, et_mobile;
    AutoCompleteTextView actv_company;
    Button submit_btn;
    String name="", address="", remarks="", email="", mobile="", company_name="", pic_1_value="", pic_2_value="",
            pic_1_encoded="", pic_2_encoded="";
    ImageView iv_first, iv_second;
    static boolean first_img_clicked=false, second_img_clicked=false;
    ProgressDialog progressDialog;

    FunctionsCall functionsCall;
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    private final Handler mHandler;
    {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case DETAILS_SUCCESS:
                        progressDialog.dismiss();
                        clearDetails();
                        break;

                    case DETAILS_FAILURE:
                        progressDialog.dismiss();
                        Snackbar.make(submit_btn, "Error sending details", Snackbar.LENGTH_SHORT).show();
                        break;
                }
            }
        };
    }

    public Details_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_details, container, false);

        et_name = (EditText) view.findViewById(R.id.et_name);
        et_address = (EditText) view.findViewById(R.id.et_address);
        et_remarks = (EditText) view.findViewById(R.id.et_remarks);
        et_email = (EditText) view.findViewById(R.id.et_email);
        et_mobile = (EditText) view.findViewById(R.id.et_mobile);
        actv_company = (AutoCompleteTextView) view.findViewById(R.id.actv_company_name);
        submit_btn = (Button) view.findViewById(R.id.submit_btn);
        iv_first = (ImageView) view.findViewById(R.id.pic_cap_1);
        iv_second = (ImageView) view.findViewById(R.id.pic_cap_2);

        functionsCall = new FunctionsCall();
        settings = ((MainActivity) getActivity()).getShared();
        editor = ((MainActivity) getActivity()).getEditor();

        /*actv_company.setAdapter(new GooglePlacesAutocompleteAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line));
        actv_company.setOnItemClickListener(this);*/

        submit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitDetails();
            }
        });

        iv_first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                first_img_clicked = true;
                second_img_clicked = false;
                captureImage();
            }
        });

        iv_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                first_img_clicked = false;
                second_img_clicked = true;
                captureImage();
            }
        });

        return view;
    }

    private void submitDetails() {
        /*name = et_name.getText().toString();
        if (functionsCall.checkEditTextValue(name, et_name, "Enter Name")) {
            mobile = et_mobile.getText().toString();
            if (functionsCall.checkEditTextValue(mobile, et_mobile, "Enter Mobile Number")) {
                if (!(mobile.length() == 10)) {
                    et_mobile.setError("Enter Correct Mobile Number");
                } else {
                    company_name = actv_company.getText().toString();
                    if (TextUtils.isEmpty(company_name)) {
                        actv_company.setError("Enter Company Name");
                    } else {
                        address = et_address.getText().toString();
                        if (functionsCall.checkEditTextValue(address, et_address, "Enter your current Address")) {
                            remarks = et_remarks.getText().toString();
                            if (functionsCall.checkEditTextValue(remarks, et_remarks, "Enter Remarks")) {
                                email = et_email.getText().toString();
                                progressDialog = ProgressDialog.show(getActivity(), "", "Posting Details please wait..", true);
                                SendingData sendingData = new SendingData();
                                SendingData.PostingDetails postingDetails = sendingData.new PostingDetails(mHandler);
                                postingDetails.execute(settings.getString("MTP_ID", ""), ""+ LocationTrace.mLastLocation.getLongitude(),
                                        ""+LocationTrace.mLastLocation.getLatitude(), name, address, remarks, pic_1_encoded, pic_2_encoded);
                            }
                        }
                    }
                }
            }
        }*/
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String str = (String) parent.getItemAtPosition(position);
        et_address.setText(str);
    }

    @TargetApi(24)
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (first_img_clicked) {
            pic_1_fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE, getActivity());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, pic_1_fileUri);
        } else {
            pic_2_fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE, getActivity());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, pic_2_fileUri);
        }
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getActivity(), "User cancelled image capture", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Sorry! Failed to capture image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void previewCapturedImage() {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            if (first_img_clicked) {
                pic_1 = BitmapFactory.decodeFile(pic_1_fileUri.getPath(), options);
                iv_first.setImageBitmap(rotateImage(pic_1, pic_1_fileUri.getPath()));
                pic_1_value = pic_1_mediaFile.toString();
                pic_1_encoded = functionsCall.encodeImage(rotateImage(pic_1, pic_1_fileUri.getPath()), 100);
            } else {
                pic_2 = BitmapFactory.decodeFile(pic_2_fileUri.getPath(), options);
                iv_second.setImageBitmap(rotateImage(pic_2, pic_2_fileUri.getPath()));
                pic_2_value = pic_2_mediaFile.toString();
                pic_2_encoded = functionsCall.encodeImage(rotateImage(pic_2, pic_2_fileUri.getPath()), 100);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 16;
            if (first_img_clicked) {
                pic_1 = BitmapFactory.decodeFile(pic_1_fileUri.getPath(), options);
                iv_first.setImageBitmap(rotateImage(pic_1, pic_1_fileUri.getPath()));
                pic_1_value = pic_1_mediaFile.toString();
                pic_1_encoded = functionsCall.encodeImage(rotateImage(pic_1, pic_1_fileUri.getPath()), 75);
            } else {
                pic_2 = BitmapFactory.decodeFile(pic_2_fileUri.getPath(), options);
                iv_second.setImageBitmap(rotateImage(pic_2, pic_2_fileUri.getPath()));
                pic_2_value = pic_2_mediaFile.toString();
                pic_2_encoded = functionsCall.encodeImage(rotateImage(pic_2, pic_2_fileUri.getPath()), 75);
            }
        }
    }

    public static Bitmap rotateImage(Bitmap src, String Imagepath) {
        Bitmap bmp = null;
        Matrix matrix = new Matrix();
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(Imagepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        Log.d("debug", "" + orientation);
        if (orientation == 1) {
            bmp = src;
        } else if (orientation == 3) {
            matrix.postRotate(180);
            bmp = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        } else if (orientation == 8) {
            matrix.postRotate(270);
            bmp = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        } else {
            matrix.postRotate(90);
            bmp = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        }
        return bmp;
    }

    public Uri getOutputMediaFileUri(int type, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", getOutputMediaFile(type));
        } else return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {
        File mediaFile = null;
        File mediaStorageDir = new File(android.os.Environment.getExternalStorageDirectory(), "MarketingTrace");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("MMdd_HHmmss", Locale.getDefault()).format(new Date());
        if (type == MEDIA_TYPE_IMAGE) {
            if (first_img_clicked) {
                pic_1_mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + ".jpg");
                mediaFile = pic_1_mediaFile;
            } else {
                pic_2_mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + ".jpg");
                mediaFile = pic_2_mediaFile;
            }
        } else {
            return null;
        }
        return mediaFile;
    }

    private void clearDetails() {
        et_name.setText("");
        et_address.setText("");
        et_remarks.setText("");
        iv_first.setImageResource(R.mipmap.ic_camera_icon);
        iv_second.setImageResource(R.mipmap.ic_camera_icon);
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index).toString();
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
//            sb.append("&types=");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return resultList;
        } catch (IOException e) {
            e.printStackTrace();
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            FunctionsCall functionsCall = new FunctionsCall();
            functionsCall.logStatus("Result-: "+jsonResults.toString());
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mHandler.removeCallbacksAndMessages(null);
    }
}
