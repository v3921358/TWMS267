package Config.constants.enums;

public enum ScriptParam {
    // 正常
    Normal((short) 0),
    // 無「停止對話」
    NoEsc((short) 0x1),
    // 角色右側顯示
    PlayerAsSpeaker((short) 0x2),
    // NPC右側顯示
    OverrideSpeakerID((short) 0x4),
    // NPC面向右邊
    FlipSpeaker((short) 0x8),
    // 角色面向右邊
    PlayerAsSpeakerFlip((short) 0x10),
    // 下置顯示對話
    BoxChat((short) 0x20),
    NPC_N((short) 0x40),
    LargeBoxChat((short) 0x80),
    Replace((short) 0x100);

    private final short value;

    ScriptParam(final short value) {
        this.value = value;
    }

    public final short getValue() {
        return this.value;
    }

    public final boolean check(int n) {
        return (n & value) != 0;
    }
}