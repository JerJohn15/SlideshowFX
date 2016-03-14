package com.twasyl.slideshowfx.leapmotion.activator;

import com.twasyl.slideshowfx.leapmotion.LeapMotionPlugin;
import com.twasyl.slideshowfx.plugin.IPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;

/**
 * Activator starting the {@link LeapMotionPlugin}.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class LeapMotionActivator implements BundleActivator {

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Hashtable<String, String> props = new Hashtable<>();
        bundleContext.registerService(IPlugin.class.getName(), new LeapMotionPlugin(), props);
    }

    @Override
    public void stop(BundleContext context) throws Exception {

    }

}