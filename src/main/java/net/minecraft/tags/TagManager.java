package net.minecraft.tags;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TagManager implements PreparableReloadListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private final RegistryAccess registryAccess;
   private TagContainer tags = TagContainer.EMPTY;

   public TagManager(RegistryAccess p_144572_) {
      this.registryAccess = p_144572_;
   }

   public TagContainer getTags() {
      return this.tags;
   }

   public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier p_13482_, ResourceManager p_13483_, ProfilerFiller p_13484_, ProfilerFiller p_13485_, Executor p_13486_, Executor p_13487_) {
      List<TagManager.LoaderInfo<?>> list = Lists.newArrayList();
      StaticTags.visitHelpers((p_144583_) -> {
         TagManager.LoaderInfo<?> loaderinfo = this.createLoader(p_13483_, p_13486_, p_144583_);
         if (loaderinfo != null) {
            list.add(loaderinfo);
         }

      });
      return CompletableFuture.allOf(list.stream().map((p_144591_) -> {
         return p_144591_.pendingLoad;
      }).toArray((p_144574_) -> {
         return new CompletableFuture[p_144574_];
      })).thenCompose(p_13482_::wait).thenAcceptAsync((p_144594_) -> {
         TagContainer.Builder tagcontainer$builder = new TagContainer.Builder();
         list.forEach((p_144586_) -> {
            p_144586_.addToBuilder(tagcontainer$builder);
         });
         TagContainer tagcontainer = tagcontainer$builder.build();
         Multimap<ResourceKey<? extends Registry<?>>, ResourceLocation> multimap = StaticTags.getAllMissingTags(tagcontainer);
         if (!multimap.isEmpty()) {
            throw new IllegalStateException("Missing required tags: " + (String)multimap.entries().stream().map((p_144596_) -> {
               return p_144596_.getKey() + ":" + p_144596_.getValue();
            }).sorted().collect(Collectors.joining(",")));
         } else {
            SerializationTags.bind(tagcontainer);
            this.tags = tagcontainer;
         }
      }, p_13487_);
   }

   @Nullable
   private <T> TagManager.LoaderInfo<T> createLoader(ResourceManager p_144576_, Executor p_144577_, StaticTagHelper<T> p_144578_) {
      Optional<? extends Registry<T>> optional = this.registryAccess.registry(p_144578_.getKey());
      if (optional.isPresent()) {
         Registry<T> registry = optional.get();
         TagLoader<T> tagloader = new TagLoader<>(registry::getOptional, p_144578_.getDirectory());
         CompletableFuture<? extends TagCollection<T>> completablefuture = CompletableFuture.supplyAsync(() -> {
            return tagloader.loadAndBuild(p_144576_);
         }, p_144577_);
         return new TagManager.LoaderInfo<>(p_144578_, completablefuture);
      } else {
         LOGGER.warn("Can't find registry for {}", (Object)p_144578_.getKey());
         return null;
      }
   }

   static class LoaderInfo<T> {
      private final StaticTagHelper<T> helper;
      final CompletableFuture<? extends TagCollection<T>> pendingLoad;

      LoaderInfo(StaticTagHelper<T> p_144600_, CompletableFuture<? extends TagCollection<T>> p_144601_) {
         this.helper = p_144600_;
         this.pendingLoad = p_144601_;
      }

      public void addToBuilder(TagContainer.Builder p_144603_) {
         p_144603_.add(this.helper.getKey(), this.pendingLoad.join());
      }
   }
}