package com.cheesygames.colonysimulation;

import com.cheesygames.colonysimulation.asset.DefaultMaterial;
import com.cheesygames.colonysimulation.world.World;
import com.cheesygames.colonysimulation.world.chunk.Chunk;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.debug.Arrow;

public class ColonySimulationGame extends Game {

    private Material m_material;

    public static void main(String[] args) {
        new ColonySimulationGame().start();
    }

    @Override
    public void simpleInitApp() {
        super.simpleInitApp();

        GameGlobal.world = new World();
        GameGlobal.world.generateWorld();
        GameGlobal.world.generateMeshes();

        initLights();
        addChunks();

        flyCam.setMoveSpeed(flyCam.getMoveSpeed() * 25f);
        attachCoordinateAxes(Vector3f.ZERO.clone());
    }

    @Override
    protected void updateGame() {

    }

    private void attachCoordinateAxes(Vector3f pos) {
        Arrow arrow = new Arrow(Vector3f.UNIT_X);
        putShape(arrow, ColorRGBA.Red).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Y);
        putShape(arrow, ColorRGBA.Green).setLocalTranslation(pos);

        arrow = new Arrow(Vector3f.UNIT_Z);
        putShape(arrow, ColorRGBA.Blue).setLocalTranslation(pos);
    }

    private Geometry putShape(Mesh shape, ColorRGBA color) {
        Geometry g = new Geometry("coordinate axis", shape);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setLineWidth(4);
        mat.setColor("Color", color);
        g.setMaterial(mat);
        g.setLocalScale(10f);
        rootNode.attachChild(g);
        return g;
    }

    protected void addChunks() {
        for (Chunk chunk : GameGlobal.world.getChunks().values()) {
            addChunk(chunk);
        }
    }

    protected void addChunk(Chunk chunk) {
        Geometry chunkGeom = new Geometry("", chunk.getMesh());
        Vector3f chunkCenter = chunk.computeCenter();
        chunkGeom.setLocalTranslation(chunkCenter.negateLocal());

        m_material = new Material(assetManager, DefaultMaterial.LIGHTING.getPath());
        m_material.setBoolean("UseMaterialColors", true);
        m_material.setColor("Ambient", ColorRGBA.White);
        m_material.setColor("Diffuse", ColorRGBA.LightGray);
        m_material.setColor("Specular", new ColorRGBA(0.1f, 0.1f, 0.1f, 1f));
        m_material.setFloat("Shininess", 64f);  // [0,128]
        m_material.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
        m_material.getAdditionalRenderState().setWireframe(true);

        chunkGeom.setMaterial(m_material);
        rootNode.attachChild(chunkGeom);
    }

    protected void initLights() {
        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setDirection(Vector3f.UNIT_XYZ.mult(-1).normalizeLocal());
        directionalLight.setColor(ColorRGBA.White);
        rootNode.addLight(directionalLight);

        AmbientLight ambientLight = new com.jme3.light.AmbientLight();
        ambientLight.setColor(ColorRGBA.White.mult(0.1f));
        rootNode.addLight(ambientLight);
    }
}
