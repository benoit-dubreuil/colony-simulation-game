package com.cheesygames.colonysimulation.game.input.cameravoxelaction;

import com.cheesygames.colonysimulation.input.listener.action.IPausableActionListener;
import com.cheesygames.colonysimulation.input.listener.analog.IPausableAnalogListener;

/**
 * Listens to the input events from {@link CameraVoxelActionInput}. Should be used inside the {@link CameraVoxelActionInputAppState}
 * app state.
 */
public class CameraVoxelActionListener implements IPausableAnalogListener<CameraVoxelActionInput>, IPausableActionListener<CameraVoxelActionInput> {

    private boolean m_shouldAddVoxel;

    @Override
    public void onAction(CameraVoxelActionInput cameraVoxelAction, boolean isPressed, float tpf) {
        // Empty
    }

    @Override
    public void onAnalog(CameraVoxelActionInput cameraVoxelAction, float value, float tpf) {
        switch (cameraVoxelAction) {
            case ADD_VOXEL:
                m_shouldAddVoxel |= true;
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
}
