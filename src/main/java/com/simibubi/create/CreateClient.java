package com.simibubi.create;

import com.jozufozu.flywheel.fabric.event.FlywheelEvents;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.trinkets.Trinkets;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueSelectionHandler;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.content.contraptions.components.structureMovement.render.SBBContraptionManager;
import com.simibubi.create.content.contraptions.relays.encased.CasingConnectivity;
import com.simibubi.create.content.curiosities.armor.AllArmorMaterials;
import com.simibubi.create.content.curiosities.armor.CopperArmorItem;
import com.simibubi.create.content.curiosities.bell.SoulPulseEffectHandler;
import com.simibubi.create.content.curiosities.weapons.PotatoCannonRenderHandler;
import com.simibubi.create.content.curiosities.zapper.ZapperRenderHandler;
import com.simibubi.create.content.logistics.trains.GlobalRailwayManager;
import com.simibubi.create.content.schematics.ClientSchematicLoader;
import com.simibubi.create.content.schematics.client.SchematicAndQuillHandler;
import com.simibubi.create.content.schematics.client.SchematicHandler;
import com.simibubi.create.events.ClientEvents;
import com.simibubi.create.events.InputEvents;
import com.simibubi.create.foundation.ClientResourceReloadListener;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.ponder.content.PonderIndex;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.CreateContexts;
import com.simibubi.create.foundation.render.RenderTypes;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.ModelSwapper;
import com.simibubi.create.foundation.utility.ghost.GhostBlocks;
import com.simibubi.create.foundation.utility.outliner.Outliner;

import io.github.fabricators_of_create.porting_lib.event.client.OverlayRenderCallback;
import io.github.fabricators_of_create.porting_lib.util.ArmorTextureRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class CreateClient implements ClientModInitializer {

	public static final SuperByteBufferCache BUFFER_CACHE = new SuperByteBufferCache();
	public static final Outliner OUTLINER = new Outliner();
	public static final GhostBlocks GHOST_BLOCKS = new GhostBlocks();
	public static final ModelSwapper MODEL_SWAPPER = new ModelSwapper();
	public static final CasingConnectivity CASING_CONNECTIVITY = new CasingConnectivity();

	public static final ClientSchematicLoader SCHEMATIC_SENDER = new ClientSchematicLoader();
	public static final SchematicHandler SCHEMATIC_HANDLER = new SchematicHandler();
	public static final SchematicAndQuillHandler SCHEMATIC_AND_QUILL_HANDLER = new SchematicAndQuillHandler();
	public static final SuperGlueSelectionHandler GLUE_HANDLER = new SuperGlueSelectionHandler();

	public static final ZapperRenderHandler ZAPPER_RENDER_HANDLER = new ZapperRenderHandler();
	public static final PotatoCannonRenderHandler POTATO_CANNON_RENDER_HANDLER = new PotatoCannonRenderHandler();
	public static final SoulPulseEffectHandler SOUL_PULSE_EFFECT_HANDLER = new SoulPulseEffectHandler();
	public static final GlobalRailwayManager RAILWAYS = new GlobalRailwayManager();

	public static final ClientResourceReloadListener RESOURCE_RELOAD_LISTENER = new ClientResourceReloadListener();

	@Override
	public void onInitializeClient() { // onCtorClient and clientInit merged
//		modEventBus.addListener(CreateClient::clientInit); // merged together
//		modEventBus.addListener(AllParticleTypes::registerFactories); // ParticleManagerRegistrationCallback in ClientEvents
		FlywheelEvents.GATHER_CONTEXT.register(CreateContexts::flwInit);
		FlywheelEvents.GATHER_CONTEXT.register(ContraptionRenderDispatcher::gatherContext);

		MODEL_SWAPPER.registerListeners();

		ZAPPER_RENDER_HANDLER.registerListeners();
		POTATO_CANNON_RENDER_HANDLER.registerListeners();

		// clientInit start

		BUFFER_CACHE.registerCompartment(CachedBufferer.GENERIC_TILE);
		BUFFER_CACHE.registerCompartment(CachedBufferer.PARTIAL);
		BUFFER_CACHE.registerCompartment(CachedBufferer.DIRECTIONAL_PARTIAL);
		BUFFER_CACHE.registerCompartment(KineticTileEntityRenderer.KINETIC_TILE);
		BUFFER_CACHE.registerCompartment(SBBContraptionManager.CONTRAPTION, 20);
		BUFFER_CACHE.registerCompartment(WorldSectionElement.DOC_WORLD_SECTION, 20);

		AllKeys.register();
		AllBlockPartials.init();
		AllStitchedTextures.init();

		PonderIndex.register();
		PonderIndex.registerTags();

		OverlayRenderCallback.EVENT.register(ClientEvents.ModBusEvents::registerGuiOverlays);
		UIRenderHelper.init();

		// fabric exclusive
		ClientEvents.register();
		InputEvents.register();
		AllPackets.channel.initClientListener();
		RenderTypes.init();
		ArmorTextureRegistry.register(AllArmorMaterials.COPPER, CopperArmorItem.TEXTURE);
		// causes class loading issues or something
		// noinspection Convert2MethodRef
		Mods.TRINKETS.executeIfInstalled(() -> () -> Trinkets.clientInit());

		fluidRenderingFix();
	}

	// TODO: remove when Registrate is fixed
	public void fluidRenderingFix() {
		SimpleFluidRenderHandler potionHandler = new SimpleFluidRenderHandler(new ResourceLocation("create:fluid/potion_still"), new ResourceLocation("create:fluid/potion_flow"));
		FluidRenderHandlerRegistry.INSTANCE.register(AllFluids.POTION.get().getSource(), potionHandler);
		FluidRenderHandlerRegistry.INSTANCE.register(AllFluids.POTION.get().getFlowing(), potionHandler);

		SimpleFluidRenderHandler teaHandler = new SimpleFluidRenderHandler(new ResourceLocation("create:fluid/tea_still"), new ResourceLocation("create:fluid/tea_flow"));
		FluidRenderHandlerRegistry.INSTANCE.register(AllFluids.TEA.get().getSource(), teaHandler);
		FluidRenderHandlerRegistry.INSTANCE.register(AllFluids.TEA.get().getFlowing(), teaHandler);

		SimpleFluidRenderHandler honeyHandler = new SimpleFluidRenderHandler(new ResourceLocation("create:fluid/honey_still"), new ResourceLocation("create:fluid/honey_flow"));
		FluidRenderHandlerRegistry.INSTANCE.register(AllFluids.HONEY.get().getSource(), honeyHandler);
		FluidRenderHandlerRegistry.INSTANCE.register(AllFluids.HONEY.get().getFlowing(), honeyHandler);

		SimpleFluidRenderHandler chocolateHandler = new SimpleFluidRenderHandler(new ResourceLocation("create:fluid/chocolate_still"), new ResourceLocation("create:fluid/chocolate_flow"));
		FluidRenderHandlerRegistry.INSTANCE.register(AllFluids.CHOCOLATE.get().getSource(), chocolateHandler);
		FluidRenderHandlerRegistry.INSTANCE.register(AllFluids.CHOCOLATE.get().getFlowing(), chocolateHandler);

		ClientSpriteRegistryCallback.event(TextureAtlas.LOCATION_BLOCKS).register((atlasTexture, registry) -> {
			registry.register(new ResourceLocation("create:fluid/honey_still"));
			registry.register(new ResourceLocation("create:fluid/honey_flow"));
			registry.register(new ResourceLocation("create:fluid/chocolate_still"));
			registry.register(new ResourceLocation("create:fluid/chocolate_flow"));
			registry.register(new ResourceLocation("create:fluid/tea_still"));
			registry.register(new ResourceLocation("create:fluid/tea_flow"));
			registry.register(new ResourceLocation("create:fluid/potion_still"));
			registry.register(new ResourceLocation("create:fluid/potion_flow"));
		});
	}

	public static void invalidateRenderers() {
		BUFFER_CACHE.invalidate();

		SCHEMATIC_HANDLER.updateRenderers();
		ContraptionRenderDispatcher.reset();
	}

	public static void checkGraphicsFanciness() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null)
			return;

		if (mc.options.graphicsMode().get() != GraphicsStatus.FABULOUS)
			return;

		if (AllConfigs.CLIENT.ignoreFabulousWarning.get())
			return;

		MutableComponent text = ComponentUtils.wrapInSquareBrackets(Components.literal("WARN"))
			.withStyle(ChatFormatting.GOLD)
			.append(Components.literal(
				" Some of Create's visual features will not be available while Fabulous graphics are enabled!"))
			.withStyle(style -> style
				.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/create dismissFabulousWarning"))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					Components.literal("Click here to disable this warning"))));

		mc.player.displayClientMessage(text, false);
	}

}
