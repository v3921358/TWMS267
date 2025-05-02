/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server.commands;

/**
 * @author PlayDK
 */
public enum PlayerRank {

    普通('@', 0, "Player"),
    MVP銅牌I('@', 1, "BronzeIMvp"),
    MVP銅牌II('@', 2, "BronzeIIMvp"),
    MVP銅牌III('@', 3, "BronzeIIIMvp"),
    MVP銅牌IV('@', 4, "BronzeIVMvp"),
    MVP銀牌('@', 5, "SilverMvp"),
    MVP金牌('@', 6, "GoldMvp"),
    MVP鑽石('@', 7, "DiamondMvp"),
    MVP紅鑽('@', 8, "RedMvp"),
    實習管理員('!', "/", 1001, "Intern"),
    遊戲管理員('!', "/", 1002, "GM"),
    超級管理員('!', "/", 1003, "SuperGM"),
    伺服管理員('!', "/", 1004, "Admin");

    private final char commandPrefix;
    private final String fullWidthCommandPrefix;
    private final int level;
    private final String folderName;

    PlayerRank(char ch, int level, String folderName) {
        this(ch, null, level, folderName);
    }

    PlayerRank(char ch, String fw, int level, String folderName) {

        this.commandPrefix = ch;
        this.fullWidthCommandPrefix = fw;
        this.level = level;
        this.folderName = folderName;
    }

    public char getCommandPrefix() {
        return commandPrefix;
    }

    public String getFullWidthCommandPrefix() {
        return fullWidthCommandPrefix;
    }

    public int getLevel() {
        return level;
    }

    public int getSpLevel() {
        return isGm() ? level - 實習管理員.getLevel() + 1 : level;
    }

    public boolean isGm() {
        return level >= 實習管理員.getLevel();
    }

    public String getFolderName() {
        return folderName;
    }

    public static PlayerRank getByLevel(int level) {
        for (PlayerRank i : PlayerRank.values()) {
            if (i.getLevel() == level) {
                return i;
            }
        }
        return PlayerRank.普通;
    }

    public static PlayerRank getByFolderName(String name) {
        for (PlayerRank i : PlayerRank.values()) {
            if (i.getFolderName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return PlayerRank.普通;
    }

    public static PlayerRank getByCommandPrefix(char ch) {
        for (PlayerRank i : PlayerRank.values()) {
            if (i.getCommandPrefix() == ch || String.valueOf(ch).equals(i.getFullWidthCommandPrefix())) {
                return i;
            }
        }
        return null;
    }
}
