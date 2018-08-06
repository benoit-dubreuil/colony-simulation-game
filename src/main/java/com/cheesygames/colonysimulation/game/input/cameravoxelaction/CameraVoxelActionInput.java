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

    ADD_VOXEL(true, false, new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));

    static {
        try {
            IEnumCachedValues.cacheValues(CameraVoxelActionInput.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Trigger[] triggers;
    private final boolean isAnalog;
    private final boolean isAction;

    CameraVoxelActionInput(boolean isAnalog, boolean isAction, Trigger... triggers) {
        this.triggers = triggers;
        this.isAnalog = isAnalog;
        this.isAction = isAction;
    }

    @Override
    public Trigger[] getTriggers() {
        return triggers;
    }

    @Override
    public boolean isAnalog() {
        return isAnalog;
    }

    @Override
    public boolean isAction() {
        return isAction;
    }
}
