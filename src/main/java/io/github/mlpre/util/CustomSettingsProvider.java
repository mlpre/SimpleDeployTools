package io.github.mlpre.util;

import com.jediterm.terminal.ui.UIUtil;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;

import java.awt.*;
import java.util.Locale;

public class CustomSettingsProvider extends DefaultSettingsProvider {

    @Override
    public float getTerminalFontSize() {
        return 16.0F;
    }

    @Override
    public Font getTerminalFont() {
        String fontName;
        if (UIUtil.isWindows) {
            if ((Locale.CHINA).equals(Locale.getDefault())) {
                fontName = "SimHei";
            } else {
                fontName = "Consolas";
            }
        } else if (UIUtil.isMac) {
            fontName = "Menlo";
        } else {
            fontName = "Monospaced";
        }
        return new Font(fontName, Font.PLAIN, (int) getTerminalFontSize());
    }

}
