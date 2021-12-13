package net.minecraft.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;

public class RegistryWriteOps<T> extends DelegatingOps<T> {
   private final RegistryAccess registryAccess;

   public static <T> RegistryWriteOps<T> create(DynamicOps<T> p_135768_, RegistryAccess p_135769_) {
      return new RegistryWriteOps<>(p_135768_, p_135769_);
   }

   private RegistryWriteOps(DynamicOps<T> p_135765_, RegistryAccess p_135766_) {
      super(p_135765_);
      this.registryAccess = p_135766_;
   }

   protected <E> DataResult<T> encode(E p_135771_, T p_135772_, ResourceKey<? extends Registry<E>> p_135773_, Codec<E> p_135774_) {
      Optional<? extends Registry<E>> optional = this.registryAccess.ownedRegistry(p_135773_);
      if (optional.isPresent()) {
         Registry<E> registry = optional.get();
         Optional<ResourceKey<E>> optional1 = registry.getResourceKey(p_135771_);
         if (optional1.isPresent()) {
            ResourceKey<E> resourcekey = optional1.get();
            return ResourceLocation.CODEC.encode(resourcekey.location(), this.delegate, p_135772_);
         }
      }

      return p_135774_.encode(p_135771_, this, p_135772_);
   }
}