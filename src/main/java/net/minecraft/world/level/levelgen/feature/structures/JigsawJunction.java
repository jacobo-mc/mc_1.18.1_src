package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class JigsawJunction {
   private final int sourceX;
   private final int sourceGroundY;
   private final int sourceZ;
   private final int deltaY;
   private final StructureTemplatePool.Projection destProjection;

   public JigsawJunction(int p_68925_, int p_68926_, int p_68927_, int p_68928_, StructureTemplatePool.Projection p_68929_) {
      this.sourceX = p_68925_;
      this.sourceGroundY = p_68926_;
      this.sourceZ = p_68927_;
      this.deltaY = p_68928_;
      this.destProjection = p_68929_;
   }

   public int getSourceX() {
      return this.sourceX;
   }

   public int getSourceGroundY() {
      return this.sourceGroundY;
   }

   public int getSourceZ() {
      return this.sourceZ;
   }

   public int getDeltaY() {
      return this.deltaY;
   }

   public StructureTemplatePool.Projection getDestProjection() {
      return this.destProjection;
   }

   public <T> Dynamic<T> serialize(DynamicOps<T> p_68934_) {
      Builder<T, T> builder = ImmutableMap.builder();
      builder.put(p_68934_.createString("source_x"), p_68934_.createInt(this.sourceX)).put(p_68934_.createString("source_ground_y"), p_68934_.createInt(this.sourceGroundY)).put(p_68934_.createString("source_z"), p_68934_.createInt(this.sourceZ)).put(p_68934_.createString("delta_y"), p_68934_.createInt(this.deltaY)).put(p_68934_.createString("dest_proj"), p_68934_.createString(this.destProjection.getName()));
      return new Dynamic<>(p_68934_, p_68934_.createMap(builder.build()));
   }

   public static <T> JigsawJunction deserialize(Dynamic<T> p_68932_) {
      return new JigsawJunction(p_68932_.get("source_x").asInt(0), p_68932_.get("source_ground_y").asInt(0), p_68932_.get("source_z").asInt(0), p_68932_.get("delta_y").asInt(0), StructureTemplatePool.Projection.byName(p_68932_.get("dest_proj").asString("")));
   }

   public boolean equals(Object p_68938_) {
      if (this == p_68938_) {
         return true;
      } else if (p_68938_ != null && this.getClass() == p_68938_.getClass()) {
         JigsawJunction jigsawjunction = (JigsawJunction)p_68938_;
         if (this.sourceX != jigsawjunction.sourceX) {
            return false;
         } else if (this.sourceZ != jigsawjunction.sourceZ) {
            return false;
         } else if (this.deltaY != jigsawjunction.deltaY) {
            return false;
         } else {
            return this.destProjection == jigsawjunction.destProjection;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int i = this.sourceX;
      i = 31 * i + this.sourceGroundY;
      i = 31 * i + this.sourceZ;
      i = 31 * i + this.deltaY;
      return 31 * i + this.destProjection.hashCode();
   }

   public String toString() {
      return "JigsawJunction{sourceX=" + this.sourceX + ", sourceGroundY=" + this.sourceGroundY + ", sourceZ=" + this.sourceZ + ", deltaY=" + this.deltaY + ", destProjection=" + this.destProjection + "}";
   }
}