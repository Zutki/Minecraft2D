/**
 * The render.RenderEngine class is responsible to creating the engine and managing engine events
 * A key way to interact with the render engine is through the use of events.
 * While using events is not entirely necessary when running in a single threaded way, it is absolutely important when running
 * in a multithreaded environment as concurrency issues can arise from the engine not being started in time or other issues
 */
package render;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import render.renderer.Renderer;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * <b>IMPORTANT:</b>
 * <br>These classes are meant to be modified to fit the needs of the program running them
 * <br><br>
 * The Render engine interacts with the java awt and swing libraries to provide render capabilities for a game
 * The engine <b>can</b> run on its own thread for speed reasons.
 * <br>Note that the engine is a singleton
 */
public class RenderEngine implements Runnable {
    private static RenderEngine renderEngine = null;
    private static Renderer renderer;
    private static JFrame window = null;
    private static Dimension windowDimension = new Dimension(1920/4, 1080/4);
    private static String title = "SARE Window";
    private static ArrayList<Event> eventQueue = new ArrayList<Event>(); // the event queue
    private static boolean isRunningQueue = false; // this variable tells the render engine whether it is currently running the event queue
    private static volatile boolean initialized = false; // turned true when the window has been initialized
    private static final Logger LOGGER = LoggerFactory.getLogger(RenderEngine.class);

    private RenderEngine() {
        renderer = new Renderer(windowDimension);
    }

    /**
     * Initializes the JFrame that everything will be drawn on, This class is meant to be user modified to suit the needs of the program
     */
    private void initialize() {
        window = new JFrame(title);
        initialized = true;
        window.add(renderer);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Here you can put whatever options you need for the JFrame
        // Some examples:
        window.setResizable(false);
        window.pack(); // be sure to call pack after calling setResizable as this helps avoid issues on some platforms

        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }


    /**
     * Creates or returns the render engine object.
     * @return the render engine
     */
    public static RenderEngine getInstance() {
        if (renderEngine == null) {
            renderEngine = new RenderEngine();
        }
        return renderEngine;
    }

    /**
     * Sets the title of the window
     * @param _title title of the window
     */
    public static void setTitle(String _title) {
        title = _title;
        window.setTitle(title);
    }

    /**
     * Returns the title of window
     * @return the title
     */
    public static String getTitle() { return title; }

    /**
     * Sets the dimensions of the window
     * @param width Width
     * @param height Height
     */
    public static void setWindowDimension(int width, int height) {
        windowDimension = new Dimension(width, height);
        window.setSize(width, height);
    }

    /**
     * Sets the dimensions of the window
     * @param dimension the dimensions to set it to
     */
    public static void setWindowDimension(Dimension dimension) {
        windowDimension = dimension;
        window.setSize(dimension);
    }

    /**
     * Gets the dimensions of the window
     * @return the dimensions of the window
     */
    public static Dimension getWindowDimension() { return windowDimension; }

    /**
     * Sets the maximum framerate that the renderer will run at
     * If the max is set to 0 then v-sync will be set
     * @param framerate maximum framerate to set
     */
    public static void setMaxFramerate(int framerate ) {
        if (framerate == 0) {
            LOGGER.info("Setting framerate from V-Sync");
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            DisplayMode dm = ge.getDefaultScreenDevice().getDisplayMode();
            int refreshRate = dm.getRefreshRate();
            if (refreshRate == DisplayMode.REFRESH_RATE_UNKNOWN) {
                LOGGER.warn("V-Sync could not be found, assuming 60 fps");
                renderer.setMaxFramerate(60);
            }
            else {
                renderer.setMaxFramerate(refreshRate);
            }
            return;
        }
        renderer.setMaxFramerate(framerate);
    }

    /**
     * Returns the maximum framerate currently set
     * @return the maximum framerate
     */
    public static int getMaxFramerate() { return renderer.getMaxFramerate(); }

    /**
     * Adds an event to the event queue, it will also run the event queue if it is not already running
     * @param event the event to add
     */
    public static void addEventToQueue(Event event) {
        eventQueue.add(event);

        if (!initialized) {
            LOGGER.warn("Window has not been initialized yet, cannot run the queue!");
            return;
        }

        if (!isRunningQueue) {
            runEventQueue();
        }
    }

    /**
     * This runs through the event queue executing each given event in the order it was added
     */
    private static void runEventQueue() {
        isRunningQueue = true;
        for (int i = 0; i < eventQueue.size(); i++) { // using a standard for loop instead of a for each to avoid a runtime exception
            eventQueue.get(i).run();
        }
        isRunningQueue = false;
    }

    /**
     * Starts the rendering
     * <br>Multithreading support
     */
    @Override
    public void run() {
        LOGGER.info("Starting the render engine"); // Note: if you have a logger implementation, then replace this with it
        SwingUtilities.invokeLater(this::initialize);

        // wait for the window to be initialized and started
        LOGGER.info("Waiting for window to start...");
        while (!initialized) Thread.onSpinWait();
        LOGGER.info("Window started without error!");

        // run the event queue
        LOGGER.info("Running through event queue "+eventQueue.size()+ " event(s)");

        // If you do not do this, it will for some reason not work???
        try {
            Thread.sleep(50l);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        runEventQueue();
    }
}
