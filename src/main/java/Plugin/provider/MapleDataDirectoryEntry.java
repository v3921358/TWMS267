package Plugin.provider;

import java.util.*;

/**
 * @author Matze
 */
public class MapleDataDirectoryEntry extends MapleDataEntry {

    private final List<MapleDataDirectoryEntry> subdirs = new ArrayList<>();
    private final List<MapleDataFileEntry> files = new ArrayList<>();
    private final Map<String, MapleDataEntry> entries = new HashMap<>();

    public MapleDataDirectoryEntry(String name, MapleDataEntity parent) {
        this(name, 0, 0, 0, parent);
    }

    public MapleDataDirectoryEntry(String name, int size, int checksum, long offset, MapleDataEntity parent) {
        super(name, size, checksum, offset, parent);
    }

    public void addDirectory(MapleDataDirectoryEntry dir) {
        subdirs.add(dir);
        entries.put(dir.getName(), dir);
    }

    public void addFile(MapleDataFileEntry fileEntry) {
        files.add(fileEntry);
        entries.put(fileEntry.getName(), fileEntry);
    }

    public List<MapleDataDirectoryEntry> getSubdirectories() {
        return Collections.unmodifiableList(subdirs);
    }

    public List<MapleDataFileEntry> getFiles() {
        return Collections.unmodifiableList(files);
    }

    public MapleDataEntry getEntry(String name) {
        return entries.get(name);
    }

    public void addAll(MapleDataDirectoryEntry root) {
        for (MapleDataDirectoryEntry dir : root.getSubdirectories()) {
            MapleDataEntry entry = getEntry(dir.getName());
            if (entry != null && entry instanceof MapleDataDirectoryEntry) {
                ((MapleDataDirectoryEntry) entry).addAll(dir);
            } else {
                addDirectory(dir);
            }
        }
        for (MapleDataFileEntry f : root.getFiles()) {
            MapleDataEntry entry = getEntry(f.getName());
            if (!(entry instanceof MapleDataFileEntry)) {
                addFile(f);
            }
        }
    }
}
