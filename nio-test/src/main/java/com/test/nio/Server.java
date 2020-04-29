/**
 * @author LENOVO
 * @date 2020/4/6 11:10
 */

package com.test.nio;

public class Server {
    private static ServerHandler nioServerHandle;

    public static void start(){
        if(nioServerHandle !=null)
            nioServerHandle.stop();
        nioServerHandle = new ServerHandler(7070);
        new Thread(nioServerHandle,"Server").start();
    }
    public static void main(String[] args){
        start();
    }
}
