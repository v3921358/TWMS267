/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Net.server;

import Client.MapleCharacter;
import Client.SecondaryStat;
import Client.skills.SkillFactory;
import Net.server.buffs.MapleStatEffect;
import tools.Pair;

import java.util.List;

public class MapleSkillEffectApp {

    /**
     * 應用BUFF狀態。
     *
     * @param effect
     * @param applyfrom
     * @param applyto
     * @param primary
     * @param effectEventData
     */
    public static void applyBuffEffect(MapleStatEffect effect, final MapleCharacter applyfrom, final MapleCharacter applyto, boolean primary, effectEventData effectEventData) {
        applyBuffEffect(effect, applyfrom, applyto, primary, effect.getSourceId(), effectEventData);
    }

    /**
     * @param effect
     * @param applyfrom
     * @param applyto
     * @param primary
     * @param sourceid
     * @param effectEventData
     */
    public static void applyBuffEffect(MapleStatEffect effect, final MapleCharacter applyfrom, final MapleCharacter applyto, boolean primary, int sourceid, effectEventData effectEventData) {
        int skilllevel = applyto.getSkillLevel(sourceid);
        if (skilllevel > 0) {
            effectEventData.duration = SkillFactory.getSkill(sourceid).getEffect(skilllevel).getDuration();
        }
    }

    public static class effectEventData {

        protected final List<Pair<SecondaryStat, Integer>> statups;
        protected int duration;

        public effectEventData(List<Pair<SecondaryStat, Integer>> statups) {
            this.statups = statups;
        }

        public effectEventData(List<Pair<SecondaryStat, Integer>> statups, int duration) {
            this.statups = statups;
            this.duration = duration;
        }

        public List<Pair<SecondaryStat, Integer>> getStatups() {
            return statups;
        }

        public int getDuration() {
            return duration;
        }
    }
}
