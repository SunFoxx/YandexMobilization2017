package ru.yandex.sunfox.yamobilization2017.yandex_api;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static ru.yandex.sunfox.yamobilization2017.MainActivity.encode;

public class Translation implements Serializable {
    private String defText = "";
    private String trText = "";
    private String trPos = "";
    private List<String> synonyms = new ArrayList<>();;
    //private List<Example> examples;
    private String direction;
    /**
     * Global list of translations history
     */
    private static List<Translation> translations = new ArrayList<>();
    private static List<Translation> favorites = new ArrayList<>();

    /**
     * Constructor with initialization from JSON respond from dictionary service
     * @param JSON
     * @param direction
     */
    public Translation(String JSON, String direction) {
        //examples = new ArrayList<>();
        this.direction = direction;
        init(JSON);
        addTranslationToHistory(this);
    }

    /**
     * Constructor with manual initialization for non-dictionary translations
     * @param defText
     * @param trText
     * @param direction
     */
    public Translation(String defText, String trText, String direction) {
        this.defText = defText;
        this.trText = trText;
        this.direction = direction;
    }

    /**
     * Parsing JSON into local fields
     */
    private void init(String JSON) {
        try {
            JSONObject jsonObject = new JSONObject(JSON);
            JSONArray def = jsonObject.getJSONArray("def");
            if (def.length() > 0) {
                JSONObject defObject = def.getJSONObject(0);
                defText = encode(defObject.getString("text"));
                //defPos = defObject.getString("pos");
                JSONArray tr = defObject.getJSONArray("tr");
                JSONObject trObject = tr.getJSONObject(0);
                trText = encode(trObject.getString("text"));
                trPos = encode(trObject.getString("pos"));
                if (trObject.has("syn")) {
                    JSONArray syn = trObject.getJSONArray("syn");
                    for (int i = 0; i < syn.length(); i++) {
                        JSONObject synText = syn.getJSONObject(i);
                        synonyms.add(encode(synText.getString("text")));
                    }
                }
                /* Examples parsing. Not needed in test task
                if (trObject.has("ex")) {
                    JSONArray ex = trObject.getJSONArray("ex");
                    for (int i = 0; i < ex.length(); i++) {
                        JSONObject exampleObj = ex.getJSONObject(0);
                        String text = exampleObj.getString("text");
                        JSONArray exTr = exampleObj.getJSONArray("tr");
                        JSONObject exTrObj = exTr.getJSONObject(0);
                        Log.d("JSON", (i + 1) + " example successfully parsed");
                        String translation = exTrObj.getString("text");
                        Example example = new Example(text, translation);
                        examples.add(example);
                    }
                }*/
            }
        } catch (JSONException e) {
            Log.e("JSON", "Error during parsing dictionary lookup, " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Log.e("String", "Encoding error");
        }
    }

    public static void addTranslationToHistory(Translation t) {
        if (t.defText.isEmpty() || t.trText.isEmpty()) return;
        //we don't want to add to history translations with same language on both sides
        String direction = t.getDirection();
        String[] langs = direction.split("-");
        if (langs.length == 2) {
            if (!(langs[0].equals(langs[1]))) {
                //making sure we won't put a duplicate in history..
                if (!translations.contains(t)) {
                    translations.add(0, t);
                    //we don't need such big history size, so let's say, 50 items is enough
                    if (translations.size() > 50) {
                        translations.remove(0);
                    }
                } else {
                    //..so if we met duplicated one, we are moving it to the top of the list
                    translations.remove(t);
                    translations.add(0, t);
                }
            }
        }
    }

    public static void addTranslationToFavorites(Translation t) {
        if (!favorites.contains(t)) {
            favorites.add(0, t);
        }
    }

    public static void removeTranslationFromFavorites(Translation t) {
        if (favorites.contains(t)) {
            favorites.remove(t);
        }
    }

    public static List<Translation> getTranslationsHistory() {
        //return Collections.unmodifiableList(translations);
        return translations;
    }

    public static List<Translation> getTranslationsFavorites() {
        return favorites;
    }

    public String getTranslatedPos() {
        return trPos;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public String getDefaultText() {
        return defText;
    }

    public String getDirection() {
        return direction;
    }

    public String getTranslatedText() {
        return trText;
    }

    public static void setTranslationsAndFavorites(List<Translation> newTranslations, List<Translation> newFavorites) {
        translations = newTranslations;
        favorites = newFavorites;
    }

    /*public List<Example> getExamples() {
        return examples;
    }*/

    /*private static class Example {
        private String text;
        private String tr;

        Example(String text, String tr) {
            this.text = text;
            this.tr = tr;
        }

        public String getText() {
            return text;
        }

        public String getTranslation() {
            return tr;
        }
    }*/

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Translation that = (Translation) o;

        if (!defText.equals(that.defText)) return false;
        if (!trText.equals(that.trText)) return false;
        return direction.equals(that.direction);
    }

    @Override
    public int hashCode() {
        int result = sumOfBytes(trText.getBytes());
        result = 11 * result + sumOfBytes(direction.getBytes());
        return result;
    }

    /**
     * Way of calculating hashcode
     * @param bytes String, converted to bytes array
     * @return sum, casted to int from long
     */
    private int sumOfBytes(byte[] bytes) {
        long sum = 0;
        for (int i = 0; i < bytes.length; i++) {
            sum += bytes[i];
        }
        return (int) sum;
    }

    @Override
    public String toString() {
        return "Translation{" +
                "defText='" + defText + '\'' +
                ", trText='" + trText + '\'' +
                ", direction='" + direction + '\'' +
                '}';
    }
}
