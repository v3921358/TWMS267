package tools.data;

public interface WritableIntValueHolder {

    short getValue();

    short getCode();

    void setValue(short newval);

    void setValue(Short code);
}
