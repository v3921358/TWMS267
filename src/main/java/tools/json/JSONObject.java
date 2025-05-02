/*      */
package tools.json;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/*      */
/*      */ public final class JSONObject {
    /*      */ private final Map<String, Object> map;

    /*      */
    /*      */ private static final class Null {
        /*      */
        protected final Object clone() {
            /* 23 */
            return this;
            /*      */
        }

        /*      */
        private Null() {
        }

        /*      */
        /*      */
        public boolean equals(Object object) {
            /* 28 */
            return (object == null || object == this);
            /*      */
        }

        /*      */
        /*      */
        /*      */
        public int hashCode() {
            /* 33 */
            return 0;
            /*      */
        }

        /*      */
        /*      */
        /*      */
        public String toString() {
            /* 38 */
            return "null";
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /* 44 */ public static final Object NULL = new Null();

    /*      */
    /*      */
    public JSONObject() {
        /* 47 */
        this.map = new HashMap<>();
        /*      */
    }

    /*      */
    /*      */
    public JSONObject(JSONObject jo, String[] names) {
        /* 51 */
        this(names.length);
        /* 52 */
        for (int i = 0; i < names.length; i++) {
            /*      */
            try {
                /* 54 */
                putOnce(names[i], jo.opt(names[i]));
                /* 55 */
            } catch (Exception exception) {
            }
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    public JSONObject(JSONTokener x) throws JSONException {
        /* 61 */
        this();
        /*      */
        /*      */
        /*      */
        /* 65 */
        if (x.nextClean() != '{') {
            /* 66 */
            throw x.syntaxError("A JSONObject text must begin with '{'");
            /*      */
        }
        /*      */
        while (true) {
            /* 69 */
            char c = x.nextClean();
            /* 70 */
            switch (c) {
                /*      */
                case '\000':
                    /* 72 */
                    throw x.syntaxError("A JSONObject text must end with '}'");
                    /*      */
                case '}':
                    /*      */
                    return;
                /*      */
            }
            /* 76 */
            x.back();
            /* 77 */
            String key = x.nextValue().toString();
            /*      */
            /*      */
            /*      */
            /*      */
            /* 82 */
            c = x.nextClean();
            /* 83 */
            if (c != ':') {
                /* 84 */
                throw x.syntaxError("Expected a ':' after a key");
                /*      */
            }
            /*      */
            /*      */
            /*      */
            /* 89 */
            if (key != null) {
                /*      */
                /* 91 */
                if (opt(key) != null)
                    /*      */ {
                    /* 93 */
                    throw x.syntaxError("Duplicate key \"" + key + "\"");
                    /*      */
                }
                /*      */
                /* 96 */
                Object value = x.nextValue();
                /* 97 */
                if (value != null) {
                    /* 98 */
                    put(key, value);
                    /*      */
                }
                /*      */
            }
            /*      */
            /*      */
            /*      */
            /* 104 */
            switch (x.nextClean()) {
                /*      */
                case ',':
                    /*      */
                case ';':
                    /* 107 */
                    if (x.nextClean() == '}') {
                        /*      */
                        return;
                        /*      */
                    }
                    /* 110 */
                    x.back();
                    continue;
                    /*      */
                case '}':
                    /*      */
                    return;
                /*      */
            }
            break;
            /*      */
        }
        /* 115 */
        throw x.syntaxError("Expected a ',' or '}'");
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject(Map<?, ?> m) {
        /* 121 */
        if (m == null) {
            /* 122 */
            this.map = new HashMap<>();
            /*      */
        } else {
            /* 124 */
            this.map = new HashMap<>(m.size());
            /* 125 */
            for (Map.Entry<?, ?> e : m.entrySet()) {
                /* 126 */
                if (e.getKey() == null) {
                    /* 127 */
                    throw new NullPointerException("Null key.");
                    /*      */
                }
                /* 129 */
                Object value = e.getValue();
                /* 130 */
                if (value != null) {
                    /* 131 */
                    this.map.put(String.valueOf(e.getKey()), wrap(value));
                    /*      */
                }
                /*      */
            }
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject(Object bean) {
        /* 195 */
        this();
        /* 196 */
        populateMap(bean);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject(Object object, String[] names) {
        /* 212 */
        this(names.length);
        /* 213 */
        Class<?> c = object.getClass();
        /* 214 */
        for (int i = 0; i < names.length; i++) {
            /* 215 */
            String name = names[i];
            /*      */
            try {
                /* 217 */
                putOpt(name, c.getField(name).get(object));
                /* 218 */
            } catch (Exception exception) {
            }
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject(String source) throws JSONException {
        /* 234 */
        this(new JSONTokener(source));
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject(String baseName, Locale locale) throws JSONException {
        /* 245 */
        this();
        /* 246 */
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale,
                /* 247 */ Thread.currentThread().getContextClassLoader());
        /*      */
        /*      */
        /*      */
        /* 251 */
        Enumeration<String> keys = bundle.getKeys();
        /* 252 */
        while (keys.hasMoreElements()) {
            /* 253 */
            Object key = keys.nextElement();
            /* 254 */
            if (key != null) {
                /*      */
                /*      */
                /*      */
                /*      */
                /*      */
                /* 260 */
                String[] path = ((String) key).split("\\.");
                /* 261 */
                int last = path.length - 1;
                /* 262 */
                JSONObject target = this;
                /* 263 */
                for (int i = 0; i < last; i++) {
                    /* 264 */
                    String segment = path[i];
                    /* 265 */
                    JSONObject nextTarget = target.optJSONObject(segment);
                    /* 266 */
                    if (nextTarget == null) {
                        /* 267 */
                        nextTarget = new JSONObject();
                        /* 268 */
                        target.put(segment, nextTarget);
                        /*      */
                    }
                    /* 270 */
                    target = nextTarget;
                    /*      */
                }
                /* 272 */
                target.put(path[last], bundle.getString((String) key));
                /*      */
            }
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    protected JSONObject(int initialCapacity) {
        /* 285 */
        this.map = new HashMap<>(initialCapacity);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject accumulate(String key, Object value) throws JSONException {
        /* 306 */
        testValidity(value);
        /* 307 */
        Object object = opt(key);
        /* 308 */
        if (object == null) {
            /* 309 */
            put(key, (value instanceof JSONArray) ? (new JSONArray())
                    /* 310 */.put(value) : value);
            /*      */
        }
        /* 312 */
        else if (object instanceof JSONArray) {
            /* 313 */
            ((JSONArray) object).put(value);
            /*      */
        } else {
            /* 315 */
            put(key, (new JSONArray()).put(object).put(value));
            /*      */
        }
        /* 317 */
        return this;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject append(String key, Object value) throws JSONException {
        /* 334 */
        testValidity(value);
        /* 335 */
        Object object = opt(key);
        /* 336 */
        if (object == null) {
            /* 337 */
            put(key, (new JSONArray()).put(value));
            /* 338 */
        } else if (object instanceof JSONArray) {
            /* 339 */
            put(key, ((JSONArray) object).put(value));
            /*      */
        } else {
            /* 341 */
            throw new JSONException("JSONObject[" + key + "] is not a JSONArray.");
            /*      */
        }
        /*      */
        /* 344 */
        return this;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public static String doubleToString(double d) {
        /* 355 */
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            /* 356 */
            return "null";
            /*      */
        }
        /*      */
        /*      */
        /*      */
        /* 361 */
        String string = Double.toString(d);
        /* 362 */
        if (string.indexOf('.') > 0 && string.indexOf('e') < 0 && string
                /* 363 */.indexOf('E') < 0) {
            /* 364 */
            while (string.endsWith("0")) {
                /* 365 */
                string = string.substring(0, string.length() - 1);
                /*      */
            }
            /* 367 */
            if (string.endsWith(".")) {
                /* 368 */
                string = string.substring(0, string.length() - 1);
                /*      */
            }
            /*      */
        }
        /* 371 */
        return string;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public Object get(String key) throws JSONException {
        /* 382 */
        if (key == null) {
            /* 383 */
            throw new JSONException("Null key.");
            /*      */
        }
        /* 385 */
        Object object = opt(key);
        /* 386 */
        if (object == null) {
            /* 387 */
            throw new JSONException("JSONObject[" + quote(key) + "] not found.");
            /*      */
        }
        /* 389 */
        return object;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public <E extends Enum<E>> E getEnum(Class<E> clazz, String key) throws JSONException {
        /* 402 */
        E val = optEnum(clazz, key);
        /* 403 */
        if (val == null)
            /*      */ {
            /*      */
            /*      */
            /* 407 */
            throw new JSONException("JSONObject[" + quote(key) + "] is not an enum of type " +
                    /* 408 */ quote(clazz.getSimpleName()) + ".");
            /*      */
        }
        /*      */
        /* 411 */
        return val;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public boolean getBoolean(String key) throws JSONException {
        /* 423 */
        Object object = get(key);
        /* 424 */
        if (object.equals(Boolean.FALSE) || (object instanceof String && ((String) object)
                /*      */
                /* 426 */.equalsIgnoreCase("false")))
            /* 427 */ return false;
        /* 428 */
        if (object.equals(Boolean.TRUE) || (object instanceof String && ((String) object)
                /*      */
                /* 430 */.equalsIgnoreCase("true"))) {
            /* 431 */
            return true;
            /*      */
        }
        /* 433 */
        throw new JSONException("JSONObject[" + quote(key) + "] is not a Boolean.");
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public BigInteger getBigInteger(String key) throws JSONException {
        /* 446 */
        Object object = get(key);
        /*      */
        try {
            /* 448 */
            return new BigInteger(object.toString());
            /* 449 */
        } catch (Exception e) {
            /* 450 */
            throw new JSONException("JSONObject[" + quote(key) + "] could not be converted to BigInteger.",
                    e);
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public BigDecimal getBigDecimal(String key) throws JSONException {
        /* 464 */
        Object object = get(key);
        /* 465 */
        if (object instanceof BigDecimal) {
            /* 466 */
            return (BigDecimal) object;
            /*      */
        }
        /*      */
        try {
            /* 469 */
            return new BigDecimal(object.toString());
            /* 470 */
        } catch (Exception e) {
            /* 471 */
            throw new JSONException("JSONObject[" + quote(key) + "] could not be converted to BigDecimal.",
                    e);
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public double getDouble(String key) throws JSONException {
        /* 485 */
        Object object = get(key);
        /*      */
        try {
            /* 487 */
            return (object instanceof Number) ? ((Number) object).doubleValue() :
                    /* 488 */ Double.parseDouble(object.toString());
            /* 489 */
        } catch (Exception e) {
            /* 490 */
            throw new JSONException("JSONObject[" + quote(key) + "] is not a number.", e);
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public float getFloat(String key) throws JSONException {
        /* 504 */
        Object object = get(key);
        /*      */
        try {
            /* 506 */
            return (object instanceof Number) ? ((Number) object).floatValue() :
                    /* 507 */ Float.parseFloat(object.toString());
            /* 508 */
        } catch (Exception e) {
            /* 509 */
            throw new JSONException("JSONObject[" + quote(key) + "] is not a number.", e);
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public Number getNumber(String key) throws JSONException {
        /* 523 */
        Object object = get(key);
        /*      */
        try {
            /* 525 */
            if (object instanceof Number) {
                /* 526 */
                return (Number) object;
                /*      */
            }
            /* 528 */
            return stringToNumber(object.toString());
            /* 529 */
        } catch (Exception e) {
            /* 530 */
            throw new JSONException("JSONObject[" + quote(key) + "] is not a number.", e);
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public int getInt(String key) throws JSONException {
        /* 544 */
        Object object = get(key);
        /*      */
        try {
            /* 546 */
            return (object instanceof Number) ? ((Number) object).intValue() :
                    /* 547 */ Integer.parseInt((String) object);
            /* 548 */
        } catch (Exception e) {
            /* 549 */
            throw new JSONException("JSONObject[" + quote(key) + "] is not an int.", e);
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONArray getJSONArray(String key) throws JSONException {
        /* 562 */
        Object object = get(key);
        /* 563 */
        if (object instanceof JSONArray) {
            /* 564 */
            return (JSONArray) object;
            /*      */
        }
        /* 566 */
        throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONArray.");
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject getJSONObject(String key) throws JSONException {
        /* 578 */
        Object object = get(key);
        /* 579 */
        if (object instanceof JSONObject) {
            /* 580 */
            return (JSONObject) object;
            /*      */
        }
        /* 582 */
        throw new JSONException("JSONObject[" + quote(key) + "] is not a JSONObject.");
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public long getLong(String key) throws JSONException {
        /* 595 */
        Object object = get(key);
        /*      */
        try {
            /* 597 */
            return (object instanceof Number) ? ((Number) object).longValue() :
                    /* 598 */ Long.parseLong((String) object);
            /* 599 */
        } catch (Exception e) {
            /* 600 */
            throw new JSONException("JSONObject[" + quote(key) + "] is not a long.", e);
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public static String[] getNames(JSONObject jo) {
        /* 611 */
        if (jo.isEmpty()) {
            /* 612 */
            return null;
            /*      */
        }
        /* 614 */
        return jo.keySet().<String>toArray(new String[jo.length()]);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public static String[] getNames(Object object) {
        /* 623 */
        if (object == null) {
            /* 624 */
            return null;
            /*      */
        }
        /* 626 */
        Class<?> klass = object.getClass();
        /* 627 */
        Field[] fields = klass.getFields();
        /* 628 */
        int length = fields.length;
        /* 629 */
        if (length == 0) {
            /* 630 */
            return null;
            /*      */
        }
        /* 632 */
        String[] names = new String[length];
        /* 633 */
        for (int i = 0; i < length; i++) {
            /* 634 */
            names[i] = fields[i].getName();
            /*      */
        }
        /* 636 */
        return names;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public String getString(String key) throws JSONException {
        /* 647 */
        Object object = get(key);
        /* 648 */
        if (object instanceof String) {
            /* 649 */
            return (String) object;
            /*      */
        }
        /* 651 */
        throw new JSONException("JSONObject[" + quote(key) + "] not a string.");
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public boolean has(String key) {
        /* 661 */
        return this.map.containsKey(key);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject increment(String key) throws JSONException {
        /* 675 */
        Object value = opt(key);
        /* 676 */
        if (value == null) {
            /* 677 */
            put(key, 1);
            /* 678 */
        } else if (value instanceof BigInteger) {
            /* 679 */
            put(key, ((BigInteger) value).add(BigInteger.ONE));
            /* 680 */
        } else if (value instanceof BigDecimal) {
            /* 681 */
            put(key, ((BigDecimal) value).add(BigDecimal.ONE));
            /* 682 */
        } else if (value instanceof Integer) {
            /* 683 */
            put(key, ((Integer) value).intValue() + 1);
            /* 684 */
        } else if (value instanceof Long) {
            /* 685 */
            put(key, ((Long) value).longValue() + 1L);
            /* 686 */
        } else if (value instanceof Double) {
            /* 687 */
            put(key, ((Double) value).doubleValue() + 1.0D);
            /* 688 */
        } else if (value instanceof Float) {
            /* 689 */
            put(key, ((Float) value).floatValue() + 1.0F);
            /*      */
        } else {
            /* 691 */
            throw new JSONException("Unable to increment [" + quote(key) + "].");
            /*      */
        }
        /* 693 */
        return this;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public boolean isNull(String key) {
        /* 705 */
        return NULL.equals(opt(key));
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public Iterator<String> keys() {
        /* 716 */
        return keySet().iterator();
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public Set<String> keySet() {
        /* 727 */
        return this.map.keySet();
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    protected Set<Map.Entry<String, Object>> entrySet() {
        /* 742 */
        return this.map.entrySet();
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public int length() {
        /* 751 */
        return this.map.size();
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public boolean isEmpty() {
        /* 760 */
        return this.map.isEmpty();
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONArray names() {
        /* 771 */
        if (this.map.isEmpty()) {
            /* 772 */
            return null;
            /*      */
        }
        /* 774 */
        return new JSONArray(this.map.keySet());
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public static String numberToString(Number number) throws JSONException {
        /* 785 */
        if (number == null) {
            /* 786 */
            throw new JSONException("Null pointer");
            /*      */
        }
        /* 788 */
        testValidity(number);
        /*      */
        /*      */
        /*      */
        /* 792 */
        String string = number.toString();
        /* 793 */
        if (string.indexOf('.') > 0 && string.indexOf('e') < 0 && string
                /* 794 */.indexOf('E') < 0) {
            /* 795 */
            while (string.endsWith("0")) {
                /* 796 */
                string = string.substring(0, string.length() - 1);
                /*      */
            }
            /* 798 */
            if (string.endsWith(".")) {
                /* 799 */
                string = string.substring(0, string.length() - 1);
                /*      */
            }
            /*      */
        }
        /* 802 */
        return string;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public Object opt(String key) {
        /* 812 */
        return (key == null) ? null : this.map.get(key);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key) {
        /* 823 */
        return optEnum(clazz, key, null);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public <E extends Enum<E>> E optEnum(Class<E> clazz, String key, E defaultValue) {
        /*      */
        try {
            /* 837 */
            Object val = opt(key);
            /* 838 */
            if (NULL.equals(val)) {
                /* 839 */
                return defaultValue;
                /*      */
            }
            /* 841 */
            if (clazz.isAssignableFrom(val.getClass()))
                /*      */ {
                /*      */
                /* 844 */
                return (E) val;
                /*      */
            }
            /*      */
            /* 847 */
            return Enum.valueOf(clazz, val.toString());
            /* 848 */
        } catch (IllegalArgumentException e) {
            /* 849 */
            return defaultValue;
            /* 850 */
        } catch (NullPointerException e) {
            /* 851 */
            return defaultValue;
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public boolean optBoolean(String key) {
        /* 863 */
        return optBoolean(key, false);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public boolean optBoolean(String key, boolean defaultValue) {
        /* 876 */
        Object val = opt(key);
        /* 877 */
        if (NULL.equals(val)) {
            /* 878 */
            return defaultValue;
            /*      */
        }
        /* 880 */
        if (val instanceof Boolean) {
            /* 881 */
            return ((Boolean) val).booleanValue();
            /*      */
        }
        /*      */
        /*      */
        try {
            /* 885 */
            return getBoolean(key);
            /* 886 */
        } catch (Exception e) {
            /* 887 */
            return defaultValue;
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public BigDecimal optBigDecimal(String key, BigDecimal defaultValue) {
        /* 901 */
        Object val = opt(key);
        /* 902 */
        if (NULL.equals(val)) {
            /* 903 */
            return defaultValue;
            /*      */
        }
        /* 905 */
        if (val instanceof BigDecimal) {
            /* 906 */
            return (BigDecimal) val;
            /*      */
        }
        /* 908 */
        if (val instanceof BigInteger) {
            /* 909 */
            return new BigDecimal((BigInteger) val);
            /*      */
        }
        /* 911 */
        if (val instanceof Double || val instanceof Float) {
            /* 912 */
            return new BigDecimal(((Number) val).doubleValue());
            /*      */
        }
        /* 914 */
        if (val instanceof Long || val instanceof Integer || val instanceof Short || val instanceof Byte)
            /*      */ {
            /* 916 */
            return new BigDecimal(((Number) val).longValue());
            /*      */
        }
        /*      */
        /*      */
        try {
            /* 920 */
            return new BigDecimal(val.toString());
            /* 921 */
        } catch (Exception e) {
            /* 922 */
            return defaultValue;
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public BigInteger optBigInteger(String key, BigInteger defaultValue) {
        /* 936 */
        Object val = opt(key);
        /* 937 */
        if (NULL.equals(val)) {
            /* 938 */
            return defaultValue;
            /*      */
        }
        /* 940 */
        if (val instanceof BigInteger) {
            /* 941 */
            return (BigInteger) val;
            /*      */
        }
        /* 943 */
        if (val instanceof BigDecimal) {
            /* 944 */
            return ((BigDecimal) val).toBigInteger();
            /*      */
        }
        /* 946 */
        if (val instanceof Double || val instanceof Float) {
            /* 947 */
            return (new BigDecimal(((Number) val).doubleValue())).toBigInteger();
            /*      */
        }
        /* 949 */
        if (val instanceof Long || val instanceof Integer || val instanceof Short || val instanceof Byte)
            /*      */ {
            /* 951 */
            return BigInteger.valueOf(((Number) val).longValue());
            /*      */
        }
        /*      */
        /*      */
        /*      */
        /*      */
        /*      */
        /*      */
        /*      */
        try {
            /* 960 */
            String valStr = val.toString();
            /* 961 */
            if (isDecimalNotation(valStr)) {
                /* 962 */
                return (new BigDecimal(valStr)).toBigInteger();
                /*      */
            }
            /* 964 */
            return new BigInteger(valStr);
            /* 965 */
        } catch (Exception e) {
            /* 966 */
            return defaultValue;
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public double optDouble(String key) {
        /* 979 */
        return optDouble(key, Double.NaN);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public double optDouble(String key, double defaultValue) {
        /* 992 */
        Object val = opt(key);
        /* 993 */
        if (NULL.equals(val)) {
            /* 994 */
            return defaultValue;
            /*      */
        }
        /* 996 */
        if (val instanceof Number) {
            /* 997 */
            return ((Number) val).doubleValue();
            /*      */
        }
        /* 999 */
        if (val instanceof String) {
            /*      */
            try {
                /* 1001 */
                return Double.parseDouble((String) val);
                /* 1002 */
            } catch (Exception e) {
                /* 1003 */
                return defaultValue;
                /*      */
            }
            /*      */
        }
        /* 1006 */
        return defaultValue;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public float optFloat(String key) {
        /* 1018 */
        return optFloat(key, Float.NaN);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public float optFloat(String key, float defaultValue) {
        /* 1031 */
        Object val = opt(key);
        /* 1032 */
        if (NULL.equals(val)) {
            /* 1033 */
            return defaultValue;
            /*      */
        }
        /* 1035 */
        if (val instanceof Number) {
            /* 1036 */
            return ((Number) val).floatValue();
            /*      */
        }
        /* 1038 */
        if (val instanceof String) {
            /*      */
            try {
                /* 1040 */
                return Float.parseFloat((String) val);
                /* 1041 */
            } catch (Exception e) {
                /* 1042 */
                return defaultValue;
                /*      */
            }
            /*      */
        }
        /* 1045 */
        return defaultValue;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public int optInt(String key) {
        /* 1057 */
        return optInt(key, 0);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public int optInt(String key, int defaultValue) {
        /* 1070 */
        Object val = opt(key);
        /* 1071 */
        if (NULL.equals(val)) {
            /* 1072 */
            return defaultValue;
            /*      */
        }
        /* 1074 */
        if (val instanceof Number) {
            /* 1075 */
            return ((Number) val).intValue();
            /*      */
        }
        /*      */
        /* 1078 */
        if (val instanceof String) {
            /*      */
            try {
                /* 1080 */
                return (new BigDecimal((String) val)).intValue();
                /* 1081 */
            } catch (Exception e) {
                /* 1082 */
                return defaultValue;
                /*      */
            }
            /*      */
        }
        /* 1085 */
        return defaultValue;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONArray optJSONArray(String key) {
        /* 1096 */
        Object o = opt(key);
        /* 1097 */
        return (o instanceof JSONArray) ? (JSONArray) o : null;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject optJSONObject(String key) {
        /* 1108 */
        Object object = opt(key);
        /* 1109 */
        return (object instanceof JSONObject) ? (JSONObject) object : null;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public long optLong(String key) {
        /* 1121 */
        return optLong(key, 0L);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public long optLong(String key, long defaultValue) {
        /* 1134 */
        Object val = opt(key);
        /* 1135 */
        if (NULL.equals(val)) {
            /* 1136 */
            return defaultValue;
            /*      */
        }
        /* 1138 */
        if (val instanceof Number) {
            /* 1139 */
            return ((Number) val).longValue();
            /*      */
        }
        /*      */
        /* 1142 */
        if (val instanceof String) {
            /*      */
            try {
                /* 1144 */
                return (new BigDecimal((String) val)).longValue();
                /* 1145 */
            } catch (Exception e) {
                /* 1146 */
                return defaultValue;
                /*      */
            }
            /*      */
        }
        /* 1149 */
        return defaultValue;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public Number optNumber(String key) {
        /* 1162 */
        return optNumber(key, null);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public Number optNumber(String key, Number defaultValue) {
        /* 1176 */
        Object val = opt(key);
        /* 1177 */
        if (NULL.equals(val)) {
            /* 1178 */
            return defaultValue;
            /*      */
        }
        /* 1180 */
        if (val instanceof Number) {
            /* 1181 */
            return (Number) val;
            /*      */
        }
        /*      */
        /* 1184 */
        if (val instanceof String) {
            /*      */
            try {
                /* 1186 */
                return stringToNumber((String) val);
                /* 1187 */
            } catch (Exception e) {
                /* 1188 */
                return defaultValue;
                /*      */
            }
            /*      */
        }
        /* 1191 */
        return defaultValue;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public String optString(String key) {
        /* 1203 */
        return optString(key, "");
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public String optString(String key, String defaultValue) {
        /* 1215 */
        Object object = opt(key);
        /* 1216 */
        return NULL.equals(object) ? defaultValue : object.toString();
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    private void populateMap(Object bean) {
        /* 1227 */
        Class<?> klass = bean.getClass();
        /*      */
        /*      */
        /*      */
        /* 1231 */
        boolean includeSuperClass = (klass.getClassLoader() != null);
        /*      */
        /* 1233 */
        Method[] methods = includeSuperClass ? klass.getMethods() : klass.getDeclaredMethods();
        /* 1234 */
        for (Method method : methods) {
            /* 1235 */
            int modifiers = method.getModifiers();
            /* 1236 */
            if (Modifier.isPublic(modifiers) &&
                    /* 1237 */ !Modifier.isStatic(modifiers) && (method
                    /* 1238 */.getParameterTypes()).length == 0 &&
                    /* 1239 */ !method.isBridge() && method
                    /* 1240 */.getReturnType() != void.class &&
                    /* 1241 */ isValidMethodName(method.getName())) {
                /* 1242 */
                String key = getKeyNameFromMethod(method);
                /* 1243 */
                if (key != null && !key.isEmpty()) {
                    /*      */
                    /* 1245 */
                    try {
                        Object result = method.invoke(bean, new Object[0]);
                        /* 1246 */
                        if (result != null) {
                            /* 1247 */
                            this.map.put(key, wrap(result));
                            /*      */
                            /*      */
                            /*      */
                            /* 1251 */
                            if (result instanceof Closeable) {
                                /*      */
                                try {
                                    /* 1253 */
                                    ((Closeable) result).close();
                                    /* 1254 */
                                } catch (IOException iOException) {
                                }
                                /*      */
                            }
                            /*      */
                        }
                        /*      */
                    }
                    /* 1258 */ catch (IllegalAccessException illegalAccessException) {
                    }
                    /* 1259 */ catch (IllegalArgumentException illegalArgumentException) {
                    }
                    /* 1260 */ catch (InvocationTargetException invocationTargetException) {
                    }
                    /*      */
                }
                /*      */
            }
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    private boolean isValidMethodName(String name) {
        /* 1268 */
        return (!"getClass".equals(name) && !"getDeclaringClass".equals(name));
        /*      */
    }

    /*      */
    private String getKeyNameFromMethod(Method method) {
        /*      */
        String key;
        /* 1272 */
        int ignoreDepth = getAnnotationDepth(method, (Class) JSONPropertyIgnore.class);
        /* 1273 */
        if (ignoreDepth > 0) {
            /* 1274 */
            int forcedNameDepth = getAnnotationDepth(method, (Class) JSONPropertyName.class);
            /* 1275 */
            if (forcedNameDepth < 0 || ignoreDepth <= forcedNameDepth)
                /*      */ {
                /*      */
                /* 1278 */
                return null;
                /*      */
            }
            /*      */
        }
        /* 1281 */
        JSONPropertyName annotation = getAnnotation(method, JSONPropertyName.class);
        /* 1282 */
        if (annotation != null && annotation.value() != null && !annotation.value().isEmpty()) {
            /* 1283 */
            return annotation.value();
            /*      */
        }
        /*      */
        /* 1286 */
        String name = method.getName();
        /* 1287 */
        if (name.startsWith("get") && name.length() > 3) {
            /* 1288 */
            key = name.substring(3);
            /* 1289 */
        } else if (name.startsWith("is") && name.length() > 2) {
            /* 1290 */
            key = name.substring(2);
            /*      */
        } else {
            /* 1292 */
            return null;
            /*      */
        }
        /*      */
        /*      */
        /*      */
        /* 1297 */
        if (Character.isLowerCase(key.charAt(0))) {
            /* 1298 */
            return null;
            /*      */
        }
        /* 1300 */
        if (key.length() == 1) {
            /* 1301 */
            key = key.toLowerCase(Locale.ROOT);
            /* 1302 */
        } else if (!Character.isUpperCase(key.charAt(1))) {
            /* 1303 */
            key = key.substring(0, 1).toLowerCase(Locale.ROOT) + key.substring(1);
            /*      */
        }
        /* 1305 */
        return key;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    private static <A extends Annotation> A getAnnotation(Method m, Class<A> annotationClass) {
        /* 1320 */
        if (m == null || annotationClass == null) {
            /* 1321 */
            return null;
            /*      */
        }
        /*      */
        /* 1324 */
        if (m.isAnnotationPresent(annotationClass)) {
            /* 1325 */
            return m.getAnnotation(annotationClass);
            /*      */
        }
        /*      */
        /*      */
        /* 1329 */
        Class<?> c = m.getDeclaringClass();
        /* 1330 */
        if (c.getSuperclass() == null) {
            /* 1331 */
            return null;
            /*      */
        }
        /*      */
        /*      */
        /* 1335 */
        for (Class<?> i : c.getInterfaces()) {
            /*      */
            try {
                /* 1337 */
                Method im = i.getMethod(m.getName(), m.getParameterTypes());
                /* 1338 */
                return getAnnotation(im, annotationClass);
                /* 1339 */
            } catch (SecurityException ex) {
                /*      */
                /* 1341 */
            } catch (NoSuchMethodException ex) {
            }
            /*      */
        }
        /*      */
        /*      */
        /*      */
        /*      */
        try {
            /* 1347 */
            return getAnnotation(c
                    /* 1348 */.getSuperclass().getMethod(m.getName(), m.getParameterTypes()), annotationClass);
            /*      */
        }
        /* 1350 */ catch (SecurityException ex) {
            /* 1351 */
            return null;
            /* 1352 */
        } catch (NoSuchMethodException ex) {
            /* 1353 */
            return null;
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    private static int getAnnotationDepth(Method m, Class<? extends Annotation> annotationClass) {
        /* 1359 */
        if (m == null || annotationClass == null) {
            /* 1360 */
            return -1;
            /*      */
        }
        /*      */
        /* 1363 */
        if (m.isAnnotationPresent(annotationClass)) {
            /* 1364 */
            return 1;
            /*      */
        }
        /*      */
        /*      */
        /* 1368 */
        Class<?> c = m.getDeclaringClass();
        /* 1369 */
        if (c.getSuperclass() == null) {
            /* 1370 */
            return -1;
            /*      */
        }
        /*      */
        /*      */
        /* 1374 */
        for (Class<?> i : c.getInterfaces()) {
            /*      */
            try {
                /* 1376 */
                Method im = i.getMethod(m.getName(), m.getParameterTypes());
                /* 1377 */
                int d = getAnnotationDepth(im, annotationClass);
                /* 1378 */
                if (d > 0)
                    /*      */ {
                    /* 1380 */
                    return d + 1;
                    /*      */
                }
                /* 1382 */
            } catch (SecurityException ex) {
                /*      */
                /* 1384 */
            } catch (NoSuchMethodException ex) {
            }
            /*      */
        }
        /*      */
        /*      */
        /*      */
        /*      */
        try {
            /* 1390 */
            int d = getAnnotationDepth(c
                    /* 1391 */.getSuperclass().getMethod(m.getName(), m.getParameterTypes()), annotationClass);
            /*      */
            /* 1393 */
            if (d > 0)
                /*      */ {
                /* 1395 */
                return d + 1;
                /*      */
            }
            /* 1397 */
            return -1;
            /* 1398 */
        } catch (SecurityException ex) {
            /* 1399 */
            return -1;
            /* 1400 */
        } catch (NoSuchMethodException ex) {
            /* 1401 */
            return -1;
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject put(String key, boolean value) throws JSONException {
        /* 1415 */
        return put(key, value ? Boolean.TRUE : Boolean.FALSE);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject put(String key, Collection<?> value) throws JSONException {
        /* 1429 */
        return put(key, new JSONArray(value));
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject put(String key, double value) throws JSONException {
        /* 1442 */
        return put(key, Double.valueOf(value));
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject put(String key, float value) throws JSONException {
        /* 1455 */
        return put(key, Float.valueOf(value));
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject put(String key, int value) throws JSONException {
        /* 1468 */
        return put(key, Integer.valueOf(value));
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject put(String key, long value) throws JSONException {
        /* 1481 */
        return put(key, Long.valueOf(value));
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject put(String key, Map<?, ?> value) throws JSONException {
        /* 1495 */
        return put(key, new JSONObject(value));
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject put(String key, Object value) throws JSONException {
        /* 1511 */
        if (key == null) {
            /* 1512 */
            throw new NullPointerException("Null key.");
            /*      */
        }
        /* 1514 */
        if (value != null) {
            /* 1515 */
            testValidity(value);
            /* 1516 */
            this.map.put(key, value);
            /*      */
        } else {
            /* 1518 */
            remove(key);
            /*      */
        }
        /* 1520 */
        return this;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject putOnce(String key, Object value) throws JSONException {
        /* 1534 */
        if (key != null && value != null) {
            /* 1535 */
            if (opt(key) != null) {
                /* 1536 */
                throw new JSONException("Duplicate key \"" + key + "\"");
                /*      */
            }
            /* 1538 */
            return put(key, value);
            /*      */
        }
        /* 1540 */
        return this;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONObject putOpt(String key, Object value) throws JSONException {
        /* 1555 */
        if (key != null && value != null) {
            /* 1556 */
            return put(key, value);
            /*      */
        }
        /* 1558 */
        return this;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public Object query(String jsonPointer) {
        /* 1581 */
        return query(new JSONPointer(jsonPointer));
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public Object query(JSONPointer jsonPointer) {
        /* 1604 */
        return jsonPointer.queryFrom(this);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public Object optQuery(String jsonPointer) {
        /* 1616 */
        return optQuery(new JSONPointer(jsonPointer));
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public Object optQuery(JSONPointer jsonPointer) {
        /*      */
        try {
            /* 1629 */
            return jsonPointer.queryFrom(this);
            /* 1630 */
        } catch (JSONPointerException e) {
            /* 1631 */
            return null;
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public static String quote(String string) {
        StringWriter sw = new StringWriter();
        synchronized (sw.getBuffer()) {
            try {
                return quote(string, sw).toString();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return string;
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public static Writer quote(String string, Writer w) throws IOException {
        /* 1657 */
        if (string == null || string.isEmpty()) {
            /* 1658 */
            w.write("\"\"");
            /* 1659 */
            return w;
            /*      */
        }
        /*      */
        /*      */
        /* 1663 */
        char c = Character.MIN_VALUE;
        /*      */
        /*      */
        /* 1666 */
        int len = string.length();
        /*      */
        /* 1668 */
        w.write(34);
        /* 1669 */
        for (int i = 0; i < len; i++) {
            /* 1670 */
            char b = c;
            /* 1671 */
            c = string.charAt(i);
            /* 1672 */
            switch (c) {
                /*      */
                case '"':
                    /*      */
                case '\\':
                    /* 1675 */
                    w.write(92);
                    /* 1676 */
                    w.write(c);
                    /*      */
                    break;
                /*      */
                case '/':
                    /* 1679 */
                    if (b == '<') {
                        /* 1680 */
                        w.write(92);
                        /*      */
                    }
                    /* 1682 */
                    w.write(c);
                    /*      */
                    break;
                /*      */
                case '\b':
                    /* 1685 */
                    w.write("\\b");
                    /*      */
                    break;
                /*      */
                case '\t':
                    /* 1688 */
                    w.write("\\t");
                    /*      */
                    break;
                /*      */
                case '\n':
                    /* 1691 */
                    w.write("\\n");
                    /*      */
                    break;
                /*      */
                case '\f':
                    /* 1694 */
                    w.write("\\f");
                    /*      */
                    break;
                /*      */
                case '\r':
                    /* 1697 */
                    w.write("\\r");
                    /*      */
                    break;
                /*      */
                default:
                    /* 1700 */
                    if (c < ' ' || (c >= '' && c < ' ') || (c >= ' ' && c < '℀')) {
                        /*      */
                        /* 1702 */
                        w.write("\\u");
                        /* 1703 */
                        String hhhh = Integer.toHexString(c);
                        /* 1`704 */
                        w.write("0000", 0, 4 - hhhh.length());
                        /* 1705 */
                        w.write(hhhh);
                        break;
                        /*      */
                    }
                    /* 1707 */
                    w.write(c);
                    /*      */
                    break;
                /*      */
            }
            /*      */
        }
        /* 1711 */
        w.write(34);
        /* 1712 */
        return w;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public Object remove(String key) {
        /* 1723 */
        return this.map.remove(key);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public boolean similar(Object other) {
        /*      */
        try {
            /* 1736 */
            if (!(other instanceof JSONObject)) {
                /* 1737 */
                return false;
                /*      */
            }
            /* 1739 */
            if (!keySet().equals(((JSONObject) other).keySet())) {
                /* 1740 */
                return false;
                /*      */
            }
            /* 1742 */
            for (Map.Entry<String, ?> entry : entrySet()) {
                /* 1743 */
                String name = entry.getKey();
                /* 1744 */
                Object valueThis = entry.getValue();
                /* 1745 */
                Object valueOther = ((JSONObject) other).get(name);
                /* 1746 */
                if (valueThis == valueOther) {
                    /*      */
                    continue;
                    /*      */
                }
                /* 1749 */
                if (valueThis == null) {
                    /* 1750 */
                    return false;
                    /*      */
                }
                /* 1752 */
                if (valueThis instanceof JSONObject) {
                    /* 1753 */
                    if (!((JSONObject) valueThis).similar(valueOther))
                        /* 1754 */ return false;
                    continue;
                    /*      */
                }
                /* 1756 */
                if (valueThis instanceof JSONArray) {
                    /* 1757 */
                    if (!((JSONArray) valueThis).similar(valueOther))
                        /* 1758 */ return false;
                    continue;
                    /*      */
                }
                /* 1760 */
                if (!valueThis.equals(valueOther)) {
                    /* 1761 */
                    return false;
                    /*      */
                }
                /*      */
            }
            /* 1764 */
            return true;
            /* 1765 */
        } catch (Throwable exception) {
            /* 1766 */
            return false;
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    protected static boolean isDecimalNotation(String val) {
        /* 1777 */
        return (val.indexOf('.') > -1 || val.indexOf('e') > -1 || val
                /* 1778 */.indexOf('E') > -1 || "-0".equals(val));
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    protected static Number stringToNumber(String val) throws NumberFormatException {
        /* 1792 */
        char initial = val.charAt(0);
        /* 1793 */
        if ((initial >= '0' && initial <= '9') || initial == '-') {
            /*      */
            /* 1795 */
            if (isDecimalNotation(val)) {
                /*      */
                /*      */
                /* 1798 */
                if (val.length() > 14) {
                    /* 1799 */
                    return new BigDecimal(val);
                    /*      */
                }
                /* 1801 */
                Double d = Double.valueOf(val);
                /* 1802 */
                if (d.isInfinite() || d.isNaN())
                    /*      */ {
                    /*      */
                    /*      */
                    /*      */
                    /* 1807 */
                    return new BigDecimal(val);
                    /*      */
                }
                /* 1809 */
                return d;
                /*      */
            }
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /*      */
            /* 1833 */
            BigInteger bi = new BigInteger(val);
            /* 1834 */
            if (bi.bitLength() <= 31) {
                /* 1835 */
                return Integer.valueOf(bi.intValue());
                /*      */
            }
            /* 1837 */
            if (bi.bitLength() <= 63) {
                /* 1838 */
                return Long.valueOf(bi.longValue());
                /*      */
            }
            /* 1840 */
            return bi;
            /*      */
        }
        /* 1842 */
        throw new NumberFormatException("val [" + val + "] is not a valid number.");
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public static Object stringToValue(String string) {
        /* 1855 */
        if (string.equals("")) {
            /* 1856 */
            return string;
            /*      */
        }
        /* 1858 */
        if (string.equalsIgnoreCase("true")) {
            /* 1859 */
            return Boolean.TRUE;
            /*      */
        }
        /* 1861 */
        if (string.equalsIgnoreCase("false")) {
            /* 1862 */
            return Boolean.FALSE;
            /*      */
        }
        /* 1864 */
        if (string.equalsIgnoreCase("null")) {
            /* 1865 */
            return NULL;
            /*      */
        }
        /*      */
        /*      */
        /*      */
        /*      */
        /*      */
        /*      */
        /* 1873 */
        char initial = string.charAt(0);
        /* 1874 */
        if ((initial >= '0' && initial <= '9') || initial == '-') {
            /*      */
            /*      */
            try {
                /*      */
                /* 1878 */
                if (isDecimalNotation(string)) {
                    /* 1879 */
                    Double d = Double.valueOf(string);
                    /* 1880 */
                    if (!d.isInfinite() && !d.isNaN()) {
                        /* 1881 */
                        return d;
                        /*      */
                    }
                    /*      */
                } else {
                    /* 1884 */
                    Long myLong = Long.valueOf(string);
                    /* 1885 */
                    if (string.equals(myLong.toString())) {
                        /* 1886 */
                        if (myLong.longValue() == myLong.intValue()) {
                            /* 1887 */
                            return Integer.valueOf(myLong.intValue());
                            /*      */
                        }
                        /* 1889 */
                        return myLong;
                        /*      */
                    }
                    /*      */
                }
                /* 1892 */
            } catch (Exception exception) {
            }
            /*      */
        }
        /*      */
        /* 1895 */
        return string;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public static void testValidity(Object o) throws JSONException {
        /* 1905 */
        if (o != null) {
            /* 1906 */
            if (o instanceof Double) {
                /* 1907 */
                if (((Double) o).isInfinite() || ((Double) o).isNaN()) {
                    /* 1908 */
                    throw new JSONException("JSON does not allow non-finite numbers.");
                    /*      */
                }
                /*      */
            }
            /* 1911 */
            else if (o instanceof Float && ((
                    /* 1912 */ (Float) o).isInfinite() || ((Float) o).isNaN())) {
                /* 1913 */
                throw new JSONException("JSON does not allow non-finite numbers.");
                /*      */
            }
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public JSONArray toJSONArray(JSONArray names) throws JSONException {
        /* 1930 */
        if (names == null || names.isEmpty()) {
            /* 1931 */
            return null;
            /*      */
        }
        /* 1933 */
        JSONArray ja = new JSONArray();
        /* 1934 */
        for (int i = 0; i < names.length(); i++) {
            /* 1935 */
            ja.put(opt(names.getString(i)));
            /*      */
        }
        /* 1937 */
        return ja;
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public String toString() {
        /*      */
        try {
            /* 1956 */
            return toString(0);
            /* 1957 */
        } catch (Exception e) {
            /* 1958 */
            return null;
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public String toString(int indentFactor) throws JSONException {
        /* 1987 */
        StringWriter w = new StringWriter();
        /* 1988 */
        synchronized (w.getBuffer()) {
            /* 1989 */
            return write(w, indentFactor, 0).toString();
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public static String valueToString(Object value) throws JSONException {
        /* 2020 */
        return JSONWriter.valueToString(value);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public static Object wrap(Object object) {
        /*      */
        try {
            /* 2036 */
            if (object == null) {
                /* 2037 */
                return NULL;
                /*      */
            }
            /* 2039 */
            if (object instanceof JSONObject || object instanceof JSONArray || NULL
                    /* 2040 */.equals(object) || object instanceof JSONString || object instanceof Byte
                    || object instanceof Character || object instanceof Short || object instanceof Integer
                    || object instanceof Long || object instanceof Boolean || object instanceof Float
                    || object instanceof Double || object instanceof String || object instanceof BigInteger
                    || object instanceof BigDecimal || object instanceof Enum)
                /*      */ {
                /*      */
                /*      */
                /*      */
                /*      */
                /*      */
                /* 2047 */
                return object;
                /*      */
            }
            /*      */
            /* 2050 */
            if (object instanceof Collection) {
                /* 2051 */
                Collection<?> coll = (Collection) object;
                /* 2052 */
                return new JSONArray(coll);
                /*      */
            }
            /* 2054 */
            if (object.getClass().isArray()) {
                /* 2055 */
                return new JSONArray(object);
                /*      */
            }
            /* 2057 */
            if (object instanceof Map) {
                /* 2058 */
                Map<?, ?> map = (Map<?, ?>) object;
                /* 2059 */
                return new JSONObject(map);
                /*      */
            }
            /* 2061 */
            Package objectPackage = object.getClass().getPackage();
            /*      */
            /* 2063 */
            String objectPackageName = (objectPackage != null) ? objectPackage.getName() : "";
            /* 2064 */
            if (objectPackageName.startsWith("java.") || objectPackageName
                    /* 2065 */.startsWith("javax.")
                    || object
                    /* 2066 */.getClass().getClassLoader() == null) {
                /* 2067 */
                return object.toString();
                /*      */
            }
            /* 2069 */
            return new JSONObject(object);
            /* 2070 */
        } catch (Exception exception) {
            /* 2071 */
            return null;
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    /*      */
    public Writer write(Writer writer) throws JSONException {
        /* 2086 */
        return write(writer, 0, 0);
        /*      */
    }

    /*      */
    /*      */
    /*      */
    static final Writer writeValue(Writer writer, Object value, int indentFactor, int indent)
            throws JSONException, IOException {
        /* 2091 */
        if (value == null || value.equals(null)) {
            /* 2092 */
            writer.write("null");
            /* 2093 */
        } else if (value instanceof JSONString) {
            /*      */
            Object o;
            /*      */
            try {
                /* 2096 */
                o = ((JSONString) value).toJSONString();
                /* 2097 */
            } catch (Exception e) {
                /* 2098 */
                throw new JSONException(e);
                /*      */
            }
            /* 2100 */
            writer.write((o != null) ? o.toString() : quote(value.toString()));
            /* 2101 */
        } else if (value instanceof Number) {
            /*      */
            /* 2103 */
            String numberAsString = numberToString((Number) value);
            /*      */
            /*      */
            /*      */
            try {
                /* 2107 */
                BigDecimal testNum = new BigDecimal(numberAsString);
                /*      */
                /* 2109 */
                writer.write(numberAsString);
                /* 2110 */
            } catch (NumberFormatException ex) {
                /*      */
                /*      */
                /* 2113 */
                quote(numberAsString, writer);
                /*      */
            }
            /* 2115 */
        } else if (value instanceof Boolean) {
            /* 2116 */
            writer.write(value.toString());
            /* 2117 */
        } else if (value instanceof Enum) {
            /* 2118 */
            writer.write(quote(((Enum) value).name()));
            /* 2119 */
        } else if (value instanceof JSONObject) {
            /* 2120 */
            ((JSONObject) value).write(writer, indentFactor, indent);
            /* 2121 */
        } else if (value instanceof JSONArray) {
            /* 2122 */
            ((JSONArray) value).write(writer, indentFactor, indent);
            /* 2123 */
        } else if (value instanceof Map) {
            /* 2124 */
            Map<?, ?> map = (Map<?, ?>) value;
            /* 2125 */
            (new JSONObject(map)).write(writer, indentFactor, indent);
            /* 2126 */
        } else if (value instanceof Collection) {
            /* 2127 */
            Collection<?> coll = (Collection) value;
            /* 2128 */
            (new JSONArray(coll)).write(writer, indentFactor, indent);
            /* 2129 */
        } else if (value.getClass().isArray()) {
            /* 2130 */
            (new JSONArray(value)).write(writer, indentFactor, indent);
            /*      */
        } else {
            /* 2132 */
            quote(value.toString(), writer);
            /*      */
        }
        /* 2134 */
        return writer;
        /*      */
    }

    /*      */
    /*      */
    static final void indent(Writer writer, int indent) throws IOException {
        /* 2138 */
        for (int i = 0; i < indent; i++) {
            /* 2139 */
            writer.write(32);
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    /*      */
    public Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
        /*      */
        try {
            /* 2146 */
            boolean commanate = false;
            /* 2147 */
            int length = length();
            /* 2148 */
            writer.write(123);
            /*      */
            /* 2150 */
            if (length == 1) {
                /* 2151 */
                Map.Entry<String, ?> entry = entrySet().iterator().next();
                /* 2152 */
                String key = entry.getKey();
                /* 2153 */
                writer.write(quote(key));
                /* 2154 */
                writer.write(58);
                /* 2155 */
                if (indentFactor > 0) {
                    /* 2156 */
                    writer.write(32);
                    /*      */
                }
                /*      */
                try {
                    /* 2159 */
                    writeValue(writer, entry.getValue(), indentFactor, indent);
                    /* 2160 */
                } catch (Exception e) {
                    /* 2161 */
                    throw new JSONException("Unable to write JSONObject value for key: " + key, e);
                    /*      */
                }
                /* 2163 */
            } else if (length != 0) {
                /* 2164 */
                int newindent = indent + indentFactor;
                /* 2165 */
                for (Map.Entry<String, ?> entry : entrySet()) {
                    /* 2166 */
                    if (commanate) {
                        /* 2167 */
                        writer.write(44);
                        /*      */
                    }
                    /* 2169 */
                    if (indentFactor > 0) {
                        /* 2170 */
                        writer.write(10);
                        /*      */
                    }
                    /* 2172 */
                    indent(writer, newindent);
                    /* 2173 */
                    String key = entry.getKey();
                    /* 2174 */
                    writer.write(quote(key));
                    /* 2175 */
                    writer.write(58);
                    /* 2176 */
                    if (indentFactor > 0) {
                        /* 2177 */
                        writer.write(32);
                        /*      */
                    }
                    /*      */
                    try {
                        /* 2180 */
                        writeValue(writer, entry.getValue(), indentFactor, newindent);
                        /* 2181 */
                    } catch (Exception e) {
                        /* 2182 */
                        throw new JSONException("Unable to write JSONObject value for key: " + key, e);
                        /*      */
                    }
                    /* 2184 */
                    commanate = true;
                    /*      */
                }
                /* 2186 */
                if (indentFactor > 0) {
                    /* 2187 */
                    writer.write(10);
                    /*      */
                }
                /* 2189 */
                indent(writer, indent);
                /*      */
            }
            /* 2191 */
            writer.write(125);
            /* 2192 */
            return writer;
            /* 2193 */
        } catch (IOException exception) {
            /* 2194 */
            throw new JSONException(exception);
            /*      */
        }
        /*      */
    }

    /*      */
    /*      */
    public Map<String, Object> toMap() {
        /* 2199 */
        Map<String, Object> results = new HashMap<>();
        /* 2200 */
        for (Map.Entry<String, Object> entry : entrySet()) {
            /*      */
            Object value;
            /* 2202 */
            if (entry.getValue() == null || NULL.equals(entry.getValue())) {
                /* 2203 */
                value = null;
                /* 2204 */
            } else if (entry.getValue() instanceof JSONObject) {
                /* 2205 */
                value = ((JSONObject) entry.getValue()).toMap();
                /* 2206 */
            } else if (entry.getValue() instanceof JSONArray) {
                /* 2207 */
                value = ((JSONArray) entry.getValue()).toList();
                /*      */
            } else {
                /* 2209 */
                value = entry.getValue();
                /*      */
            }
            /* 2211 */
            results.put(entry.getKey(), value);
            /*      */
        }
        /* 2213 */
        return results;
        /*      */
    }
    /*      */
}
