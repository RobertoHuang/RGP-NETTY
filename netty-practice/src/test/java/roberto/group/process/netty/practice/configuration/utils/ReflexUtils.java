/**
 * FileName: ReflexUtils
 * Author:   HuangTaiHong
 * Date:     2019/5/9 15:37
 * Description: 反射工具类.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.configuration.utils;

/**
 * 〈反射工具类.〉
 *
 * @author HuangTaiHong
 * @create 2019/5/9
 * @since 1.0.0
 */
public class ReflexUtils {
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

        Object o = ReflexUtils.newInstance(c);
        if (!t.isInstance(o)) {
            throw new RuntimeException(c.getName() + " is not an instance of " + t.getName());
        }
        return t.cast(o);
    }
}