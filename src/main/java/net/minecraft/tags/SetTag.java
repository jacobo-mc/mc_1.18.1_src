package net.minecraft.tags;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;

public class SetTag<T> implements Tag<T> {
   private final ImmutableList<T> valuesList;
   private final Set<T> values;
   @VisibleForTesting
   protected final Class<?> closestCommonSuperType;

   protected SetTag(Set<T> p_13214_, Class<?> p_13215_) {
      this.closestCommonSuperType = p_13215_;
      this.values = p_13214_;
      this.valuesList = ImmutableList.copyOf(p_13214_);
   }

   public static <T> SetTag<T> empty() {
      return new SetTag<>(ImmutableSet.of(), Void.class);
   }

   public static <T> SetTag<T> create(Set<T> p_13223_) {
      return new SetTag<>(p_13223_, findCommonSuperClass(p_13223_));
   }

   public boolean contains(T p_13221_) {
      return this.closestCommonSuperType.isInstance(p_13221_) && this.values.contains(p_13221_);
   }

   public List<T> getValues() {
      return this.valuesList;
   }

   private static <T> Class<?> findCommonSuperClass(Set<T> p_13226_) {
      if (p_13226_.isEmpty()) {
         return Void.class;
      } else {
         Class<?> oclass = null;

         for(T t : p_13226_) {
            if (oclass == null) {
               oclass = t.getClass();
            } else {
               oclass = findClosestAncestor(oclass, t.getClass());
            }
         }

         return oclass;
      }
   }

   private static Class<?> findClosestAncestor(Class<?> p_13218_, Class<?> p_13219_) {
      while(!p_13218_.isAssignableFrom(p_13219_)) {
         p_13218_ = p_13218_.getSuperclass();
      }

      return p_13218_;
   }
}