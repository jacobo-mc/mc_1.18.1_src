package net.minecraft.resources;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Registry;

public final class RegistryFileCodec<E> implements Codec<Supplier<E>> {
   private final ResourceKey<? extends Registry<E>> registryKey;
   private final Codec<E> elementCodec;
   private final boolean allowInline;

   public static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> p_135590_, Codec<E> p_135591_) {
      return create(p_135590_, p_135591_, true);
   }

   public static <E> Codec<List<Supplier<E>>> homogeneousList(ResourceKey<? extends Registry<E>> p_135601_, Codec<E> p_135602_) {
      return Codec.either(create(p_135601_, p_135602_, false).listOf(), p_135602_.<Supplier<E>>xmap((p_135604_) -> {
         return () -> {
            return p_135604_;
         };
      }, Supplier::get).listOf()).xmap((p_135578_) -> {
         return p_135578_.map((p_179856_) -> {
            return p_179856_;
         }, (p_179852_) -> {
            return p_179852_;
         });
      }, Either::left);
   }

   private static <E> RegistryFileCodec<E> create(ResourceKey<? extends Registry<E>> p_135593_, Codec<E> p_135594_, boolean p_135595_) {
      return new RegistryFileCodec<>(p_135593_, p_135594_, p_135595_);
   }

   private RegistryFileCodec(ResourceKey<? extends Registry<E>> p_135574_, Codec<E> p_135575_, boolean p_135576_) {
      this.registryKey = p_135574_;
      this.elementCodec = p_135575_;
      this.allowInline = p_135576_;
   }

   public <T> DataResult<T> encode(Supplier<E> p_135586_, DynamicOps<T> p_135587_, T p_135588_) {
      return p_135587_ instanceof RegistryWriteOps ? ((RegistryWriteOps)p_135587_).encode(p_135586_.get(), p_135588_, this.registryKey, this.elementCodec) : this.elementCodec.encode(p_135586_.get(), p_135587_, p_135588_);
   }

   public <T> DataResult<Pair<Supplier<E>, T>> decode(DynamicOps<T> p_135608_, T p_135609_) {
      return p_135608_ instanceof RegistryReadOps ? ((RegistryReadOps)p_135608_).decodeElement(p_135609_, this.registryKey, this.elementCodec, this.allowInline) : this.elementCodec.decode(p_135608_, p_135609_).map((p_135580_) -> {
         return p_135580_.mapFirst((p_179850_) -> {
            return () -> {
               return p_179850_;
            };
         });
      });
   }

   public String toString() {
      return "RegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
   }
}
