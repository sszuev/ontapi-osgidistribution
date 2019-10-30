package org.github.owlcs.ontapi.utils;

import org.apache.felix.framework.FrameworkFactory;
import org.junit.Assert;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.wiring.BundleRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Test utilities to work with bundles.
 * <p>
 * Created by @szuev on 19.02.2018.
 */
public class Bundles {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestBundle.class);

    public static Framework create(Map config) throws BundleException {
        FrameworkFactory factory = new FrameworkFactory();
        Framework res = factory.newFramework(config);
        Assert.assertNotNull("Null framework", res);
        Assert.assertNotEquals("Framework state", res.getState(), Bundle.ACTIVE);
        res.init();
        Assert.assertNotEquals("Framework state", res.getState(), Bundle.ACTIVE);
        return res;
    }

    public static boolean isFragmentBundle(Bundle b) {
        return (b.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0;
    }

    public static Bundle getBundle(BundleContext context, String name) {
        return Arrays.stream(context.getBundles()).filter(b -> name.equals(b.getSymbolicName())).findFirst().orElse(null);
    }

    public static TestBundle setUp(Framework framework, String name, Supplier<URI> location) throws BundleException {
        TestBundle res = new TestBundle(framework, name);
        BundleContext context = framework.getBundleContext();
        Assert.assertNotNull("Null context", context);
        int count = context.getBundles().length;
        Bundle bundle = getBundle(context, name);
        if (bundle != null) {
            LOGGER.info("Bundle <{}> is already installed", name);
        } else {
            count++;
            URI path = location.get();
            LOGGER.info("Install bundle {} fron path <{}>", name, path);
            bundle = context.installBundle(path.toString());
            Assert.assertNotNull("Null bundle", bundle);
            Assert.assertEquals("Not installed", Bundle.INSTALLED, bundle.getState());
            Assert.assertFalse("Fragment", isFragmentBundle(bundle));
            Assert.assertNotNull("No bundle found", context.getBundle(path.toString()));
            LOGGER.debug("Start bundle {}", name);
            bundle.start();
        }
        Assert.assertEquals("Not active", Bundle.ACTIVE, bundle.getState());
        Assert.assertEquals("Wrong bundles count", count, context.getBundles().length);
        return res;
    }
}
