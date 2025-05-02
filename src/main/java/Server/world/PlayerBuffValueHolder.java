package Server.world;

import Client.SecondaryStat;
import Net.server.buffs.MapleStatEffect;

import java.io.Serializable;
import java.util.Map;

/*
 * 角色BUFF的具體信息
 */
public class PlayerBuffValueHolder implements Serializable {

    private static final long serialVersionUID = 9179541993413738569L;
    public final long startTime;
    public final MapleStatEffect effect;
    public final Map<SecondaryStat, Integer> statup;
    public int localDuration, fromChrId;

    public PlayerBuffValueHolder(long startTime, MapleStatEffect effect, Map<SecondaryStat, Integer> statup, int localDuration, int fromChrId) {
        this.startTime = startTime;
        this.effect = effect;
        this.statup = statup;
        this.localDuration = localDuration;
        this.fromChrId = fromChrId;
    }

    public PlayerBuffValueHolder(long startTime, MapleStatEffect effect, Map<SecondaryStat, Integer> statup) {
        this.startTime = startTime;
        this.effect = effect;
        this.statup = statup;
    }
}
