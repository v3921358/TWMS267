package Config.constants.enums;

public enum Holiday {
    None,
    ChineseNewYear(1, 1, true),
    Halloween(10, 31, false),
    ;

    private int _month, _dayOfMonth;
    private final boolean _isLunar;

    Holiday() {
        _month = 0;
        _dayOfMonth = 0;
        _isLunar = false;
    }

    Holiday(int month, int dayOfMonth, boolean isLunar) {
        _month = month;
        _dayOfMonth = dayOfMonth;
        _isLunar = isLunar;
    }

    public int getMonth() {
        return _month;
    }

    public int getDayOfMonth() {
        return _dayOfMonth;
    }

    public boolean isLunar() {
        return _isLunar;
    }

    public void setMonth(int month) {
        _month = month;
    }

    public void setDayOfMonth(int dayOfMonth) {
        _dayOfMonth = dayOfMonth;
    }
}
