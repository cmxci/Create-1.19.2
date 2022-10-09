package com.simibubi.create.foundation.mixin.fabric;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.apache.commons.lang3.mutable.MutableObject;

import com.simibubi.create.foundation.mixin.fabric.MultiPlayerGameModeAccessor;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

	@Inject(method = "method_41933", at = @At("HEAD"), cancellable = true)
	private void create$reorderPlacementPackets1(MutableObject mutableObject, LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, int sequence, CallbackInfoReturnable<Packet> ci) {
		ci.setReturnValue(new ServerboundUseItemOnPacket(hand, hitResult, sequence));
	}

	@Inject(method = "useItemOn", at = @At("TAIL"), cancellable = true)
	private void create$reorderPlacementPackets2(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> ci) {
		ci.setReturnValue(((MultiPlayerGameModeAccessor)(Object)this).create$callPerformUseItemOn(player, hand, hitResult));
	}
}
