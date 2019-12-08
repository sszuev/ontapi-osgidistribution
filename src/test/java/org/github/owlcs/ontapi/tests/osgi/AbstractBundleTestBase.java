package org.github.owlcs.ontapi.tests.osgi;

import org.github.owlcs.ontapi.utils.Bundles;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Properties;

/**
 * Created by @szuev on 19.02.2018.
 */
@SuppressWarnings("WeakerAccess")
public abstract class AbstractBundleTestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBundleTestBase.class);

    protected static Framework framework;
    protected static Map<?, ?> config = loadFelixConfig();

    @BeforeClass
    public static void prepare() throws BundleException {
        config.forEach((k, v) -> LOGGER.debug("{}\t=>\t{}", k, v));
        framework = Bundles.create(config);
        LOGGER.info("Start framework");
        framework.start();
        Assert.assertEquals("Not started", framework.getState(), Bundle.ACTIVE);
    }

    @AfterClass
    public static void after() throws BundleException, InterruptedException {
        LOGGER.info("Stop framework");
        framework.stop();
        framework.waitForStop(0);
    }

    public static Map<?, ?> loadFelixConfig() {
        Properties res = new Properties();
        try (InputStream in = AbstractBundleTestBase.class.getResourceAsStream("/felix.properties")) {
            res.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to load felix config.", e);
        }
        return res;
    }
}
