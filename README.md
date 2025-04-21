## You have a shared memory segment that:

{ 2 writer processes need to modify,

2 reader processes need to read from}

The challenge is coordinating access so that:

1.Writers get exclusive access (no other writers or readers while writing)

2.Multiple readers can read simultaneously (since reading doesn't change data)


## Writer-Priority Scenario
Ensure writers get immediate access when they're ready, preventing writer starvation.
When a writer wants to write:

1. It gets immediate priority over any waiting readers

2. All current readers must finish reading

3. No new readers can start while a writer is waiting or writing

## Reader-Priority Scenario
Maximize reader throughput when no writers are active.

- Multiple readers can read simultaneously

- When a writer wants to write:

   1. It must wait until all current readers finish

   2. New readers arriving while writer waits can still start reading

Only when no readers are active does the writer get exclusive access