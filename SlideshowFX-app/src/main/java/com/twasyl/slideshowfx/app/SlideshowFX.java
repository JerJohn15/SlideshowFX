package com.twasyl.slideshowfx.app;

import com.sun.javafx.PlatformUtil;
import com.twasyl.slideshowfx.controllers.SlideshowFXController;
import com.twasyl.slideshowfx.engine.presentation.PresentationEngine;
import com.twasyl.slideshowfx.engine.template.TemplateEngine;
import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.hosting.connector.IHostingConnector;
import com.twasyl.slideshowfx.osgi.OSGiManager;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.utils.io.DeleteFileVisitor;
import com.twasyl.slideshowfx.utils.time.DateTimeUtils;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JavaFX application class to launch SlideshowFX.
 *
 * @author Thierry Wasylczenko
 * @version 1.1
 * @since SlideshowFX 1.0
 */
public class SlideshowFX extends Application {

    static {
        /*
         * Set the path of LeapMotion libraries when the application is packaged. This is mostly useful when the app is
         * bundled as a Mac Bundle.
         *
         * We first look for a custom property "dynamic.java.library.path". If it is set to true, the hack will be performed.
         * This property is useful to be sure the app is working inside an IDE and in production. Indeed, the custom
         * property should only be used when the application is packaged.
         */
        final String defineDynamicJavaLibraryPath = System.getProperty("dynamic.java.library.path");

        if("true".equals(defineDynamicJavaLibraryPath)) {
            /*
             * We then look for a custom property "project.stage" in order to locate the folder containing the LeapMotion
             * native libraries. Currently the only values that are considered as valid for the property are "test" and
             * "development". Currently both values have the same result.
             *
             * If the property has a valid value, then the parent folder containing the LeapMotion libraries is considered
             * to be the "lib" folder at the root of the SlideshowFX project.
             * If the property has an invalid value, then the parent folder containing the LeapMotion libraries is
             * considered to be the one containing the SlideshowFX application JAR file.
             */
            try {
                File parentFolder;
                final String projectStage = System.getProperty("project.stage");

                if("test".equals(projectStage) || "development".equals(projectStage)) {
                    parentFolder = new File("lib");
                } else {
                    // Trick to get the app JAR file
                    final File appJarFile = new File(SlideshowFX.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                    parentFolder = appJarFile.getParentFile();
                }


                String platform = "";
                if(PlatformUtil.isMac()) platform = "osx";

                else if(PlatformUtil.isWindows()) {
                    if("64".equals(System.getProperty("sun.arch.data.model"))) platform = "windows_x64";
                    if("86".equals(System.getProperty("sun.arch.data.model"))) platform = "windows_x86";
                }

                else if(PlatformUtil.isLinux() || PlatformUtil.isUnix()) {
                    if("64".equals(System.getProperty("sun.arch.data.model"))) platform = "linux_x64";
                    if("86".equals(System.getProperty("sun.arch.data.model"))) platform = "linux_x86";
                }

                /*
                 * Once we know where the JAR is, we assume the libraries are located next to it in a "Leap" folder and then
                 * in a subfolder for each platform architecture.
                 */
                System.setProperty("java.library.path", new File(parentFolder, "Leap/" + platform).getAbsolutePath());

                Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");

                if (fieldSysPath != null) {
                    fieldSysPath.setAccessible(true);
                    fieldSysPath.set(null, null);
                }
            } catch (URISyntaxException | IllegalAccessException | NoSuchFieldException e) {
                Logger.getLogger(SlideshowFX.class.getName()).severe("Impossible to set java.library.path for LeapMotion");
            }
        }
    }

    private static final Logger LOGGER = Logger.getLogger(SlideshowFX.class.getName());
    private static final String PRESENTATION_ARGUMENT_PREFIX = "presentation";
    private static final String TEMPLATE_ARGUMENT_PREFIX = "template";

    private static final ReadOnlyObjectProperty<Stage> stage = new SimpleObjectProperty<>();
    private static final ReadOnlyObjectProperty<Scene> presentationBuilderScene = new SimpleObjectProperty<>();

    private final ReadOnlyObjectProperty<SlideshowFXController> mainController = new SimpleObjectProperty<>();
    private Set<File> filesToOpen;

    @Override
    public void init() throws Exception {
        // Initialize the configuration
        GlobalConfiguration.createApplicationDirectory();

        if(GlobalConfiguration.createConfigurationFile()) {
           GlobalConfiguration.fillConfigurationWithDefaultValue();
        } else {
            GlobalConfiguration.fillConfigurationWithDefaultValue();
        }

        // Start the MarkupManager
        LOGGER.info("Starting Felix");
        OSGiManager.getInstance().startAndDeploy();

        // Retrieve the files to open at startup
        final Map<String, String> params = getParameters().getNamed();
        if(params != null && !params.isEmpty()) {
            this.filesToOpen = new HashSet<>();

            // Only files that exist and can be read and opened are added to the list of files to open
            params.forEach((paramName, paramValue) -> {
                if(paramName != null && (paramName.startsWith(PRESENTATION_ARGUMENT_PREFIX) ||
                        paramName.startsWith(TEMPLATE_ARGUMENT_PREFIX))) {

                    final File file = new File(paramValue);

                    if (file.exists() && file.canRead() && file.canWrite() && !this.filesToOpen.contains(file)) {
                        this.filesToOpen.add(file);
                    }
                }
            });
        }

        // Try to load parameters that are passed dynamically with just a value
        final List<String> unnamedParams = getParameters().getUnnamed();
        if(unnamedParams != null && !unnamedParams.isEmpty()) {
            unnamedParams.forEach(param -> {
                final File file = new File(param);

                if((file.getName().endsWith(TemplateEngine.DEFAULT_ARCHIVE_EXTENSION) ||
                        file.getName().endsWith(PresentationEngine.DEFAULT_ARCHIVE_EXTENSION))
                        && file.exists() && file.canRead() && file.canWrite() && !this.filesToOpen.contains(file)) {
                    this.filesToOpen.add(file);
                }
            });
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        ((SimpleObjectProperty<Stage>) SlideshowFX.stage).set(stage);

        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/twasyl/slideshowfx/fxml/SlideshowFX.fxml"));
        final Parent root = loader.load();
        ((SimpleObjectProperty<SlideshowFXController>) this.mainController).set(loader.getController());

        final Scene scene = new Scene(root);
        ((SimpleObjectProperty<Scene>) presentationBuilderScene).set(scene);

        stage.setTitle("SlideshowFX");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.getIcons().addAll(
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/16.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/32.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/64.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/128.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/256.png")),
                new Image(SlideshowFX.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/512.png")));
        stage.show();

        if(this.filesToOpen != null && !this.filesToOpen.isEmpty()) {
            this.filesToOpen.forEach(file -> {
                try {
                    this.mainController.get().openTemplateOrPresentation(file);
                } catch (IllegalAccessException | FileNotFoundException e) {
                    LOGGER.log(Level.SEVERE, "Can not open file at startup", e);
                }
            });
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        this.mainController.get().closeAllPresentations(true);

        deleteTemporaryFiles();
        stopInternalServer();
        stopOSGIManager();
    }

    /**
     * Deletes temporary files older than the configuration parameter {@link GlobalConfiguration#TEMPORARY_FILES_MAX_AGE_PARAMETER}.
     */
    private void deleteTemporaryFiles() {
        if(GlobalConfiguration.canDeleteTemporaryFiles()) {
            LOGGER.info("Cleaning temporary files");
            final File tempDirectory = new File(System.getProperty("java.io.tmpdir"));

            Arrays.stream(tempDirectory.listFiles())
                    .filter(file -> file.getName().startsWith("sfx-"))
                    .filter(DateTimeUtils.getFilterForFilesOlderThanGivenDays(GlobalConfiguration.getTemporaryFilesMaxAge()))
                    .forEach(file -> {
                        try {
                            Files.walkFileTree(file.toPath(), new DeleteFileVisitor());
                        } catch (IOException e) {
                            LOGGER.log(Level.SEVERE,
                                    String.format("Can not delete temporary file %1$s", file.getAbsolutePath()),
                                    e);
                        }
                    });
        }
    }

    /**
     * Stop the internal server if it is running.
     */
    private void stopInternalServer() {
        if(SlideshowFXServer.getSingleton() != null) {
            LOGGER.info("Closing the internal server");
            SlideshowFXServer.getSingleton().stop();
        }
    }

    /**
     * Stops the OSGI manager. If some {@link IHostingConnector} are registered, disconnect from them.
     */
    private void stopOSGIManager() {
        this.stopHostingConnectors();
        LOGGER.info("Stopping the OSGi manager");
        OSGiManager.getInstance().stop();
    }

    /**
     * Disconnect all hosting connectors if they are running.
     */
    private void stopHostingConnectors() {
        final List<IHostingConnector> connectors = OSGiManager.getInstance().getInstalledServices(IHostingConnector.class);

        if(!connectors.isEmpty()) {
            LOGGER.info("Disconnecting from all hosting connectors");
            connectors.forEach(hostingConnector -> hostingConnector.disconnect());
        }
    }

    public static ReadOnlyObjectProperty<Stage> stageProperty() { return stage; }
    public static Stage getStage() { return stageProperty().get(); }

    public static void main(String[] args) {
        SlideshowFX.launch(args);
    }
}