package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocationPredicate {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final LocationPredicate ANY = new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, (StructureFeature<?>)null, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   private final MinMaxBounds.Doubles x;
   private final MinMaxBounds.Doubles y;
   private final MinMaxBounds.Doubles z;
   @Nullable
   private final ResourceKey<Biome> biome;
   @Nullable
   private final StructureFeature<?> feature;
   @Nullable
   private final ResourceKey<Level> dimension;
   @Nullable
   private final Boolean smokey;
   private final LightPredicate light;
   private final BlockPredicate block;
   private final FluidPredicate fluid;

   public LocationPredicate(MinMaxBounds.Doubles p_52606_, MinMaxBounds.Doubles p_52607_, MinMaxBounds.Doubles p_52608_, @Nullable ResourceKey<Biome> p_52609_, @Nullable StructureFeature<?> p_52610_, @Nullable ResourceKey<Level> p_52611_, @Nullable Boolean p_52612_, LightPredicate p_52613_, BlockPredicate p_52614_, FluidPredicate p_52615_) {
      this.x = p_52606_;
      this.y = p_52607_;
      this.z = p_52608_;
      this.biome = p_52609_;
      this.feature = p_52610_;
      this.dimension = p_52611_;
      this.smokey = p_52612_;
      this.light = p_52613_;
      this.block = p_52614_;
      this.fluid = p_52615_;
   }

   public static LocationPredicate inBiome(ResourceKey<Biome> p_52635_) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, p_52635_, (StructureFeature<?>)null, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public static LocationPredicate inDimension(ResourceKey<Level> p_52639_) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, (StructureFeature<?>)null, p_52639_, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public static LocationPredicate inFeature(StructureFeature<?> p_52628_) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, p_52628_, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public static LocationPredicate atYLocation(MinMaxBounds.Doubles p_187443_) {
      return new LocationPredicate(MinMaxBounds.Doubles.ANY, p_187443_, MinMaxBounds.Doubles.ANY, (ResourceKey<Biome>)null, (StructureFeature<?>)null, (ResourceKey<Level>)null, (Boolean)null, LightPredicate.ANY, BlockPredicate.ANY, FluidPredicate.ANY);
   }

   public boolean matches(ServerLevel p_52618_, double p_52619_, double p_52620_, double p_52621_) {
      if (!this.x.matches(p_52619_)) {
         return false;
      } else if (!this.y.matches(p_52620_)) {
         return false;
      } else if (!this.z.matches(p_52621_)) {
         return false;
      } else if (this.dimension != null && this.dimension != p_52618_.dimension()) {
         return false;
      } else {
         BlockPos blockpos = new BlockPos(p_52619_, p_52620_, p_52621_);
         boolean flag = p_52618_.isLoaded(blockpos);
         Optional<ResourceKey<Biome>> optional = p_52618_.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getResourceKey(p_52618_.getBiome(blockpos));
         if (!optional.isPresent()) {
            return false;
         } else if (this.biome == null || flag && this.biome == optional.get()) {
            if (this.feature == null || flag && p_52618_.structureFeatureManager().getStructureWithPieceAt(blockpos, this.feature).isValid()) {
               if (this.smokey == null || flag && this.smokey == CampfireBlock.isSmokeyPos(p_52618_, blockpos)) {
                  if (!this.light.matches(p_52618_, blockpos)) {
                     return false;
                  } else if (!this.block.matches(p_52618_, blockpos)) {
                     return false;
                  } else {
                     return this.fluid.matches(p_52618_, blockpos);
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (!this.x.isAny() || !this.y.isAny() || !this.z.isAny()) {
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.add("x", this.x.serializeToJson());
            jsonobject1.add("y", this.y.serializeToJson());
            jsonobject1.add("z", this.z.serializeToJson());
            jsonobject.add("position", jsonobject1);
         }

         if (this.dimension != null) {
            Level.RESOURCE_KEY_CODEC.encodeStart(JsonOps.INSTANCE, this.dimension).resultOrPartial(LOGGER::error).ifPresent((p_52633_) -> {
               jsonobject.add("dimension", p_52633_);
            });
         }

         if (this.feature != null) {
            jsonobject.addProperty("feature", this.feature.getFeatureName());
         }

         if (this.biome != null) {
            jsonobject.addProperty("biome", this.biome.location().toString());
         }

         if (this.smokey != null) {
            jsonobject.addProperty("smokey", this.smokey);
         }

         jsonobject.add("light", this.light.serializeToJson());
         jsonobject.add("block", this.block.serializeToJson());
         jsonobject.add("fluid", this.fluid.serializeToJson());
         return jsonobject;
      }
   }

   public static LocationPredicate fromJson(@Nullable JsonElement p_52630_) {
      if (p_52630_ != null && !p_52630_.isJsonNull()) {
         JsonObject jsonobject = GsonHelper.convertToJsonObject(p_52630_, "location");
         JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonobject, "position", new JsonObject());
         MinMaxBounds.Doubles minmaxbounds$doubles = MinMaxBounds.Doubles.fromJson(jsonobject1.get("x"));
         MinMaxBounds.Doubles minmaxbounds$doubles1 = MinMaxBounds.Doubles.fromJson(jsonobject1.get("y"));
         MinMaxBounds.Doubles minmaxbounds$doubles2 = MinMaxBounds.Doubles.fromJson(jsonobject1.get("z"));
         ResourceKey<Level> resourcekey = jsonobject.has("dimension") ? ResourceLocation.CODEC.parse(JsonOps.INSTANCE, jsonobject.get("dimension")).resultOrPartial(LOGGER::error).map((p_52637_) -> {
            return ResourceKey.create(Registry.DIMENSION_REGISTRY, p_52637_);
         }).orElse((ResourceKey<Level>)null) : null;
         StructureFeature<?> structurefeature = jsonobject.has("feature") ? StructureFeature.STRUCTURES_REGISTRY.get(GsonHelper.getAsString(jsonobject, "feature")) : null;
         ResourceKey<Biome> resourcekey1 = null;
         if (jsonobject.has("biome")) {
            ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(jsonobject, "biome"));
            resourcekey1 = ResourceKey.create(Registry.BIOME_REGISTRY, resourcelocation);
         }

         Boolean obool = jsonobject.has("smokey") ? jsonobject.get("smokey").getAsBoolean() : null;
         LightPredicate lightpredicate = LightPredicate.fromJson(jsonobject.get("light"));
         BlockPredicate blockpredicate = BlockPredicate.fromJson(jsonobject.get("block"));
         FluidPredicate fluidpredicate = FluidPredicate.fromJson(jsonobject.get("fluid"));
         return new LocationPredicate(minmaxbounds$doubles, minmaxbounds$doubles1, minmaxbounds$doubles2, resourcekey1, structurefeature, resourcekey, obool, lightpredicate, blockpredicate, fluidpredicate);
      } else {
         return ANY;
      }
   }

   public static class Builder {
      private MinMaxBounds.Doubles x = MinMaxBounds.Doubles.ANY;
      private MinMaxBounds.Doubles y = MinMaxBounds.Doubles.ANY;
      private MinMaxBounds.Doubles z = MinMaxBounds.Doubles.ANY;
      @Nullable
      private ResourceKey<Biome> biome;
      @Nullable
      private StructureFeature<?> feature;
      @Nullable
      private ResourceKey<Level> dimension;
      @Nullable
      private Boolean smokey;
      private LightPredicate light = LightPredicate.ANY;
      private BlockPredicate block = BlockPredicate.ANY;
      private FluidPredicate fluid = FluidPredicate.ANY;

      public static LocationPredicate.Builder location() {
         return new LocationPredicate.Builder();
      }

      public LocationPredicate.Builder setX(MinMaxBounds.Doubles p_153971_) {
         this.x = p_153971_;
         return this;
      }

      public LocationPredicate.Builder setY(MinMaxBounds.Doubles p_153975_) {
         this.y = p_153975_;
         return this;
      }

      public LocationPredicate.Builder setZ(MinMaxBounds.Doubles p_153979_) {
         this.z = p_153979_;
         return this;
      }

      public LocationPredicate.Builder setBiome(@Nullable ResourceKey<Biome> p_52657_) {
         this.biome = p_52657_;
         return this;
      }

      public LocationPredicate.Builder setFeature(@Nullable StructureFeature<?> p_153973_) {
         this.feature = p_153973_;
         return this;
      }

      public LocationPredicate.Builder setDimension(@Nullable ResourceKey<Level> p_153977_) {
         this.dimension = p_153977_;
         return this;
      }

      public LocationPredicate.Builder setLight(LightPredicate p_153969_) {
         this.light = p_153969_;
         return this;
      }

      public LocationPredicate.Builder setBlock(BlockPredicate p_52653_) {
         this.block = p_52653_;
         return this;
      }

      public LocationPredicate.Builder setFluid(FluidPredicate p_153967_) {
         this.fluid = p_153967_;
         return this;
      }

      public LocationPredicate.Builder setSmokey(Boolean p_52655_) {
         this.smokey = p_52655_;
         return this;
      }

      public LocationPredicate build() {
         return new LocationPredicate(this.x, this.y, this.z, this.biome, this.feature, this.dimension, this.smokey, this.light, this.block, this.fluid);
      }
   }
}