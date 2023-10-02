package org.example.user;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public final class NettyUser {

    static final String HOST = "127.0.0.1";
    static final int PORT = 8001;
    static String userName;
    static String input;
    static Channel channel;

    public static void main(String[] args) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group) // Установка EventLoopGroup, чтобы обрабатывать все события для клиента.
                    .channel(NioSocketChannel.class) // Использование NIO, принять новое соединение
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            // Коммуникация сокет-канал происходит в потоках байтов.
                            // Декодер строк и кодировщик помогают преобразованию
                            // между байтами и строкой.
                            p.addLast(new StringDecoder());
                            p.addLast(new StringEncoder());

                            // Клиентский обработчик.
                            p.addLast(new UserHandler());

                        }
                    });

            // Старт клиента.
            ChannelFuture f = b.connect(HOST, PORT).sync();
            System.out.println("Connected to server.");
            System.out.print("Please enter your name: ");
            Scanner scanner = new Scanner(System.in);
            if (scanner.hasNext()) {
                userName = scanner.nextLine();
                System.out.println("Welcome " + userName + ".\n" +
                        "Type your message to chat or type \"quit\" to exit.\n");
            }

            while (scanner.hasNext()) {
                input = scanner.nextLine();
                if (input.equals("quit")) System.exit(0);
                channel = f.sync().channel();
                channel.writeAndFlush("[" + userName + "]: " + input);
                channel.flush();
            }

            // Ожидание пока соединение не будет закрыто.
            f.channel().closeFuture().sync();
        } finally {
            // Завершение всех циклов обработки событий,
            // чтобы завершить все потоки.
            group.shutdownGracefully();
        }
    }
}
