package fastbot.scene;

import fastsoftware3d.material.Material;
import fastsoftware3d.model.ObjLoader;
import fastsoftware3d.model.ObjLoader.ModelData;
import fastsoftware3d.scene.ModelNode;

import java.io.File;

public class BotHead {

    private static final String DEFAULT_ASSETS_PATH = "examples/Demo/assets/";
    private static final String LOCAL_ASSETS_PATH = "assets/";
    private static final float MODEL_SCALE = 20.0f;

    private ModelNode headNode;
    private ModelNode neckNode;
    
    private BotHeadTextureDrawer textureDrawer;
    private BotHeadGestures gestures;

    public BotHead() {
        initModel();
    }

    private void initModel() {
        try {
            String basePath = new File("assets").exists() ? LOCAL_ASSETS_PATH : DEFAULT_ASSETS_PATH;
            
            ModelData headModel = ObjLoader.load(basePath + "head.obj");
            ModelData neckModel = ObjLoader.load(basePath + "neck.obj");
            Material headMat = Material.fromPng(basePath + "texture.png");

            scaleModel(headModel, MODEL_SCALE);
            scaleModel(neckModel, MODEL_SCALE);

            headNode = new ModelNode(headModel, headMat);
            neckNode = new ModelNode(neckModel, headMat);

            headNode.getTransform().x = 0.0f;
            headNode.getTransform().y = 0.0f;
            headNode.getTransform().z = 0.0f;

            neckNode.getTransform().x = 0.0f;
            neckNode.getTransform().y = 0.0f;
            neckNode.getTransform().z = 0.0f;

            textureDrawer = new BotHeadTextureDrawer(headMat);
            gestures = new BotHeadGestures(headNode, textureDrawer);

        } catch (Exception e) {
            System.err.println("Error loading BotHead: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void scaleModel(ModelData m, float s) {
        for (int i = 0; i < m.vertexCount; i++) {
            int off = i * 3;
            float oldX = m.vertices[off];
            float oldY = m.vertices[off + 1];
            float oldZ = m.vertices[off + 2];
            m.vertices[off] = oldZ * s;
            m.vertices[off + 1] = oldY * s;
            m.vertices[off + 2] = oldX * s;
        }
        m.boundingRadius *= s;
        for (int i = 0; i < m.faceCount; i++) {
            int off = i * 3;
            int vTmp = m.vIndices[off + 1];
            m.vIndices[off + 1] = m.vIndices[off + 2];
            m.vIndices[off + 2] = vTmp;
            int uvTmp = m.uvIndices[off + 1];
            m.uvIndices[off + 1] = m.uvIndices[off + 2];
            m.uvIndices[off + 2] = uvTmp;
        }
    }

    public synchronized void update(long deltaMs) {
        if (gestures != null) {
            gestures.update(deltaMs);
        }
        if (textureDrawer != null) {
            textureDrawer.commit();
        }
    }

    public void performGesture(String name) {
        if (gestures != null) {
            gestures.performGesture(name);
        }
    }
    
    public void setMouthOpenness(float openness) {
        if (textureDrawer != null) {
            textureDrawer.setMouthOpenness(openness);
        }
    }

    public void setEyeOffset(int x, int y) {
        if (textureDrawer != null) {
            textureDrawer.setEyeOffset(x, y);
        }
    }

    public void setEyeScale(float scale) {
        if (textureDrawer != null) {
            textureDrawer.setEyeScale(scale);
        }
    }

    public void setMouthOffset(int x, int y) {
        if (textureDrawer != null) {
            textureDrawer.setMouthOffset(x, y);
        }
    }
    
    public void setUserIsTyping(boolean typing) {
        if (gestures != null) {
            gestures.setUserIsTyping(typing);
        }
    }

    public String getLastGestureName() {
        return gestures != null ? gestures.getLastGestureName() : "";
    }

    public boolean isPerformingGesture() {
        return gestures != null && gestures.isPerformingGesture();
    }

    public ModelNode getHeadNode() {
        return headNode;
    }

    public ModelNode getNeckNode() {
        return neckNode;
    }
}
