package com.simibubi.create.content.curiosities.bell;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class PeculiarBellTileEntity extends AbstractBellTileEntity {

	public PeculiarBellTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public PartialModel getBellModel() {
		return AllBlockPartials.PECULIAR_BELL;
	}

}