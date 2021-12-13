package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class SinglePoolElement extends StructurePoolElement {
   private static final Codec<Either<ResourceLocation, StructureTemplate>> TEMPLATE_CODEC = Codec.of(SinglePoolElement::encodeTemplate, ResourceLocation.CODEC.map(Either::left));
   public static final Codec<SinglePoolElement> CODEC = RecordCodecBuilder.create((p_69118_) -> {
      return p_69118_.group(templateCodec(), processorsCodec(), projectionCodec()).apply(p_69118_, SinglePoolElement::new);
   });
   protected final Either<ResourceLocation, StructureTemplate> template;
   protected final Supplier<StructureProcessorList> processors;

   private static <T> DataResult<T> encodeTemplate(Either<ResourceLocation, StructureTemplate> p_69114_, DynamicOps<T> p_69115_, T p_69116_) {
      Optional<ResourceLocation> optional = p_69114_.left();
      return !optional.isPresent() ? DataResult.error("Can not serialize a runtime pool element") : ResourceLocation.CODEC.encode(optional.get(), p_69115_, p_69116_);
   }

   protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Supplier<StructureProcessorList>> processorsCodec() {
      return StructureProcessorType.LIST_CODEC.fieldOf("processors").forGetter((p_69148_) -> {
         return p_69148_.processors;
      });
   }

   protected static <E extends SinglePoolElement> RecordCodecBuilder<E, Either<ResourceLocation, StructureTemplate>> templateCodec() {
      return TEMPLATE_CODEC.fieldOf("location").forGetter((p_69112_) -> {
         return p_69112_.template;
      });
   }

   protected SinglePoolElement(Either<ResourceLocation, StructureTemplate> p_69102_, Supplier<StructureProcessorList> p_69103_, StructureTemplatePool.Projection p_69104_) {
      super(p_69104_);
      this.template = p_69102_;
      this.processors = p_69103_;
   }

   public SinglePoolElement(StructureTemplate p_69106_) {
      this(Either.right(p_69106_), () -> {
         return ProcessorLists.EMPTY;
      }, StructureTemplatePool.Projection.RIGID);
   }

   public Vec3i getSize(StructureManager p_161664_, Rotation p_161665_) {
      StructureTemplate structuretemplate = this.getTemplate(p_161664_);
      return structuretemplate.getSize(p_161665_);
   }

   private StructureTemplate getTemplate(StructureManager p_69120_) {
      return this.template.map(p_69120_::getOrCreate, Function.identity());
   }

   public List<StructureTemplate.StructureBlockInfo> getDataMarkers(StructureManager p_69142_, BlockPos p_69143_, Rotation p_69144_, boolean p_69145_) {
      StructureTemplate structuretemplate = this.getTemplate(p_69142_);
      List<StructureTemplate.StructureBlockInfo> list = structuretemplate.filterBlocks(p_69143_, (new StructurePlaceSettings()).setRotation(p_69144_), Blocks.STRUCTURE_BLOCK, p_69145_);
      List<StructureTemplate.StructureBlockInfo> list1 = Lists.newArrayList();

      for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : list) {
         if (structuretemplate$structureblockinfo.nbt != null) {
            StructureMode structuremode = StructureMode.valueOf(structuretemplate$structureblockinfo.nbt.getString("mode"));
            if (structuremode == StructureMode.DATA) {
               list1.add(structuretemplate$structureblockinfo);
            }
         }
      }

      return list1;
   }

   public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager p_69137_, BlockPos p_69138_, Rotation p_69139_, Random p_69140_) {
      StructureTemplate structuretemplate = this.getTemplate(p_69137_);
      List<StructureTemplate.StructureBlockInfo> list = structuretemplate.filterBlocks(p_69138_, (new StructurePlaceSettings()).setRotation(p_69139_), Blocks.JIGSAW, true);
      Collections.shuffle(list, p_69140_);
      return list;
   }

   public BoundingBox getBoundingBox(StructureManager p_69133_, BlockPos p_69134_, Rotation p_69135_) {
      StructureTemplate structuretemplate = this.getTemplate(p_69133_);
      return structuretemplate.getBoundingBox((new StructurePlaceSettings()).setRotation(p_69135_), p_69134_);
   }

   public boolean place(StructureManager p_69122_, WorldGenLevel p_69123_, StructureFeatureManager p_69124_, ChunkGenerator p_69125_, BlockPos p_69126_, BlockPos p_69127_, Rotation p_69128_, BoundingBox p_69129_, Random p_69130_, boolean p_69131_) {
      StructureTemplate structuretemplate = this.getTemplate(p_69122_);
      StructurePlaceSettings structureplacesettings = this.getSettings(p_69128_, p_69129_, p_69131_);
      if (!structuretemplate.placeInWorld(p_69123_, p_69126_, p_69127_, structureplacesettings, p_69130_, 18)) {
         return false;
      } else {
         for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : StructureTemplate.processBlockInfos(p_69123_, p_69126_, p_69127_, structureplacesettings, this.getDataMarkers(p_69122_, p_69126_, p_69128_, false))) {
            this.handleDataMarker(p_69123_, structuretemplate$structureblockinfo, p_69126_, p_69128_, p_69130_, p_69129_);
         }

         return true;
      }
   }

   protected StructurePlaceSettings getSettings(Rotation p_69108_, BoundingBox p_69109_, boolean p_69110_) {
      StructurePlaceSettings structureplacesettings = new StructurePlaceSettings();
      structureplacesettings.setBoundingBox(p_69109_);
      structureplacesettings.setRotation(p_69108_);
      structureplacesettings.setKnownShape(true);
      structureplacesettings.setIgnoreEntities(false);
      structureplacesettings.addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
      structureplacesettings.setFinalizeEntities(true);
      if (!p_69110_) {
         structureplacesettings.addProcessor(JigsawReplacementProcessor.INSTANCE);
      }

      this.processors.get().list().forEach(structureplacesettings::addProcessor);
      this.getProjection().getProcessors().forEach(structureplacesettings::addProcessor);
      return structureplacesettings;
   }

   public StructurePoolElementType<?> getType() {
      return StructurePoolElementType.SINGLE;
   }

   public String toString() {
      return "Single[" + this.template + "]";
   }
}