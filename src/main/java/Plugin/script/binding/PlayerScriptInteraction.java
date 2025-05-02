package Plugin.script.binding;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.MapleReward;
import Client.inventory.Item;
import Client.inventory.ItemAttribute;
import Client.inventory.MaplePet;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Config.constants.enums.ScriptType;
import Config.constants.enums.UserChatMessageType;
import Net.server.MapleInventoryManipulator;
import Net.server.MapleItemInformationProvider;
import Net.server.RaffleItem;
import Net.server.RafflePool;
import Net.server.commands.SuperGMCommand;
import Net.server.events.DimensionMirrorEvent;
import Net.server.life.*;
import Net.server.maps.MapleMap;
import Net.server.maps.MapleReactor;
import Net.server.maps.MapleReactorFactory;
import Net.server.quest.MapleQuest;
import Opcode.Headler.OutHeader;
import Opcode.Opcode.EffectOpcode;
import Packet.*;
import Plugin.script.EventManager;
import Plugin.script.ScriptManager;
import Server.BossEventHandler.Caning;
import Server.channel.ChannelServer;
import Server.world.WorldBroadcastService;
import SwordieX.client.party.Party;
import SwordieX.client.party.PartyMember;
import SwordieX.client.party.PartyResult;
import connection.packet.WvsContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import tools.DateUtil;
import tools.Pair;
import tools.data.MaplePacketLittleEndianWriter;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class PlayerScriptInteraction extends ScriptBase {

    private static MapleNPC npc;
    @Getter
    private final MapleCharacter player;
    @Getter
    private final MapleClient client;
    private int quest;
    private boolean start;
    private JFrame frame;
    private JTextField handlerField;
    private JTextArea packetContentArea;

    public PlayerScriptInteraction(MapleCharacter player) {
        this.player = player;
        this.client = player.getClient();
    }

    public static int getNpc() {
        return npc.getId();
    }

    /**
     * 根据事件名获取当前频道事件实例
     * 不需要当前角色处于实例中也可以获取
     *
     */
    public ScriptEvent getEvent() {
        return getPlayer().getEventInstance();
    }

    public ScriptEvent makeEvent(String script, Object attachment) {
        ScriptEvent event = new EventManager(script, getPlayer().getClient().getChannel(), null).runScript(getPlayer(), script, true, attachment);
        return event;
    }

    /**
     * 根据事件名获取当前频道事件实例
     * 不需要当前角色处于实例中也可以获取
     *
     * @param event
     * @return
     */
    public ScriptEvent getEvent(String event) {
        for (MapleMap map : ChannelServer.getInstance(getPlayer().getClient().getChannel()).getMapFactory().getAllMaps()){
            if (map.getEvent() != null && map.getEvent().getName().equals(event)) {
                return map.getEvent();
            }
        }
        return null;
    }

    /**
     * 获取当前频道所有角色
     *
     * @return
     */
    public List<MapleCharacter> getChannelPlayers() {
        int channel = getClient().getChannelServer().getChannel();
        return ChannelServer.getInstance(channel).getPlayerStorage().getAllCharacters();
    }



    public int[] resetRememberedMap(String variable) {

        String rMap = getPlayer().getQuestInfo(100642, variable + "_rMap");
        String rPoratl = getPlayer().getQuestInfo(100642, variable + "_rPoratl");

        if (rMap == null || rMap.equals("")) {
            rMap = "100000000";
        } else {
            getPlayer().updateOneQuestInfo(100642, variable + "_rMap", "");
        }
        if (rPoratl == null || rPoratl.equals("")) {
            rPoratl = "0";
        } else {
            getPlayer().updateOneQuestInfo(100642, variable + "_rPoratl", "");
        }

        return new int[]{Integer.parseInt(rMap), Integer.parseInt(rPoratl)};

    }

    public void rememberMap(String variable) {
        getPlayer().updateOneQuestInfo(100642, variable + "_rMap", Integer.toString(getPlayer().getMapId()));
        getPlayer().updateOneQuestInfo(100642, variable + "_rPoratl", null);
    }

    public String getRememberedMap(String variable) {
        return getPlayer().getQuestInfo(100642, variable + "_rMap");
    }

    /**
     * 创建道具实例。
     *
     * @param itemId
     * @return
     */
    public Item makeItemWithId(int itemId) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (!ii.itemExists(itemId)) {
            getPlayer().dropMessage(5, itemId + " 這個道具不存在.");
            return null;
        } else {
            Item item;
            int flag = (short) ItemAttribute.Seal.getValue();
            final MaplePet pet;
            if (ItemConstants.類型.寵物(itemId)) {
                pet = MaplePet.createPet(itemId);
            } else {
                pet = null;
            }
            item = new Item(itemId, (byte) 0, (byte) 1, 0);
            item.setPet(pet);
            return item;
        }
    }


    /**
     * 设置全局共享变量
     *
     * @param key
     * @param value
     */
    public void setVariable(String key, Object value) {
        if (value == null || "".equals(value)) {
            getPlayer().getVariable().remove(key);
        } else {
            getPlayer().getVariable().put(key, value);
        }
    }

    /**
     * 获取全局共享变量
     *
     * @param key
     * @return
     */
    public Object getVariable(String key) {
        return getPlayer().getVariable().get(key);
    }


    public String httpGet(String url) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "application/json")
                    .GET()
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // 获取响应码
            int responseCode = response.statusCode();
            if (responseCode == 200) {
                return response.body();
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("[httpPost]error:", e);
            return null;
        }
    }


    public String httpPost(String url, String body) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(5))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // 获取响应码
            int responseCode = response.statusCode();
            if (responseCode == 200) {
                return response.body();
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("[httpPost]error:", e);
            return null;
        }
    }

    /**
     * 啟動BOSSUI
     *
     * @param BossType
     * @param difficulty 0~6分別為簡單 ~ 故事難度
     */
    public void startBossUI(int BossType, int[] difficulty) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.CTX_PORTAL_BOSS_EVENT_UI.getValue());
        mplew.writeInt(BossType);
        mplew.writeBool(false); // 關閉入口
        mplew.writeInt(difficulty.length);
        for (int i = 0; i < difficulty.length; i++) {
            mplew.write(difficulty[i]);
            mplew.writeHexString("00 00 00 05 00 00 00 00 00 00 00 01 05 00 00 00");
        }
        mplew.writeInt(3);
        mplew.writeInt(0);
        mplew.writeInt(8);
        mplew.writeInt(0);
        mplew.writeInt(80003600);
        mplew.writeInt(1);
        mplew.writeInt(80003601);
        mplew.writeInt(2);
        mplew.writeInt(80003602);
        mplew.writeInt(3);
        mplew.writeInt(80003603);
        mplew.writeInt(4);
        mplew.writeInt(80003604);
        mplew.writeInt(5);
        mplew.writeInt(80003605);
        mplew.writeInt(6);
        mplew.writeInt(80003606);
        mplew.writeInt(7);
        mplew.writeInt(80003607);
        mplew.writeInt(1);
        mplew.writeInt(11);
        mplew.writeInt(0);
        mplew.writeInt(-2024172);
        mplew.writeInt(1);
        mplew.writeInt(-2024173);
        mplew.writeInt(2);
        mplew.writeInt(-2024174);
        mplew.writeInt(3);
        mplew.writeInt(-2024175);
        mplew.writeInt(4);
        mplew.writeInt(-2024176);
        mplew.writeInt(5);
        mplew.writeInt(-2024177);
        mplew.writeInt(6);
        mplew.writeInt(-2024178);
        mplew.writeInt(7);
        mplew.writeInt(-2024179);
        mplew.writeInt(8);
        mplew.writeInt(-2024180);
        mplew.writeInt(9);
        mplew.writeInt(-2024181);
        mplew.writeInt(10);
        mplew.writeInt(-2024182);
        mplew.writeInt(2);
        mplew.writeInt(14);
        mplew.writeInt(0);
        mplew.writeInt(-2024193);
        mplew.writeInt(1);
        mplew.writeInt(-2024187);
        mplew.writeInt(2);
        mplew.writeInt(-2024186);
        mplew.writeInt(3);
        mplew.writeInt(-2024194);
        mplew.writeInt(4);
        mplew.writeInt(-2024195);
        mplew.writeInt(5);
        mplew.writeInt(-2024188);
        mplew.writeInt(6);
        mplew.writeInt(-2024183);
        mplew.writeInt(7);
        mplew.writeInt(-2024184);
        mplew.writeInt(8);
        mplew.writeInt(-2024185);
        mplew.writeInt(9);
        mplew.writeInt(-2024189);
        mplew.writeInt(10);
        mplew.writeInt(-2024190);
        mplew.writeInt(11);
        mplew.writeInt(-2024191);
        mplew.writeInt(12);
        mplew.writeInt(-2024192);
        mplew.writeInt(13);
        mplew.writeInt(-2024237);
        mplew.writeInt(3);
        mplew.writeInt(0);
        mplew.writeInt(8);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeInt(1);
        mplew.writeInt(2);
        mplew.writeInt(2);
        mplew.writeInt(3);
        mplew.writeInt(3);
        mplew.writeInt(4);
        mplew.writeInt(4);
        mplew.writeInt(5);
        mplew.writeInt(5);
        mplew.writeInt(6);
        mplew.writeInt(6);
        mplew.writeInt(7);
        mplew.writeInt(7);
        mplew.writeInt(1);
        mplew.writeInt(11);
        mplew.writeInt(0);
        mplew.writeInt(0);
        mplew.writeInt(1);
        mplew.writeInt(1);
        mplew.writeInt(2);
        mplew.writeInt(2);
        mplew.writeInt(3);
        mplew.writeInt(3);
        mplew.writeInt(4);
        mplew.writeInt(4);
        mplew.writeInt(5);
        mplew.writeInt(5);
        mplew.writeInt(6);
        mplew.writeInt(6);
        mplew.writeInt(7);
        mplew.writeInt(7);
        mplew.writeInt(8);
        mplew.writeInt(8);
        mplew.writeInt(9);
        mplew.writeInt(9);
        mplew.writeInt(10);
        mplew.writeInt(10);
        mplew.writeInt(2);
        mplew.writeInt(14);
        mplew.writeInt(0);
        mplew.writeInt(13);
        mplew.writeInt(1);
        mplew.writeInt(0);
        mplew.writeInt(2);
        mplew.writeInt(1);
        mplew.writeInt(3);
        mplew.writeInt(2);
        mplew.writeInt(4);
        mplew.writeInt(3);
        mplew.writeInt(5);
        mplew.writeInt(4);
        mplew.writeInt(6);
        mplew.writeInt(5);
        mplew.writeInt(7);
        mplew.writeInt(6);
        mplew.writeInt(8);
        mplew.writeInt(7);
        mplew.writeInt(9);
        mplew.writeInt(8);
        mplew.writeInt(10);
        mplew.writeInt(9);
        mplew.writeInt(11);
        mplew.writeInt(10);
        mplew.writeInt(12);
        mplew.writeInt(11);
        mplew.writeInt(13);
        mplew.writeInt(12);
        mplew.writeInt(0);
        mplew.writeInt(6);
        mplew.writeInt(-2024195);
        mplew.writeInt(1);
        mplew.writeInt(-2024194);
        mplew.writeInt(-2024194);
        mplew.writeInt(1);
        mplew.writeInt(-2024195);
        mplew.writeInt(-2024187);
        mplew.writeInt(1);
        mplew.writeInt(-2024186);
        mplew.writeInt(-2024186);
        mplew.writeInt(1);
        mplew.writeInt(-2024187);
        mplew.writeInt(80003606);
        mplew.writeInt(1);
        mplew.writeInt(80003607);
        mplew.writeInt(80003607);
        mplew.writeInt(1);
        mplew.writeInt(80003606);
        mplew.writeInt(1);
        mplew.writeInt(-2024237);
        mplew.writeInt(200);
        mplew.writeInt(503417);
        mplew.writeMapleAsciiString("qState");
        mplew.write(1);
        mplew.write(0);
        mplew.write(49);
        getPlayer().send(mplew.getPacket());
        if (getPlayer().getParty() == null) {
            Party party = Party.createNewParty(false, false, getPlayer().getName() + "的隊伍", getPlayer().getClient().getWorld());
            PartyMember pm = new PartyMember(getPlayer());
            party.setPartyLeaderID(pm.getCharID());
            party.getPartyMembers()[0] = pm;
            getPlayer().setParty(party);
            getPlayer().write(WvsContext.partyResult(PartyResult.createNewParty(party)));
        }
    }

    public int pecketToolUI() {
        frame = new JFrame("[Tools] PhantomTMS_Packet Tools by Hertz.");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 200);
        frame.setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel();

        inputPanel.setLayout(new GridLayout(5, 5));

        JLabel handlerLabel = new JLabel("請輸入封包包頭:");
        handlerField = new JTextField();
        JLabel packetContentLabel = new JLabel("請輸入封包內容:");
        packetContentArea = new JTextArea();

        inputPanel.add(handlerLabel);
        inputPanel.add(handlerField);
        inputPanel.add(packetContentLabel);
        inputPanel.add(packetContentArea);

        JButton sendButton = new JButton("發送");
        sendButton.addActionListener(e -> sendPacket());

        frame.add(inputPanel, BorderLayout.CENTER);
        frame.add(sendButton, BorderLayout.SOUTH);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        return 1;
    }

    /*
     * 解散公會
     */
    public void guildDisband(int guildId) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(OutHeader.LP_GuildResult.getValue());
        mplew.writeInt(40);
        mplew.writeInt(guildId);
        getPlayer().send(mplew.getPacket());
    }

    private void sendPacket() {
        String handlerText = handlerField.getText();
        String packetContentText = packetContentArea.getText();
        try {
            int sendOpcode = Integer.parseInt(handlerText);
            String packetContent = packetContentText.replaceAll("\\s+", "");
            MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
            mplew.writeShort(sendOpcode);
            if (packetContent == null) {
                return;
            } else {
                mplew.writeHexString(packetContent);
            }
            client.getPlayer().dropMessage(15, "[發送封包包頭]: " + sendOpcode + " / 封包內容:[" + Arrays.toString(mplew.getPacket()) + "]");
            client.announce(mplew.getPacket());
            JOptionPane.showMessageDialog(frame, "資料包發送成功.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "處理程序格式無效.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "資料包內容格式無效.");
        }
    }

    public void setDelay(int delay) {
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setSpirtValue(int value) {
        for (MapleCharacter player : getPlayer().getMap().getAllChracater()) {
            Caning.setSpirtValue(value, player.getClient());
        }
    }

    public void setSelMapLoad() {
        for (MapleCharacter player : getPlayer().getMap().getAllChracater()) {
            Caning.setSelMapLoad(player.getClient());
        }
    }

    public void setSelMapLoadNext() {
        for (MapleCharacter player : getPlayer().getMap().getAllChracater()) {
            Caning.setSelMapLoadNext(player.getClient());
        }
    }

    public void setSelMapLoadParty() {
        for (MapleCharacter player : getPlayer().getMap().getAllChracater()) {
            Caning.setSelMapLoadParty(player.getClient());
        }
    }

    public void showHint(String message, int heigth, int width) {
        getClient().announce(MaplePacketCreator.sendHint(message, heigth, width, null));
    }

    public MapleCharacter sendAccReward(int accountId, int itemId, long amount, String desc) {
        return sendAccRewardPeriod(accountId, 0, itemId, amount, desc);
    }

    public MapleCharacter sendAccRewardPeriod(int accountId, int day, int itemId, long amount, String desc) {
        return sendReward(accountId, 0, DateUtil.getNextDayTime(0), day <= 0 ? 0 : (DateUtil.getNextDayTime(day) - 60000), itemId, amount, desc);
    }

    public MapleMonsterInformationProvider getMonsterInfo() {
        return MapleMonsterInformationProvider.getInstance();
    }

    public MapleReactor getReactor(int id) {
        return new MapleReactor(MapleReactorFactory.getReactor(id), id);
    }

    public final MapleMonster getMonster(final int mobId) {
        return MapleLifeFactory.getMonster(mobId);
    }

    public MapleMonster getEliteMonster(int id) {
        return MapleLifeFactory.getEliteMonster(id);
    }

    public MapleMonster getEliteMonster(int mobId, MapleMonsterStats stats) {
        return MapleLifeFactory.getEliteMonster(mobId, stats);
    }


    public MapleMonster getEliteMonster(int mobId, MapleMonsterStats stats, int eliteGrade) {
        return MapleLifeFactory.getEliteMonster(mobId, stats, eliteGrade);
    }

    public MapleMonster getEliteMonster(int mobId, MapleMonsterStats stats, int eliteGrade, int eliteType) {
        return MapleLifeFactory.getEliteMonster(mobId, stats, eliteGrade, eliteType);
    }

    public long getCurrentTime() {
        return System.currentTimeMillis();
    }

    public List<Pair<Integer, Integer>> getAllHotTimeItems() {
        return SuperGMCommand.HotTime.HotTimeItems;
    }

    public List<RaffleItem> getRaffleMainReward(int type) {
        return RafflePool.getMainReward(type);
    }

    public MapleItemInformationProvider getItemInfo() {

        return MapleItemInformationProvider.getInstance();
    }

    public MapleCharacter sendReward(int accountId, int characterId, long start, long end, int itemId, long amount, String desc) {
        int type;
        if (itemId < 1000000) {
            if (itemId > 2 && itemId < 6) {
                type = itemId;
            } else {
                return null;
            }
        } else if (getItemInfo().isCash(itemId)) {
            type = MapleReward.現金道具;
        } else if (getItemInfo().itemExists(itemId)) {
            type = MapleReward.道具;
        } else {
            return null;
        }
        MapleCharacter.addReward(accountId, characterId, start, end, type, amount, itemId, desc);
        for (ChannelServer cserv : ChannelServer.getAllInstances()) {
            for (MapleCharacter mch : cserv.getPlayerStorage().getAllCharacters()) {
                if (mch.getAccountID() == accountId || mch.getId() == characterId) {
                    mch.updateReward();
                    return mch;
                }
            }
        }
        return null;
    }

    public void runScript(String scriptName) throws Exception {
        runScript(0, "expands/" + scriptName);
    }

    /**
     * 不要更改這個方法，不然會導致runScript執行完後還會繼續執行runScript後面的代碼導致作用域問題和回調地獄
     */
    public void runScript(int npcId, String scriptName) throws Exception {
        var scriptString = ScriptManager.getScriptString(scriptName);
        if (!scriptString.isEmpty()) {
            ScriptEngine engine = (ScriptEngine) getPlayer().getScriptManager().getInvocableByType(getPlayer().getScriptManager().getLastActiveScriptType());
            getPlayer().dropSpouseMessage(UserChatMessageType.青, "[runScript] " + scriptName + ".js");
            Bindings bindings = engine.createBindings();
            bindings.put("party", getPlayer().getParty() == null ? null : new ScriptParty(getPlayer().getParty()));
            bindings.put("player", new ScriptPlayer(getPlayer()));
            bindings.put("map", new ScriptField(getPlayer().getMap()));
            bindings.put("sh", new ScriptHelper());
            bindings.put("npc", new ScriptNpc(getPlayer().getClient(), npcId, scriptName, ScriptType.Npc, null));
            CompiledScript cs = ((Compilable) engine).compile(scriptString);
            cs.eval(bindings);
        }
        throw new NullPointerException(ScriptManager.INTENDED_NPE_MSG);
    }

    /* creat hertz */
    public void showUnityPortal() {
        List<DimensionMirrorEvent> list = Arrays.stream(DimensionMirrorEvent.values()).filter(it -> it.getMapID() > 0).collect(Collectors.toList());
        getClient().announce(UIPacket.showDimensionMirror(list));
    }

    public void SayEldasMessage(String Notice) {
        getPlayer().send(EldasPacket.Eldas_200(Notice));
        getPlayer().removeItem(2636883, 1);
    }

    public void eventSay(String Notice) {
        getPlayer().send(MessengerPacket.npcEffectChat_BlackLock(Notice));
    }


    public void addEdraCount(int Count) {
        getPlayer().addEdraSoul(Count);
    }

    public void CreatGuildName() {
        getPlayer().send(GuildPacket.genericGuildMessage((byte) 0x00));
    }

    public void playMusicBox(String music) {
        MaplePacketLittleEndianWriter ctx = new MaplePacketLittleEndianWriter();
        ctx.writeShort(-2);
        ctx.write(9);
        ctx.writeMapleAsciiString(music);
        ctx.writeInt(0);
        ctx.writeInt(0);
        ctx.writeInt(0);
        getPlayer().send(ctx.getPacket());
    }

    public void playMusicBox2(String music) {
        MaplePacketLittleEndianWriter ctx = new MaplePacketLittleEndianWriter();
        ctx.writeShort(-2);
        ctx.write(9);
        ctx.writeMapleAsciiString(music);
        ctx.writeInt(0);
        ctx.writeInt(0);
        ctx.writeInt(0);
        getPlayer().send(ctx.getPacket());
    }

    public void playMusicBox3(String music) {
        MaplePacketLittleEndianWriter ctx = new MaplePacketLittleEndianWriter();
        ctx.writeShort(-2);
        ctx.write(9);
        ctx.writeMapleAsciiString(music);
        ctx.writeInt(0);
        ctx.writeInt(0);
        ctx.writeInt(0);
        getPlayer().send(ctx.getPacket());
    }

    public void playMusicBox4(String music) {
        MaplePacketLittleEndianWriter ctx = new MaplePacketLittleEndianWriter();
        ctx.writeShort(-2);
        ctx.write(9);
        ctx.writeMapleAsciiString(music);
        ctx.writeInt(0);
        ctx.writeInt(0);
        ctx.writeInt(0);
        getPlayer().send(ctx.getPacket());
    }

    public void playMusicBox5(String music) {
        MaplePacketLittleEndianWriter ctx = new MaplePacketLittleEndianWriter();
        ctx.writeShort(-2);
        ctx.write(9);
        ctx.writeMapleAsciiString(music);
        ctx.writeInt(0);
        ctx.writeInt(0);
        ctx.writeInt(0);
        getPlayer().send(ctx.getPacket());
    }

    public void WeatherMessage(String fieldMessage, int type, int ms) {
        getPlayer().getMap().showWeatherEffectNotice(fieldMessage, type, ms);
    }

    /* new */
    public boolean isQuestStarted(int questId) {
        return getPlayer().getQuestStatus(questId) == 1;
    }

    public boolean isQuestFinished(int questId) {
        return getPlayer().getQuestStatus(questId) == 2;
    }

    /* new */
    public boolean isQuestCompleted(int questId) {
        return getPlayer().getQuestStatus(questId) == 2;
    }


    public int getQuest() {
        return quest;
    }

    public boolean isStart() {
        return start;
    }

    public void forceStartQuest() {
        forceStartQuest(false);
    }

    public void forceStartQuest(boolean isWorldShare) {
        forceStartQuest(null, isWorldShare);
    }


    public void forceStartQuest(String customData) {
        forceStartQuest(customData, false);
    }

    public void forceStartQuest(String customData, boolean isWorldShare) {
        MapleQuest.getInstance(quest).forceStart(getPlayer(), getNpc(), customData, isWorldShare);
    }

    public void forceCompleteQuest() {
        forceCompleteQuest(false);
    }

    public void forceCompleteQuest(int questId) {
        MapleQuest.getInstance(questId).forceComplete(getPlayer(), 0, false);
    }

    public void forceCompleteQuest(boolean isWorldShare) {
        MapleQuest.getInstance(quest).forceComplete(getPlayer(), 0, isWorldShare);
    }

    public void resetQuest() {
        MapleQuest.getInstance(quest).reset(getPlayer());
    }

    public String getQuestCustomData() {
        return getPlayer().getQuestNAdd(MapleQuest.getInstance(quest)).getCustomData();
    }

    public void setQuestCustomData(String customData) {
        getPlayer().getQuestNAdd(MapleQuest.getInstance(quest)).setCustomData(customData);
    }

    public void showCompleteQuestEffect() {
        getPlayer().getClient().announce(EffectPacket.showSpecialEffect(EffectOpcode.UserEffect_QuestComplete)); // 任務完成
        getPlayer().getMap().broadcastMessage(getPlayer(), EffectPacket.showForeignEffect(getPlayer().getId(), EffectOpcode.UserEffect_QuestComplete), false);
    }

    public final void spawnNpcForPlayer(int npcId, int x, int y) {
        getPlayer().getMap().spawnNpcForPlayer(getClient(), npcId, new Point(x, y));
    }

    /*
     * 隨機抽獎
     * 參數 道具的ID
     * 參數 道具的數量
     */
    public int gainGachaponItem(int id, int quantity) {
        return gainGachaponItem(id, quantity, getPlayer().getMap().getStreetName() + " - " + getPlayer().getMap().getMapName());
    }

    /*
     * 隨機抽獎
     * 參數 道具的ID
     * 參數 道具的數量
     * 參數 獲得裝備的日誌
     */
    public int gainGachaponItem(int id, int quantity, String msg) {
        byte rareness = GameConstants.gachaponRareItem(id);
        return gainGachaponItem(id, quantity, msg, rareness == 1 || rareness == 2 || rareness == 3);
    }

    public int gainGachaponItem(int id, int quantity, String msg, boolean smega) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        try {
            if (!ii.itemExists(id)) {
                return -1;
            }
            Item item = MapleInventoryManipulator.addbyId_Gachapon(getClient(), id, quantity, "從 " + msg + " 中獲得時間: " + DateUtil.getNowTime());
            if (item == null) {
                return -1;
            }
            if (smega) {
                WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.gachaponMsg("恭喜" + getPlayer().getName() + "從" + msg + "獲得{" + ii.getName(item.getItemId()) + "}", item));
            }
            return item.getItemId();
        } catch (Exception e) {
            log.error("gainGachaponItem 錯誤", e);
        }
        return -1;
    }

    /*
     * NPC給玩家道具帶公告
     * 參數 道具的ID
     * 參數 道具的數量
     * 參數 獲得裝備的日誌
     * 參數 公告喇叭的類型[1-3]
     */
    public int gainGachaponItem(int id, int quantity, String msg, int rareness) {
        return gainGachaponItem(id, quantity, msg, rareness, false, 0);
    }

    /*
     * NPC給玩家道具帶公告
     * 參數 道具的ID
     * 參數 道具的數量
     * 參數 獲得裝備的日誌
     * 參數 公告喇叭的類型[1-3]
     * 參數 道具的使用時間
     */
    public int gainGachaponItem(int id, int quantity, String msg, int rareness, long period) {
        return gainGachaponItem(id, quantity, msg, rareness, false, period);
    }

    /*
     * NPC給玩家道具帶公告
     * 參數 道具的ID
     * 參數 道具的數量
     * 參數 獲得裝備的日誌
     * 參數 公告喇叭的類型[1-3]
     * 參數 是否NPC購買
     * 參數 道具的使用時間
     */
    public int gainGachaponItem(int id, int quantity, String msg, int rareness, boolean buy) {
        return gainGachaponItem(id, quantity, msg, rareness, buy, 0);
    }

    /*
     * NPC給玩家道具帶公告
     * 參數 道具的ID
     * 參數 道具的數量
     * 參數 獲得裝備的日誌
     * 參數 公告喇叭的類型[1-3]
     * 參數 是否NPC購買
     * 參數 道具的使用時間
     */
    public int gainGachaponItem(int id, int quantity, String msg, int rareness, boolean buy, long period) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        try {
            if (!ii.itemExists(id)) {
                return -1;
            }
            Item item = MapleInventoryManipulator.addbyId_Gachapon(getClient(), id, quantity, "從 " + msg + " 中" + (buy ? "購買" : "獲得") + "時間: " + DateUtil.getNowTime(), period);
            if (item == null) {
                return -1;
            }
            if (rareness == 1 || rareness == 2 || rareness == 3) {
                WorldBroadcastService.getInstance().broadcastMessage(MaplePacketCreator.getGachaponMega(getPlayer().getName(), " : 從" + msg + "中" + (buy ? "購買" : "獲得") + "{" + ii.getName(item.getItemId()) + "}！大家一起恭喜他（她）吧！！！！", item, (byte) rareness, getClient().getChannel()));
            }
            return item.getItemId();
        } catch (Exception e) {
            log.error("gainGachaponItem 錯誤", e);
        }
        return -1;
    }

}
