package fastbot.scene;

import fastsoftware3d.camera.Camera;
import fastsoftware3d.core.Framebuffer;
import fastsoftware3d.core.RenderPipeline;
import fastsoftware3d.rasterizer.NativeRasterizer;
import fastsoftware3d.scene.Renderer3D;
import fastsoftware3d.scene.Scene;
import fastterminal.FastTerminalScene;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import fastterminal3d.FastTerminal3DRenderer;

public class Scene1Background {
    private Camera camera;
    private Scene scene;
    private Renderer3D activeRenderer;
    private BufferedImage renderBuffer;
    private int[] renderPixels;

    private int ssaa;
    private int cols;
    private int rows;

    public Scene1Background(int ssaa) {
        this.ssaa = ssaa;
        this.camera = new Camera(
                -230f, 15f, 76f,
                -1.9740f, -0.0500f, 23.0f
        );
        initScene();
    }

    private BotHead botHead;

    private void initScene() {
        scene = new Scene();
        try {
            botHead = new BotHead();
            if (botHead.getHeadNode() != null) scene.getRoot().addChild(botHead.getHeadNode());
            if (botHead.getNeckNode() != null) scene.getRoot().addChild(botHead.getNeckNode());
        } catch (Exception e) {
            System.err.println("Error loading scene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(long deltaMs) {
        if (botHead != null) {
            botHead.update(deltaMs);
        }
    }

    public void resize(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        int renderW = cols * ssaa;
        int renderH = rows * 2 * ssaa;

        renderBuffer = new BufferedImage(renderW, renderH, BufferedImage.TYPE_INT_RGB);
        renderPixels = ((DataBufferInt) renderBuffer.getRaster().getDataBuffer()).getData();

        Framebuffer fb = new Framebuffer(renderW, renderH, renderPixels);
        RenderPipeline pipeline = new RenderPipeline(camera, fb, new NativeRasterizer());
        activeRenderer = new Renderer3D(pipeline);
    }

    public void render(FastTerminalScene canvas) {
        if (renderPixels == null) return;

//        Arrays.fill(renderPixels, 0x000000);
        activeRenderer.clear();

        Graphics2D g = renderBuffer.createGraphics();
        scene.render(activeRenderer, g);
        g.dispose();
        activeRenderer.getPipeline().postProcess();

        FastTerminal3DRenderer.render(renderPixels, cols * ssaa, rows * 2 * ssaa, canvas, cols, rows, ssaa, false);
    }

    public BotHead getBotHead() {
        return botHead;
    }
}


