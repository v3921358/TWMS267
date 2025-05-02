/*     */
package tools.config;

/*     */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.config.transformers.PropertyTransformer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/*     */
/*     */
public class ConfigurableProcessor {
    /* 15 */
    private static final Logger Logger = LoggerFactory.getLogger(ConfigurableProcessor.class);

    /*     */
    /*     */
    public static void process(Object object, Properties... properties) {
        Class<?> clazz;
        /* 20 */
        if (object instanceof Class) {
            /* 21 */
            clazz = (Class) object;
            /* 22 */
            object = null;
        } else {
            /* 24 */
            clazz = object.getClass();
        }
        /*     */
        /* 27 */
        process(clazz, object, properties);
    }

    /*     */
    private static void process(Class<?> clazz, Object obj, Properties[] props) {
        /* 31 */
        processFields(clazz, obj, props);
        /*     */
        /* 33 */
        if (obj == null) {
            /* 34 */
            for (Class<?> itf : clazz.getInterfaces()) {
                /* 35 */
                process(itf, obj, props);
            }
        }
        /*     */
        /* 39 */
        Class<?> superClass = clazz.getSuperclass();
        /* 40 */
        if (superClass != null && superClass != Object.class) {
            /* 41 */
            process(superClass, obj, props);
        }
    }

    /*     */
    private static void processFields(Class<?> clazz, Object obj, Properties[] props) {
        /* 46 */
        for (Field f : clazz.getDeclaredFields()) {
            /* 47 */
            if (!Modifier.isStatic(f.getModifiers()) || obj == null) {
                /*     */
                /* 50 */
                if (Modifier.isStatic(f.getModifiers()) || obj != null) {
                    /*     */
                    /* 53 */
                    if (f.isAnnotationPresent((Class) Property.class)) {
                        /* 54 */
                        if (Modifier.isFinal(f.getModifiers())) {
                            /* 55 */
                            Logger.error(
                                    "Attempt to proceed final field " + f.getName() + " of class " + clazz.getName());
                            /* 56 */
                            throw new RuntimeException();
                        }
                        /* 58 */
                        processField(f, obj, props);
                    }
                }
            }
        }
    }

    /*     */
    private static void processField(Field f, Object obj, Properties[] props) {
        /* 64 */
        boolean oldAccessible = f.isAccessible();
        /* 65 */
        f.setAccessible(true);
        try {
            /* 67 */
            Property property = f.<Property>getAnnotation(Property.class);
            /* 68 */
            if (!"DO_NOT_OVERWRITE_INITIALIAZION_VALUE".equals(property.defaultValue())
                    || isKeyPresent(property.key(), props)) {
                /* 69 */
                f.set(obj, getFieldValue(f, props));
                /* 70 */
            } else if (Logger.isDebugEnabled()) {
                /* 71 */
                Logger.debug(
                        "Field " + f.getName() + " of class " + f.getDeclaringClass().getName() + " wasn't modified");
            }
            /* 73 */
        } catch (Exception e) {
            /* 74 */
            throw new RuntimeException(
                    "Can't transform field " + f.getName() + " of class " + f.getDeclaringClass(), e);
        }
        /* 76 */
        f.setAccessible(oldAccessible);
    }

    /*     */
    private static Object getFieldValue(Field field, Properties[] props) throws TransformationException {
        /* 80 */
        Property property = field.<Property>getAnnotation(Property.class);
        /* 81 */
        String defaultValue = property.defaultValue();
        /* 82 */
        String key = property.key();
        /* 83 */
        String value = null;
        /*     */
        /* 85 */
        if (key.isEmpty()) {
            /* 86 */
            Logger.warn("Property " + field.getName() + " of class " + field.getDeclaringClass().getName()
                    + " has empty key");
        } else {
            /* 88 */
            value = findPropertyByKey(key, props);
        }
        /*     */
        /* 91 */
        if (value == null || value.trim().equals("")) {
            /* 92 */
            value = defaultValue;
            /* 93 */
            if (Logger.isDebugEnabled()) {
                /* 94 */
                Logger.debug("Using default value for field " + field.getName() + " of class " + field
                        /* 95 */.getDeclaringClass().getName());
            }
        }
        /*     */
        /* 99 */
        PropertyTransformer<?> pt = PropertyTransformerFactory.newTransformer(field.getType(), property
                /* 100 */.propertyTransformer());
        /* 101 */
        return pt.transform(value, field);
    }

    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    private static String findPropertyByKey(String key, Properties[] props) {
        /* 112 */
        for (Properties p : props) {
            /* 113 */
            if (p.containsKey(key)) {
                /* 114 */
                return p.getProperty(key);
            }
        }
        /*     */
        /* 118 */
        return null;
    }

    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    /*     */
    private static boolean isKeyPresent(String key, Properties[] props) {
        /* 129 */
        return (findPropertyByKey(key, props) != null);
    }

    /*     */
    public static List<String> getKeys(Properties prop) {
        /* 133 */
        List<String> ret = new ArrayList<>();
        /* 134 */
        for (Object o : prop.values()) {
            /* 135 */
            ret.add((String) o);
        }
        /* 137 */
        return ret;
    }
}
