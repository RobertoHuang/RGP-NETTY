/**
 * FileName: CustomSerializerManager
 * Author:   HuangTaiHong
 * Date:     2019/1/5 11:11
 * Description: Manage the custom serializer according to the class name.
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package roberto.group.process.netty.practice.serialize.custom;

import roberto.group.process.netty.practice.command.code.RemoteCommandCode;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 〈一句话功能简述〉<br>
 * 〈Manage the custom serializer according to the class name.〉
 *
 * @author HuangTaiHong
 * @create 2019/1/5
 * @since 1.0.0
 */
public class CustomSerializerManager {
    /** For RPC Key:class name */
    private static ConcurrentHashMap<String, CustomSerializer> classCustomSerializer = new ConcurrentHashMap();

    /** For user defined command Key:RemoteCommandCode */
    private static ConcurrentHashMap<RemoteCommandCode, CustomSerializer> commandCustomSerializer = new ConcurrentHashMap();

    public static void clear() {
        classCustomSerializer.clear();
        commandCustomSerializer.clear();
    }

    public static CustomSerializer getCustomSerializer(String className) {
        if (!classCustomSerializer.isEmpty()) {
            return classCustomSerializer.get(className);
        }
        return null;
    }

    public static void registerCustomSerializer(String className, CustomSerializer serializer) {
        CustomSerializer prevSerializer = classCustomSerializer.putIfAbsent(className, serializer);
        if (prevSerializer != null) {
            throw new RuntimeException("CustomSerializer has been registered for class: " + className + ", the custom serializer is: " + prevSerializer.getClass().getName());
        }
    }


    public static CustomSerializer getCustomSerializer(RemoteCommandCode remoteCommandCode) {
        if (!commandCustomSerializer.isEmpty()) {
            return commandCustomSerializer.get(remoteCommandCode);
        }
        return null;
    }

    public static void registerCustomSerializer(RemoteCommandCode remoteCommandCode, CustomSerializer serializer) {
        CustomSerializer prevSerializer = commandCustomSerializer.putIfAbsent(remoteCommandCode, serializer);
        if (prevSerializer != null) {
            throw new RuntimeException("CustomSerializer has been registered for command code: " + remoteCommandCode + ", the custom serializer is: " + prevSerializer.getClass().getName());
        }
    }
}