package org.github.owlcs.ontapi.tests.osgi;

import org.junit.Before;
import org.junit.runners.Parameterized;
import org.osgi.framework.BundleException;
import ru.avicomp.ontapi.tests.osgi.ClassesITTest;
import ru.avicomp.ontapi.tests.osgi.NamedBundleTestBase;
import ru.avicomp.ontapi.utils.Bundles;
import ru.avicomp.ontapi.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by @szuev on 19.02.2018.
 */
public class BundlesCoreITTest extends ClassesITTest {


    @Parameterized.Parameters(name = "{0}")
    public static List<Tester> getTesters() {
        List<Tester> testers = new ArrayList<>(ClassesITTest.getTesters());
        testers.add(Tester.of(com.clarkparsia.owlapi.explanation.io.ConciseExplanationRenderer.class, ReflectionUtils::newInstance));
        testers.add(Tester.of(org.semanticweb.owlapi.rio.RioBinaryRdfStorerFactory.class, ReflectionUtils::newInstance));
        testers.add(Tester.of(org.obolibrary.oboformat.model.OBODoc.class, ReflectionUtils::newInstance));
        testers.add(Tester.of(org.semanticweb.owlapi.functional.parser.OWLFunctionalSyntaxOWLParser.class, ReflectionUtils::newInstance));
        testers.add(Tester.of(org.apache.jena.graph.NodeFactory.class, ReflectionUtils::newInstance));
        testers.add(Tester.of(org.tukaani.xz.XZInputStream.class));
        testers.add(Tester.of(org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNode.class));
        testers.add(Tester.of(org.eclipse.rdf4j.model.IRI.class));
        testers.add(Tester.of(org.apache.jena.rdf.model.RDFNode.class));
        return testers;
    }

    @Before
    @Override
    public void setUp() throws BundleException {
        //Bundles.setUp(framework, "ru.avicomp.ontapi-bundle", () -> Jars.find("ru.avicomp", "ontapi-osgi"));
        this.context = Bundles.setUp(framework, "ru.avicomp.owlapi-distribution-bundle", NamedBundleTestBase::getLocalJarPath);
    }

    public BundlesCoreITTest(Tester tester) {
        super(tester);
    }

}
