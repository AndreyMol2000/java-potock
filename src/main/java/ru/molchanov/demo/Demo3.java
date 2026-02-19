package ru.molchanov.demo;

public class Demo3 {
    public static void main(String[] args) {
        Thread thread1 = new Thread(() -> {
            System.out.println("имя =" + Thread.currentThread().getName());

        }, "worker01");


        Thread thread2 = new Thread(() -> {
            System.out.println("имя =" + Thread.currentThread().getName());

        }, "worker02");

        thread1.start();
        thread2.start();
    }
}
