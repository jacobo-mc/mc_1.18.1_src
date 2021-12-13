package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;

public class PillagerOutpostFeature extends JigsawFeature {
   public static final WeightedRandomList<MobSpawnSettings.SpawnerData> OUTPOST_ENEMIES = WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1));

   public PillagerOutpostFeature(Codec<JigsawConfiguration> p_66562_) {
      super(p_66562_, 0, true, true, PillagerOutpostFeature::checkLocation);
   }

   private static boolean checkLocation(PieceGeneratorSupplier.Context<JigsawConfiguration> p_197134_) {
      int i = p_197134_.chunkPos().x >> 4;
      int j = p_197134_.chunkPos().z >> 4;
      WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(0L));
      worldgenrandom.setSeed((long)(i ^ j << 4) ^ p_197134_.seed());
      worldgenrandom.nextInt();
      if (worldgenrandom.nextInt(5) != 0) {
         return false;
      } else {
         return !isNearVillage(p_197134_.chunkGenerator(), p_197134_.seed(), p_197134_.chunkPos());
      }
   }

   private static boolean isNearVillage(ChunkGenerator p_191049_, long p_191050_, ChunkPos p_191051_) {
      StructureFeatureConfiguration structurefeatureconfiguration = p_191049_.getSettings().getConfig(StructureFeature.VILLAGE);
      if (structurefeatureconfiguration == null) {
         return false;
      } else {
         int i = p_191051_.x;
         int j = p_191051_.z;

         for(int k = i - 10; k <= i + 10; ++k) {
            for(int l = j - 10; l <= j + 10; ++l) {
               ChunkPos chunkpos = StructureFeature.VILLAGE.getPotentialFeatureChunk(structurefeatureconfiguration, p_191050_, k, l);
               if (k == chunkpos.x && l == chunkpos.z) {
                  return true;
               }
            }
         }

         return false;
      }
   }
}