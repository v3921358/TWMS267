package Config.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.config.ConfigurableProcessor;
import tools.config.Property;
import tools.config.PropertyTransformerFactory;
import tools.config.TransformationException;
import tools.config.transformers.PropertyTransformer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;

public class ConfigurableProcessor2 extends ConfigurableProcessor {
    private static final Logger log = LoggerFactory.getLogger(ConfigurableProcessor2.class);

    public static void process(Object object, Properties... properties) {
        Class clazz;
        if (object instanceof Class) {
            clazz = (Class) object;
            object = null;
        } else {
            clazz = object.getClass();
        }

        process(clazz, object, properties);
    }

    private static void process(Class<?> clazz, Object obj, Properties[] props) {
        processFields(clazz, obj, props);
        if (obj == null) {
            Class[] var3 = clazz.getInterfaces();
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                Class<?> itf = var3[var5];
                process(itf, obj, props);
            }
        }

        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            process(superClass, obj, props);
        }

    }

    private static void processFields(Class<?> clazz, Object obj, Properties[] props) {
        Field[] var3 = clazz.getDeclaredFields();
        int var4 = var3.length;

        for (int var5 = 0; var5 < var4; ++var5) {
            Field f = var3[var5];
            if ((!Modifier.isStatic(f.getModifiers()) || obj == null) && (Modifier.isStatic(f.getModifiers()) || obj != null) && f.isAnnotationPresent(Property.class)) {
                if (Modifier.isFinal(f.getModifiers())) {
                    log.error("Attempt to proceed final field " + f.getName() + " of class " + clazz.getName());
                    throw new RuntimeException();
                }

                processField(f, obj, props);
            }
        }

    }

    private static void processField(Field f, Object obj, Properties[] props) {
        boolean oldAccessible = f.isAccessible();
        f.setAccessible(true);

        try {
            Property property = f.getAnnotation(Property.class);
            if ("DO_NOT_OVERWRITE_INITIALIAZION_VALUE".equals(property.defaultValue()) && !isKeyPresent(property.key(), props)) {
                if (log.isDebugEnabled()) {
                    log.debug("Field " + f.getName() + " of class " + f.getDeclaringClass().getName() + " wasn't modified");
                }
            } else {
                f.set(obj, getFieldValue(f, props));
            }
        } catch (Exception var5) {
            throw new RuntimeException("Can't transform field " + f.getName() + " of class " + f.getDeclaringClass(), var5);
        }

        f.setAccessible(oldAccessible);
    }

    private static Object getFieldValue(Field field, Properties[] props) throws TransformationException {
        Property property = field.getAnnotation(Property.class);
        String defaultValue = property.defaultValue();
        String key = property.key();
        String value = null;
        if (key.isEmpty()) {
            log.warn("Property " + field.getName() + " of class " + field.getDeclaringClass().getName() + " has empty key");
        } else {
            value = findPropertyByKey(key, props);
        }

        if (value == null || value.trim().equals("")) {
            value = defaultValue;
            Config.setProperty(key, defaultValue);
            if (log.isDebugEnabled()) {
                log.debug("Using default value for field " + field.getName() + " of class " + field.getDeclaringClass().getName());
            }
        } else if (value.indexOf('{') != -1) {
            value = interpolate(value, props);
        }

        PropertyTransformer<?> pt = PropertyTransformerFactory.newTransformer(field.getType(), property.propertyTransformer());
        return pt.transform(value, field);
    }

    private static String findPropertyByKey(String key, Properties[] props) {
        Properties[] var2 = props;
        int var3 = props.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            Properties p = var2[var4];
            if (p.containsKey(key)) {
                return p.getProperty(key);
            }
        }

        return null;
    }

    private static boolean isKeyPresent(String key, Properties[] props) {
        return findPropertyByKey(key, props) != null;
    }

    private static String interpolate(String template, Properties[] props) {
        StringBuilder result = new StringBuilder();
        StringBuilder keyBuilder = new StringBuilder();
        boolean inPlaceholder = false;

        for (int i = 0; i < template.length(); i++) {
            char ch = template.charAt(i);

            // Handle double {{ and }} for escaping
            if (ch == '{') {
                if (i + 1 < template.length() && template.charAt(i + 1) == '{') {
                    result.append('{');
                    i++;  // Skip the next '{'
                } else {
                    inPlaceholder = true;
                    keyBuilder.setLength(0);  // Reset key builder
                }
            } else if (ch == '}') {
                if (i + 1 < template.length() && template.charAt(i + 1) == '}') {
                    result.append('}');
                    i++;  // Skip the next '}'
                } else if (inPlaceholder) {
                    String key = keyBuilder.toString();
                    String value = findPropertyByKey(key, props);
                    if (value != null) {
                        result.append(value);
                    } else {
                        result.append("{").append(key).append("}");
                    }
                    inPlaceholder = false;
                } else {
                    result.append('}');
                }
            } else {
                if (inPlaceholder) {
                    keyBuilder.append(ch);  // Building the key inside {}
                } else {
                    result.append(ch);  // Regular characters
                }
            }
        }

        return result.toString();
    }
}
