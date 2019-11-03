package org.github.owlcs.ontapi.utils;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;

/**
 * Created by @szuev on 19.02.2018.
 */
@SuppressWarnings("WeakerAccess")
public class TestBundle {

    private final Framework framework;
    private final String name;

    protected TestBundle(Framework framework, String name) {
        this.name = name;
        this.framework = framework;
    }

    public Bundle getBundle() {
        return Bundles.getBundle(getContext(), name);
    }

    public BundleContext getContext() {
        return framework.getBundleContext();
    }

}
