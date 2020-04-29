/**
 * @author LENOVO
 * @date 2020/4/5 23:56
 */

package com.test.nio;

import java.io.IOException;
import java.util.Scanner;

public class Client {
    private static ClientHandler clientHandler;

    public static void start(){
        if(clientHandler!=null){
            clientHandler.stop();
        }
        clientHandler = new ClientHandler("127.0.0.1",7070);
        new Thread(clientHandler,"client").start();

    }

    public static boolean sendMsg(String msg) throws IOException {
        clientHandler.sendMsg(msg);
        return true;
    }

    public static void main(String[] args) throws IOException {
        start();
        Scanner scanner = new Scanner(System.in);
        while (Client.sendMsg(scanner.next()));
    }
}
