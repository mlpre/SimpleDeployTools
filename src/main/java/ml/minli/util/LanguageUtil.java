package ml.minli.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageUtil {

    public static final ResourceBundle resourceBundle;

    static {
        if (Locale.CHINA.equals(Locale.getDefault())) {
            resourceBundle = ResourceBundle.getBundle("language.app", Locale.CHINA);
        } else {
            resourceBundle = ResourceBundle.getBundle("language.app", Locale.ENGLISH);
        }
    }

    public static String getValue(String key) {
        return resourceBundle.getString(key);
    }

}
