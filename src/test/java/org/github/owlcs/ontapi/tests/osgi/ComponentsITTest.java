package org.github.owlcs.ontapi.tests.osgi;

import org.github.owlcs.ontapi.utils.Jars;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by @szuev on 19.02.2018.
 */
@SuppressWarnings("WeakerAccess")
public class ComponentsITTest extends NamedBundleTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentsITTest.class);

    public static List<String> getLibs() {
        return Jars.getJarEntries(Paths.get(getLocalJarPath()), n -> n.startsWith("lib/") && n.endsWith(".jar"));
    }

    @Test
    public void testListComponents() {
        List<String> libs = getLibs();
        libs.forEach(x -> LOGGER.debug("Found lib: {}", x));
        String s = String.join(",", libs);
        LOGGER.info("All : {}", s);
        Assert.assertEquals(42, libs.size());
    }

    @Test
    public void testInstallComponents() throws BundleException {
        BundleContext context = getContext();
        Bundle main = getBundle();

        Assert.assertNotNull(main);
        List<String> libs = getLibs();
        for (String lib : libs) {
            URL url = main.getResource(lib);
            testLib(context, url.toString());
        }
    }

    private static void testLib(BundleContext context, String location) throws BundleException {
        LOGGER.info("Test file <{}>", location);
        Bundle bundle = context.installBundle(location);
        Assert.assertNotNull("Null bundle for <" + location + ">", bundle);
        try {
            bundle.start();
            LOGGER.info("OK: <{}>", location);
        } catch (BundleException b) {
            LOGGER.warn("ERROR: <{}>: '{}'", location, b.getMessage());
        }
    }
}
