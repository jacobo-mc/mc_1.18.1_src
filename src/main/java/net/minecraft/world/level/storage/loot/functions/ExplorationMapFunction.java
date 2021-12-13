package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExplorationMapFunction extends LootItemConditionalFunction {
   static final Logger LOGGER = LogManager.getLogger();
   public static final StructureFeature<?> DEFAULT_FEATURE = StructureFeature.BURIED_TREASURE;
   public static final String DEFAULT_DECORATION_NAME = "mansion";
   public static final MapDecoration.Type DEFAULT_DECORATION = MapDecoration.Type.MANSION;
   public static final byte DEFAULT_ZOOM = 2;
   public static final int DEFAULT_SEARCH_RADIUS = 50;
   public static final boolean DEFAULT_SKIP_EXISTING = true;
   final StructureFeature<?> destination;
   final MapDecoration.Type mapDecoration;
   final byte zoom;
   final int searchRadius;
   final boolean skipKnownStructures;

   ExplorationMapFunction(LootItemCondition[] p_80531_, StructureFeature<?> p_80532_, MapDecoration.Type p_80533_, byte p_80534_, int p_80535_, boolean p_80536_) {
      super(p_80531_);
      this.destination = p_80532_;
      this.mapDecoration = p_80533_;
      this.zoom = p_80534_;
      this.searchRadius = p_80535_;
      this.skipKnownStructures = p_80536_;
   }

   public LootItemFunctionType getType() {
      return LootItemFunctions.EXPLORATION_MAP;
   }

   public Set<LootContextParam<?>> getReferencedContextParams() {
      return ImmutableSet.of(LootContextParams.ORIGIN);
   }

   public ItemStack run(ItemStack p_80547_, LootContext p_80548_) {
      if (!p_80547_.is(Items.MAP)) {
         return p_80547_;
      } else {
         Vec3 vec3 = p_80548_.getParamOrNull(LootContextParams.ORIGIN);
         if (vec3 != null) {
            ServerLevel serverlevel = p_80548_.getLevel();
            BlockPos blockpos = serverlevel.findNearestMapFeature(this.destination, new BlockPos(vec3), this.searchRadius, this.skipKnownStructures);
            if (blockpos != null) {
               ItemStack itemstack = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), this.zoom, true, true);
               MapItem.renderBiomePreviewMap(serverlevel, itemstack);
               MapItemSavedData.addTargetDecoration(itemstack, blockpos, "+", this.mapDecoration);
               itemstack.setHoverName(new TranslatableComponent("filled_map." + this.destination.getFeatureName().toLowerCase(Locale.ROOT)));
               return itemstack;
            }
         }

         return p_80547_;
      }
   }

   public static ExplorationMapFunction.Builder makeExplorationMap() {
      return new ExplorationMapFunction.Builder();
   }

   public static class Builder extends LootItemConditionalFunction.Builder<ExplorationMapFunction.Builder> {
      private StructureFeature<?> destination = ExplorationMapFunction.DEFAULT_FEATURE;
      private MapDecoration.Type mapDecoration = ExplorationMapFunction.DEFAULT_DECORATION;
      private byte zoom = 2;
      private int searchRadius = 50;
      private boolean skipKnownStructures = true;

      protected ExplorationMapFunction.Builder getThis() {
         return this;
      }

      public ExplorationMapFunction.Builder setDestination(StructureFeature<?> p_80572_) {
         this.destination = p_80572_;
         return this;
      }

      public ExplorationMapFunction.Builder setMapDecoration(MapDecoration.Type p_80574_) {
         this.mapDecoration = p_80574_;
         return this;
      }

      public ExplorationMapFunction.Builder setZoom(byte p_80570_) {
         this.zoom = p_80570_;
         return this;
      }

      public ExplorationMapFunction.Builder setSearchRadius(int p_165206_) {
         this.searchRadius = p_165206_;
         return this;
      }

      public ExplorationMapFunction.Builder setSkipKnownStructures(boolean p_80576_) {
         this.skipKnownStructures = p_80576_;
         return this;
      }

      public LootItemFunction build() {
         return new ExplorationMapFunction(this.getConditions(), this.destination, this.mapDecoration, this.zoom, this.searchRadius, this.skipKnownStructures);
      }
   }

   public static class Serializer extends LootItemConditionalFunction.Serializer<ExplorationMapFunction> {
      public void serialize(JsonObject p_80587_, ExplorationMapFunction p_80588_, JsonSerializationContext p_80589_) {
         super.serialize(p_80587_, p_80588_, p_80589_);
         if (!p_80588_.destination.equals(ExplorationMapFunction.DEFAULT_FEATURE)) {
            p_80587_.add("destination", p_80589_.serialize(p_80588_.destination.getFeatureName()));
         }

         if (p_80588_.mapDecoration != ExplorationMapFunction.DEFAULT_DECORATION) {
            p_80587_.add("decoration", p_80589_.serialize(p_80588_.mapDecoration.toString().toLowerCase(Locale.ROOT)));
         }

         if (p_80588_.zoom != 2) {
            p_80587_.addProperty("zoom", p_80588_.zoom);
         }

         if (p_80588_.searchRadius != 50) {
            p_80587_.addProperty("search_radius", p_80588_.searchRadius);
         }

         if (!p_80588_.skipKnownStructures) {
            p_80587_.addProperty("skip_existing_chunks", p_80588_.skipKnownStructures);
         }

      }

      public ExplorationMapFunction deserialize(JsonObject p_80583_, JsonDeserializationContext p_80584_, LootItemCondition[] p_80585_) {
         StructureFeature<?> structurefeature = readStructure(p_80583_);
         String s = p_80583_.has("decoration") ? GsonHelper.getAsString(p_80583_, "decoration") : "mansion";
         MapDecoration.Type mapdecoration$type = ExplorationMapFunction.DEFAULT_DECORATION;

         try {
            mapdecoration$type = MapDecoration.Type.valueOf(s.toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException illegalargumentexception) {
            ExplorationMapFunction.LOGGER.error("Error while parsing loot table decoration entry. Found {}. Defaulting to {}", s, ExplorationMapFunction.DEFAULT_DECORATION);
         }

         byte b0 = GsonHelper.getAsByte(p_80583_, "zoom", (byte)2);
         int i = GsonHelper.getAsInt(p_80583_, "search_radius", 50);
         boolean flag = GsonHelper.getAsBoolean(p_80583_, "skip_existing_chunks", true);
         return new ExplorationMapFunction(p_80585_, structurefeature, mapdecoration$type, b0, i, flag);
      }

      private static StructureFeature<?> readStructure(JsonObject p_80581_) {
         if (p_80581_.has("destination")) {
            String s = GsonHelper.getAsString(p_80581_, "destination");
            StructureFeature<?> structurefeature = StructureFeature.STRUCTURES_REGISTRY.get(s.toLowerCase(Locale.ROOT));
            if (structurefeature != null) {
               return structurefeature;
            }
         }

         return ExplorationMapFunction.DEFAULT_FEATURE;
      }
   }
}