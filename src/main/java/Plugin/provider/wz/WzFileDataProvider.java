package Plugin.provider.wz;

import Plugin.provider.*;
import Plugin.provider.wz.util.WzLittleEndianAccessor;
import tools.data.BufferedRandomAccessFile;
import tools.data.RandomAccessByteStream;

import java.io.File;
import java.util.*;

public class WzFileDataProvider implements MapleDataProvider {

    private final File wzfile;
    public WzHeader Header;
    private final MapleDataDirectoryEntry root;
    private long cOffset;
    private final List<WzFileDataProvider> subWzFiles;
    private final Map<String, WzFileDataProvider> folderWzFiles;
    private final String subStr;

    public WzFileDataProvider(File wzfile) {
        this(wzfile, "", wzfile.isDirectory(), null);
    }

    public WzFileDataProvider(File wzfile, String subStr, boolean loadWzAsFolder, WzFileDataProvider parent) {
        if (loadWzAsFolder && subStr.isEmpty()) {
            File candidate = new File(wzfile.getPath() + File.separator + wzfile.getName() + ".wz");
            if (!candidate.exists()) {
                candidate = new File(wzfile.getPath() + File.separator + wzfile.getName() + "00.ms");
            }
            wzfile = candidate;
        }
        if (!wzfile.exists()) {
            throw new RuntimeException("讀取檔案時出錯:檔案不存在");
        }

        this.folderWzFiles = new LinkedHashMap<>();
        this.wzfile = wzfile;
        String nodePath = wzfile.getName().replaceAll((loadWzAsFolder ? "_" : "") + "\\d+", "").split("\\.")[0];
        if (parent != null) {
            nodePath = parent.root.getName() + "/" + nodePath;
        }
        root = new MapleDataDirectoryEntry(nodePath, null);
        this.subStr = subStr;

        try (BufferedRandomAccessFile raf = new BufferedRandomAccessFile(wzfile, "r")) {
            WzLittleEndianAccessor wlea = new WzLittleEndianAccessor(new RandomAccessByteStream(raf));
            this.Header = new WzHeader(wlea);
            wlea.header = this.Header;
            wlea.seek(Header.DataStartPosition);
            // Root directory
            parseDirectory(root, wlea, loadWzAsFolder && subStr.isEmpty());

            cOffset = wlea.getPosition();
            getOffsets(root);
        } catch (Exception ignored) {
        }

        subWzFiles = new ArrayList<>();
        if (subStr.isEmpty()) {
            for (File file : Objects.requireNonNull(wzfile.getParentFile().listFiles())) {
                if (file.isDirectory() || file.getPath().equalsIgnoreCase(wzfile.getPath())) {
                    continue;
                }
                if (file.getName().replaceAll((loadWzAsFolder ? "_" : "") + "\\d+", "")
                        .equalsIgnoreCase(wzfile.getName())) {
                    String sub = file.getName();
                    sub = sub.substring(0, sub.lastIndexOf("."));
                    WzFileDataProvider data = new WzFileDataProvider(file, sub, loadWzAsFolder, parent);
                    subWzFiles.add(data);
                    root.addAll(data.getRoot());
                }
            }
        }
    }

    private void getOffsets(MapleDataDirectoryEntry dir) {
        for (MapleDataFileEntry file : dir.getFiles()) {
            if (!subStr.equalsIgnoreCase(file.getSub())) {
                continue;
            }
            file.setOffset(cOffset);
            cOffset += file.getSize();
        }
        for (MapleDataDirectoryEntry sdir : dir.getSubdirectories()) {
            getOffsets(sdir);
        }
    }

    private void parseDirectory(MapleDataDirectoryEntry dir, WzLittleEndianAccessor wlea, boolean loadWzAsFolder) {
        // Amount of entries
        int entryCount = wlea.readCompressedInt();
        for (int i = 0; i < entryCount; i++) {

            // Get Type
            // Type (if & 1 then its a Directory, else its an Object)
            long pos = wlea.getPosition();
            byte type = wlea.readByte();
            wlea.seek(pos);

            String fname = wlea.readStringBlock(Header.FStart + 1);
            int fsize = wlea.readCompressedInt();
            int checksum = wlea.readCompressedInt();
            long offset = wlea.readOffset();

            if (type == 3) {
                dir.addDirectory(new MapleDataDirectoryEntry(fname, fsize, checksum, offset, dir));
            } else {
                dir.addFile(new MapleDataFileEntry(fname, fsize, checksum, offset, dir, subStr));
            }
        }

        for (MapleDataDirectoryEntry idir : dir.getSubdirectories()) {
            if (!loadWzAsFolder) {
                parseDirectory(idir, wlea, false);
            } else {
                File dirFile = new File(wzfile.getParent() + File.separator + idir.getName());
                if (dirFile.exists() && dirFile.isDirectory()) {
                    WzFileDataProvider data = new WzFileDataProvider(dirFile, "", true, this);
                    idir.addAll(data.getRoot());
                    folderWzFiles.put(data.getRoot().getName(), data);
                }
            }
        }
    }

    @Override
    public MapleData getData(String path) {
        MapleData ret = null;
        if (!folderWzFiles.isEmpty()) {
            String pName = root.getName();
            pName += "/" + (path.contains("/") ? path.substring(0, path.indexOf("/")) : path);
            if (folderWzFiles.containsKey(pName)) {
                return folderWzFiles.get(pName).getData(path.substring(path.indexOf("/") + 1));
            }
        }

        WzIMGFile imgFile = null;
        List<String> segments = new LinkedList<>(Arrays.asList(path.split("/")));
        MapleDataDirectoryEntry dir = root;
        Iterator<String> it = segments.iterator();
        label:
        while (it.hasNext()) {
            String segment = it.next();
            MapleDataEntry mde = dir.getEntry(segment);
            it.remove();
            switch (mde) {
                case MapleDataDirectoryEntry mapleDataDirectoryEntry:
                    dir = mapleDataDirectoryEntry;
                    break;
                case MapleDataFileEntry entry:
                    if (!subStr.equals(entry.getSub())) {
                        break label;
                    }
                    imgFile = new WzIMGFile(wzfile.getPath(), entry);
                    break label;
                case null, default:
                    break label;
            }
        }

        if (imgFile == null) {
            for (WzFileDataProvider wz : subWzFiles) {
                ret = wz.getData(path);
                if (ret != null) {
                    break;
                }
            }
        } else {
            ret = imgFile.getRoot();
            StringBuilder p = null;
            for (String segment : segments) {
                if (p == null) {
                    p = new StringBuilder();
                } else {
                    p.append("/");
                }
                p.append(segment);
            }
            if (p != null) {
                ret = ret.getChildByPath(p.toString());
            }
        }
        return ret;
    }

    @Override
    public MapleDataDirectoryEntry getRoot() {
        return root;
    }

    public File getFile() {
        return wzfile;
    }
}
