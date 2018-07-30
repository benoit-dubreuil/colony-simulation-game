package com.cheesygames.colonysimulation;

import com.cheesygames.colonysimulation.appstate.VoxelFaceRayCastPreviewer;
import com.cheesygames.colonysimulation.asset.DefaultMaterial;
import com.cheesygames.colonysimulation.world.World;
import com.cheesygames.colonysimulation.world.chunk.Chunk;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Box;

public class ColonySimulationGame extends Game {

    private Material m_chunkMaterial;
    private Material m_chunkBoundsMaterial;

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
        initCrossHairs();
        addChunks();

        flyCam.setMoveSpeed(flyCam.getMoveSpeed() * 18f);
        cam.setLocation(Vector3f.UNIT_Y.mult(25));
        attachCoordinateAxes(Vector3f.ZERO.clone());

        VoxelFaceRayCastPreviewer voxelFaceRayCastPreviewer = new VoxelFaceRayCastPreviewer();
        stateManager.attach(voxelFaceRayCastPreviewer);
    }

    @Override
    protected void updateGame() {
    }

    private Node attachCoordinateAxes(Vector3f pos) {
        Node arrowNode = new Node();
        arrowNode.setLocalTranslation(pos);

        Arrow arrow = new Arrow(Vector3f.UNIT_X);
        arrowNode.attachChild(putShape(arrow, ColorRGBA.Red));

        arrow = new Arrow(Vector3f.UNIT_Y);
        arrowNode.attachChild(putShape(arrow, ColorRGBA.Green));

        arrow = new Arrow(Vector3f.UNIT_Z);
        arrowNode.attachChild(putShape(arrow, ColorRGBA.Blue));

        rootNode.attachChild(arrowNode);

        return arrowNode;
    }

    private Geometry putShape(Mesh shape, ColorRGBA color) {
        Geometry g = new Geometry("coordinate axis", shape);
        Material mat = new Material(assetManager, DefaultMaterial.UNSHADED.getPath());
        mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setLineWidth(4);
        mat.setColor("Color", color);
        g.setMaterial(mat);
        g.setLocalScale(10f);

        return g;
    }

    protected void addChunks() {
        m_chunkMaterial = new Material(assetManager, DefaultMaterial.LIGHTING.getPath());
        m_chunkMaterial.setBoolean("UseMaterialColors", true);
        m_chunkMaterial.setColor("Ambient", ColorRGBA.White);
        m_chunkMaterial.setColor("Diffuse", ColorRGBA.LightGray);
        m_chunkMaterial.setColor("Specular", new ColorRGBA(0.1f, 0.1f, 0.1f, 1f));
        m_chunkMaterial.setFloat("Shininess", 64f);  // [0,128]

        m_chunkBoundsMaterial = new Material(assetManager, DefaultMaterial.UNSHADED.getPath());
        m_chunkBoundsMaterial.setColor("Color", ColorRGBA.Blue);
        m_chunkBoundsMaterial.getAdditionalRenderState().setWireframe(true);
        m_chunkBoundsMaterial.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);

        for (Chunk chunk : GameGlobal.world.getChunks().values()) {
            addChunk(chunk);
        }
    }

    protected void addChunk(Chunk chunk) {
        Node chunkNode = new Node("");
        Geometry chunkGeom = new Geometry("", chunk.getMesh());
        Box boundsMesh = new Box(GameGlobal.world.getChunkSize().x / 2f, GameGlobal.world.getChunkSize().y / 2f, GameGlobal.world.getChunkSize().z / 2f);
        Geometry chunkBounds = new Geometry("", boundsMesh);

        chunkBounds.setLocalTranslation(boundsMesh.xExtent - 0.5f, boundsMesh.yExtent - 0.5f, boundsMesh.zExtent - 0.5f);
        chunkNode.setLocalTranslation(chunk.computePositionIndex().toVector3f());

        chunkGeom.setMaterial(m_chunkMaterial);
        chunkBounds.setMaterial(m_chunkBoundsMaterial);

        chunkNode.attachChild(chunkGeom);
        chunkNode.attachChild(chunkBounds);

        rootNode.attachChild(chunkNode);
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

    /** A centred plus sign to help the player aim. */
    protected void initCrossHairs() {
        setDisplayStatView(false);
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+"); // crosshairs
        ch.setLocalTranslation( // center
            settings.getWidth() / 2 - ch.getLineWidth()/2,
            settings.getHeight() / 2 + ch.getLineHeight()/2, 0);
        guiNode.attachChild(ch);
    }
}
