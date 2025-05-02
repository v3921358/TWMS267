package Net.server.maps;

import Client.MapleCharacter;
import Client.MapleClient;
import Client.inventory.Equip;
import Client.inventory.Item;
import Client.inventory.MapleInventoryType;
import Config.constants.GameConstants;
import Config.constants.ItemConstants;
import Packet.InventoryPacket;
import tools.Randomizer;

import java.awt.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MapleMapItem extends MapleMapObject {

    private final int ownerID;
    private final boolean playerDrop;
    private final ReentrantLock lock = new ReentrantLock();
    protected Item item;
    private MapleMapObject dropper;
    protected int meso = 0;
    protected int pointType = -1;
    protected int questid = -1;
    private byte ownType;
    private boolean pickedUp = false;
    private long nextExpiry = 0, nextFFA = 0;
    private int skill;
    private byte enterType = 1;
    private int delay;
    private int animation = 1;
    private int pickUpID;
    private int onlySelfID = -1;
    private int nDropMotionType = 0;
    private int nDropSpeed = 0;
    private int nRand = 0;
    private int sourceOID = 0;
    private boolean bCollisionPickUp = false;

    public MapleMapItem(Item item, Point position, MapleMapObject dropper, MapleCharacter owner, byte ownType, boolean playerDrop) {
        setPosition(position);
        this.item = item;
        this.dropper = dropper;
        this.ownerID = owner.getId();
        this.ownType = ownType;
        this.playerDrop = playerDrop;
    }

    public MapleMapItem(Item item, Point position, MapleMapObject dropper, MapleCharacter owner, byte ownType, boolean playerDrop, int questid) {
        setPosition(position);
        this.item = item;
        this.dropper = dropper;
        this.ownerID = owner.getId();
        this.ownType = ownType;
        this.playerDrop = playerDrop;
        this.questid = questid;
    }

    public MapleMapItem(int meso, Point position, MapleMapObject dropper, MapleCharacter owner, byte ownType, boolean playerDrop) {
        setPosition(position);
        this.item = null;
        this.dropper = dropper;
        this.ownerID = owner.getId();
        this.meso = meso;
        this.ownType = ownType;
        this.playerDrop = playerDrop;
    }

    public MapleMapItem(int pointType, Item item, Point position, MapleMapObject dropper, MapleCharacter owner, byte ownType, boolean playerDrop) {
        setPosition(position);
        this.item = item;
        this.dropper = dropper;
        this.ownerID = owner.getId();
        this.pointType = pointType;
        this.ownType = ownType;
        this.playerDrop = playerDrop;
    }

    public MapleMapItem(Item item, Point position) {
        setPosition(position);
        this.item = item;
        this.ownerID = 0;
        this.ownType = 2;
        this.playerDrop = false;
        this.nRand = Randomizer.nextInt(150) + 50;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item z) {
        this.item = z;
    }

    public int getQuest() {
        return questid;
    }

    public int getItemId() {
        if (getMeso() > 0) {
            return meso;
        }
        if(item == null){
            return -1;
        }
        return item.getItemId();
    }

    public MapleMapObject getDropper() {
        return dropper;
    }

    public int getOwnerID() {
        return ownerID;
    }

    public int getMeso() {
        return meso;
    }

    public boolean isPlayerDrop() {
        return playerDrop;
    }

    public boolean isPickedUp() {
        return pickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        this.pickedUp = pickedUp;
    }

    public byte getOwnType() {
        return ownType;
    }

    public void setOwnType(byte z) {
        this.ownType = z;
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.ITEM;
    }

    @Override
    public int getRange() {
        return GameConstants.maxViewRange();
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        if ((getMeso() > 0 || (getMeso() <= 0 && item != null)) && (questid <= 0 || (client.getPlayer().getQuestStatus(questid) == 1 && client.getPlayer().needQuestItem(questid, item.getItemId())))) {
            if (getOnlySelfID() >= 0 && (client == null || client.getPlayer() == null || client.getPlayer().getId() != getOnlySelfID())) {
                return;
            }
            client.announce(InventoryPacket.dropItemFromMapObject(this, this.getPosition(), getPosition(), (byte) 2));
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(InventoryPacket.removeItemFromMap(getObjectId(), getAnimation(), getPickUpID()));
    }

    public Lock getLock() {
        return lock;
    }

    public void registerExpire(long time) {
        nextExpiry = System.currentTimeMillis() + time;
    }

    public void registerFFA(long time) {
        nextFFA = System.currentTimeMillis() + time;
    }

    public boolean shouldExpire(long now) {
        return !pickedUp && nextExpiry > 0 && nextExpiry < now;
    }

    public boolean shouldFFA(long now) {
        return !pickedUp && ownType < 2 && nextFFA > 0 && nextFFA < now;
    }

    public boolean hasFFA() {
        return nextFFA > 0;
    }

    public void expire(MapleMap map) {
        pickedUp = true;
        map.broadcastMessage(InventoryPacket.removeItemFromMap(getObjectId(), 0, 0));
        map.removeMapObject(this);
    }

    public int getState() {
        if (this.getMeso() > 0) {
            return 0;
        }
        if (ItemConstants.getInventoryType(item.getItemId(), false) != MapleInventoryType.EQUIP) {
            return 0;
        }
        Equip equip = (Equip) item;
        int state = equip.getState(false);
        int addstate = equip.getState(true);
        if (state <= 0 || state >= 17) {
            state = (state -= 16) < 0 ? 0 : state;
        }
        if (addstate <= 0 || addstate >= 17) {
            addstate = (addstate -= 16) < 0 ? 0 : addstate;
        }
        return state > addstate ? state : addstate;
    }

    public void setSkill(int skill) {
        this.skill = skill;
    }

    public int getSkill() {
        return skill;
    }

    public byte getEnterType() {
        return enterType;
    }

    public void setEnterType(byte enterType) {
        this.enterType = enterType;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getDelay() {
        return delay;
    }

    public int getAnimation() {
        return animation;
    }

    public void setAnimation(int animation) {
        this.animation = animation;
    }

    public void setPickUpID(int pickUpID) {
        this.pickUpID = pickUpID;
    }

    public int getPickUpID() {
        return pickUpID;
    }

    public void setOnlySelfID(int onlySelfID) {
        this.onlySelfID = onlySelfID;
    }

    public int getOnlySelfID() {
        return onlySelfID;
    }

    public int getDropMotionType() {
        return nDropMotionType;
    }

    public void setDropMotionType(int type) {
        nDropMotionType = type;
    }

    public int getDropSpeed() {
        return nDropSpeed;
    }

    public void setDropSpeed(int speed) {
        nDropSpeed = speed;
    }

    public int getRand() {
        return nRand;
    }

    public void setRand(int rand) {
        nRand = rand;
    }

    public int getSourceOID() {
        return sourceOID;
    }

    public void setSourceOID(int oid) {
        sourceOID = oid;
    }

    public boolean isCollisionPickUp() {
        return bCollisionPickUp;
    }

    public void setCollisionPickUp(boolean b) {
        bCollisionPickUp = b;
    }

    public int getPointType() {
        return pointType;
    }

    public void setPointType(int pointType) {
        this.pointType = pointType;
    }
}
