package Plugin.script;

import Config.constants.enums.NpcMessageType;

import java.util.ArrayList;
import java.util.List;

public class ScriptMemory {

    private List<NpcScriptInfo> memory = new ArrayList<>();
    private int position = -1;
    private int sayIndex = 0;

    public boolean hasBack() {
        if (position < 0) return false;
        NpcScriptInfo nsi = memory.get(position);
        if (nsi == null) return false;
        return nsi.isNextPossible();
    }

    public boolean hasNext() {
        if (position + 1 >= memory.size()) return false;
        return memory.get(position + 1) != null;
    }

    public NpcScriptInfo decrementAndGet() {
        if (position <= 0) return null;
        return memory.get(--position);
    }

    public NpcScriptInfo getAndDecrement() {
        return memory.get(position--);
    }

    public NpcScriptInfo get() {
        if (memory.isEmpty() || position < 0 || position >= memory.size()) return null;
        return memory.get(position);
    }

    public NpcScriptInfo getAndIncrement() {
        return memory.get(position++);
    }

    public NpcScriptInfo incrementAndGet() {
        if (position + 1 >= memory.size()) return null;
        return memory.get(++position);
    }

    public void add(NpcScriptInfo nsi) {
        memory.add(nsi);
        if (nsi.getMessageType() == NpcMessageType.Say)
            nsi.setIndex(++sayIndex);
        position++;
    }

    public void clear() {
        position = -1;
        sayIndex = 0;
        memory.clear();
    }
}
