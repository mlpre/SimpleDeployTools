package io.github.mlpre.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageUtil {

    public static final ResourceBundle resourceBundle;

    static {
        ResourceBundle defaultResourceBundle = ResourceBundle.getBundle("language.app", Locale.getDefault());
        if (defaultResourceBundle != null) {
            resourceBundle = defaultResourceBundle;
        } else {
            resourceBundle = ResourceBundle.getBundle("language.app", Locale.ENGLISH);
        }
    }

    public static String getValue(String key) {
        return resourceBundle.getString(key);
    }

}
