package org.github.owlcs.ontapi.tests.osgi;

import org.github.owlcs.ontapi.utils.Bundles;
import org.github.owlcs.ontapi.utils.Jars;
import org.github.owlcs.ontapi.utils.TestBundle;
import org.junit.Assert;
import org.junit.Before;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by @szuev on 19.02.2018.
 */
@SuppressWarnings("WeakerAccess")
public class NamedBundleTestBase extends AbstractBundleTestBase {

    protected TestBundle context;

    @Before
    public void setUp() throws BundleException {
        this.context = Bundles.setUp(framework, "ru.avicomp.ontapi-bundle", NamedBundleTestBase::getLocalJarPath);
    }

    public static URI getLocalJarPath() {
        Path target = Paths.get("target");
        Assert.assertTrue("Can't find target directory", Files.exists(target));
        return Jars.find(target);
    }

    public Bundle getBundle() {
        return context.getBundle();
    }

    public BundleContext getContext() {
        return context.getContext();
    }
}
