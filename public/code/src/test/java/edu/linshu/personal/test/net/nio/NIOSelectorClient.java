package edu.linshu.personal.test.net.nio;

import edu.linshu.personal.core.net.IClientSocket;
import edu.linshu.personal.core.net.nio.ISelector;
import edu.linshu.personal.core.net.nio.NIOSocket;
import lombok.extern.java.Log;
import org.junit.Test;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;


/**
 * @author Song Yu Tao 745698872@qq.com
 * @date 2019/06/26 22:06
 */
@SuppressWarnings("all")
@Log
public class NIOSelectorClient {
    @Test
    public void test() throws IOException, InterruptedException {
        sendRequest();
    }

    static void sendRequest() throws IOException, InterruptedException {
        sendRequest(null);
    }

    static void sendRequest(String msg) throws IOException, InterruptedException {
        Selector selector = Selector.open();

        IClientSocket client = new NIOSocket();
        ISelector clientSelector = (ISelector) client;

        if (!client.connect("0.0.0.0", 55555)) {
            clientSelector.register(selector, SelectionKey.OP_CONNECT);
        }

        while (!client.isClosed()) {
            selector.select();

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (key.isConnectable()) {
                    if (!client.finishConnection()) {
                        continue;
                    }

                    log.info("连接成功：" + client.getRemoteSocketAddress());

                    clientSelector.register(selector, SelectionKey.OP_WRITE);
                }

                if (key.isWritable()) {
                    if (Objects.isNull(msg)) {
                        log.info("等待接收用户输入信息：");
                        Scanner scanner = new Scanner(System.in);
                        msg = scanner.nextLine();
                        scanner.close();
                    }

                    log.info("准备发送用户消息：" + msg);
                    client.sendMsg(msg, StandardCharsets.UTF_8);
                    log.info("发送用户消息成功");

                    clientSelector.register(selector, SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    try {
                        log.info("等待接收服务器信息: ");
                        String readMsg = client.readMsg(StandardCharsets.UTF_8);

                        log.info("接收到服务器响应信息：\r\n\r\n" + readMsg + "\r\n客户端即将关闭");
                    } finally {
                        key.cancel();
                        client.close();
                    }
                }
            }

            selector.selectNow();
        }
    }

}