package tools.json;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class JSONPointer {
    private static final String ENCODING = "utf-8";
    private final List<String> refTokens;

    public static class Builder {
        /* 66 */ private final List<String> refTokens = new ArrayList<>();

        public JSONPointer build() {
            /* 73 */
            return new JSONPointer(this.refTokens);
        }

        public Builder append(String token) {
            /* 89 */
            if (token == null) {
                /* 90 */
                throw new NullPointerException("token cannot be null");
            }
            /* 92 */
            this.refTokens.add(token);
            /* 93 */
            return this;
        }

        public Builder append(int arrayIndex) {
            /* 104 */
            this.refTokens.add(String.valueOf(arrayIndex));
            /* 105 */
            return this;
        }
    }

    public static Builder builder() {
        /* 125 */
        return new Builder();
    }

    public JSONPointer(String pointer) {
        String refs;
        /* 140 */
        if (pointer == null) {
            /* 141 */
            throw new NullPointerException("pointer cannot be null");
        }
        /* 143 */
        if (pointer.isEmpty() || pointer.equals("#")) {
            /* 144 */
            this.refTokens = Collections.emptyList();

            return;
        }
        /* 148 */
        if (pointer.startsWith("#/")) {
            /* 149 */
            refs = pointer.substring(2);
            try {
                /* 151 */
                refs = URLDecoder.decode(refs, "utf-8");
                /* 152 */
            } catch (UnsupportedEncodingException e) {
                /* 153 */
                throw new RuntimeException(e);
            }
            /* 155 */
        } else if (pointer.startsWith("/")) {
            /* 156 */
            refs = pointer.substring(1);
        } else {
            /* 158 */
            throw new IllegalArgumentException("a JSON pointer should start with '/' or '#/'");
        }
        /* 160 */
        this.refTokens = new ArrayList<>();
        /* 161 */
        int slashIdx = -1;
        /* 162 */
        int prevSlashIdx = 0;
        do {
            /* 164 */
            prevSlashIdx = slashIdx + 1;
            /* 165 */
            slashIdx = refs.indexOf('/', prevSlashIdx);
            /* 166 */
            if (prevSlashIdx == slashIdx || prevSlashIdx == refs.length()) {

                /* 169 */
                this.refTokens.add("");
                /* 170 */
            } else if (slashIdx >= 0) {
                /* 171 */
                String token = refs.substring(prevSlashIdx, slashIdx);
                /* 172 */
                this.refTokens.add(unescape(token));
            } else {

                /* 175 */
                String token = refs.substring(prevSlashIdx);
                /* 176 */
                this.refTokens.add(unescape(token));
            }
            /* 178 */
        } while (slashIdx >= 0);
    }

    public JSONPointer(List<String> refTokens) {
        /* 186 */
        this.refTokens = new ArrayList<>(refTokens);
    }

    private String unescape(String token) {
        /* 190 */
        return token.replace("~1", "/").replace("~0", "~")
                /* 191 */.replace("\\\"", "\"")
                /* 192 */.replace("\\\\", "\\");
    }

    public Object queryFrom(Object document) throws JSONPointerException {
        /* 206 */
        if (this.refTokens.isEmpty()) {
            /* 207 */
            return document;
        }
        /* 209 */
        Object current = document;
        /* 210 */
        for (String token : this.refTokens) {
            /* 211 */
            if (current instanceof JSONObject) {
                /* 212 */
                current = ((JSONObject) current).opt(unescape(token));
                continue;
                /* 213 */
            }
            if (current instanceof JSONArray) {
                /* 214 */
                current = readByIndexToken(current, token);
                continue;
            }
            /* 216 */
            throw new JSONPointerException(
                    String.format("value [%s] is not an array or object therefore its key %s cannot be resolved",
                            new Object[]{current, token}));
        }

        /* 221 */
        return current;
    }

    private Object readByIndexToken(Object current, String indexToken) throws JSONPointerException {
        try {
            /* 234 */
            int index = Integer.parseInt(indexToken);
            /* 235 */
            JSONArray currentArr = (JSONArray) current;
            /* 236 */
            if (index >= currentArr.length()) {
                /* 237 */
                throw new JSONPointerException(String.format(
                        "index %d is out of bounds - the array has %d elements", new Object[]{Integer.valueOf(index),
                                /* 238 */ Integer.valueOf(currentArr.length())}));
            }
            try {
                /* 241 */
                return currentArr.get(index);
                /* 242 */
            } catch (JSONException e) {
                /* 243 */
                throw new JSONPointerException("Error reading value at index position " + index, e);
            }
            /* 245 */
        } catch (NumberFormatException e) {
            /* 246 */
            throw new JSONPointerException(
                    String.format("%s is not an array index", new Object[]{indexToken}), e);
        }
    }

    public String toString() {
        /* 256 */
        StringBuilder rval = new StringBuilder("");
        /* 257 */
        for (String token : this.refTokens) {
            /* 258 */
            rval.append('/').append(escape(token));
        }
        /* 260 */
        return rval.toString();
    }

    private String escape(String token) {
        /* 273 */
        return token.replace("~", "~0")
                /* 274 */.replace("/", "~1")
                /* 275 */.replace("\\", "\\\\")
                /* 276 */.replace("\"", "\\\"");
    }

    public String toURIFragment() {
        try {
            /* 285 */
            StringBuilder rval = new StringBuilder("#");
            /* 286 */
            for (String token : this.refTokens) {
                /* 287 */
                rval.append('/').append(URLEncoder.encode(token, "utf-8"));
            }
            /* 289 */
            return rval.toString();
            /* 290 */
        } catch (UnsupportedEncodingException e) {
            /* 291 */
            throw new RuntimeException(e);
        }
    }
}
