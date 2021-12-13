package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureFeatureManager {
   private final LevelAccessor level;
   private final WorldGenSettings worldGenSettings;
   private final StructureCheck structureCheck;

   public StructureFeatureManager(LevelAccessor p_196667_, WorldGenSettings p_196668_, StructureCheck p_196669_) {
      this.level = p_196667_;
      this.worldGenSettings = p_196668_;
      this.structureCheck = p_196669_;
   }

   public StructureFeatureManager forWorldGenRegion(WorldGenRegion p_47273_) {
      if (p_47273_.getLevel() != this.level) {
         throw new IllegalStateException("Using invalid feature manager (source level: " + p_47273_.getLevel() + ", region: " + p_47273_);
      } else {
         return new StructureFeatureManager(p_47273_, this.worldGenSettings, this.structureCheck);
      }
   }

   public List<? extends StructureStart<?>> startsForFeature(SectionPos p_186611_, StructureFeature<?> p_186612_) {
      LongSet longset = this.level.getChunk(p_186611_.x(), p_186611_.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(p_186612_);
      Builder<StructureStart<?>> builder = ImmutableList.builder();

      for(long i : longset) {
         SectionPos sectionpos = SectionPos.of(new ChunkPos(i), this.level.getMinSection());
         StructureStart<?> structurestart = this.getStartForFeature(sectionpos, p_186612_, this.level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_STARTS));
         if (structurestart != null && structurestart.isValid()) {
            builder.add(structurestart);
         }
      }

      return builder.build();
   }

   @Nullable
   public StructureStart<?> getStartForFeature(SectionPos p_47298_, StructureFeature<?> p_47299_, FeatureAccess p_47300_) {
      return p_47300_.getStartForFeature(p_47299_);
   }

   public void setStartForFeature(SectionPos p_47302_, StructureFeature<?> p_47303_, StructureStart<?> p_47304_, FeatureAccess p_47305_) {
      p_47305_.setStartForFeature(p_47303_, p_47304_);
   }

   public void addReferenceForFeature(SectionPos p_47293_, StructureFeature<?> p_47294_, long p_47295_, FeatureAccess p_47296_) {
      p_47296_.addReferenceForFeature(p_47294_, p_47295_);
   }

   public boolean shouldGenerateFeatures() {
      return this.worldGenSettings.generateFeatures();
   }

   public StructureStart<?> getStructureAt(BlockPos p_186608_, StructureFeature<?> p_186609_) {
      for(StructureStart<?> structurestart : this.startsForFeature(SectionPos.of(p_186608_), p_186609_)) {
         if (structurestart.getBoundingBox().isInside(p_186608_)) {
            return structurestart;
         }
      }

      return StructureStart.INVALID_START;
   }

   public StructureStart<?> getStructureWithPieceAt(BlockPos p_186614_, StructureFeature<?> p_186615_) {
      for(StructureStart<?> structurestart : this.startsForFeature(SectionPos.of(p_186614_), p_186615_)) {
         for(StructurePiece structurepiece : structurestart.getPieces()) {
            if (structurepiece.getBoundingBox().isInside(p_186614_)) {
               return structurestart;
            }
         }
      }

      return StructureStart.INVALID_START;
   }

   public boolean hasAnyStructureAt(BlockPos p_186606_) {
      SectionPos sectionpos = SectionPos.of(p_186606_);
      return this.level.getChunk(sectionpos.x(), sectionpos.z(), ChunkStatus.STRUCTURE_REFERENCES).hasAnyStructureReferences();
   }

   public StructureCheckResult checkStructurePresence(ChunkPos p_196671_, StructureFeature<?> p_196672_, boolean p_196673_) {
      return this.structureCheck.checkStart(p_196671_, p_196672_, p_196673_);
   }

   public void addReference(StructureStart<?> p_196675_) {
      p_196675_.addReference();
      this.structureCheck.incrementReference(p_196675_.getChunkPos(), p_196675_.getFeature());
   }
}