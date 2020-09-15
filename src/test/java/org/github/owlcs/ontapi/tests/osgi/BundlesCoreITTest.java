package org.github.owlcs.ontapi.tests.osgi;

import com.google.common.collect.LinkedListMultimap;
import org.github.owlcs.ontapi.utils.Bundles;
import org.github.owlcs.ontapi.utils.ReflectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * To test some functionality inside a bundle (using reflection).
 * Created by @szuev on 19.02.2018.
 */
@RunWith(Parameterized.class)
public class BundlesCoreITTest extends NamedBundleTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(BundlesCoreITTest.class);

    @Parameterized.Parameter
    public Tester<?> tester;

    @Parameterized.Parameters(name = "{0}")
    public static List<Tester<?>> getTesters() {
        List<Tester<?>> res = Arrays.asList(
                Tester.of(com.github.owlcs.ontapi.OntologyManager.class)
                , Tester.of(com.github.owlcs.ontapi.Ontology.class)
                , Tester.of(org.semanticweb.owlapi.model.OWLOntologyManager.class)
                , Tester.of(org.apache.jena.graph.Graph.class)
                , Tester.of(com.github.owlcs.ontapi.jena.model.OntClass.UnaryRestrictionCE.class)
                , Tester.of(com.github.owlcs.ontapi.transforms.SWRLTransform.class)
                , Tester.of(com.github.owlcs.ontapi.internal.ONTObject.class)
                , Tester.of(org.semanticweb.owlapi.model.IRI.class, BundlesCoreITTest::testIRI)
                , Tester.of(com.github.owlcs.ontapi.OntManagers.class, BundlesCoreITTest::testOntManager)
                , Tester.of(com.github.owlcs.ontapi.jena.OntModelFactory.class, BundlesCoreITTest::testOntModelFactory)
                , Tester.of(com.github.owlcs.ontapi.DataFactoryImpl.class, BundlesCoreITTest::testDataFactory)
                , Tester.of(com.clarkparsia.owlapi.explanation.io.ConciseExplanationRenderer.class, ReflectionUtils::newInstance)
                , Tester.of(org.semanticweb.owlapi.rio.RioBinaryRdfStorerFactory.class, ReflectionUtils::newInstance)
                , Tester.of(org.obolibrary.oboformat.model.OBODoc.class, ReflectionUtils::newInstance)
                , Tester.of(org.semanticweb.owlapi.functional.parser.OWLFunctionalSyntaxOWLParser.class, ReflectionUtils::newInstance)
                , Tester.of(org.apache.jena.graph.NodeFactory.class, ReflectionUtils::newInstance)
                , Tester.of(org.tukaani.xz.XZInputStream.class)
                , Tester.of(org.eclipse.rdf4j.model.IRI.class)
                , Tester.of(org.apache.jena.rdf.model.RDFNode.class)
                , Tester.of(org.apache.jena.riot.lang.BlankNodeAllocatorHash.class, BundlesCoreITTest::testBlankNodeAllocator));
        if ("withDefaultImpl".equals(System.getProperty("profile"))) { // OWL-API-impl is included
            LOGGER.info("The profile 'withDefaultImpl' is specified.");
            res = new ArrayList<>(res);
            res.add(Tester.of(org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNode.class));
        }
        return res;
    }

    public static void testOntModelFactory(Class<com.github.owlcs.ontapi.jena.OntModelFactory> c) {
        Object res = ReflectionUtils.invokeStaticMethod(c, "createModel");
        Assert.assertNotNull(res);
        String expected = com.github.owlcs.ontapi.jena.model.OntModel.class.getName();
        Assert.assertTrue(Arrays.stream(res.getClass().getInterfaces()).map(Class::getName).anyMatch(expected::equals));
    }

    public static void testOntManager(Class<com.github.owlcs.ontapi.OntManagers> clazz) {
        ReflectionUtils.newInstance(clazz);
        Stream.of("createManager", "createConcurrentManager", "getDataFactory")
                .forEach(x -> Assert.assertNotNull(ReflectionUtils.invokeStaticMethod(clazz, x)));
    }

    public static void testDataFactory(Class<com.github.owlcs.ontapi.DataFactoryImpl> clazz) {
        Object factory = ReflectionUtils.newInstance(clazz);
        Object individual = ReflectionUtils.invokeMethod(clazz, factory, "getOWLAnonymousIndividual", LinkedListMultimap.create());
        Assert.assertNotNull(individual);
        String s = individual.toString();
        Assert.assertTrue("Wrong label for " + s, s.startsWith("_:"));
    }

    public static void testIRI(Class<org.semanticweb.owlapi.model.IRI> clazz) {
        LinkedListMultimap<Class<?>, Object> params = LinkedListMultimap.create();
        params.put(String.class, "http://the-uri.net");
        Object res = ReflectionUtils.invokeStaticMethod(clazz, "create", params);
        Assert.assertSame(clazz, res.getClass());
    }

    public static void testBlankNodeAllocator(Class<? extends org.apache.jena.riot.lang.BlankNodeAllocator> clazz) {
        Object instance = ReflectionUtils.newInstance(clazz);
        Object res = ReflectionUtils.invokeMethod(clazz, instance, "create", LinkedListMultimap.create());
        Assert.assertNotNull(res);
        Assert.assertEquals(org.apache.jena.graph.Node_Blank.class.getName(), res.getClass().getName());
    }

    @Before
    @Override
    public void setUp() throws BundleException {
        //Bundles.setUp(framework, "com.github.owlcs.ontapi-bundle", () -> Jars.find("com.github.owlcs", "ontapi-osgi"));
        this.context = Bundles.setUp(framework, "com.github.owlcs.ontapi-distribution-bundle", NamedBundleTestBase::getLocalJarPath);
    }

    @Test
    public void testBundleClassFunctionality() {
        Class<?> given = tester.clazz;
        try {
            LOGGER.info("To test: {}", given);
            Bundle bundle = getBundle();
            Assert.assertNotNull(bundle);
            Class<?> actual = bundle.loadClass(given.getName());
            Assert.assertNotNull("No " + given + " is found.", actual);
            // different class loaders
            Assert.assertNotEquals("Classes expected to be from different class-loaders", given, actual);
            tester.performTest(actual);
            LOGGER.info("OK: {}", given);
        } catch (AssertionError e) {
            throw e;
        } catch (Throwable e) {
            throw new AssertionError("Has a problem with the bundle: <" + e.getMessage() + ">, " +
                    "class to test: " + given.getName(), e);
        }
    }

    public static class Tester<X> {
        private final Class<X> clazz;
        private final Consumer<Class<X>> action;

        Tester(Class<X> clazz, Consumer<Class<X>> action) {
            this.clazz = clazz;
            this.action = action;
        }

        public static <T> Tester<T> of(Class<T> clazz, Consumer<Class<T>> action) {
            return new Tester<>(clazz, action);
        }

        public static <X> Tester<X> of(Class<X> clazz) {
            return new Tester<>(clazz, null);
        }

        @Override
        public String toString() {
            return clazz.getName();
        }

        @SuppressWarnings("unchecked")
        void performTest(Class<?> other) {
            Assert.assertEquals(clazz.getName(), other.getName());
            if (action == null) return;
            LOGGER.info("Perform test for <{}>", other);
            action.accept((Class<X>) other);
        }
    }
}
