package org.github.owlcs.ontapi.tests.func;

import com.github.owlcs.ontapi.*;
import com.github.owlcs.ontapi.jena.OntModelFactory;
import com.github.owlcs.ontapi.jena.model.OntModel;
import com.github.owlcs.ontapi.jena.model.OntStatement;
import com.github.owlcs.ontapi.jena.utils.Graphs;
import com.github.owlcs.ontapi.jena.vocabulary.OWL;
import com.github.owlcs.ontapi.jena.vocabulary.RDF;
import org.apache.jena.rdf.model.Resource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.io.OWLParserFactory;
import org.semanticweb.owlapi.io.StreamDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Functional test to verify ontapi distribution.
 * Created by @szuev on 15.02.2018.
 */
public class FunctionalTest {
    private static Logger logger;

    @BeforeClass
    public static void before() {
        logger = LoggerFactory.getLogger(FunctionalTest.class);
    }

    @Test
    public void testGeneralFunctionality() throws Exception {
        String uri = "http://test";
        OntModel g = OntModelFactory.createModel();
        g.setID(uri);
        StringWriter sw = new StringWriter();
        g.write(sw, OntFormat.TURTLE.getID());
        logger.debug("{}", sw);
        Assert.assertEquals(uri, Graphs.getURI(g.getGraph()));

        InputStream in = new ByteArrayInputStream(sw.toString().getBytes(StandardCharsets.UTF_8));
        OntologyManager m = OntManagers.createManager();
        Ontology ont = m.loadOntologyFromOntologyDocument(in);
        Assert.assertEquals(uri, ont.getOntologyID()
                .getOntologyIRI()
                .map(String::valueOf).orElseThrow(AssertionError::new));
        OWLOntology owl = m.getOntology(IRI.create(uri));
        Assert.assertNotNull(owl);

        String clazz = uri + "#class";
        OWLDataFactory df = OntManagers.getDataFactory();
        ont.add(df.getOWLDeclarationAxiom(df.getOWLClass(IRI.create(clazz))));

        Assert.assertEquals(clazz, owl.axioms(AxiomType.DECLARATION)
                .map(OWLDeclarationAxiom::getEntity)
                .map(HasIRI::getIRI)
                .map(String::valueOf)
                .findFirst().orElseThrow(AssertionError::new));
        Assert.assertNotNull(clazz, ont.asGraphModel()
                .statements(null, RDF.type, OWL.Class)
                .map(OntStatement::getSubject)
                .map(Resource::getURI)
                .findFirst().orElseThrow(AssertionError::new));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        owl.saveOntology(out);
        logger.debug("{}", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void testJenaFormatsCompileScope() throws Exception {
        OWLOntologyManager m = OntManagers.createConcurrentONT();
        OWLDataFactory df = m.getOWLDataFactory();
        OWLClass c = df.getOWLClass("http://ex.com#class");
        OWLNamedIndividual i = df.getOWLNamedIndividual("http://ex.com#indi");
        OWLOntology o = m.createOntology();
        o.add(df.getOWLDeclarationAxiom(c));
        o.add(df.getOWLDeclarationAxiom(i));
        o.add(df.getOWLClassAssertionAxiom(c, i));
        o.axioms().forEach(a -> logger.info("{}", a));

        Set<OntFormat> formats = OntFormat.formats()
                .filter(OntFormat::isJena)
                .filter(OntFormat::isWriteSupported).collect(Collectors.toSet());

        Assert.assertEquals(9, formats.size());

        for (OntFormat f : formats) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            o.saveOntology(f.createOwlFormat(), out);
            logger.info("FORMAT: {{}}", f);
            logger.debug("{}", new String(out.toByteArray(), StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testFactoriesTestScope() {
        String loadFactory = "com.github.owlcs.ontapi.OntologyFactoryImpl";
        try {
            Class.forName(loadFactory);
        } catch (ClassNotFoundException e) {
            throw new AssertionError("No " + loadFactory, e);
        }
        List<String> parsers = OWLLangRegistry.parserFactories()
                .map(OWLParserFactory::getClass).map(Class::getName)
                .collect(Collectors.toList());
        logger.debug("Parsers");
        parsers.forEach(logger::debug);
        List<String> storers = OWLLangRegistry.storerFactories()
                .map(OWLStorerFactory::getClass).map(Class::getName)
                .collect(Collectors.toList());
        logger.debug("Storers");
        storers.forEach(logger::debug);

        // scope 'test'
        Assert.assertEquals("Wrong parsers count: " + parsers, 19, parsers.size());
        Assert.assertEquals("Wrong storers count: " + storers, 20, storers.size());
    }

    @Test
    public void testOWLFormatsTestScope() throws Exception {
        OWLOntologyManager m = OntManagers.createManager();
        OWLDataFactory df = m.getOWLDataFactory();
        OWLObjectProperty p = df.getOWLObjectProperty("http://ex.com#obj-prop");
        OWLNamedIndividual i1 = df.getOWLNamedIndividual("http://ex.com#indi-1");
        OWLNamedIndividual i2 = df.getOWLNamedIndividual("http://ex.com#indi-2");
        OWLOntology o = m.createOntology(IRI.create("http://ex.com"));
        o.add(df.getOWLDeclarationAxiom(p));
        o.add(df.getOWLDeclarationAxiom(i1));
        o.add(df.getOWLObjectPropertyAssertionAxiom(p, i1, i2));
        o.axioms().forEach(a -> logger.info("{}", a));

        List<OWLLangRegistry.OWLLang> formats = OntFormat.formats()
                .flatMap(OntFormat::owlLangs)
                .filter(OWLLangRegistry.OWLLang::isWritable)
                .collect(Collectors.toList());

        formats.forEach(f -> logger.debug("{}:", f));

        Assert.assertEquals("Wrong formats count", 20, formats.size());

        for (OWLLangRegistry.OWLLang lang : formats) {
            OWLStorerFactory factory = lang.getStorerFactory();
            OWLStorer storer = factory.createStorer();
            OWLDocumentFormat format = lang.getFormatFactory().get();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            logger.info("FORMAT: {{}-{}:'{}'}",
                    factory.getClass().getSimpleName(), format.getClass().getSimpleName(), format.getKey());
            storer.storeOntology(o, new StreamDocumentTarget(out), format);
            logger.debug("{}", new String(out.toByteArray(), StandardCharsets.UTF_8));
        }
    }
}
