package SwordieX.client;

import SwordieX.client.character.Char;
import SwordieX.enums.AccountType;
import SwordieX.util.FileTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private int id;
    private String name;
    private String password;
    private String pic;
    private AccountType accountType;
    private int age;
    private byte gender;
    private byte blockReason;
    private byte gradeCode;
    private long chatUnblockDate;
    private int characterSlots;
    private FileTime creationDate;
    private int maplePoints;
    private int nxPrepaid;
    //    private Set<Account> accounts;
    private FileTime banExpireDate = FileTime.currentTime();
    private String banReason;
    private Char currentChr;
    //    private Account currentAcc;
    private String email;
    private String registerIp;

    public User() {
    }

    public User(String name) {
        this.name = name;
        this.accountType = AccountType.Player;
        this.creationDate = FileTime.currentTime();
//        this.accounts = new HashSet<>();
//        this.offenseManager = new OffenseManager();
    }

    public String getSecurityName() {
        StringBuilder sb = new StringBuilder(name);
        if (sb.length() >= 4) {
            sb.replace(1, 3, "**");
        } else if (sb.length() >= 3) {
            sb.replace(1, 2, "*");
        }
        if (sb.length() > 4) {
            sb.replace(sb.length() - 1, sb.length(), "*");
        }
        return sb.toString();
    }
}
