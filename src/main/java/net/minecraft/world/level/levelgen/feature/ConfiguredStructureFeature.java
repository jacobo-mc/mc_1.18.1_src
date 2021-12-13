package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class ConfiguredStructureFeature<FC extends FeatureConfiguration, F extends StructureFeature<FC>> {
   public static final Codec<ConfiguredStructureFeature<?, ?>> DIRECT_CODEC = Registry.STRUCTURE_FEATURE.byNameCodec().dispatch((p_65410_) -> {
      return p_65410_.feature;
   }, StructureFeature::configuredStructureCodec);
   public static final Codec<Supplier<ConfiguredStructureFeature<?, ?>>> CODEC = RegistryFileCodec.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, DIRECT_CODEC);
   public static final Codec<List<Supplier<ConfiguredStructureFeature<?, ?>>>> LIST_CODEC = RegistryFileCodec.homogeneousList(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, DIRECT_CODEC);
   public final F feature;
   public final FC config;

   public ConfiguredStructureFeature(F p_65407_, FC p_65408_) {
      this.feature = p_65407_;
      this.config = p_65408_;
   }

   public StructureStart<?> generate(RegistryAccess p_190828_, ChunkGenerator p_190829_, BiomeSource p_190830_, StructureManager p_190831_, long p_190832_, ChunkPos p_190833_, int p_190834_, StructureFeatureConfiguration p_190835_, LevelHeightAccessor p_190836_, Predicate<Biome> p_190837_) {
      return this.feature.generate(p_190828_, p_190829_, p_190830_, p_190831_, p_190832_, p_190833_, p_190834_, p_190835_, this.config, p_190836_, p_190837_);
   }
}