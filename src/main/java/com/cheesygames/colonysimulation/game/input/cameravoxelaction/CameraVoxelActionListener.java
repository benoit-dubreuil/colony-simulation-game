package com.cheesygames.colonysimulation.game.input.cameravoxelaction;

import com.cheesygames.colonysimulation.input.listener.action.IPausableActionListener;
import com.cheesygames.colonysimulation.input.listener.analog.IPausableAnalogListener;

/**
 * Listens to the input events from {@link CameraVoxelActionInput}. Should be used inside the {@link CameraVoxelActionInputAppState} app state.
 */
public class CameraVoxelActionListener implements IPausableAnalogListener<CameraVoxelActionInput>, IPausableActionListener<CameraVoxelActionInput> {

    private boolean m_shouldAddVoxel;
    private boolean m_shouldDestroyVoxel;
    private float m_timeSinceLastVoxelAdded;
    private float m_timeSinceLastVoxelDestroyed;

    @Override
    public void onAction(CameraVoxelActionInput cameraVoxelAction, boolean isPressed, float tpf) {
        // Empty
    }

    @Override
    public void onAnalog(CameraVoxelActionInput cameraVoxelAction, float value, float tpf) {
        switch (cameraVoxelAction) {
            case ADD_VOXEL:
                if ((m_timeSinceLastVoxelAdded += tpf) * cameraVoxelAction.getMaxInputPerSecond() >= 1) {
                    m_shouldAddVoxel |= true;
                    m_timeSinceLastVoxelAdded = 0;
                }
                break;

            case DESTROY_VOXEL:
                if ((m_timeSinceLastVoxelDestroyed += tpf) * cameraVoxelAction.getMaxInputPerSecond() >= 1) {
                    m_shouldDestroyVoxel |= true;
                    m_timeSinceLastVoxelDestroyed = 0;
                }
                break;
        }
    }

    @Override
    public Class<CameraVoxelActionInput> getActionListenerEnumClass() {
        return CameraVoxelActionInput.class;
    }

    @Override
    public Class<CameraVoxelActionInput> getAnalogListenerEnumClass() {
        return CameraVoxelActionInput.class;
    }

    public boolean shouldAddVoxel() {
        return m_shouldAddVoxel;
    }

    public void setShouldAddVoxel(boolean shouldAddVoxel) {
        m_shouldAddVoxel = shouldAddVoxel;
    }

    public boolean shouldDestroyVoxel() {
        return m_shouldDestroyVoxel;
    }

    public void setShouldDestroyVoxel(boolean shouldDestroyVoxel) {
        m_shouldDestroyVoxel = shouldDestroyVoxel;
    }
}
