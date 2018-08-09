package com.cheesygames.colonysimulation.game.input.cameravoxelaction;

import com.cheesygames.colonysimulation.GameGlobal;
import com.cheesygames.colonysimulation.asset.DefaultMaterial;
import com.cheesygames.colonysimulation.input.ActionInputAppState;
import com.cheesygames.colonysimulation.math.MeshBufferUtils;
import com.cheesygames.colonysimulation.math.bounding.ray.VoxelRay;
import com.cheesygames.colonysimulation.math.direction.Direction3D;
import com.cheesygames.colonysimulation.math.vector.Vector3i;
import com.cheesygames.colonysimulation.world.chunk.Chunk;
import com.cheesygames.colonysimulation.world.chunk.EmptyChunk;
import com.cheesygames.colonysimulation.world.chunk.IChunkVoxelData;
import com.cheesygames.colonysimulation.world.chunk.voxel.VoxelType;
import com.cheesygames.colonysimulation.world.raycast.VoxelFaceRayCastContinuousTraverser;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Application state that shows the currently selected voxel and allows its edition.
 */
public class CameraVoxelActionInputAppState extends ActionInputAppState<CameraVoxelActionInput, CameraVoxelActionListener, CameraVoxelActionListener> {

    private static final List<Vector3f> FACE_MESH_VERTICES = new ArrayList<>(MeshBufferUtils.SHARED_VERTICES_PER_QUAD);

    public static final double RAY_DISTANCE = 40;

    static {
        FACE_MESH_VERTICES.add(new Vector3f(-0.5f, 0.5f, 0.5f));
        FACE_MESH_VERTICES.add(new Vector3f(0.5f, 0.5f, 0.5f));
        FACE_MESH_VERTICES.add(new Vector3f(0.5f, -0.5f, 0.5f));
        FACE_MESH_VERTICES.add(new Vector3f(-0.5f, -0.5f, 0.5f));
    }

    private VoxelRay m_voxelRay;

    // Keep in mind that it will be changed by the ray
    private Vector3i m_finalVoxel;

    private VoxelFaceRayCastContinuousTraverser m_rayCastAction;
    private Geometry m_facePreview;

    public CameraVoxelActionInputAppState() {
        this(new CameraVoxelActionListener());
    }

    public CameraVoxelActionInputAppState(CameraVoxelActionListener cameraVoxelActionListener) {
        super(CameraVoxelActionInput.class, cameraVoxelActionListener, cameraVoxelActionListener);

        m_voxelRay = new VoxelRay();
        m_finalVoxel = new Vector3i();
        m_rayCastAction = new VoxelFaceRayCastContinuousTraverser(GameGlobal.world);
        m_facePreview = createFacePreview();

        m_voxelRay.setLength(RAY_DISTANCE);

        m_rayCastAction.setReturnCondition((index, voxelType) -> {
            boolean stopRayCast = voxelType == VoxelType.SOLID;

            if (stopRayCast) {
                if (m_rayCastAction.getIncomingDirection() != Direction3D.ZERO && m_actionListener.shouldAddVoxel()) {
                    m_rayCastAction.setLastTraversedChunk(modifyVoxelAtRayCast(m_rayCastAction.getLastTraversedChunk(),
                        m_rayCastAction.getLastTraversedChunkIndex(),
                        m_rayCastAction.getLastTraversedRelativeVoxelIndex(),
                        VoxelType.SOLID));
                }

                if (m_actionListener.shouldDestroyVoxel() && !m_actionListener.shouldAddVoxel()) {
                    m_rayCastAction.setChunk(modifyVoxelAtRayCast(m_rayCastAction.getChunk(),
                        m_rayCastAction.getChunkIndex(),
                        m_rayCastAction.getRelativeVoxelIndex(),
                        VoxelType.AIR));
                }
            }

            return stopRayCast;
        });
    }

    /**
     * Modifies the voxel at the supplied voxel's chunk relative index. If the chunk does not exist, i.e. if it's an instance of {@link EmptyChunk}, then create and generate the
     * chunk at the specified chunk index and then apply the voxel modification.
     *
     * @param possiblyEmptyChunk The chunk to modify. It can either be an instance of {@link EmptyChunk} or {@link Chunk}.
     * @param chunkIndex         The index of the supplied chunk. It is not a reference to the return value of {@link Chunk#getIndex()}, if the parameter possiblyEmptyChunk is an
     *                           instance of {@link Chunk}.
     * @param relativeVoxelIndex The index in the supplied chunk of where the voxel to modify is.
     * @param newVoxel           The new value for the specified voxel.
     *
     * @return The parameter possiblyEmptyChunk if it is an instance of {@link Chunk}, otherwise the newly generated {@link Chunk} at the supplied chunk index.
     */
    private static IChunkVoxelData modifyVoxelAtRayCast(IChunkVoxelData possiblyEmptyChunk, Vector3i chunkIndex, Vector3i relativeVoxelIndex, VoxelType newVoxel) {
        Chunk chunk;

        // Generate or get the chunk
        if (possiblyEmptyChunk instanceof EmptyChunk) {
            chunk = GameGlobal.world.getWorldGenerator().generateChunk(new Vector3i(chunkIndex));
        }
        else {
            chunk = (Chunk) possiblyEmptyChunk;
        }

        VoxelType oldVoxel = chunk.getVoxelAt(relativeVoxelIndex).voxelType;
        if (oldVoxel != newVoxel) {
            chunk.setVoxelTypeAt(newVoxel, relativeVoxelIndex);

            // Update adjacent chunk(s) if the voxel is on a or multiple sides
            Vector3i adjacentChunkIndex = new Vector3i();
            for (int voxelIndexComponent = 0; voxelIndexComponent < Vector3i.COMPONENT_COUNT; ++voxelIndexComponent) {
                adjacentChunkIndex.set(chunkIndex);

                if (relativeVoxelIndex.get(voxelIndexComponent) == 0) {
                    adjacentChunkIndex.set(voxelIndexComponent, adjacentChunkIndex.get(voxelIndexComponent) - 1);
                }
                else if (relativeVoxelIndex.get(voxelIndexComponent) == GameGlobal.world.getChunkSize().get(voxelIndexComponent) - 1) {
                    adjacentChunkIndex.set(voxelIndexComponent, adjacentChunkIndex.get(voxelIndexComponent) + 1);
                }

                Chunk adjacentChunk = GameGlobal.world.getChunkAt(adjacentChunkIndex);
                if (adjacentChunk != null) {
                    GameGlobal.world.redrawChunk(adjacentChunk);
                }
            }

            // Add or redraw the chunk
            if (possiblyEmptyChunk instanceof EmptyChunk) {
                chunk.computeIsEmpty();
                GameGlobal.world.addChunk(chunk);
            }
            else {
                GameGlobal.world.redrawChunk(chunk);
            }
        }

        return chunk;
    }

    private Geometry createFacePreview() {
        Material facePreviewMat = new Material(GameGlobal.assetManager, DefaultMaterial.UNSHADED.getPath());
        facePreviewMat.setColor("Color", ColorRGBA.Red);
        facePreviewMat.getAdditionalRenderState().setDepthTest(false);

        Geometry facePreview = new Geometry("Voxel Face Preview", createFaceMesh());
        facePreview.setMaterial(facePreviewMat);

        return facePreview;
    }

    private Mesh createFaceMesh() {
        Mesh mesh = new Mesh();
        mesh.setMode(Mesh.Mode.LineLoop);

        FloatBuffer positionBuffer = MeshBufferUtils.createPositionBuffer(FACE_MESH_VERTICES);
        MeshBufferUtils.setMeshBuffer(mesh, VertexBuffer.Type.Position, positionBuffer);

        mesh.updateBound();
        return mesh;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (!enabled) {
            m_facePreview.removeFromParent();
        }
    }

    @Override
    public void stateDetached(AppStateManager stateManager) {
        m_facePreview.removeFromParent();
    }

    @Override
    public void update(float tpf) {
        Camera cam = GameGlobal.game.getCamera();

        m_voxelRay.setStart(cam.getLocation());
        m_voxelRay.setDirection(cam.getDirection());

        m_voxelRay.rayCastLocal(m_rayCastAction, m_finalVoxel);
        m_actionListener.setShouldAddVoxel(false);
        m_actionListener.setShouldDestroyVoxel(false);

        if (m_voxelRay.wasStopped() && m_rayCastAction.getTraversedVoxelCount() > 2) {
            m_facePreview.setLocalRotation(m_rayCastAction.getIncomingDirection().getOpposite().getRotation());
            m_facePreview.setLocalTranslation(m_finalVoxel.x, m_finalVoxel.y, m_finalVoxel.z);

            if (m_facePreview.getParent() == null) {
                GameGlobal.rootNode.attachChild(m_facePreview);
            }
        }
        else {
            m_facePreview.removeFromParent();
        }
    }
}
