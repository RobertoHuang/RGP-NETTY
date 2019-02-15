/**
 * FileName: AbstractConfigManager
 * Author:   HuangTaiHong
 * Date:     2019/2/15 14:01
 * Description: config manager.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.manager;

import roberto.group.process.netty.practice.configuration.component.ConfigDefine;
import roberto.group.process.netty.practice.configuration.component.ConfigKey;
import roberto.group.process.netty.practice.configuration.component.ConfigType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 〈config manager.〉
 *
 * @author HuangTaiHong
 * @create 2019/2/15
 * @since 1.0.0
 */
public abstract class AbstractConfigManager {
    private final ConfigDefine configDefine;
    private final Map<String, Object> params;

    @SuppressWarnings("unchecked")
    public AbstractConfigManager(ConfigDefine configDefine, Map<?, ?> params) {
        for (Map.Entry<?, ?> entry : params.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new IllegalArgumentException("key must be a string,key:" + entry.getKey());
            }
        }
        this.configDefine = configDefine;
        this.params = configDefine.parse((Map<String, ?>) params);
    }

    protected Object get(String key) {
        if (!params.containsKey(key)) {
            throw new IllegalArgumentException("Unknown config,key:" + key);
        }
        return params.get(key);
    }

    protected Short getShort(String key) {
        return (Short) get(key);
    }

    protected Boolean getBoolean(String key) {
        return (Boolean) get(key);
    }

    protected String getString(String key) {
        return (String) get(key);
    }

    protected Integer getInt(String key) {
        return (Integer) get(key);
    }

    protected Long getLong(String key) {
        return (Long) get(key);
    }

    protected Class<?> getClass(String key) {
        return (Class<?>) get(key);
    }

    @SuppressWarnings("unchecked")
    protected List<String> getList(String key) {
        return (List<String>) get(key);
    }

    protected ConfigType<?> typeOf(String key) {
        ConfigKey<?> configKey = configDefine.getConfigKeys().get(key);
        return (configKey == null) ? null : configKey.getConfigType();
    }

    protected <T> T getInstance(String key, Class<T> t) {
        Class<?> c = getClass(key);
        if (c == null) {
            return null;
        }
        Object o = this.newInstance(c);
        if (!t.isInstance(o)) {
            throw new RuntimeException(c.getName() + " is not an instance of " + t.getName());
        }
        return t.cast(o);
    }

    protected <T> List<T> getListInstance(String key, Class<T> t) {
        List<T> objects = new ArrayList<>();
        List<String> classNames = getList(key);

        if (classNames == null) {
            return objects;
        }

        for (Object clazz : classNames) {
            T o;
            if (clazz instanceof String) {
                o = AbstractConfigManager.getInstanceByClassName((String) clazz, t);
            } else if (clazz instanceof Class<?>) {
                Object instance = AbstractConfigManager.newInstance((Class<?>) clazz);
                if (!t.isInstance(instance)) {
                    throw new RuntimeException(((Class<?>) clazz).getName() + " is not an instance of " + t.getName());
                } else {
                    o = t.cast(instance);
                }
            } else {
                throw new RuntimeException("List contains element of type " + clazz.getClass().getName() + ", expected String or Class");
            }
            objects.add(o);
        }
        return objects;
    }

    /*********************************工具类*********************************/
    public static <T> T newInstance(Class<T> c) {
        if (c == null) {
            throw new RuntimeException("class cannot be null");
        }

        try {
            return c.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Could not find a public no-argument constructor for " + c.getName(), e);
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new RuntimeException("Could not instantiate class " + c.getName(), e);
        }
    }

    public static <T> T getInstanceByClassName(String className, Class<T> t) {
        Class<?> c;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("class not found,name:" + className, e);
        }

        if (c == null) {
            return null;
        }

        Object o = AbstractConfigManager.newInstance(c);
        if (!t.isInstance(o)) {
            throw new RuntimeException(c.getName() + " is not an instance of " + t.getName());
        }
        return t.cast(o);
    }
}