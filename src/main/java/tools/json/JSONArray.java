package tools.json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

public final class JSONArray implements Iterable<Object> {
    public JSONArray() {
        /* 16 */
        this.myArrayList = new ArrayList();
    }

    private final ArrayList<Object> myArrayList;

    public JSONArray(JSONTokener x) throws JSONException {
        /* 20 */
        this();
        /* 21 */
        if (x.nextClean() != '[') {
            /* 22 */
            throw x.syntaxError("A JSONArray text must start with '['");
        }

        /* 25 */
        char nextChar = x.nextClean();
        /* 26 */
        if (nextChar == '\000') {
            /* 28 */
            throw x.syntaxError("Expected a ',' or ']'");
        }
        /* 30 */
        if (nextChar != ']') {
            /* 31 */
            x.back();
            while (true) {
                /* 33 */
                if (x.nextClean() == ',') {
                    /* 34 */
                    x.back();
                    /* 35 */
                    this.myArrayList.add(JSONObject.NULL);
                } else {
                    /* 37 */
                    x.back();
                    /* 38 */
                    this.myArrayList.add(x.nextValue());
                }
                /* 40 */
                switch (x.nextClean()) {

                    case '\000':
                        /* 43 */
                        throw x.syntaxError("Expected a ',' or ']'");
                    case ',':
                        /* 45 */
                        nextChar = x.nextClean();
                        /* 46 */
                        if (nextChar == '\000') {
                            /* 48 */
                            throw x.syntaxError("Expected a ',' or ']'");
                        }
                        /* 50 */
                        if (nextChar == ']') {
                            return;
                        }
                        /* 53 */
                        x.back();
                        continue;
                    case ']':
                        return;
                }
                break;
            }
            /* 58 */
            throw x.syntaxError("Expected a ',' or ']'");
        }
    }

    public JSONArray(String source) throws JSONException {
        /* 65 */
        this(new JSONTokener(source));
    }

    public JSONArray(Collection<?> collection) {
        /* 69 */
        if (collection == null) {
            /* 70 */
            this.myArrayList = new ArrayList();
        } else {
            /* 72 */
            this.myArrayList = new ArrayList(collection.size());
            /* 73 */
            for (Object o : collection) {
                /* 74 */
                this.myArrayList.add(JSONObject.wrap(o));
            }
        }
    }

    public JSONArray(Object array) throws JSONException {
        /* 80 */
        this();
        /* 81 */
        if (array.getClass().isArray()) {
            /* 82 */
            int length = Array.getLength(array);
            /* 83 */
            this.myArrayList.ensureCapacity(length);
            /* 84 */
            for (int i = 0; i < length; i++) {
                /* 85 */
                put(JSONObject.wrap(Array.get(array, i)));
            }
        } else {
            /* 88 */
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        }
    }

    public Iterator<Object> iterator() {
        /* 94 */
        return this.myArrayList.iterator();
    }

    public Object get(int index) throws JSONException {
        /* 98 */
        Object object = opt(index);
        /* 99 */
        if (object == null) {
            /* 100 */
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        /* 102 */
        return object;
    }

    public boolean getBoolean(int index) throws JSONException {
        /* 106 */
        Object object = get(index);
        /* 107 */
        if (object.equals(Boolean.FALSE) || (object instanceof String && ((String) object)

                /* 109 */.equalsIgnoreCase("false")))
            /* 110 */ return false;
        /* 111 */
        if (object.equals(Boolean.TRUE) || (object instanceof String && ((String) object)

                /* 113 */.equalsIgnoreCase("true"))) {
            /* 114 */
            return true;
        }
        /* 116 */
        throw new JSONException("JSONArray[" + index + "] is not a boolean.");
    }

    public double getDouble(int index) throws JSONException {
        /* 120 */
        Object object = get(index);
        try {
            /* 122 */
            return (object instanceof Number) ? ((Number) object).doubleValue() :
                    /* 123 */ Double.parseDouble((String) object);
            /* 124 */
        } catch (Exception e) {
            /* 125 */
            throw new JSONException("JSONArray[" + index + "] is not a number.", e);
        }
    }

    public float getFloat(int index) throws JSONException {
        /* 130 */
        Object object = get(index);
        try {
            /* 132 */
            return (object instanceof Number) ? ((Number) object).floatValue() :
                    /* 133 */ Float.parseFloat(object.toString());
            /* 134 */
        } catch (Exception e) {
            /* 135 */
            throw new JSONException("JSONArray[" + index + "] is not a number.", e);
        }
    }

    public Number getNumber(int index) throws JSONException {
        /* 141 */
        Object object = get(index);
        try {
            /* 143 */
            if (object instanceof Number) {
                /* 144 */
                return (Number) object;
            }
            /* 146 */
            return JSONObject.stringToNumber(object.toString());
            /* 147 */
        } catch (Exception e) {
            /* 148 */
            throw new JSONException("JSONArray[" + index + "] is not a number.", e);
        }
    }

    public <E extends Enum<E>> E getEnum(Class<E> clazz, int index) throws JSONException {
        /* 153 */
        E val = optEnum(clazz, index);
        /* 154 */
        if (val == null) {

            /* 158 */
            throw new JSONException(
                    "JSONArray[" + index + "] is not an enum of type " + JSONObject.quote(clazz.getSimpleName()) + ".");
        }
        /* 160 */
        return val;
    }

    public BigDecimal getBigDecimal(int index) throws JSONException {
        /* 164 */
        Object object = get(index);
        try {
            /* 166 */
            return new BigDecimal(object.toString());
            /* 167 */
        } catch (Exception e) {
            /* 168 */
            throw new JSONException("JSONArray[" + index + "] could not convert to BigDecimal.", e);
        }
    }

    public BigInteger getBigInteger(int index) throws JSONException {
        /* 174 */
        Object object = get(index);
        try {
            /* 176 */
            return new BigInteger(object.toString());
            /* 177 */
        } catch (Exception e) {
            /* 178 */
            throw new JSONException("JSONArray[" + index + "] could not convert to BigInteger.", e);
        }
    }

    public int getInt(int index) throws JSONException {
        /* 184 */
        Object object = get(index);
        try {
            /* 186 */
            return (object instanceof Number) ? ((Number) object).intValue() :
                    /* 187 */ Integer.parseInt((String) object);
            /* 188 */
        } catch (Exception e) {
            /* 189 */
            throw new JSONException("JSONArray[" + index + "] is not a number.", e);
        }
    }

    public JSONArray getJSONArray(int index) throws JSONException {
        /* 194 */
        Object object = get(index);
        /* 195 */
        if (object instanceof JSONArray) {
            /* 196 */
            return (JSONArray) object;
        }
        /* 198 */
        throw new JSONException("JSONArray[" + index + "] is not a JSONArray.");
    }

    public JSONObject getJSONObject(int index) throws JSONException {
        /* 202 */
        Object object = get(index);
        /* 203 */
        if (object instanceof JSONObject) {
            /* 204 */
            return (JSONObject) object;
        }
        /* 206 */
        throw new JSONException("JSONArray[" + index + "] is not a JSONObject.");
    }

    public long getLong(int index) throws JSONException {
        /* 210 */
        Object object = get(index);
        try {
            /* 212 */
            return (object instanceof Number) ? ((Number) object).longValue() :
                    /* 213 */ Long.parseLong((String) object);
            /* 214 */
        } catch (Exception e) {
            /* 215 */
            throw new JSONException("JSONArray[" + index + "] is not a number.", e);
        }
    }

    public String getString(int index) throws JSONException {
        /* 220 */
        Object object = get(index);
        /* 221 */
        if (object instanceof String) {
            /* 222 */
            return (String) object;
        }
        /* 224 */
        throw new JSONException("JSONArray[" + index + "] not a string.");
    }

    public boolean isNull(int index) {
        /* 228 */
        return JSONObject.NULL.equals(opt(index));
    }

    public String join(String separator) throws JSONException {
        /* 232 */
        int len = length();
        /* 233 */
        StringBuilder sb = new StringBuilder();

        /* 235 */
        for (int i = 0; i < len; i++) {
            /* 236 */
            if (i > 0) {
                /* 237 */
                sb.append(separator);
            }
            /* 239 */
            sb.append(JSONObject.valueToString(this.myArrayList.get(i)));
        }
        /* 241 */
        return sb.toString();
    }

    public int length() {
        /* 245 */
        return this.myArrayList.size();
    }

    public Object opt(int index) {
        /* 249 */
        return (index < 0 || index >= length()) ? null
                : this.myArrayList
                /* 250 */.get(index);
    }

    public boolean optBoolean(int index) {
        /* 254 */
        return optBoolean(index, false);
    }

    public boolean optBoolean(int index, boolean defaultValue) {
        try {
            /* 259 */
            return getBoolean(index);
            /* 260 */
        } catch (Exception e) {
            /* 261 */
            return defaultValue;
        }
    }

    public double optDouble(int index) {
        /* 266 */
        return optDouble(index, Double.NaN);
    }

    public double optDouble(int index, double defaultValue) {
        /* 270 */
        Object val = opt(index);
        /* 271 */
        if (JSONObject.NULL.equals(val)) {
            /* 272 */
            return defaultValue;
        }
        /* 274 */
        if (val instanceof Number) {
            /* 275 */
            return ((Number) val).doubleValue();
        }
        /* 277 */
        if (val instanceof String) {
            try {
                /* 279 */
                return Double.parseDouble((String) val);
                /* 280 */
            } catch (Exception e) {
                /* 281 */
                return defaultValue;
            }
        }
        /* 284 */
        return defaultValue;
    }

    public float optFloat(int index) {
        /* 288 */
        return optFloat(index, Float.NaN);
    }

    public float optFloat(int index, float defaultValue) {
        /* 292 */
        Object val = opt(index);
        /* 293 */
        if (JSONObject.NULL.equals(val)) {
            /* 294 */
            return defaultValue;
        }
        /* 296 */
        if (val instanceof Number) {
            /* 297 */
            return ((Number) val).floatValue();
        }
        /* 299 */
        if (val instanceof String) {
            try {
                /* 301 */
                return Float.parseFloat((String) val);
                /* 302 */
            } catch (Exception e) {
                /* 303 */
                return defaultValue;
            }
        }
        /* 306 */
        return defaultValue;
    }

    public int optInt(int index) {
        /* 310 */
        return optInt(index, 0);
    }

    public int optInt(int index, int defaultValue) {
        /* 314 */
        Object val = opt(index);
        /* 315 */
        if (JSONObject.NULL.equals(val)) {
            /* 316 */
            return defaultValue;
        }
        /* 318 */
        if (val instanceof Number) {
            /* 319 */
            return ((Number) val).intValue();
        }

        /* 322 */
        if (val instanceof String) {
            try {
                /* 324 */
                return (new BigDecimal(val.toString())).intValue();
                /* 325 */
            } catch (Exception e) {
                /* 326 */
                return defaultValue;
            }
        }
        /* 329 */
        return defaultValue;
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, int index) {
        /* 333 */
        return optEnum(clazz, index, null);
    }

    public <E extends Enum<E>> E optEnum(Class<E> clazz, int index, E defaultValue) {
        try {
            /* 338 */
            Object val = opt(index);
            /* 339 */
            if (JSONObject.NULL.equals(val)) {
                /* 340 */
                return defaultValue;
            }
            /* 342 */
            if (clazz.isAssignableFrom(val.getClass())) {

                /* 345 */
                return (E) val;
            }

            /* 348 */
            return Enum.valueOf(clazz, val.toString());
            /* 349 */
        } catch (IllegalArgumentException e) {
            /* 350 */
            return defaultValue;
            /* 351 */
        } catch (NullPointerException e) {
            /* 352 */
            return defaultValue;
        }
    }

    public BigInteger optBigInteger(int index, BigInteger defaultValue) {
        /* 358 */
        Object val = opt(index);
        /* 359 */
        if (JSONObject.NULL.equals(val)) {
            /* 360 */
            return defaultValue;
        }
        /* 362 */
        if (val instanceof BigInteger) {
            /* 363 */
            return (BigInteger) val;
        }
        /* 365 */
        if (val instanceof BigDecimal) {
            /* 366 */
            return ((BigDecimal) val).toBigInteger();
        }
        /* 368 */
        if (val instanceof Double || val instanceof Float) {
            /* 369 */
            return (new BigDecimal(((Number) val).doubleValue())).toBigInteger();
        }
        /* 371 */
        if (val instanceof Long || val instanceof Integer || val instanceof Short || val instanceof Byte) {
            /* 373 */
            return BigInteger.valueOf(((Number) val).longValue());
        }
        try {
            /* 376 */
            String valStr = val.toString();
            /* 377 */
            if (JSONObject.isDecimalNotation(valStr)) {
                /* 378 */
                return (new BigDecimal(valStr)).toBigInteger();
            }
            /* 380 */
            return new BigInteger(valStr);
            /* 381 */
        } catch (Exception e) {
            /* 382 */
            return defaultValue;
        }
    }

    public BigDecimal optBigDecimal(int index, BigDecimal defaultValue) {
        /* 387 */
        Object val = opt(index);
        /* 388 */
        if (JSONObject.NULL.equals(val)) {
            /* 389 */
            return defaultValue;
        }
        /* 391 */
        if (val instanceof BigDecimal) {
            /* 392 */
            return (BigDecimal) val;
        }
        /* 394 */
        if (val instanceof BigInteger) {
            /* 395 */
            return new BigDecimal((BigInteger) val);
        }
        /* 397 */
        if (val instanceof Double || val instanceof Float) {
            /* 398 */
            return new BigDecimal(((Number) val).doubleValue());
        }
        /* 400 */
        if (val instanceof Long || val instanceof Integer || val instanceof Short || val instanceof Byte) {
            /* 402 */
            return new BigDecimal(((Number) val).longValue());
        }
        try {
            /* 405 */
            return new BigDecimal(val.toString());
            /* 406 */
        } catch (Exception e) {
            /* 407 */
            return defaultValue;
        }
    }

    public JSONArray optJSONArray(int index) {
        /* 412 */
        Object o = opt(index);
        /* 413 */
        return (o instanceof JSONArray) ? (JSONArray) o : null;
    }

    public JSONObject optJSONObject(int index) {
        /* 417 */
        Object o = opt(index);
        /* 418 */
        return (o instanceof JSONObject) ? (JSONObject) o : null;
    }

    public long optLong(int index) {
        /* 422 */
        return optLong(index, 0L);
    }

    public long optLong(int index, long defaultValue) {
        /* 426 */
        Object val = opt(index);
        /* 427 */
        if (JSONObject.NULL.equals(val)) {
            /* 428 */
            return defaultValue;
        }
        /* 430 */
        if (val instanceof Number) {
            /* 431 */
            return ((Number) val).longValue();
        }

        /* 434 */
        if (val instanceof String) {
            try {
                /* 436 */
                return (new BigDecimal(val.toString())).longValue();
                /* 437 */
            } catch (Exception e) {
                /* 438 */
                return defaultValue;
            }
        }
        /* 441 */
        return defaultValue;
    }

    public Number optNumber(int index) {
        /* 445 */
        return optNumber(index, null);
    }

    public Number optNumber(int index, Number defaultValue) {
        /* 449 */
        Object val = opt(index);
        /* 450 */
        if (JSONObject.NULL.equals(val)) {
            /* 451 */
            return defaultValue;
        }
        /* 453 */
        if (val instanceof Number) {
            /* 454 */
            return (Number) val;
        }

        /* 457 */
        if (val instanceof String) {
            try {
                /* 459 */
                return JSONObject.stringToNumber((String) val);
                /* 460 */
            } catch (Exception e) {
                /* 461 */
                return defaultValue;
            }
        }
        /* 464 */
        return defaultValue;
    }

    public String optString(int index) {
        /* 468 */
        return optString(index, "");
    }

    public String optString(int index, String defaultValue) {
        /* 472 */
        Object object = opt(index);
        /* 473 */
        return JSONObject.NULL.equals(object) ? defaultValue
                : object
                /* 474 */.toString();
    }

    public JSONArray put(boolean value) {
        /* 478 */
        return put(value ? Boolean.TRUE : Boolean.FALSE);
    }

    public JSONArray put(Collection<?> value) {
        /* 482 */
        return put(new JSONArray(value));
    }

    public JSONArray put(double value) throws JSONException {
        /* 486 */
        return put(Double.valueOf(value));
    }

    public JSONArray put(float value) throws JSONException {
        /* 490 */
        return put(Float.valueOf(value));
    }

    public JSONArray put(int value) {
        /* 494 */
        return put(Integer.valueOf(value));
    }

    public JSONArray put(long value) {
        /* 498 */
        return put(Long.valueOf(value));
    }

    public JSONArray put(Map<?, ?> value) {
        /* 502 */
        return put(new JSONObject(value));
    }

    public JSONArray put(Object value) {
        /* 506 */
        JSONObject.testValidity(value);
        /* 507 */
        this.myArrayList.add(value);
        /* 508 */
        return this;
    }

    public JSONArray put(int index, boolean value) throws JSONException {
        /* 512 */
        return put(index, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public JSONArray put(int index, Collection<?> value) throws JSONException {
        /* 516 */
        return put(index, new JSONArray(value));
    }

    public JSONArray put(int index, double value) throws JSONException {
        /* 520 */
        return put(index, Double.valueOf(value));
    }

    public JSONArray put(int index, float value) throws JSONException {
        /* 524 */
        return put(index, Float.valueOf(value));
    }

    public JSONArray put(int index, int value) throws JSONException {
        /* 528 */
        return put(index, Integer.valueOf(value));
    }

    public JSONArray put(int index, long value) throws JSONException {
        /* 532 */
        return put(index, Long.valueOf(value));
    }

    public JSONArray put(int index, Map<?, ?> value) throws JSONException {
        /* 536 */
        put(index, new JSONObject(value));
        /* 537 */
        return this;
    }

    public JSONArray put(int index, Object value) throws JSONException {
        /* 541 */
        if (index < 0) {
            /* 542 */
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        /* 544 */
        if (index < length()) {
            /* 545 */
            JSONObject.testValidity(value);
            /* 546 */
            this.myArrayList.set(index, value);
            /* 547 */
            return this;
        }
        /* 549 */
        if (index == length()) {
            /* 551 */
            return put(value);
        }

        /* 555 */
        this.myArrayList.ensureCapacity(index + 1);
        /* 556 */
        while (index != length()) {
            /* 558 */
            this.myArrayList.add(JSONObject.NULL);
        }
        /* 560 */
        return put(value);
    }

    public Object query(String jsonPointer) {
        /* 564 */
        return query(new JSONPointer(jsonPointer));
    }

    public Object query(JSONPointer jsonPointer) {
        /* 568 */
        return jsonPointer.queryFrom(this);
    }

    public Object optQuery(String jsonPointer) {
        /* 572 */
        return optQuery(new JSONPointer(jsonPointer));
    }

    public Object optQuery(JSONPointer jsonPointer) {
        try {
            /* 577 */
            return jsonPointer.queryFrom(this);
            /* 578 */
        } catch (JSONPointerException e) {
            /* 579 */
            return null;
        }
    }

    public Object remove(int index) {
        /* 584 */
        return (index >= 0 && index < length()) ? this.myArrayList
                /* 585 */.remove(index) : null;
    }

    public boolean similar(Object other) {
        /* 590 */
        if (!(other instanceof JSONArray)) {
            /* 591 */
            return false;
        }
        /* 593 */
        int len = length();
        /* 594 */
        if (len != ((JSONArray) other).length()) {
            /* 595 */
            return false;
        }
        /* 597 */
        for (int i = 0; i < len; i++) {
            /* 598 */
            Object valueThis = this.myArrayList.get(i);
            /* 599 */
            Object valueOther = ((JSONArray) other).myArrayList.get(i);
            /* 600 */
            if (valueThis != valueOther) {

                /* 603 */
                if (valueThis == null) {
                    /* 604 */
                    return false;
                }
                /* 606 */
                if (valueThis instanceof JSONObject) {
                    /* 607 */
                    if (!((JSONObject) valueThis).similar(valueOther)) {
                        /* 608 */
                        return false;
                    }
                    /* 610 */
                } else if (valueThis instanceof JSONArray) {
                    /* 611 */
                    if (!((JSONArray) valueThis).similar(valueOther)) {
                        /* 612 */
                        return false;
                    }
                    /* 614 */
                } else if (!valueThis.equals(valueOther)) {
                    /* 615 */
                    return false;
                }
            }
            /* 618 */
        }
        return true;
    }

    public JSONObject toJSONObject(JSONArray names) throws JSONException {
        /* 622 */
        if (names == null || names.isEmpty() || isEmpty()) {
            /* 623 */
            return null;
        }
        /* 625 */
        JSONObject jo = new JSONObject(names.length());
        /* 626 */
        for (int i = 0; i < names.length(); i++) {
            /* 627 */
            jo.put(names.getString(i), opt(i));
        }
        /* 629 */
        return jo;
    }

    public String toString() {
        try {
            /* 635 */
            return toString(0);
            /* 636 */
        } catch (Exception e) {
            /* 637 */
            return null;
        }
    }

    public String toString(int indentFactor) throws JSONException {
        /* 642 */
        StringWriter sw = new StringWriter();
        /* 643 */
        synchronized (sw.getBuffer()) {
            /* 644 */
            return write(sw, indentFactor, 0).toString();
        }
    }

    public Writer write(Writer writer) throws JSONException {
        /* 649 */
        return write(writer, 0, 0);
    }

    public Writer write(Writer writer, int indentFactor, int indent) throws JSONException {
        try {
            /* 654 */
            boolean commanate = false;
            /* 655 */
            int length = length();
            /* 656 */
            writer.write(91);

            /* 658 */
            if (length == 1) {
                try {
                    /* 660 */
                    JSONObject.writeValue(writer, this.myArrayList.get(0), indentFactor, indent);
                }
                /* 662 */ catch (Exception e) {
                    /* 663 */
                    throw new JSONException("Unable to write JSONArray value at index: 0", e);
                }
                /* 665 */
            } else if (length != 0) {
                /* 666 */
                int newindent = indent + indentFactor;

                /* 668 */
                for (int i = 0; i < length; i++) {
                    /* 669 */
                    if (commanate) {
                        /* 670 */
                        writer.write(44);
                    }
                    /* 672 */
                    if (indentFactor > 0) {
                        /* 673 */
                        writer.write(10);
                    }
                    /* 675 */
                    JSONObject.indent(writer, newindent);
                    try {
                        /* 677 */
                        JSONObject.writeValue(writer, this.myArrayList.get(i), indentFactor, newindent);
                    }
                    /* 679 */ catch (Exception e) {
                        /* 680 */
                        throw new JSONException("Unable to write JSONArray value at index: " + i, e);
                    }
                    /* 682 */
                    commanate = true;
                }
                /* 684 */
                if (indentFactor > 0) {
                    /* 685 */
                    writer.write(10);
                }
                /* 687 */
                JSONObject.indent(writer, indent);
            }
            /* 689 */
            writer.write(93);
            /* 690 */
            return writer;
            /* 691 */
        } catch (IOException e) {
            /* 692 */
            throw new JSONException(e);
        }
    }

    public List<Object> toList() {
        /* 697 */
        List<Object> results = new ArrayList(this.myArrayList.size());
        /* 698 */
        for (Object element : this.myArrayList) {
            /* 699 */
            if (element == null || JSONObject.NULL.equals(element)) {
                /* 700 */
                results.add(null);
                continue;
                /* 701 */
            }
            if (element instanceof JSONArray) {
                /* 702 */
                results.add(((JSONArray) element).toList());
                continue;
                /* 703 */
            }
            if (element instanceof JSONObject) {
                /* 704 */
                results.add(((JSONObject) element).toMap());
                continue;
            }
            /* 706 */
            results.add(element);
        }

        /* 709 */
        return results;
    }

    public String[] toStringArray() {
        /* 713 */
        List<String> results = new ArrayList<>(this.myArrayList.size());
        /* 714 */
        for (Object element : this.myArrayList) {
            /* 715 */
            assert element instanceof String;
            /* 716 */
            results.add((String) element);
        }
        /* 718 */
        return results.<String>toArray(new String[length()]);
    }

    public int[] toIntArray() {
        /* 722 */
        int[] intArray = new int[length()];
        /* 723 */
        for (int i = 0; i < this.myArrayList.size(); i++) {
            /* 724 */
            Object element = this.myArrayList.get(i);
            /* 725 */
            assert element instanceof Number;
            /* 726 */
            intArray[i] = ((Number) element).intValue();
        }
        /* 728 */
        return intArray;
    }

    public boolean isEmpty() {
        /* 732 */
        return this.myArrayList.isEmpty();
    }
}
