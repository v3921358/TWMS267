package Client;

import Config.constants.JobConstants;
import Net.server.MapleUnionData;
import tools.Triple;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * create by Ethan on 20170811
 *
 * @author Ethan
 */
public class MapleUnion {
    private final Map<Integer, MapleUnionEntry> allUnions = new HashMap<>();
    private final Map<Integer, MapleUnionEntry> fightingUnions = new HashMap<>();
    private final Map<Integer, Board> boards = new HashMap<>();
    private final Map<Integer, Integer> skills = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final int[] addStats = new int[16];
    private int state;

    public MapleUnion() {
        reload();
    }

    private void reload() {
        lock.writeLock().lock();
        try {
            for (int i = 0; i < addStats.length; ++i) {
                addStats[i] = 0;
            }
            skills.clear();
            boards.clear();
//            MapleUnionData.getInstance().getBoardInfo().forEach((key, value) -> boards.put(key, new Board(key, value.getXPos(), value.getYPos(), value.getGroupIndex())));
            for (Map.Entry<Integer, MapleUnionBoardEntry> entry : MapleUnionData.getInstance().getBoardInfo().entrySet()) {
                boards.put(entry.getKey(), new Board(entry.getKey(), entry.getValue().getXPos(), entry.getValue().getYPos(), entry.getValue().getGroupIndex()));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void update() {
        lock.writeLock().lock();
        try {
            reload();
            for (Map.Entry<Integer, MapleUnionEntry> entry : fightingUnions.entrySet()) {
                MapleUnionData data = MapleUnionData.getInstance();
                MapleUnionEntry union = entry.getValue();
                MapleUnionBoardEntry boardEntry = MapleUnionData.getInstance().getBoardInfo().get(union.getBoardIndex());
                if (boardEntry != null) {
                    int xPos = boardEntry.getXPos();
                    int yPos = boardEntry.getYPos();
                    int rotate = union.getRotate();
                    int job = union.getJob();
                    int level = union.getLevel();
                    int rank = data.getCharacterRank(job, level);
                    if (rank < 0) {
                        continue;
                    }
                    Map<Integer, Point> sizeInfo = data.getSizeInfo(JobConstants.getJobBranch(union.getJob()), rank);
                    if (sizeInfo == null) {
                        continue;
                    }
                    Triple<Integer, Integer, Integer> cardInfo = data.getCardInfo(job, level);
                    if (cardInfo == null) {
                        continue;
                    }
                    int skillId = cardInfo.mid;
//                    if (skills.computeIfAbsent(skillId, key -> skills.put(key, rank)) < rank) {
//                        skills.put(skillId, rank);
//                    }
                    if (skills.containsKey(skillId)) {
                        if (skills.get(skillId) < rank) {
                            skills.put(skillId, rank);
                        }
                    } else {
                        skills.put(skillId, rank);
                    }
                    for (Map.Entry<Integer, Point> entry2 : sizeInfo.entrySet()) {
                        int n = rotate % 1000;
                        int count = rotate / 1000;
                        int x = entry2.getValue().x;
                        int y = entry2.getValue().y;
                        switch (count) {
                            case 1: {
                                x *= -1;
                                break;
                            }
                            case 2: {
                                y *= -1;
                                break;
                            }
                            case 3: {
                                x *= -1;
                                y *= -1;
                                break;
                            }
                        }
                        Board board = getBoardByPos(xPos + (int) Math.round(x * Math.cos(Math.toRadians(n)) - y * Math.sin(Math.toRadians(n))), yPos + (int) Math.round(x * Math.sin(Math.toRadians(n)) + y * Math.cos(Math.toRadians(n))));
                        if (board != null) {
                            board.setActive(true);
                        }
                    }
                }
            }
            for (Board board : boards.values()) {
                if (board.isActive()) {
                    ++addStats[board.getGroupIndex()];
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int getTotalLevel() {
        int level = 0;
        for (MapleUnionEntry union : allUnions.values()) {
            level += union.getLevel();
        }
        return level;
    }

    public int getLevel() {
        int level = 0;
        List<Integer> allLv = allUnions.values().stream().map(MapleUnionEntry::getLevel).collect(Collectors.toCollection(LinkedList::new));
        Collections.sort(allLv, Collections.reverseOrder());
        int i = 0;
        for (int lv : allLv) {
            if (++i > 40) {
                break;
            }
            level += lv;
        }
        return level;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    private Board getBoardByPos(int x, int y) {
        for (Board board : this.boards.values()) {
            if (board.getXPos() == x && board.getYPos() == y) {
                return board;
            }
        }
        return null;
    }

    public Map<Integer, Integer> getSkills() {
        this.lock.readLock().lock();
        try {
            return this.skills;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public Map<Integer, MapleUnionEntry> getFightingUnions() {
        this.lock.readLock().lock();
        try {
            return this.fightingUnions;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public Map<Integer, MapleUnionEntry> getAllUnions() {
        this.lock.readLock().lock();
        try {
            return this.allUnions;
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public int[] getAddStats() {
        return addStats;
    }

    public class Board {
        private final int index;
        private final int xPos;
        private final int yPos;
        private boolean active = false;
        private final int groupIndex;

        public Board(int index, int xPos, int yPos, int groupIndex) {
            this.index = index;
            this.xPos = xPos;
            this.yPos = yPos;
            this.groupIndex = groupIndex;
        }

        public int getGroupIndex() {
            return groupIndex;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isActive() {
            return active;
        }

        public int getYPos() {
            return yPos;
        }

        public int getXPos() {
            return xPos;
        }
    }
}
