package com.cheesygames.colonysimulation;

import com.cheesygames.colonysimulation.asset.DefaultMaterial;
import com.cheesygames.colonysimulation.math.bounding.VoxelRay;
import com.cheesygames.colonysimulation.math.bounding.VoxelWorldUtils;
import com.cheesygames.colonysimulation.math.vector.Vector3i;
import com.cheesygames.colonysimulation.world.World;
import com.cheesygames.colonysimulation.world.chunk.Chunk;
import com.cheesygames.colonysimulation.world.chunk.voxel.VoxelType;
import com.cheesygames.colonysimulation.world.raycast.VoxelFaceRayCastContinuousTraverser;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.MouseButtonTrigger;
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
        Material mat = new Material(assetManager, DefaultMaterial.UNSHADED.getPath());
        mat.getAdditionalRenderState().setWireframe(true);
        mat.getAdditionalRenderState().setLineWidth(4);
        mat.setColor("Color", color);
        g.setMaterial(mat);
        g.setLocalScale(10f);
        rootNode.attachChild(g);
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

    private Box rayBox;
    private Geometry rayCastVisual;
    private Material rayMat;

    private final ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("VoxelRay") && !keyPressed) {
                VoxelRay ray = new VoxelRay(cam.getLocation().clone(), cam.getDirection().clone().normalizeLocal(), 40);
                VoxelFaceRayCastContinuousTraverser rayCastAction = new VoxelFaceRayCastContinuousTraverser(VoxelWorldUtils.getVoxelIndex(cam.getLocation()), GameGlobal.world);

                System.out.println();
                System.out.println(cam.getLocation());
                System.out.println(VoxelWorldUtils.getVoxelIndex(cam.getLocation()));

                rayCastAction.setReturnCondition((index, voxelType, direction) -> {
                    System.out.println(index + ", " + direction);
                    return voxelType == VoxelType.SOLID;
                });

                if (rayCastVisual == null) {
                    rayBox = new Box(World.VOXEL_HALF_EXTENT, World.VOXEL_HALF_EXTENT, World.VOXEL_HALF_EXTENT);
                    rayCastVisual = new Geometry("", rayBox);
                    rayMat = new Material(assetManager, DefaultMaterial.UNSHADED.getPath());

                    rayMat.setColor("Color", ColorRGBA.Red);   // set color of material to blue
                    rayCastVisual.setMaterial(rayMat);                   // set the cube's material
                    rootNode.attachChild(rayCastVisual);
                }

                Vector3i index = new Vector3i();

                System.out.println("Ray Cast");
                ray.rayCastLocal(World.VOXEL_HALF_EXTENT, rayCastAction, index);

                Vector3i visualLocalPosition = ray.wasStopped() ? index.add(rayCastAction.getIncomingDirection().getOpposite().getDirection()) : index;
                rayCastVisual.setLocalTranslation(visualLocalPosition.toVector3f());
            }
        }
    };

    @Override
    protected void initKeys() {
        super.initKeys();

        inputManager.addMapping("VoxelRay", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "VoxelRay");
    }
}
