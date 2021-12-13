package net.minecraft.core;

import com.mojang.serialization.Lifecycle;
import java.util.OptionalInt;
import net.minecraft.resources.ResourceKey;

public abstract class WritableRegistry<T> extends Registry<T> {
   public WritableRegistry(ResourceKey<? extends Registry<T>> p_123346_, Lifecycle p_123347_) {
      super(p_123346_, p_123347_);
   }

   public abstract <V extends T> V registerMapping(int p_123348_, ResourceKey<T> p_123349_, V p_123350_, Lifecycle p_123351_);

   public abstract <V extends T> V register(ResourceKey<T> p_123356_, V p_123357_, Lifecycle p_123358_);

   public abstract <V extends T> V registerOrOverride(OptionalInt p_123352_, ResourceKey<T> p_123353_, V p_123354_, Lifecycle p_123355_);

   public abstract boolean isEmpty();
}