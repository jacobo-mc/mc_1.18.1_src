package net.minecraft.world.level.dimension;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public final class LevelStem {
   public static final Codec<LevelStem> CODEC = RecordCodecBuilder.create((p_63986_) -> {
      return p_63986_.group(DimensionType.CODEC.fieldOf("type").flatXmap(ExtraCodecs.nonNullSupplierCheck(), ExtraCodecs.nonNullSupplierCheck()).forGetter(LevelStem::typeSupplier), ChunkGenerator.CODEC.fieldOf("generator").forGetter(LevelStem::generator)).apply(p_63986_, p_63986_.stable(LevelStem::new));
   });
   public static final ResourceKey<LevelStem> OVERWORLD = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("overworld"));
   public static final ResourceKey<LevelStem> NETHER = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_nether"));
   public static final ResourceKey<LevelStem> END = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_end"));
   private static final Set<ResourceKey<LevelStem>> BUILTIN_ORDER = Sets.newLinkedHashSet(ImmutableList.of(OVERWORLD, NETHER, END));
   private final Supplier<DimensionType> type;
   private final ChunkGenerator generator;

   public LevelStem(Supplier<DimensionType> p_63979_, ChunkGenerator p_63980_) {
      this.type = p_63979_;
      this.generator = p_63980_;
   }

   public Supplier<DimensionType> typeSupplier() {
      return this.type;
   }

   public DimensionType type() {
      return this.type.get();
   }

   public ChunkGenerator generator() {
      return this.generator;
   }

   public static MappedRegistry<LevelStem> sortMap(MappedRegistry<LevelStem> p_63988_) {
      MappedRegistry<LevelStem> mappedregistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental());

      for(ResourceKey<LevelStem> resourcekey : BUILTIN_ORDER) {
         LevelStem levelstem = p_63988_.get(resourcekey);
         if (levelstem != null) {
            mappedregistry.register(resourcekey, levelstem, p_63988_.lifecycle(levelstem));
         }
      }

      for(Entry<ResourceKey<LevelStem>, LevelStem> entry : p_63988_.entrySet()) {
         ResourceKey<LevelStem> resourcekey1 = entry.getKey();
         if (!BUILTIN_ORDER.contains(resourcekey1)) {
            mappedregistry.register(resourcekey1, entry.getValue(), p_63988_.lifecycle(entry.getValue()));
         }
      }

      return mappedregistry;
   }

   public static boolean stable(long p_63983_, MappedRegistry<LevelStem> p_63984_) {
      List<Entry<ResourceKey<LevelStem>, LevelStem>> list = Lists.newArrayList(p_63984_.entrySet());
      if (list.size() != BUILTIN_ORDER.size()) {
         return false;
      } else {
         Entry<ResourceKey<LevelStem>, LevelStem> entry = list.get(0);
         Entry<ResourceKey<LevelStem>, LevelStem> entry1 = list.get(1);
         Entry<ResourceKey<LevelStem>, LevelStem> entry2 = list.get(2);
         if (entry.getKey() == OVERWORLD && entry1.getKey() == NETHER && entry2.getKey() == END) {
            if (!entry.getValue().type().equalTo(DimensionType.DEFAULT_OVERWORLD) && entry.getValue().type() != DimensionType.DEFAULT_OVERWORLD_CAVES) {
               return false;
            } else if (!entry1.getValue().type().equalTo(DimensionType.DEFAULT_NETHER)) {
               return false;
            } else if (!entry2.getValue().type().equalTo(DimensionType.DEFAULT_END)) {
               return false;
            } else if (entry1.getValue().generator() instanceof NoiseBasedChunkGenerator && entry2.getValue().generator() instanceof NoiseBasedChunkGenerator) {
               NoiseBasedChunkGenerator noisebasedchunkgenerator = (NoiseBasedChunkGenerator)entry1.getValue().generator();
               NoiseBasedChunkGenerator noisebasedchunkgenerator1 = (NoiseBasedChunkGenerator)entry2.getValue().generator();
               if (!noisebasedchunkgenerator.stable(p_63983_, NoiseGeneratorSettings.NETHER)) {
                  return false;
               } else if (!noisebasedchunkgenerator1.stable(p_63983_, NoiseGeneratorSettings.END)) {
                  return false;
               } else if (!(noisebasedchunkgenerator.getBiomeSource() instanceof MultiNoiseBiomeSource)) {
                  return false;
               } else {
                  MultiNoiseBiomeSource multinoisebiomesource = (MultiNoiseBiomeSource)noisebasedchunkgenerator.getBiomeSource();
                  if (!multinoisebiomesource.stable(MultiNoiseBiomeSource.Preset.NETHER)) {
                     return false;
                  } else {
                     BiomeSource biomesource = entry.getValue().generator().getBiomeSource();
                     if (biomesource instanceof MultiNoiseBiomeSource && !((MultiNoiseBiomeSource)biomesource).stable(MultiNoiseBiomeSource.Preset.OVERWORLD)) {
                        return false;
                     } else if (!(noisebasedchunkgenerator1.getBiomeSource() instanceof TheEndBiomeSource)) {
                        return false;
                     } else {
                        TheEndBiomeSource theendbiomesource = (TheEndBiomeSource)noisebasedchunkgenerator1.getBiomeSource();
                        return theendbiomesource.stable(p_63983_);
                     }
                  }
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }
}