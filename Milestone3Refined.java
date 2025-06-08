import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class Milestone3Refined {

    // Shared memory segment with read-write locks
    static class SharedMemory {
        private final StringBuilder data = new StringBuilder();
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        public void write(String content) {
            lock.writeLock().lock();
            try {
                data.append(content).append(" ");
                System.out.println("Written: " + content + " | Data: " + data.toString().trim());
                Thread.sleep(100); // Simulate write delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lock.writeLock().unlock();
            }
        }

        public String read() {
            lock.readLock().lock();
            try {
                Thread.sleep(50); // Simulate read delay
                return data.toString().trim();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "";
            } finally {
                lock.readLock().unlock();
            }
        }

        public void clear() {
            lock.writeLock().lock();
            try {
                data.setLength(0);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    // Level 1: Writers
    static class Level1Writer implements Runnable {
        private final SharedMemory memory;
        private final int id;

        public Level1Writer(SharedMemory memory, int id) {
            this.memory = memory;
            this.id = id;
        }

        @Override
        public void run() {
            for (int i = 0; i < 3; i++) {
                String entry = "W" + id + "D" + i;
                System.out.println("[Level 1] Writer " + id + " writing: " + entry);
                memory.write(entry);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    // Level 2: Processor (reads from Level 1 memory and writes to Level 2 memory)
    static class Level2Processor implements Runnable {
        private final SharedMemory inputMemory;
        private final SharedMemory outputMemory;

        public Level2Processor(SharedMemory input, SharedMemory output) {
            this.inputMemory = input;
            this.outputMemory = output;
        }

        @Override
        public void run() {
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(300); // Wait for data to accumulate

                    String rawData;
                    // Exclusive snapshot
                    inputMemory.lock.readLock().lock();
                    try {
                        rawData = inputMemory.read();
                    } finally {
                        inputMemory.lock.readLock().unlock();
                    }

                    if (rawData.isEmpty()) continue;

                    System.out.println("[Level 2] Processor read: " + rawData);

                    String[] tokens = rawData.split("\\s+");
                    String processed = "PROCESSED[" + tokens.length + " items: " + String.join(",", tokens) + "]";

                    System.out.println("[Level 2] Processor writing: " + processed);
                    outputMemory.write(processed);

                    // Clear Level 1 after processing
                    inputMemory.clear();

                    Thread.sleep(400); // Simulate processing delay
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    // Level 3: Readers
    static class Level3Reader implements Runnable {
        private final SharedMemory memory;
        private final int id;

        public Level3Reader(SharedMemory memory, int id) {
            this.memory = memory;
            this.id = id;
        }

        @Override
        public void run() {
            for (int i = 0; i < 4; i++) {
                try {
                    Thread.sleep(500); // Wait for processed data

                    String data = memory.read();
                    System.out.println("[Level 3] Reader " + id + " read: " + data);
                    System.out.println("[Level 3] Reader " + id + " analyzing data...");
                    Thread.sleep(300); // Simulate analysis
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    // Main method to launch the pipeline
    public static void main(String[] args) {
        System.out.println("Multi-Level Reader-Writer System Starting...");
        System.out.println("============================================");

        SharedMemory level1Memory = new SharedMemory();
        SharedMemory level2Memory = new SharedMemory();

        ExecutorService executor = Executors.newFixedThreadPool(7);

        try {
            System.out.println("\n--- LEVEL 1: Writers ---");
            for (int i = 1; i <= 3; i++) {
                executor.submit(new Level1Writer(level1Memory, i));
            }

            System.out.println("\n--- LEVEL 2: Processor ---");
            executor.submit(new Level2Processor(level1Memory, level2Memory));

            System.out.println("\n--- LEVEL 3: Readers ---");
            for (int i = 1; i <= 3; i++) {
                executor.submit(new Level3Reader(level2Memory, i));
            }

            Thread.sleep(8000); // Run system for a duration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        }

        System.out.println("============================================");
        System.out.println("System Shutdown: Multi-Level Reader-Writer Complete.");
    }
}
