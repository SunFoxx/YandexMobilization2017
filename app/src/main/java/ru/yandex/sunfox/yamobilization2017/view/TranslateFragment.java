package ru.yandex.sunfox.yamobilization2017.view;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.yandex.sunfox.yamobilization2017.MainActivity;
import ru.yandex.sunfox.yamobilization2017.R;
import ru.yandex.sunfox.yamobilization2017.yandex_api.Translation;
import ru.yandex.sunfox.yamobilization2017.yandex_api.YandexFacade;

import static ru.yandex.sunfox.yamobilization2017.MainActivity.encode;

public class TranslateFragment extends Fragment {

    private EditText translateField;
    private TextView translation;
    private TextView posText;
    private Spinner spinnerFrom;
    private Spinner spinnerTo;
    private CheckBox favorite;
    private YandexFacade yandexFacade;
    private ListView synonymList;
    private Context context;

    private CompoundButton.OnCheckedChangeListener favoriteListener;

    public TranslateFragment(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.translate_fragment, container, false);
    }

    /**
     * Actions, which performed here, can cause an Exception inside onCreate method, so I just moved them into OnCreated
     *
     * @param savedInstanceState -
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        if (view != null) {
            translateField = (EditText) view.findViewById(R.id.translateField);
            translation = (TextView) view.findViewById(R.id.translatedText);
            posText = (TextView) view.findViewById(R.id.posText);
            spinnerFrom = (Spinner) view.findViewById(R.id.spinnerFrom);
            spinnerTo = (Spinner) view.findViewById(R.id.spinnerTo);
            synonymList = (ListView) view.findViewById(R.id.synonimsList);
            favorite = (CheckBox) view.findViewById(R.id.translate_fragment_favorite_checkbox);

            final HistoryAdapter historyAdapter = HistoryFragment.historyAdapter;

            favoriteListener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Translation current = getCurrentTranslation();
                    if (current != null) {
                        if (isChecked) {
                            Translation.addTranslationToFavorites(current);
                            if (historyAdapter != null) {
                                historyAdapter.notifyDataSetChanged();
                            }
                        } else {
                            Translation.removeTranslationFromFavorites(current);
                            if (historyAdapter != null) {
                                historyAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            };

            if (MainActivity.isConnected) {
                //v Main translation procedure is placed here
                translateField.addTextChangedListener(new TextWatcher() {

                    Handler handler = new Handler(Looper.getMainLooper()); //UI Thread
                    Runnable performer;
                    final long DELAY = 700;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        final String from = s.toString().trim();
                        handler.removeCallbacks(performer);
                        performer = new Runnable() {
                            @Override
                            public void run() {
                                performTranslate(from);
                            }
                        };
                        handler.postDelayed(performer, DELAY);
                    }
                });        //this one will translate text during typing, with a small delay
                translateField.addTextChangedListener(new TextWatcher() {

                    Handler handler = new Handler(Looper.getMainLooper()); //UI Thread
                    Runnable performer;
                    final long DELAY = 700;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        final String from = s.toString().trim();
                        handler.removeCallbacks(performer);
                        performer = new Runnable() {
                            @Override
                            public void run() {
                                performTranslate(from);
                            }
                        };
                        handler.postDelayed(performer, DELAY);

                    }
                });        //this one will translate text during typing, with a small delay
                translateField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {             //this one will close keyboard on touch outside EditText box
                        if (!hasFocus)
                            hideKeyboard(v);
                    }
                });
                yandexFacade = new YandexFacade();
                fillSpinners();
            } else {
                translation.setText("No internet connectivity. Check your connection and restart app");
            }

        }
    }

    public void clear() {
        if (translateField != null) {
            synonymList.setAdapter(null);
            posText.setText("");
            translateField.setText("");
            if (!MainActivity.isConnected)
                translation.setText("No internet connectivity. Check your connection and restart app");
            else
                translation.setText("");
            favorite.setVisibility(View.INVISIBLE);
            favorite.setOnCheckedChangeListener(null);
            favorite.setChecked(false);
        }
    }

    /**
     * Simply translate text argument, and place the result directly into a TextViews
     * Including refreshing of views, error codes tests and dictionary lookup request
     *
     * @param text "from" translation text
     */
    private void performTranslate(String text) {
        synonymList.setAdapter(null);
        posText.setText("");
        translation.setText("");
        favorite.setVisibility(View.INVISIBLE);
        favorite.setOnCheckedChangeListener(null);
        favorite.setChecked(false);
        if (MainActivity.isConnectionAvailable() && MainActivity.isConnected) {
            String direction = getDirection();
            if (yandexFacade.isCorrectLanguageSelected(text, direction)) {
                String translated = "";
                try {
                    translated = encode(yandexFacade.translate(text.trim(), direction));
                } catch (UnsupportedEncodingException e) {
                    Log.e("String", "Encoding error");
                }
                //Catching wrong respond codes
                if (translated.length() == 3) {
                    switch (translated) {
                        case "401":
                        case "402":
                            Log.e("YandexAPI", "Translation, code " + translated + ", wrong API key");
                            return;
                        case "404":
                            Log.e("YandexAPI", "Translation, code " + translated + ", Exceeded the max text size");
                            return;
                        case "422":
                            Log.e("YandexAPI", "Translation, code " + translated + ", Text cannot be translated");
                            return;
                        case "501":
                            Log.e("YandexAPI", "Translation, code " + translated + ", Selected translation direction is not supported");
                            return;
                        default:
                            break;
                    }
                }
                translation.setText(translated);
                Translation current = getCurrentTranslation();
                if (current != null) {
                    if (Translation.getTranslationsFavorites().contains(current))
                        favorite.setChecked(true);
                }
                favorite.setVisibility(View.VISIBLE);
                favorite.setOnCheckedChangeListener(favoriteListener);
                HistoryAdapter historyAdapter = HistoryFragment.historyAdapter;

                //First we need to make sure that our translation is implemented in dictionary.
                Translation tr = yandexFacade.lookup(text, direction);
                if (tr != null) {
                    Map<String, String> synonymsMap = new HashMap<>();
                    for (int i = 0; i < tr.getSynonyms().size(); i++) {
                        synonymsMap.put((i + 1) + "", tr.getSynonyms().get(i));
                    }

                    SynonymAdapter adapter = new SynonymAdapter(context, synonymsMap);
                    synonymList.setAdapter(adapter);
                    posText.setText(tr.getTranslatedPos());
                    if (historyAdapter != null) historyAdapter.notifyDataSetChanged();
                }
                //..if not, we need to create new Translation object manually so it will be able to get into history
                else {
                    Translation.addTranslationToHistory(new Translation(text, translated, direction));
                    if (historyAdapter != null) historyAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * getting "aa-bb" pattern for a translation request. Result based on selected spinner items
     *
     * @return direction string for a request
     */
    private String getDirection() {
        String from = spinnerFrom.getSelectedItem().toString();
        String to = spinnerTo.getSelectedItem().toString();
        for (String key : yandexFacade.getLangMap().keySet()) {
            try {
                if (from.equals(encode(yandexFacade.getLangMap().get(key)))) {
                    from = key;
                }
                if (to.equals(encode(yandexFacade.getLangMap().get(key)))) {
                    to = key;
                }
            } catch (UnsupportedEncodingException e) {
                Log.e("String", "Encoding error");
            }
        }
        String result = from + "-" + to;
        return result;
    }

    /**
     * Hiding keyboard
     *
     * @param view focused view
     */
    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * filling spinners with appropriate and supported languages
     */
    private void fillSpinners() {
        //getting List of available languages
        List<String> selection = new ArrayList<>();
        for (String key : yandexFacade.getLangMap().keySet()) {
            try {
                selection.add(encode(yandexFacade.getLangMap().get(key)));
            } catch (UnsupportedEncodingException e) {
                Log.e("String", "Encoding error");
            }
        }
        Collections.sort(selection);

        //filling spinners with values
        ArrayAdapter<String> spinnerFromAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, selection);
        spinnerFromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrom.setAdapter(spinnerFromAdapter);
        ArrayAdapter<String> spinnerToAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, selection);
        spinnerToAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTo.setAdapter(spinnerToAdapter);

        //setting selection of "from" spinner to device language and "to" spinner to default value ("en")
        String device = YandexFacade.appLang; // "la" format lang name
        String defaultLang = "";
        String deviceFull = "";
        try {
            defaultLang = encode(yandexFacade.getLangMap().get("en"));
            deviceFull = encode(yandexFacade.getLangMap().get(device)); //"language" format lang name
        } catch (UnsupportedEncodingException e) {
            Log.e("String", "Encoding error");
        }
        for (String lang : selection) {
            if (deviceFull.equals(lang)) {
                spinnerFrom.setSelection(selection.indexOf(lang));
            }
            if (defaultLang.equals(lang)) {
                spinnerTo.setSelection(selection.indexOf(lang));
            }

        }

        //Selection of a new language from a spinner must perform translate from\to a new language
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String text = translateField.getText().toString();
                if (!text.isEmpty())
                    performTranslate(text);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        spinnerFrom.setOnItemSelectedListener(listener);
        spinnerTo.setOnItemSelectedListener(listener);
    }

    /**
     * Getting translation from history (if it exists) by using current views state
     *
     * @return
     */
    @Nullable
    private Translation getCurrentTranslation() {
        String defText = translateField.getText().toString().trim();
        String trText = translation.getText().toString().trim();
        String direction = getDirection();
        if (defText.isEmpty() || trText.isEmpty()) return null;
        Translation current = new Translation(defText, trText, direction);
        if (Translation.getTranslationsHistory().contains(current))
            return Translation.getTranslationsHistory().get(Translation.getTranslationsHistory().indexOf(current));
        return null;
    }

    /**
     * Event handler for an arrow button between language spinners. On click, must switch those languages
     *
     * @param view doing nothing here
     */
    public void onSwitchLangPressed(View view) {
        int temp = spinnerTo.getSelectedItemPosition();
        spinnerTo.setSelection(spinnerFrom.getSelectedItemPosition());
        spinnerFrom.setSelection(temp);
        performTranslate(translateField.getText().toString());
    }
}
