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
        sem_wait(&data.mutex);
        if (data.waiting_writers > 0 || data.writers > 0) {
            sem_post(&data.mutex);
            continue; // retry later
        }
        data.readers++;
        if (data.readers == 1)
            sem_wait(&data.write_sem); // first reader locks writers
        sem_post(&data.mutex);

        printf("Reader %d reads: %d\n", id, data.value);
        sleep(1);

        sem_wait(&data.mutex);
        data.readers--;
        if (data.readers == 0)
            sem_post(&data.write_sem); // last reader releases writers
        sem_post(&data.mutex);
        sleep(1);
    }
    return NULL;
}

void* writer(void* arg) {
    int id = *(int*)arg;
    while (1) {
        sem_wait(&data.mutex);
        data.waiting_writers++;
        sem_post(&data.mutex);

        sem_wait(&data.write_sem); // exclusive access

        sem_wait(&data.mutex);
        data.waiting_writers--;
        data.writers++;
        sem_post(&data.mutex);

        data.value++;
        printf("Writer %d writes: %d\n", id, data.value);
        sleep(2);

        sem_wait(&data.mutex);
        data.writers--;
        sem_post(&data.mutex);

        sem_post(&data.write_sem);
        sleep(4);
    }
    return NULL;
}

int main() {
    pthread_t readers[NUM_READERS], writers[NUM_WRITERS];
    int reader_ids[NUM_READERS], writer_ids[NUM_WRITERS];
    
    init_data();


    for (int i = 0; i < NUM_WRITERS; i++) {
        writer_ids[i] = i + 1;
        pthread_create(&writers[i], NULL, writer, &writer_ids[i]);
    }
    for (int i = 0; i < NUM_READERS; i++) {
        reader_ids[i] = i + 1;
        pthread_create(&readers[i], NULL, reader, &reader_ids[i]);
    }
    
    sleep(14);
    
    sem_destroy(&data.mutex);
    sem_destroy(&data.write_sem);
    sem_destroy(&data.read_sem);
    
    return 0;
}
