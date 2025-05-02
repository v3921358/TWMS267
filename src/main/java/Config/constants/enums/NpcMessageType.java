package Config.constants.enums;

import java.util.Arrays;

public enum NpcMessageType {
    Say(0),
    SayUnk(1),
    SayImage(2),
    AskYesNo(3),
    AskText(4, ResponseType.Text),
    AskNumber(5, ResponseType.Answer),
    AskMenu(6, ResponseType.Answer),
    InitialQuiz(7),
    InitialSpeedQuiz(8),
    ICQuiz(9),
    AskAvatar(10, ResponseType.Answer),
    AskAndroid(11, ResponseType.Answer),
    AskPet(12, ResponseType.Answer),
    AskPetAll(13, ResponseType.Answer),
    AskActionPetEvolution(14),
    SCRIPT(15),
    AskAccept(16),
    AskAcceptNoEsc(17),
    AskBoxtext(18, ResponseType.Text),
    AskSlideMenu(19, ResponseType.Answer),
    AskIngameDirection(20),
    PlayMovieClip(21, ResponseType.Answer),
    AskCenter(22),
    AskAvatar2(23),
    //
    AskOlympicQuiz(25),
    AskYesNoUnk(26),
    AskSelectMenu(27, ResponseType.Answer),
    AskAngelicBuster(28, ResponseType.Answer),
    SayIllustration(29),
    SayDualIllustration(30),
    AskYesNoIllustration(31),
    AskAcceptIllustration(32),
    AskMenuIllustration(33),
    AskYesNoDualIllustration(34),
    AskAcceptDualIllustration(35),
    AskMenuDualIllustration(36),
    Ask_SSN2(37),
    AskAvatarZero(38, ResponseType.Answer),
    Monologue(39),
    AskBoxTextBgImg(40),
    AskUserSurvey(41),
    SUCCESS_CAMERA(42),
    AskAvatarMixColor(43),
    AskAvatarMixColor2(44),
    SayAvatarMixColorChanged(45),
    //
    NPC_ACTION(47),
    OnAskScreenShinningStarMsg(48),
    OnAskNumberUseKeyPad(49, ResponseType.Text),
    OnSpinOffGuitarRhythmGame(50),
    OnGhostParkEnter(51),
    Unk(52 + 4),
    //
    //
    //
    //
    //
    //
    AskAvatarUnk(59),
    AskAvatarUnk2(60),
    //
    AskConfirmAvatarChange(62, ResponseType.Answer),
    //
    AskAvatarRandomMixColor(64, ResponseType.Answer),
    Unk1(65),
    //
    //
    //
    SayUnk1(69),
    SayDualUnk1(70),
    //
    //
    //
    //
    //
    //
    Unk9(77), // 熒幕頂部提示「用滑鼠(方向鍵)選擇目的地後，可用SpaceBar(Enter)來輸入。」下方是文字%s
    Unk10(78), // 熒幕頂部向上箭頭, 旁邊是文字提示%s
    None(-1),
    ;

    private final byte val;
    private final ResponseType responseType;

    NpcMessageType(int val) {
        this.val = (byte) val;
        this.responseType = ResponseType.Response;
    }

    NpcMessageType(int val, ResponseType responseType) {
        this.val = (byte) val;
        this.responseType = responseType;
    }

    public static NpcMessageType getByVal(byte val) {
        return Arrays.stream(values()).filter(v -> v.getVal() == val).findAny().orElse(None);
    }

    public byte getVal() {
        return val;
    }


    public ResponseType getResponseType() {
        return responseType;
    }

    public enum ResponseType {
        Response, Answer, Text
    }
}
