package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StructureFeature<C extends FeatureConfiguration> {
   public static final BiMap<String, StructureFeature<?>> STRUCTURES_REGISTRY = HashBiMap.create();
   private static final Map<StructureFeature<?>, GenerationStep.Decoration> STEP = Maps.newHashMap();
   private static final Logger LOGGER = LogManager.getLogger();
   public static final StructureFeature<JigsawConfiguration> PILLAGER_OUTPOST = register("Pillager_Outpost", new PillagerOutpostFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
   public static final StructureFeature<MineshaftConfiguration> MINESHAFT = register("Mineshaft", new MineshaftFeature(MineshaftConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
   public static final StructureFeature<NoneFeatureConfiguration> WOODLAND_MANSION = register("Mansion", new WoodlandMansionFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
   public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE = register("Jungle_Pyramid", new JunglePyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
   public static final StructureFeature<NoneFeatureConfiguration> DESERT_PYRAMID = register("Desert_Pyramid", new DesertPyramidFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
   public static final StructureFeature<NoneFeatureConfiguration> IGLOO = register("Igloo", new IglooFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
   public static final StructureFeature<RuinedPortalConfiguration> RUINED_PORTAL = register("Ruined_Portal", new RuinedPortalFeature(RuinedPortalConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
   public static final StructureFeature<ShipwreckConfiguration> SHIPWRECK = register("Shipwreck", new ShipwreckFeature(ShipwreckConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
   public static final StructureFeature<NoneFeatureConfiguration> SWAMP_HUT = register("Swamp_Hut", new SwamplandHutFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
   public static final StructureFeature<NoneFeatureConfiguration> STRONGHOLD = register("Stronghold", new StrongholdFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.STRONGHOLDS);
   public static final StructureFeature<NoneFeatureConfiguration> OCEAN_MONUMENT = register("Monument", new OceanMonumentFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
   public static final StructureFeature<OceanRuinConfiguration> OCEAN_RUIN = register("Ocean_Ruin", new OceanRuinFeature(OceanRuinConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
   public static final StructureFeature<NoneFeatureConfiguration> NETHER_BRIDGE = register("Fortress", new NetherFortressFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION);
   public static final StructureFeature<NoneFeatureConfiguration> END_CITY = register("EndCity", new EndCityFeature(NoneFeatureConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
   public static final StructureFeature<ProbabilityFeatureConfiguration> BURIED_TREASURE = register("Buried_Treasure", new BuriedTreasureFeature(ProbabilityFeatureConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
   public static final StructureFeature<JigsawConfiguration> VILLAGE = register("Village", new VillageFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
   public static final StructureFeature<RangeConfiguration> NETHER_FOSSIL = register("Nether_Fossil", new NetherFossilFeature(RangeConfiguration.CODEC), GenerationStep.Decoration.UNDERGROUND_DECORATION);
   public static final StructureFeature<JigsawConfiguration> BASTION_REMNANT = register("Bastion_Remnant", new BastionFeature(JigsawConfiguration.CODEC), GenerationStep.Decoration.SURFACE_STRUCTURES);
   public static final List<StructureFeature<?>> NOISE_AFFECTING_FEATURES = ImmutableList.of(PILLAGER_OUTPOST, VILLAGE, NETHER_FOSSIL, STRONGHOLD);
   public static final int MAX_STRUCTURE_RANGE = 8;
   private final Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec;
   private final PieceGeneratorSupplier<C> pieceGenerator;
   private final PostPlacementProcessor postPlacementProcessor;

   private static <F extends StructureFeature<?>> F register(String p_67090_, F p_67091_, GenerationStep.Decoration p_67092_) {
      STRUCTURES_REGISTRY.put(p_67090_.toLowerCase(Locale.ROOT), p_67091_);
      STEP.put(p_67091_, p_67092_);
      return Registry.register(Registry.STRUCTURE_FEATURE, p_67090_.toLowerCase(Locale.ROOT), p_67091_);
   }

   public StructureFeature(Codec<C> p_197165_, PieceGeneratorSupplier<C> p_197166_) {
      this(p_197165_, p_197166_, PostPlacementProcessor.NONE);
   }

   public StructureFeature(Codec<C> p_197168_, PieceGeneratorSupplier<C> p_197169_, PostPlacementProcessor p_197170_) {
      this.configuredStructureCodec = p_197168_.fieldOf("config").xmap((p_67094_) -> {
         return new ConfiguredStructureFeature<>(this, p_67094_);
      }, (p_67064_) -> {
         return p_67064_.config;
      }).codec();
      this.pieceGenerator = p_197169_;
      this.postPlacementProcessor = p_197170_;
   }

   public GenerationStep.Decoration step() {
      return STEP.get(this);
   }

   public static void bootstrap() {
   }

   @Nullable
   public static StructureStart<?> loadStaticStart(StructurePieceSerializationContext p_191129_, CompoundTag p_191130_, long p_191131_) {
      String s = p_191130_.getString("id");
      if ("INVALID".equals(s)) {
         return StructureStart.INVALID_START;
      } else {
         StructureFeature<?> structurefeature = Registry.STRUCTURE_FEATURE.get(new ResourceLocation(s.toLowerCase(Locale.ROOT)));
         if (structurefeature == null) {
            LOGGER.error("Unknown feature id: {}", (Object)s);
            return null;
         } else {
            ChunkPos chunkpos = new ChunkPos(p_191130_.getInt("ChunkX"), p_191130_.getInt("ChunkZ"));
            int i = p_191130_.getInt("references");
            ListTag listtag = p_191130_.getList("Children", 10);

            try {
               PiecesContainer piecescontainer = PiecesContainer.load(listtag, p_191129_);
               if (structurefeature == OCEAN_MONUMENT) {
                  piecescontainer = OceanMonumentFeature.regeneratePiecesAfterLoad(chunkpos, p_191131_, piecescontainer);
               }

               return new StructureStart<>(structurefeature, chunkpos, i, piecescontainer);
            } catch (Exception exception) {
               LOGGER.error("Failed Start with id {}", s, exception);
               return null;
            }
         }
      }
   }

   public Codec<ConfiguredStructureFeature<C, StructureFeature<C>>> configuredStructureCodec() {
      return this.configuredStructureCodec;
   }

   public ConfiguredStructureFeature<C, ? extends StructureFeature<C>> configured(C p_67066_) {
      return new ConfiguredStructureFeature<>(this, p_67066_);
   }

   public BlockPos getLocatePos(ChunkPos p_191115_) {
      return new BlockPos(p_191115_.getMinBlockX(), 0, p_191115_.getMinBlockZ());
   }

   @Nullable
   public BlockPos getNearestGeneratedFeature(LevelReader p_67047_, StructureFeatureManager p_67048_, BlockPos p_67049_, int p_67050_, boolean p_67051_, long p_67052_, StructureFeatureConfiguration p_67053_) {
      int i = p_67053_.spacing();
      int j = SectionPos.blockToSectionCoord(p_67049_.getX());
      int k = SectionPos.blockToSectionCoord(p_67049_.getZ());

      for(int l = 0; l <= p_67050_; ++l) {
         for(int i1 = -l; i1 <= l; ++i1) {
            boolean flag = i1 == -l || i1 == l;

            for(int j1 = -l; j1 <= l; ++j1) {
               boolean flag1 = j1 == -l || j1 == l;
               if (flag || flag1) {
                  int k1 = j + i * i1;
                  int l1 = k + i * j1;
                  ChunkPos chunkpos = this.getPotentialFeatureChunk(p_67053_, p_67052_, k1, l1);
                  StructureCheckResult structurecheckresult = p_67048_.checkStructurePresence(chunkpos, this, p_67051_);
                  if (structurecheckresult != StructureCheckResult.START_NOT_PRESENT) {
                     if (!p_67051_ && structurecheckresult == StructureCheckResult.START_PRESENT) {
                        return this.getLocatePos(chunkpos);
                     }

                     ChunkAccess chunkaccess = p_67047_.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
                     StructureStart<?> structurestart = p_67048_.getStartForFeature(SectionPos.bottomOf(chunkaccess), this, chunkaccess);
                     if (structurestart != null && structurestart.isValid()) {
                        if (p_67051_ && structurestart.canBeReferenced()) {
                           p_67048_.addReference(structurestart);
                           return this.getLocatePos(structurestart.getChunkPos());
                        }

                        if (!p_67051_) {
                           return this.getLocatePos(structurestart.getChunkPos());
                        }
                     }

                     if (l == 0) {
                        break;
                     }
                  }
               }
            }

            if (l == 0) {
               break;
            }
         }
      }

      return null;
   }

   protected boolean linearSeparation() {
      return true;
   }

   public final ChunkPos getPotentialFeatureChunk(StructureFeatureConfiguration p_191123_, long p_191124_, int p_191125_, int p_191126_) {
      int i = p_191123_.spacing();
      int j = p_191123_.separation();
      int k = Math.floorDiv(p_191125_, i);
      int l = Math.floorDiv(p_191126_, i);
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      worldgenrandom.setLargeFeatureWithSalt(p_191124_, k, l, p_191123_.salt());
      int i1;
      int j1;
      if (this.linearSeparation()) {
         i1 = worldgenrandom.nextInt(i - j);
         j1 = worldgenrandom.nextInt(i - j);
      } else {
         i1 = (worldgenrandom.nextInt(i - j) + worldgenrandom.nextInt(i - j)) / 2;
         j1 = (worldgenrandom.nextInt(i - j) + worldgenrandom.nextInt(i - j)) / 2;
      }

      return new ChunkPos(k * i + i1, l * i + j1);
   }

   public StructureStart<?> generate(RegistryAccess p_191133_, ChunkGenerator p_191134_, BiomeSource p_191135_, StructureManager p_191136_, long p_191137_, ChunkPos p_191138_, int p_191139_, StructureFeatureConfiguration p_191140_, C p_191141_, LevelHeightAccessor p_191142_, Predicate<Biome> p_191143_) {
      ChunkPos chunkpos = this.getPotentialFeatureChunk(p_191140_, p_191137_, p_191138_.x, p_191138_.z);
      if (p_191138_.x == chunkpos.x && p_191138_.z == chunkpos.z) {
         Optional<PieceGenerator<C>> optional = this.pieceGenerator.createGenerator(new PieceGeneratorSupplier.Context<>(p_191134_, p_191135_, p_191137_, p_191138_, p_191141_, p_191142_, p_191143_, p_191136_, p_191133_));
         if (optional.isPresent()) {
            StructurePiecesBuilder structurepiecesbuilder = new StructurePiecesBuilder();
            WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
            worldgenrandom.setLargeFeatureSeed(p_191137_, p_191138_.x, p_191138_.z);
            optional.get().generatePieces(structurepiecesbuilder, new PieceGenerator.Context<>(p_191141_, p_191134_, p_191136_, p_191138_, p_191142_, worldgenrandom, p_191137_));
            StructureStart<C> structurestart = new StructureStart<>(this, p_191138_, p_191139_, structurepiecesbuilder.build());
            if (structurestart.isValid()) {
               return structurestart;
            }
         }
      }

      return StructureStart.INVALID_START;
   }

   public boolean canGenerate(RegistryAccess p_197172_, ChunkGenerator p_197173_, BiomeSource p_197174_, StructureManager p_197175_, long p_197176_, ChunkPos p_197177_, C p_197178_, LevelHeightAccessor p_197179_, Predicate<Biome> p_197180_) {
      return this.pieceGenerator.createGenerator(new PieceGeneratorSupplier.Context<>(p_197173_, p_197174_, p_197176_, p_197177_, p_197178_, p_197179_, p_197180_, p_197175_, p_197172_)).isPresent();
   }

   public PostPlacementProcessor getPostPlacementProcessor() {
      return this.postPlacementProcessor;
   }

   public String getFeatureName() {
      return STRUCTURES_REGISTRY.inverse().get(this);
   }

   public BoundingBox adjustBoundingBox(BoundingBox p_191127_) {
      return p_191127_;
   }
}