package com.twasyl.slideshowfx.leapmotion;

import com.sun.javafx.PlatformUtil;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.leapmotion.activator.LeapMotionActivator;
import com.twasyl.slideshowfx.leapmotion.controller.SlideshowFXLeapController;
import com.twasyl.slideshowfx.leapmotion.listener.NodeUpdaterLeapListener;
import com.twasyl.slideshowfx.plugin.AbstractPlugin;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tooltip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
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

        // this.extractLibraries(GlobalConfiguration.getNativeLibrariesFolder());
        this.redefineJavaLibraryPath(GlobalConfiguration.getNativeLibrariesFolder());
        this.initializeSlideshowFXUiElement();
        this.initializeLeapMotionController();
    }

    /**
     * Extract the native LeapMotion libraries in a subfolder named {@code Leap} within the given folder.
     * @param inFolder The folder were a subfolder will be created with the native libraries.
     * @throws Exception If something went wrong extracting the native libraries
     */
    private void extractLibraries(final File inFolder) throws Exception {
        if(!inFolder.exists() && !inFolder.mkdirs()) {
            throw new IOException("The native libraries folder doesn't exist and can't be created");
        }

        // Trick to get the app JAR file
        final File bundleJar = new File(LeapMotionActivator.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

        final File leapFolder = new File(inFolder, LEAP_MOTION_NATIVE_LIBRARY_FOLDER_NAME);
        if(!leapFolder.exists() && !leapFolder.mkdir()) {
            throw new IOException("Can not create LeapMotion native library folder");
        }

        final String internalResourcesFolder = getInternalResourcesFolder();
        final String resourceRootPath = "com/twasyl/slideshowfx/leapmotion/" + internalResourcesFolder + "/";

        final JarFile jar = new JarFile(bundleJar);

        final Enumeration<JarEntry> entries = jar.entries();
        while(entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();

            if(!entry.isDirectory() && entry.getName().startsWith(resourceRootPath)) {
                final File destinationFile = new File(leapFolder, this.getFileName(entry));

                try(final InputStream input = jar.getInputStream(entry);
                    final FileOutputStream output = new FileOutputStream(destinationFile)) {

                    final byte[] buffer = new byte[1024];
                    int bytesRead;

                    while((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }

                    output.flush();
                }
            }
        }
    }

    /**
     * Get the folder in which the LeapMotion native libraries are stored within the JAR.
     * @return The folder containing LeapMotion's native libraries.
     * @throws Exception If something went wrong determining the LeapMotion's native libraries.
     */
    private String getInternalResourcesFolder() throws Exception {
        if(PlatformUtil.isMac()) return "osx";
        else {
            final String architecture = getSystemArchitecture();
            final String os;

            if(PlatformUtil.isWindows()) os = "windows";
            else if(PlatformUtil.isUnix()) os = "linux";
            else throw new Exception("Unknown platform");

            return String.format("%1$s_x%2$s", os, architecture);
        }
    }

    /**
     * Get the architecture of the system the application is running on.
     * @return The value of the property "sun.arch.data.model".
     */
    private String getSystemArchitecture() {
        return System.getProperty("sun.arch.data.model");
    }

    /**
     * Extracts the file name of the given {@link JarEntry entry}.
     * @param entry The entry to extract the file name for.
     * @return The name of the file.
     */
    private String getFileName(final JarEntry entry) {
        final int slashIndex = entry.getName().lastIndexOf('/');

        if(slashIndex == -1) {
            return entry.getName();
        } else if(slashIndex < entry.getName().length() - 1) {
            return entry.getName().substring(slashIndex);
        } else {
            return null;
        }
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
     * Redefine the {@code java.library.path} to add the LeapMotion native libraries folder to it.
     * @param rootFolder The root folder where the folder containing the LeapMotion native libraries are stored.
     */
    private void redefineJavaLibraryPath(final File rootFolder) throws Exception {
        /* final File leapDir = new File(rootFolder, LEAP_MOTION_NATIVE_LIBRARY_FOLDER_NAME);

        final Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        usrPathsField.setAccessible(true);

        final String[] currentLibraryPath = (String[]) usrPathsField.get(null);

        final String[] newLibraryPath = Arrays.copyOf(currentLibraryPath, currentLibraryPath.length + 1);
        newLibraryPath[newLibraryPath.length - 1] = leapDir.getAbsolutePath();
        usrPathsField.set(null, newLibraryPath); */

        System.loadLibrary("libLeap");
        System.loadLibrary("libLeapJava");
    }

    /**
     * Initializes the LeapMotion controller.
     */
    private void initializeLeapMotionController() {
        this.leapController = new SlideshowFXLeapController();
        this.leapController.addListener(new NodeUpdaterLeapListener((CheckBox) this.slideshowFXUiElement));
    }
}
