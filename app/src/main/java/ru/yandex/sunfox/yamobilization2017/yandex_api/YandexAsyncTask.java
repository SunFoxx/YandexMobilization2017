package ru.yandex.sunfox.yamobilization2017.yandex_api;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * AsyncTask to establish connection with YandexAPI, send POST request and return JSON string as a respond
 */
public class YandexAsyncTask extends AsyncTask<Void,Void,String> {
    private String apiKey;
    private Request request;
    private String paramsSet;

    /**
     * @param request Request enum to identify a certain request
     * @param apiKey used api key
     * @param params array of parameters, each parameter must match pattern "name=value"
     */
    YandexAsyncTask(Request request, String apiKey, String... params) {
        this.apiKey = apiKey;
        this.request = request;
        fillParamsSet(params);
    }

    /**
     * creating a char sequence of parameters to add them in request URL after an API key
     * @param params array of parameters, each parameter must match pattern "name=value"
     */
    private void fillParamsSet(String... params) {
        StringBuilder builder = new StringBuilder("");
        if (params.length > 0) {
            builder.append("&");
            for (int i = 0; i < params.length; i++) {
                builder.append(params[i]);
                if (i != params.length - 1) builder.append("&");
            }
        }
        paramsSet = builder.toString();
    }

    @Override
    protected String doInBackground(Void... params) {
        String baseUrl = "https://translate.yandex.net/api/v1.5/tr.json/";
        if (request.equals(Request.LANGS_DICTONARY) || request.equals(Request.LOOKUP)) {
            baseUrl = "https://dictionary.yandex.net/api/v1/dicservice.json/";
        }
        String urlStr = baseUrl + request.toString() + "?key=" + apiKey + paramsSet;
        String result = "";
        try {
            URL urlObj = new URL(urlStr);
            HttpsURLConnection connection = (HttpsURLConnection) urlObj.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);

            InputStream input = connection.getInputStream();
            StringBuilder jsonBuilder = new StringBuilder();
            int character;
            while ((character = input.read()) != -1) {
                jsonBuilder.append((char) character);
            }

            result = jsonBuilder.toString();
            input.close();
            connection.disconnect();
        } catch (MalformedURLException e) {
            Log.e("HTTPS", "Wrong url formatting, " + e.getMessage());
        } catch (IOException e) {
            Log.e("HTTPS", "IOException, " + e.getMessage());
        }
        return result;
    }

}