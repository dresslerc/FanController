package io.spark.core.cloud;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import carstendressler.com.util.HTTPHeader;
import carstendressler.com.util.SimpleHttpClient;

/**
 * Created by Carsten on 7/27/2014.
 */
public class Client {

    public String getAccessToken(Context context, String Username, String Password) {

        Map<String, String> jsonMap = new HashMap<String, String>();

        jsonMap.put("username", Username);
        jsonMap.put("password", Password);

        String json = new Gson().toJson(jsonMap);

        List<HTTPHeader> headers = new ArrayList<HTTPHeader>();
        headers.add(new HTTPHeader("Content-Type", "application/json"));

        String respBuffer = null;
        try {
            respBuffer = SimpleHttpClient.httpPost("https://www.spark.io/sign-in", json, headers);
        } catch (IOException e1) {
        }
        AuthenicationResponse authResp = new Gson().fromJson(respBuffer, AuthenicationResponse.class);

        headers.add(new HTTPHeader("Authorization", "Bearer " + authResp.auth_token.token));
        try {
            respBuffer = SimpleHttpClient.httpGet("https://www.spark.io/access-token", headers);
        } catch (IOException e) {
        }
        LinkedTreeMap accessTokenResp = new Gson().fromJson(respBuffer, LinkedTreeMap.class);

        return accessTokenResp.get("access_token").toString();
    }

    public List<Device> GetDevices(Context context, String AccessToken) {

        String respBuffer = null;

        try {
            respBuffer = SimpleHttpClient.httpGet("https://api.spark.io/v1/devices?access_token=" + AccessToken, null);
            List<Device> devices = new Gson().fromJson(respBuffer, new TypeToken<List<Device>>() {
            }.getType());
            return devices;

        } catch (IOException e) {
        }

        return null;

    }
}
