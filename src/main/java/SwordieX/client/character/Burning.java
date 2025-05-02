package SwordieX.client.character;

import SwordieX.util.FileTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Burning {
    /* [ 創建角色的燃燒狀態 ] */
    public static final int 無 = 0;
    public static final int 超級燃燒 = 1;
    public static final int 燃燒加速器 = 2;
    public static final int 極限燃燒 = 3;

    private FileTime startTime;
    private FileTime endTime;
    private int startLv;
    private int endLv;
    private int burningType;

    public Burning(int burningType, int startLv, int endLv, FileTime startTime, FileTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.startLv = startLv;
        this.endLv = endLv;
        this.burningType = burningType;
    }
}
