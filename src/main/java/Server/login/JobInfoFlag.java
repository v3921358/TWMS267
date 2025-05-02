package Server.login;

public enum JobInfoFlag {

    臉型(0x1),
    髮型(0x2),
    臉飾(0x4),
    耳朵(0x8),
    尾巴(0x10),
    帽子(0x20),
    衣服(0x40),
    褲裙(0x80),
    披風(0x100),
    鞋子(0x200),
    手套(0x400),
    武器(0x800),
    副手(0x1000),
    ;
    private final int value;

    JobInfoFlag(int value) {
        this.value = value;
    }

    public int getVelue() {
        return value;
    }

    public boolean check(int x) {
        return (value & x) != 0;
    }
}
