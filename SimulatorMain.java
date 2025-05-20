import java.util.*;

class Frame {
    int frameNumber;
    String processId;
    String segmentName;
    int pageNumber;
    boolean isFree;

    Frame(int frameNumber) {
        this.frameNumber = frameNumber;
        this.isFree = true;
        this.processId = "";
        this.segmentName = "";
        this.pageNumber = -1;
    }

    @Override
    public String toString() {
        return isFree ? "Free Frame [" + frameNumber + "]" :
                "Used Frame [Process=" + processId + ", Segment=" + segmentName + ", Page=" + pageNumber + ", Frame=" + frameNumber + "]";
    }
}

class Segment {
    String name;
    int size;

    Segment(String name, int size) {
        this.name = name;
        this.size = size;
    }
}

class PagingSegmentationManager {
    private List<Frame> frames;
    private int frameSize;

    PagingSegmentationManager(int totalMemorySize, int frameSize) {
        this.frameSize = frameSize;
        int frameCount = totalMemorySize / frameSize;
        frames = new ArrayList<>();
        for (int i = 0; i < frameCount; i++) {
            frames.add(new Frame(i));
        }
    }

    public boolean allocate(String processId, List<Segment> segments) {
        for (Segment segment : segments) {
            int pagesNeeded = (int) Math.ceil((double) segment.size / frameSize);
            List<Frame> availableFrames = new ArrayList<>();

            for (Frame frame : frames) {
                if (frame.isFree) {
                    availableFrames.add(frame);
                    if (availableFrames.size() == pagesNeeded) break;
                }
            }

            if (availableFrames.size() < pagesNeeded) {
                System.out.println("Not enough memory for segment: " + segment.name);
                return false;
            }

            for (int i = 0; i < pagesNeeded; i++) {
                Frame frame = availableFrames.get(i);
                frame.isFree = false;
                frame.processId = processId;
                frame.segmentName = segment.name;
                frame.pageNumber = i;
            }
        }
        return true;
    }

    public void deallocate(String processId) {
        for (Frame frame : frames) {
            if (!frame.isFree && frame.processId.equals(processId)) {
                frame.isFree = true;
                frame.processId = "";
                frame.segmentName = "";
                frame.pageNumber = -1;
            }
        }
    }

    public void displayMemory() {
        for (Frame frame : frames) {
            System.out.println(frame);
        }
    }

    public void showInternalFragmentation() {
        int totalWasted = 0;
        Map<String, Integer> processPages = new HashMap<>();
        for (Frame frame : frames) {
            if (!frame.isFree) {
                String key = frame.processId + ":" + frame.segmentName;
                processPages.put(key, processPages.getOrDefault(key, 0) + 1);
            }
        }

        for (String key : processPages.keySet()) {
            // Simulate average of half a page unused in each segment's last page
            totalWasted += frameSize / 2;
        }

        System.out.println("Estimated Internal Fragmentation: " + totalWasted + " units");
    }
}

public class SimulatorMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter total memory size: ");
        int totalMemory = scanner.nextInt();
        System.out.print("Enter frame size: ");
        int frameSize = scanner.nextInt();
        PagingSegmentationManager manager = new PagingSegmentationManager(totalMemory, frameSize);

        while (true) {
            System.out.println("\n1. Allocate process (Segmentation + Paging)");
            System.out.println("2. Deallocate process");
            System.out.println("3. Display memory");
            System.out.println("4. Show internal fragmentation");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter Process ID: ");
                    String pid = scanner.next();
                    List<Segment> segments = new ArrayList<>();
                    System.out.print("Enter Code Segment Size: ");
                    segments.add(new Segment("Code", scanner.nextInt()));
                    System.out.print("Enter Data Segment Size: ");
                    segments.add(new Segment("Data", scanner.nextInt()));
                    System.out.print("Enter Stack Segment Size: ");
                    segments.add(new Segment("Stack", scanner.nextInt()));
                    manager.allocate(pid, segments);
                    break;
                case 2:
                    System.out.print("Enter Process ID to deallocate: ");
                    manager.deallocate(scanner.next());
                    break;
                case 3:
                    manager.displayMemory();
                    break;
                case 4:
                    manager.showInternalFragmentation();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }
}
