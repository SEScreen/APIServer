package net.sescreen.apisrv;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.io.File;

public class Main{

    public static final File uploadsDirectory=new File("./uploads");
    public static DatabaseQueryController mainDB;

    public static void main(String[] args) throws InterruptedException {
        mainDB=new DatabaseQueryController("192.168.1.100","apiserver","HMTUuj4rQjaDYVe8","main");
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("Init new channel");
                            ChannelPipeline pipeline = ch.pipeline();


                            pipeline.addLast(new HttpRequestDecoder());
                            pipeline.addLast(new HttpResponseEncoder());
                            //pipeline.addLast("httpd",new HttpUploadServerHandler());
                            //pipeline.addLast("http",new HttpUploadServerHandler());
                            pipeline.addLast("uploader",new UploadHandler());
                            pipeline.addLast("getter",new GetHandler());
                            pipeline.addLast("deleter",new DeleteHandler());
                            pipeline.addLast("blackhole",new BlackHoleHandler());

                            // Remove the following line if you don't want automatic content compression.
                            pipeline.addLast(new HttpContentCompressor());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(8080).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdown();
            bossGroup.shutdown();
        }
    }
}