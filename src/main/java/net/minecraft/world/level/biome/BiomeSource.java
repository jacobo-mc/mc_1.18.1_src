package net.minecraft.world.level.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Graph;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.apache.commons.lang3.mutable.MutableInt;

public abstract class BiomeSource implements BiomeResolver {
   public static final Codec<BiomeSource> CODEC = Registry.BIOME_SOURCE.byNameCodec().dispatchStable(BiomeSource::codec, Function.identity());
   private final Set<Biome> possibleBiomes;
   private final List<BiomeSource.StepFeatureData> featuresPerStep;

   protected BiomeSource(Stream<Supplier<Biome>> p_47896_) {
      this(p_47896_.map(Supplier::get).distinct().collect(ImmutableList.toImmutableList()));
   }

   protected BiomeSource(List<Biome> p_47894_) {
      this.possibleBiomes = new ObjectLinkedOpenHashSet<>(p_47894_);
      this.featuresPerStep = this.buildFeaturesPerStep(p_47894_, true);
   }

   private List<BiomeSource.StepFeatureData> buildFeaturesPerStep(List<Biome> p_186728_, boolean p_186729_) {
      Object2IntMap<PlacedFeature> object2intmap = new Object2IntOpenHashMap<>();
      MutableInt mutableint = new MutableInt(0);

      record FeatureData(int featureIndex, int step, PlacedFeature feature) {
      }

      Comparator<FeatureData> comparator = Comparator.comparingInt(FeatureData::step).thenComparingInt(FeatureData::featureIndex);
      Map<FeatureData, Set<FeatureData>> map = new TreeMap<>(comparator);
      int i = 0;

      for(Biome biome : p_186728_) {
         List<FeatureData> list = Lists.newArrayList();
         List<List<Supplier<PlacedFeature>>> list1 = biome.getGenerationSettings().features();
         i = Math.max(i, list1.size());

         for(int j = 0; j < list1.size(); ++j) {
            for(Supplier<PlacedFeature> supplier : list1.get(j)) {
               PlacedFeature placedfeature = supplier.get();
               list.add(new FeatureData(object2intmap.computeIfAbsent(placedfeature, (p_186732_) -> {
                  return mutableint.getAndIncrement();
               }), j, placedfeature));
            }
         }

         for(int l = 0; l < list.size(); ++l) {
            Set<FeatureData> set2 = map.computeIfAbsent(list.get(l), (p_186723_) -> {
               return new TreeSet<>(comparator);
            });
            if (l < list.size() - 1) {
               set2.add(list.get(l + 1));
            }
         }
      }

      Set<FeatureData> set = new TreeSet<>(comparator);
      Set<FeatureData> set1 = new TreeSet<>(comparator);
      List<FeatureData> list2 = Lists.newArrayList();

      for(FeatureData biomesource$1featuredata : map.keySet()) {
         if (!set1.isEmpty()) {
            throw new IllegalStateException("You somehow broke the universe; DFS bork (iteration finished with non-empty in-progress vertex set");
         }

         if (!set.contains(biomesource$1featuredata) && Graph.depthFirstSearch(map, set, set1, list2::add, biomesource$1featuredata)) {
            if (!p_186729_) {
               throw new IllegalStateException("Feature order cycle found");
            }

            List<Biome> list3 = new ArrayList<>(p_186728_);

            int k1;
            do {
               k1 = list3.size();
               ListIterator<Biome> listiterator = list3.listIterator();

               while(listiterator.hasNext()) {
                  Biome biome1 = listiterator.next();
                  listiterator.remove();

                  try {
                     this.buildFeaturesPerStep(list3, false);
                  } catch (IllegalStateException illegalstateexception) {
                     continue;
                  }

                  listiterator.add(biome1);
               }
            } while(k1 != list3.size());

            throw new IllegalStateException("Feature order cycle found, involved biomes: " + list3);
         }
      }

      Collections.reverse(list2);
      Builder<BiomeSource.StepFeatureData> builder = ImmutableList.builder();

      for(int i1 = 0; i1 < i; ++i1) {
         int j1 = i1;
         List<PlacedFeature> list4 = list2.stream().filter((p_186720_) -> {
            return p_186720_.step() == j1;
         }).map(FeatureData::feature).collect(Collectors.toList());
         int l1 = list4.size();
         Object2IntMap<PlacedFeature> object2intmap1 = new Object2IntOpenCustomHashMap<>(l1, Util.identityStrategy());

         for(int k = 0; k < l1; ++k) {
            object2intmap1.put(list4.get(k), k);
         }

         builder.add(new BiomeSource.StepFeatureData(list4, object2intmap1));
      }

      return builder.build();
   }

   protected abstract Codec<? extends BiomeSource> codec();

   public abstract BiomeSource withSeed(long p_47916_);

   public Set<Biome> possibleBiomes() {
      return this.possibleBiomes;
   }

   public Set<Biome> getBiomesWithin(int p_186705_, int p_186706_, int p_186707_, int p_186708_, Climate.Sampler p_186709_) {
      int i = QuartPos.fromBlock(p_186705_ - p_186708_);
      int j = QuartPos.fromBlock(p_186706_ - p_186708_);
      int k = QuartPos.fromBlock(p_186707_ - p_186708_);
      int l = QuartPos.fromBlock(p_186705_ + p_186708_);
      int i1 = QuartPos.fromBlock(p_186706_ + p_186708_);
      int j1 = QuartPos.fromBlock(p_186707_ + p_186708_);
      int k1 = l - i + 1;
      int l1 = i1 - j + 1;
      int i2 = j1 - k + 1;
      Set<Biome> set = Sets.newHashSet();

      for(int j2 = 0; j2 < i2; ++j2) {
         for(int k2 = 0; k2 < k1; ++k2) {
            for(int l2 = 0; l2 < l1; ++l2) {
               int i3 = i + k2;
               int j3 = j + l2;
               int k3 = k + j2;
               set.add(this.getNoiseBiome(i3, j3, k3, p_186709_));
            }
         }
      }

      return set;
   }

   @Nullable
   public BlockPos findBiomeHorizontal(int p_186711_, int p_186712_, int p_186713_, int p_186714_, Predicate<Biome> p_186715_, Random p_186716_, Climate.Sampler p_186717_) {
      return this.findBiomeHorizontal(p_186711_, p_186712_, p_186713_, p_186714_, 1, p_186715_, p_186716_, false, p_186717_);
   }

   @Nullable
   public BlockPos findBiomeHorizontal(int p_186696_, int p_186697_, int p_186698_, int p_186699_, int p_186700_, Predicate<Biome> p_186701_, Random p_186702_, boolean p_186703_, Climate.Sampler p_186704_) {
      int i = QuartPos.fromBlock(p_186696_);
      int j = QuartPos.fromBlock(p_186698_);
      int k = QuartPos.fromBlock(p_186699_);
      int l = QuartPos.fromBlock(p_186697_);
      BlockPos blockpos = null;
      int i1 = 0;
      int j1 = p_186703_ ? 0 : k;

      for(int k1 = j1; k1 <= k; k1 += p_186700_) {
         for(int l1 = SharedConstants.debugGenerateSquareTerrainWithoutNoise ? 0 : -k1; l1 <= k1; l1 += p_186700_) {
            boolean flag = Math.abs(l1) == k1;

            for(int i2 = -k1; i2 <= k1; i2 += p_186700_) {
               if (p_186703_) {
                  boolean flag1 = Math.abs(i2) == k1;
                  if (!flag1 && !flag) {
                     continue;
                  }
               }

               int k2 = i + i2;
               int j2 = j + l1;
               if (p_186701_.test(this.getNoiseBiome(k2, l, j2, p_186704_))) {
                  if (blockpos == null || p_186702_.nextInt(i1 + 1) == 0) {
                     blockpos = new BlockPos(QuartPos.toBlock(k2), p_186697_, QuartPos.toBlock(j2));
                     if (p_186703_) {
                        return blockpos;
                     }
                  }

                  ++i1;
               }
            }
         }
      }

      return blockpos;
   }

   public abstract Biome getNoiseBiome(int p_186735_, int p_186736_, int p_186737_, Climate.Sampler p_186738_);

   public void addMultinoiseDebugInfo(List<String> p_186724_, BlockPos p_186725_, Climate.Sampler p_186726_) {
   }

   public List<BiomeSource.StepFeatureData> featuresPerStep() {
      return this.featuresPerStep;
   }

   static {
      Registry.register(Registry.BIOME_SOURCE, "fixed", FixedBiomeSource.CODEC);
      Registry.register(Registry.BIOME_SOURCE, "multi_noise", MultiNoiseBiomeSource.CODEC);
      Registry.register(Registry.BIOME_SOURCE, "checkerboard", CheckerboardColumnBiomeSource.CODEC);
      Registry.register(Registry.BIOME_SOURCE, "the_end", TheEndBiomeSource.CODEC);
   }

   public static record StepFeatureData(List<PlacedFeature> features, ToIntFunction<PlacedFeature> indexMapping) {
   }
}