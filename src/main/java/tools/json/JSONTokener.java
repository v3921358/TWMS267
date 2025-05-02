package tools.json;

import java.io.*;

final class JSONTokener {
    private long character;
    private boolean eof;
    private long index;
    private long line;
    private char previous;
    private final Reader reader;
    private boolean usePrevious;
    private long characterPreviousLine;

    public JSONTokener(Reader reader) {
        /* 46 */
        this.reader = reader.markSupported() ? reader : new BufferedReader(reader);

        /* 49 */
        this.eof = false;
        /* 50 */
        this.usePrevious = false;
        /* 51 */
        this.previous = Character.MIN_VALUE;
        /* 52 */
        this.index = 0L;
        /* 53 */
        this.character = 1L;
        /* 54 */
        this.characterPreviousLine = 0L;
        /* 55 */
        this.line = 1L;
    }

    public JSONTokener(InputStream inputStream) {
        /* 65 */
        this(new InputStreamReader(inputStream));
    }

    public JSONTokener(String s) {
        /* 75 */
        this(new StringReader(s));
    }

    public void back() throws JSONException {
        /* 88 */
        if (this.usePrevious || this.index <= 0L) {
            /* 89 */
            throw new JSONException("Stepping back two steps is not supported");
        }
        /* 91 */
        decrementIndexes();
        /* 92 */
        this.usePrevious = true;
        /* 93 */
        this.eof = false;
    }

    private void decrementIndexes() {
        /* 100 */
        this.index--;
        /* 101 */
        if (this.previous == '\r' || this.previous == '\n') {
            /* 102 */
            this.line--;
            /* 103 */
            this.character = this.characterPreviousLine;
            /* 104 */
        } else if (this.character > 0L) {
            /* 105 */
            this.character--;
        }
    }

    public static int dehexchar(char c) {
        /* 117 */
        if (c >= '0' && c <= '9') {
            /* 118 */
            return c - 48;
        }
        /* 120 */
        if (c >= 'A' && c <= 'F') {
            /* 121 */
            return c - 55;
        }
        /* 123 */
        if (c >= 'a' && c <= 'f') {
            /* 124 */
            return c - 87;
        }
        /* 126 */
        return -1;
    }

    public boolean end() {
        /* 135 */
        return (this.eof && !this.usePrevious);
    }

    public boolean more() throws JSONException {
        /* 148 */
        if (this.usePrevious) {
            /* 149 */
            return true;
        }
        try {
            /* 152 */
            this.reader.mark(1);
            /* 153 */
        } catch (IOException e) {
            /* 154 */
            throw new JSONException("Unable to preserve stream position", e);
        }

        try {
            /* 158 */
            if (this.reader.read() <= 0) {
                /* 159 */
                this.eof = true;
                /* 160 */
                return false;
            }
            /* 162 */
            this.reader.reset();
            /* 163 */
        } catch (IOException e) {
            /* 164 */
            throw new JSONException("Unable to read the next character from the stream", e);
        }
        /* 166 */
        return true;
    }

    public char next() throws JSONException {
        int c;
        /* 178 */
        if (this.usePrevious) {
            /* 179 */
            this.usePrevious = false;
            /* 180 */
            c = this.previous;
        } else {
            try {
                /* 183 */
                c = this.reader.read();
                /* 184 */
            } catch (IOException exception) {
                /* 185 */
                throw new JSONException(exception);
            }
        }
        /* 188 */
        if (c <= 0) {
            /* 189 */
            this.eof = true;
            /* 190 */
            return Character.MIN_VALUE;
        }
        /* 192 */
        incrementIndexes(c);
        /* 193 */
        this.previous = (char) c;
        /* 194 */
        return this.previous;
    }

    private void incrementIndexes(int c) {
        /* 204 */
        if (c > 0) {
            /* 205 */
            this.index++;
            /* 206 */
            if (c == 13) {
                /* 207 */
                this.line++;
                /* 208 */
                this.characterPreviousLine = this.character;
                /* 209 */
                this.character = 0L;
                /* 210 */
            } else if (c == 10) {
                /* 211 */
                if (this.previous != '\r') {
                    /* 212 */
                    this.line++;
                    /* 213 */
                    this.characterPreviousLine = this.character;
                }
                /* 215 */
                this.character = 0L;
            } else {
                /* 217 */
                this.character++;
            }
        }
    }

    public char next(char c) throws JSONException {
        /* 231 */
        char n = next();
        /* 232 */
        if (n != c) {
            /* 233 */
            if (n > '\000') {
                /* 234 */
                throw syntaxError("Expected '" + c + "' and instead saw '" + n + "'");
            }

            /* 237 */
            throw syntaxError("Expected '" + c + "' and instead saw ''");
        }
        /* 239 */
        return n;
    }

    public String next(int n) throws JSONException {
        /* 252 */
        if (n == 0) {
            /* 253 */
            return "";
        }

        /* 256 */
        char[] chars = new char[n];
        /* 257 */
        int pos = 0;

        /* 259 */
        while (pos < n) {
            /* 260 */
            chars[pos] = next();
            /* 261 */
            if (end()) {
                /* 262 */
                throw syntaxError("Substring bounds error");
            }
            /* 264 */
            pos++;
        }
        /* 266 */
        return new String(chars);
    }

    public char nextClean() throws JSONException {
        char c;
        do {
            /* 278 */
            c = next();
            /* 279 */
        } while (c != '\000' && c <= ' ');
        /* 280 */
        return c;
    }

    public String nextString(char quote) throws JSONException {
        /* 300 */
        StringBuilder sb = new StringBuilder();
        while (true) {
            /* 302 */
            char c = next();
            /* 303 */
            switch (c) {
                case '\000':
                case '\n':
                case '\r':
                    /* 307 */
                    throw syntaxError("Unterminated string");
                case '\\':
                    /* 309 */
                    c = next();
                    /* 310 */
                    switch (c) {
                        case 'b':
                            /* 312 */
                            sb.append('\b');
                            continue;
                        case 't':
                            /* 315 */
                            sb.append('\t');
                            continue;
                        case 'n':
                            /* 318 */
                            sb.append('\n');
                            continue;
                        case 'f':
                            /* 321 */
                            sb.append('\f');
                            continue;
                        case 'r':
                            /* 324 */
                            sb.append('\r');
                            continue;
                        case 'u':
                            try {
                                /* 328 */
                                sb.append((char) Integer.parseInt(next(4), 16));
                                /* 329 */
                            } catch (NumberFormatException e) {
                                /* 330 */
                                throw syntaxError("Illegal escape.", e);
                            }
                            continue;
                        case '"':
                        case '\'':
                        case '/':
                        case '\\':
                            /* 337 */
                            sb.append(c);
                            continue;
                    }
                    /* 340 */
                    throw syntaxError("Illegal escape.");
            }

            /* 344 */
            if (c == quote) {
                /* 345 */
                return sb.toString();
            }
            /* 347 */
            sb.append(c);
        }
    }

    public String nextTo(char delimiter) throws JSONException {
        /* 363 */
        StringBuilder sb = new StringBuilder();
        while (true) {
            /* 365 */
            char c = next();
            /* 366 */
            if (c == delimiter || c == '\000' || c == '\n' || c == '\r') {
                /* 367 */
                if (c != '\000') {
                    /* 368 */
                    back();
                }
                /* 370 */
                return sb.toString().trim();
            }
            /* 372 */
            sb.append(c);
        }
    }

    public String nextTo(String delimiters) throws JSONException {
        /* 388 */
        StringBuilder sb = new StringBuilder();
        while (true) {
            /* 390 */
            char c = next();
            /* 391 */
            if (delimiters.indexOf(c) >= 0 || c == '\000' || c == '\n' || c == '\r') {

                /* 393 */
                if (c != '\000') {
                    /* 394 */
                    back();
                }
                /* 396 */
                return sb.toString().trim();
            }
            /* 398 */
            sb.append(c);
        }
    }

    public Object nextValue() throws JSONException {
        /* 411 */
        char c = nextClean();

        /* 414 */
        switch (c) {
            case '"':
            case '\'':
                /* 417 */
                return nextString(c);
            case '{':
                /* 419 */
                back();
                /* 420 */
                return new JSONObject(this);
            case '[':
                /* 422 */
                back();
                /* 423 */
                return new JSONArray(this);
        }

        /* 435 */
        StringBuilder sb = new StringBuilder();
        /* 436 */
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            /* 437 */
            sb.append(c);
            /* 438 */
            c = next();
        }
        /* 440 */
        back();

        /* 442 */
        String string = sb.toString().trim();
        /* 443 */
        if ("".equals(string)) {
            /* 444 */
            throw syntaxError("Missing value");
        }
        /* 446 */
        return JSONObject.stringToValue(string);
    }

    public char skipTo(char to) throws JSONException {

        /* 453 */
        try {
            long startIndex = this.index;
            /* 454 */
            long startCharacter = this.character;
            /* 455 */
            long startLine = this.line;
            /* 456 */
            this.reader.mark(1000000);
            while (true) {
                /* 458 */
                char c = next();
                /* 459 */
                if (c == '\000') {

                    /* 463 */
                    this.reader.reset();
                    /* 464 */
                    this.index = startIndex;
                    /* 465 */
                    this.character = startCharacter;
                    /* 466 */
                    this.line = startLine;
                    /* 467 */
                    return Character.MIN_VALUE;
                }
                /* 469 */
                if (c == to) {
                    /* 470 */
                    this.reader.mark(1);

                    /* 474 */
                    back();
                    /* 475 */
                    return c;
                }
            }
        } catch (IOException exception) {
            throw new JSONException(exception);
        }
        /* 479 */
    }

    public JSONException syntaxError(String message) {
        return new JSONException(message + this);
    }

    public JSONException syntaxError(String message, Throwable causedBy) {
        /* 483 */
        return new JSONException(message + this, causedBy);
    }

    public String toString() {
        /* 488 */
        return " at " + this.index + " [character " + this.character + " line " + this.line + "]";
    }
}
