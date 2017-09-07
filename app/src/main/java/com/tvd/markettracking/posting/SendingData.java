package com.tvd.markettracking.posting;

import android.os.AsyncTask;
import android.os.Handler;

import com.tvd.markettracking.values.GetSetValues;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class SendingData {
    private ReceivingData receivingData = new ReceivingData();

    private String UrlPostConnection(String Post_Url, HashMap<String, String> datamap) throws IOException {
        String response = "";
        URL url = new URL(Post_Url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(15000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(getPostDataString(datamap));
        writer.flush();
        writer.close();
        os.close();
        int responseCode=conn.getResponseCode();
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            String line;
            BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line=br.readLine()) != null) {
                response+=line;
            }
        }
        else {
            response="";
        }
        return response;
    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private String UrlGetConnection(String Get_Url) throws IOException {
        String response = "";
        URL url = new URL(Get_Url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(15000);
        conn.setConnectTimeout(15000);
        int responseCode=conn.getResponseCode();
        if (responseCode == HttpsURLConnection.HTTP_OK) {
            String line;
            BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line=br.readLine()) != null) {
                response+=line;
            }
        }
        else {
            response="";
        }
        return response;
    }

    public class SendingLocationData extends AsyncTask<String, String, String> {
        String response="";

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> datamap = new HashMap<>();
            datamap.put("MTP_ID", params[0]);
            datamap.put("Longitude", params[1]);
            datamap.put("Latitude", params[2]);
            try {
                response = UrlPostConnection(DataAPI.BASE_URL+"TRKRLOG", datamap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    public class Login_Details extends AsyncTask<String, String, String> {
        String response="";
        Handler handler;
        GetSetValues getset;

        public Login_Details(Handler handler, GetSetValues getset) {
            this.handler = handler;
            this.getset = getset;
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> datamap = new HashMap<>();
            datamap.put("MTP_ID", params[0]);
            datamap.put("PASSWORD", params[1]);
            try {
                response = UrlPostConnection(DataAPI.BASE_URL+"LOGIN", datamap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            receivingData.logindetails(result, handler, getset);
        }
    }

    public class PostingDetails extends AsyncTask<String, String, String> {
        String response="";
        Handler handler;

        public PostingDetails(Handler handler) {
            this.handler = handler;
        }

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> datamap = new HashMap<>();
            datamap.put("MTP_ID", params[0]);
            datamap.put("LONGITUDE", params[1]);
            datamap.put("LATITUDE", params[2]);
            datamap.put("TOMEET", params[3]);
            datamap.put("TOADDRESS", params[4]);
            datamap.put("REMARKS", params[5]);
            datamap.put("TO_CPHOTO", params[6]);
            datamap.put("TO_PHOTO", params[7]);
            try {
                response = UrlPostConnection(DataAPI.BASE_URL, datamap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            receivingData.visiting_details(result, handler);
        }
    }
}
