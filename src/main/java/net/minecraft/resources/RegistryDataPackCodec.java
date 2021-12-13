package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;

public final class RegistryDataPackCodec<E> implements Codec<MappedRegistry<E>> {
   private final Codec<MappedRegistry<E>> directCodec;
   private final ResourceKey<? extends Registry<E>> registryKey;
   private final Codec<E> elementCodec;

   public static <E> RegistryDataPackCodec<E> create(ResourceKey<? extends Registry<E>> p_135559_, Lifecycle p_135560_, Codec<E> p_135561_) {
      return new RegistryDataPackCodec<>(p_135559_, p_135560_, p_135561_);
   }

   private RegistryDataPackCodec(ResourceKey<? extends Registry<E>> p_135545_, Lifecycle p_135546_, Codec<E> p_135547_) {
      this.directCodec = MappedRegistry.directCodec(p_135545_, p_135546_, p_135547_);
      this.registryKey = p_135545_;
      this.elementCodec = p_135547_;
   }

   public <T> DataResult<T> encode(MappedRegistry<E> p_135555_, DynamicOps<T> p_135556_, T p_135557_) {
      return this.directCodec.encode(p_135555_, p_135556_, p_135557_);
   }

   public <T> DataResult<Pair<MappedRegistry<E>, T>> decode(DynamicOps<T> p_135563_, T p_135564_) {
      DataResult<Pair<MappedRegistry<E>, T>> dataresult = this.directCodec.decode(p_135563_, p_135564_);
      return p_135563_ instanceof RegistryReadOps ? dataresult.flatMap((p_135553_) -> {
         return ((RegistryReadOps)p_135563_).decodeElements(p_135553_.getFirst(), this.registryKey, this.elementCodec).map((p_179848_) -> {
            return Pair.of(p_179848_, (T)p_135553_.getSecond());
         });
      }) : dataresult;
   }

   public String toString() {
      return "RegistryDataPackCodec[" + this.directCodec + " " + this.registryKey + " " + this.elementCodec + "]";
   }
}