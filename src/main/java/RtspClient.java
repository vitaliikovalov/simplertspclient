/**
 * Created by Cctv on 27.03.2015.
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

import java.net.URI;

public final class RtspClient {

    static final String URL = "rtsp://admin:admin@91.236.250.26:554/cam/realmonitor?channel=1&subtype=0";
    public static void main(String[] args) throws Exception {
        URI uri = new URI(URL);
        String scheme = uri.getScheme() == null? "rtsp" : uri.getScheme();
        String host = uri.getHost() == null? "127.0.0.1" : uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            if ("rtsp".equalsIgnoreCase(scheme)) {
                port = 554;
            }
        }
        String userInfo = uri.getUserInfo();
        String userName = userInfo.split(":")[0];
        String userPassword = userInfo.split(":")[1];
        if (!"rtsp".equalsIgnoreCase(scheme)) {
            System.err.println("Only RTSP is supported.");
            return;
        }
        System.out.println(scheme);
        System.out.println(host);
        System.out.println(port);
        System.out.println(userName);
        System.out.println(userPassword);
        System.out.println(uri.getPath());
        System.out.println(uri.getQuery());

        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new RtspClientInitializer());

            // Make the connection attempt.
            Channel ch = b.connect(host, port).sync().channel();

            // Prepare the HTTP request.
            HttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
            request.headers().set(HttpHeaders.Names.HOST, host);
            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

            // Send the HTTP request.
            ch.writeAndFlush(request);

            // Wait for the server to close the connection.
            ch.closeFuture().sync();
        } finally {
            // Shut down executor threads to exit.
            group.shutdownGracefully();
        }
    }
}


