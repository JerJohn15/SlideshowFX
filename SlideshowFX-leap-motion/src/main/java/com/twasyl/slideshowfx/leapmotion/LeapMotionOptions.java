package com.twasyl.slideshowfx.leapmotion;

import com.twasyl.slideshowfx.plugin.IPluginOptions;
import com.twasyl.slideshowfx.utils.PlatformHelper;

import java.beans.PropertyChangeSupport;

/**
 * Options of the LeapMotion plugin.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class LeapMotionOptions implements IPluginOptions {

    protected final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private boolean enabled;

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        final boolean oldValue = this.enabled;
        this.enabled = enabled;
        PlatformHelper.run(() -> this.propertyChangeSupport.firePropertyChange("archiveFile", oldValue, this.enabled));
    }
}
