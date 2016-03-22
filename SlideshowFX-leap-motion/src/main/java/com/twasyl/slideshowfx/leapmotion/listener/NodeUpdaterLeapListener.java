package com.twasyl.slideshowfx.leapmotion.listener;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Listener;
import javafx.scene.control.CheckBox;

import java.util.logging.Logger;

/**
 * This class is in charge of modifying UI elements according the state of the LeapMotion controller.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class NodeUpdaterLeapListener extends Listener {
    private CheckBox checkBox;

    public NodeUpdaterLeapListener(final CheckBox checkBox) {
        super();

        if(checkBox == null) throw new NullPointerException("The checkbox to update can not be null");
        this.checkBox = checkBox;
    }

    @Override
    public void onInit(Controller controller) {
        super.onInit(controller);

        if(controller.isConnected()) {
           this.checkBox.setDisable(false);
           this.checkBox.setSelected(true);
        } else {
            this.checkBox.setDisable(true);
        }
    }

    @Override
    public void onConnect(Controller controller) {
        super.onConnect(controller);
        this.checkBox.setDisable(false);
        // We don't select the checkbox because even if the LeapMotion becomes available, the user may not want
        // to enable it.
    }

    @Override
    public void onDisconnect(Controller controller) {
        super.onDisconnect(controller);
        this.checkBox.setDisable(true);
        this.checkBox.setSelected(false);
    }
}
