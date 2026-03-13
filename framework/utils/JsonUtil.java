package framework.utils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

public class JsonUtil {

    public static String toJson(Object value) {
        StringBuilder sb = new StringBuilder();
        write(value, sb);
        return sb.toString();
    }

    private static void write(Object value, StringBuilder sb) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            writeString((String) value, sb);
        } else if (value instanceof Character) {
            writeString(value.toString(), sb);
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value.toString());
        } else if (value instanceof Enum) {
            writeString(((Enum<?>) value).name(), sb);
        } else if (value instanceof Map) {
            writeMap((Map<?, ?>) value, sb);
        } else if (value instanceof Collection) {
            writeCollection((Collection<?>) value, sb);
        } else if (value.getClass().isArray()) {
            writeArray(value, sb);
        } else {
            writeObject(value, sb);
        }
    }

    private static void writeString(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }

    private static void writeMap(Map<?, ?> map, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            writeString(String.valueOf(entry.getKey()), sb);
            sb.append(':');
            write(entry.getValue(), sb);
        }
        sb.append('}');
    }

    private static void writeCollection(Collection<?> collection, StringBuilder sb) {
        sb.append('[');
        boolean first = true;
        for (Object item : collection) {
            if (!first) sb.append(',');
            first = false;
            write(item, sb);
        }
        sb.append(']');
    }

    private static void writeArray(Object array, StringBuilder sb) {
        sb.append('[');
        int length = java.lang.reflect.Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) sb.append(',');
            write(java.lang.reflect.Array.get(array, i), sb);
        }
        sb.append(']');
    }

    private static void writeObject(Object obj, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isSynthetic()) continue;
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(obj);
                if (!first) sb.append(',');
                first = false;
                writeString(field.getName(), sb);
                sb.append(':');
                write(fieldValue, sb);
            } catch (IllegalAccessException ignored) {
            }
        }
        sb.append('}');
    }
}