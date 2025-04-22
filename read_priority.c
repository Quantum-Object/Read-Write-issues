#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <semaphore.h>
#include <unistd.h>

#define NUM_READERS 2
#define NUM_WRITERS 2



// ----------------------- COMMON DATA -----------------------
typedef struct {
    int value;
    sem_t mutex;
    sem_t write_sem;
    int reader_count;
} SharedData;

SharedData data;

void init_data() {
    data.value = 0;
    sem_init(&data.mutex, 0, 1);
    sem_init(&data.write_sem, 0, 1);
    data.reader_count = 0;
}
 // ----------------------- READER -----------------------


void* reader(void* arg) {
    int id = *(int*)arg; 
    while (1) { 
        sem_wait(&data.mutex);  // ---------> lock the mutex 
        data.reader_count++; 

        if (data.reader_count == 1) {
            sem_wait(&data.write_sem);
        }
        sem_post(&data.mutex); //  ---------> unlock the mutex, NOTE that this is done before the actual read


        // Reading the shared data
        printf("Reader %d reads: %d\n", id, data.value);
        sleep(2);




        sem_wait(&data.mutex);
        data.reader_count--;


        if (data.reader_count == 0) {
            sem_post(&data.write_sem);
        }

        sem_post(&data.mutex);

        sleep(2);
    }
    return NULL;
}
// ----------------------- WRITER -----------------------

void* writer(void* arg) {
    int id = *(int*)arg;
    while (1) {
        sem_wait(&data.write_sem); // a writer waits indifinitely for the mutex
        
        data.value++;    // WRITING for the sake of example
        printf("Writer %d writes: %d\n", id, data.value);
        sleep(2);
        
        sem_post(&data.write_sem);
        sleep(2);
    }
    return NULL;
}


// ---------------------- MAIN ----------------------
int main() {
    pthread_t readers[NUM_READERS], writers[NUM_WRITERS];
    int reader_ids[NUM_READERS], writer_ids[NUM_WRITERS];
    
    init_data();

    for (int i = 0; i < NUM_READERS; i++) {
        reader_ids[i] = i + 1;
        pthread_create(&readers[i], NULL, reader, &reader_ids[i]);
    }
    for (int i = 0; i < NUM_WRITERS; i++) {
        writer_ids[i] = i + 1;
        pthread_create(&writers[i], NULL, writer, &writer_ids[i]);
    }
    
    sleep(10); // will run for 30 seconds 
    
    sem_destroy(&data.mutex);
    sem_destroy(&data.write_sem);
    
    return 0;
}