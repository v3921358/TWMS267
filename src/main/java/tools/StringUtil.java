package tools;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class StringUtil {
    /* 16 */ private static final Pattern NaturalNumberPattern = Pattern.compile("^[0-9]+$");

    public static String getLeftPaddedStr(String in, char padchar, int length) {
        /* 29 */
        StringBuilder builder = new StringBuilder(length);
        /* 30 */
        for (int x = (in.getBytes()).length; x < length; x++) {
            /* 31 */
            builder.append(padchar);
        }
        /* 33 */
        builder.append(in);
        /* 34 */
        return builder.toString();
    }

    public static String getRightPaddedStr(int in, char padchar, int length) {
        /* 48 */
        return getRightPaddedStr(String.valueOf(in), padchar, length);
    }

    public static String getRightPaddedStr(long in, char padchar, int length) {
        /* 52 */
        return getRightPaddedStr(String.valueOf(in), padchar, length);
    }

    public static String getRightPaddedStr(String in, char padchar, int length) {
        /* 56 */
        StringBuilder builder = new StringBuilder(in);
        /* 57 */
        for (int x = (in.getBytes()).length; x < length; x++) {
            /* 58 */
            builder.append(padchar);
        }
        /* 60 */
        return builder.toString();
    }

    public static String joinStringFrom(String[] arr, int start) {
        /* 73 */
        return joinStringFrom(arr, start, " ");
    }

    public static String joinStringFrom(String[] arr, int start, String sep) {
        /* 86 */
        StringBuilder builder = new StringBuilder();
        /* 87 */
        for (int i = start; i < arr.length; i++) {
            /* 88 */
            builder.append(arr[i]);
            /* 89 */
            if (i != arr.length - 1) {
                /* 90 */
                builder.append(sep);
            }
        }
        /* 93 */
        return builder.toString();
    }

    public static String makeEnumHumanReadable(String enumName) {
        /* 103 */
        StringBuilder builder = new StringBuilder(enumName.length() + 1);
        /* 104 */
        for (String word : enumName.split("_")) {
            /* 105 */
            if (word.length() <= 2) {
                /* 106 */
                builder.append(word);
            } else {
                /* 108 */
                builder.append(word.charAt(0));
                /* 109 */
                builder.append(word.substring(1).toLowerCase());
            }
            /* 111 */
            builder.append(' ');
        }
        /* 113 */
        return builder.substring(0, enumName.length());
    }

    public static int countCharacters(String str, char chr) {
        /* 129 */
        int ret = 0;
        /* 130 */
        for (int i = 0; i < str.length(); i++) {
            /* 131 */
            if (str.charAt(i) == chr) {
                /* 132 */
                ret++;
            }
        }
        /* 135 */
        return ret;
    }

    public static String getReadableMillis(long startMillis, long endMillis) {
        /* 139 */
        StringBuilder sb = new StringBuilder();
        /* 140 */
        double elapsedSeconds = (endMillis - startMillis) / 1000.0D;
        /* 141 */
        int elapsedSecs = (int) elapsedSeconds % 60;
        /* 142 */
        int elapsedMinutes = (int) (elapsedSeconds / 60.0D);
        /* 143 */
        int elapsedMins = elapsedMinutes % 60;
        /* 144 */
        int elapsedHrs = elapsedMinutes / 60;
        /* 145 */
        int elapsedHours = elapsedHrs % 24;
        /* 146 */
        int elapsedDays = elapsedHrs / 24;
        /* 147 */
        if (elapsedDays > 0) {
            /* 148 */
            boolean mins = (elapsedHours > 0);
            /* 149 */
            sb.append(getLeftPaddedStr(String.valueOf(elapsedDays), '0', 2));
            /* 150 */
            sb.append("天");
            /* 151 */
            if (mins) {
                /* 152 */
                boolean secs = (elapsedMins > 0);
                /* 153 */
                sb.append(getLeftPaddedStr(String.valueOf(elapsedHours), '0', 2));
                /* 154 */
                sb.append("时");
                /* 155 */
                if (secs) {
                    /* 156 */
                    boolean millis = (elapsedSecs > 0);
                    /* 157 */
                    sb.append(getLeftPaddedStr(String.valueOf(elapsedMins), '0', 2));
                    /* 158 */
                    sb.append("分");
                    /* 159 */
                    if (millis) {
                        /* 160 */
                        sb.append(getLeftPaddedStr(String.valueOf(elapsedSecs), '0', 2));
                        /* 161 */
                        sb.append("秒");
                    }
                }
            }
            /* 165 */
        } else if (elapsedHours > 0) {
            /* 166 */
            boolean mins = (elapsedMins > 0);
            /* 167 */
            sb.append(getLeftPaddedStr(String.valueOf(elapsedHours), '0', 2));
            /* 168 */
            sb.append("时");
            /* 169 */
            if (mins) {
                /* 170 */
                boolean secs = (elapsedSecs > 0);
                /* 171 */
                sb.append(getLeftPaddedStr(String.valueOf(elapsedMins), '0', 2));
                /* 172 */
                sb.append("分");
                /* 173 */
                if (secs) {
                    /* 174 */
                    sb.append(getLeftPaddedStr(String.valueOf(elapsedSecs), '0', 2));
                    /* 175 */
                    sb.append("秒");
                }
            }
            /* 178 */
        } else if (elapsedMinutes > 0) {
            /* 179 */
            boolean secs = (elapsedSecs > 0);
            /* 180 */
            sb.append(getLeftPaddedStr(String.valueOf(elapsedMins), '0', 2));
            /* 181 */
            sb.append("分");
            /* 182 */
            if (secs) {
                /* 183 */
                sb.append(getLeftPaddedStr(String.valueOf(elapsedSecs), '0', 2));
                /* 184 */
                sb.append("秒");
            }
            /* 186 */
        } else if (elapsedSeconds > 0.0D) {
            /* 187 */
            sb.append(getLeftPaddedStr(String.valueOf(elapsedSecs), '0', 2));
            /* 188 */
            sb.append("秒");
        } else {
            /* 190 */
            sb.append("None.");
        }
        /* 192 */
        return sb.toString();
    }

    public static int[] StringtoInt(String str, String separator) {
        /* 196 */
        StringTokenizer strTokens = new StringTokenizer(str, separator);
        /* 197 */
        int[] strArray = new int[strTokens.countTokens()];
        /* 198 */
        int i = 0;
        /* 199 */
        while (strTokens.hasMoreTokens()) {
            /* 200 */
            strArray[i] = Integer.parseInt(strTokens.nextToken().trim());
            /* 201 */
            i++;
        }
        /* 203 */
        return strArray;
    }

    public static boolean[] StringtoBoolean(String str, String separator) {
        /* 207 */
        StringTokenizer strTokens = new StringTokenizer(str, separator);
        /* 208 */
        boolean[] strArray = new boolean[strTokens.countTokens()];
        /* 209 */
        int i = 0;
        /* 210 */
        while (strTokens.hasMoreTokens()) {
            /* 211 */
            strArray[i] = Boolean.parseBoolean(strTokens.nextToken().trim());
            /* 212 */
            i++;
        }
        /* 214 */
        return strArray;
    }

    public static boolean isNumber(String str) {
        /* 218 */
        return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
    }

    public static boolean isNaturalNumber(String str) {
        /* 222 */
        return NaturalNumberPattern.matcher(str).matches();
    }

    public static String codeString(String fileName) throws Exception {
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));

        int p = (bin.read() << 8) + bin.read();
        String code = "GBK";
        switch (p) {
            case 61371:
                code = "UTF-8";

                return code;
            case 65534:
                code = "Unicode";
                return code;
            case 65279:
                code = "UTF-16BE";
                return code;
            case 23669:
                code = "ANSI|ASCII";
                return code;
        }

        return code;
    }

    /**
     * 判断字符串是否为空字符串
     *
     * @param str 要判断的字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        if (str == null) return true;
        return str.replaceAll("\\s", "").equals(CommonConstants.EMPTY_STR);
    }
}
