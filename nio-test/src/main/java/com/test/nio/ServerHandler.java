/**
 * @author LENOVO
 * @date 2020/4/6 10:17
 */

package com.test.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ServerHandler implements Runnable{

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private volatile boolean started;

    public ServerHandler(int port){

        try {
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);
            started = true;
            System.out.println("服务器启动，端口号"+port);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stop(){
        started = false;
    }
    public void run() {
        while (started){
            try {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it =  selectionKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()){
                    key = it.next();
                    it.remove();
                    if(key.isValid()){
                        if(key.isAcceptable()){
                            ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
                            //accept 创建socketChannel实例，意味完成tcp三次握手
                            SocketChannel sc = ssc.accept();
                            System.out.println("建立连接"+key.toString());
                            sc.configureBlocking(false);
                            //连接建立，可以关心读事件
                            sc.register(selector,SelectionKey.OP_READ);
                        }
                        //读消息
                        if(key.isReadable()){
                            System.out.println("可以读取"+key.toString());
                            SocketChannel sc = (SocketChannel) key.channel();
                            ByteBuffer buffer = ByteBuffer.allocate(10);
                            int readBytes = sc.read(buffer);
                            if(readBytes>0){
                                buffer.flip();
                                //创建数组
                                byte[] bytes = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                String msg = new String(bytes,"UTF-8");
                                System.out.println("消息"+msg);
                                String result = "Hello,"+msg+",Now is "+new java.util.Date(
                                        System.currentTimeMillis()).toString() ;
                                //应答消息
                                byte[] sendBytes = result.getBytes();
                                ByteBuffer writeBuffer = ByteBuffer.allocate(sendBytes.length);
                                //写入缓冲区
                                writeBuffer.put(sendBytes);
                                writeBuffer.flip();
                                sc.write(writeBuffer);

                            }else if(readBytes<0) {
                                key.cancel();
                                sc.close();
                            }
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //selector关闭后释放管理资源
        if(selector != null){
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
