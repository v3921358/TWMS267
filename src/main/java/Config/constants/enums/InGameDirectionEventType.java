package Config.constants.enums;

public enum InGameDirectionEventType {

    ForcedAction(0),
    Delay(2),
    EffectPlay(3),
    ForcedInput(3),
    PatternInputRequest(4),
    CameraMove(5),
    CameraOnCharacter(6),
    CameraZoom(7),
    UNK_226_1(8),
    CameraReleaseFromUserPoint(9),
    VansheeMode(10),
    FaceOff(11),
    Monologue(12),
    MonologueScroll(13),
    AvatarLookSet(14),
    RemoveAdditionalEffect(15),
    UNK_226_2(16),
    UNK_226_3(17),
    ForcedMove(18),
    ForcedFlip(19),
    InputUI(20),
    UNK_226_4(21),
    UNK_226_5(22),
    UNK_226_6(23),
    UNK_226_7(24),
    UNK_226_8(25);

    private final int type;

    InGameDirectionEventType(final int type) {
        this.type = type;
    }

    public final int getType() {
        return this.type;
    }

    public static InGameDirectionEventType getByType(final int type) {
        for (InGameDirectionEventType igdt : values()) {
            if (type == igdt.getType()) {
                return igdt;
            }
        }
        return null;
    }
}
