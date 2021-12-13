package net.minecraft.nbt.visitors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;

public class CollectFields extends CollectToTag {
   private int fieldsToGetCount;
   private final Set<TagType<?>> wantedTypes;
   private final Deque<CollectFields.StackFrame> stack = new ArrayDeque<>();

   public CollectFields(CollectFields.WantedField... p_197606_) {
      this.fieldsToGetCount = p_197606_.length;
      Builder<TagType<?>> builder = ImmutableSet.builder();
      CollectFields.StackFrame collectfields$stackframe = new CollectFields.StackFrame(1);

      for(CollectFields.WantedField collectfields$wantedfield : p_197606_) {
         collectfields$stackframe.addEntry(collectfields$wantedfield);
         builder.add(collectfields$wantedfield.type);
      }

      this.stack.push(collectfields$stackframe);
      builder.add(CompoundTag.TYPE);
      this.wantedTypes = builder.build();
   }

   public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> p_197614_) {
      return p_197614_ != CompoundTag.TYPE ? StreamTagVisitor.ValueResult.HALT : super.visitRootEntry(p_197614_);
   }

   public StreamTagVisitor.EntryResult visitEntry(TagType<?> p_197608_) {
      CollectFields.StackFrame collectfields$stackframe = this.stack.element();
      if (this.depth() > collectfields$stackframe.depth()) {
         return super.visitEntry(p_197608_);
      } else if (this.fieldsToGetCount <= 0) {
         return StreamTagVisitor.EntryResult.HALT;
      } else {
         return !this.wantedTypes.contains(p_197608_) ? StreamTagVisitor.EntryResult.SKIP : super.visitEntry(p_197608_);
      }
   }

   public StreamTagVisitor.EntryResult visitEntry(TagType<?> p_197610_, String p_197611_) {
      CollectFields.StackFrame collectfields$stackframe = this.stack.element();
      if (this.depth() > collectfields$stackframe.depth()) {
         return super.visitEntry(p_197610_, p_197611_);
      } else if (collectfields$stackframe.fieldsToGet.remove(p_197611_, p_197610_)) {
         --this.fieldsToGetCount;
         return super.visitEntry(p_197610_, p_197611_);
      } else {
         if (p_197610_ == CompoundTag.TYPE) {
            CollectFields.StackFrame collectfields$stackframe1 = collectfields$stackframe.fieldsToRecurse.get(p_197611_);
            if (collectfields$stackframe1 != null) {
               this.stack.push(collectfields$stackframe1);
               return super.visitEntry(p_197610_, p_197611_);
            }
         }

         return StreamTagVisitor.EntryResult.SKIP;
      }
   }

   public StreamTagVisitor.ValueResult visitContainerEnd() {
      if (this.depth() == this.stack.element().depth()) {
         this.stack.pop();
      }

      return super.visitContainerEnd();
   }

   public int getMissingFieldCount() {
      return this.fieldsToGetCount;
   }

   static record StackFrame(int depth, Map<String, TagType<?>> fieldsToGet, Map<String, CollectFields.StackFrame> fieldsToRecurse) {
      public StackFrame(int p_197620_) {
         this(p_197620_, new HashMap<>(), new HashMap<>());
      }

      public void addEntry(CollectFields.WantedField p_197629_) {
         if (this.depth <= p_197629_.path.size()) {
            this.fieldsToRecurse.computeIfAbsent(p_197629_.path.get(this.depth - 1), (p_197627_) -> {
               return new CollectFields.StackFrame(this.depth + 1);
            }).addEntry(p_197629_);
         } else {
            this.fieldsToGet.put(p_197629_.name, p_197629_.type);
         }

      }
   }

   public static record WantedField(List<String> path, TagType<?> type, String name) {
      public WantedField(TagType<?> p_197653_, String p_197654_) {
         this(List.of(), p_197653_, p_197654_);
      }

      public WantedField(String p_197645_, TagType<?> p_197646_, String p_197647_) {
         this(List.of(p_197645_), p_197646_, p_197647_);
      }

      public WantedField(String p_197640_, String p_197641_, TagType<?> p_197642_, String p_197643_) {
         this(List.of(p_197640_, p_197641_), p_197642_, p_197643_);
      }
   }
}