package org.example.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public final class NettyServer {

    // Порт, на котором сервер будет прослушивать соединения.
    static final int PORT = 8001;

    public static void main(String[] args) throws Exception {

        // Конфигурируем сервер

        // Создаем группы boss и worker.
        // Boss принимает подключения от клиента.
        // Worker обрабатывает дальнейшую коммуникацию через соединения.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup) // Установка boss и worker групп
                    .channel(NioServerSocketChannel.class) // Использование NIO, принять новое соединение
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            // Коммуникация сокет-канал происходит в потоках байтов.
                            // Декодер строк и кодировщик помогают преобразованию
                            // между байтами и строкой.
                            p.addLast(new StringDecoder());
                            p.addLast(new StringEncoder());
                            // Обработчик на сервере.
                            p.addLast(new ServerHandler());
                        }
                    });

            // Стартуем сервер.
            ChannelFuture f = b.bind(PORT).sync();
            System.out.println("Server started and waiting for clients...");

            // Ждем, пока сокет сервера не будет закрыт.
            f.channel().closeFuture().sync();
        } finally {
            // Завершаем все циклы обработки событий,
            // чтобы завершить все потоки.
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
