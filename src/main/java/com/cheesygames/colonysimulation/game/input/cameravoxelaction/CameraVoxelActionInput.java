package com.cheesygames.colonysimulation.game.input.cameravoxelaction;

import com.cheesygames.colonysimulation.input.IActionInput;
import com.cheesygames.colonysimulation.reflection.IEnumCachedValues;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;

/**
 * The actions and their inputs for the player camera and for selecting and editing voxels.
 */
public enum CameraVoxelActionInput implements IActionInput {

    ADD_VOXEL(true, false, 8, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT)),
    ADD_LIGHT_VOXEL(true, false, 8, new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE)),
    DESTROY_VOXEL(true, false, 8, new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

    static {
        try {
            IEnumCachedValues.cacheValues(CameraVoxelActionInput.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Trigger[] m_triggers;
    private final boolean m_isAnalog;
    private final boolean m_isAction;
    private final float m_maxInputPerSecond;

    CameraVoxelActionInput(boolean isAnalog, boolean isAction, float maxInputPerSecond, Trigger... triggers) {
        this.m_triggers = triggers;
        this.m_isAnalog = isAnalog;
        this.m_isAction = isAction;
        this.m_maxInputPerSecond = maxInputPerSecond;
    }

    @Override
    public Trigger[] getTriggers() {
        return m_triggers;
    }

    @Override
    public boolean isAnalog() {
        return m_isAnalog;
    }

    @Override
    public boolean isAction() {
        return m_isAction;
    }

    public float getMaxInputPerSecond() {
        return m_maxInputPerSecond;
    }
}
