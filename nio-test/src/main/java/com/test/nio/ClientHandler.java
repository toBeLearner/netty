/**
 * @author LENOVO
 * @date 2020/4/5 23:01
 */

package com.test.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ClientHandler implements Runnable{

    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;

    private volatile boolean started;

    public ClientHandler(String ip,int port){
        this.host = ip;
        this.port = port;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            started = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  void stop(){
        this.started = false;
    }

    public void run() {
        try {
            if(socketChannel.connect(new InetSocketAddress(host,port))){}
            else{
                socketChannel.register(selector,SelectionKey.OP_CONNECT);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        //循环遍历selector
        while (started){
            try {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()){
                    key = it.next();
                    it.remove();
                    if(key.isValid()){
                        SocketChannel sc = (SocketChannel) key.channel();
                        if(key.isConnectable()){
                            if(sc.finishConnect()){}
                            else {System.exit(1);}
                        }
                    }
                    //有数据
                    if(key.isReadable()){
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        //将channel数据写进缓冲区
                        //Reads a sequence of bytes from this channel into the given buff
                        int readBytes = socketChannel.read(buffer);
                        if(readBytes>0){
                            buffer.flip();
                            byte[] bytes = new byte[buffer.remaining()];
                            //This method transfers bytes from this buffer into the given destination array
                            buffer.get(bytes);
                            String result = new String(bytes,"UTF-8");
                            System.out.println("accept message "+result);
                        }else {
                            key.cancel();
                            socketChannel.close();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        //关闭释放
        if(selector !=null){

                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }

    }
    public void sendMsg(String msg) throws IOException {
        socketChannel.register(selector,SelectionKey.OP_READ);
        byte[] bytes = msg.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        socketChannel.write(buffer);

    }
}
