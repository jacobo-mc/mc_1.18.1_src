package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class DefaultedRegistry<T> extends MappedRegistry<T> {
   private final ResourceLocation defaultKey;
   private T defaultValue;

   public DefaultedRegistry(String p_122312_, ResourceKey<? extends Registry<T>> p_122313_, Lifecycle p_122314_) {
      super(p_122313_, p_122314_);
      this.defaultKey = new ResourceLocation(p_122312_);
   }

   public <V extends T> V registerMapping(int p_122319_, ResourceKey<T> p_122320_, V p_122321_, Lifecycle p_122322_) {
      if (this.defaultKey.equals(p_122320_.location())) {
         this.defaultValue = (T)p_122321_;
      }

      return super.registerMapping(p_122319_, p_122320_, p_122321_, p_122322_);
   }

   public int getId(@Nullable T p_122324_) {
      int i = super.getId(p_122324_);
      return i == -1 ? super.getId(this.defaultValue) : i;
   }

   @Nonnull
   public ResourceLocation getKey(T p_122330_) {
      ResourceLocation resourcelocation = super.getKey(p_122330_);
      return resourcelocation == null ? this.defaultKey : resourcelocation;
   }

   @Nonnull
   public T get(@Nullable ResourceLocation p_122328_) {
      T t = super.get(p_122328_);
      return (T)(t == null ? this.defaultValue : t);
   }

   public Optional<T> getOptional(@Nullable ResourceLocation p_122332_) {
      return Optional.ofNullable(super.get(p_122332_));
   }

   @Nonnull
   public T byId(int p_122317_) {
      T t = super.byId(p_122317_);
      return (T)(t == null ? this.defaultValue : t);
   }

   @Nonnull
   public T getRandom(Random p_122326_) {
      T t = super.getRandom(p_122326_);
      return (T)(t == null ? this.defaultValue : t);
   }

   public ResourceLocation getDefaultKey() {
      return this.defaultKey;
   }
}