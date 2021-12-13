package net.minecraft.resources;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Registry;

public final class RegistryLookupCodec<E> extends MapCodec<Registry<E>> {
   private final ResourceKey<? extends Registry<E>> registryKey;

   public static <E> RegistryLookupCodec<E> create(ResourceKey<? extends Registry<E>> p_135623_) {
      return new RegistryLookupCodec<>(p_135623_);
   }

   private RegistryLookupCodec(ResourceKey<? extends Registry<E>> p_135617_) {
      this.registryKey = p_135617_;
   }

   public <T> RecordBuilder<T> encode(Registry<E> p_135619_, DynamicOps<T> p_135620_, RecordBuilder<T> p_135621_) {
      return p_135621_;
   }

   public <T> DataResult<Registry<E>> decode(DynamicOps<T> p_135625_, MapLike<T> p_135626_) {
      return p_135625_ instanceof RegistryReadOps ? ((RegistryReadOps)p_135625_).registry(this.registryKey) : DataResult.error("Not a registry ops");
   }

   public String toString() {
      return "RegistryLookupCodec[" + this.registryKey + "]";
   }

   public <T> Stream<T> keys(DynamicOps<T> p_135632_) {
      return Stream.empty();
   }
}