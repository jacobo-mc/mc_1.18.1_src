package net.minecraft.tags;

public class SerializationTags {
   private static volatile TagContainer instance = StaticTags.createCollection();

   public static TagContainer getInstance() {
      return instance;
   }

   public static void bind(TagContainer p_13203_) {
      instance = p_13203_;
   }
}