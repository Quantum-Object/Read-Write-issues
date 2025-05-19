import java.util.*;

class MemoryBlock {
    int start;
    int size;
    boolean isFree;
    String processId;

    MemoryBlock(int start, int size) {
        this.start = start;
        this.size = size;
        this.isFree = true;
        this.processId = "";
    }

    @Override
    public String toString() {
        return isFree ? "Free Block [start=" + start + ", size=" + size + "]" :
                "Used Block [Process=" + processId + ", start=" + start + ", size=" + size + "]";
    }
}

class ProcessSegment {
    String name;
    int size;

    ProcessSegment(String name, int size) {
        this.name = name;
        this.size = size;
    }
}

class SegmentedMemoryManager {
    private List<MemoryBlock> memory;

    SegmentedMemoryManager(int totalSize) {
        memory = new ArrayList<>();
        memory.add(new MemoryBlock(0, totalSize));
    }

    public boolean allocate(String processId, List<ProcessSegment> segments) {
        for (ProcessSegment segment : segments) {
            boolean allocated = false;
            for (int i = 0; i < memory.size(); i++) {
                MemoryBlock block = memory.get(i);
                if (block.isFree && block.size >= segment.size) {
                    MemoryBlock allocatedBlock = new MemoryBlock(block.start, segment.size);
                    allocatedBlock.isFree = false;
                    allocatedBlock.processId = processId + ":" + segment.name;
                    memory.set(i, allocatedBlock);

                    if (block.size > segment.size) {
                        memory.add(i + 1, new MemoryBlock(block.start + segment.size, block.size - segment.size));
                    }

                    allocated = true;
                    break;
                }
            }
            if (!allocated) {
                System.out.println("Not enough memory for segment: " + segment.name);
                return false;
            }
        }
        return true;
    }

    public void deallocate(String processId) {
        for (MemoryBlock block : memory) {
            if (!block.isFree && block.processId.startsWith(processId)) {
                block.isFree = true;
                block.processId = "";
            }
        }
        mergeFreeBlocks();
    }

    private void mergeFreeBlocks() {
        for (int i = 0; i < memory.size() - 1; ) {
            MemoryBlock current = memory.get(i);
            MemoryBlock next = memory.get(i + 1);
            if (current.isFree && next.isFree) {
                current.size += next.size;
                memory.remove(i + 1);
            } else {
                i++;
            }
        }
    }

    public void displayMemory() {
        for (MemoryBlock block : memory) {
            System.out.println(block);
        }
    }

    public void showExternalFragmentation() {
        int total = 0;
        for (MemoryBlock block : memory) {
            if (block.isFree) total += block.size;
        }
        System.out.println("Total External Fragmentation: " + total);
    }
}

public class SimulatorMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        SegmentedMemoryManager memoryManager = new SegmentedMemoryManager(1000); // 1000 units of memory

        while (true) {
            System.out.println("\n1. Allocate process (Segmentation)");
            System.out.println("2. Deallocate process");
            System.out.println("3. Display memory");
            System.out.println("4. Show external fragmentation");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter Process ID: ");
                    String pid = scanner.next();
                    List<ProcessSegment> segments = new ArrayList<>();
                    System.out.print("Enter Code Segment Size: ");
                    segments.add(new ProcessSegment("Code", scanner.nextInt()));
                    System.out.print("Enter Data Segment Size: ");
                    segments.add(new ProcessSegment("Data", scanner.nextInt()));
                    System.out.print("Enter Stack Segment Size: ");
                    segments.add(new ProcessSegment("Stack", scanner.nextInt()));
                    memoryManager.allocate(pid, segments);
                    break;
                case 2:
                    System.out.print("Enter Process ID to deallocate: ");
                    memoryManager.deallocate(scanner.next());
                    break;
                case 3:
                    memoryManager.displayMemory();
                    break;
                case 4:
                    memoryManager.showExternalFragmentation();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
