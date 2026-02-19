package ru.molchanov.demo;

public class MyTask  implements Runnable{
    @Override
    public void run() {
        for (int i =0 ; i<5 ; i++){
        System.out.println("поток" + Thread.currentThread()+ "счетчик" +i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.out.println("поток был прерван");
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("Поток завершен");
    }
}
