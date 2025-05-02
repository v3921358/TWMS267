package tools.config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class PropertiesUtils {
    public static Properties load(File file) throws IOException {
        /* 20 */
        FileInputStream fis = new FileInputStream(file);
        /* 21 */
        BufferedReader buff = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
        /* 22 */
        Properties props = new Properties();
        /* 23 */
        props.load(buff);
        /* 24 */
        fis.close();
        /* 25 */
        buff.close();
        /* 26 */
        return props;
    }

    public static Properties[] load(List<File> files) throws IOException {
        /* 30 */
        Properties[] result = new Properties[files.size()];
        /* 31 */
        for (int i = 0; i < result.length; i++) {
            /* 32 */
            result[i] = load(files.get(i));
        }
        /* 34 */
        return result;
    }

    public static Properties[] loadAllFromDirectory(String dir) throws IOException {
        /* 38 */
        return loadAllFromDirectory(new File(dir));
    }

    public static Properties[] loadAllFromDirectory(File dir) throws IOException {
        /* 42 */
        return load(getAllPropertiesFiles(dir));
    }

    public static List<File> getAllPropertiesFiles(File dir) {
        try {
            /* 47 */
            return (List<File>) Files.list(dir.toPath()).map(Path::toFile)
                    .filter(it -> it.getName().endsWith(".properties")).collect(Collectors.toList());
            /* 48 */
        } catch (IOException e) {
            /* 49 */
            e.printStackTrace();
            /* 50 */
            return Collections.emptyList();
        }
    }
}
