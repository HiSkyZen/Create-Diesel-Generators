package com.jesz.createdieselgenerators.blocks.renderer;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.blockentity.BlockEntityInstance;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.materials.oriented.OrientedData;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.bearing.IBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaternionf;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class NoShaftBearingInstance<B extends KineticBlockEntity & IBearingBlockEntity> extends BlockEntityInstance<B> implements DynamicInstance {
    final OrientedData topInstance;

    final Axis rotationAxis;
    final Quaternionf blockOrientation;

    public NoShaftBearingInstance(MaterialManager materialManager, B blockEntity) {
        super(materialManager, blockEntity);

        Direction facing = blockState.getValue(BlockStateProperties.FACING);
        rotationAxis = Axis.of(Direction.get(Direction.AxisDirection.POSITIVE, blockEntity.getBlockState().getValue(FACING).getAxis()).step());

        blockOrientation = getBlockStateOrientation(facing);

        PartialModel top =
                blockEntity.isWoodenTop() ? AllPartialModels.BEARING_TOP_WOODEN : AllPartialModels.BEARING_TOP;

        topInstance = getOrientedMaterial().getModel(top, blockState).createInstance();

        topInstance.setPosition(getInstancePosition()).setRotation(blockOrientation);
    }

    @Override
    public void beginFrame() {
        float interpolatedAngle = blockEntity.getInterpolatedAngle(AnimationTickHolder.getPartialTicks() - 1);
        Quaternionf rot = rotationAxis.rotationDegrees(interpolatedAngle);

        rot.mul(blockOrientation);

        topInstance.setRotation(rot);
    }

    @Override
    public void updateLight() {
        super.updateLight();
        relight(pos, topInstance);
    }

    @Override
    public void remove() {
        topInstance.delete();
    }

    static Quaternionf getBlockStateOrientation(Direction facing) {
        Quaternionf orientation;

        if (facing.getAxis().isHorizontal()) {
            orientation = Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing.getOpposite()));
        } else {
            orientation = new Quaternionf();
        }

        orientation.mul(Axis.XP.rotationDegrees(-90 - AngleHelper.verticalAngle(facing)));
        return orientation;
    }
}
