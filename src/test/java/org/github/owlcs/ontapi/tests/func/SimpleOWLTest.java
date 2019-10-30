package org.github.owlcs.ontapi.tests.func;

import com.github.owlcs.ontapi.OntManagers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.semanticweb.owlapi.formats.RioTurtleDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Functional test to verify common ontapi and owlapi-impl functionality.
 * <p>
 * Created by @szuev on 15.02.2018.
 */
@RunWith(Parameterized.class)
public class SimpleOWLTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleOWLTest.class);

    private final TestFactory factory;

    public SimpleOWLTest(TestFactory factory) {
        this.factory = factory;
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<TestFactory> getData() {
        return Arrays.stream(TestFactory.values()).collect(Collectors.toList());
    }

    @Test
    public void validateGeneralFunctionalityTest() throws Exception {
        OWLOntologyManager m = factory.create();
        OWLDataFactory df = m.getOWLDataFactory();
        OWLClass c = df.getOWLClass("http://ex.com#class");
        OWLNamedIndividual i = df.getOWLNamedIndividual("http://ex.com#indi");
        OWLObjectProperty p = df.getOWLObjectProperty("http://ex.com#obj-prop");
        OWLNamedIndividual i1 = df.getOWLNamedIndividual("http://ex.com#indi-1");
        OWLNamedIndividual i2 = df.getOWLNamedIndividual("http://ex.com#indi-2");

        OWLOntology o = m.createOntology(IRI.create("http://ex.com#"));

        o.add(df.getOWLDeclarationAxiom(c));
        o.add(df.getOWLDeclarationAxiom(i));
        o.add(df.getOWLClassAssertionAxiom(c, i));
        o.add(df.getOWLDeclarationAxiom(p));
        o.add(df.getOWLDeclarationAxiom(i1));
        o.add(df.getOWLObjectPropertyAssertionAxiom(p, i1, i2));

        List<OWLDocumentFormat> formats = factory.isONT() ?
                Collections.singletonList(new TurtleDocumentFormat()) :
                Arrays.asList(new TurtleDocumentFormat(), new RioTurtleDocumentFormat());
        for (OWLDocumentFormat format : formats) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            o.saveOntology(format, out);
            LOGGER.debug("{}", new String(out.toByteArray(), StandardCharsets.UTF_8));
        }
    }

    private enum TestFactory {
        ONT(OntManagers::createONT),
        ONT_CONCUR(OntManagers::createConcurrentONT),
        OWL(OntManagers::createOWL),
        OWL_CONCUR(OntManagers::createConcurrentOWL),
        ;
        private final Supplier<OWLOntologyManager> delegate;

        TestFactory(Supplier<OWLOntologyManager> delegate) {
            this.delegate = delegate;
        }

        public OWLOntologyManager create() {
            return delegate.get();
        }

        public boolean isONT() {
            return equals(ONT) || equals(ONT_CONCUR);
        }

    }
}
