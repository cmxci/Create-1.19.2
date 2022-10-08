package com.simibubi.create;

import java.util.List;

import javax.annotation.Nullable;

import com.simibubi.create.AllTags.AllFluidTags;
import com.simibubi.create.content.contraptions.fluids.VirtualFluid;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.palettes.AllPaletteStoneTypes;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.tterrag.registrate.fabric.EnvExecutor;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import com.tterrag.registrate.util.entry.FluidEntry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.EmptyItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.FullItemFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import static net.minecraft.world.item.Items.BUCKET;
import static net.minecraft.world.item.Items.GLASS_BOTTLE;
import static net.minecraft.world.item.Items.HONEY_BOTTLE;

@SuppressWarnings("UnstableApiUsage")
public class AllFluids {
	// Fabric: since a honey block is 4 bottles, we can't use the default 1/3 (27000)
	// we can't make a block take 108000, since then it can't fit in the basin
	public static final long HONEY_BOTTLE_AMOUNT = FluidConstants.BLOCK / 4;

	private static final CreateRegistrate REGISTRATE = Create.registrate();

	public static final FluidEntry<PotionFluid> POTION =
			REGISTRATE.virtualFluid("potion", /*PotionFluidAttributes::new,*/ PotionFluid::new)
					.lang("Potion")
					.onRegister(potion -> {
						Fluid still = potion.getSource();
						Fluid flowing = potion.getFlowing();
						PotionFluidVariantAttributeHandler handler = new PotionFluidVariantAttributeHandler();
						FluidVariantAttributes.register(still, handler);
						FluidVariantAttributes.register(flowing, handler);
						// evil. why do we need this like this only here.
						EnvExecutor.runWhenOn(EnvType.CLIENT, () -> new Runnable() {
							@Override
							@Environment(EnvType.CLIENT)
							public void run() {
								PotionFluidVariantRenderHandler handler = new PotionFluidVariantRenderHandler();
								FluidVariantRendering.register(still, handler);
								FluidVariantRendering.register(flowing, handler);
								// TODO: remove when Registrate is fixed
								SimpleFluidRenderHandler handler2 = new SimpleFluidRenderHandler(new ResourceLocation("create:fluid/potion_still"), new ResourceLocation("create:fluid/potion_flow"));
								FluidRenderHandlerRegistry.INSTANCE.register(still, handler2);
								FluidRenderHandlerRegistry.INSTANCE.register(flowing, handler2);
							}
						});
					})
					.register();

	public static final FluidEntry<VirtualFluid> TEA = REGISTRATE.virtualFluid("tea")
			.lang("Builder's Tea")
			.tag(AllTags.forgeFluidTag("tea"))
			.fluidRenderingAttributes(() -> new SimpleFluidRenderHandler(new ResourceLocation("create:fluid/tea_still"), new ResourceLocation("create:fluid/tea_flow"))) // TODO: remove when Registrate is fixed
			.onRegisterAfter(Registry.ITEM_REGISTRY, tea -> {
				Fluid still = tea.getSource();
				Fluid flowing = tea.getFlowing();
				FluidStorage.combinedItemApiProvider(AllItems.BUILDERS_TEA.get()).register(context ->
						new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(still), FluidConstants.BOTTLE));
				FluidStorage.combinedItemApiProvider(GLASS_BOTTLE).register(context ->
						new EmptyItemFluidStorage(context, bottle -> ItemVariant.of(AllItems.BUILDERS_TEA.get()), still, FluidConstants.BOTTLE));

				FluidVariantAttributes.register(still, new TeaFluidVariantAttributeHandler());
				FluidVariantAttributes.register(flowing, new TeaFluidVariantAttributeHandler());
			})
			.register();

	public static final FluidEntry<SimpleFlowableFluid.Flowing> HONEY =
			REGISTRATE.standardFluid("honey"/*, NoColorFluidAttributes::new*/)
					.lang("Honey")
//					.attributes(b -> b.viscosity(2000)
//							.density(1400))
					.fluidProperties(p -> p.levelDecreasePerBlock(2)
							.tickRate(25)
							.flowSpeed(3)
							.blastResistance(100f))
					.tag(AllFluidTags.HONEY.tag)
					.source(SimpleFlowableFluid.Source::new) // TODO: remove when Registrate fixes FluidBuilder
					.bucket()
					.tag(AllTags.forgeItemTag("buckets/honey"))
					.build()
//					.fluidRenderingAttributes(() -> SimpleFluidRenderHandler::new)
					.fluidRenderingAttributes(() -> new SimpleFluidRenderHandler(new ResourceLocation("create:fluid/honey_still"), new ResourceLocation("create:fluid/honey_flow"))) // TODO: remove when Registrate is fixed
					.onRegisterAfter(Registry.ITEM_REGISTRY, honey -> {
						Fluid source = honey.getSource();
						FluidStorage.combinedItemApiProvider(HONEY_BOTTLE).register(context ->
								new FullItemFluidStorage(context, bottle -> ItemVariant.of(GLASS_BOTTLE), FluidVariant.of(source), HONEY_BOTTLE_AMOUNT));
						FluidStorage.combinedItemApiProvider(GLASS_BOTTLE).register(context ->
								new EmptyItemFluidStorage(context, bottle -> ItemVariant.of(HONEY_BOTTLE), source, HONEY_BOTTLE_AMOUNT));
						FluidStorage.combinedItemApiProvider(source.getBucket()).register(context ->
								new FullItemFluidStorage(context, bucket -> ItemVariant.of(BUCKET), FluidVariant.of(source), FluidConstants.BUCKET));
						FluidStorage.combinedItemApiProvider(BUCKET).register(context ->
								new EmptyItemFluidStorage(context, bucket -> ItemVariant.of(source.getBucket()), source, FluidConstants.BUCKET));

						FluidVariantAttributeHandler handler = new FluidNameAttributeHandler("block.create.honey");
						FluidVariantAttributes.register(honey, handler);
						FluidVariantAttributes.register(source, handler);
					})
					.register();

	public static final FluidEntry<SimpleFlowableFluid.Flowing> CHOCOLATE =
			REGISTRATE.standardFluid("chocolate"/*, NoColorFluidAttributes::new*/)
					.lang("Chocolate")
					.tag(AllTags.forgeFluidTag("chocolate"))
//					.attributes(b -> b.viscosity(1500)
//							.density(1400))
					.fluidProperties(p -> p.levelDecreasePerBlock(2)
							.tickRate(25)
							.flowSpeed(3)
							.blastResistance(100f))
//					.fluidRenderingAttributes(() -> SimpleFluidRenderHandler::new)
					.fluidRenderingAttributes(() -> new SimpleFluidRenderHandler(new ResourceLocation("create:fluid/chocolate_still"), new ResourceLocation("create:fluid/chocolate_flow"))) // TODO: remove when Registrate is fixed
					.onRegisterAfter(Registry.ITEM_REGISTRY, chocolate -> {
						Fluid source = chocolate.getSource();
						// transfer values
						FluidStorage.combinedItemApiProvider(source.getBucket()).register(context ->
								new FullItemFluidStorage(context, bucket -> ItemVariant.of(BUCKET), FluidVariant.of(source), FluidConstants.BUCKET));
						FluidStorage.combinedItemApiProvider(BUCKET).register(context ->
								new EmptyItemFluidStorage(context, bucket -> ItemVariant.of(source.getBucket()), source, FluidConstants.BUCKET));

						FluidVariantAttributeHandler handler = new FluidNameAttributeHandler("block.create.chocolate");
						FluidVariantAttributes.register(chocolate, handler);
						FluidVariantAttributes.register(source, handler);
					})
					.register();

	// Load this class

	public static void register() {
		// TODO: remove this when Registrate is fixed
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

	@Nullable
	public static BlockState getLavaInteraction(FluidState fluidState) {
		Fluid fluid = fluidState.getType();
		if (fluid.isSame(HONEY.get()))
			return AllPaletteStoneTypes.LIMESTONE.getBaseBlock()
					.get()
					.defaultBlockState();
		if (fluid.isSame(CHOCOLATE.get()))
			return AllPaletteStoneTypes.SCORIA.getBaseBlock()
					.get()
					.defaultBlockState();
		return null;
	}

//	/**
//	 * Removing alpha from tint prevents optifine from forcibly applying biome
//	 * colors to modded fluids (Makes translucent fluids disappear)
//	 */
//	private static class NoColorFluidAttributes extends FluidAttributes {
//
//		protected NoColorFluidAttributes(Builder builder, Fluid fluid) {
//			super(builder, fluid);
//		}
//
//		@Override
//		public int getColor(BlockAndTintGetter world, BlockPos pos) {
//			return 0x00ffffff;
//		}
//
//	}

	@Environment(EnvType.CLIENT)
	private static class PotionFluidVariantRenderHandler implements FluidVariantRenderHandler {
		@Override
		public int getColor(FluidVariant fluidVariant, @Nullable BlockAndTintGetter view, @Nullable BlockPos pos) {
			return PotionUtils.getColor(PotionUtils.getAllEffects(fluidVariant.getNbt())) | 0xff000000;
		}

		@Override
		public void appendTooltip(FluidVariant fluidVariant, List<Component> tooltip, TooltipFlag tooltipContext) {
			PotionFluidHandler.addPotionTooltip(fluidVariant, tooltip, 1);
		}
	}

	private static class PotionFluidVariantAttributeHandler implements FluidVariantAttributeHandler {
		@Override
		public Component getName(FluidVariant fluidVariant) {
			return Component.translatable(getTranslationKey(fluidVariant));
		}

		public String getTranslationKey(FluidVariant stack) {
			CompoundTag tag = stack.getNbt();
			if (tag == null)
				return "create.potion.invalid";
			ItemLike itemFromBottleType =
					PotionFluidHandler.itemFromBottleType(NBTHelper.readEnum(tag, "Bottle", BottleType.class));
			return PotionUtils.getPotion(tag)
					.getName(itemFromBottleType.asItem()
							.getDescriptionId() + ".effect.");
		}
	}

	private static class TeaFluidVariantAttributeHandler implements FluidVariantAttributeHandler {
		@Override
		public Component getName(FluidVariant fluidVariant) {
			return Component.translatable("fluid.create.tea");
		}
	}

	private record FluidNameAttributeHandler(String key) implements FluidVariantAttributeHandler {
		@Override
		public Component getName(FluidVariant fluidVariant) {
			return Component.translatable(this.key);
		}
	}
}
