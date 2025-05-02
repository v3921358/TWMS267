package Net;

import Config.configs.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class DumpWzList {

    public static void copyFolderWithPrefix(String srcFolder, String destFolder, String[] prefixes) {
        Path srcPath = Paths.get(srcFolder);
        Path destPath = Paths.get(destFolder);

        try {
            Files.walk(srcPath)
                    .forEach(source -> {
                        String relativePath = srcPath.relativize(source).toString();
                        Path destination = destPath.resolve(relativePath);

                        if (Files.isDirectory(source)) {
                            if (Arrays.stream(prefixes).noneMatch(prefix -> destination.getFileName().toString().startsWith(prefix))) {
                                try {
                                    Files.createDirectories(destination);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            if (Arrays.stream(prefixes).noneMatch(prefix -> source.getFileName().toString().startsWith(prefix))) {
                                try {
                                    Files.createDirectories(destination.getParent());
                                    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String WZPATH = Config.getProperty("wzpath", "Data");
        String srcFolder = WZPATH;
        String destFolder = "Data2";
        String[] prefixes = {"_Canvas", "Sound", "Base"};
        copyFolderWithPrefix(srcFolder, destFolder, prefixes);
    }
}

