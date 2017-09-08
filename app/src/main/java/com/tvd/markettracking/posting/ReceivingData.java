package com.tvd.markettracking.posting;

import android.os.Handler;

import com.tvd.markettracking.values.GetSetValues;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.tvd.markettracking.values.ConstantValues.DETAILS_FAILURE;
import static com.tvd.markettracking.values.ConstantValues.DETAILS_SUCCESS;
import static com.tvd.markettracking.values.ConstantValues.LOGIN_FAILURE;
import static com.tvd.markettracking.values.ConstantValues.LOGIN_SUCCESS;

public class ReceivingData {
    private String parseServerXML(String result) {
        String value="";
        XmlPullParserFactory pullParserFactory;
        InputStream res;
        try {
            res = new ByteArrayInputStream(result.getBytes());
            pullParserFactory = XmlPullParserFactory.newInstance();
            pullParserFactory.setNamespaceAware(true);
            XmlPullParser parser = pullParserFactory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(res, null);
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        switch (name) {
                            case "string":
                                value =  parser.nextText();
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    void logindetails(String result, Handler handler, GetSetValues getSet) {
        result = parseServerXML(result);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(result);
            String message = jsonObject.getString("message");
            if (StringUtils.startsWithIgnoreCase(message, "Success")) {
                handler.sendEmptyMessage(LOGIN_SUCCESS);
            } else handler.sendEmptyMessage(LOGIN_FAILURE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void visiting_details(String result, Handler handler) {
        result = parseServerXML(result);
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(result);
            String message = jsonObject.getString("message");
            if (StringUtils.startsWithIgnoreCase(message, "Success")) {
                handler.sendEmptyMessage(DETAILS_SUCCESS);
            } else handler.sendEmptyMessage(DETAILS_FAILURE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
