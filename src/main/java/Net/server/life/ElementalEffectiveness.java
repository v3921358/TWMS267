package Net.server.life;

public enum ElementalEffectiveness {

    正常(1.0), 免疫(0.0), 增強(0.5), 虛弱(1.5);
    private final double value;

    ElementalEffectiveness(double val) {
        this.value = val;
    }

    public static ElementalEffectiveness getByNumber(int num) {
        switch (num) {
            case 1:
                return 免疫;
            case 2:
                return 增強;
            case 3:
                return 虛弱;
            default:
                throw new IllegalArgumentException("Unkown effectiveness: " + num);
        }
    }

    public double getValue() {
        return value;
    }
}
