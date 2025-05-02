package SwordieX.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Util {

    private static final Map<Class, Class> boxedToPrimClasses = new HashMap<>();

    static {
        boxedToPrimClasses.put(Boolean.class, boolean.class);
        boxedToPrimClasses.put(Byte.class, byte.class);
        boxedToPrimClasses.put(Short.class, short.class);
        boxedToPrimClasses.put(Character.class, char.class);
        boxedToPrimClasses.put(Integer.class, int.class);
        boxedToPrimClasses.put(Long.class, long.class);
        boxedToPrimClasses.put(Float.class, float.class);
        boxedToPrimClasses.put(Double.class, double.class);
    }

    /**
     * Returns a random number from <code>start</code> up to <code>end</code>. Creates a new Random class upon call.
     * If <code>start</code> is greater than <code>end</code>, <code>start</code> will be swapped with <code>end</code>.
     *
     * @param start the lower bound of the random number
     * @param end   the upper bound of the random number
     * @return A random number from <code>start</code> up to <code>end</code>
     */
    public static int getRandom(int start, int end) {
        if (end - start == 0) {
            return start;
        }
        if (start > end) {
            int temp = end;
            end = start;
            start = temp;
        }
        return start + new Random().nextInt(end - start);
    }

    /**
     * Creates a byte array given a string. Ignores spaces and the '|' character.
     *
     * @param s The String to transform
     * @return The byte array that the String contained (if there is any, some RuntimeException otherwise)
     */
    public static byte[] getByteArrayByString(String s) {
        s = s.replace("|", " ");
        s = s.replace(" ", "");
        s = s.replace("\n", "");
        s = s.replace("\r", "");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Turns a byte array into a readable String (e.g., 3A 00 89 BF).
     *
     * @param arr The array to transform
     * @return The readable byte array
     */
    public static String readableByteArray(byte[] arr) {
        StringBuilder res = new StringBuilder();
        for (byte b : arr) {
            res.append(String.format("%02X ", b));
        }
        return res.toString();
    }

    /**
     * Gets a single element from a collection by using a predicate. Returns a random element if there are multiple
     * elements for which the predicate holds.
     *
     * @param collection The collection the element should be gathered from
     * @param pred       The predicate that should hold for the element
     * @param <T>        The type of the collection's elements
     * @return An element for which the predicate holds, or null if there is none
     */
    public static <T> T findWithPred(Collection<T> collection, Predicate<T> pred) {
        return collection.stream().filter(pred).findAny().orElse(null);
    }

    /**
     * Gets a single element from an array by using a predicate. Returns a random element if there are multiple
     * elements for which the predicate holds.
     *
     * @param arr  The array the element should be gathered from
     * @param pred The predicate that should hold for the element
     * @param <T>  The type of the collection's elements
     * @return An element for which the predicate holds, or null if there is none
     */
    public static <T> T findWithPred(T[] arr, Predicate<T> pred) {
        return findWithPred(Arrays.asList(arr), pred);
    }

    /**
     * Returns a formatted number, using English locale.
     *
     * @param number The number to be formatted
     * @return The formatted number
     */
    public static String formatNumber(String number) {
        return NumberFormat.getInstance(Locale.ENGLISH).format(Long.parseLong(number));
    }

    public static Class<?> convertBoxedToPrimitiveClass(Class<?> clazz) {
        return boxedToPrimClasses.getOrDefault(clazz, clazz);
    }

    /**
     * Searches through the given directory recursively to find all files
     *
     * @param toAdd the set to add the files to
     * @param dir   the directory to start in
     */
    public static void findAllFilesInDirectory(Set<File> toAdd, File dir) {
        // depth first search
        if (dir != null) {
            if (dir.isDirectory()) {
                for (File file : Objects.requireNonNull(dir.listFiles())) {
                    if (file.isDirectory()) {
                        findAllFilesInDirectory(toAdd, file);
                    } else {
                        toAdd.add(file);
                    }
                }
            }
        }
    }

    public static List<Class<?>> getClasses(String packageName, boolean isRecursive, Class<? extends Annotation> annotation) throws IOException, ClassNotFoundException {
        List<Class<?>> classList = new ArrayList<Class<?>>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String strFile = packageName.replaceAll("\\.", "/");
        Enumeration<URL> urls = loader.getResources(strFile);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url != null) {
                String protocol = url.getProtocol();
                String pkgPath = url.getPath();
                if ("file".equals(protocol)) {
                    findClasses(classList, packageName, pkgPath, isRecursive, annotation);
                } else if ("jar".equals(protocol)) {
                    findClasses(classList, packageName, url, isRecursive, annotation);
                }
            }
        }

        return classList;
    }

    private static void findClasses(List<Class<?>> clazzList, String packageName, URL url, boolean isRecursive, Class<? extends Annotation> annotation) throws IOException, ClassNotFoundException {
        JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
        JarFile jarFile = jarURLConnection.getJarFile();
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            String jarEntryName = jarEntry.getName();
            int endIndex = jarEntryName.lastIndexOf(".");
            String clazzName;
            if (endIndex > 0) {
                clazzName = jarEntryName.substring(0, endIndex);
            } else {
                clazzName = jarEntryName;
            }
            clazzName = clazzName.replace("/", ".");
            String prefix = null;
            endIndex = clazzName.lastIndexOf(".");
            if (endIndex > 0) {
                prefix = clazzName.substring(0, endIndex);
            }
            if (prefix != null && jarEntryName.endsWith(".class")) {
                if (prefix.equals(packageName)) {
                    addClass(clazzList, clazzName, annotation);
                } else if (isRecursive && prefix.startsWith(packageName)) {
                    addClass(clazzList, clazzName, annotation);
                }
            }
        }
    }

    private static void findClasses(List<Class<?>> classes, String packageName, String packagePath, boolean isRecursive, Class<? extends Annotation> annotation) throws ClassNotFoundException {
        if (classes == null) {
            return;
        }
        File[] files = filterClassFiles(packagePath);
        if (files != null) {
            for (File f : files) {
                String fileName = f.getName();
                if (f.isFile()) {
                    String clazzName = getClassName(packageName, fileName);
                    addClass(classes, clazzName, annotation);
                } else {
                    if (isRecursive) {
                        String subPkgName = packageName + "." + fileName;
                        String subPkgPath = packagePath + "/" + fileName;
                        findClasses(classes, subPkgName, subPkgPath, true, annotation);
                    }
                }
            }
        }
    }

    private static File[] filterClassFiles(String packagePath) {
        if (packagePath == null) {
            return null;
        }

        return new File(packagePath).listFiles(file -> (file.isFile() && file.getName().endsWith(".class")) || file.isDirectory());
    }

    private static String getClassName(String pkgName, String fileName) {
        int endIndex = fileName.lastIndexOf(".");
        String clazz = null;
        if (endIndex >= 0) {
            clazz = fileName.substring(0, endIndex);
        }
        String clazzName = null;
        if (clazz != null) {
            clazzName = pkgName + "." + clazz;
        }
        return clazzName;
    }

    private static void addClass(List<Class<?>> classes, String className, Class<? extends Annotation> annotation) throws ClassNotFoundException {
        if (classes != null && className != null) {
            Class<?> clazz = Class.forName(className);
            if (annotation == null) {
                classes.add(clazz);
            } else if (clazz.isAnnotationPresent(annotation)) {
                classes.add(clazz);
            }
        }
    }

    /**
     * Creates a directory if there is none.
     *
     * @param dir The directory to create
     */
    public static void makeDirIfAbsent(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public static int getCurrentTime() {
        return (int) System.currentTimeMillis();
    }

    public static long getCurrentTimeLong() {
        return System.currentTimeMillis();
    }

    public static boolean isNumber(String string) {
        return string != null && string.matches("-?\\d+(\\.\\d+)?");
    }
}
