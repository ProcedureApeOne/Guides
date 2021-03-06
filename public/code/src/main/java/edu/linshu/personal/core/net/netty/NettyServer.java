package edu.linshu.personal.core.net.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.List;
import java.util.function.Supplier;

/**
 * @author Song Yu Tao 745698872@qq.com
 * @date 2019/07/02 11:09
 */
public class NettyServer {

    private final Integer port;

    public NettyServer(Integer port) {
        this.port = port;
    }

    public void run(Supplier<List<ChannelHandler>> handlersSupplier) throws InterruptedException {
        EventLoopGroup boseGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boseGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();

                            handlersSupplier.get().forEach(pipeline::addLast);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
            ;

            ChannelFuture channelFuture = bootstrap.bind(port).sync();

            channelFuture.channel().closeFuture().sync();
        } finally {
            boseGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
