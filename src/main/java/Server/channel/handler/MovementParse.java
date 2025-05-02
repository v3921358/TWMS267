package Server.channel.handler;

import Net.server.maps.AnimatedMapleMapObject;
import Net.server.movement.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.data.MaplePacketReader;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MovementParse {

    /**
     * Logger for this class.
     */
    public static final Logger log = LoggerFactory.getLogger("Movement");

    /*
     * 1 = 玩家移動
     * 2 = 怪物移動
     * 3 = 寵物移動
     * 4 = 召喚獸移動
     * 5 = 寶貝龍移動
     * 6 = 玩家攻擊怪物移動
     * 7 = 花狐移動
     */
    public static List<LifeMovementFragment> parseMovement(MaplePacketReader slea, int kind) {
        List<LifeMovementFragment> res = new ArrayList<>();
        int numCommands = slea.readShort(); //循環次數
        String packet = slea.toString(true);
        try {
            for (int i = 0; i < numCommands; i++) {
                byte command = slea.readByte(); //移動類型
                switch (command) {
                    case 0:
                    case 8:
                    case 15:
                    case 17:
                    case 19:
                    case 72:
                    case 73:
                    case 74:
                    case 75:
                    case 76:
                    case 77:
                    case 94:
                    case 108: {
                        short x = slea.readShort();
                        short y = slea.readShort();
                        short vx = slea.readShort();
                        short vy = slea.readShort();
                        short fh = slea.readShort();
                        short footStart = (command == 15 || command == 17) ? slea.readShort() : 0;
                        short xoffset = slea.readShort();
                        short yoffset = slea.readShort();
                        short unk1 = slea.readShort();

                        byte moveAction = slea.readByte();
                        short elapse = slea.readShort();
                        byte forcedStop = slea.readByte();
                        MovementNormal mn = new MovementNormal(command, elapse, moveAction, forcedStop);
                        mn.setPosition(new Point(x, y));
                        mn.setPixelsPerSecond(new Point(vx, vy));
                        mn.setFH(fh);
                        mn.setFootStart(footStart);
                        mn.setOffset(new Point(xoffset, yoffset));
                        mn.setUnk1(unk1);
                        res.add(mn);
                        break;
                    }
                    case 1:
                    case 2:
                    case 18:
                    case 21:
                    case 22:
                    case 24:
                    case 62:
                    case 65:
                    case 66:
                    case 67:
                    case 68:
                    case 69:
                    case 70:
                    case 99: {
                        short vx = slea.readShort();
                        short vy = slea.readShort();
                        short footStart = 0;
                        if (command == 21 || command == 22) {
                            footStart = slea.readShort();
                        }
                        short xoffset = 0;
                        short yoffset = 0;
                        if (command == 62) {
                            xoffset = slea.readShort();
                            yoffset = slea.readShort();
                        }

                        byte moveAction = slea.readByte();
                        short elapse = slea.readShort();
                        byte forcedStop = slea.readByte();
                        MovementJump mj = new MovementJump(command, elapse, moveAction, forcedStop);
                        mj.setPixelsPerSecond(new Point(vx, vy));
                        mj.setFootStart(footStart);
                        mj.setOffset(new Point(xoffset, yoffset));
                        res.add(mj);
                        break;
                    }
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 9:
                    case 10:
                    case 11:
                    case 13:
                    case 26:
                    case 27:
                    case 53:
                    case 54:
                    case 55:
                    case 59:
                    case 83:
                    case 84:
                    case 85:
                    case 87:
                    case 89:
                    case 111: {
                        short x = slea.readShort();
                        short y = slea.readShort();
                        short fh = slea.readShort();
                        int unk2 = slea.readInt();

                        byte moveAction = slea.readByte();
                        short elapse = slea.readShort();
                        byte forcedStop = slea.readByte();
                        MovementTeleport mt = new MovementTeleport(command, elapse, moveAction, forcedStop);
                        mt.setPosition(new Point(x, y));
                        mt.setFH(fh);
                        mt.setUnk2(unk2);
                        res.add(mt);
                        break;
                    }
                    case 12: {
                        res.add(new MovementStatChange(command, slea.readByte()));
                        break;
                    }
                    case 14:
                    case 16: {
                        short fh = slea.readShort();
                        short vx = slea.readShort();
                        short vy = slea.readShort();

                        byte moveAction = slea.readByte();
                        short elapse = slea.readShort();
                        byte forcedStop = slea.readByte();
                        MovementStartFallDown msfd = new MovementStartFallDown(command, elapse, moveAction, forcedStop);
                        msfd.setFH(fh);
                        msfd.setPixelsPerSecond(new Point(vx, vy));
                        res.add(msfd);
                        break;
                    }
                    case 23:
                    case 102:
                    case 103: {
                        short x = slea.readShort();
                        short y = slea.readShort();
                        short vx = slea.readShort();
                        short vy = slea.readShort();

                        byte moveAction = slea.readByte();
                        short elapse = slea.readShort();
                        byte forcedStop = slea.readByte();
                        MovementFlyingBlock um = new MovementFlyingBlock(command, elapse, moveAction, forcedStop);
                        um.setPosition(new Point(x, y));
                        um.setPixelsPerSecond(new Point(vx, vy));
                        res.add(um);
                        break;
                    }
                    case 29: {
                        int unk3 = slea.readInt();

                        byte moveAction = slea.readByte();
                        short elapse = slea.readShort();
                        byte forcedStop = slea.readByte();
                        MovementNew1 mn1 = new MovementNew1(command, elapse, moveAction, forcedStop);
                        mn1.setUnk3(unk3);
                        res.add(mn1);
                        break;
                    }
                    case 30:
                    case 42: {
                        int unk2 = slea.readInt();

                        byte moveAction = slea.readByte();
                        short elapse = slea.readShort();
                        byte forcedStop = slea.readByte();
                        MovementNew2 mn2 = new MovementNew2(command, elapse, moveAction, forcedStop);
                        mn2.setUnk2(unk2);
                        res.add(mn2);
                        break;
                    }
                    case 31:
                    case 32:
                    case 33:
                    case 34:
                    case 35:
                    case 36:
                    case 37:
                    case 38:
                    case 39:
                    case 40:
                    case 41:
                    case 43:
                    case 44:
                    case 45:
                    case 46:
                    case 47:
                    case 48:
                    case 49:
                    case 51:
                    case 52:
                    case 56:
                    case 58:
                    case 60:
                    case 61:
                    case 63:
                    case 64:
                    case 78:
                    case 79:
                    case 81:
                    case 86:
                    case 88:
                    case 90:
                    case 91:
                    case 92:
                    case 93:
                    case 95:
                    case 96:
                    case 97:
                    case 98:
                    case 100:
                    case 101:
                    case 104:
                    case 105:
                    case 106:
                    case 107: {
                        byte moveAction = slea.readByte();
                        short elapse = slea.readShort();
                        byte forcedStop = slea.readByte();
                        res.add(new MovementBase(command, moveAction, elapse, forcedStop));
                        break;
                    }
                    case 50: {
                        short x = slea.readShort();
                        short y = slea.readShort();
                        short vx = slea.readShort();
                        short vy = slea.readShort();
                        short xoffset = slea.readShort();

                        byte moveAction = slea.readByte();
                        short elapse = slea.readShort();
                        byte forcedStop = slea.readByte();
                        MovementOffsetX mox = new MovementOffsetX(command, elapse, moveAction, forcedStop);
                        mox.setPosition(new Point(x, y));
                        mox.setPixelsPerSecond(new Point(vx, vy));
                        mox.setOffset(new Point(xoffset, 0));
                        res.add(mox);
                        break;
                    }
                    case 57:
                    case 71:
                    case 110: {
                        short x = slea.readShort();
                        short y = slea.readShort();
                        short vx = slea.readShort();
                        short vy = slea.readShort();
                        short fh = slea.readShort();

                        byte moveAction = slea.readByte();
                        short elapse = slea.readShort();
                        byte forcedStop = slea.readByte();
                        MovementAngle ma = new MovementAngle(command, elapse, moveAction, forcedStop);
                        ma.setPosition(new Point(x, y));
                        ma.setPixelsPerSecond(new Point(vx, vy));
                        ma.setFH(fh);
                        res.add(ma);
                        break;
                    }
                    /*case 81:
                    case 83: {
                        final AranMovement am = new AranMovement(command);
                        for (int l = 0; l < 7; ++l) {
                            am.aS(l, slea.readShort());
                        }
                        res.add(am);
                        break;
                    }*/
                    default: {
                        byte moveAction = slea.readByte();
                        short elapse = slea.readShort();
                        byte forcedStop = slea.readByte();
                        res.add(new MovementBase(command, moveAction, elapse, forcedStop));
                        break;
                    }
                }
            }
            byte bVal = slea.readByte();
            slea.skip(bVal >> 1);
            if ((bVal & 1) != 0) {
                slea.skip(1);
            }
            if (numCommands != res.size()) {
                log.error(getKindName(kind) + " 循環次數[" + numCommands + "]和實際上獲取的循環次數[" + res.size() + "]不符" + packet);
                return null;
            }
            return res;
        } catch (Exception e) {
            log.error(getKindName(kind) + "封包解析出錯：" + packet, e);
            return null;
        }
    }

    public static void updatePosition(List<LifeMovementFragment> movement, AnimatedMapleMapObject target, int yoffset) {
        if (movement == null) {
            return;
        }
        int lastMoveTime = 0;
        for (LifeMovementFragment move : movement) {
            if (move instanceof LifeMovement) {
                if (move instanceof MovementNormal) {
                    Point position = ((MovementNormal) move).getPosition();
                    position.y += yoffset;
                    target.setPosition(position);
                    target.setHomeFH(target.getCurrentFH());
                    target.setCurrentFh(((MovementNormal) move).getFH());
                }
                target.setStance(((LifeMovement) move).getMoveAction());
                lastMoveTime += ((LifeMovement) move).getElapse();
            }
        }
        target.setLastMoveTime(lastMoveTime);
    }

    public static String getKindName(int kind) {
        String moveMsg;
        switch (kind) {
            case 1:
                moveMsg = "玩家";
                break;
            case 2:
                moveMsg = "怪物";
                break;
            case 3:
                moveMsg = "寵物";
                break;
            case 4:
                moveMsg = "召喚獸";
                break;
            case 5:
                moveMsg = "寶貝龍";
                break;
            case 6:
                moveMsg = "萌獸";
                break;
            case 7:
                moveMsg = "花狐";
                break;
            case 8:
                moveMsg = "人型花狐";
                break;
            case 9:
                moveMsg = "機器人";
                break;
            case 10:
                moveMsg = "NPC";
                break;
            default:
                moveMsg = "未知kind";
                break;
        }
        return moveMsg;
    }
}
