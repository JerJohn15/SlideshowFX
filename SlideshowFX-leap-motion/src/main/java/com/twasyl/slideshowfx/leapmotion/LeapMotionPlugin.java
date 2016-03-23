package com.twasyl.slideshowfx.leapmotion;

import com.twasyl.slideshowfx.leapmotion.controller.SlideshowFXLeapController;
import com.twasyl.slideshowfx.leapmotion.listener.NodeUpdaterLeapListener;
import com.twasyl.slideshowfx.plugin.AbstractPlugin;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;

import java.util.logging.Logger;

/**
 * The plugin allowing the interaction between a presentation and the LeapMotion controller.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0.0
 * @version 1.0
 */
public class LeapMotionPlugin extends AbstractPlugin<LeapMotionOptions> {

    private static final Logger LOGGER = Logger.getLogger(LeapMotionPlugin.class.getName());
    public static final String LEAP_MOTION_NATIVE_LIBRARY_FOLDER_NAME = "Leap";
    private SlideshowFXLeapController leapController;

    public LeapMotionPlugin() throws Exception {
        super("LeapMotion", new LeapMotionOptions());

        this.loadLeapMotionNativeLibraries();
        this.initializeSlideshowFXUiElement();
        this.initializeLeapMotionController();
    }

    /**
     * Initializes the UI element allowing to interact with the LeapMotion in the SlideshowFX UI.
     */
    private void initializeSlideshowFXUiElement() {
        final FontAwesomeIconView graphic = new FontAwesomeIconView(FontAwesomeIcon.HAND_ALT_UP);
        graphic.setGlyphStyle("-fx-fill: app-color-orange");
        graphic.setGlyphSize(20);

        final Tooltip tooltip = new Tooltip("LeapMotion controller");

        final CheckBox checkBox = new CheckBox();
        checkBox.setId("leapMotionEnabled");
        checkBox.setSelected(false);
        checkBox.setGraphic(graphic);
        checkBox.setTooltip(tooltip);
        checkBox.selectedProperty().addListener((value, oldSelected, newSelected) -> {
            this.leapController.setEnabled(newSelected);
        });

        this.slideshowFXUiElement = checkBox;
    }

    /**
     * Loads the necessary LeapMotion native libraries.
     */
    private void loadLeapMotionNativeLibraries() throws Exception {
        System.loadLibrary("Leap");
        System.loadLibrary("LeapJava");
    }

    /**
     * Initializes the LeapMotion controller.
     */
    private void initializeLeapMotionController() {
        this.leapController = new SlideshowFXLeapController();
        this.leapController.addListener(new NodeUpdaterLeapListener((CheckBox) this.slideshowFXUiElement));
    }
}
