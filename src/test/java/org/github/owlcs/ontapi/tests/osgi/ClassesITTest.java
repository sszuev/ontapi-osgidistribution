package org.github.owlcs.ontapi.tests.osgi;

import com.github.owlcs.ontapi.DataFactoryImpl;
import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.google.common.collect.LinkedListMultimap;
import org.github.owlcs.ontapi.utils.ReflectionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by @szuev on 19.02.2018.
 */
@SuppressWarnings("WeakerAccess")
public abstract class ClassesITTest extends NamedBundleTestBase {
    static final Logger LOGGER = LoggerFactory.getLogger(ClassesITTest.class);

    private final Tester<?> tester;

    public ClassesITTest(Tester<?> tester) {
        this.tester = tester;
    }

    @Test
    public void testClassFunc() {
        Class<?> classToTest = tester.clazz;
        try {
            LOGGER.info("To test: {}", classToTest);

            Bundle bundle = getBundle();
            Assert.assertNotNull(bundle);
            Class<?> classFromBundle = bundle.loadClass(classToTest.getName());
            Assert.assertNotNull("No " + classToTest + " found.", classFromBundle);
            // different class loaders
            Assert.assertNotEquals("Classes expected to be from different class-loaders", classToTest, classFromBundle);
            tester.test(classFromBundle);
            LOGGER.info("OK: {}", classToTest);
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable e) {
            throw new AssertionError("Has a problem with bundle: <" + e.getMessage() + ">, " +
                    "class to test: " + classToTest.getName(), e);
        }
    }

    public static void testOntModelFactory(Class<OntModelFactory> c) {
        ReflectionUtils.invokeStaticMethod(c, "createModel");
    }

    public static void testOntManager(Class<OntManagers> c) {
        ReflectionUtils.newInstance(c);
        Stream.of("createManager", "createConcurrentManager", "getDataFactory")
                .forEach(name -> ReflectionUtils.invokeStaticMethod(c, name));
    }

    public static void testDataFactory(Class<DataFactoryImpl> c) {
        Object df = ReflectionUtils.newInstance(c);
        Object i = ReflectionUtils.invokeMethod(c, df, "getOWLAnonymousIndividual", LinkedListMultimap.create());
        Assert.assertNotNull(i);
        String s = i.toString();
        LOGGER.info("Anon individual {}", s);
    }

    public static void testIRI(Class<IRI> c) {
        LinkedListMultimap<Class<?>, Object> params = LinkedListMultimap.create();
        params.put(String.class, "http://the-uri.net");
        ReflectionUtils.invokeStaticMethod(c, "create", params);
    }


    public static class Tester<C> {
        private final Class<C> clazz;
        private final Consumer<Class<C>> action;

        Tester(Class<C> clazz, Consumer<Class<C>> action) {
            this.clazz = clazz;
            this.action = action;
        }

        @Override
        public String toString() {
            return clazz.getName();
        }

        @SuppressWarnings("unchecked")
        void test(Class<?> other) {
            if (action == null) return;
            LOGGER.info("Test <{}>", other);
            action.accept((Class<C>) other);
        }

        public static <T> Tester<T> of(Class<T> clazz, Consumer<Class<T>> action) {
            return new Tester<>(clazz, action);
        }

        public static <X> Tester<X> of(Class<X> clazz) {
            return new Tester<>(clazz, null);
        }
    }

}
