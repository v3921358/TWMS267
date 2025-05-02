package Net.server;

import java.util.ArrayList;

public final class VCoreDataEntry {
    private int id = 0;
    private String name;
    private String desc;
    private int type = 0;
    private int maxLevel = 0;
    private final ArrayList<String> jobs = new ArrayList<>();
    private final ArrayList<Integer> connectSkill = new ArrayList<>();
    private boolean nobAbleGemStone = false;
    private boolean notAbleCraft = false;
    private boolean noDisassemble = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public ArrayList<String> getJobs() {
        return jobs;
    }

    public boolean haveJob(String job) {
        return haveJob(job, false);
    }

    public boolean haveJob(String job, boolean onlyJob) {
        return jobs.contains(job) || (!onlyJob && jobs.contains("all"));
    }

    public void addJob(String job) {
        jobs.add(job);
    }

    public ArrayList<Integer> getConnectSkill() {
        return connectSkill;
    }

    public boolean isNobAbleGemStone() {
        return nobAbleGemStone;
    }

    public void setNobAbleGemStone(boolean b) {
        nobAbleGemStone = b;
    }

    public boolean isNotAbleCraft() {
        return notAbleCraft;
    }

    public void setNotAbleCraft(boolean b) {
        notAbleCraft = b;
    }

    public boolean isDisassemble() {
        return noDisassemble;
    }

    public void setDisassemble(boolean b) {
        noDisassemble = b;
    }
}
