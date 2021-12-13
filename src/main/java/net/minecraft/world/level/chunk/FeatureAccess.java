package net.minecraft.world.level.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public interface FeatureAccess {
   @Nullable
   StructureStart<?> getStartForFeature(StructureFeature<?> p_62632_);

   void setStartForFeature(StructureFeature<?> p_62635_, StructureStart<?> p_62636_);

   LongSet getReferencesForFeature(StructureFeature<?> p_62637_);

   void addReferenceForFeature(StructureFeature<?> p_62633_, long p_62634_);

   Map<StructureFeature<?>, LongSet> getAllReferences();

   void setAllReferences(Map<StructureFeature<?>, LongSet> p_62638_);
}