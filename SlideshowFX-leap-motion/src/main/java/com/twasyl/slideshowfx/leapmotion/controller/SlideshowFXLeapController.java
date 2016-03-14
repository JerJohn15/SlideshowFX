package com.twasyl.slideshowfx.leapmotion.controller;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Listener;
import com.twasyl.slideshowfx.app.SlideshowFXState;
import com.twasyl.slideshowfx.leapmotion.listener.SlideshowFXLeapListener;

import java.beans.PropertyChangeListener;

/**
 * An implementation of the {@link Listener} specific to SlideshowFX.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class SlideshowFXLeapController extends Controller {

    private PropertyChangeListener applicationStateListener;
    private SlideshowFXLeapListener leapListener;
    private boolean enabled;

    public SlideshowFXLeapController() {
        this.initializeApplicationStateListener();
        this.leapListener = new SlideshowFXLeapListener(null);

        // TODO LeapMotion : find a way to make this work in OSGi
        //SlideshowFX.addPropertyChangeListener(this.applicationStateListener);
    }

    /**
     * Initialize the listener managing the application's state change. If the application is in the
     * {@link SlideshowFXState#PRESENTING} state, then a {@link SlideshowFXLeapListener} will be registered to this
     * controller. But if the application is in the {@link SlideshowFXState#RUNNING} state, then the listener will be
     * unregistered.
     */
    private void initializeApplicationStateListener() {
        this.applicationStateListener = event -> {
            if("APPLICATION_STATE".equals(event.getPropertyName()) && event.getNewValue() != null) {
                if(event.getNewValue() == SlideshowFXState.PRESENTING && isEnabled()) {
                    this.addListener(this.leapListener);
                } else if(event.getNewValue() == SlideshowFXState.RUNNING) {
                    this.removeListener(this.leapListener);
                }
            }
        };
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.leapListener.setTracking(true);
    }
}
