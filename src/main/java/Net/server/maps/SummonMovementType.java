package Net.server.maps;

public enum SummonMovementType {

    STOP(0), //不會移動
    WALK(1), //飛行跟隨
    WALK_RANDOM(2), //自由移動
    FLY(3), //跟隨並且隨機移動打怪
    FLY_RANDOM(4),
    SMART(5), //gavi only
    FIX_V_MOVE(6),//移動一定距離
    WALK_SMART(7),//移動跟隨
    WALK_CLONE(8),//跟隨移動跟隨攻擊
    FLY_CLONE(9),
    WALK_HANG(10),
    JAGUAR(11),//豹弩
    FLY_JAGUAR(12),
    固定一段距離(13),//固定一段距離
    固定跟隨攻擊(14),//固定跟隨攻擊
    UNKNOWN_16(16),
    UNKNOWN_17(17);
    //3, 6,7, etc is tele follow. idk any skills that use
    private final int val;

    SummonMovementType(int val) {
        this.val = val;
    }

    public int getValue() {
        return val;
    }
}
