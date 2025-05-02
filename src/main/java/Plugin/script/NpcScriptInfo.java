package Plugin.script;

import Client.inventory.Item;
import Config.constants.enums.NpcMessageType;
import Config.constants.enums.ScriptParam;

import java.util.List;

public class NpcScriptInfo {

    private List<Item> items;
    private int[] options, options2;
    private byte speakerType = 3; // ?
    private int overrideSpeakerTemplateID = 0;
    private short param;
    private byte color = 0;
    private String text;
    private NpcMessageType messageType;
    private String[] images;
    private int srcBeauty;
    private int drtBeauty;
    private int srcBeauty2;
    private int drtBeauty2;
    private long min;
    private long max;
    private int col;
    private int line;
    private int itemID;
    private String defaultText;
    private long defaultNumber;
    private byte type;
    private int time;
    private String title;
    private String problemText;
    private String hintText;
    private int quizType;
    private int answer;
    private int correctAnswers;
    private int remaining;
    private byte secondLookValue;
    private int dlgType;
    private int defaultSelect;
    private String[] selectText;
    private int objectID;
    private int templateID;
    private int innerOverrideSpeakerTemplateID;
    private boolean prevPossible, nextPossible;
    private int delay;
    private int unk;
    private boolean bUnk;
    private int index = 0;

    public NpcScriptInfo deepCopy() {
        NpcScriptInfo nsi = new NpcScriptInfo();
        nsi.items = items;
        if (options != null) {
            nsi.options = new int[options.length];
            System.arraycopy(options, 0, nsi.options, 0, options.length);
        }
        if (options2 != null) {
            nsi.options2 = new int[options2.length];
            System.arraycopy(options2, 0, nsi.options2, 0, options2.length);
        }
        nsi.speakerType = speakerType;
        nsi.overrideSpeakerTemplateID = overrideSpeakerTemplateID;
        nsi.param = param;
        nsi.color = color;
        nsi.text = text;
        nsi.messageType = messageType;
        if (images != null) {
            nsi.images = images.clone();
        }
        nsi.srcBeauty = srcBeauty;
        nsi.drtBeauty = drtBeauty;
        nsi.srcBeauty2 = srcBeauty2;
        nsi.drtBeauty2 = drtBeauty2;
        nsi.min = min;
        nsi.max = max;
        nsi.col = col;
        nsi.line = line;
        nsi.itemID = itemID;
        nsi.defaultText = defaultText;
        nsi.defaultNumber = defaultNumber;
        nsi.type = type;
        nsi.time = time;
        nsi.title = title;
        nsi.problemText = problemText;
        nsi.hintText = hintText;
        nsi.quizType = quizType;
        nsi.answer = answer;
        nsi.correctAnswers = correctAnswers;
        nsi.remaining = remaining;
        nsi.secondLookValue = secondLookValue;
        nsi.dlgType = dlgType;
        nsi.defaultSelect = defaultSelect;
        nsi.selectText = selectText;
        nsi.objectID = objectID;
        nsi.templateID = templateID;
        nsi.prevPossible = prevPossible;
        nsi.nextPossible = nextPossible;
        nsi.delay = delay;
        nsi.unk = unk;
        nsi.bUnk = bUnk;
        nsi.index = index;
        return nsi;
    }

    public byte getSpeakerType() {
        return speakerType;
    }

    public void setSpeakerType(int speakerType) {
        this.speakerType = (byte) speakerType;
    }

    public int getOverrideSpeakerTemplateID() {
        return overrideSpeakerTemplateID;
    }

    public void setOverrideSpeakerTemplateID(int overrideSpeakerTemplateID) {
        this.overrideSpeakerTemplateID = overrideSpeakerTemplateID;
    }

    public short getParam() {
        return param;
    }

    public void addParam(ScriptParam param) {
        addParam(param.getValue());
    }

    public void addParam(int param) {
        this.param |= (short) param;
    }

    public void removeParam(ScriptParam param) {
        removeParam(param.getValue());
    }

    public void removeParam(int param) {
        this.param &= (short) ~param;
    }

    public void setParam(int param) {
        this.param = (short) param;
    }

    public byte getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = (byte) color;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setMessageType(NpcMessageType messageType) {
        this.messageType = messageType;
    }

    public NpcMessageType getMessageType() {
        return messageType;
    }

    public void setImages(String[] images) {
        this.images = images;
    }

    public String[] getImages() {
        return images;
    }

    public void setSrcBeauty(int srcBeauty) {
        this.srcBeauty = srcBeauty;
    }

    public int getSrcBeauty() {
        return srcBeauty;
    }

    public void setDrtBeauty(int drtBeauty) {
        this.drtBeauty = drtBeauty;
    }

    public int getDrtBeauty() {
        return drtBeauty;
    }

    public void setSrcBeauty2(int srcBeauty) {
        this.srcBeauty2 = srcBeauty;
    }

    public int getSrcBeauty2() {
        return srcBeauty2;
    }

    public void setDrtBeauty2(int drtBeauty) {
        this.drtBeauty2 = drtBeauty;
    }

    public int getDrtBeauty2() {
        return drtBeauty2;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getMin() {
        return min;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public long getMax() {
        return max;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getCol() {
        return col;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getItemID() {
        return itemID;
    }

    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }

    public String getDefaultText() {
        return defaultText;
    }

    public long getDefaultNumber() {
        return defaultNumber;
    }

    public void setDefaultNumber(long defaultNumber) {
        this.defaultNumber = defaultNumber;
    }

    public byte getType() {
        return type;
    }

    public void setType(int type) {
        this.type = (byte) type;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProblemText() {
        return problemText;
    }

    public void setProblemText(String problemText) {
        this.problemText = problemText;
    }

    public String getHintText() {
        return hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    public int getQuizType() {
        return quizType;
    }

    public void setQuizType(int quizType) {
        this.quizType = quizType;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public byte getSecondLookValue() {
        return secondLookValue;
    }

    public void setSecondLookValue(int vale) {
        this.secondLookValue = (byte) vale;
    }

    public int getDlgType() {
        return dlgType;
    }

    public void setDlgType(int dlgType) {
        this.dlgType = dlgType;
    }

    public void setDefaultSelect(int defaultSelect) {
        this.defaultSelect = defaultSelect;
    }

    public int getDefaultSelect() {
        return defaultSelect;
    }

    public void setSelectText(String[] selectText) {
        this.selectText = selectText;
    }

    public String[] getSelectText() {
        return selectText;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setOptions(int[] options) {
        this.options = options;
    }

    public int[] getOptions() {
        return options;
    }

    public void setOptions2(int[] options) {
        this.options2 = options;
    }

    public int[] getOptions2() {
        return options2;
    }

    public boolean hasParam(ScriptParam param) {
        return param.check(getParam());
    }

    public int getObjectID() {
        return objectID;
    }

    public void setObjectID(int objectID) {
        this.objectID = objectID;
    }

    public int getTemplateID() {
        return templateID;
    }

    public void setTemplateID(int templateID) {
        this.templateID = templateID;
    }

    public void setInnerOverrideSpeakerTemplateID(int innerOverrideSpeakerTemplateID) {
        if (innerOverrideSpeakerTemplateID > 0)
            addParam(ScriptParam.OverrideSpeakerID);
        else
            removeParam(ScriptParam.OverrideSpeakerID);
        this.innerOverrideSpeakerTemplateID = innerOverrideSpeakerTemplateID;
    }

    public boolean isPrevPossible() {
        return prevPossible;
    }

    public void setPrevPossible(boolean prevPossible) {
        this.prevPossible = prevPossible;
    }

    public boolean isNextPossible() {
        return nextPossible;
    }

    public void setNextPossible(boolean nextPossible) {
        this.nextPossible = nextPossible;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getUnk() {
        return unk;
    }

    public void setUnk(int unk) {
        this.unk = unk;
    }

    public boolean isUnk() {
        return bUnk;
    }

    public void setBUnk(boolean unk) {
        this.bUnk = unk;
    }

    public int getInnerOverrideSpeakerTemplateID() {
        return innerOverrideSpeakerTemplateID;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}