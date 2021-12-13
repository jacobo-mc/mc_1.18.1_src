package net.minecraft.world.level.levelgen.structure;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public final class StructureStart<C extends FeatureConfiguration> {
   public static final String INVALID_START_ID = "INVALID";
   public static final StructureStart<?> INVALID_START = new StructureStart(null, new ChunkPos(0, 0), 0, new PiecesContainer(List.of()));
   private final StructureFeature<C> feature;
   private final PiecesContainer pieceContainer;
   private final ChunkPos chunkPos;
   private int references;
   @Nullable
   private volatile BoundingBox cachedBoundingBox;

   public StructureStart(StructureFeature<C> p_192656_, ChunkPos p_192657_, int p_192658_, PiecesContainer p_192659_) {
      this.feature = p_192656_;
      this.chunkPos = p_192657_;
      this.references = p_192658_;
      this.pieceContainer = p_192659_;
   }

   public BoundingBox getBoundingBox() {
      BoundingBox boundingbox = this.cachedBoundingBox;
      if (boundingbox == null) {
         boundingbox = this.feature.adjustBoundingBox(this.pieceContainer.calculateBoundingBox());
         this.cachedBoundingBox = boundingbox;
      }

      return boundingbox;
   }

   public void placeInChunk(WorldGenLevel p_73584_, StructureFeatureManager p_73585_, ChunkGenerator p_73586_, Random p_73587_, BoundingBox p_73588_, ChunkPos p_73589_) {
      List<StructurePiece> list = this.pieceContainer.pieces();
      if (!list.isEmpty()) {
         BoundingBox boundingbox = (list.get(0)).boundingBox;
         BlockPos blockpos = boundingbox.getCenter();
         BlockPos blockpos1 = new BlockPos(blockpos.getX(), boundingbox.minY(), blockpos.getZ());

         for(StructurePiece structurepiece : list) {
            if (structurepiece.getBoundingBox().intersects(p_73588_)) {
               structurepiece.postProcess(p_73584_, p_73585_, p_73586_, p_73587_, p_73588_, p_73589_, blockpos1);
            }
         }

         this.feature.getPostPlacementProcessor().afterPlace(p_73584_, p_73585_, p_73586_, p_73587_, p_73588_, p_73589_, this.pieceContainer);
      }
   }

   public CompoundTag createTag(StructurePieceSerializationContext p_192661_, ChunkPos p_192662_) {
      CompoundTag compoundtag = new CompoundTag();
      if (this.isValid()) {
         compoundtag.putString("id", Registry.STRUCTURE_FEATURE.getKey(this.getFeature()).toString());
         compoundtag.putInt("ChunkX", p_192662_.x);
         compoundtag.putInt("ChunkZ", p_192662_.z);
         compoundtag.putInt("references", this.references);
         compoundtag.put("Children", this.pieceContainer.save(p_192661_));
         return compoundtag;
      } else {
         compoundtag.putString("id", "INVALID");
         return compoundtag;
      }
   }

   public boolean isValid() {
      return !this.pieceContainer.isEmpty();
   }

   public ChunkPos getChunkPos() {
      return this.chunkPos;
   }

   public boolean canBeReferenced() {
      return this.references < this.getMaxReferences();
   }

   public void addReference() {
      ++this.references;
   }

   public int getReferences() {
      return this.references;
   }

   protected int getMaxReferences() {
      return 1;
   }

   public StructureFeature<?> getFeature() {
      return this.feature;
   }

   public List<StructurePiece> getPieces() {
      return this.pieceContainer.pieces();
   }
}
