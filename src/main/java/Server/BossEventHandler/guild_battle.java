package Server.BossEventHandler;

import Client.MapleCharacter;
import Net.server.Timer;
import connection.packet.FieldPacket;
import SwordieX.field.fieldeffect.FieldEffect;

public class guild_battle {

    public static void StartGuildBattle(MapleCharacter chr) {
        if (chr != null && chr.getMapId() == 941000100) {
            chr.changeMap(941000200, 0);
        }
        if (chr != null && chr.getMapId() == 941000200) {
            Timer.EventTimer.getInstance().schedule(() -> {
                chr.send(FieldPacket.fieldEffect(FieldEffect.getFieldEffectFromWz("Effect/EventEffect.img/ChallengeMission/Count/3", 0)));
            }, 1000);
            Timer.EventTimer.getInstance().schedule(() -> {
                chr.send(FieldPacket.fieldEffect(FieldEffect.getFieldEffectFromWz("Effect/EventEffect.img/ChallengeMission/Count/2", 0)));
            }, 2 * 1000);
            Timer.EventTimer.getInstance().schedule(() -> {
                chr.send(FieldPacket.fieldEffect(FieldEffect.getFieldEffectFromWz("Effect/EventEffect.img/ChallengeMission/Count/1", 0)));
            }, 3 * 1000);
            Timer.EventTimer.getInstance().schedule(() -> {
                chr.send(FieldPacket.fieldEffect(FieldEffect.getFieldEffectFromWz("Effect/EventEffect.img/ChallengeMission/start", 0)));
            }, 4 * 1000);
            Timer.EventTimer.getInstance().schedule(() -> {
                startDamagePro(chr);
            }, 5 * 1000);
        }
    }

    public static void startDamagePro(MapleCharacter chr) {
        Timer.EventTimer.getInstance().schedule(() -> {

        }, 5 * 1000); // 5S更新一次傷害總值a
    }

}
