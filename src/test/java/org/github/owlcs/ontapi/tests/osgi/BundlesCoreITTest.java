package org.github.owlcs.ontapi.tests.osgi;

import com.github.owlcs.ontapi.DataFactoryImpl;
import com.github.owlcs.ontapi.OntManagers;
import com.github.owlcs.ontapi.OntologyManager;
import com.github.owlcs.ontapi.OntologyModel;
import com.github.owlcs.ontapi.jena.OntModelFactory;
import org.apache.jena.graph.Graph;
import org.github.owlcs.ontapi.utils.Bundles;
import org.github.owlcs.ontapi.utils.ReflectionUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.osgi.framework.BundleException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by @szuev on 19.02.2018.
 */
@RunWith(Parameterized.class)
public class BundlesCoreITTest extends ClassesITTest {

    public BundlesCoreITTest(Tester tester) {
        super(tester);
    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Tester> getTesters() {
        List<Tester> res = Arrays.asList(
                Tester.of(OntologyManager.class)
                , Tester.of(OntologyModel.class)
                , Tester.of(OWLOntologyManager.class)
                , Tester.of(Graph.class)
                , Tester.of(com.github.owlcs.ontapi.jena.model.OntCE.UnaryRestrictionCE.class)
                , Tester.of(com.github.owlcs.ontapi.transforms.SWRLTransform.class)
                , Tester.of(com.github.owlcs.ontapi.internal.ONTObject.class)
                , Tester.of(IRI.class, ClassesITTest::testIRI)
                , Tester.of(OntManagers.class, ClassesITTest::testOntManager)
                , Tester.of(OntModelFactory.class, ClassesITTest::testOntModelFactory)
                , Tester.of(DataFactoryImpl.class, ClassesITTest::testDataFactory)
                , Tester.of(com.clarkparsia.owlapi.explanation.io.ConciseExplanationRenderer.class, ReflectionUtils::newInstance)
                , Tester.of(org.semanticweb.owlapi.rio.RioBinaryRdfStorerFactory.class, ReflectionUtils::newInstance)
                , Tester.of(org.obolibrary.oboformat.model.OBODoc.class, ReflectionUtils::newInstance)
                , Tester.of(org.semanticweb.owlapi.functional.parser.OWLFunctionalSyntaxOWLParser.class, ReflectionUtils::newInstance)
                , Tester.of(org.apache.jena.graph.NodeFactory.class, ReflectionUtils::newInstance)
                , Tester.of(org.tukaani.xz.XZInputStream.class)
                , Tester.of(org.eclipse.rdf4j.model.IRI.class)
                , Tester.of(org.apache.jena.rdf.model.RDFNode.class));
        if ("withDefaultImpl".equals(System.getProperty("profile"))) { // OWL-API-impl is included
            LOGGER.info("The profile 'withDefaultImpl' is specified.");
            res = new ArrayList<>(res);
            res.add(Tester.of(org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNode.class));
        }
        return res;
    }

    @Before
    @Override
    public void setUp() throws BundleException {
        //Bundles.setUp(framework, "com.github.owlcs.ontapi-bundle", () -> Jars.find("com.github.owlcs", "ontapi-osgi"));
        this.context = Bundles.setUp(framework, "com.github.owlcs.ontapi-distribution-bundle", NamedBundleTestBase::getLocalJarPath);
    }

}
