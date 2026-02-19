package ru.molchanov.demo;

public class Demo4 {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("главный поток работатет");
        simulateMainWorker();

        Thread thread = new Thread(()->{
            System.out.println("name" + Thread.currentThread().getName());
            levl1();
        });

        thread.start();
        thread.join();
    }

    private static void simulateMainWorker(){
        System.out.println("simulateworker");
        Thread.dumpStack();
    }

    private static void levl1(){
        System.out.println("lvll1");

    }
}
