package tools;

import java.util.regex.Pattern;

public class StringTool {
    public static int parseInt(String s) {
        if (s != null && !s.isEmpty() && Pattern.compile("[0-9]*").matcher(s).matches()) {
            return Integer.parseInt(s);
        }
        return 0;
    }
}
