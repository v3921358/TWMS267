package Client;

import java.util.Arrays;
import java.util.Map;

public class MessageOption {
    private int _objectId;

    public void setObjectId(int value) {
        this._objectId = value;
    }

    public int getObjectId() {
        return this._objectId;
    }

    private int _amount;

    public void setAmount(int value) {
        this._amount = value;
    }

    public int getAmount() {
        return this._amount;
    }

    private String _text;

    public void setText(String value) {
        this._text = value;
    }

    public String getText() {
        return this._text;
    }

    private String _text2;

    public void setText2(String value) {
        this._text2 = value;
    }

    public String getText2() {
        return this._text2;
    }

    private int _combo;

    public void setCombo(int value) {
        this._combo = value;
    }

    public int getCombo() {
        return this._combo;
    }

    private byte _mode;

    public void setMode(int value) {
        this._mode = (byte) value;
    }

    public byte getMode() {
        return this._mode;
    }

    private byte _color;

    public void setColor(int value) {
        this._color = (byte) value;
    }

    public byte getColor() {
        return this._color;
    }

    private boolean _bOnQuest;

    public void setOnQuest(boolean value) {
        this._bOnQuest = value;
    }

    public boolean getOnQuest() {
        return this._bOnQuest;
    }

    private byte _nQuestBonusRate;
    private int _diseaseType;

    public void setDiseaseType(int value) {
        this._diseaseType = value;
    }

    public int getDiseaseType() {
        return _diseaseType;
    }

    private long _expLost;

    public void setExpLost(long value) {
        this._expLost = value;
    }

    public long getExpLost() {
        return _expLost;
    }

    private long _mask;

    public void setMask(long value) {
        this._mask = value;
    }

    public long getMask() {
        return this._mask;
    }

    private int _intExp;

    public void setIntExp(int value) {
        this._intExp = value;
    }

    public long getIntExp() {
        return this._intExp;
    }

    private byte _byteExp;
    private long _longExp;

    public void setLongExp(long value) {
        this._longExp = value;
    }

    public long getLongExp() {
        return this._longExp;
    }

    private long _longGain;

    public void setLongGain(long value) {
        this._longGain = value;
    }

    public long getLongGain() {
        return this._longGain;
    }

    private short _job;

    public void setJob(short value) {
        this._job = value;
    }

    public short getJob() {
        return this._job;
    }

    private int[] _int_data;

    public void setIntegerData(int[] value) {
        this._int_data = Arrays.copyOf(value, value.length);
    }

    public int[] getIntegerData() {
        return this._int_data;
    }

    private Map<MapleExpStat, Object> _expGainData;

    public void setExpGainData(Map<MapleExpStat, Object> value) {
        this._expGainData = value;
    }

    public Map<MapleExpStat, Object> getExpGainData() {
        return this._expGainData;
    }

    private MapleQuestStatus _questStatus;

    public void setQuestStatus(MapleQuestStatus value) {
        this._questStatus = value;
    }

    public MapleQuestStatus getQuestStatus() {
        return this._questStatus;
    }
}
