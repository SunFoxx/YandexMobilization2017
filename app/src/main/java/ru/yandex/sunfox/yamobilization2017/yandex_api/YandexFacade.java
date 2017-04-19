package ru.yandex.sunfox.yamobilization2017.yandex_api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class YandexFacade {

    private static final String apiKeyTranslate = "trnsl.1.1.20170315T151824Z.59d4a65beb04b807.cc93c48b001f65d6f94c02d4336e65fbd88570fd";
    private static final String apiKeyDictonary = "dict.1.1.20170321T090050Z.0577e48092789121.00e0f742040e87146693158c4c148837b5a2ccc6";
    public static String appLang;
    private Map<String, String> langMap;

    public YandexFacade() {
        langMap = new HashMap<>();
        getAppLanguageAndFillLangMap();
    }

    public Map<String, String> getLangMap() {
        return langMap;
    }

    /**
     * Filling langMap with pairs of Language code and it's translation to the current device language
     * Also setting current app language to appropriate by YandexAPI for further requests
     */
    private void getAppLanguageAndFillLangMap() {
        //Getting primary language code list (actually, a map, because of AsyncTask realization). Basic language - en
        Map<String, String> temporaryDefaultMap = new HashMap<>();
        YandexAsyncTask taskDefault = new YandexAsyncTask(Request.LANGS, apiKeyTranslate, "ui=en");
        taskDefault.execute();
        try {
            temporaryDefaultMap = parseLangJSONRespond(taskDefault.get());
            String deviceLang = Locale.getDefault().getLanguage().substring(0, 2);
            //If YandexAPI not supporting current device language, we must use default langMap with english codes and translations
            if (!temporaryDefaultMap.containsKey(deviceLang)) {
                langMap = temporaryDefaultMap;
                appLang = "en";
            } else {
                YandexAsyncTask taskLegacy = new YandexAsyncTask(Request.LANGS, apiKeyTranslate, "ui=" + deviceLang);
                taskLegacy.execute();
                langMap = parseLangJSONRespond(taskLegacy.get());
                appLang = deviceLang;
            }
        } catch (Exception e) {
            Log.e("YandexAPI", "Can't get language list from server");
        }
    }

    /**
     * Building a map with pairs of language code and it's full name, from a JSON respond
     *
     * @param JSON respond from YandexAPI which we need to parse
     * @return Map, filled with langs
     */
    private Map<String, String> parseLangJSONRespond(String JSON) {
        Map<String, String> langMap = new HashMap<>();
        try {
            JSONObject json = new JSONObject(JSON);
            JSONObject jLangs = json.getJSONObject("langs");
            JSONArray jNames = jLangs.names();
            for (int i = 0; i < jNames.length(); i++) {
                String name = jNames.get(i).toString();
                langMap.put(name, jLangs.get(name).toString());
            }
        } catch (JSONException e) {
            Log.e("JSON", "Can't parse server respond");
        }
        return langMap;
    }

    /**
     * Translate request with JSON respond
     *
     * @param text      text to translate
     * @param direction direction of translate
     * @return parsed JSON respond with translation
     */
    public String translate(String text, String direction) {
        String result = "";
        if (text.isEmpty()) return "";
        String formattedText = text.replaceAll(" ", "%20");
        YandexAsyncTask task = new YandexAsyncTask(Request.TRANSLATE, apiKeyTranslate, "lang=" + direction, "text=" + formattedText);
        task.execute();
        try {
            String respond = task.get();
            JSONObject jObject = new JSONObject(respond);
            int code = jObject.getInt("code");
            if (code == 200) {
                if (jObject.has("text")) {
                    JSONArray jText = jObject.getJSONArray("text");
                    if (jText.length() > 0)
                        result = jText.get(0).toString();
                }
            } else {
                result = code + "";
            }
        } catch (InterruptedException e) {
            Log.e("YandexAPI", "translate task thread interrupted");
        } catch (ExecutionException e) {
            Log.e("YandexAPI", "can't translate text using translate request");
        } catch (JSONException e) {
            Log.e("YandexAPI", "error during parsing translation respond");
        }
        return result;
    }

    /**
     * Performing lookup request to Dictionary API
     *
     * @param text      text to translate
     * @param direction translate direction
     * @return Translation object containing all parsed data from JSON respond
     */
    public Translation lookup(String text, String direction) {
        if (text.isEmpty()) return null;
        YandexAsyncTask getLangs = new YandexAsyncTask(Request.LANGS_DICTONARY, apiKeyDictonary);
        getLangs.execute();
        boolean isAvailable = false;
        try {
            isAvailable = isDictionaryDirectionAvailable(getLangs.get(), direction);
        } catch (Exception e) {
            Log.e("YandexAPI", "Can't get language list from server");
        }
        if (!isAvailable) {
            return null;
        }

        String ui;
        switch (appLang) {
            case "ru":
            case "uk":
            case "tr":
                ui = "ui=" + appLang;
                break;
            default:
                ui = "ui=en";
        }
        YandexAsyncTask lookup = new YandexAsyncTask(Request.LOOKUP, apiKeyDictonary, ui, "lang=" + direction, "text=" + text);
        lookup.execute();
        try {
            String respond = lookup.get();
            Translation translation = new Translation(respond, direction);
            if (translation.getDefaultText().isEmpty() && translation.getTranslatedText().isEmpty())
                return null;
            return translation;
        } catch (InterruptedException e) {
            Log.e("YandexAPI", "lookup task thread interrupted");
        } catch (ExecutionException e) {
            Log.e("YandexAPI", "can't translate text by using dictionary lookup");
        }
        return null;
    }

    /**
     * Detect request to check if current text matches selected language
     *
     * @param text
     * @param direction
     * @return
     */
    public boolean isCorrectLanguageSelected(String text, String direction) {
        if (text.isEmpty()) return false;
        String fixedText = text.replaceAll(" ", "%20");
        YandexAsyncTask task = new YandexAsyncTask(Request.DETECT, apiKeyTranslate, "text=" + fixedText, "hint=" + direction.substring(0, 2));
        task.execute();
        try {
            String respond = task.get();
            JSONObject jObject = new JSONObject(respond);
            if (jObject.has("lang")) {
                String lang = jObject.getString("lang");
                if (lang.equals(direction.substring(0, 2))) return true;
            }
        } catch (InterruptedException e) {
            Log.e("YandexAPI", "task thread interrupted");
        } catch (ExecutionException e) {
            Log.e("YandexAPI", "can't determine if correct language selected");
        } catch (JSONException e) {
            Log.e("YandexAPI", "error during parsing detect respond");
        }
        return false;
    }

    /**
     * This method is checking if YandexDictionary API supports our direction
     *
     * @param JSON      respond from lang request to dictionary API
     * @param direction needed direction
     * @return true if Yandex API supports that direction
     */
    private boolean isDictionaryDirectionAvailable(String JSON, String direction) {
        try {
            JSONArray array = new JSONArray(JSON);
            for (int i = 0; i < array.length(); i++) {
                String jsonItem = array.getString(i);
                if (direction.equals(jsonItem)) return true;
            }
        } catch (JSONException e) {
            Log.e("YandexAPI", "Can't parse dictionary server respond");
        }
        return false;
    }
}
