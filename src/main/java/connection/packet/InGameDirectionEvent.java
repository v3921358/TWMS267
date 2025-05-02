package connection.packet;

import Config.constants.enums.InGameDirectionEventType;
import connection.Encodable;
import connection.OutPacket;
import SwordieX.util.Position;

public class InGameDirectionEvent implements Encodable {
    private final InGameDirectionEventType type;
    private int arg1, arg2, arg3, arg4, arg5;
    private String str, str2;
    private Position pos;
    private int[] arr;

    public InGameDirectionEvent(InGameDirectionEventType type) {
        this.type = type;
    }

    @Override
    public void encode(OutPacket outPacket) {
        outPacket.encodeByte(type.getType());
        switch (type) {
            case ForcedAction:
                outPacket.encodeInt(arg1); // nAction
                if (arg1 <= 0x80F) {
                    outPacket.encodeInt(arg2); // nDuration
                }
                break;
            case Delay:
                outPacket.encodeInt(arg1); // nDelay
                break;
            case EffectPlay:
                outPacket.encodeString(str); // sEffectUOL
                outPacket.encodeInt(arg1); // tDuration (0 => take effect's duration)
                outPacket.encodePositionInt(pos);
                outPacket.encodeByte(arg2 >= -1);
                if (arg2 >= -1) {
                    outPacket.encodeInt(arg2); // z
                }
                outPacket.encodeByte(arg3 >= -1);
                if (arg3 >= -1) {
                    outPacket.encodeInt(arg3); // dwNpcID (for CNpcPool::GetNpcForExtend)
                    outPacket.encodeByte(arg4);
                    outPacket.encodeByte(arg5);
                }
                outPacket.encodeByte(str2 != null);
                if (str2 != null) {
                    outPacket.encodeString(str2);
                }
                break;
            case ForcedInput:
                outPacket.encodeInt(arg1); // nForcedInput
                break;
            case PatternInputRequest:
                outPacket.encodeString(str); // sPattern
                outPacket.encodeInt(arg1); // nAct
                outPacket.encodeInt(arg2); // nRequestCount
                outPacket.encodeInt(arg3); // nTime
                break;
            case CameraMove:
                outPacket.encodeByte(arg1); // bBack
                outPacket.encodeInt(arg2); // nPixelPerSec
                if (arg1 == 0) {
                    outPacket.encodePositionInt(pos);
                } else {
                    outPacket.encodeByte(0);// not sure if really exists but in GMS(v198) there is extra 0 byte
                }
                break;
            case CameraOnCharacter:
                outPacket.encodeInt(arg1); // dwNpcID
                break;
            case CameraZoom:
                outPacket.encodeByte(arg1);
                if (arg1 == 0) {
                    outPacket.encodeInt(arg2); // nTime
                    outPacket.encodeInt(arg3); // nScale
                    outPacket.encodeInt(arg4); // nTimePos
                    outPacket.encodePositionInt(pos);
                }
                break;
            case UNK_226_1:
                break;
            case CameraReleaseFromUserPoint:
                break;
            case VansheeMode:
                outPacket.encodeByte(arg1); // bVanshee
                break;
            case FaceOff:
                outPacket.encodeInt(arg1); // nFaceItemID
                break;
            case Monologue:
                outPacket.encodeString(str); // sStr
                outPacket.encodeByte(arg1); // bIsEnd
                break;
            case MonologueScroll:
                outPacket.encodeString(str); // sStr
                outPacket.encodeByte(arg1); // stayModal
                outPacket.encodeShort(arg2);
                outPacket.encodeInt(arg3); // nUpdateSpeedTime
                outPacket.encodeInt(arg4); // nDecTic
                break;
            case AvatarLookSet:
                outPacket.encodeByte(arr.length);
                for (int itemID : arr) {
                    outPacket.encodeInt(itemID);
                }
                break;
            case RemoveAdditionalEffect:
                outPacket.encodeShort(67);
                break;
            case UNK_226_2:
            case UNK_226_3:
                break;
            case ForcedMove:
                outPacket.encodeInt(arg1); // nDir
                outPacket.encodeInt(arg2); // nSpeed
                break;
            case ForcedFlip:
                outPacket.encodeInt(arg1); // nForcedFlip
                break;
            case InputUI:
                outPacket.encodeByte(arg1); // nIdx
                break;
            case UNK_226_4:
                break;
            case UNK_226_5:
                outPacket.encodeInt(arg1);
                break;
            case UNK_226_6:
                outPacket.encodeString(str);
                break;
            case UNK_226_7:
                outPacket.encodeInt(arg1);
                break;
            case UNK_226_8:
                outPacket.encodeString(str);
                outPacket.encodeByte(arg1);
                outPacket.encodeByte(arg2);
                break;
        }
    }

    public static InGameDirectionEvent forcedAction(int action, int duration) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.ForcedAction);

        igdr.arg1 = action;
        igdr.arg2 = duration;

        return igdr;
    }

    public static InGameDirectionEvent delay(int delay) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.Delay);
        igdr.arg1 = delay;
        return igdr;
    }

    public static InGameDirectionEvent effectPlay(String effectUOL, int duration, Position position, int z,
                                                  int npcIdForExtend, boolean onUser, int idk2, String sUnk) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.EffectPlay);

        igdr.str = effectUOL;
        igdr.arg1 = duration;
        igdr.pos = position;
        igdr.arg2 = z;
        igdr.arg3 = npcIdForExtend;
        igdr.arg4 = onUser ? 0 : 1;
        igdr.arg5 = idk2; // flip with user
        igdr.str2 = sUnk;

        return igdr;
    }

    public static InGameDirectionEvent forcedInput(int forcedInput) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.ForcedInput);

        igdr.arg1 = forcedInput;

        return igdr;
    }

    public static InGameDirectionEvent patternInputRequest(String pattern, int act, int requestCount, int time) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.PatternInputRequest);

        igdr.str = pattern;
        igdr.arg1 = act;
        igdr.arg2 = requestCount;
        igdr.arg3 = time;

        return igdr;
    }

    /**
     * Moves the camera.
     *
     * @param back        if the camera move should go back to its original state
     * @param pixelPerSec speed of camera movement
     * @param pos         position the camera should move to (not applicable if back is true)
     * @return the event
     */
    public static InGameDirectionEvent cameraMove(boolean back, int pixelPerSec, Position pos) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.CameraMove);

        igdr.arg1 = back ? 1 : 0;
        igdr.arg2 = pixelPerSec;
        igdr.pos = pos;

        return igdr;
    }

    /**
     * Sets a camera on a character or npc.
     *
     * @param npcID the npc's id on which the camera should focus on, or 0 if the camera should focus on the player
     * @return the event
     */
    public static InGameDirectionEvent cameraOnCharacter(int npcID) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.CameraOnCharacter);

        igdr.arg1 = npcID;

        return igdr;
    }

    public static InGameDirectionEvent cameraZoom(boolean b, int time, int scale, int timePos, Position position) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.CameraZoom);

        igdr.arg1 = b ? 1 : 0;
        igdr.arg2 = time;
        igdr.arg3 = scale;
        igdr.arg4 = timePos;
        igdr.pos = position;

        return igdr;
    }

    public static InGameDirectionEvent cameraReleaseFromUserPoint() {
        return new InGameDirectionEvent(InGameDirectionEventType.CameraReleaseFromUserPoint);
    }

    public static InGameDirectionEvent vansheeMode(boolean vansheeMode) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.VansheeMode);

        igdr.arg1 = vansheeMode ? 1 : 0;

        return igdr;
    }

    public static InGameDirectionEvent faceOff(int faceItemID) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.FaceOff);

        igdr.arg1 = faceItemID;

        return igdr;
    }

    public static InGameDirectionEvent monologue(String msg, boolean isEnd) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.Monologue);

        igdr.str = msg;
        igdr.arg1 = isEnd ? 1 : 0;

        return igdr;
    }

    public static InGameDirectionEvent monologueScroll(String msg, boolean stayModal, short align, int updateSpeedTime,
                                                       int decTic) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.MonologueScroll);

        igdr.str = msg;
        igdr.arg1 = stayModal ? 1 : 0;
        igdr.arg2 = align;
        igdr.arg3 = updateSpeedTime;
        igdr.arg4 = decTic;

        return igdr;
    }

    public static InGameDirectionEvent avatarLookSet(int[] equipIDs) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.AvatarLookSet);
        igdr.arr = equipIDs;
        return igdr;
    }


    public static InGameDirectionEvent removeAdditionalEffect() {
        return new InGameDirectionEvent(InGameDirectionEventType.RemoveAdditionalEffect);
    }

    public static InGameDirectionEvent forcedMove(boolean left, int distance) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.ForcedMove);

        igdr.arg1 = left ? 1 : 2; // left = 1, right = 2
        igdr.arg2 = distance;

        return igdr;
    }

    public static InGameDirectionEvent forcedFlip(boolean left) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.ForcedFlip);

        igdr.arg1 = left ? -1 : 1; // left = -1, right = 1

        return igdr;
    }

    public static InGameDirectionEvent inputUI(int idx) {
        InGameDirectionEvent igdr = new InGameDirectionEvent(InGameDirectionEventType.InputUI);

        igdr.arg1 = idx;

        return igdr;
    }
}
