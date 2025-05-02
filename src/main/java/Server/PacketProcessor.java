package Server;

import Client.MapleClient;
import Handler.OverseasHandler;
import Handler.warpToGameHandler;
import Opcode.Headler.InHeader;
import Opcode.Headler.OutHeader;
import Packet.*;
import Server.BossEventHandler.Demian.Demian;
import Server.BossEventHandler.Seren;
import Server.BossEventHandler.Will;
import Server.auction.AuctionHandler;
import Server.cashshop.handler.BuyCashItemHandler;
import Server.cashshop.handler.CashShopOperation;
import Server.cashshop.handler.CouponCodeHandler;
import Server.channel.MonsterCollectionHandler;
import Server.channel.handler.*;
import Server.login.handler.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.Randomizer;
import tools.data.MaplePacketLittleEndianWriter;
import tools.data.MaplePacketReader;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static Handler.warpToGameHandler.getChr;
import static Handler.warpToGameHandler.is墨玄招式lock;
import static Server.cashshop.handler.CashShopOperation.keyUI;

public final class PacketProcessor {
    private static final Logger log = LoggerFactory.getLogger(PacketProcessor.class);
    private static final Map<InHeader, PacketHandler> handlers = new HashMap<>();
    private static final List<Short> unkProcessOps = new LinkedList<>();

    public interface PacketHandler {
        void handlePacket(MaplePacketReader slea, MapleClient c, ServerType type);
    }


    public static void getProcessor(final InHeader header, final MaplePacketReader slea, final ServerType type, final MapleClient c) {
        switch (header) {
            case CP_StartDamageRecord:
                byte turn = slea.readByte();
                switch (turn) {
                    case 0: // false
                        c.getPlayer().setKeyValue("DamageGetKillCount", "0");
                        c.getPlayer().dropMessage(5, "清除戰鬥分析紀錄。");
                        break;
                    case 1: // true
                        c.getPlayer().setKeyValue("DamageGetKillCount", "1");
                        c.getPlayer().dropMessage(5, "啟動戰鬥分析紀錄。");
                        c.announce(UIPacket.startDamageRecord());
                        break;
                }
                break;
            case CP_UserCharacterInfoRequest: {
                c.announce(MaplePacketCreator.showCharacterInfo(slea, c.getPlayer()));
                break;
            }
            case BOSS_CARNING_WARP_BOSS: {
                MaplePacketLittleEndianWriter Carning = new MaplePacketLittleEndianWriter();
                Carning.writeShort(1994);
                int Buttion = slea.readInt();
                int SelBoss = slea.readInt();
                if (Buttion == 0) {
                    Carning.writeInt(3);
                    Carning.writeInt(c.getPlayer().getId());
                    Carning.writeInt(SelBoss);
                    Carning.writeInt(0);
                }
                if (Buttion == 1) {
                    Carning.writeInt(3);
                    Carning.writeInt(c.getPlayer().getId());
                    Carning.writeInt(2);
                    Carning.writeInt(0);
                }

                if (Buttion == 3) {
                    c.getPlayer().changeMap(410007025, 0);
                }
                c.announce(Carning.getPacket());
                break;
            }

            case BOSS_KARLOS_ONPACKET: {
                MaplePacketLittleEndianWriter karlos = new MaplePacketLittleEndianWriter();
                karlos.writeShort(OutHeader.BOSS_KALOS_ONPACKET.getValue());
                slea.skip(8);
                int unk1 = slea.readInt();
                int unk2 = slea.readInt();
                int unk3 = slea.readInt();
                karlos.writeInt(5);
                karlos.writeInt(28);
                karlos.writeInt(2);
                karlos.writeInt(c.getPlayer().getId());
                karlos.writeInt(1500);
                karlos.writeInt(unk1);
                karlos.writeInt(unk2);
                karlos.writeInt(unk3);
                c.getPlayer().dropMessage(-1, "USE: Karlos_action");
                c.announce(karlos.getPacket());
                break;
            }
            case CP_FUNCKEYMAP_UNIONKEY_BLOCK_NUMLOCK:
                c.outPacket(OutHeader.LP_PartyCandidateResult.getValue(), (byte) 0);
                break;
            case CP_ExceptionLog:
                ClientErrorLogHandler.handlePacket(slea, c);
                break;
            case CTX_DUMP_CLIENT_LOG:
                PacketErrorHandler.handlePacket(slea, c);
                break;
            case CTX_CLIENT_CRASH:
                log.info("玩家客戶端崩潰：accId=" + c.getAccID() + ", player=" + c.getPlayer());
                break;
            case CP_MigrateIn:
                InterServerHandler.Loggedin(slea, c, type);
                break;
            case GAME_EXIT:
            case CP_CloseWindow:
                PlayerHandler.GameExit(c);
                break;
            case CP_GetBuyCashPointUI:
                ShowAccCash.handlePacket(slea, c);
                break;
            case CP_QuickBuyItemByCS:
                PlayerHandler.quickBuyCashShopItem(slea, c, c.getPlayer());
                break;
            case REQUEST_CONNECTION:
                c.announce(LoginPacket.addConnection());
                break;
            case CP_CREAT_NEW_CHAR_2PW:
                c.announce(LoginPacket.checkSPWExistAttachResult());
                c.announce(LoginPacket.checkSPWExistResult(3, 0));
                break;
            case CP_CREAT_NEW_CHARACTER:
                CreateChar2Pw.handlePacket(slea, c);
                break;
            case Creat_New_Char_Check_Name:
                CheckCharNameHandler.handlePacket(slea, c);
                break;
            case Creat_New_Char:
                CreateCharHandler.handlePacket(slea, c);
                break;
            case CP_UpdateCharacterSelectList:
                UpdatePlayerSlots.handlePacket(slea);
                break;
            case CP_DeleteCharacter:
                DeleteCharHandler.handlePacket(slea, c);
                break;
            case CP_ReservedDeleteCharacterConfirm:
                DeleteCharHandler.ReservedDeleteCharacterConfirm(slea, c);
                break;
            case CP_ReservedDeleteCharacterCancel:
                DeleteCharHandler.ReservedDeleteCharacterCancel(slea, c);
                break;
            case CP_SelectCharacter:
                CharSelectedHandler.handlePacket(slea, c);
                break;
            case CP_DirectGoToField:
                CharSelectedHandler.handlePacket(slea, c);
                break;
            case CP_OpcodeEncryptionError:
                LoginPasswordHandler.handlerAuthKey(slea, c);
                break;
            case CP_CreateCharVerificationCode:
                SecurityPacketHandler.CreateVerify(slea, c);
                break;
            case CASH_SHOP_KEY:
                keyUI(c);
                break;
            case CP_CashShopQueryCashRequest:
                // 2430
                c.announce(MTSCSPacket.CashShopQueryCashResult(c.getPlayer()));
                c.announce(keyUI(c));
                break;
            case CP_CashShopChargeParamRequest:
                CashShopOperation.openRechargeWeb(c);
                break;
            case CP_UserTransferFieldRequest:
                switch (type) {
                    case CashShopServer:
                        CashShopOperation.LeaveCS(slea, c, c.getPlayer());
                        break;
                    case ChannelServer:
                        PlayerHandler.ChangeMap(slea, c, c.getPlayer());
                        c.write(warpToGameHandler.EquipRuneSetting());
                        break;
                }
                break;
            case CP_CashShopCashItemRequest:
                BuyCashItemHandler.CashShopCashItemRequest(slea, c, c.getPlayer());
                break;
            case CP_CashShopGiftMateInfoRequest:
                BuyCashItemHandler.商城送禮(slea, c, c.getPlayer());
                break;
            case CP_CashShopCheckMileageRequest:
                CashShopOperation.CheckMileageRequest(c);
                break;
            case CP_CashShopCheckCouponRequest:
                CouponCodeHandler.handlePacket(slea, c, c.getPlayer());
                break;
            case CP_MVP_SpecialPack_Request:
                BuyCashItemHandler.ReceiveMvpLevelPacket(slea, c.getPlayer());
                break;
            case MVP_RoyalPack_Request:
                BuyCashItemHandler.ReceiveMvpLevelPacket(slea, c.getPlayer());
                break;
            case MVP_GradePack_Request:
                BuyCashItemHandler.ReceiveMvpGradePacket(c.getPlayer());
                break;
            case OPEN_AVATAR_RANDOM_BOX:
                BuyCashItemHandler.openAvatarRandomBox(slea, c, c.getPlayer());
                break;
            case CP_AuctionRequest:
                AuctionHandler.AuctionRequest(slea, c, c.getPlayer());
                break;
            case CP_AuctionExit:
                AuctionHandler.AuctionExit(slea, c, c.getPlayer());
                break;
            case CHECK_THIS_WEEK_EVENT:
                c.announce(MaplePacketCreator.SystemProcess());
                break;
            case CP_SAVE_UNION_QUEST:
                PlayerHandler.handleMapleUnion(slea, c.getPlayer());
                break;
            case CP_HexaAction:
                HexaHandler.hexaActionHandler(slea, c, c.getPlayer());
                break;
            case CP_MemoRequest:
                ChatHandler.MemoRequest(slea, c, c.getPlayer());
                break;
            case CP_MemoInGameRequest:
                ChatHandler.MemoRequest(slea, c, c.getPlayer());
                break;
            case CP_PassiveskillInfoUpdate:
                PlayerHandler.PassiveSkillInfoUpdate(slea, c, c.getPlayer());
                break;
            case CP_UserTransferChannelRequest:
                InterServerHandler.ChangeChannel(slea, c, c.getPlayer());
                break;
            case CP_UserMigrateToCashShopRequest:
                InterServerHandler.enterCS(c, c.getPlayer());
                break;
            case CP_UserMigrateToAuctionRequest:
                AuctionHandler.EnterAuctionRequest(c, c.getPlayer());
                break;
            case CP_UserReturnEffectResponse:
                ItemScrollHandler.ReturnEffectConfirm(slea, c, c.getPlayer());
                break;
            case CLIENT_CHANGE_PLAYER_COOKIE:
                InterServerHandler.ChangePlayer(slea, c);
                break;
            case CP_UserMove:
                PlayerHandler.MovePlayer(slea, c, c.getPlayer());
                break;
            ///-------------------------{Attack}-------------------------------
            case CP_UserMeleeAttack:
                PlayerHandler.UserMeleeAttack(slea, c, c.getPlayer());
                useAttack(c);
                break;
            case CP_UserShootAttack:
                PlayerHandler.UserShootAttack(slea, c, c.getPlayer());
                useAttack(c);
                break;
            case CP_UserMagicAttack:
                PlayerHandler.UserMagicAttack(slea, c, c.getPlayer());
                useAttack(c);
                break;
            case CP_SummonedAttack:
                SummonHandler.UserSummonAttack(slea, c, c.getPlayer());
                useAttack(c);
                break;
            case CP_UserBodyAttack:
                PlayerHandler.UserBodyAttack(slea, c, c.getPlayer());
                useAttack(c);
                break;
            case CP_UserAreaDotAttack:
                PlayerHandler.UserAreaDotAttack(slea, c, c.getPlayer());
                useAttack(c);
                break;
            case UserSpotlightAttack:
                PlayerHandler.UserSpotlightAttack(slea, c, c.getPlayer());
                useAttack(c);
                break;
            case UserNonTargetForceAtomAttack:
                PlayerHandler.UserNonTargetForceAtomAttack(slea, c, c.getPlayer());
                useAttack(c);
                break;
            ///-------------------------{Attack END}-------------------------------
            case CP_SkillLoadRunTime:
                break;
            case CP_UserSkillUseRequest: {
                UserSkillUseHandler.userSkillUseRequest(slea, c, c.getPlayer());
                break;
            }
            case CP_UserRequestInstanceTable:
                ItemMakerHandler.ProfessionInfo(slea, c, c.getPlayer());
                break;
            case CRAFT_DONE:
                ItemMakerHandler.CraftComplete(slea, c, c.getPlayer());
                break;
            case CRAFT_MAKE:
                ItemMakerHandler.CraftMake(slea, c, c.getPlayer());
                break;
            case CRAFT_EFFECT:
                ItemMakerHandler.CraftEffect(slea, c, c.getPlayer());
                break;
            case CP_GatherRequest:
                ItemMakerHandler.StartHarvest(slea, c, c.getPlayer());
                break;
            case CP_GatherEndNotice:
                ItemMakerHandler.StopHarvest(slea, c, c.getPlayer());
                break;
            case CP_DecomposerRequest:
                ItemMakerHandler.MakeExtractor(slea, c, c.getPlayer());
                break;
            case CP_UserEquipmentEnchantWithSingleUIRequest:
                ItemScrollHandler.UseEquipEnchanting(slea, c, c.getPlayer());
                break;
            case CP_UserBagItemUseRequest:
                ItemMakerHandler.UseBag(slea, c, c.getPlayer());
                break;
            case CP_UserRecipeOpenItemUseRequest:
                ItemMakerHandler.UseRecipe(slea, c, c.getPlayer());
                break;
            case CP_AndroidMove:
                PlayerHandler.MoveAndroid(slea, c, c.getPlayer());
                break;
            case CP_FoxManActionSetUseRequest:
                SummonHandler.FoxManActionSetUseRequest(slea, c.getPlayer());
                break;
            case CP_UserEmotion:
                PlayerHandler.ChangeEmotion(slea.readInt(), c.getPlayer());
                break;
            case CP_AndroidEmotion:
                PlayerHandler.ChangeAndroidEmotion(slea.readInt(), c.getPlayer());
                break;
            case CP_UserHit:
                TakeDamageHandler.TakeDamage(slea, c, c.getPlayer());
                break;
            case CP_UserChangeStatRequest:
                PlayerHandler.Heal(slea, c.getPlayer());
                break;
            case CP_UserSkillFinishRequest:
                PlayerHandler.SpecialSkillUse(slea, c.getPlayer());
                break;
            case CP_UserSkillCancelRequest:
                PlayerHandler.CancelBuffHandler(slea.readInt(), c.getPlayer());
                break;
            case CP_UserEffectLocal:
                PlayerHandler.CancelMech(slea, c.getPlayer());
                break;
            case USE_AFFECTED_AREA_REQUEST:
                PlayersHandler.UseAffectedArea(slea, c, c.getPlayer());
                break;
            case CP_UserStatChangeItemCancelRequest:
                PlayerHandler.CancelItemEffect(slea.readInt(), c.getPlayer());
                break;
            case CTX_USER_SIT:
                //attackIngLock(c);
                //PlayerHandler.UseChair(slea, c, c.getPlayer());
                break;
            case CTX_USER_STAND:
                //attackIngLock(c);
                //PlayerHandler.CancelChair(slea.readShort(), c, c.getPlayer());
                break;
            case CP_UserActivateEffectItem:
                PlayerHandler.UseItemEffect(slea, c, c.getPlayer());
                break;
            case CP_UserActivateNickItem:
                PlayerHandler.UseTitleEffect(slea, c, c.getPlayer());
                break;
            case CP_MicroBuffEndTime:
                PlayerHandler.MicroBuffEndTime(slea, c.getPlayer());
                break;
            case CP_UserActivateDamageSkin:
                PlayerHandler.UseActivateDamageSkin(slea, c.getPlayer());
                break;
            case PHANTOM_SHRUOD:
                PlayerHandler.PhantomShroud(slea, c);
                break;
            case USE_ACTIVATE_DAMAGE_SKIN_PREMIUM:
                PlayerHandler.UseActivateDamageSkinPremium(slea, c.getPlayer());
                break;
            case CP_UserDamageSkinSaveRequest:
                PlayerHandler.UserSaveDamageSkin(slea, c, c.getPlayer());
                break;
            case BOSS_DEMIAN_FLY_ON_PACKET:
                c.getPlayer().send(Demian.AttackNode(slea, c.getPlayer()));
                break;
            case CP_UserSkillPrepareRequest:
                PlayerHandler.UserSkillPrepareRequest(slea, c, c.getPlayer());
                break;
            case SUPER_CANNON_REQUEST:
                PlayerHandler.UserSupserCannotRequest(slea, c.getPlayer());
                break;
            case CP_PhysicalCheck:
                SecurityPacketHandler.handlePhysicalCheckPacket(slea, c);
                break;
            case CP_QuickslotKeyMappedModified:
                PlayerHandler.QuickSlot(slea, c.getPlayer());
                break;
            case CP_FuncKeyMappedModified:
                PlayerHandler.ChangeKeymap(slea, c.getPlayer());
                break;
            case CP_UserPortalScriptRequest:
                PlayerHandler.ChangeMapSpecial(slea, c, c.getPlayer());
                break;
            case CP_UserPortalTeleportRequest:
                PlayerHandler.InnerPortal(slea, c, c.getPlayer());
                break;
            case CP_UserMapTransferRequest:
                PlayerHandler.TrockAddMap(slea, c, c.getPlayer());
                break;
            case CP_UserAntiMacroItemUseRequest:
                PlayersHandler.LieDetector(slea, c, c.getPlayer(), true);
                break;
            case CP_UserAntiMacroSkillUseRequest:
                PlayersHandler.LieDetector(slea, c, c.getPlayer(), false);
                break;
            case CP_UserAntiMacroQuestionResult:
                PlayersHandler.LieDetectorResponse(slea, c);
                break;
            case CP_UserAntiMacroRefreshRequest:
                PlayersHandler.LieDetectorRefresh(slea, c);
                break;
            case USER_HOWLING_STORM_STACK:
                PlayerHandler.UserHowlingStormStack(slea, c.getPlayer());
                break;
            case CP_SELECT_ANDROID_SHOP:
                PlayerHandler.AndroidShop(slea, c.getPlayer());
                break;
            case CP_UserCompleteNpcSpeech:
                PlayerHandler.CompleteNpcSpeech(slea, c.getPlayer());
                break;
            case AUTO_USE_JUDGEMENT:
                PlayerHandler.UserJudgement(slea, c.getPlayer());
                break;
            case CP_RequestIncCombo:
                PlayerHandler.AranCombo(c, c.getPlayer(), +1);
                break;  // v245 combo 處置
            case CP_RequestDecCombo:
                PlayerHandler.AranCombo(c, c.getPlayer(), -10);
                break;
            case CP_UserThrowGrenade:
                PlayerHandler.UserThrowGrenade(slea, c, c.getPlayer());
                break;
            case CP_UserDestroyGrenade:
                PlayerHandler.UserDestroyGrenade(slea, c.getPlayer());
                break;
            case CP_UserMacroSysDataModified:
                PlayerHandler.ChangeSkillMacro(slea, c.getPlayer());
                break;
            case CP_UserGivePopularityRequest:
                PlayersHandler.GiveFame(slea, c, c.getPlayer());
                break;
            case TRANSFORM_PLAYER:
                PlayersHandler.TransformPlayer(slea, c, c.getPlayer());
                break;
            case CP_EnterTownPortalRequest:
                PlayersHandler.requestEnterTownPortal(slea, c.getPlayer());
                break;
            case CP_EnterRandomPortalRequest:
                PlayersHandler.EnterRandomPortalRequest(slea, c.getPlayer());
                break;
            case CP_EnterOpenGateRequest:
                PlayersHandler.UseMechDoor(slea, c.getPlayer());
                break;
            case CP_ReactorHit:
                PlayersHandler.HitReactor(slea, c);
                break;
            case CP_ReactorClick:
                //c.getPlayer().send(PacketProcessor.DummyPacket((short)1640, "E6 0B 00 00 01 7D FF C6 01 00 00 00 08 07 00 00 9F 69 51 00"));
                PlayersHandler.TouchReactor(slea, c);
                break;
            case CP_ReactorRectInMob:
                PlayersHandler.ReactorRectInMob(slea, c);
                break;
            case CP_RuneStoneUseReq:
                PlayersHandler.UseRune(slea, c.getPlayer());
                break;
            case CP_RuneStoneSkillReq:
                PlayersHandler.UseRuneSkillReq(slea, c.getPlayer());
                break;
            case USE_RUNE_ACTION:
                PlayersHandler.UseRuneAction(slea, c.getPlayer());
                break;
            case CTX_FIELD_CHECK_SCRIPT:
                String index = Integer.toString(slea.readByte());
                slea.skip(5);
                String difficulty = Integer.toString(slea.readByte());
                c.getPlayer().getScriptManager().startBossUIScript(9900003, index, difficulty);
                break;
            case BOSS_CARNING_PARTY_RECV: {
                int mode = slea.readInt();
                switch (mode) {
                    case 0:
                        MaplePacketLittleEndianWriter sel = new MaplePacketLittleEndianWriter();
                        sel.writeShort(OutHeader.BOSS_CARNING_OUT_PACKET_1995.getValue());
                        int selslot = slea.readInt();
                        sel.writeInt(3);
                        sel.writeInt(c.getPlayer().getId());
                        sel.writeInt(selslot);
                        sel.writeInt(0);
                        c.getPlayer().send(sel.getPacket());
                        if (selslot == 0) {
                            // GOONGI_FIELD_DIR
                            c.getPlayer().setKeyValue("KarNing_Boss", String.valueOf(c.getPlayer().getMapId() + 20));
                        }
                        if (selslot == 1) {
                            // DOOL_FIELD_DIR
                            c.getPlayer().setKeyValue("KarNing_Boss", String.valueOf(c.getPlayer().getMapId() + 60));
                        }
                        if (selslot == 2) {
                            // HONDON_FIELD_DIR
                            c.getPlayer().setKeyValue("KarNing_Boss", String.valueOf(c.getPlayer().getMapId() + 100));
                        }
                        break;
                    case 1:
                        boolean checkCanWarpToMAP = slea.readBool();
                        if (checkCanWarpToMAP) {
                            c.getPlayer().changeMap(Integer.parseInt(c.getPlayer().getKeyValue("KarNing_Boss")), 0);
                        }
                        break;
                    case 3:
                        c.getPlayer().changeMap(410007025, 0);
                        break;
                }
                break;
            }
            case MOBZONESTATE_RESULT:
                PlayersHandler.MobZoneStateResult(slea, c.getPlayer());
                break;
            case PeacemakerHeal:
                PlayersHandler.PeacemakerHeal(slea, c.getPlayer());
                break;
            case CP_UserADBoardClose:
                c.getPlayer().setChalkboard(null);
                break;
            case CP_UserGatherItemRequest:
                InventoryHandler.ItemGather(slea, c);
                break;
            case CP_UserSortItemRequest:
                InventoryHandler.ItemSort(slea, c);
                MaplePacketCreator.ExclRequest();
                break;
            case CP_UserChangeSlotPositionRequest:
                InventoryHandler.ItemMove(slea, c);
                break;
            case CP_UserPopOrPushBagItemToInven:
                InventoryHandler.MoveBag(slea, c);
                break;
            case CP_UserBagToBagItem:
                InventoryHandler.SwitchBag(slea, c);
                break;
            case CP_UserItemMakeRequest:
                ItemMakerHandler.ItemMaker(slea, c);
                break;
            case CP_DropPickUpRequest:
                InventoryHandler.Pickup_Player(slea, c, c.getPlayer());
                break;
            case CP_UserToadsHammerRequest:
                InventoryHandler.UseToadsHammer(slea, c.getPlayer());
                break;
            case CP_UserToadsHammerHelpRequest:
                //NPCScriptManager.dispose(c);
                break;
            case CP_UserConsumeCashItemUseRequest:
                UseCashItemHandler.handlePacket(slea, c, c.getPlayer());
                break;
            case CP_UserAdditionalSlotExtendItemUseRequest:
                InventoryHandler.UseAdditionalAddItem(slea, c, c.getPlayer());
                break;
            case CP_UserStatChangeItemUseRequest:
                InventoryHandler.UseItem(slea, c, c.getPlayer());
                break;
            case CP_UserConsumeHairItemUseRequest:
                InventoryHandler.UseCosmetic(slea, c, c.getPlayer());
                break;
            case UserConsumeHairMixItemUseRequest:
                InventoryHandler.ConsumeMixHairItemUseRequest(slea, c, c.getPlayer());
                break;
            case LapidificationStateChange:
                PlayersHandler.LapidificationStateChange(slea, c.getPlayer(), c);
                break;
            case CP_USER_POTENTIAL_SKILL_RAND_SET:
                InventoryHandler.UseReducer(slea, c, c.getPlayer());
                break;
            case CP_USER_POTENTIAL_SKILL_RAND_STAT_SET:
                InventoryHandler.UseReducerPrestige(slea, c, c.getPlayer());
                break;
            case CP_USER_POTENTIAL_SKILL_RAND_RAND_SET_UI:
                InventoryHandler.UseReducerPrestige(slea, c, c.getPlayer());
                break;
            case CP_UserItemReleaseRequest: // 道具鑑定放大鏡
                InventoryHandler.UseMagnify(slea, c, c.getPlayer());
                break;
            case CP_UserMemorialCubeOptionRequest:
                InventoryHandler.applyBlackCube(slea, c, c.getPlayer());
                break;
            case CP_UserScriptItemUseRequest:
                InventoryHandler.UseScriptedNPCItem(slea, c, c.getPlayer());
                break;
            case CP_PLAYER_USE_GO_HOME_SCROLL:
                InventoryHandler.UseReturnScroll(slea, c, c.getPlayer());
                break;
            case CP_UserCharSlotIncItemUseRequest:
                ItemScrollHandler.handlePacket(slea, c, c.getPlayer(), false);
                break;
            case CP_UserUpgradeItemUseRequest:
                ItemScrollHandler.handlePacket(slea, c, c.getPlayer(), false);
                break;
            case CP_UserExItemUpgradeItemUseRequest:
                ItemScrollHandler.userSelectExItemUpgradeItemUseRequest(slea, c, c.getPlayer());
                break;
            case CP_UserUpgradeAssistItemUseRequest:
                ItemScrollHandler.handlePacket(slea, c, c.getPlayer(), true);
                break;
            case CP_UserItemOptionUpgradeItemUseRequest:
                ItemScrollHandler.handlePacket(slea, c, c.getPlayer(), false);
                break;
            case CP_UserAdditionalOptUpgradeItemUseRequest:
                ItemScrollHandler.handlePacket(slea, c, c.getPlayer(), false);
                break;
            case CP_UserHyperUpgradeItemUseRequest:
                ItemScrollHandler.handlePacket(slea, c, c.getPlayer(), false);
                break;
            case CP_UserItemSlotExtendItemUseRequest:
                InventoryHandler.UseAdditionalItem(slea, c, c.getPlayer());
                break;
            case CP_UserWeaponTempItemOptionRequest:
                InventoryHandler.UserWeaponTempItemOptionRequest(slea, c, c.getPlayer());
                break;
            case CP_UserItemSkillSocketUpgradeItemUseRequest:
                InventoryHandler.UserItemSkillSocketUpgradeItemUseRequest(slea, c, c.getPlayer());
                break;
            case CP_UserItemSkillOptionUpgradeItemUseRequest:
                InventoryHandler.UserItemSkillOptionUpgradeItemUseRequest(slea, c, c.getPlayer());
                break;
            case CP_UserFreeMiracleCubeItemUseRequest:
                InventoryHandler.UseMiracleCube(slea, c.getPlayer());
                break;
            case CP_UserMobSummonItemUseRequest:
                InventoryHandler.UseSummonBag(slea, c, c.getPlayer());
                break;
            case USE_TREASUER_CHEST:
                break;
            case CP_UserTamingMobFoodItemUseRequest:
                InventoryHandler.UseMountFood(slea, c, c.getPlayer());
                break;
            case REWARD_ITEM:
                InventoryHandler.UseRewardItem(slea.readShort(), slea.readInt(), c, c.getPlayer());
                break;
            case CP_CrossHunterShopRequest:
                InventoryHandler.BuyCrossHunterItem(slea, c, c.getPlayer());
                break;
            case CP_MobMove:
                MobHandler.MoveMonster(slea, c, c.getPlayer());
                break;
            case CP_MobApplyCtrl:
                MobHandler.MobApplyCtrl(slea.readInt(), c.getPlayer());
                break;
            case CP_MobSelfDestruct:
                MobHandler.MobSelfDestruct(slea.readInt(), c.getPlayer());
                break;
            case CP_MobAreaAttackDisease:
                MobHandler.MobAreaAttackDisease(slea, c.getPlayer());
                break;
            case CP_UserShopRequest:
                NPCHandler.NPCShop(slea, c, c.getPlayer());
                break;
            case CP_PLAYER_NPC_TALK:
                NPCHandler.NPCTalk(slea, c, c.getPlayer());
                break;
            case CP_UserScriptMessageAnswer:
                NPCHandler.userScriptMessageAnswer(slea, c);
                break;
            case CP_Field_Npc_Move:
            case CP_Field_Npc_Action:
                NPCHandler.NPCAnimation(slea, c);
                break;
            case CP_UserQuestRequest:
                NPCHandler.QuestAction(slea, c, c.getPlayer());
                break;
            case CP_UserMedalReissueRequest:
                PlayerHandler.ReIssueMedal(slea, c, c.getPlayer());
                break;
            case CP_UserTrunkRequest:
                NPCHandler.Storage(slea, c, c.getPlayer());
                break;
            case CP_UserChat:
                ChatHandler.UserChat(slea, c, c.getPlayer(), false);
                break;
            case CP_UserItemChat:
                ChatHandler.UserChat(slea, c, c.getPlayer(), true);
                break;
            case CP_GroupMessage:
                ChatHandler.Others(slea, c, c.getPlayer(), false);
                break;
            case CP_GroupItemMessage:
                ChatHandler.Others(slea, c, c.getPlayer(), true);
                break;
            case CP_Whisper:
                ChatHandler.Whisper_Find(slea, c, false);
                break;
            case CP_ItemWhisper:
                ChatHandler.Whisper_Find(slea, c, true);
                break;
            case CP_Messenger:
                ChatHandler.Messenger(slea, c);
                break;
            case CP_LikePoint:
                ChatHandler.ShowLoveRank(slea, c);
                break;
            case CP_RequestArrowPlaterObj:
                PlayerHandler.SpawnArrowsTurret(slea, c, c.getPlayer());
                break;
            case USE_GROWTH_HELPER_REQUEST:
                PlayerHandler.UserGrowthHelperRequest(slea, c, c.getPlayer());
                break;
            case ENTER_STARTPLANET:
                c.dropMessage("此功能暫未開放！");
                break;
            case TRACK_FLAMES:
                PlayerHandler.showTrackFlames(slea, c, c.getPlayer());
                break;
            case CP_UserAbilityMassUpRequest:
                StatsHandling.AutoAssignAP(slea, c, c.getPlayer());
                break;
            case CP_UserAbilityUpRequest:
                StatsHandling.DistributeAP(slea, c, c.getPlayer());
                break;
            case CP_UserSkillUpRequest:
                StatsHandling.DistributeSP(slea, c, c.getPlayer());
                break;
            case CP_UserHyperStatSkillUpRequest:
                c.getPlayer().updateTick(slea.readInt());
                StatsHandling.DistributeHyperAP(slea.readInt(), slea.readInt(), slea.readInt(), c, c.getPlayer());
                break;
            case CP_UserHyperSkillUpRequest:
                c.getPlayer().updateTick(slea.readInt());
                StatsHandling.DistributeHyperSP(slea.readInt(), c, c.getPlayer());
                break;
            case DRAGON_HIT:
                MaplePacketLittleEndianWriter union = new MaplePacketLittleEndianWriter();
                union.writeShort(1512);
                int point = slea.readInt();
                union.writeInt(point);
                union.writeInt(1);
                union.write(0);
                c.announce(union.getPacket());
                break;
            case CP_UserHyperSkillResetRequset:
                StatsHandling.ResetHyperSP(slea, c, c.getPlayer());
                break;
            case CP_UserHyperStatSkillResetRequest:
                StatsHandling.ResetHyperAP(c, c.getPlayer(), false, slea.readInt(), slea.readInt());
                HyperPacket.getHyperUIReset();
                break;
            case CP_UserHyperStatSkillChangePresetRequest:
                StatsHandling.ChangeHyperAPPreset(slea, c, c.getPlayer());
                break;
            case CP_MiniRoom:
                PlayerInteractionHandler.PlayerInteraction(slea, c, c.getPlayer());
                break;
            case CP_GuildRequest:
                GuildHandler.Guild(slea, c);
                break;
            case CP_GuildResult:
                int ResultType = slea.readInt();
                short LeaderNameSize = slea.readShort();
                String name = slea.readMapleAsciiString();
                GuildHandler.DenyGuildRequest(name, c);
                break;
            case UNION_WAR_EXIT:
                c.getPlayer().warpdelay(921172200, 1);
                break;
            case CP_GuildJoinRequest:
                GuildHandler.GuildApply(slea, c);
                break;
            case CP_GuildJoinAccept:
                GuildHandler.AcceptGuildApply(slea, c);
                break;
            case CP_GuildJoinReject:
                GuildHandler.DenyGuildApply(slea, c);
                break;
            case CP_MakeEnterFieldPacketForQuickMove:
                NPCHandler.OpenQuickMoveNpc(slea, c, c.getPlayer());
                break;
            case USER_OPNE_DAMAGE_SKIN_UI:
                c.announce(InventoryPacket.UserDamageSkinSaveResult(3, 1, c.getPlayer()));
                break;
            case CP_UserQuickMoveScript:
                NPCHandler.OpenQuickMoveNpcScript(slea, c, c.getPlayer());
                break;
            case BBS_OPERATION:
                BBSHandler.BBSOperation(slea, c);
                break;
            case SELECT_JAGUAR:
                PlayerHandler.selectJaguar(slea, c, c.getPlayer());
                break;
            case GIVE_KSPSYCHIC:
                PlayerHandler.showKSPsychicGrabHanlder(slea, c, c.getPlayer());
                break;
            case ATTACK_KSPSYCHIC:
                PlayerHandler.showKSPsychicAttackHanlder(slea, c, c.getPlayer());
                break;
            case CANCEL_KSPSYCHIC:
                PlayerHandler.showKSPsychicReleaseHanlder(slea, c, c.getPlayer());
                break;
            case GIVE_KSULTIMATE:
                PlayerHandler.showGiveKSUltimate(slea, c, c.getPlayer());
                break;
            case ATTACK_KSULTIMATE:
                PlayerHandler.showAttackKSUltimate(slea, c, c.getPlayer());
                break;
            case MIST_KSULTIMAT:
                PlayerHandler.showKSMonsterEffect(slea, c, c.getPlayer());
                break;
            case CANCEL_KSULTIMATE:
                PlayerHandler.showCancelKSUltimate(slea, c, c.getPlayer());
                break;
            case TORNADO_KSULTIMATE:
                PlayerHandler.showTornadoKSUltimate(slea, c, c.getPlayer());
                break;
            case CP_ADD_FRIEND:
                BuddyListHandler.BuddyOperation(slea, c);
                break;
            case CP_UserRequestCreateItemPot:
                ItemMakerHandler.UsePot(slea, c);
                break;
            case CP_UserRequestRemoveItemPot:
                ItemMakerHandler.ClearPot(slea, c);
                break;
            case CP_UserRequestIncItemPotLifeSatiety:
                ItemMakerHandler.FeedPot(slea, c);
                break;
            case CP_UserRequestCureItemPotLifeSick:
                ItemMakerHandler.CurePot(slea, c);
                break;
            case CP_UserRequestComplateToItemPot:
                ItemMakerHandler.RewardPot(slea, c);
                break;
            case CP_SummonedHit:
                SummonHandler.DamageSummon(slea, c.getPlayer());
                break;
            case CP_SummonedMove:
                SummonHandler.MoveSummon(slea, c.getPlayer());
                break;
            case CP_DragonMove:
                SummonHandler.MoveDragon(slea, c.getPlayer());
                break;
            case CP_DragonGlide:
                SummonHandler.DragonFly(slea, c.getPlayer());
                break;
            case CP_SummonedSkill:
                SummonHandler.SummonedSkill(slea, c, c.getPlayer());
                break;
            case SummonedMagicAltar:
                SummonHandler.SummonedMagicAltar(slea, c, c.getPlayer());
                break;
            case CP_SummonedRemove:
                SummonHandler.RemoveSummon(slea, c);
                break;
            case CP_SummonedAction:
                SummonHandler.SummonedAction(slea, c, c.getPlayer());
                break;
            case SummonedSarahAction:
                SummonHandler.SummonedSarahAction(slea, c, c.getPlayer());
                break;
            case SummonedJavelinAction:
                SummonHandler.SummonedJavelinAction(slea, c, c.getPlayer());
                break;
            case CP_UserActivatePetRequest:
                PetHandler.SpawnPet(slea, c, c.getPlayer());
                break;
            case CP_UserRegisterPetAutoBuffRequest:
                PetHandler.Pet_AutoBuff(slea, c, c.getPlayer());
                break;
            case PREVIEW_CHOICE_BEAUTY_CARD:
                PlayerHandler.previewChoiceBeautyCard(slea, c, c.getPlayer());
                break;
            case CP_PetMove:
                PetHandler.MovePet(slea, c.getPlayer());
                break;
            case CP_PetAction:
                PetHandler.PetChat(slea, c, c.getPlayer());
                break;
            case CP_PetInteractionRequest:
                PetHandler.PetCommand(slea, c, c.getPlayer());
                break;
            case CP_UserPetFoodItemUseRequest:
                PetHandler.PetFood(slea, c, c.getPlayer());
                break;
            case CP_PetDropPickUpRequest:
                InventoryHandler.Pickup_Pet(slea, c, c.getPlayer());
                break;
            case CP_PetStatChangeItemUseRequest:
                PetHandler.Pet_AutoPotion(slea, c, c.getPlayer());
                break;
            case CP_PetUpdateExceptionListRequest:
                PetHandler.PetExcludeItems(slea, c, c.getPlayer());
                break;
            case CP_PetFoodItemUseRequest:
                slea.skip(4);
                PetHandler.PetFood(slea, c, c.getPlayer());
                break;
            case CP_UserCashPetPickUpOnOffRequest:
                PetHandler.AllowPetLoot(slea, c, c.getPlayer());
                break;
            case CTX_OPEN_CORE:
                slea.skip(4);
                int use = slea.readInt();
                for (int i = 0; i < use; i++) {
                    c.getPlayer().gainRandVSkill(Randomizer.isSuccess(20) ? 0 : 1, true, true);
                }
                c.getPlayer().removeItem(2436078, use);
                break;
            case CP_UserCashPetSkillSettingRequest:
                PetHandler.AllowPetAutoEat(slea, c, c.getPlayer());
                break;
            case CP_UserParcelRequest:
                DueyHandler.DueyOperation(slea, c);
                break;
            case CP_UserEntrustedShopRequest:
                HiredMerchantHandler.UseHiredMerchant(c, true);
                break;
            case CP_UserStoreBankRequest:
                HiredMerchantHandler.MerchantItemStore(slea, c);
                break;
            case CP_UserRepairDurability:
                NPCHandler.repair(slea, c);
                break;
            case CP_UserRepairDurabilityAll:
                NPCHandler.repairAll(c);
                break;
            case CP_ShopScannerRequest:
                InventoryHandler.Owl(slea, c);
                break;
            case CP_ShopLinkRequest:
                InventoryHandler.OwlWarp(slea, c);
                break;
            case CP_UserShopScannerItemUseRequest:
                InventoryHandler.OwlMinerva(slea, c);
                break;
            case CP_RPSGame:
                NPCHandler.RPSGame(slea, c);
                break;
            case CP_UserFollowCharacterRequest:
                PlayersHandler.FollowRequest(slea, c);
                break;
            case CP_SetPassenserResult:
                PlayersHandler.FollowReply(slea, c);
                break;
            case CP_UserFollowCharacterWithdraw:
                PlayersHandler.FollowReply(slea, c);
                break;
            case CP_MarriageRequest:
                PlayersHandler.RingAction(slea, c);
                break;
            case GACH_EXP:
                PlayersHandler.GachExp(slea, c);
                break;
            case ITEM_WORLD_WARP_SEEN:
                InventoryHandler.TeleRock(slea, c);
                break;
            case CP_UserClaimRequest:
                PlayersHandler.Report(slea, c);
                break;
            case CP_UserRemoteShopOpenRequest:
                HiredMerchantHandler.RemoteStore(slea, c);
                break;
            case USE_CONTENT_MAP_MINI:
                PlayerHandler.UseContentMap(slea, c, c.getPlayer());
                break;
            case USE_CONTENT_MAP:
                PlayerHandler.UseContentMap(slea, c, c.getPlayer());
                break;
            case CP_UserTransferFreeMarketRequest:
                PlayerHandler.ChangeMarketMap(slea, c, c.getPlayer());
                break;
            case DEL_TEACH_SKILL:
                PlayerHandler.DelTeachSkill(slea, c, c.getPlayer());
                break;
            case SET_TEACH_SKILL: // 傳授學習
                PlayerHandler.SetTeachSkill(slea, c, c.getPlayer());
                break;
            case SET_CHAR_CASH:
                PlayerHandler.showPlayerCash(slea, c);
                break;
            case CP_UserFieldTransferRequest:
                PlayerHandler.fieldTransferRequest(slea, c.getPlayer());
                break;
            case OPEN_WORLD_MAP:
                c.announce(MaplePacketCreator.openWorldMap());
                break;
            case Auto5thRevenant_ReduceAnger:
                PlayerHandler.Auto5thRevenant_ReduceAnger(slea, c.getPlayer());
                break;
            case Auto5thRevenant_ReduceHP:
                PlayerHandler.Auto5thRevenantReduceHP(slea, c.getPlayer());
                break;
            case SilhouetteMirageCharge:
                PlayerHandler.SilhouetteMirageCharge(slea, c.getPlayer());
                break;
            case UseJupiterThunder:
                PlayerHandler.UseJupiterThunder(slea, c.getPlayer());
                break;
            case JupiterThunderAction:
                PlayerHandler.JupiterThunderAction(slea, c.getPlayer());
                break;
            case JupiterThunderEnd:
                PlayerHandler.JupiterThunderEnd(slea, c.getPlayer());
                break;
            case Auto5thGoddessBless:
                PlayerHandler.Auto5thGoddessBless(slea, c.getPlayer());
                break;
            case CP_GoldHammerRequest:
                UseHammerHandler.UseHammer(slea, c);
                break;
            case CP_GoldHammerComplete:
                slea.skip(4);
                c.announce(MTSCSPacket.sendGoldHammerResult(2, slea.readInt()));
                break;
            case CP_PlatinumHammerRequest:
                UseHammerHandler.PlatinumHammerResponse(slea, c);
                break;
            //case CP_StartDamageRecord:
            //    PlayerHandler.startBattleStatistics(slea, c);
            //    break;
            case CP_UserRequestSetStealSkillSlot:
                PhantomMemorySkill.MemorySkillChoose(slea, c);
                break;
            case CP_UserRequestStealSkillMemory:
                PhantomMemorySkill.MemorySkillChange(slea, c);
                break;
            case CP_UserRequestStealSkillList:
                PhantomMemorySkill.UserRequestStealSkillList(slea, c);
                break;
            case PLAYER_VIEW_RANGE:
                byte by2 = slea.readByte();
                if (by2 == 1) {
                    c.sendEnableActions();
                }
                break;
            // case MAP_HEART_BEAT:

            //     break;
            case CP_EgoEquipCreateUpgradeItemCostRequest:
                EgoEquipHandler.EgoEquipCreateUpgradeItemCostRequest(slea, c, c.getPlayer());
                break;
            case CP_EgoEquipCreateUpgradeItem:
                ItemScrollHandler.ChangeWeaponPotential_WP(slea, c, c.getPlayer());
                break;
            case CP_EgoEquipGaugeCompleteReturn:
                EgoEquipHandler.EgoEquipGaugeCompleteReturn(slea, c, c.getPlayer());
                break;
            case CP_EgoEquipTalkRequest:
                EgoEquipHandler.EgoEquipTalkRequest(slea, c, c.getPlayer());
                break;
            case CP_EgoEquipCheckUpdateItemRequest:
                EgoEquipHandler.EgoEquipCheckUpdateItemRequest(slea, c, c.getPlayer());
                break;
            case CP_InheritanceInfoRequest:
                EgoEquipHandler.InheritanceInfoRequest(slea, c, c.getPlayer());
                break;
            case CP_InheritanceUpgradeRequest:
                EgoEquipHandler.InheritanceUpgradeRequest(slea, c, c.getPlayer());
                break;
            case CP_UserRequestFlyingSwordStart:
                PlayerHandler.useTempestBlades(slea, c, c.getPlayer());
                break;
            case BossPartyCheckRequest:
                PlayerHandler.WarpTOBossEventMap(slea, c);
                break;
            case CP_WaitQueueRequest:
                PlayerHandler.WaitQueueRequest(slea, c);
                break;
            case CP_TouchPlayer: // 右鍵查閱玩家資訊反射封包 575 > 1141 [v266.3]
                c.announce(MaplePacketCreator.CheckTrickOrTreatRequest());
                break;
            case CP_CheckProcess:
                SystemProcess.SystemProcess(slea, c, c.getPlayer());
                break;
            case SOUL_MODE:
                PlayerHandler.updateSoulEffect(slea, c, c.getPlayer());
                break;
            case USE_TOWERCHAIR_SETTING:
                PlayerHandler.UseTowerChairSetting(slea, c, c.getPlayer());
                break;
            case VMATRIX_MAKE_REQUEST: /// 製作
                PlayerHandler.VCoreOperation(slea, c, c.getPlayer());
                break;
            case VMATRIX_HELP_REQUEST:
                PlayerHandler.VmatrixHelpRequest(slea, c.getPlayer());
                break;
            case VMATRIX_VERIFY:
                PlayerHandler.VmatrixVerify(slea, c);
                break;
            case SkillStageChangeRequest:
                PlayerHandler.SkillStageChangeRequest(slea, c, c.getPlayer());
                break;
//            //case HAPPY_DAY :
//            //PlayerHandler.DailyGift(c);
//            //break; }
            case HAPPY_DAY:
                int giftID = slea.readInt();
                MaplePacketLittleEndianWriter gift = new MaplePacketLittleEndianWriter();
                gift.writeShort(2478);
                gift.write(2);
                gift.writeInt(0);
                gift.writeInt(giftID);
                c.announce(gift.getPacket());
                break;
            case SKILL_ONOFF:
                PlayerHandler.handleSkillOnOff(slea, c, c.getPlayer());
                break;
            case AvatarEffectSkillOnOff:
                PlayerHandler.AvatarEffectSkillOnOff(slea, c, c.getPlayer());
                break;
            case RevolvingCannonRequest:
                PlayerHandler.handleRevolvingCannon(slea, c.getPlayer());
                break;
            case MULTI_SKILL_ATTACK_REQUEST:
                PlayerHandler.MultiSkillAttackRequest(slea, c, c.getPlayer());
                break;
            case MULTI_SKILL_CHARGE_REQUEST:
                PlayerHandler.MultiSkillChargeRequest(slea, c.getPlayer());
                break;
            case MULTI_SKILL_TIMEOUT_CHARGE_REQUEST:
                PlayerHandler.MultiSkillTimeoutChargeRequest(slea, c.getPlayer());
                break;
            case ReincarnationModeSelect:
                PlayerHandler.ReincarnationModeSelect(slea, c.getPlayer());
                break;
            case CreateForceAtomObject:
                PlayerHandler.CreateForceAtomObject(slea, c.getPlayer());
                break;
            case DivineJudgmentStatReset:
                PlayerHandler.DivineJudgmentStatReset(slea, c.getPlayer());
                break;
            case SpeedMirageObjectCreate:
                PlayerHandler.SpeedMirageObjectCreate(slea, c.getPlayer());
                break;
            case ApplyAffectAreaEffect:
                PlayerHandler.ApplyAffectAreaEffect(slea, c.getPlayer());
                break;
            case CTX_EVENT_UI_BUTTON_REQUEST:
                PlayerHandler.EventUIBottonHandler(slea, c.getPlayer());
                break;
            case CHARGE_INFINITE_FLAME:
                PlayerHandler.ChargeInfiniteFlame(slea, c);
                break;
            case CHARGE_PRIMAL_GRENADE:
                PlayerHandler.ChargePrimalGrenade(slea, c);
                break;
            case ADELE_CHARGE_REQUEST:
                AdeleHandler.AdeleChargeRequest(slea, c, c.getPlayer());
                break;
            case MaliceChargeRequest:
                PlayerHandler.MaliceChargeRequest(slea, c, c.getPlayer());
                break;
            case LaraSkillChargeRequest:
                PlayerHandler.LaraSkillChargeRequest(slea, c, c.getPlayer());
                break;
            case PoisonAreaCreate:
                PlayerHandler.PoisonAreaCreate(slea, c, c.getPlayer());
                break;
            case POTION_POT_USE:
                PotionPotHandler.PotionPotUse(slea, c, c.getPlayer());
                break;
            case POTION_POT_ADD:
                PotionPotHandler.PotionPotAdd(slea, c, c.getPlayer());
                break;
            case POTION_POT_MODE:
                PotionPotHandler.PotionPotMode(slea, c, c.getPlayer());
                break;
            case POTION_POT_INCR:
                PotionPotHandler.PotionPotIncr(slea, c, c.getPlayer());
                break;
            case APPLY_HYUNCUBE:
                InventoryHandler.applyHyunCube(slea, c, c.getPlayer());
                break;
            case CP_ZeroTag:
                PlayerHandler.zeroTag(slea, c, c.getPlayer());
                break;
            case CP_ZeroShareCashEquipPart:
                PlayerHandler.changeZeroLook(slea, c, c.getPlayer(), false);
                break;
            case CP_UserFinalAttackRequest:
                PlayerHandler.ExtraAttack(slea, c, c.getPlayer());
                break;
            case CREATE_AFFECTED_AREA_REQUEST:
                PlayerHandler.spawnSpecial(slea, c, c.getPlayer());
                break;
            case CP_UserB2BodyRequest:
                PlayerHandler.MoveEnergyBall(slea, c);
                break;
            case USER_TRUMP_SKILL_ACTION_REQUEST:
                PlayerHandler.UserTrumpSkillActionRequest(slea, c.getPlayer());
                break;
            case DOT_HEAL_HP_REQUEST:
                PlayerHandler.DotHealHPMPRequest(c.getPlayer());
                break;
            case CP_UserDotHeal:
                PlayerHandler.DotHealHPMPRequest(c.getPlayer());
                break;
            case REWARD_REQUEST:
                c.getPlayer().updateReward();
                break;
            case EFFECT_SWITCH:
                PlayerHandler.effectSwitch(slea, c);
                break;
            case SELECT_CHAIR:
                if (c.getPlayer() != null) {
                    PlayerHandler.selectChair(slea, c, c.getPlayer());
                }
                break;
            case TAP_JOY_RESPONSE:
                UseCashItemHandler.TapJoyResponse(slea, c.getPlayer());
                break;
            case TAP_JOY_DONE:
                UseCashItemHandler.TapJoyDone(slea, c.getPlayer());
                break;
            case TAP_JOY_NEXT_STAGE:
                UseCashItemHandler.TapJoyNextStage(slea, c.getPlayer());
                break;
            case CP_OPEN_UNION_UI_REQUEST:
                PlayerHandler.CP_OPEN_UNION_UI_REQUEST(c.getPlayer());
                break;
            case UPDATE_BULLET_COUNT:
                PlayerHandler.updateBulletCount(slea, c.getPlayer());
                break;
            case MapleUnionRequest://976
                PlayerHandler.handleMapleUnion(slea, c.getPlayer());
                break;
            case EventReviveRequest:
                PlayerHandler.EventReviveRequest(slea, c, c.getPlayer());
                break;
            case RemoteControlDiceNumber:
                PlayerHandler.handleRemoteControlDice(slea, c.getPlayer());
                break;
            case ForceTargetRequest:
                PlayerHandler.GhostArrowHandler(slea, c.getPlayer());
                break;
            case UserArcaneForceRequest:
                InventoryHandler.arcaneForceRequest(slea, c.getPlayer());
                break;
            case UserAuthenticForceRequest:
                InventoryHandler.authenticForceRequest(slea, c.getPlayer());
                break;
            case CP_MobSkillDelayEnd:
                MobSkillDelayEndHandler.handlePacket(slea, c);
                break;
            case Ping_ClientToGamge:
                int i = slea.readInt();
                c.getPlayer().updateTick(i);
                c.getPlayer().getCheatTracker().setLastChannelTick(i);
                c.getPlayer().getCheatTracker().setLastEnterChannel();
                break;
            case VAddSkillAttackRequest:
                PlayerHandler.VAddSkillAttackRequest(slea, c, c.getPlayer());
                break;
            case CP_MobCrcDataResult:
                PlayerHandler.OverloadModeResult(c.getPlayer());
                break;
            case CP_RequestSetHpBaseDamage:
                PlayerHandler.RequestSetHpBaseDamage(c.getPlayer());
                break;
            case CP_UserForceAtomCollision:
                PlayerHandler.UserForceAtomCollision(slea, c, c.getPlayer());
                break;
            case DimensionMirrorMove:
                PlayerHandler.DimensionMirrorMove(slea, c, c.getPlayer());
                break;
            case CP_MobCrcKeyChangedReply:
                PlayerHandler.DevilFrenzyResult(slea, c, c.getPlayer());
                break;
            case CP_UserChangeSoulCollectionRequest:
                PlayerHandler.UserChangeSoulCollectionRequest(slea, c, c.getPlayer());
                break;
            case MONSTER_DEAD_COUNT:
                PlayerHandler.ComboCheckRequest(slea, c, c.getPlayer());
                break;
            case PoisonAreaRemove:
                PlayerHandler.PoisonAreaRemove(slea, c.getPlayer());
                break;
            case JobFreeChangeRequest:
                PlayerHandler.JobFreeChangeRequest(slea, c.getPlayer());
                break;
            case ErosionsrReduce:
                PlayerHandler.ErosionsrReduce(slea, c, c.getPlayer());
                break;
            case SelflessState:
                PlayerHandler.SelflessState(slea, c, c.getPlayer());
                break;
            case CP_ReqMakingSkillEff:
                PlayerHandler.ReqMakingSkillEff(slea, c, c.getPlayer());
                break;
            case UserSetCustomizeEffect:
                PlayerHandler.UserSetCustomizeEffect(slea, c, c.getPlayer());
                break;
            case CP_TowerRankRequest:
                PlayerHandler.TowerRankRequest(slea, c, c.getPlayer());
                break;
            case CP_MobHitByMob:
                MobHandler.MobHitByMob(slea, c, c.getPlayer());
                break;
            case CP_UserCalcDamageStatSetRequest:
                PlayerHandler.UserCalcDamageStatSetRequest(slea, c, c.getPlayer());
                break;
            case MonsterCollectionCompleteRewardRequest:
                MonsterCollectionHandler.CompleteRewardRequest(slea, c, c.getPlayer());
                break;
            case MonsterCollectionExploreRequest:
                MonsterCollectionHandler.ExploreRequest(slea, c, c.getPlayer());
                break;
            case UserCharacterPotentialRequest:
                PlayerHandler.UserCharacterPotentialRequest(slea, c, c.getPlayer());
                break;
            case CP_UserTemporaryStatUpdateRequest:
                PlayerHandler.UserTemporaryStatUpdateRequest(slea, c, c.getPlayer());
                break;
            case CP_MobAttackMob:
                MobHandler.MobAttackMob(slea, c, c.getPlayer());
                break;
            case CP_MobEscortCollision:
                MobHandler.MobEscortCollision(slea, c, c.getPlayer());
                break;
            case CP_MobRequestEscortInfo:
                MobHandler.MobRequestEscortInfo(slea, c, c.getPlayer());
                break;
            case CrystalCharge:
                PlayerHandler.CrystalCharge(slea, c, c.getPlayer());
                break;
            case UserRunScript:
                PlayerHandler.RunScript(slea, c, c.getPlayer());
                break;
            case CHAR_INFO_WARP_MY_HOME:
                PlayerHandler.MyHomeRunScript(slea, c, c.getPlayer());
                break;
            case CHANGE_ANDROID_ANTENNA:
                PlayerHandler.ChangeAndroidAntenna(slea, c, c.getPlayer());
                break;
            case CTX_UNION_PRESET_REQUEST:
                PlayerHandler.CTX_UNION_PRESET_REQUEST(slea, c, c.getPlayer());
                break;
            case ForceAtomNextTarget:
                PlayerHandler.ForceAtomNextTarget(slea, c, c.getPlayer());
                break;
            case Sword_Action:
                AdeleHandler.ForceAtomObjectAction(slea, c, c.getPlayer());
                break;
            case Sword_Remove:
                AdeleHandler.ForceAtomObjectRemove(slea, c, c.getPlayer());
                break;
            case Sword_Move:
                AdeleHandler.ForceAtomObjectMove(slea, c, c.getPlayer());
                break;
            case HoYoungHealRequest:
                PlayerHandler.HoYoungHealRequest(slea, c, c.getPlayer());
                break;
            case CP_UserRenameRequest:
                PlayerHandler.ChangeNameRequest(slea, c, c.getPlayer());
                break;
            case CheckSPWOnRename:
                PlayerHandler.ChangeNamePwCheck(slea, c, c.getPlayer());
                break;
            case CombingRoomActionReq:
                PlayerHandler.CombingRoomActionReq(slea, c, c.getPlayer());
                break;
            case JENO_ENERGY_STORAGE_SYSTEM:
                PlayerHandler.JENO_ENERGY_STORAGE_SYSTEM(slea, c.getPlayer());
                break;

            case CHANGE_CHANNEL_AUTH:
                // AuthPacket.sendChannelAuthPacket(c);
                break;
            case USER_OPEN_V_SKILL_UI:
                // AuthPacket.sendChannelAuthPacket(c);
                break;
            case MIN_5_CLIENT_CHECK:
                // AuthPacket.getMin5Check(slea);
                break;
//            /* 露希妲 */
//
//            //------------------------------------------------------------------------------------------------------
//
//            /* 戴米安 */
//            case DEMIAN_ATTACKED:
//                MobHandler.demianAttacked(slea, c);
//                break;
//
//            case FLYING_SWORD_ATTACK_MODE:
//                // DemianPacket.FlyingSwordNode(c);;
//                break;
//            case STIGMAINCINERATEUSE:
//                MobHandler.useStigmaIncinerate(slea, c);
//                break;
//            //------------------------------------------------------------------------------------------------------
//
//            /* 威爾 */
//
            case BOSS_WILL_USE_MOONGAUGE:
                c.announce(Will.場景變更(c));
                break;
            case WILL_SPIDER_TOUCH:
                PlayerHandler.touchSpider(slea, c, c.getPlayer().getMap());
                break;
//            //----------------------------------------------------------------------------------------------------
//
//            case BOSS_JIN_BLACK_HAND_RECV:
//                if ( c.getPlayer().getMap().getId() == 450010500) {
//                    JinHillah.BlackHandAttack(slea, c);
//                }
//                break;
//            //------------------------------------------------------------------------------------------------------
//
//            /* 玩家操作 */
//
//            /* 頓凱爾 */
//
//            //------------------------------------------------------------------------------------------------------
//
//            /* 黑魔法師 */
//            case BOSS_BLACK_MAGE_ACTION:
//                BlackMagePacket.UseAction(slea, c);
//                break;
//            case UNLOCKTRINITY:
//                PlayerHandler.unlockTrinity(c);
//                break;
//            case BLACKMAGEBALLRECV:
//                PlayerHandler.BlackMageBallRecv(slea, c.getPlayer());
//                break;
//            case BLACK_MAGE_BALL_DOWN:
//                PlayerHandler.blackMageBallDown(slea, c, c.getPlayer());
//                break;
//            //------------------------------------------------------------------------------------------------------
//
//            /* 賽蓮 */
//            case BATTLEGROUND_SELECT_AVATER:
//                Seren.SerenRazerHit(slea, c, c.getPlayer());
//                break;
            case SEREN_USE_SUNLIGHT_SKILL:
                Seren.SerenRazerHit(slea, c, c.getPlayer());
                break;
//            case SEREN_UPDATE_SUNLIGHT_INFO:
//                Seren.SerenUpdateInfo(slea, c, c.getPlayer());
//                break;
//

//
//            //------------------------------------------------------------------------------------------------------
            case ACHIEVEMENT:
                UIPacket.getAchievementUI(slea, c.getPlayer());
                break;
            default:
                if (header.getValue() > -2 && !unkProcessOps.contains(header.getValue())) {
                    unkProcessOps.add(header.getValue());
                }
                c.sendEnableActions();
                break;
        }
    }
    
    public static void useAttack(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_UseAttack.getValue());
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        c.announce(mplew.getPacket());
    }

    public static void changeMapAuth(MapleClient c) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_SECURITY_REQUEST.getValue());
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        mplew.write(0);
        c.announce(mplew.getPacket());
    }
}
