package ru.molchanov.demo.dz2;

public class dz2 {
    // Общий монитор (lock), чтобы сделать BLOCKED
    private static final Object LOCK = new Object();

    // Объект для wait/notify, чтобы сделать WAITING
    private static final Object WAIT_MONITOR = new Object();

    public static void main(String[] args) throws InterruptedException {

        // 1) Поток, который уходит в TIMED_WAITING через sleep()
        Thread timedWaitingThread = new Thread(() -> {
            log("TIMED thread: start (expect RUNNABLE -> TIMED_WAITING)");
            try {
                Thread.sleep(2000); // TIMED_WAITING
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            log("TIMED thread: end (expect TERMINATED soon)");
        }, "T_TIMED");

        // 2) Поток, который уходит в WAITING через wait()
        Thread waitingThread = new Thread(() -> {
            log("WAIT thread: start (expect RUNNABLE -> WAITING)");
            synchronized (WAIT_MONITOR) {
                try {
                    WAIT_MONITOR.wait(); // WAITING
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log("WAIT thread: resumed after notify (expect TERMINATED soon)");
        }, "T_WAIT");

        // 3) Поток, который захватывает LOCK и держит его, создавая BLOCKED для другого потока
        Thread lockHolderThread = new Thread(() -> {
            log("HOLDER thread: trying to enter LOCK (expect RUNNABLE)");
            synchronized (LOCK) {
                log("HOLDER thread: entered LOCK, holding it 3s");
                try {
                    Thread.sleep(3000); // TIMED_WAITING, но при этом держит LOCK
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log("HOLDER thread: leaving LOCK");
            }
            log("HOLDER thread: end");
        }, "T_HOLDER");

        // 4) Поток, который попытается войти в LOCK и будет BLOCKED пока T_HOLDER держит монитор
        Thread blockedThread = new Thread(() -> {
            log("BLOCKED thread: trying to enter LOCK (expect BLOCKED until free)");
            synchronized (LOCK) {
                log("BLOCKED thread: entered LOCK (after being BLOCKED)");
            }
            log("BLOCKED thread: end");
        }, "T_BLOCKED");

        // ---- ВАЖНО: фиксируем NEW ----
        printStates("BEFORE start (all should be NEW)",
                timedWaitingThread, waitingThread, lockHolderThread, blockedThread);

        // Стартуем часть потоков
        timedWaitingThread.start();
        waitingThread.start();
        lockHolderThread.start();

        // Чуть подождём, чтобы holder точно зашёл в LOCK
        Thread.sleep(200);

        // Теперь запускаем поток, который должен стать BLOCKED
        blockedThread.start();

        // Отдельный мониторинг: наблюдаем состояния в цикле
        Thread monitor = new Thread(() -> {
            while (true) {
                printStates("MONITOR",
                        timedWaitingThread, waitingThread, lockHolderThread, blockedThread);

                // когда все завершатся — выходим
                if (allTerminated(timedWaitingThread, waitingThread, lockHolderThread, blockedThread)) {
                    break;
                }

                try {
                    Thread.sleep(200); // чтобы не спамить слишком быстро
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            log("MONITOR: finished");
        }, "T_MONITOR");

        monitor.start();

        // Подождём, чтобы WAIT поток точно ушёл в WAITING
        Thread.sleep(800);

        // Разбудим WAITING поток
        synchronized (WAIT_MONITOR) {
            log("MAIN: notify WAIT thread");
            WAIT_MONITOR.notify();
        }

        // Дожидаемся завершения потоков
        timedWaitingThread.join();
        waitingThread.join();
        lockHolderThread.join();
        blockedThread.join();

        // Финальные состояния
        printStates("AFTER join (all should be TERMINATED)",
                timedWaitingThread, waitingThread, lockHolderThread, blockedThread);

        monitor.join();
        log("MAIN: done");
    }

    private static boolean allTerminated(Thread... threads) {
        for (Thread t : threads) {
            if (t.getState() != Thread.State.TERMINATED) return false;
        }
        return true;
    }

    private static void printStates(String tag, Thread... threads) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(tag).append("] ");
        for (Thread t : threads) {
            sb.append(t.getName())
                    .append("=")
                    .append(t.getState())
                    .append("  ");
        }
        System.out.println(sb);
    }

    private static void log(String msg) {
        System.out.println("[" + Thread.currentThread().getName() + "] " + msg);
    }
}

