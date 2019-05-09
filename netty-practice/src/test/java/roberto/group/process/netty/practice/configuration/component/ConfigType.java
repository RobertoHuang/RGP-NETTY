/**
 * FileName: ConfigType
 * Author:   HuangTaiHong
 * Date:     2019/5/9 10:55
 * Description: 配置字段类型.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.component;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roberto.group.process.netty.practice.configuration.exception.ConfigException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 〈配置字段类型.〉
 *
 * @author HuangTaiHong
 * @create 2019/5/9
 * @since 1.0.0
 */
public abstract class ConfigType<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigType.class);

    private static final String STR_TRUE = "true";
    private static final String STR_FALSE = "false";

    // 枚举时使用
    public ConfigType<T> with(String name, String... values) {
        return this;
    }

    public abstract T convertValue(String name, Object object);

    public static final ConfigType<Short> SHORT = new ConfigType<Short>() {
        @Override
        public Short convertValue(String name, Object value) {
            try {
                if (value == null) {
                    return null;
                } else if (value instanceof Short) {
                    return (Short) value;
                } else if (value instanceof String) {
                    return Short.parseShort(((String) value).trim());
                }
            } catch (Exception e) {

            }
            String message = name + " Expected value to be a short, but it was " + value;
            LOGGER.error(message);
            throw new ConfigException(message);
        }
    };

    public static final ConfigType<Boolean> BOOLEAN = new ConfigType<Boolean>() {
        @Override
        public Boolean convertValue(String name, Object value) {
            try {
                if (value == null) {
                    return null;
                } else if (value instanceof Boolean) {
                    return (Boolean) value;
                } else if (value instanceof String) {
                    String trimValue = ((String) value).trim().toLowerCase();
                    if (STR_TRUE.equals(trimValue)) {
                        return true;
                    } else if (STR_FALSE.equals(trimValue)) {
                        return false;
                    }
                }
            } catch (Exception e) {

            }
            String message = name + " Expected value to be either true or false, but it was " + value;
            LOGGER.error(message);
            throw new ConfigException(message);
        }
    };

    public static final ConfigType<String> STRING = new ConfigType<String>() {
        @Override
        public String convertValue(String name, Object value) {
            if (value == null) {
                return null;
            } else if (value instanceof String) {
                return ((String) value).trim();
            }

            String message = name + " Expected value to be a string, but it was " + value;
            LOGGER.error(message);
            throw new ConfigException(message);
        }
    };

    public static final ConfigType<Integer> INT = new ConfigType<Integer>() {
        @Override
        public Integer convertValue(String name, Object value) {
            try {
                if (value == null) {
                    return null;
                } else if (value instanceof Integer) {
                    return (Integer) value;
                } else if (value instanceof String) {
                    return Integer.parseInt(((String) value).trim());
                }
            } catch (Exception e) {

            }
            String message = name + " Expected value to be a integer, but it was " + value;
            LOGGER.error(message);
            throw new ConfigException(message);
        }
    };

    public static final ConfigType<Integer> UNSIGN_INT = new ConfigType<Integer>() {
        @Override
        public Integer convertValue(String name, Object value) {
            Integer i = null;
            try {
                if (value == null) {
                    return null;
                } else if (value instanceof Integer) {
                    i = (Integer) value;
                } else if (value instanceof String) {
                    i = Integer.parseInt(((String) value).trim());
                }
            } catch (Exception e) {

            }

            if (i == null) {
                String message = name + " Expected value to be a unsign int, but it was " + value;
                LOGGER.error(message);
                throw new ConfigException(message);
            }

            if (i < 0) {
                String message = name + " Expected value to be a unsign int, but it was less than 0, value is " + i;
                LOGGER.error(message);
                throw new ConfigException(message);
            }
            return i;
        }
    };

    public static final ConfigType<Long> LONG = new ConfigType<Long>() {
        @Override
        public Long convertValue(String name, Object value) {
            try {
                if (value == null) {
                    return null;
                } else if (value instanceof Long) {
                    return (Long) value;
                } else if (value instanceof String) {
                    return Long.parseLong(((String) value).trim());
                }
            } catch (Exception e) {

            }
            String message = name + " Expected value to be a long, but it was " + value;
            LOGGER.error(message);
            throw new ConfigException(message);
        }
    };

    public static final ConfigType<List<?>> LIST = new ConfigType<List<?>>() {
        @Override
        public List<?> convertValue(String name, Object value) {
            try {
                if (value == null) {
                    return null;
                } else if (value instanceof List) {
                    return (List<?>) value;
                } else if (value instanceof String) {
                    String trimValue = ((String) value).trim();
                    if (trimValue.isEmpty()) {
                        return Collections.emptyList();
                    }
                    return Arrays.asList(trimValue.split(","));
                }
            } catch (Exception e) {

            }
            String message = name + " Expected value to be a list, but it was " + value;
            LOGGER.error(message);
            throw new ConfigException(message);
        }
    };

    public static final ConfigType<String> ENUM = new ConfigType<String>() {
        Map<String, String[]> enmuValues = new HashMap<>();

        public ConfigType<String> with(String name, String... values) {
            enmuValues.put(name, values);
            return this;
        }

        @Override
        public String convertValue(String name, Object value) {
            if (!(value instanceof String)) {
                String message = name + " Expected value to be a string, but it was " + value;
                LOGGER.error(message);
                throw new ConfigException(message);
            }

            String valueStr = ((String) value).trim();
            String[] values = enmuValues.get(name);
            for (String v : values) {
                if (StringUtils.equalsIgnoreCase(v, valueStr)) {
                    return valueStr;
                }
            }

            String message = name + " Expected value is a enum value in (" + StringUtils.join(values, ",") + ") but the actual value was " + valueStr;
            LOGGER.error(message);
            throw new ConfigException(message);
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
                    String message = name + " can not find class,class name: " + value;
                    LOGGER.error(message);
                    throw new ConfigException(message);
                }
            }
            String message = name + " Expected a Class instance or class name, but it was a " + value.getClass().getName();
            LOGGER.error(message);
            throw new ConfigException(message);
        }
    };
}