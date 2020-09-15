package org.github.owlcs.ontapi.utils;

import com.google.common.collect.LinkedListMultimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.stream.Collectors;

/**
 * Helper to work with reflection.
 * Created by @ssz on 15.09.2018.
 */
public class ReflectionUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

    public static Object newInstance(Class<?> c) {
        try {
            return c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AssertionError(c.getName() + "#newInstance() failed:", e);
        }
    }

    public static Object invokeStaticMethod(Class<?> clazz,
                                            String methodName) {
        return invokeStaticMethod(clazz, methodName, LinkedListMultimap.create());
    }

    public static Object invokeStaticMethod(Class<?> clazz,
                                            String methodName,
                                            LinkedListMultimap<Class<?>, Object> params) {
        return invokeMethod(clazz, null, methodName, params);
    }

    /**
     * Finds and invokes the specified method.
     *
     * @param classType      Class type
     * @param instanceObject instance of {@code clazz}
     * @param methodName     String method
     * @param params         {@link LinkedListMultimap} of params
     * @return Object, result of method
     */
    public static Object invokeMethod(Class<?> classType,
                                      Object instanceObject,
                                      String methodName,
                                      LinkedListMultimap<Class<?>, Object> params) {
        String name = MessageFormat.format("{0}#{1}({2})", classType.getName(), methodName,
                params.keySet().stream().map(Class::getSimpleName).collect(Collectors.joining(", ")));
        LOGGER.info("Test method <{}>", name);
        Method method;
        try {
            method = classType.getMethod(methodName, params.keySet().toArray(new Class[0]));
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Can't find " + name, e);
        }
        try {
            Object res = method.invoke(instanceObject, params.values().toArray());
            LOGGER.info("{}: {}", name, res);
            return res;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new AssertionError("Can't invoke " + name, e);
        }
    }
}
