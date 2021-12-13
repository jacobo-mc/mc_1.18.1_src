package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleReloadableResourceManager implements ReloadableResourceManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map<String, FallbackResourceManager> namespacedPacks = Maps.newHashMap();
   private final List<PreparableReloadListener> listeners = Lists.newArrayList();
   private final Set<String> namespaces = Sets.newLinkedHashSet();
   private final List<PackResources> packs = Lists.newArrayList();
   private final PackType type;

   public SimpleReloadableResourceManager(PackType p_10878_) {
      this.type = p_10878_;
   }

   public void add(PackResources p_10881_) {
      this.packs.add(p_10881_);

      for(String s : p_10881_.getNamespaces(this.type)) {
         this.namespaces.add(s);
         FallbackResourceManager fallbackresourcemanager = this.namespacedPacks.get(s);
         if (fallbackresourcemanager == null) {
            fallbackresourcemanager = new FallbackResourceManager(this.type, s);
            this.namespacedPacks.put(s, fallbackresourcemanager);
         }

         fallbackresourcemanager.add(p_10881_);
      }

   }

   public Set<String> getNamespaces() {
      return this.namespaces;
   }

   public Resource getResource(ResourceLocation p_10895_) throws IOException {
      ResourceManager resourcemanager = this.namespacedPacks.get(p_10895_.getNamespace());
      if (resourcemanager != null) {
         return resourcemanager.getResource(p_10895_);
      } else {
         throw new FileNotFoundException(p_10895_.toString());
      }
   }

   public boolean hasResource(ResourceLocation p_10903_) {
      ResourceManager resourcemanager = this.namespacedPacks.get(p_10903_.getNamespace());
      return resourcemanager != null ? resourcemanager.hasResource(p_10903_) : false;
   }

   public List<Resource> getResources(ResourceLocation p_10906_) throws IOException {
      ResourceManager resourcemanager = this.namespacedPacks.get(p_10906_.getNamespace());
      if (resourcemanager != null) {
         return resourcemanager.getResources(p_10906_);
      } else {
         throw new FileNotFoundException(p_10906_.toString());
      }
   }

   public Collection<ResourceLocation> listResources(String p_10885_, Predicate<String> p_10886_) {
      Set<ResourceLocation> set = Sets.newHashSet();

      for(FallbackResourceManager fallbackresourcemanager : this.namespacedPacks.values()) {
         set.addAll(fallbackresourcemanager.listResources(p_10885_, p_10886_));
      }

      List<ResourceLocation> list = Lists.newArrayList(set);
      Collections.sort(list);
      return list;
   }

   private void clear() {
      this.namespacedPacks.clear();
      this.namespaces.clear();
      this.packs.forEach(PackResources::close);
      this.packs.clear();
   }

   public void close() {
      this.clear();
   }

   public void registerReloadListener(PreparableReloadListener p_10883_) {
      this.listeners.add(p_10883_);
   }

   public ReloadInstance createReload(Executor p_143947_, Executor p_143948_, CompletableFuture<Unit> p_143949_, List<PackResources> p_143950_) {
      LOGGER.info("Reloading ResourceManager: {}", () -> {
         return p_143950_.stream().map(PackResources::getName).collect(Collectors.joining(", "));
      });
      this.clear();

      for(PackResources packresources : p_143950_) {
         try {
            this.add(packresources);
         } catch (Exception exception) {
            LOGGER.error("Failed to add resource pack {}", packresources.getName(), exception);
            return new SimpleReloadableResourceManager.FailingReloadInstance(new SimpleReloadableResourceManager.ResourcePackLoadingFailure(packresources, exception));
         }
      }

      return (ReloadInstance)(LOGGER.isDebugEnabled() ? new ProfiledReloadInstance(this, Lists.newArrayList(this.listeners), p_143947_, p_143948_, p_143949_) : SimpleReloadInstance.of(this, Lists.newArrayList(this.listeners), p_143947_, p_143948_, p_143949_));
   }

   public Stream<PackResources> listPacks() {
      return this.packs.stream();
   }

   static class FailingReloadInstance implements ReloadInstance {
      private final SimpleReloadableResourceManager.ResourcePackLoadingFailure exception;
      private final CompletableFuture<Unit> failedFuture;

      public FailingReloadInstance(SimpleReloadableResourceManager.ResourcePackLoadingFailure p_10911_) {
         this.exception = p_10911_;
         this.failedFuture = new CompletableFuture<>();
         this.failedFuture.completeExceptionally(p_10911_);
      }

      public CompletableFuture<Unit> done() {
         return this.failedFuture;
      }

      public float getActualProgress() {
         return 0.0F;
      }

      public boolean isDone() {
         return true;
      }

      public void checkExceptions() {
         throw this.exception;
      }
   }

   public static class ResourcePackLoadingFailure extends RuntimeException {
      private final PackResources pack;

      public ResourcePackLoadingFailure(PackResources p_10919_, Throwable p_10920_) {
         super(p_10919_.getName(), p_10920_);
         this.pack = p_10919_;
      }

      public PackResources getPack() {
         return this.pack;
      }
   }
}