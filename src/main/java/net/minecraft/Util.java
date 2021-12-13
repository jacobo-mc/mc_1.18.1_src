package net.minecraft;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.MoreExecutors;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.Hash.Strategy;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.block.state.properties.Property;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

public class Util {
   static final Logger LOGGER = LogManager.getLogger();
   private static final int DEFAULT_MAX_THREADS = 255;
   private static final String MAX_THREADS_SYSTEM_PROPERTY = "max.bg.threads";
   private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
   private static final ExecutorService BOOTSTRAP_EXECUTOR = makeExecutor("Bootstrap");
   private static final ExecutorService BACKGROUND_EXECUTOR = makeExecutor("Main");
   private static final ExecutorService IO_POOL = makeIoExecutor();
   public static LongSupplier timeSource = System::nanoTime;
   public static final UUID NIL_UUID = new UUID(0L, 0L);
   public static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders().stream().filter((p_143794_) -> {
      return p_143794_.getScheme().equalsIgnoreCase("jar");
   }).findFirst().orElseThrow(() -> {
      return new IllegalStateException("No jar file system provider found");
   });
   private static Consumer<String> thePauser = (p_183990_) -> {
   };

   public static void preInitLog4j() {
   }

   public static <K, V> Collector<Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
      return Collectors.toMap(Entry::getKey, Entry::getValue);
   }

   public static <T extends Comparable<T>> String getPropertyName(Property<T> p_137454_, Object p_137455_) {
      return p_137454_.getName((T)(p_137455_));
   }

   public static String makeDescriptionId(String p_137493_, @Nullable ResourceLocation p_137494_) {
      return p_137494_ == null ? p_137493_ + ".unregistered_sadface" : p_137493_ + "." + p_137494_.getNamespace() + "." + p_137494_.getPath().replace('/', '.');
   }

   public static long getMillis() {
      return getNanos() / 1000000L;
   }

   public static long getNanos() {
      return timeSource.getAsLong();
   }

   public static long getEpochMillis() {
      return Instant.now().toEpochMilli();
   }

   private static ExecutorService makeExecutor(String p_137478_) {
      int i = Mth.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, getMaxThreads());
      ExecutorService executorservice;
      if (i <= 0) {
         executorservice = MoreExecutors.newDirectExecutorService();
      } else {
         executorservice = new ForkJoinPool(i, (p_183945_) -> {
            ForkJoinWorkerThread forkjoinworkerthread = new ForkJoinWorkerThread(p_183945_) {
               protected void onTermination(Throwable p_137590_) {
                  if (p_137590_ != null) {
                     Util.LOGGER.warn("{} died", this.getName(), p_137590_);
                  } else {
                     Util.LOGGER.debug("{} shutdown", (Object)this.getName());
                  }

                  super.onTermination(p_137590_);
               }
            };
            forkjoinworkerthread.setName("Worker-" + p_137478_ + "-" + WORKER_COUNT.getAndIncrement());
            return forkjoinworkerthread;
         }, Util::onThreadException, true);
      }

      return executorservice;
   }

   private static int getMaxThreads() {
      String s = System.getProperty("max.bg.threads");
      if (s != null) {
         try {
            int i = Integer.parseInt(s);
            if (i >= 1 && i <= 255) {
               return i;
            }

            LOGGER.error("Wrong {} property value '{}'. Should be an integer value between 1 and {}.", "max.bg.threads", s, 255);
         } catch (NumberFormatException numberformatexception) {
            LOGGER.error("Could not parse {} property value '{}'. Should be an integer value between 1 and {}.", "max.bg.threads", s, 255);
         }
      }

      return 255;
   }

   public static ExecutorService bootstrapExecutor() {
      return BOOTSTRAP_EXECUTOR;
   }

   public static ExecutorService backgroundExecutor() {
      return BACKGROUND_EXECUTOR;
   }

   public static ExecutorService ioPool() {
      return IO_POOL;
   }

   public static void shutdownExecutors() {
      shutdownExecutor(BACKGROUND_EXECUTOR);
      shutdownExecutor(IO_POOL);
   }

   private static void shutdownExecutor(ExecutorService p_137532_) {
      p_137532_.shutdown();

      boolean flag;
      try {
         flag = p_137532_.awaitTermination(3L, TimeUnit.SECONDS);
      } catch (InterruptedException interruptedexception) {
         flag = false;
      }

      if (!flag) {
         p_137532_.shutdownNow();
      }

   }

   private static ExecutorService makeIoExecutor() {
      return Executors.newCachedThreadPool((p_183942_) -> {
         Thread thread = new Thread(p_183942_);
         thread.setName("IO-Worker-" + WORKER_COUNT.getAndIncrement());
         thread.setUncaughtExceptionHandler(Util::onThreadException);
         return thread;
      });
   }

   public static <T> CompletableFuture<T> failedFuture(Throwable p_137499_) {
      CompletableFuture<T> completablefuture = new CompletableFuture<>();
      completablefuture.completeExceptionally(p_137499_);
      return completablefuture;
   }

   public static void throwAsRuntime(Throwable p_137560_) {
      throw p_137560_ instanceof RuntimeException ? (RuntimeException)p_137560_ : new RuntimeException(p_137560_);
   }

   private static void onThreadException(Thread p_137496_, Throwable p_137497_) {
      pauseInIde(p_137497_);
      if (p_137497_ instanceof CompletionException) {
         p_137497_ = p_137497_.getCause();
      }

      if (p_137497_ instanceof ReportedException) {
         Bootstrap.realStdoutPrintln(((ReportedException)p_137497_).getReport().getFriendlyReport());
         System.exit(-1);
      }

      LOGGER.error(String.format("Caught exception in thread %s", p_137496_), p_137497_);
   }

   @Nullable
   public static Type<?> fetchChoiceType(TypeReference p_137457_, String p_137458_) {
      return !SharedConstants.CHECK_DATA_FIXER_SCHEMA ? null : doFetchChoiceType(p_137457_, p_137458_);
   }

   @Nullable
   private static Type<?> doFetchChoiceType(TypeReference p_137552_, String p_137553_) {
      Type<?> type = null;

      try {
         type = DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getWorldVersion())).getChoiceType(p_137552_, p_137553_);
      } catch (IllegalArgumentException illegalargumentexception) {
         LOGGER.error("No data fixer registered for {}", (Object)p_137553_);
         if (SharedConstants.IS_RUNNING_IN_IDE) {
            throw illegalargumentexception;
         }
      }

      return type;
   }

   public static Runnable wrapThreadWithTaskName(String p_143788_, Runnable p_143789_) {
      return SharedConstants.IS_RUNNING_IN_IDE ? () -> {
         Thread thread = Thread.currentThread();
         String s = thread.getName();
         thread.setName(p_143788_);

         try {
            p_143789_.run();
         } finally {
            thread.setName(s);
         }

      } : p_143789_;
   }

   public static <V> Supplier<V> wrapThreadWithTaskName(String p_183947_, Supplier<V> p_183948_) {
      return SharedConstants.IS_RUNNING_IN_IDE ? () -> {
         Thread thread = Thread.currentThread();
         String s = thread.getName();
         thread.setName(p_183947_);

         Object object;
         try {
            object = p_183948_.get();
         } finally {
            thread.setName(s);
         }

         return (V)object;
      } : p_183948_;
   }

   public static Util.OS getPlatform() {
      String s = System.getProperty("os.name").toLowerCase(Locale.ROOT);
      if (s.contains("win")) {
         return Util.OS.WINDOWS;
      } else if (s.contains("mac")) {
         return Util.OS.OSX;
      } else if (s.contains("solaris")) {
         return Util.OS.SOLARIS;
      } else if (s.contains("sunos")) {
         return Util.OS.SOLARIS;
      } else if (s.contains("linux")) {
         return Util.OS.LINUX;
      } else {
         return s.contains("unix") ? Util.OS.LINUX : Util.OS.UNKNOWN;
      }
   }

   public static Stream<String> getVmArguments() {
      RuntimeMXBean runtimemxbean = ManagementFactory.getRuntimeMXBean();
      return runtimemxbean.getInputArguments().stream().filter((p_183987_) -> {
         return p_183987_.startsWith("-X");
      });
   }

   public static <T> T lastOf(List<T> p_137510_) {
      return p_137510_.get(p_137510_.size() - 1);
   }

   public static <T> T findNextInIterable(Iterable<T> p_137467_, @Nullable T p_137468_) {
      Iterator<T> iterator = p_137467_.iterator();
      T t = iterator.next();
      if (p_137468_ != null) {
         T t1 = t;

         while(t1 != p_137468_) {
            if (iterator.hasNext()) {
               t1 = iterator.next();
            }
         }

         if (iterator.hasNext()) {
            return iterator.next();
         }
      }

      return t;
   }

   public static <T> T findPreviousInIterable(Iterable<T> p_137555_, @Nullable T p_137556_) {
      Iterator<T> iterator = p_137555_.iterator();

      T t;
      T t1;
      for(t = null; iterator.hasNext(); t = t1) {
         t1 = iterator.next();
         if (t1 == p_137556_) {
            if (t == null) {
               t = (T)(iterator.hasNext() ? Iterators.getLast(iterator) : p_137556_);
            }
            break;
         }
      }

      return t;
   }

   public static <T> T make(Supplier<T> p_137538_) {
      return p_137538_.get();
   }

   public static <T> T make(T p_137470_, Consumer<T> p_137471_) {
      p_137471_.accept(p_137470_);
      return p_137470_;
   }

   public static <K> Strategy<K> identityStrategy() {
      return (Strategy<K>) Util.IdentityStrategy.INSTANCE;
   }

   public static <V> CompletableFuture<List<V>> sequence(List<? extends CompletableFuture<? extends V>> p_137568_) {
      return p_137568_.stream().reduce(CompletableFuture.completedFuture(Lists.newArrayList()), (p_143819_, p_143820_) -> {
         return p_143820_.thenCombine(p_143819_, (p_183939_, p_183940_) -> {
            List<V> list = Lists.newArrayListWithCapacity(p_183940_.size() + 1);
            list.addAll(p_183940_);
            list.add(p_183939_);
            return list;
         });
      }, (p_183967_, p_183968_) -> {
         return p_183967_.thenCombine(p_183968_, (p_183953_, p_183954_) -> {
            List<V> list = Lists.newArrayListWithCapacity(p_183953_.size() + p_183954_.size());
            list.addAll(p_183953_);
            list.addAll(p_183954_);
            return list;
         });
      });
   }

   public static <V> CompletableFuture<List<V>> sequenceFailFast(List<? extends CompletableFuture<? extends V>> p_143841_) {
      List<V> list = Lists.newArrayListWithCapacity(p_143841_.size());
      CompletableFuture<?>[] completablefuture = new CompletableFuture[p_143841_.size()];
      CompletableFuture<Void> completablefuture1 = new CompletableFuture<>();
      p_143841_.forEach((p_183959_) -> {
         int i = list.size();
         list.add((V)null);
         completablefuture[i] = p_183959_.whenComplete((p_183964_, p_183965_) -> {
            if (p_183965_ != null) {
               completablefuture1.completeExceptionally(p_183965_);
            } else {
               list.set(i, p_183964_);
            }

         });
      });
      return CompletableFuture.allOf(completablefuture).applyToEither(completablefuture1, (p_183951_) -> {
         return list;
      });
   }

   public static <T> Stream<T> toStream(Optional<? extends T> p_137520_) {
      return DataFixUtils.orElseGet(p_137520_.map(Stream::of), Stream::empty);
   }

   public static <T> Optional<T> ifElse(Optional<T> p_137522_, Consumer<T> p_137523_, Runnable p_137524_) {
      if (p_137522_.isPresent()) {
         p_137523_.accept(p_137522_.get());
      } else {
         p_137524_.run();
      }

      return p_137522_;
   }

   public static Runnable name(Runnable p_137475_, Supplier<String> p_137476_) {
      return p_137475_;
   }

   public static void logAndPauseIfInIde(String p_143786_) {
      LOGGER.error(p_143786_);
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         doPause(p_143786_);
      }

   }

   public static void logAndPauseIfInIde(String p_200891_, Throwable p_200892_) {
      LOGGER.error(p_200891_, p_200892_);
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         doPause(p_200891_);
      }

   }

   public static <T extends Throwable> T pauseInIde(T p_137571_) {
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         LOGGER.error("Trying to throw a fatal exception, pausing in IDE", p_137571_);
         doPause(p_137571_.getMessage());
      }

      return p_137571_;
   }

   public static void setPause(Consumer<String> p_183970_) {
      thePauser = p_183970_;
   }

   private static void doPause(String p_183985_) {
      Instant instant = Instant.now();
      LOGGER.warn("Did you remember to set a breakpoint here?");
      boolean flag = Duration.between(instant, Instant.now()).toMillis() > 500L;
      if (!flag) {
         thePauser.accept(p_183985_);
      }

   }

   public static String describeError(Throwable p_137576_) {
      if (p_137576_.getCause() != null) {
         return describeError(p_137576_.getCause());
      } else {
         return p_137576_.getMessage() != null ? p_137576_.getMessage() : p_137576_.toString();
      }
   }

   public static <T> T getRandom(T[] p_137546_, Random p_137547_) {
      return p_137546_[p_137547_.nextInt(p_137546_.length)];
   }

   public static int getRandom(int[] p_137543_, Random p_137544_) {
      return p_137543_[p_137544_.nextInt(p_137543_.length)];
   }

   public static <T> T getRandom(List<T> p_143805_, Random p_143806_) {
      return p_143805_.get(p_143806_.nextInt(p_143805_.size()));
   }

   private static BooleanSupplier createRenamer(final Path p_137503_, final Path p_137504_) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            try {
               Files.move(p_137503_, p_137504_);
               return true;
            } catch (IOException ioexception) {
               Util.LOGGER.error("Failed to rename", (Throwable)ioexception);
               return false;
            }
         }

         public String toString() {
            return "rename " + p_137503_ + " to " + p_137504_;
         }
      };
   }

   private static BooleanSupplier createDeleter(final Path p_137501_) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            try {
               Files.deleteIfExists(p_137501_);
               return true;
            } catch (IOException ioexception) {
               Util.LOGGER.warn("Failed to delete", (Throwable)ioexception);
               return false;
            }
         }

         public String toString() {
            return "delete old " + p_137501_;
         }
      };
   }

   private static BooleanSupplier createFileDeletedCheck(final Path p_137562_) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            return !Files.exists(p_137562_);
         }

         public String toString() {
            return "verify that " + p_137562_ + " is deleted";
         }
      };
   }

   private static BooleanSupplier createFileCreatedCheck(final Path p_137573_) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            return Files.isRegularFile(p_137573_);
         }

         public String toString() {
            return "verify that " + p_137573_ + " is present";
         }
      };
   }

   private static boolean executeInSequence(BooleanSupplier... p_137549_) {
      for(BooleanSupplier booleansupplier : p_137549_) {
         if (!booleansupplier.getAsBoolean()) {
            LOGGER.warn("Failed to execute {}", (Object)booleansupplier);
            return false;
         }
      }

      return true;
   }

   private static boolean runWithRetries(int p_137450_, String p_137451_, BooleanSupplier... p_137452_) {
      for(int i = 0; i < p_137450_; ++i) {
         if (executeInSequence(p_137452_)) {
            return true;
         }

         LOGGER.error("Failed to {}, retrying {}/{}", p_137451_, i, p_137450_);
      }

      LOGGER.error("Failed to {}, aborting, progress might be lost", (Object)p_137451_);
      return false;
   }

   public static void safeReplaceFile(File p_137463_, File p_137464_, File p_137465_) {
      safeReplaceFile(p_137463_.toPath(), p_137464_.toPath(), p_137465_.toPath());
   }

   public static void safeReplaceFile(Path p_137506_, Path p_137507_, Path p_137508_) {
      int i = 10;
      if (!Files.exists(p_137506_) || runWithRetries(10, "create backup " + p_137508_, createDeleter(p_137508_), createRenamer(p_137506_, p_137508_), createFileCreatedCheck(p_137508_))) {
         if (runWithRetries(10, "remove old " + p_137506_, createDeleter(p_137506_), createFileDeletedCheck(p_137506_))) {
            if (!runWithRetries(10, "replace " + p_137506_ + " with " + p_137507_, createRenamer(p_137507_, p_137506_), createFileCreatedCheck(p_137506_))) {
               runWithRetries(10, "restore " + p_137506_ + " from " + p_137508_, createRenamer(p_137508_, p_137506_), createFileCreatedCheck(p_137506_));
            }

         }
      }
   }

   public static int offsetByCodepoints(String p_137480_, int p_137481_, int p_137482_) {
      int i = p_137480_.length();
      if (p_137482_ >= 0) {
         for(int j = 0; p_137481_ < i && j < p_137482_; ++j) {
            if (Character.isHighSurrogate(p_137480_.charAt(p_137481_++)) && p_137481_ < i && Character.isLowSurrogate(p_137480_.charAt(p_137481_))) {
               ++p_137481_;
            }
         }
      } else {
         for(int k = p_137482_; p_137481_ > 0 && k < 0; ++k) {
            --p_137481_;
            if (Character.isLowSurrogate(p_137480_.charAt(p_137481_)) && p_137481_ > 0 && Character.isHighSurrogate(p_137480_.charAt(p_137481_ - 1))) {
               --p_137481_;
            }
         }
      }

      return p_137481_;
   }

   public static Consumer<String> prefix(String p_137490_, Consumer<String> p_137491_) {
      return (p_183974_) -> {
         p_137491_.accept(p_137490_ + p_183974_);
      };
   }

   public static DataResult<int[]> fixedSize(IntStream p_137540_, int p_137541_) {
      int[] aint = p_137540_.limit((long)(p_137541_ + 1)).toArray();
      if (aint.length != p_137541_) {
         String s = "Input is not a list of " + p_137541_ + " ints";
         return aint.length >= p_137541_ ? DataResult.error(s, Arrays.copyOf(aint, p_137541_)) : DataResult.error(s);
      } else {
         return DataResult.success(aint);
      }
   }

   public static <T> DataResult<List<T>> fixedSize(List<T> p_143796_, int p_143797_) {
      if (p_143796_.size() != p_143797_) {
         String s = "Input is not a list of " + p_143797_ + " elements";
         return p_143796_.size() >= p_143797_ ? DataResult.error(s, p_143796_.subList(0, p_143797_)) : DataResult.error(s);
      } else {
         return DataResult.success(p_143796_);
      }
   }

   public static void startTimerHackThread() {
      Thread thread = new Thread("Timer hack thread") {
         public void run() {
            while(true) {
               try {
                  Thread.sleep(2147483647L);
               } catch (InterruptedException interruptedexception) {
                  Util.LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                  return;
               }
            }
         }
      };
      thread.setDaemon(true);
      thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
      thread.start();
   }

   public static void copyBetweenDirs(Path p_137564_, Path p_137565_, Path p_137566_) throws IOException {
      Path path = p_137564_.relativize(p_137566_);
      Path path1 = p_137565_.resolve(path);
      Files.copy(p_137566_, path1);
   }

   public static String sanitizeName(String p_137484_, CharPredicate p_137485_) {
      return p_137484_.toLowerCase(Locale.ROOT).chars().mapToObj((p_183977_) -> {
         return p_137485_.test((char)p_183977_) ? Character.toString((char)p_183977_) : "_";
      }).collect(Collectors.joining());
   }

   public static <T, R> Function<T, R> memoize(final Function<T, R> p_143828_) {
      return new Function<T, R>() {
         private final Map<T, R> cache = Maps.newHashMap();

         public R apply(T p_143849_) {
            return this.cache.computeIfAbsent(p_143849_, p_143828_);
         }

         public String toString() {
            return "memoize/1[function=" + p_143828_ + ", size=" + this.cache.size() + "]";
         }
      };
   }

   public static <T, U, R> BiFunction<T, U, R> memoize(final BiFunction<T, U, R> p_143822_) {
      return new BiFunction<T, U, R>() {
         private final Map<Pair<T, U>, R> cache = Maps.newHashMap();

         public R apply(T p_143859_, U p_143860_) {
            return this.cache.computeIfAbsent(Pair.of(p_143859_, p_143860_), (p_143857_) -> {
               return p_143822_.apply(p_143857_.getFirst(), p_143857_.getSecond());
            });
         }

         public String toString() {
            return "memoize/2[function=" + p_143822_ + ", size=" + this.cache.size() + "]";
         }
      };
   }

   static {
      System.setProperty("log4j2.formatMsgNoLookups", "true");
      LoggerContext loggercontext = LoggerContext.getContext(false);
      PropertyChangeListener propertychangelistener = (p_201393_) -> {
         Configuration configuration = loggercontext.getConfiguration();
         if (configuration != null) {
            StrSubstitutor strsubstitutor = configuration.getStrSubstitutor();
            if (strsubstitutor != null) {
               strsubstitutor.setVariableResolver((StrLookup)null);
            }
         }

      };
      propertychangelistener.propertyChange((PropertyChangeEvent)null);
      loggercontext.addPropertyChangeListener(propertychangelistener);
   }

   static enum IdentityStrategy implements Strategy<Object> {
      INSTANCE;

      public int hashCode(Object p_137626_) {
         return System.identityHashCode(p_137626_);
      }

      public boolean equals(Object p_137623_, Object p_137624_) {
         return p_137623_ == p_137624_;
      }
   }

   public static enum OS {
      LINUX("linux"),
      SOLARIS("solaris"),
      WINDOWS("windows") {
         protected String[] getOpenUrlArguments(URL p_137662_) {
            return new String[]{"rundll32", "url.dll,FileProtocolHandler", p_137662_.toString()};
         }
      },
      OSX("mac") {
         protected String[] getOpenUrlArguments(URL p_137667_) {
            return new String[]{"open", p_137667_.toString()};
         }
      },
      UNKNOWN("unknown");

      private final String telemetryName;

      OS(String p_183998_) {
         this.telemetryName = p_183998_;
      }

      public void openUrl(URL p_137651_) {
         try {
            Process process = AccessController.doPrivileged((PrivilegedExceptionAction<Process>)(() -> {
               return Runtime.getRuntime().exec(this.getOpenUrlArguments(p_137651_));
            }));

            for(String s : IOUtils.readLines(process.getErrorStream())) {
               Util.LOGGER.error(s);
            }

            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();
         } catch (IOException | PrivilegedActionException privilegedactionexception) {
            Util.LOGGER.error("Couldn't open url '{}'", p_137651_, privilegedactionexception);
         }

      }

      public void openUri(URI p_137649_) {
         try {
            this.openUrl(p_137649_.toURL());
         } catch (MalformedURLException malformedurlexception) {
            Util.LOGGER.error("Couldn't open uri '{}'", p_137649_, malformedurlexception);
         }

      }

      public void openFile(File p_137645_) {
         try {
            this.openUrl(p_137645_.toURI().toURL());
         } catch (MalformedURLException malformedurlexception) {
            Util.LOGGER.error("Couldn't open file '{}'", p_137645_, malformedurlexception);
         }

      }

      protected String[] getOpenUrlArguments(URL p_137652_) {
         String s = p_137652_.toString();
         if ("file".equals(p_137652_.getProtocol())) {
            s = s.replace("file:", "file://");
         }

         return new String[]{"xdg-open", s};
      }

      public void openUri(String p_137647_) {
         try {
            this.openUrl((new URI(p_137647_)).toURL());
         } catch (MalformedURLException | IllegalArgumentException | URISyntaxException urisyntaxexception) {
            Util.LOGGER.error("Couldn't open uri '{}'", p_137647_, urisyntaxexception);
         }

      }

      public String telemetryName() {
         return this.telemetryName;
      }
   }
}
