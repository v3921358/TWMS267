package tools;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BeanUtil {

    /**
     * 將一個 Map 對像轉化為一個 JavaBean
     *
     * @param type 要轉化的類型
     * @param map  包含屬性值的 map
     * @return 轉化出來的 JavaBean 對像
     * @throws IntrospectionException    如果分析類屬性失敗
     * @throws IllegalAccessException    如果實例化 JavaBean 失敗
     * @throws InstantiationException    如果實例化 JavaBean 失敗
     * @throws InvocationTargetException 如果調用屬性的 setter 方法失敗
     */
    public static <T> T fromMap(Class<T> type, Map map) throws IntrospectionException, IllegalAccessException, InstantiationException, InvocationTargetException {
        BeanInfo beanInfo = Introspector.getBeanInfo(type); // 獲取類屬性
        T obj = type.newInstance(); // 創建 JavaBean 對像

        // 給 JavaBean 對象的屬性賦值
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            String propertyName = descriptor.getName();

            if (map.containsKey(propertyName)) {
                // 下面一句可以 try 起來，這樣當一個屬性賦值失敗的時候就不會影響其他屬性賦值。
                Object value = map.get(propertyName);

                Object[] args = new Object[1];
                args[0] = value;

                descriptor.getWriteMethod().invoke(obj, args);
            }
        }
        return obj;
    }

    /**
     * 將一個 JavaBean 對像轉化為一個  Map
     *
     * @param bean 要轉化的JavaBean 對像
     * @return 轉化出來的  Map 對像
     * @throws IntrospectionException    如果分析類屬性失敗
     * @throws IllegalAccessException    如果實例化 JavaBean 失敗
     * @throws InvocationTargetException 如果調用屬性的 setter 方法失敗
     */
    public static Map<String, Object> toMap(Object bean) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        Class type = bean.getClass();
        Map<String, Object> returnMap = new HashMap<>();
        BeanInfo beanInfo = Introspector.getBeanInfo(type);

        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            String propertyName = descriptor.getName();
            if (!propertyName.equals("class")) {
                Method readMethod = descriptor.getReadMethod();
                Object result = readMethod.invoke(bean);
                returnMap.put(propertyName, result);
            }
        }
        return returnMap;
    }
}