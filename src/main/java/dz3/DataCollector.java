package dz3;

import java.util.*;

public class DataCollector {
    private final Queue<Item> queue = new ArrayDeque<>();
    private final Set<String> processedKeys = new HashSet<>();

    private int processedCount = 0;
    private boolean closed = false; // когда true, новых данных больше не будет

    // добавляет элемент в очередь, будит ожидающих
    public synchronized void collectItem(Item item) {
        if (closed) throw new IllegalStateException("Collector is closed");
        queue.add(item);
        notifyAll(); // разбудим всех, кто ждет данные
    }

    // взять элемент для обработки (если данных нет - ждать)
    public synchronized Item takeItem() throws InterruptedException {
        while (queue.isEmpty() && !closed) {
            wait(); // поток уснет, пока не появятся данные или пока collector не закроют
        }
        return queue.poll(); // может быть null, если closed и очередь пуста
    }

    public synchronized void incrementProcessed() {
        processedCount++;
    }

    public synchronized int getProcessedCount() {
        return processedCount;
    }

    public synchronized boolean isAlreadyProcessed(String key) {
        return processedKeys.contains(key);
    }

    // отметка обработки ключа (атомарно с проверкой)
    public synchronized boolean markProcessedIfNew(String key) {
        if (processedKeys.contains(key)) return false;
        processedKeys.add(key);
        return true;
    }

    // сообщаем рабочим потокам: данных больше не будет
    public synchronized void close() {
        closed = true;
        notifyAll();
    }
}