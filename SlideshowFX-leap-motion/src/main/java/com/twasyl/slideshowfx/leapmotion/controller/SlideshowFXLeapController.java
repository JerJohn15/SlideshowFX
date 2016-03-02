package com.twasyl.slideshowfx.leapmotion.controller;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Listener;

/**
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class SlideshowFXLeapController extends Controller {

    public SlideshowFXLeapController() {
        this.addListener(new Listener() {
            @Override
            public void onInit(Controller controller) {
                if(!controller.isConnected()) {
                    SlideshowFXController.this.leapMotionEnabled.setDisable(true);
                } else {
                    SlideshowFXController.this.leapMotionEnabled.setDisable(false);
                    SlideshowFXController.this.leapMotionEnabled.setSelected(true);
                }

            }

            @Override
            public void onConnect(Controller controller) {
                SlideshowFXController.this.leapMotionEnabled.setDisable(false);
                // We don't select the checkbox because even if the LeapMotion becom available, the user may not want
                // to enable it.
            }

            @Override
            public void onDisconnect(Controller controller) {
                SlideshowFXController.this.leapMotionEnabled.setDisable(true);
                SlideshowFXController.this.leapMotionEnabled.setSelected(false);
            }
        });
    }
}
