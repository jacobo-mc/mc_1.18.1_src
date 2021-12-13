package net.minecraft.server.packs.resources;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.packs.PackResources;
import net.minecraft.util.Unit;

public interface ReloadableResourceManager extends ResourceManager, AutoCloseable {
   default CompletableFuture<Unit> reload(Executor p_10716_, Executor p_10717_, List<PackResources> p_10718_, CompletableFuture<Unit> p_10719_) {
      return this.createReload(p_10716_, p_10717_, p_10719_, p_10718_).done();
   }

   ReloadInstance createReload(Executor p_143930_, Executor p_143931_, CompletableFuture<Unit> p_143932_, List<PackResources> p_143933_);

   void registerReloadListener(PreparableReloadListener p_10714_);

   void close();
}