/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import Config.constants.SkillConstants;
import Net.server.CharacterCardFactory;
import tools.Pair;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author PlayDK
 * 角色卡系統
 */
public class MapleCharacterCards {

    private final List<Pair<Integer, Integer>> skills = new ArrayList<>();  // 技能id, 技能等級
    private Map<Integer, CardData> cards = new LinkedHashMap<>();  // order

    public Map<Integer, CardData> getCards() {
        return cards;
    }

    public void setCards(Map<Integer, CardData> cads) {
        cards = cads;
    }

    public List<Pair<Integer, Integer>> getCardEffects() {
        return skills;
    }

    public void calculateEffects() {
        skills.clear(); // 重置角色卡所有技能
        int deck1amount = 0, deck2amount = 0, deck3amount = 0, reqRank1 = 0, reqRank2 = 0, reqRank3 = 0;
        List<Integer> cardSkillIds1 = new LinkedList<>();
        List<Integer> cardSkillIds2 = new LinkedList<>();
        List<Integer> cardSkillIds3 = new LinkedList<>();
        CharacterCardFactory cardFactory = CharacterCardFactory.getInstance();
        for (Entry<Integer, CardData> cardInfo : cards.entrySet()) {
            if (cardInfo.getValue().chrId > 0) { // 檢測角色ID大於0才有
                //角色卡ID 技能ID 技能等級
                Triple<Integer, Integer, Integer> skillData = cardFactory.getCardSkill(cardInfo.getValue().job, cardInfo.getValue().level);
                if (cardInfo.getKey() < 4) { //卡片的位置也就是第1個卡片組合
                    if (skillData != null) {
                        cardSkillIds1.add(skillData.getLeft());
                        skills.add(new Pair<>(skillData.getMid(), skillData.getRight())); //添加技能 [ID] [等級]
                    }
                    deck1amount++;
                    if (reqRank1 == 0 || reqRank1 > cardInfo.getValue().level) {
                        reqRank1 = cardInfo.getValue().level; // take lowest
                    }
                } else if (cardInfo.getKey() > 3 && cardInfo.getKey() < 7) {
                    if (skillData != null) {
                        cardSkillIds2.add(skillData.getLeft());
                        skills.add(new Pair<>(skillData.getMid(), skillData.getRight())); //添加技能 [ID] [等級]
                    }
                    deck2amount++;
                    if (reqRank2 == 0 || reqRank2 > cardInfo.getValue().level) {
                        reqRank2 = cardInfo.getValue().level; // take lowest
                    }
                } else {
                    if (skillData != null) {
                        cardSkillIds3.add(skillData.getLeft());
                        skills.add(new Pair<>(skillData.getMid(), skillData.getRight())); //添加技能 [ID] [等級]
                    }
                    deck3amount++;
                    if (reqRank3 == 0 || reqRank3 > cardInfo.getValue().level) {
                        reqRank3 = cardInfo.getValue().level; // take lowest
                    }
                }
            }
        }

        if (deck1amount == 3 && cardSkillIds1.size() == 3) { //組合技能 需要有3張卡片且是在同1組才有
            List<Integer> uid = cardFactory.getUniqueSkills(cardSkillIds1); //獲取組合卡片的技能ID
            for (int i : uid) {
                skills.add(new Pair<>(i, SkillConstants.getCardSkillLevel(reqRank1))); // 組合卡片
            }
            skills.add(new Pair<>(cardFactory.getRankSkill(reqRank1), 1));
        }
        if (deck2amount == 3 && cardSkillIds2.size() == 3) {
            List<Integer> uid = cardFactory.getUniqueSkills(cardSkillIds2);
            for (int i : uid) {
                skills.add(new Pair<>(i, SkillConstants.getCardSkillLevel(reqRank2)));
            }
            skills.add(new Pair<>(cardFactory.getRankSkill(reqRank2), 1));
        }
        if (deck3amount == 3 && cardSkillIds3.size() == 3) {
            List<Integer> uid = cardFactory.getUniqueSkills(cardSkillIds3);
            for (int i : uid) {
                skills.add(new Pair<>(i, SkillConstants.getCardSkillLevel(reqRank3)));
            }
            skills.add(new Pair<>(cardFactory.getRankSkill(reqRank3), 1));
        }
    }

    public void recalcLocalStats(MapleCharacter chr) {
        int pos = -1;
        for (Entry<Integer, CardData> x : cards.entrySet()) {
            if (x.getValue().chrId == chr.getId()) {
                pos = x.getKey();
                break;
            }
        }
        if (pos != -1) {
            if (!CharacterCardFactory.getInstance().canHaveCard(chr.getLevel(), chr.getJob())) {
                cards.remove(pos); // we don't need to reset pos as its not needed
            } else {
                cards.put(pos, new CardData(chr.getId(), chr.getLevel(), chr.getJob())); // override old
            }
        }
        calculateEffects(); // recalculate, just incase 
    }

    public void loadCards(MapleClient c, boolean channelserver) {
        cards = CharacterCardFactory.getInstance().loadCharacterCards(c.getAccID(), c.getWorldId());
        if (channelserver) {
            calculateEffects();
        }
    }

    public void connectData(MaplePacketLittleEndianWriter mplew) {
        if (cards.isEmpty()) {  // we don't show for new characters 
            mplew.writeZeroBytes(12 * 9); // V.156 9*9->12*9
            return;
        }
        int poss = 0;
        for (CardData i : cards.values()) {
            poss++;
            if (poss > 9) {
                return;
            }
            mplew.writeInt(i.chrId);
            mplew.writeInt(i.level); //V.156 byte->int
            mplew.writeInt(i.job);
        }
    }
}
