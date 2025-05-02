package Plugin.provider.nx;

import Plugin.provider.*;
import Plugin.provider.nx.util.NxLittleEndianAccessor;
import tools.data.BufferedRandomAccessFile;
import tools.data.RandomAccessByteStream;

import java.io.File;
import java.util.*;

public class NXFileDataProvider implements MapleDataProvider {

    private final File nxfile;
    protected final NXTables tables;
    public final NXHeader Header;
    private MapleDataDirectoryEntry root;
    private final List<NXFileDataProvider> subNxFiles;
    private final Map<String, NXFileDataProvider> folderNxFiles;
    private String subStr;

    public NXFileDataProvider(File nxfile) {
        this(nxfile, "", nxfile.isDirectory(), null);
    }

    public NXFileDataProvider(File nxfile, String subStr, boolean loadNxAsFolder, NXFileDataProvider parent) {
        if (!nxfile.exists() && subStr.isEmpty()) {
            throw new RuntimeException("讀取檔案時出錯:檔案不存在");
        }
        this.folderNxFiles = new LinkedHashMap<>();
        this.nxfile = nxfile;
        this.subStr = subStr;

        try (BufferedRandomAccessFile raf = new BufferedRandomAccessFile(nxfile, "r")) {
            NxLittleEndianAccessor nlea = new NxLittleEndianAccessor(new RandomAccessByteStream(raf));
            tables = new NXTables(nlea);
            Header = nlea.Header;
            String nodePath = nxfile.getName().replaceAll((loadNxAsFolder ? "_" : "") + "\\d+", "");
            nodePath = nodePath.substring(0, nodePath.lastIndexOf('.'));
            if (parent != null) {
                nodePath = parent.root.getName() + "/" + nodePath;
            }
            root = new MapleDataDirectoryEntry(nodePath, 20, 0, Header.NodeOffset, null);
            parseDirectory(root, nlea, loadNxAsFolder && subStr.isEmpty());
        } catch (Exception e) {
            throw new RuntimeException("讀取檔案時出錯");
        }

        subNxFiles = new ArrayList<>();
        if (subStr.isEmpty()) {
            for (File file : Objects.requireNonNull(nxfile.getParentFile().listFiles())) {
                if (file.isDirectory() || file.getPath().equalsIgnoreCase(nxfile.getPath())) {
                    continue;
                }
                if (file.getName().replaceAll((loadNxAsFolder ? "_" : "") + "\\d+", "").toLowerCase().equals(nxfile.getName().toLowerCase())) {
                    String sub = file.getName();
                    sub = sub.substring(0, sub.lastIndexOf("."));
                    NXFileDataProvider data = new NXFileDataProvider(file, sub, loadNxAsFolder, parent);
                    subNxFiles.add(data);
                    root.addAll(data.getRoot());
                }
            }
        }
    }

    private void parseDirectory(MapleDataDirectoryEntry dir, NxLittleEndianAccessor nlea, boolean loadNxAsFolder) {
        nlea.seek(dir.getOffset());
        nlea.readInt(); // Node name
        int fCID = nlea.readInt(); // First Child ID
        short entryCount = nlea.readShort(); // Children count
        for (int i = 0; i < entryCount; i++) {
            long offset = Header.NodeOffset + (fCID + i) * NXNode.NODE_SIZE;
            nlea.seek(offset);
            String name = tables.getString(nlea, (int) nlea.readUInt());
            int cFCID = nlea.readInt();
            nlea.readShort();
            int type = nlea.readInt();
            if (type == 0 && cFCID != 0 && !name.toLowerCase().endsWith(".img")) {
                dir.addDirectory(new MapleDataDirectoryEntry(name, 20, 0, offset, dir));
            } else {
                dir.addFile(new MapleDataFileEntry(name, 20, 0, offset, dir, subStr));
            }
        }

        for (MapleDataDirectoryEntry idir : dir.getSubdirectories()) {
            if (!loadNxAsFolder) {
                parseDirectory(idir, nlea, false);
            } else {
                File dirFile = new File(nxfile.getParent() + File.separator + idir.getName());
                if (dirFile.exists() && dirFile.isDirectory()) {
                    NXFileDataProvider data = new NXFileDataProvider(dirFile, "", true, this);
                    idir.addAll(data.getRoot());
                    folderNxFiles.put(data.getRoot().getName(), data);
                }
            }
        }
    }

    public NxIMGFile getImgFile(String path) {
        String[] segments = path.split("/");
        MapleDataDirectoryEntry dir = root;
        for (int x = 0; x < segments.length - 1; x++) {
            dir = (MapleDataDirectoryEntry) dir.getEntry(segments[x]);
            if (dir == null) {
                return null;
            }
        }
        MapleDataFileEntry entry = (MapleDataFileEntry) dir.getEntry(segments[segments.length - 1]);
        if (entry == null || !subStr.equals(entry.getSub())) {
            return null;
        }
        return new NxIMGFile(nxfile.getPath(), entry, Header, tables);
    }

    @Override
    public MapleData getData(String path) {
        MapleData ret = null;
        if (!folderNxFiles.isEmpty()) {
            String pName = root.getName();
            pName += "/" + (path.contains("/") ? path.substring(0, path.indexOf("/")) : path);
            if (folderNxFiles.containsKey(pName)) {
                return folderNxFiles.get(pName).getData(path.substring(path.indexOf("/") + 1));
            }
        }
        NxIMGFile file = null;
        List<String> segments = new LinkedList<>(Arrays.asList(path.split("/")));
        MapleDataDirectoryEntry dir = root;
        Iterator<String> it = segments.iterator();
        while (it.hasNext()) {
            String segment = it.next();
            MapleDataEntry mde = dir.getEntry(segment);
            it.remove();
            if (mde == null) {
                break;
            } else if (mde instanceof MapleDataDirectoryEntry) {
                dir = (MapleDataDirectoryEntry) mde;
            } else if (mde instanceof MapleDataFileEntry) {
                MapleDataFileEntry entry = (MapleDataFileEntry) mde;
                if (entry == null || !subStr.equals(entry.getSub())) {
                    break;
                }
                file = new NxIMGFile(nxfile.getPath(), entry, Header, tables);
                break;
            } else {
                break;
            }
        }
        if (file == null) {
            for (NXFileDataProvider nx : subNxFiles) {
                ret = nx.getData(path);
                if (ret != null) {
                    break;
                }
            }
        } else {
            ret = file.getRoot();
            String p = null;
            for (String segment : segments) {
                if (p == null) {
                    p = "";
                } else {
                    p += "/";
                }
                p += segment;
            }
            if (p != null) {
                ret = ret.getChildByPath(p);
            }
        }
        return ret;
    }

    @Override
    public MapleDataDirectoryEntry getRoot() {
        return root;
    }

    public File getFile() {
        return nxfile;
    }
}
