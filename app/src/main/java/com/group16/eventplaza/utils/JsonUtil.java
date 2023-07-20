package com.group16.eventplaza.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {
    /**
     * map to object
     */
    public static Object mapToObject(Map<String, Object> map, Class<?> beanClass) throws Exception {
        if (map == null) {
            return null;
        }
        Object object = beanClass.newInstance();
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            int mod = field.getModifiers();
            if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
                continue;
            }
            field.setAccessible(true);
            field.set(object, map.get(field.getName()));
        }
        return object;
    }

    /**
     * objectToMap
     */
    public static Map<String, Object> objectToMap(Object obj) throws Exception {
        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            map.put(field.getName(), field.get(obj));
        }
        return map;
    }

}
