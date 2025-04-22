#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <semaphore.h>
#include <unistd.h>

#define NUM_READERS 2
#define NUM_WRITERS 2

typedef struct {
    int value;
    sem_t mutex;         // Protects access to readers and writers
    sem_t write_sem;     // For writer exclusivity
    sem_t read_sem;      // To block readers when writers are waiting
    int readers; 
    int writers; 
    int waiting_writers;
} SharedData;

SharedData data;

void init_data() {
    data.value = 0;
    sem_init(&data.mutex, 0, 1);
    sem_init(&data.write_sem, 0, 1);
    sem_init(&data.read_sem, 0, 1);
    data.readers = 0;
    data.writers = 0;
    data.waiting_writers = 0;
}

void* reader(void* arg) {
    int id = *(int*)arg;
    while (1) {
        // Block if there are writers (active or waiting)
        sem_wait(&data.read_sem);
        
        sem_wait(&data.mutex);
        data.readers++;
        if (data.readers == 1) {
            sem_wait(&data.write_sem); // First reader locks writers out
        }
        sem_post(&data.mutex);
        
        sem_post(&data.read_sem); // Allow other readers to proceed

        printf("Reader %d reads: %d\n", id, data.value);
        sleep(1);

        sem_wait(&data.mutex);
        data.readers--;
        if (data.readers == 0) {
            sem_post(&data.write_sem); // Last reader releases write lock
        }
        sem_post(&data.mutex);
    }
    return NULL;
}

void* writer(void* arg) {
    int id = *(int*)arg;
    while (1) {
        sem_wait(&data.mutex);
        data.waiting_writers++;
        if (data.waiting_writers == 1) {
            sem_wait(&data.read_sem); // First waiting writer blocks new readers
        }
        sem_post(&data.mutex);

        sem_wait(&data.write_sem); // Get exclusive access
        data.writers++;
        data.waiting_writers--;
        
        data.value++;
        printf("Writer %d writes: %d\n", id, data.value);
        sleep(2);

        sem_wait(&data.mutex);
        data.writers--;
        if (data.waiting_writers == 0) {
            sem_post(&data.read_sem); // Unblock readers if no more writers
        }
        sem_post(&data.mutex);

        sem_post(&data.write_sem);
        sleep(2);
    }
    return NULL;
}

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
    
    sleep(10);
    
    sem_destroy(&data.mutex);
    sem_destroy(&data.write_sem);
    sem_destroy(&data.read_sem);
    
    return 0;
}