package Config.constants.enums;

import Packet.BuddyListPacket;

public enum FriendOperationMode {

    FriendReq_LoadFriend((byte) 0),
    FriendReq_SetFriend((byte) 1),
    FriendReq_AcceptFriend((byte) 2),
    FriendReq_AcceptAccountFriend((byte) 3),
    FriendReq_DeleteFriend((byte) 4),
    FriendReq_DeleteAccountFriend((byte) 5),
    FriendReq_RefuseFriend((byte) 6),
    FriendReq_RefuseAccountFriend((byte) 7),
    FriendReq_NotifyLogin((byte) 8),
    FriendReq_NotifyLogout((byte) 9),
    FriendReq_IncMaxCount((byte) 10),
    FriendReq_ConvertAccountFriend((byte) 11),
    FriendReq_ModifyFriend((byte) 12),
    FriendReq_ModifyFriendGroup((byte) 13),
    FriendReq_ModifyAccountFriendGroup((byte) 14),
    FriendReq_SetOffline((byte) 15),
    FriendReq_SetOnline((byte) 16),
    FriendReq_SetBlackList((byte) 17),
    FriendReq_DeleteBlackList((byte) 18),
    FriendRes_LoadFriend_Done((byte) 19),
    FriendRes_LoadAccountIDOfCharacterFriend_Done((byte) 20),
    // 21
    // 22
    FriendRes_NotifyChange_FriendInfo((byte) 25),
    FriendRes_Invite((byte) 26),
    FriendRes_SetFriend_Done((byte) 27),
    FriendRes_SetFriend_FullMe((byte) 28),
    FriendRes_SetFriend_FullOther((byte) 29),
    FriendRes_SetFriend_AlreadySet((byte) 30),
    FriendRes_SetFriend_AlreadyRequested((byte) 31),
    FriendRes_SetFriend_Ready((byte) 32),
    FriendRes_SetFriend_CantSelf((byte) 33),
    FriendRes_SetFriend_Master((byte) 34),
    FriendRes_SetFriend_UnknownUser((byte) 35),
    FriendRes_SetFriend_Unknown((byte) 36),
    FriendRes_SetFriend_RemainCharacterFriend((byte) 37),
    // 36
    FriendRes_SetMessengerMode((byte) 39),
    FriendRes_SendSingleFriendInfo((byte) 40),
    FriendRes_AcceptFriend_Unknown((byte) 41),
    FriendRes_DeleteFriend_Done((byte) 42),
    FriendRes_DeleteFriend_Unknown((byte) 43),
    FriendRes_Notify((byte) 44),
    FriendRes_NotifyNewFriend((byte) 45),
    FriendRes_IncMaxCount_Done((byte) 46),
    FriendRes_IncMaxCount_Unknown((byte) 47),
    FriendRes_RefuseFriend((byte) 48);

    private final byte type;

    FriendOperationMode(final byte type) {
        this.type = type;
    }

    public byte getValue() {
        return type;
    }

    public static FriendOperationMode getByAction(byte type) {
        for (FriendOperationMode it : values()) {
            if (it.type == type) {
                return it;
            }
        }
        return null;
    }

    public final byte[] getPacket() {
        return BuddyListPacket.buddylistMessage(type);
    }
}
