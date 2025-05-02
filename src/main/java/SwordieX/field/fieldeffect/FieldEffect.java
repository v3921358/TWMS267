package SwordieX.field.fieldeffect;

import Net.server.life.MapleMonster;
import Opcode.Headler.OutHeader;
import connection.OutPacket;
import tools.data.MaplePacketLittleEndianWriter;

public class FieldEffect {

    private FieldEffectType fieldEffectType;
    private String string;
    private String string2;
    private String string3;
    private String string4;
    private int arg1;
    private int arg2;
    private int arg3;
    private int arg4;
    private int arg5;
    private int arg6;
    private long arg7;
    private long arg8;
    private int arg9;
    private int arg10;
    private int arg11;

    public void encode(OutPacket outPacket) {
        outPacket.encodeByte(getFieldEffectType().getVal());
        switch (getFieldEffectType()) {
            case Summon:
                outPacket.encodeByte(arg1);
                outPacket.encodeInt(arg2);
                outPacket.encodeInt(arg3);
                break;
            case Tremble:
                outPacket.encodeByte(arg1);
                outPacket.encodeInt(arg2);
                outPacket.encodeShort(arg3);
                break;
            case Object:
                outPacket.encodeString(string);// String
                break;
            case ObjectDisable:
                outPacket.encodeString(string);// String
                outPacket.encodeByte(arg1);    // boolean: ON/OFF
                break;
            case Screen:
                outPacket.encodeString(string);
                break;
            case Unk5:
                outPacket.encodeInt(arg1);
                outPacket.encodeInt(arg2);
                outPacket.encodeInt(arg3);
                outPacket.encodeString(string);
                outPacket.encodeString(string2);
                outPacket.encodeInt(arg4);
                break;
            case Unk6:
                outPacket.encodeInt(arg1);
                outPacket.encodeInt(arg2);
                outPacket.encodeString(string);
                outPacket.encodeString(string2);
                break;
            case PlaySound:
                outPacket.encodeString(string);// Sound
                outPacket.encodeInt(arg1);// Volume
                outPacket.encodeInt(arg2);
                outPacket.encodeInt(arg3);
                break;
            case MobHPTag:
                outPacket.encodeInt(arg1);     // Mob Template ID
                outPacket.encodeLong(arg7);    // Mob HP
                outPacket.encodeLong(arg8);    // KB值
                outPacket.encodeByte(arg4);    // HP Tag Colour
                outPacket.encodeByte(arg5);    // HP Tab BG Colour
                break;
            case ChangeBGM:
                outPacket.encodeString(string);// sDir
                outPacket.encodeInt(arg1);     // idk
                outPacket.encodeInt(arg2);     // tStartTime
                outPacket.encodeInt(arg3);
                break;
            case BGMVolumeOnly:
                outPacket.encodeByte(arg1 != 0); // bBGMVolumeOnly
                break;
            case BGMVolume:
                outPacket.encodeInt(arg1);     // nVolume
                outPacket.encodeInt(arg2);     // nFadingDuration
                break;
            case Unk12:
                outPacket.encodeInt(arg1);
                break;
            case Unk13:
                outPacket.encodeInt(arg1);
                break;
            case Unk14:
                break;
            case Unk15:
                outPacket.encodeInt(arg1);
                break;
            case Unk16:
                break;
            case RewardRoulette:
                outPacket.encodeInt(arg1);     // Reward Job ID
                outPacket.encodeInt(arg2);     // Reward Part ID
                outPacket.encodeInt(arg3);     // Reward Level ID
                break;
            case ScreenDelayed:
                outPacket.encodeString(string);// Directory to the Effect
                outPacket.encodeInt(arg1);     // Delay in ms
                break;
            case TopScreen:
                outPacket.encodeString(string);// Directory to the Effect
                break;
            case TopScreenDelayed:                   // Goes over other effects
                outPacket.encodeString(string);// Directory to the Effect
                outPacket.encodeInt(arg1);     // Delay in ms
                break;
            case ScreenAutoLetterBox:
                outPacket.encodeString(string);// Path to the Effect
                outPacket.encodeInt(arg1);     // Delay in ms
                break;
            case FloatingUI:
                outPacket.encodeString(string);
                outPacket.encodeInt(arg1);
                outPacket.encodeInt(arg2);
                break;
            case Unk23:
                outPacket.encodeString(string);
                outPacket.encodeByte(arg1);
                outPacket.encodeByte(arg2 != 0);
                break;
            case Blind:
                outPacket.encodeByte(arg1);
                outPacket.encodeShort(arg2);
                outPacket.encodeShort(arg3);
                outPacket.encodeShort(arg4);
                outPacket.encodeShort(arg5);
                outPacket.encodeInt(arg6);
                outPacket.encodeInt((int) arg7); // new int
                break;
            case GreyScale:
                outPacket.encodeShort(arg1);   // GreyField Type
                outPacket.encodeByte(arg2);    // boolean: ON/OFF
                break;
            case OnOffLayer:
                outPacket.encodeByte(arg1);    // nType (0 = On, 1 = Move, 2 = Off)
                outPacket.encodeInt(arg2);     // tDuration
                outPacket.encodeString(string);// sKey
                if (arg1 == 0) {
                    outPacket.encodeInt(arg3); // nRX
                    outPacket.encodeInt(arg4); // nRY
                    outPacket.encodeInt(arg5); // nZ
                    outPacket.encodeString(string2); // pOrigin
                    outPacket.encodeInt(arg6); // nOrigin
                    outPacket.encodeByte((byte) arg7); // bPostRender
                    outPacket.encodeInt(arg9); // idk
                    outPacket.encodeByte((byte) arg8); // bRepeat?
                    outPacket.encodeInt(arg10);
                    outPacket.encodeInt(arg11);
                } else if (arg1 == 1) {
                    outPacket.encodeInt(arg3); // nDX
                    outPacket.encodeInt(arg4); // nDY
                } else if (arg1 == 2) {
                    outPacket.encodeByte((byte) arg8); // ?
                } else if (arg1 == 3) {
                    outPacket.encodeString("");
                    outPacket.encodeInt(0);
                    outPacket.encodeInt(0);
                } else if (arg1 == 4) {
                    outPacket.encodeString("");
                    outPacket.encodeInt(0);
                    outPacket.encodeInt(0);
                    outPacket.encodeByte(0);
                    outPacket.encodeInt(0);
                    outPacket.encodeByte(0);
                }
                break;
            case Overlap:                    // Takes a Snapshot of the Client and slowly fades away
                outPacket.encodeInt(arg1);     // Duration of the overlap (ms)
                break;
            case OverlapDetail:
                outPacket.encodeInt(arg1);     // Fade In
                outPacket.encodeInt(arg2);     // wait time
                outPacket.encodeInt(arg3);     // Fade Out
                outPacket.encodeByte(arg4);    // some boolean
                break;
            case RemoveOverlapDetail:
                outPacket.encodeInt(arg1);     // Fade Out duration
                break;
            case Unk30:
                outPacket.encodeString(string);
                break;
            case ColorChange:
                outPacket.encodeShort(arg1);   // GreyField Type (but doesn't contain Reactor
                outPacket.encodeShort(arg2);   // red      (255 is normal value)
                outPacket.encodeShort(arg3);   // green    (255 is normal value)
                outPacket.encodeShort(arg4);   // blue     (255 is normal value)
                outPacket.encodeInt(arg5);     // time in ms, that it takes to transition from old colours to the new colours
                outPacket.encodeInt(0);          // is in queue
                if (arg1 == 4) {// Npc
                    outPacket.encodeInt(arg6); // Npc Id (?)
                }
            case StageClear:
                outPacket.encodeInt(arg1);     // Exp Number given
                break;
            case TopScreenWithOrigin:
                outPacket.encodeString(string);
                outPacket.encodeByte(arg1);
                break;
            case SpineScreen:
                outPacket.encodeByte(arg1); // bBinary
                outPacket.encodeByte(arg2); // bLoop
                outPacket.encodeByte(arg3); // bPostRender
                outPacket.encodeInt(arg4); // tEndDelay
                outPacket.encodeString(string); // sPath
                outPacket.encodeString(string2); // sAnimationName
                outPacket.encodeString(string3);
                outPacket.encodeBoolean(arg5 > 0);
                outPacket.encodeInt(arg6);
                outPacket.encodeInt((int) arg7);
                outPacket.encodeInt((int) arg8);
                outPacket.encodeInt(arg9);
                boolean hasKey = string4 != null && !"".equals(string4);
                outPacket.encodeByte(hasKey);
                if (hasKey) {
                    outPacket.encodeString(string4); // sKeyName
                }
                break;
            case OffSpineScreen:
                outPacket.encodeString(string); // sKeyName
                outPacket.encodeInt(arg1); // nType
                if (arg1 == 1) {
                    outPacket.encodeInt(arg2); // tAlpha
                } else if (arg1 == 2) {
                    outPacket.encodeString(string2); // sAnimationName
                }
                break;
            case Introd:
                outPacket.encodeString("intord");
                outPacket.encodeByte(0);
                outPacket.encodeByte(0);
                outPacket.encodeByte(0);
            case Unk37:
                outPacket.encodeString(string);
                outPacket.encodeByte(arg1);
                if (arg1 == 22 || arg1 == 20) {
                    outPacket.encodeInt(arg2);
                }
                break;
            case Unk38:
                outPacket.encodeString(string);
                break;
            case Unk39:
                // sub_1420D5740
                outPacket.encodeInt(arg1);
                outPacket.encodeString(string);
                if (arg1 == 1) {
                    outPacket.encodeInt(0);
                } else if (arg1 == 2) {
                    outPacket.encodeInt(0);
                    outPacket.encodeInt(0);
                    outPacket.encodeInt(0);
                    outPacket.encodeInt(0);
                }
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeString("");
                outPacket.encodeInt(0);
                outPacket.encodeByte(0);
                outPacket.encodeInt(0);
                outPacket.encodeByte(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                break;
            case Unk40:
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                break;
            case Unk41:
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                break;
            case Unk42:
                outPacket.encodeString("");
                outPacket.encodeString("");
                outPacket.encodeByte(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeString("");
                outPacket.encodeByte(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeByte(0);
                break;
            case Unk43:
                outPacket.encodeString("");
                outPacket.encodeInt(0);
                break;
            case Unk44:
                outPacket.encodeString("");
                outPacket.encodeString("");
                outPacket.encodeByte(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                outPacket.encodeByte(0);
                outPacket.encodeByte(0);
                outPacket.encodeString("");
                outPacket.encodeByte(0);
                outPacket.encodeInt(0);
                boolean OnOff = false;
                outPacket.encodeByte(OnOff);
                if (OnOff) {
                    outPacket.encodeString("");
                    outPacket.encodeByte(0);
                    outPacket.encodeInt(0);
                }
                outPacket.encodeInt(0);
                outPacket.encodeInt(0);
                break;
            case Unk45:
                outPacket.encodeString("");
                int v9 = 0;
                outPacket.encodeInt(v9);
                if ((v9 - 1) > 0) {
                    if ((v9 - 1) == 1) {
                        outPacket.encodeString("");
                    } else {
                        outPacket.encodeInt(0);
                    }
                }
                break;
            case Unk46:
                outPacket.encodeByte(0);
                outPacket.encodeShort(0);
                outPacket.encodeShort(0);
                outPacket.encodeShort(0);
                outPacket.encodeShort(0);
                outPacket.encodeInt(0);
                outPacket.encodeByte(0);
                break;
        }
    }

    public static FieldEffect mobHPTagFieldEffect(MapleMonster mob) {
        return mobHPTagFieldEffect(mob.getId() == 9400589 ? 9300184 : mob.getId(),
                mob.getHp(), mob.getMobMaxHp(), mob.getStats().getTagColor(),
                mob.getStats().getTagBgColor());
    }

    public static FieldEffect mobHPTagFieldEffect(int id, long hp, long maxHp, byte tagColor, byte tagBgColor) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.MobHPTag);

        fieldEffect.arg1 = id;
        fieldEffect.arg7 = hp;
        fieldEffect.arg8 = maxHp;
        fieldEffect.arg4 = tagColor;
        fieldEffect.arg5 = tagBgColor;

        return fieldEffect;
    }

    public static FieldEffect changeBGM(String dir, int startTime, int idk, int idk1) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.ChangeBGM);

        fieldEffect.string = dir;
        fieldEffect.arg1 = startTime;
        fieldEffect.arg2 = idk;
        fieldEffect.arg3 = idk1;

        return fieldEffect;
    }

    public static FieldEffect bgmVolumeOnly(boolean volumeOnly) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.BGMVolumeOnly);

        fieldEffect.arg1 = volumeOnly ? 1 : 0;

        return fieldEffect;
    }

    public static FieldEffect bgmVolume(int volume, int fadingDuration) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.BGMVolume);

        fieldEffect.arg1 = volume;
        fieldEffect.arg2 = fadingDuration;

        return fieldEffect;
    }

    public static FieldEffect getFieldEffectTremble(int type, int delay, int b) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.Tremble);

        fieldEffect.arg1 = type;
        fieldEffect.arg2 = delay;
        fieldEffect.arg3 = b;

        return fieldEffect;
    }

    public static FieldEffect getFieldEffectFromObject(String dir) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.Object);
        fieldEffect.string = dir;

        return fieldEffect;
    }

    public static FieldEffect getFieldEffectScreen(String dir) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.Screen);
        fieldEffect.string = dir;

        return fieldEffect;
    }

    public static FieldEffect getFieldBackgroundEffectFromWz(String dir, int delay) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.ScreenDelayed);

        fieldEffect.string = dir;
        fieldEffect.arg1 = delay;

        return fieldEffect;
    }

    public static FieldEffect getOffFieldEffectTopScreen(String dir) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.TopScreen);

        fieldEffect.string = dir;

        return fieldEffect;
    }

    public static FieldEffect getOffFieldEffectFromWz(String dir, int delay) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.TopScreenDelayed);

        fieldEffect.string = dir;
        fieldEffect.arg1 = delay;

        return fieldEffect;
    }

    public static FieldEffect getFieldEffectFromWz(String dir, int delay) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.ScreenAutoLetterBox);

        fieldEffect.string = dir;
        fieldEffect.arg1 = delay;

        return fieldEffect;
    }

    public static FieldEffect setFieldGrey(GreyFieldType greyFieldType, boolean setGrey) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.GreyScale);

        fieldEffect.arg1 = greyFieldType.getVal();
        fieldEffect.arg2 = setGrey ? 1 : 0;

        return fieldEffect;
    }

    public static FieldEffect setFieldColor(GreyFieldType colorFieldType, short red, short green, short blue, int time) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.ColorChange);

        fieldEffect.arg1 = colorFieldType.getVal();
        fieldEffect.arg2 = red;
        fieldEffect.arg3 = green;
        fieldEffect.arg4 = blue;
        fieldEffect.arg5 = time;

        return fieldEffect;
    }

    public static FieldEffect showClearStageExpWindow(int expNumber) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.StageClear);

        fieldEffect.arg1 = expNumber;

        return fieldEffect;
    }

    public static FieldEffect takeSnapShotOfClient(int duration) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.Overlap);

        fieldEffect.arg1 = duration;

        return fieldEffect;
    }

    public static FieldEffect onOffLayer(int type, int duration, String key, int x, int y,
                                         int z, String origin, int org, boolean postRender, int idk, boolean repeat, int v1, int v2) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.OnOffLayer);

        fieldEffect.arg1 = type;
        fieldEffect.arg2 = duration;
        fieldEffect.string = key;
        fieldEffect.arg3 = x;
        fieldEffect.arg4 = y;
        fieldEffect.arg5 = z;
        fieldEffect.string2 = origin;
        fieldEffect.arg6 = org;
        fieldEffect.arg7 = postRender ? 1 : 0;
        fieldEffect.arg9 = idk;
        fieldEffect.arg8 = repeat ? 1 : 0; // unsure if it's repeat
        fieldEffect.arg10 = v1;
        fieldEffect.arg11 = v2;

        return fieldEffect;
    }

    public static FieldEffect takeSnapShotOfClient2(int transitionDurationToSnapShot, int inBetweenDuration, int transitionBack, boolean someBoolean) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.OverlapDetail);

        fieldEffect.arg1 = transitionDurationToSnapShot;
        fieldEffect.arg2 = inBetweenDuration;
        fieldEffect.arg3 = transitionBack;
        fieldEffect.arg4 = someBoolean ? 1 : 0;

        return fieldEffect;
    }

    public static FieldEffect removeOverlapScreen(int duration) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.RemoveOverlapDetail);

        fieldEffect.arg1 = duration;

        return fieldEffect;
    }

    public static FieldEffect playSound(String sound, int vol, int n1, int n2) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.PlaySound);

        fieldEffect.string = sound;
        fieldEffect.arg1 = vol;
        fieldEffect.arg2 = n1;
        fieldEffect.arg3 = n2;

        return fieldEffect;
    }

    public static FieldEffect blind(int enable, int x, int color, int unk1, int unk2, int time, int unk3) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.Blind);

        fieldEffect.arg1 = enable;
        fieldEffect.arg2 = x;
        fieldEffect.arg3 = color;
        fieldEffect.arg4 = unk1;
        fieldEffect.arg5 = unk2;
        fieldEffect.arg6 = time;
        fieldEffect.arg7 = unk3;

        return fieldEffect;
    }

    public static FieldEffect showSpineScreen(int endDelay, String path,
                                              String animationName, String str) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.SpineScreen);

        fieldEffect.arg1 = 0;
        fieldEffect.arg2 = 0;
        fieldEffect.arg3 = 1;
        fieldEffect.arg4 = endDelay;
        fieldEffect.string = path;
        fieldEffect.string2 = animationName;
        fieldEffect.string3 = str;
        fieldEffect.arg5 = 0;
        fieldEffect.arg6 = 0;
        fieldEffect.arg7 = 0;
        fieldEffect.arg8 = 0;
        fieldEffect.arg9 = 0;
        fieldEffect.string4 = null;

        return fieldEffect;
    }

    public static FieldEffect offSpineScreen(String keyName, int type, String aniName, int alphaDecayTime) {
        FieldEffect fieldEffect = new FieldEffect(FieldEffectType.OffSpineScreen);

        fieldEffect.string = keyName;
        fieldEffect.arg1 = type;
        fieldEffect.string2 = aniName;
        fieldEffect.arg2 = alphaDecayTime;

        return fieldEffect;
    }

    public FieldEffect(FieldEffectType fieldEffectType) {
        this.fieldEffectType = fieldEffectType;
    }

    public FieldEffect() {
    }

    public static byte[] OpenCUI() {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SetDirectionMode.getValue());
        mplew.write(0);
        mplew.write(1);
        return mplew.getPacket();
    }

    public FieldEffectType getFieldEffectType() {
        return fieldEffectType;
    }

    public void setFieldEffectType(FieldEffectType fieldEffectType) {
        this.fieldEffectType = fieldEffectType;
    }
}
