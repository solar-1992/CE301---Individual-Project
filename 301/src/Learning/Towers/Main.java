package Learning.Towers;

import Learning.Towers.Entities.Entity;
import Learning.Towers.Influence.InfluenceMap;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

/**
 * Created with IntelliJ IDEA.
 * User: Piers
 * Date: 16/10/12
 * Time: 11:49
 */
public class Main {
    public static int SCREEN_WIDTH = 1600;
    public static int SCREEN_HEIGHT = 900;

    public static int MAP_WIDTH = 1600;
    public static int MAP_HEIGHT = 900;
    public static int SQUARE_WIDTH = 6;
    public static int SQUARE_COUNT = 200;
    public static boolean FULL_SCREEN = true;
    public static final int CELL_SIZE = 75;

    public static GameLoop GAME_LOOP;
    public static CollisionBoard COLLISION_BOARD;
    public static InfluenceMap INFLUENCE_MAP;

    boolean cDown = false;

    public Main() {

        Main.GAME_LOOP = new GameLoop(20);
        Main.COLLISION_BOARD = new CollisionBoard(Main.MAP_WIDTH, Main.MAP_HEIGHT, CELL_SIZE);
        Main.INFLUENCE_MAP = new InfluenceMap(Main.MAP_WIDTH, Main.MAP_HEIGHT, 30, 40);

        if (FULL_SCREEN) System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
        // Set up the display
        try {
            Display.setDisplayMode(new DisplayMode(SCREEN_WIDTH, SCREEN_HEIGHT));
            Display.setFullscreen(true);
            Display.create();

            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(0, SCREEN_WIDTH, 0, SCREEN_HEIGHT, 1, -1);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        } catch (Exception e) {

        }

        Thread physics = new Thread(Main.COLLISION_BOARD);
        physics.setDaemon(true);
        physics.setName("Collision Detection");
        physics.start();

        for (int i = 0; i < Main.SQUARE_COUNT; i++) Main.GAME_LOOP.addEntity(new Entity(Main.SQUARE_WIDTH, 1f, 1f, 1f));

        Thread loop = new Thread(GAME_LOOP);
        loop.setDaemon(true);
        loop.setName("Game Loop");
        loop.start();

        Thread influence = new Thread(INFLUENCE_MAP);
        influence.setDaemon(true);
        influence.setName("Influence Thread");
        influence.start();

        GAME_LOOP.addFaction(new Faction(0, 1, 0, 0, new Vector2D(50, MAP_HEIGHT / 2)));
        GAME_LOOP.addFaction(new Faction(1, 0, 1, 0, new Vector2D(MAP_WIDTH - 100, MAP_HEIGHT / 3)));
        GAME_LOOP.addFaction(new Faction(2, 0, 0, 1, new Vector2D(MAP_WIDTH - 100, MAP_HEIGHT * 2 / 3)));
//        GAME_LOOP.addFaction(new Faction(3, 1, 0, 1));
//        GAME_LOOP.addFaction(new Faction(4, 0, 1, 1));
//        GAME_LOOP.addFaction(new Faction(5, 1, 1, 0));
//        GAME_LOOP.addFaction(new Faction(6, 0.25f, 0.75f, 0.25f));
//        GAME_LOOP.addFaction(new Faction(7, 0.75f, 0.25f, 0.25f));
//        GAME_LOOP.addFaction(new Faction(8, 0.25f, 0.25f, 0.75f));


        // Set the loops to do work
        COLLISION_BOARD.setPaused(false);
        GAME_LOOP.setPaused(false);
        INFLUENCE_MAP.setPaused(false);

        boolean stepping = false;
        boolean paused = false;

        /**
         * Main render loop
         */
        while (!Display.isCloseRequested()) {
            try {
                Thread.sleep(20);
            } catch (Exception e) {

            }

            // Keyboard release
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                break;
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_P)) {
                paused = !paused;
                COLLISION_BOARD.setPaused(paused);
                GAME_LOOP.setPaused(paused);
                INFLUENCE_MAP.setPaused(paused);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
                stepping = !stepping;
            }

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            Main.GAME_LOOP.draw();
            INFLUENCE_MAP.draw();

            Display.update();
        }
        Display.destroy();
    }

    public static void main(String[] args) {
        new Main();
    }
}
