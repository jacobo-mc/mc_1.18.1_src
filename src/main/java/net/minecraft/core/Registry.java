package net.minecraft.core;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.FloatProviderType;
import net.minecraft.util.valueproviders.IntProviderType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSourceType;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSizeType;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElementType;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraft.world.level.levelgen.heightproviders.HeightProviderType;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType;
import net.minecraft.world.level.storage.loot.providers.score.ScoreboardNameProviders;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Registry<T> implements Keyable, IdMap<T> {
   protected static final Logger LOGGER = LogManager.getLogger();
   private static final Map<ResourceLocation, Supplier<?>> LOADERS = Maps.newLinkedHashMap();
   public static final ResourceLocation ROOT_REGISTRY_NAME = new ResourceLocation("root");
   protected static final WritableRegistry<WritableRegistry<?>> WRITABLE_REGISTRY = new MappedRegistry<>(createRegistryKey("root"), Lifecycle.experimental());
   public static final Registry<? extends Registry<?>> REGISTRY = WRITABLE_REGISTRY;
   public static final ResourceKey<Registry<SoundEvent>> SOUND_EVENT_REGISTRY = createRegistryKey("sound_event");
   public static final ResourceKey<Registry<Fluid>> FLUID_REGISTRY = createRegistryKey("fluid");
   public static final ResourceKey<Registry<MobEffect>> MOB_EFFECT_REGISTRY = createRegistryKey("mob_effect");
   public static final ResourceKey<Registry<Block>> BLOCK_REGISTRY = createRegistryKey("block");
   public static final ResourceKey<Registry<Enchantment>> ENCHANTMENT_REGISTRY = createRegistryKey("enchantment");
   public static final ResourceKey<Registry<EntityType<?>>> ENTITY_TYPE_REGISTRY = createRegistryKey("entity_type");
   public static final ResourceKey<Registry<Item>> ITEM_REGISTRY = createRegistryKey("item");
   public static final ResourceKey<Registry<Potion>> POTION_REGISTRY = createRegistryKey("potion");
   public static final ResourceKey<Registry<ParticleType<?>>> PARTICLE_TYPE_REGISTRY = createRegistryKey("particle_type");
   public static final ResourceKey<Registry<BlockEntityType<?>>> BLOCK_ENTITY_TYPE_REGISTRY = createRegistryKey("block_entity_type");
   public static final ResourceKey<Registry<Motive>> MOTIVE_REGISTRY = createRegistryKey("motive");
   public static final ResourceKey<Registry<ResourceLocation>> CUSTOM_STAT_REGISTRY = createRegistryKey("custom_stat");
   public static final ResourceKey<Registry<ChunkStatus>> CHUNK_STATUS_REGISTRY = createRegistryKey("chunk_status");
   public static final ResourceKey<Registry<RuleTestType<?>>> RULE_TEST_REGISTRY = createRegistryKey("rule_test");
   public static final ResourceKey<Registry<PosRuleTestType<?>>> POS_RULE_TEST_REGISTRY = createRegistryKey("pos_rule_test");
   public static final ResourceKey<Registry<MenuType<?>>> MENU_REGISTRY = createRegistryKey("menu");
   public static final ResourceKey<Registry<RecipeType<?>>> RECIPE_TYPE_REGISTRY = createRegistryKey("recipe_type");
   public static final ResourceKey<Registry<RecipeSerializer<?>>> RECIPE_SERIALIZER_REGISTRY = createRegistryKey("recipe_serializer");
   public static final ResourceKey<Registry<Attribute>> ATTRIBUTE_REGISTRY = createRegistryKey("attribute");
   public static final ResourceKey<Registry<GameEvent>> GAME_EVENT_REGISTRY = createRegistryKey("game_event");
   public static final ResourceKey<Registry<PositionSourceType<?>>> POSITION_SOURCE_TYPE_REGISTRY = createRegistryKey("position_source_type");
   public static final ResourceKey<Registry<StatType<?>>> STAT_TYPE_REGISTRY = createRegistryKey("stat_type");
   public static final ResourceKey<Registry<VillagerType>> VILLAGER_TYPE_REGISTRY = createRegistryKey("villager_type");
   public static final ResourceKey<Registry<VillagerProfession>> VILLAGER_PROFESSION_REGISTRY = createRegistryKey("villager_profession");
   public static final ResourceKey<Registry<PoiType>> POINT_OF_INTEREST_TYPE_REGISTRY = createRegistryKey("point_of_interest_type");
   public static final ResourceKey<Registry<MemoryModuleType<?>>> MEMORY_MODULE_TYPE_REGISTRY = createRegistryKey("memory_module_type");
   public static final ResourceKey<Registry<SensorType<?>>> SENSOR_TYPE_REGISTRY = createRegistryKey("sensor_type");
   public static final ResourceKey<Registry<Schedule>> SCHEDULE_REGISTRY = createRegistryKey("schedule");
   public static final ResourceKey<Registry<Activity>> ACTIVITY_REGISTRY = createRegistryKey("activity");
   public static final ResourceKey<Registry<LootPoolEntryType>> LOOT_ENTRY_REGISTRY = createRegistryKey("loot_pool_entry_type");
   public static final ResourceKey<Registry<LootItemFunctionType>> LOOT_FUNCTION_REGISTRY = createRegistryKey("loot_function_type");
   public static final ResourceKey<Registry<LootItemConditionType>> LOOT_ITEM_REGISTRY = createRegistryKey("loot_condition_type");
   public static final ResourceKey<Registry<LootNumberProviderType>> LOOT_NUMBER_PROVIDER_REGISTRY = createRegistryKey("loot_number_provider_type");
   public static final ResourceKey<Registry<LootNbtProviderType>> LOOT_NBT_PROVIDER_REGISTRY = createRegistryKey("loot_nbt_provider_type");
   public static final ResourceKey<Registry<LootScoreProviderType>> LOOT_SCORE_PROVIDER_REGISTRY = createRegistryKey("loot_score_provider_type");
   public static final ResourceKey<Registry<DimensionType>> DIMENSION_TYPE_REGISTRY = createRegistryKey("dimension_type");
   public static final ResourceKey<Registry<Level>> DIMENSION_REGISTRY = createRegistryKey("dimension");
   public static final ResourceKey<Registry<LevelStem>> LEVEL_STEM_REGISTRY = createRegistryKey("dimension");
   public static final DefaultedRegistry<GameEvent> GAME_EVENT = registerDefaulted(GAME_EVENT_REGISTRY, "step", () -> {
      return GameEvent.STEP;
   });
   public static final Registry<SoundEvent> SOUND_EVENT = registerSimple(SOUND_EVENT_REGISTRY, () -> {
      return SoundEvents.ITEM_PICKUP;
   });
   public static final DefaultedRegistry<Fluid> FLUID = registerDefaulted(FLUID_REGISTRY, "empty", () -> {
      return Fluids.EMPTY;
   });
   public static final Registry<MobEffect> MOB_EFFECT = registerSimple(MOB_EFFECT_REGISTRY, () -> {
      return MobEffects.LUCK;
   });
   public static final DefaultedRegistry<Block> BLOCK = registerDefaulted(BLOCK_REGISTRY, "air", () -> {
      return Blocks.AIR;
   });
   public static final Registry<Enchantment> ENCHANTMENT = registerSimple(ENCHANTMENT_REGISTRY, () -> {
      return Enchantments.BLOCK_FORTUNE;
   });
   public static final DefaultedRegistry<EntityType<?>> ENTITY_TYPE = registerDefaulted(ENTITY_TYPE_REGISTRY, "pig", () -> {
      return EntityType.PIG;
   });
   public static final DefaultedRegistry<Item> ITEM = registerDefaulted(ITEM_REGISTRY, "air", () -> {
      return Items.AIR;
   });
   public static final DefaultedRegistry<Potion> POTION = registerDefaulted(POTION_REGISTRY, "empty", () -> {
      return Potions.EMPTY;
   });
   public static final Registry<ParticleType<?>> PARTICLE_TYPE = registerSimple(PARTICLE_TYPE_REGISTRY, () -> {
      return ParticleTypes.BLOCK;
   });
   public static final Registry<BlockEntityType<?>> BLOCK_ENTITY_TYPE = registerSimple(BLOCK_ENTITY_TYPE_REGISTRY, () -> {
      return BlockEntityType.FURNACE;
   });
   public static final DefaultedRegistry<Motive> MOTIVE = registerDefaulted(MOTIVE_REGISTRY, "kebab", () -> {
      return Motive.KEBAB;
   });
   public static final Registry<ResourceLocation> CUSTOM_STAT = registerSimple(CUSTOM_STAT_REGISTRY, () -> {
      return Stats.JUMP;
   });
   public static final DefaultedRegistry<ChunkStatus> CHUNK_STATUS = registerDefaulted(CHUNK_STATUS_REGISTRY, "empty", () -> {
      return ChunkStatus.EMPTY;
   });
   public static final Registry<RuleTestType<?>> RULE_TEST = registerSimple(RULE_TEST_REGISTRY, () -> {
      return RuleTestType.ALWAYS_TRUE_TEST;
   });
   public static final Registry<PosRuleTestType<?>> POS_RULE_TEST = registerSimple(POS_RULE_TEST_REGISTRY, () -> {
      return PosRuleTestType.ALWAYS_TRUE_TEST;
   });
   public static final Registry<MenuType<?>> MENU = registerSimple(MENU_REGISTRY, () -> {
      return MenuType.ANVIL;
   });
   public static final Registry<RecipeType<?>> RECIPE_TYPE = registerSimple(RECIPE_TYPE_REGISTRY, () -> {
      return RecipeType.CRAFTING;
   });
   public static final Registry<RecipeSerializer<?>> RECIPE_SERIALIZER = registerSimple(RECIPE_SERIALIZER_REGISTRY, () -> {
      return RecipeSerializer.SHAPELESS_RECIPE;
   });
   public static final Registry<Attribute> ATTRIBUTE = registerSimple(ATTRIBUTE_REGISTRY, () -> {
      return Attributes.LUCK;
   });
   public static final Registry<PositionSourceType<?>> POSITION_SOURCE_TYPE = registerSimple(POSITION_SOURCE_TYPE_REGISTRY, () -> {
      return PositionSourceType.BLOCK;
   });
   public static final Registry<StatType<?>> STAT_TYPE = registerSimple(STAT_TYPE_REGISTRY, () -> {
      return Stats.ITEM_USED;
   });
   public static final DefaultedRegistry<VillagerType> VILLAGER_TYPE = registerDefaulted(VILLAGER_TYPE_REGISTRY, "plains", () -> {
      return VillagerType.PLAINS;
   });
   public static final DefaultedRegistry<VillagerProfession> VILLAGER_PROFESSION = registerDefaulted(VILLAGER_PROFESSION_REGISTRY, "none", () -> {
      return VillagerProfession.NONE;
   });
   public static final DefaultedRegistry<PoiType> POINT_OF_INTEREST_TYPE = registerDefaulted(POINT_OF_INTEREST_TYPE_REGISTRY, "unemployed", () -> {
      return PoiType.UNEMPLOYED;
   });
   public static final DefaultedRegistry<MemoryModuleType<?>> MEMORY_MODULE_TYPE = registerDefaulted(MEMORY_MODULE_TYPE_REGISTRY, "dummy", () -> {
      return MemoryModuleType.DUMMY;
   });
   public static final DefaultedRegistry<SensorType<?>> SENSOR_TYPE = registerDefaulted(SENSOR_TYPE_REGISTRY, "dummy", () -> {
      return SensorType.DUMMY;
   });
   public static final Registry<Schedule> SCHEDULE = registerSimple(SCHEDULE_REGISTRY, () -> {
      return Schedule.EMPTY;
   });
   public static final Registry<Activity> ACTIVITY = registerSimple(ACTIVITY_REGISTRY, () -> {
      return Activity.IDLE;
   });
   public static final Registry<LootPoolEntryType> LOOT_POOL_ENTRY_TYPE = registerSimple(LOOT_ENTRY_REGISTRY, () -> {
      return LootPoolEntries.EMPTY;
   });
   public static final Registry<LootItemFunctionType> LOOT_FUNCTION_TYPE = registerSimple(LOOT_FUNCTION_REGISTRY, () -> {
      return LootItemFunctions.SET_COUNT;
   });
   public static final Registry<LootItemConditionType> LOOT_CONDITION_TYPE = registerSimple(LOOT_ITEM_REGISTRY, () -> {
      return LootItemConditions.INVERTED;
   });
   public static final Registry<LootNumberProviderType> LOOT_NUMBER_PROVIDER_TYPE = registerSimple(LOOT_NUMBER_PROVIDER_REGISTRY, () -> {
      return NumberProviders.CONSTANT;
   });
   public static final Registry<LootNbtProviderType> LOOT_NBT_PROVIDER_TYPE = registerSimple(LOOT_NBT_PROVIDER_REGISTRY, () -> {
      return NbtProviders.CONTEXT;
   });
   public static final Registry<LootScoreProviderType> LOOT_SCORE_PROVIDER_TYPE = registerSimple(LOOT_SCORE_PROVIDER_REGISTRY, () -> {
      return ScoreboardNameProviders.CONTEXT;
   });
   public static final ResourceKey<Registry<FloatProviderType<?>>> FLOAT_PROVIDER_TYPE_REGISTRY = createRegistryKey("float_provider_type");
   public static final Registry<FloatProviderType<?>> FLOAT_PROVIDER_TYPES = registerSimple(FLOAT_PROVIDER_TYPE_REGISTRY, () -> {
      return FloatProviderType.CONSTANT;
   });
   public static final ResourceKey<Registry<IntProviderType<?>>> INT_PROVIDER_TYPE_REGISTRY = createRegistryKey("int_provider_type");
   public static final Registry<IntProviderType<?>> INT_PROVIDER_TYPES = registerSimple(INT_PROVIDER_TYPE_REGISTRY, () -> {
      return IntProviderType.CONSTANT;
   });
   public static final ResourceKey<Registry<HeightProviderType<?>>> HEIGHT_PROVIDER_TYPE_REGISTRY = createRegistryKey("height_provider_type");
   public static final Registry<HeightProviderType<?>> HEIGHT_PROVIDER_TYPES = registerSimple(HEIGHT_PROVIDER_TYPE_REGISTRY, () -> {
      return HeightProviderType.CONSTANT;
   });
   public static final ResourceKey<Registry<BlockPredicateType<?>>> BLOCK_PREDICATE_TYPE_REGISTRY = createRegistryKey("block_predicate_type");
   public static final Registry<BlockPredicateType<?>> BLOCK_PREDICATE_TYPES = registerSimple(BLOCK_PREDICATE_TYPE_REGISTRY, () -> {
      return BlockPredicateType.NOT;
   });
   public static final ResourceKey<Registry<NoiseGeneratorSettings>> NOISE_GENERATOR_SETTINGS_REGISTRY = createRegistryKey("worldgen/noise_settings");
   public static final ResourceKey<Registry<ConfiguredWorldCarver<?>>> CONFIGURED_CARVER_REGISTRY = createRegistryKey("worldgen/configured_carver");
   public static final ResourceKey<Registry<ConfiguredFeature<?, ?>>> CONFIGURED_FEATURE_REGISTRY = createRegistryKey("worldgen/configured_feature");
   public static final ResourceKey<Registry<PlacedFeature>> PLACED_FEATURE_REGISTRY = createRegistryKey("worldgen/placed_feature");
   public static final ResourceKey<Registry<ConfiguredStructureFeature<?, ?>>> CONFIGURED_STRUCTURE_FEATURE_REGISTRY = createRegistryKey("worldgen/configured_structure_feature");
   public static final ResourceKey<Registry<StructureProcessorList>> PROCESSOR_LIST_REGISTRY = createRegistryKey("worldgen/processor_list");
   public static final ResourceKey<Registry<StructureTemplatePool>> TEMPLATE_POOL_REGISTRY = createRegistryKey("worldgen/template_pool");
   public static final ResourceKey<Registry<Biome>> BIOME_REGISTRY = createRegistryKey("worldgen/biome");
   public static final ResourceKey<Registry<NormalNoise.NoiseParameters>> NOISE_REGISTRY = createRegistryKey("worldgen/noise");
   public static final ResourceKey<Registry<WorldCarver<?>>> CARVER_REGISTRY = createRegistryKey("worldgen/carver");
   public static final Registry<WorldCarver<?>> CARVER = registerSimple(CARVER_REGISTRY, () -> {
      return WorldCarver.CAVE;
   });
   public static final ResourceKey<Registry<Feature<?>>> FEATURE_REGISTRY = createRegistryKey("worldgen/feature");
   public static final Registry<Feature<?>> FEATURE = registerSimple(FEATURE_REGISTRY, () -> {
      return Feature.ORE;
   });
   public static final ResourceKey<Registry<StructureFeature<?>>> STRUCTURE_FEATURE_REGISTRY = createRegistryKey("worldgen/structure_feature");
   public static final Registry<StructureFeature<?>> STRUCTURE_FEATURE = registerSimple(STRUCTURE_FEATURE_REGISTRY, () -> {
      return StructureFeature.MINESHAFT;
   });
   public static final ResourceKey<Registry<StructurePieceType>> STRUCTURE_PIECE_REGISTRY = createRegistryKey("worldgen/structure_piece");
   public static final Registry<StructurePieceType> STRUCTURE_PIECE = registerSimple(STRUCTURE_PIECE_REGISTRY, () -> {
      return StructurePieceType.MINE_SHAFT_ROOM;
   });
   public static final ResourceKey<Registry<PlacementModifierType<?>>> PLACEMENT_MODIFIER_REGISTRY = createRegistryKey("worldgen/placement_modifier_type");
   public static final Registry<PlacementModifierType<?>> PLACEMENT_MODIFIERS = registerSimple(PLACEMENT_MODIFIER_REGISTRY, () -> {
      return PlacementModifierType.COUNT;
   });
   public static final ResourceKey<Registry<BlockStateProviderType<?>>> BLOCK_STATE_PROVIDER_TYPE_REGISTRY = createRegistryKey("worldgen/block_state_provider_type");
   public static final ResourceKey<Registry<FoliagePlacerType<?>>> FOLIAGE_PLACER_TYPE_REGISTRY = createRegistryKey("worldgen/foliage_placer_type");
   public static final ResourceKey<Registry<TrunkPlacerType<?>>> TRUNK_PLACER_TYPE_REGISTRY = createRegistryKey("worldgen/trunk_placer_type");
   public static final ResourceKey<Registry<TreeDecoratorType<?>>> TREE_DECORATOR_TYPE_REGISTRY = createRegistryKey("worldgen/tree_decorator_type");
   public static final ResourceKey<Registry<FeatureSizeType<?>>> FEATURE_SIZE_TYPE_REGISTRY = createRegistryKey("worldgen/feature_size_type");
   public static final ResourceKey<Registry<Codec<? extends BiomeSource>>> BIOME_SOURCE_REGISTRY = createRegistryKey("worldgen/biome_source");
   public static final ResourceKey<Registry<Codec<? extends ChunkGenerator>>> CHUNK_GENERATOR_REGISTRY = createRegistryKey("worldgen/chunk_generator");
   public static final ResourceKey<Registry<Codec<? extends SurfaceRules.ConditionSource>>> CONDITION_REGISTRY = createRegistryKey("worldgen/material_condition");
   public static final ResourceKey<Registry<Codec<? extends SurfaceRules.RuleSource>>> RULE_REGISTRY = createRegistryKey("worldgen/material_rule");
   public static final ResourceKey<Registry<StructureProcessorType<?>>> STRUCTURE_PROCESSOR_REGISTRY = createRegistryKey("worldgen/structure_processor");
   public static final ResourceKey<Registry<StructurePoolElementType<?>>> STRUCTURE_POOL_ELEMENT_REGISTRY = createRegistryKey("worldgen/structure_pool_element");
   public static final Registry<BlockStateProviderType<?>> BLOCKSTATE_PROVIDER_TYPES = registerSimple(BLOCK_STATE_PROVIDER_TYPE_REGISTRY, () -> {
      return BlockStateProviderType.SIMPLE_STATE_PROVIDER;
   });
   public static final Registry<FoliagePlacerType<?>> FOLIAGE_PLACER_TYPES = registerSimple(FOLIAGE_PLACER_TYPE_REGISTRY, () -> {
      return FoliagePlacerType.BLOB_FOLIAGE_PLACER;
   });
   public static final Registry<TrunkPlacerType<?>> TRUNK_PLACER_TYPES = registerSimple(TRUNK_PLACER_TYPE_REGISTRY, () -> {
      return TrunkPlacerType.STRAIGHT_TRUNK_PLACER;
   });
   public static final Registry<TreeDecoratorType<?>> TREE_DECORATOR_TYPES = registerSimple(TREE_DECORATOR_TYPE_REGISTRY, () -> {
      return TreeDecoratorType.LEAVE_VINE;
   });
   public static final Registry<FeatureSizeType<?>> FEATURE_SIZE_TYPES = registerSimple(FEATURE_SIZE_TYPE_REGISTRY, () -> {
      return FeatureSizeType.TWO_LAYERS_FEATURE_SIZE;
   });
   public static final Registry<Codec<? extends BiomeSource>> BIOME_SOURCE = registerSimple(BIOME_SOURCE_REGISTRY, Lifecycle.stable(), () -> {
      return BiomeSource.CODEC;
   });
   public static final Registry<Codec<? extends ChunkGenerator>> CHUNK_GENERATOR = registerSimple(CHUNK_GENERATOR_REGISTRY, Lifecycle.stable(), () -> {
      return ChunkGenerator.CODEC;
   });
   public static final Registry<Codec<? extends SurfaceRules.ConditionSource>> CONDITION = registerSimple(CONDITION_REGISTRY, SurfaceRules.ConditionSource::bootstrap);
   public static final Registry<Codec<? extends SurfaceRules.RuleSource>> RULE = registerSimple(RULE_REGISTRY, SurfaceRules.RuleSource::bootstrap);
   public static final Registry<StructureProcessorType<?>> STRUCTURE_PROCESSOR = registerSimple(STRUCTURE_PROCESSOR_REGISTRY, () -> {
      return StructureProcessorType.BLOCK_IGNORE;
   });
   public static final Registry<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT = registerSimple(STRUCTURE_POOL_ELEMENT_REGISTRY, () -> {
      return StructurePoolElementType.EMPTY;
   });
   private final ResourceKey<? extends Registry<T>> key;
   private final Lifecycle lifecycle;

   private static <T> ResourceKey<Registry<T>> createRegistryKey(String p_122979_) {
      return ResourceKey.createRegistryKey(new ResourceLocation(p_122979_));
   }

   public static <T extends WritableRegistry<?>> void checkRegistry(WritableRegistry<T> p_122970_) {
      p_122970_.forEach((p_194585_) -> {
         if (p_194585_.keySet().isEmpty()) {
            Util.logAndPauseIfInIde("Registry '" + p_122970_.getKey(p_194585_) + "' was empty after loading");
         }

         if (p_194585_ instanceof DefaultedRegistry) {
            ResourceLocation resourcelocation = ((DefaultedRegistry)p_194585_).getDefaultKey();
            Validate.notNull(p_194585_.get(resourcelocation), "Missing default of DefaultedMappedRegistry: " + resourcelocation);
         }

      });
   }

   private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> p_123000_, Supplier<T> p_123001_) {
      return registerSimple(p_123000_, Lifecycle.experimental(), p_123001_);
   }

   private static <T> DefaultedRegistry<T> registerDefaulted(ResourceKey<? extends Registry<T>> p_122996_, String p_122997_, Supplier<T> p_122998_) {
      return registerDefaulted(p_122996_, p_122997_, Lifecycle.experimental(), p_122998_);
   }

   private static <T> Registry<T> registerSimple(ResourceKey<? extends Registry<T>> p_122982_, Lifecycle p_122983_, Supplier<T> p_122984_) {
      return internalRegister(p_122982_, new MappedRegistry<>(p_122982_, p_122983_), p_122984_, p_122983_);
   }

   private static <T> DefaultedRegistry<T> registerDefaulted(ResourceKey<? extends Registry<T>> p_122991_, String p_122992_, Lifecycle p_122993_, Supplier<T> p_122994_) {
      return internalRegister(p_122991_, new DefaultedRegistry<>(p_122992_, p_122991_, p_122993_), p_122994_, p_122993_);
   }

   private static <T, R extends WritableRegistry<T>> R internalRegister(ResourceKey<? extends Registry<T>> p_122986_, R p_122987_, Supplier<T> p_122988_, Lifecycle p_122989_) {
      ResourceLocation resourcelocation = p_122986_.location();
      LOADERS.put(resourcelocation, p_122988_);
      WritableRegistry<R> writableregistry = (WritableRegistry<R>)WRITABLE_REGISTRY;
      return (R)writableregistry.register((ResourceKey)p_122986_, p_122987_, p_122989_);
   }

   protected Registry(ResourceKey<? extends Registry<T>> p_122920_, Lifecycle p_122921_) {
      Bootstrap.checkBootstrapCalled(() -> {
         return "registry " + p_122920_;
      });
      this.key = p_122920_;
      this.lifecycle = p_122921_;
   }

   public ResourceKey<? extends Registry<T>> key() {
      return this.key;
   }

   public Lifecycle lifecycle() {
      return this.lifecycle;
   }

   public String toString() {
      return "Registry[" + this.key + " (" + this.lifecycle + ")]";
   }

   public Codec<T> byNameCodec() {
      Codec<T> codec = ResourceLocation.CODEC.flatXmap((p_194590_) -> {
         return Optional.ofNullable(this.get(p_194590_)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error("Unknown registry key in " + this.key + ": " + p_194590_);
         });
      }, (p_194601_) -> {
         return this.getResourceKey(p_194601_).map(ResourceKey::location).map(DataResult::success).orElseGet(() -> {
            return DataResult.error("Unknown registry element in " + this.key + ":" + p_194601_);
         });
      });
      Codec<T> codec1 = ExtraCodecs.idResolverCodec((p_194599_) -> {
         return this.getResourceKey(p_194599_).isPresent() ? this.getId(p_194599_) : -1;
      }, this::byId, -1);
      return ExtraCodecs.overrideLifecycle(ExtraCodecs.orCompressed(codec, codec1), this::lifecycle, (p_194592_) -> {
         return this.lifecycle;
      });
   }

   public <U> Stream<U> keys(DynamicOps<U> p_123030_) {
      return this.keySet().stream().map((p_194578_) -> {
         return p_123030_.createString(p_194578_.toString());
      });
   }

   @Nullable
   public abstract ResourceLocation getKey(T p_123006_);

   public abstract Optional<ResourceKey<T>> getResourceKey(T p_123008_);

   public abstract int getId(@Nullable T p_122977_);

   @Nullable
   public abstract T get(@Nullable ResourceKey<T> p_122980_);

   @Nullable
   public abstract T get(@Nullable ResourceLocation p_123002_);

   public abstract Lifecycle lifecycle(T p_123012_);

   public abstract Lifecycle elementsLifecycle();

   public Optional<T> getOptional(@Nullable ResourceLocation p_123007_) {
      return Optional.ofNullable(this.get(p_123007_));
   }

   public Optional<T> getOptional(@Nullable ResourceKey<T> p_123010_) {
      return Optional.ofNullable(this.get(p_123010_));
   }

   public T getOrThrow(ResourceKey<T> p_123014_) {
      T t = this.get(p_123014_);
      if (t == null) {
         throw new IllegalStateException("Missing key in " + this.key + ": " + p_123014_);
      } else {
         return t;
      }
   }

   public abstract Set<ResourceLocation> keySet();

   public abstract Set<Entry<ResourceKey<T>, T>> entrySet();

   @Nullable
   public abstract T getRandom(Random p_175464_);

   public Stream<T> stream() {
      return StreamSupport.stream(this.spliterator(), false);
   }

   public abstract boolean containsKey(ResourceLocation p_123011_);

   public abstract boolean containsKey(ResourceKey<T> p_175475_);

   public static <T> T register(Registry<? super T> p_122962_, String p_122963_, T p_122964_) {
      return register(p_122962_, new ResourceLocation(p_122963_), p_122964_);
   }

   public static <V, T extends V> T register(Registry<V> p_122966_, ResourceLocation p_122967_, T p_122968_) {
      return register(p_122966_, ResourceKey.create(p_122966_.key, p_122967_), p_122968_);
   }

   public static <V, T extends V> T register(Registry<V> p_194580_, ResourceKey<V> p_194581_, T p_194582_) {
      return (T)((WritableRegistry)p_194580_).register(p_194581_, p_194582_, Lifecycle.stable());
   }

   public static <V, T extends V> T registerMapping(Registry<V> p_122957_, int p_122958_, String p_122959_, T p_122960_) {
      return (T)((WritableRegistry)p_122957_).registerMapping(p_122958_, ResourceKey.create(p_122957_.key, new ResourceLocation(p_122959_)), p_122960_, Lifecycle.stable());
   }

   static {
      BuiltinRegistries.bootstrap();
      LOADERS.forEach((p_194587_, p_194588_) -> {
         if (p_194588_.get() == null) {
            LOGGER.error("Unable to bootstrap registry '{}'", (Object)p_194587_);
         }

      });
      checkRegistry(WRITABLE_REGISTRY);
   }
}
