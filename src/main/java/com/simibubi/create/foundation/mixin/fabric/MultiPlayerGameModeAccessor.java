package com.simibubi.create.foundation.mixin.fabric;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {

	@Invoker("performUseItemOn")
	InteractionResult create$callPerformUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult);
}
