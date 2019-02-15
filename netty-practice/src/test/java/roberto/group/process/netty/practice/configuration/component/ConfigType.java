/**
 * FileName: ConfigType
 * Author:   HuangTaiHong
 * Date:     2019/2/15 10:17
 * Description: config value type.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.component;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 〈config value type.〉
 *
 * @author HuangTaiHong
 * @create 2019/2/15
 * @since 1.0.0
 */
public abstract class ConfigType<T> {
    public abstract T convertValue(String name, Object o);

    // 枚举时使用
    public ConfigType<T> with(String name, String... values) {
        return this;
    }

    public static final ConfigType<Short> SHORT = new ConfigType<Short>() {
        @Override
        public Short convertValue(String name, Object value) {
            if (value instanceof Short) {
                return (Short) value;
            } else if (value instanceof String) {
                return Short.parseShort(((String) value).trim());
            }
            throw new IllegalArgumentException(name + " Expected value to be a short, but it was a " + value.getClass().getName());
        }
    };

    public static final ConfigType<Boolean> BOOLEAN = new ConfigType<Boolean>() {
        @Override
        public Boolean convertValue(String name, Object value) {
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else if (value instanceof String) {
                String trimValue = ((String) value).trim().toLowerCase();
                if ("true".equals(trimValue)) {
                    return true;
                } else if (trimValue.equals("false")) {
                    return false;
                }
                throw new IllegalArgumentException(name + " Expected value to be either true or false");
            }
            throw new IllegalArgumentException(name + " Expected value to be either true or false");
        }
    };

    public static final ConfigType<String> STRING = new ConfigType<String>() {
        @Override
        public String convertValue(String name, Object value) {
            if (value instanceof String) {
                return ((String) value).trim();
            }
            throw new IllegalArgumentException(name + " Expected value to be a string, but it was a ");
        }
    };

    public static final ConfigType<Integer> INT = new ConfigType<Integer>() {
        @Override
        public Integer convertValue(String name, Object value) {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.parseInt(((String) value).trim());
            }
            throw new IllegalArgumentException(name + " Expected value to be a integer, but it was a " + value.getClass().getName());
        }
    };

    public static final ConfigType<Integer> UNSIGN_INT = new ConfigType<Integer>() {
        @Override
        public Integer convertValue(String name, Object value) {
            Integer i = null;
            if (value instanceof Integer) {
                i = (Integer) value;
            } else if (value instanceof String) {
                i = Integer.parseInt(((String) value).trim());
            }
            if (i == null) {
                throw new IllegalArgumentException(name + " Expected value to be a unsign int, but it was a " + value.getClass().getName());
            }
            if (i < 0) {
                throw new IllegalArgumentException(name + " Expected value to be a unsign int, but it was less than 0,value is " + i);
            }
            return i;
        }
    };

    public static final ConfigType<Long> LONG = new ConfigType<Long>() {
        @Override
        public Long convertValue(String name, Object value) {
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof String) {
                return Long.parseLong(((String) value).trim());
            }
            throw new RuntimeException(name + " Expected value to be a long, but it was a " + value.getClass().getName());
        }
    };

    public static final ConfigType<List<?>> LIST = new ConfigType<List<?>>() {
        @Override
        public List<?> convertValue(String name, Object value) {
            if (value instanceof List) {
                return (List<?>) value;
            } else if (value instanceof String) {
                String trimValue = ((String) value).trim();
                if (trimValue.isEmpty()) {
                    return Collections.emptyList();
                }
                return Arrays.asList(trimValue.split(","));
            }
            throw new RuntimeException(name + " Expected value to be a list, but it was a " + value.getClass().getName());
        }
    };

    public static final ConfigType<String> ENUM = new ConfigType<String>() {
        Map<String, String[]> enmuValues = new HashMap<>();

        @Override
        public ConfigType<String> with(String name, String... values) {
            enmuValues.put(name, values);
            return this;
        }

        @Override
        public String convertValue(String name, Object value) {
            if (!(value instanceof String)) {
                throw new RuntimeException(name + " Expected value to be a string, but it was a " + value.getClass().getName());
            }
            String valueStr = ((String) value).trim();
            String[] values = enmuValues.get(name);
            for (String v : values) {
                if (StringUtils.equalsIgnoreCase(v, valueStr)) {
                    return valueStr;
                }
            }
            throw new IllegalArgumentException(name + " Expected value is a enum value in (" + StringUtils.join(values, ",") + ") but the actual value was " + valueStr);
        }
    };

    public static final ConfigType<Class<?>> CLASS = new ConfigType<Class<?>>() {
        @Override
        public Class<?> convertValue(String name, Object value) {
            if (value instanceof Class) {
                return (Class<?>) value;
            } else if (value instanceof String) {
                try {
                    return Class.forName(((String) value).trim());
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException(name + " can not find class,class name: " + value);
                }
            }
            throw new IllegalArgumentException(name + " Expected a Class instance or class name,but it was a " + value.getClass().getName());
        }
    };
}