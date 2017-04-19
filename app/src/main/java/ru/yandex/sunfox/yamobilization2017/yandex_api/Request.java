package ru.yandex.sunfox.yamobilization2017.yandex_api;

public enum Request {
    LANGS, LANGS_DICTONARY, DETECT, TRANSLATE, LOOKUP;

    @Override
    public String toString() {
        switch (this) {
            case LANGS_DICTONARY:
            case LANGS:
                return "getLangs";
            case DETECT:
                return "detect";
            case TRANSLATE:
                return "translate";
            case LOOKUP:
                return "lookup";
            default:
                return "null";
        }
    }
}
