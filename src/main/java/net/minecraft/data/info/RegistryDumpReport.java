package net.minecraft.data.info;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;

public class RegistryDumpReport implements DataProvider {
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private final DataGenerator generator;

   public RegistryDumpReport(DataGenerator p_124053_) {
      this.generator = p_124053_;
   }

   public void run(HashCache p_124061_) throws IOException {
      JsonObject jsonobject = new JsonObject();
      Registry.REGISTRY.keySet().forEach((p_124057_) -> {
         jsonobject.add(p_124057_.toString(), dumpRegistry(Registry.REGISTRY.get(p_124057_)));
      });
      Path path = this.generator.getOutputFolder().resolve("reports/registries.json");
      DataProvider.save(GSON, p_124061_, jsonobject, path);
   }

   private static <T> JsonElement dumpRegistry(Registry<T> p_124059_) {
      JsonObject jsonobject = new JsonObject();
      if (p_124059_ instanceof DefaultedRegistry) {
         ResourceLocation resourcelocation = ((DefaultedRegistry)p_124059_).getDefaultKey();
         jsonobject.addProperty("default", resourcelocation.toString());
      }

      int j = ((Registry)Registry.REGISTRY).getId(p_124059_);
      jsonobject.addProperty("protocol_id", j);
      JsonObject jsonobject1 = new JsonObject();

      for(ResourceLocation resourcelocation1 : p_124059_.keySet()) {
         T t = p_124059_.get(resourcelocation1);
         int i = p_124059_.getId(t);
         JsonObject jsonobject2 = new JsonObject();
         jsonobject2.addProperty("protocol_id", i);
         jsonobject1.add(resourcelocation1.toString(), jsonobject2);
      }

      jsonobject.add("entries", jsonobject1);
      return jsonobject;
   }

   public String getName() {
      return "Registry Dump";
   }
}
