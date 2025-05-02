package SwordieX.client.character;

import Client.MapleCharacter;
import Plugin.script.ScriptManager;
import SwordieX.client.Client;
import SwordieX.client.character.avatar.AvatarData;
import SwordieX.client.party.Party;
import SwordieX.client.party.PartyMember;
import connection.OutPacket;
import SwordieX.enums.Stat;
import SwordieX.world.field.Field;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Char {

    private static final Logger log = LoggerFactory.getLogger(Char.class);

    private MapleCharacter chr;

    public MapleCharacter getCharacter() {
        return chr;
    }

    public void setCharacter(MapleCharacter chr) {
        this.chr = chr;
        updateInfoByChar(chr);
    }

    public void updateInfoByChar(MapleCharacter chr) {
        this.client = new Client(chr.getClient());
        this.id = chr.getId();
        this.userId = chr.getAccountID();
        this.avatarData = chr.getAvatarData();
    }

    @Getter
    @Setter
    private Client client;
    @Setter
    private int id;
    @Getter
    @Setter
    private int userId;

    @Setter
    @Getter
    private AvatarData avatarData;

    @Setter
    @Getter
    private int partyID = 0;

    @Setter
    @Getter
    private Field field;
    @Getter
    private final ScriptManager scriptManager = new ScriptManager(this);
    @Getter
    private boolean online;
    @Getter
    private Party party;

    public Integer getId() {
        return id;
    }

    public void setOnline(boolean online) {
//        boolean changed = online != this.online;

        this.online = online;
//        if (getGuild() != null) {
//            setGuild(getGuild()); // Hack to ensure that all chars have the same instance of a guild
//            Guild g = getGuild();
//            GuildMember gm = g.getMemberByCharID(getId());
//            if (gm != null) {
//                gm.setOnline(online);
//                gm.setChr(online ? this : null);
//                Alliance ally = getGuild().getAlliance();
//                if (ally != null) {
//                    ally.broadcast(WvsContext.allianceResult(
//                            AllianceResult.notifyLoginOrLogout(ally, g, gm, changed)), this);
//                } else {
//                    getGuild().broadcast(WvsContext.guildResult(
//                            GuildResult.notifyLoginOrLogout(g, gm, online, changed)), this);
//                }
//            }
//        }
        if (getParty() != null) {
            PartyMember pm = getParty().getPartyMemberByID(getId());
            if (pm != null) {
//                List<Integer> changedTypes = pm.getChangedTypes(online ? this : null);
                pm.setChr(online ? chr : null);
//                for (int type : changedTypes) {
//                    getParty().broadcast(WvsContext.partyResult(PartyResult.setMemberData(pm, type)));
//                }
                getParty().updateFull();
            }
        }


//        for (Friend f : getOnlineFriends()) {
//            boolean account = f.isAccount();
//            Char chr = f.getChr();
//            Friend me;
//            if (account) {
//                me = chr.getUser().getFriendByUserID(getAccount().getId());
//            } else {
//                me = chr.getFriendByCharID(getId());
//            }
//            if (me != null) {
//                me.setChr(online ? this : null);
//                me.setFlag(account ?
//                        online ? FriendFlag.AccountFriendOnline : FriendFlag.AccountFriendOffline
//                        : online ? FriendFlag.FriendOnline : FriendFlag.FriendOffline);
//                chr.write(WvsContext.friendResult(FriendResult.updateFriend(me)));
//            }
//        }
    }


    public void partyOnline(boolean online) {
        this.online = online;
        if (getParty() != null) {
            PartyMember pm = getParty().getPartyMemberByID(getId());
            if (pm != null) {
                pm.setChr(online ? chr : null);
            }
        }
    }

    public void setParty(Party party) {
        if (party != null) {
            setPartyID(party.getId());
        } else {
            setPartyID(0);
        }
        this.party = party;
    }

    /**
     * Returns the Char's name.
     *
     * @return The Char's name.
     */
    public String getName() {
        return getAvatarData().getCharacterStat().getName();
    }

    public int getJob() {
        return getAvatarData().getCharacterStat().getJob();
    }

    public int getSubJob() {
        return getAvatarData().getCharacterStat().getSubJob();
    }

    public int getLevel() {
        return getAvatarData().getCharacterStat().getLevel();
    }

    public int getFieldID() {
        return (int) getAvatarData().getCharacterStat().getPosMap();
    }

    /**
     * Returns the current HP of this Char.
     *
     * @return the current HP of this Char.
     */
    public int getHP() {
        return getStat(Stat.hp);
    }

    /**
     * Gets a raw Stat from this Char, unaffected by things such as equips and skills.
     *
     * @param charStat The requested Stat
     * @return the requested stat's value
     */
    public int getStat(Stat charStat) {
        CharacterStat cs = getAvatarData().getCharacterStat();
        switch (charStat) {
            case str:
                return cs.getStr();
            case dex:
                return cs.getDex();
            case inte:
                return cs.getInt();
            case luk:
                return cs.getLuk();
            case hp:
                return cs.getHp();
            case mhp:
                return cs.getMaxHp();
            case mp:
                return cs.getMp();
            case mmp:
                return cs.getMaxMp();
            case ap:
                return cs.getAp();
            case level:
                return cs.getLevel();
            case skin:
                return cs.getSkin();
            case face:
                return cs.getFace();
            case hair:
                return cs.getHair();
            case pop:
                return cs.getPop();
            case charismaEXP:
                return cs.getCharismaExp();
            case charmEXP:
                return cs.getCharmExp();
            case craftEXP:
                return cs.getCraftExp();
            case insightEXP:
                return cs.getInsightExp();
            case senseEXP:
                return cs.getSenseExp();
            case willEXP:
                return cs.getWillExp();
            case fatigue:
                return cs.getFatigue();
            case job:
                return cs.getJob();
        }
        return -1;
    }

    /**
     * Writes a packet to this Char's client.
     *
     * @param outPacket The OutPacket to write.
     */
    public void write(OutPacket outPacket) {
        if (getClient() != null) {
            getClient().write(outPacket);
        }
    }
}
