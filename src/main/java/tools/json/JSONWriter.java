package tools.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

final class JSONWriter {
    private static final int maxdepth = 200;
    private boolean comma;
    protected char mode;
    private final JSONObject[] stack;
    private int top;
    protected Appendable writer;

    public JSONWriter(Appendable w) {
        /* 100 */
        this.comma = false;
        /* 101 */
        this.mode = 'i';
        /* 102 */
        this.stack = new JSONObject[200];
        /* 103 */
        this.top = 0;
        /* 104 */
        this.writer = w;
    }

    private JSONWriter append(String string) throws JSONException {
        /* 115 */
        if (string == null) {
            /* 116 */
            throw new JSONException("Null pointer");
        }
        /* 118 */
        if (this.mode == 'o' || this.mode == 'a') {
            try {
                /* 120 */
                if (this.comma && this.mode == 'a') {
                    /* 121 */
                    this.writer.append(',');
                }
                /* 123 */
                this.writer.append(string);
                /* 124 */
            } catch (IOException e) {

                /* 128 */
                throw new JSONException(e);
            }
            /* 130 */
            if (this.mode == 'o') {
                /* 131 */
                this.mode = 'k';
            }
            /* 133 */
            this.comma = true;
            /* 134 */
            return this;
        }
        /* 136 */
        throw new JSONException("Value out of sequence.");
    }

    public JSONWriter array() throws JSONException {
        /* 150 */
        if (this.mode == 'i' || this.mode == 'o' || this.mode == 'a') {
            /* 151 */
            push(null);
            /* 152 */
            append("[");
            /* 153 */
            this.comma = false;
            /* 154 */
            return this;
        }
        /* 156 */
        throw new JSONException("Misplaced array.");
    }

    private JSONWriter end(char m, char c) throws JSONException {
        /* 168 */
        if (this.mode != m) {
            /* 169 */
            throw new JSONException((m == 'a') ? "Misplaced endArray." : "Misplaced endObject.");
        }

        /* 173 */
        pop(m);
        try {
            /* 175 */
            this.writer.append(c);
            /* 176 */
        } catch (IOException e) {

            /* 180 */
            throw new JSONException(e);
        }
        /* 182 */
        this.comma = true;
        /* 183 */
        return this;
    }

    public JSONWriter endArray() throws JSONException {
        /* 194 */
        return end('a', ']');
    }

    public JSONWriter endObject() throws JSONException {
        /* 205 */
        return end('k', '}');
    }

    public JSONWriter key(String string) throws JSONException {
        /* 218 */
        if (string == null) {
            /* 219 */
            throw new JSONException("Null key.");
        }
        /* 221 */
        if (this.mode == 'k') {
            try {
                /* 223 */
                JSONObject topObject = this.stack[this.top - 1];

                /* 225 */
                if (topObject.has(string)) {
                    /* 226 */
                    throw new JSONException("Duplicate key \"" + string + "\"");
                }
                /* 228 */
                topObject.put(string, true);
                /* 229 */
                if (this.comma) {
                    /* 230 */
                    this.writer.append(',');
                }
                /* 232 */
                this.writer.append(JSONObject.quote(string));
                /* 233 */
                this.writer.append(':');
                /* 234 */
                this.comma = false;
                /* 235 */
                this.mode = 'o';
                /* 236 */
                return this;
                /* 237 */
            } catch (IOException e) {

                /* 241 */
                throw new JSONException(e);
            }
        }
        /* 244 */
        throw new JSONException("Misplaced key.");
    }

    public JSONWriter object() throws JSONException {
        /* 259 */
        if (this.mode == 'i') {
            /* 260 */
            this.mode = 'o';
        }
        /* 262 */
        if (this.mode == 'o' || this.mode == 'a') {
            /* 263 */
            append("{");
            /* 264 */
            push(new JSONObject());
            /* 265 */
            this.comma = false;
            /* 266 */
            return this;
        }
        /* 268 */
        throw new JSONException("Misplaced object.");
    }

    private void pop(char c) throws JSONException {
        /* 280 */
        if (this.top <= 0) {
            /* 281 */
            throw new JSONException("Nesting error.");
        }
        /* 283 */
        char m = (this.stack[this.top - 1] == null) ? 'a' : 'k';
        /* 284 */
        if (m != c) {
            /* 285 */
            throw new JSONException("Nesting error.");
        }
        /* 287 */
        this.top--;
        /* 288 */
        this.mode = (this.top == 0) ? 'd' : ((this.stack[this.top - 1] == null) ? 'a' : 'k');
    }

    private void push(JSONObject jo) throws JSONException {
        /* 302 */
        if (this.top >= 200) {
            /* 303 */
            throw new JSONException("Nesting too deep.");
        }
        /* 305 */
        this.stack[this.top] = jo;
        /* 306 */
        this.mode = (jo == null) ? 'a' : 'k';
        /* 307 */
        this.top++;
    }

    public static String valueToString(Object value) throws JSONException {
        /* 333 */
        if (value == null || value.equals(null)) {
            /* 334 */
            return "null";
        }
        /* 336 */
        if (value instanceof JSONString) {
            Object object;
            try {
                /* 339 */
                object = ((JSONString) value).toJSONString();
                /* 340 */
            } catch (Exception e) {
                /* 341 */
                throw new JSONException(e);
            }
            /* 343 */
            if (object instanceof String) {
                /* 344 */
                return (String) object;
            }
            /* 346 */
            throw new JSONException("Bad value from toJSONString: " + object);
        }
        /* 348 */
        if (value instanceof Number) {

            /* 350 */
            String numberAsString = JSONObject.numberToString((Number) value);

            try {
                /* 354 */
                BigDecimal unused = new BigDecimal(numberAsString);

                /* 356 */
                return numberAsString;
                /* 357 */
            } catch (NumberFormatException ex) {

                /* 360 */
                return JSONObject.quote(numberAsString);
            }
        }
        /* 363 */
        if (value instanceof Boolean || value instanceof JSONObject || value instanceof JSONArray) {
            /* 365 */
            return value.toString();
        }
        /* 367 */
        if (value instanceof Map) {
            /* 368 */
            Map<?, ?> map = (Map<?, ?>) value;
            /* 369 */
            return (new JSONObject(map)).toString();
        }
        /* 371 */
        if (value instanceof Collection) {
            /* 372 */
            Collection<?> coll = (Collection) value;
            /* 373 */
            return (new JSONArray(coll)).toString();
        }
        /* 375 */
        if (value.getClass().isArray()) {
            /* 376 */
            return (new JSONArray(value)).toString();
        }
        /* 378 */
        if (value instanceof Enum) {
            /* 379 */
            return JSONObject.quote(((Enum) value).name());
        }
        /* 381 */
        return JSONObject.quote(value.toString());
    }

    public JSONWriter value(boolean b) throws JSONException {
        /* 393 */
        return append(b ? "true" : "false");
    }

    public JSONWriter value(double d) throws JSONException {
        /* 404 */
        return value(Double.valueOf(d));
    }

    public JSONWriter value(long l) throws JSONException {
        /* 415 */
        return append(Long.toString(l));
    }

    public JSONWriter value(Object object) throws JSONException {
        /* 428 */
        return append(valueToString(object));
    }
}
