package Config.configs;

import tools.config.Property;

public class CubeConfig {

    /**
     * 方塊出現最高級屬性的概率（1-1000）默認25
     */
    @Property(key = "game.cube.mrate", defaultValue = "25")
    public static int CUBE_SS_RATE;

    @Property(key = "game.cube.lrate", defaultValue = "1")
    public static int CURE_LEVEL_UP_RATE;
}
