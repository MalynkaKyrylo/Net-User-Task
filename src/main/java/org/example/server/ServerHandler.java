package org.example.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;
import java.util.List;

// @Sharable означает, что можно зарегистрироваться
// и поделиться обработчиком с несколькими клиентами.
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<String> {

    // Список подключенных клиентских каналов.
    static final List<Channel> channels = new ArrayList<>();

    // Всякий раз, когда клиент подключается к серверу через канал,
    // добавляем его канал в список каналов.
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        System.out.println("Client joined - " + ctx);
        channels.add(ctx.channel());
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String msg) {
        System.out.println("Message received: " + msg);
        for (Channel c : channels) {
            c.writeAndFlush("Hello " + msg + '\n');
        }
    }

    // В случае исключения закрываем канал.
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.out.println("Closing connection for client - " + ctx);
        ctx.close();
    }
}
