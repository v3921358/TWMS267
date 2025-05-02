package connection.packet;

import Client.inventory.Item;
import Config.constants.enums.NpcMessageType;
import Config.constants.enums.ScriptParam;
import Opcode.Headler.OutHeader;
import Plugin.script.NpcScriptInfo;
import connection.OutPacket;

public final class ScriptMan {

    public static OutPacket scriptMessage(NpcScriptInfo nsi, NpcMessageType nmt) {
        OutPacket outPacket = new OutPacket(OutHeader.LP_ScriptMessage);

        outPacket.encodeInt(nsi.getObjectID());
        outPacket.encodeByte(nsi.getSpeakerType());
        outPacket.encodeInt(nsi.getTemplateID());

        int overrideTemplate = nsi.getOverrideSpeakerTemplateID();
        boolean override = overrideTemplate > 0 || nsi.getInnerOverrideSpeakerTemplateID() > 0;
        outPacket.encodeByte(override);
        if (override) {
            outPacket.encodeInt(overrideTemplate);
        }
        outPacket.encodeByte(nmt.getVal());
        outPacket.encodeShort(nsi.getParam());
        outPacket.encodeByte(nsi.getColor());

        switch (nmt) {
            case Say:  // Npc Say
                outPacket.encodeInt(nsi.getIndex());
                if (ScriptParam.OverrideSpeakerID.check(nsi.getParam())) {
                    outPacket.encodeInt(nsi.getInnerOverrideSpeakerTemplateID());
                }
                outPacket.encodeString(nsi.getText());
                outPacket.encodeByte(nsi.isPrevPossible());
                outPacket.encodeByte(nsi.isNextPossible());
                outPacket.encodeInt(nsi.getDelay());
                outPacket.encodeByte(0);
                break;
            case SayUnk:
                outPacket.encodeString("");
                outPacket.encodeByte(0);
                outPacket.encodeByte(0);
                outPacket.encodeInt(0);
                break;
            case SayImage: // Talk - image photo
                String[] images = nsi.getImages();
                outPacket.encodeByte(images.length);
                for (String image : images) {
                    outPacket.encodeString(image);
                }
                break;
            case AskYesNo:
            case AskAccept:
            case AskMenu:
                if (ScriptParam.OverrideSpeakerID.check(nsi.getParam())) {
                    outPacket.encodeInt(nsi.getInnerOverrideSpeakerTemplateID());
                }
                outPacket.encodeString(nsi.getText());
                break;
            case AskText:
                if (ScriptParam.OverrideSpeakerID.check(nsi.getParam())) {
                    outPacket.encodeInt(nsi.getInnerOverrideSpeakerTemplateID());
                }
                outPacket.encodeString(nsi.getText());
                outPacket.encodeString(nsi.getDefaultText());
                outPacket.encodeShort((short) nsi.getMin());
                outPacket.encodeShort((short) nsi.getMax());
                break;
            case AskNumber:
                if (ScriptParam.OverrideSpeakerID.check(nsi.getParam())) {
                    outPacket.encodeInt(nsi.getInnerOverrideSpeakerTemplateID());
                }
                outPacket.encodeString(nsi.getText());
                outPacket.encodeLong(nsi.getDefaultNumber());
                outPacket.encodeLong(nsi.getMin());
                outPacket.encodeLong(nsi.getMax());
                break;
            case InitialQuiz:
                outPacket.encodeByte(nsi.getType());
                if (nsi.getType() != 1) {
                    outPacket.encodeString(nsi.getTitle());
                    outPacket.encodeString(nsi.getProblemText());
                    outPacket.encodeString(nsi.getHintText());
                    outPacket.encodeInt((int) nsi.getMin());
                    outPacket.encodeInt((int) nsi.getMax());
                    outPacket.encodeInt(nsi.getTime()); // in seconds
                }
                break;
            case InitialSpeedQuiz:
                outPacket.encodeByte(nsi.getType());
                if (nsi.getType() != 1) {
                    outPacket.encodeInt(nsi.getQuizType());
                    outPacket.encodeInt(nsi.getAnswer());
                    outPacket.encodeInt(nsi.getCorrectAnswers());
                    outPacket.encodeInt(nsi.getRemaining());
                    outPacket.encodeInt(nsi.getTime()); // in seconds
                }
                break;
            case ICQuiz:
                outPacket.encodeByte(nsi.getType());
                if (nsi.getType() != 1) {
                    outPacket.encodeString(nsi.getText());
                    outPacket.encodeString(nsi.getHintText());
                    outPacket.encodeInt(nsi.getTime()); // in seconds
                }
                break;
            case AskAvatar:
                outPacket.encodeInt(nsi.getItemID());
                outPacket.encodeString(nsi.getText());
                outPacket.encodeByte(nsi.getSecondLookValue());

                outPacket.encodeByte(nsi.getOptions().length);
                for (int option : nsi.getOptions()) {
                    outPacket.encodeInt(option);
                }
                outPacket.encodeInt(nsi.getSrcBeauty());
                break;
            case AskAndroid:
                outPacket.encodeInt(nsi.getItemID());
                outPacket.encodeString(nsi.getText());
                outPacket.encodeByte(nsi.getOptions().length);
                for (int option : nsi.getOptions()) {
                    outPacket.encodeInt(option);
                }
                outPacket.encodeInt(nsi.getSrcBeauty());
                break;
            case AskPet:
            case AskActionPetEvolution:
                outPacket.encodeString(nsi.getText());
                outPacket.encodeByte(nsi.getItems().size());
                for (Item item : nsi.getItems()) {
                    outPacket.encodeLong(item.getSN());
                    outPacket.encodeByte(item.getPosition());
                }
                break;
            case AskPetAll:
                outPacket.encodeString(nsi.getText());
                outPacket.encodeByte(nsi.getItems().size());
                outPacket.encodeByte(true);
                outPacket.encodeByte(true);
                for (Item item : nsi.getItems()) {
                    outPacket.encodeLong(item.getSN());
                    outPacket.encodeByte(item.getPosition());
                }
                break;
            case AskAcceptNoEsc:
                outPacket.encodeInt(0);
                outPacket.encodeString(nsi.getText());
                break;
            case AskBoxtext:
                if (ScriptParam.OverrideSpeakerID.check(nsi.getParam())) {
                    outPacket.encodeInt(nsi.getInnerOverrideSpeakerTemplateID());
                }
                outPacket.encodeString(nsi.getText());
                outPacket.encodeString(nsi.getDefaultText());
                outPacket.encodeShort(nsi.getCol());
                outPacket.encodeShort(nsi.getLine());
                break;
            case AskSlideMenu:
                outPacket.encodeInt(nsi.getDlgType());
                outPacket.encodeInt((nsi.getDlgType() == 0) ? nsi.getDefaultSelect() : 0);
                outPacket.encodeString(nsi.getText());
                break;
            case AskOlympicQuiz:
                outPacket.encodeByte(nsi.getType());
                if (nsi.getType() != 1) {
                    outPacket.encodeString("BeijingOlympic");
                    outPacket.encodeInt(nsi.getQuizType());
                    outPacket.encodeInt(nsi.getAnswer());
                    outPacket.encodeInt(nsi.getCorrectAnswers());
                    outPacket.encodeInt(nsi.getRemaining());
                    outPacket.encodeInt(nsi.getTime()); // in seconds
                }
                break;
            case AskYesNoUnk:
                outPacket.encodeString("");
                outPacket.encodeInt(0);
                break;
            case AskSelectMenu:
                outPacket.encodeInt(nsi.getDlgType());
                if (nsi.getDlgType() <= 1) {
                    outPacket.encodeInt(0);
                    outPacket.encodeByte(0);
                    outPacket.encodeInt(nsi.getDefaultSelect());
                    outPacket.encodeInt(nsi.getSelectText().length);
                    for (String selectText : nsi.getSelectText()) {
                        outPacket.encodeString(selectText);
                    }
                }
                break;
            case AskAngelicBuster:
                break;
            case SayIllustration:
            case SayDualIllustration:
                if (ScriptParam.OverrideSpeakerID.check(nsi.getParam())) {
                    outPacket.encodeInt(nsi.getInnerOverrideSpeakerTemplateID());
                }
                outPacket.encodeString(nsi.getText());
                outPacket.encodeByte(nsi.isPrevPossible());
                outPacket.encodeByte(nsi.isNextPossible());
                outPacket.encodeInt(nsi.getDelay());
                outPacket.encodeInt(nsi.getUnk());
                if (nmt == NpcMessageType.SayDualIllustration) {
                    outPacket.encodeInt(0);
                    outPacket.encodeInt(0);
                } else outPacket.encodeByte(nsi.isUnk());
                break;
            case AskYesNoIllustration:
            case AskAcceptIllustration:
            case AskYesNoDualIllustration:
            case AskAcceptDualIllustration:
                if (ScriptParam.OverrideSpeakerID.check(nsi.getParam())) {
                    outPacket.encodeInt(nsi.getInnerOverrideSpeakerTemplateID());
                }
                outPacket.encodeString(nsi.getText());
                outPacket.encodeInt(nsi.getDelay());
                outPacket.encodeInt(nsi.getUnk());
                if (nmt == NpcMessageType.AskYesNoDualIllustration || nmt == NpcMessageType.AskAcceptDualIllustration) {
                    outPacket.encodeInt(0);
                    outPacket.encodeInt(0);
                } else outPacket.encodeByte(nsi.isUnk());
                break;
            case AskMenuIllustration:
            case AskMenuDualIllustration:
                outPacket.encodeString(nsi.getText());
                outPacket.encodeInt(nsi.getDelay());
                outPacket.encodeInt(nsi.getUnk());
                if (nmt == NpcMessageType.AskMenuDualIllustration) {
                    outPacket.encodeInt(0);
                    outPacket.encodeInt(0);
                } else outPacket.encodeByte(nsi.isUnk());
                break;
            case AskAvatarZero:
                outPacket.encodeInt(nsi.getItemID());
                outPacket.encodeString(nsi.getText());
                outPacket.encodeByte(nsi.getOptions().length);
                for (int option : nsi.getOptions()) {
                    outPacket.encodeInt(option);
                }
                outPacket.encodeByte(nsi.getOptions2().length);
                for (int option : nsi.getOptions2()) {
                    outPacket.encodeInt(option);
                }
                outPacket.encodeInt(nsi.getSrcBeauty());
                outPacket.encodeInt(nsi.getSrcBeauty2());
                break;
            case AskBoxTextBgImg:
                outPacket.encodeShort(0);
                outPacket.encodeString("");
                outPacket.encodeString("");
                outPacket.encodeShort(0);
                outPacket.encodeShort(0);
                outPacket.encodeShort(0);
                outPacket.encodeShort(0);
                break;
            case AskUserSurvey:
                outPacket.encodeInt((int) nsi.getDefaultNumber());
                outPacket.encodeByte(1);
                outPacket.encodeString(nsi.getText());
                break;
            case AskAvatarMixColor:
            case AskAvatarMixColor2:
                outPacket.encodeInt(nsi.getItemID());
                outPacket.encodeString(nsi.getText());
                outPacket.encodeByte(nsi.getSecondLookValue());
                outPacket.encodeInt(nsi.getSrcBeauty());
                break;
            case SayAvatarMixColorChanged:
                outPacket.encodeString(nsi.getText());
                outPacket.encodeInt(nsi.getItemID());
                if (nsi.getItemID() > 0) {
                    outPacket.encodeByte(nsi.getSecondLookValue());
                    outPacket.encodeInt(nsi.getSrcBeauty());
                    outPacket.encodeInt(nsi.getDrtBeauty());
                    outPacket.encodeInt(nsi.getSrcBeauty2());
                    outPacket.encodeInt(nsi.getDrtBeauty2());
                }
                break;
            case OnAskNumberUseKeyPad:
                outPacket.encodeInt((int) nsi.getDefaultNumber());
                break;
            case OnSpinOffGuitarRhythmGame:
                int value = 0;
                outPacket.encodeInt(value);
                if (value == 0) {
                    outPacket.encodeInt(0);
                    outPacket.encodeInt(0);
                } else if (value == 1) {
                    outPacket.encodeInt(0);
                    outPacket.encodeString("");
                }
                break;
            case OnGhostParkEnter:
            case Unk: {
                int nCount = 0;
                outPacket.encodeInt(nCount);
                for (int i = 0; i < nCount; i++) {
                    outPacket.encodeInt(0);
                    outPacket.encodeInt(0);
                    outPacket.encodeInt(0);
                }
                break;
            }
            case AskAvatarUnk:
            case AskAvatarUnk2: {
                outPacket.encodeInt(0);
                outPacket.encodeString("");
                int nCount = 0;
                outPacket.encodeByte(nCount);
                for (int i = 0; i < nCount; i++) {
                    outPacket.encodeByte(0);
                    outPacket.encodeByte(0);
                    int nCount2 = 0;
                    outPacket.encodeByte(nCount2);
                    for (int j = 0; j < nCount2; j++) {
                        outPacket.encodeInt(0);
                    }
                }
                break;
            }
            case AskConfirmAvatarChange:
                outPacket.encodeInt(nsi.getItemID());
                outPacket.encodeByte(nsi.getSecondLookValue());
                outPacket.encodeByte(nsi.getSecondLookValue());
                outPacket.encodeInt(nsi.getSrcBeauty());
                if (nsi.getSecondLookValue() == 101)
                    outPacket.encodeInt(nsi.getSrcBeauty2());
                break;
            case AskAvatarRandomMixColor:
                outPacket.encodeInt(nsi.getItemID());
                outPacket.encodeByte(nsi.getSecondLookValue());
                outPacket.encodeString(nsi.getText());
                break;
            case Unk1:
                if (ScriptParam.OverrideSpeakerID.check(nsi.getParam())) {
                    outPacket.encodeInt(nsi.getInnerOverrideSpeakerTemplateID());
                }
                outPacket.encodeString(nsi.getText());
                outPacket.encodeString(nsi.getDefaultText());
                outPacket.encodeShort((short) nsi.getMin());
                outPacket.encodeShort((short) nsi.getMax());
                boolean unk = true;
                outPacket.encodeByte(unk);
                if (unk) {
                    outPacket.encodeArr("B5 90 3E 43 0E 00 00 00 F6 BA BB 30 90 BA BB 30 F9 BA BB 30 09 09 01 00 00 00 00 DF CD 4E 33 04 00 00 00 06 28 87 40");
                }
                break;
            case SayUnk1:
            case SayDualUnk1:
                if (ScriptParam.OverrideSpeakerID.check(nsi.getParam())) {
                    outPacket.encodeInt(nsi.getInnerOverrideSpeakerTemplateID());
                }
                outPacket.encodeString(nsi.getText());
                outPacket.encodeByte(nsi.isPrevPossible());
                outPacket.encodeByte(nsi.isNextPossible());
                outPacket.encodeInt(nsi.getDelay());
                outPacket.encodeInt(nsi.getUnk());
                if (nmt == NpcMessageType.SayDualUnk1) {
                    outPacket.encodeInt(0);
                    outPacket.encodeInt(0);
                } else outPacket.encodeByte(nsi.isUnk());
                break;
            case Unk9:
                outPacket.encodeString("");
                outPacket.encodeInt(0);
                break;
            case Unk10:
                outPacket.encodeString("");
                outPacket.encodeByte(0);
                outPacket.encodeByte(0);
                outPacket.encodeByte(0);
                outPacket.encodeByte(0);
                outPacket.encodeInt(0);
                break;
        }

        return outPacket;
    }
}
