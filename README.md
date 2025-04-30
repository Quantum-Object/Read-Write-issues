# Milestone 1 – Reader-Writer Synchronization

This project demonstrates two classic synchronization strategies using C, POSIX threads, and semaphores for handling concurrent access to shared memory by multiple reader and writer threads.

## Objectives
- Implement **Reader-Priority** synchronization: allows multiple readers simultaneously unless a writer is writing.
- Implement **Writer-Priority** synchronization: prevents writer starvation by giving writers precedence once they request access.

## Features
- Reader and writer threads simulate concurrent access.
- Shared integer value used to demonstrate read/write operations.
- Mutual exclusion and synchronization handled via semaphores and mutexes.
- Includes both priority strategies as separate programs.

## Files
- `reader_priority.c`: Implements reader-priority synchronization.
- `writer_priority.c`: Implements writer-priority synchronization.
- `report.docx`: Detailed explanation of the strategies and sample output behavior.

## Compilation & Execution
```bash
gcc -pthread reader_priority.c -o reader_priority
./reader_priority

gcc -pthread writer_priority.c -o writer_priority
./writer_priority
```

