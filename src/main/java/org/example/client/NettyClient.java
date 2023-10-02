package org.example.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public final class NettyClient {

    static final String HOST = "127.0.0.1";
    static final int PORT = 8001;

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
                            p.addLast(new ClientHandler());

                        }
                    });

            // Старт клиента.
            ChannelFuture f = b.connect(HOST, PORT).sync();

            String input = "Tom";
            Channel channel = f.sync().channel();
            channel.writeAndFlush(input);
            channel.flush();

            // Ожидание пока соединение не будет закрыто.
            f.channel().closeFuture().sync();
        } finally {
            // Завершение всех циклов обработки событий,
            // чтобы завершить все потоки.
            group.shutdownGracefully();
        }
    }
}
