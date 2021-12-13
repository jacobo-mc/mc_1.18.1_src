package net.minecraft.data.advancements;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AdvancementProvider implements DataProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private final DataGenerator generator;
   private final List<Consumer<Consumer<Advancement>>> tabs = ImmutableList.of(new TheEndAdvancements(), new HusbandryAdvancements(), new AdventureAdvancements(), new NetherAdvancements(), new StoryAdvancements());

   public AdvancementProvider(DataGenerator p_123966_) {
      this.generator = p_123966_;
   }

   public void run(HashCache p_123969_) {
      Path path = this.generator.getOutputFolder();
      Set<ResourceLocation> set = Sets.newHashSet();
      Consumer<Advancement> consumer = (p_123977_) -> {
         if (!set.add(p_123977_.getId())) {
            throw new IllegalStateException("Duplicate advancement " + p_123977_.getId());
         } else {
            Path path1 = createPath(path, p_123977_);

            try {
               DataProvider.save(GSON, p_123969_, p_123977_.deconstruct().serializeToJson(), path1);
            } catch (IOException ioexception) {
               LOGGER.error("Couldn't save advancement {}", path1, ioexception);
            }

         }
      };

      for(Consumer<Consumer<Advancement>> consumer1 : this.tabs) {
         consumer1.accept(consumer);
      }

   }

   private static Path createPath(Path p_123971_, Advancement p_123972_) {
      return p_123971_.resolve("data/" + p_123972_.getId().getNamespace() + "/advancements/" + p_123972_.getId().getPath() + ".json");
   }

   public String getName() {
      return "Advancements";
   }
}