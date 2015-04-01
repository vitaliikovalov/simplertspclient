/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package rtsp;

import example.HttpSnoopServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.rtsp.RtspHeaders;
import io.netty.handler.codec.rtsp.RtspMethods;
import io.netty.handler.codec.rtsp.RtspVersions;
import org.apache.commons.codec.binary.Base64;
import rtsp.RtspClientInitializer;

import java.net.URI;

/**
 * A simple HTTP client that prints out the content of the HTTP response to
 * {@link System#out} to test {@link HttpSnoopServer}.
 */
public final class RtspClient {

    private static String username = "admin";
    private static String password = "admin";


    public static void main(String[] args) throws Exception {
        URI uri = new URI("rtsp://91.236.250.26:554/");
        String host = uri.getHost() == null? "192.168.1.139" : uri.getHost();
        int port = uri.getPort();

        String authString = username + ":" + password;
        System.out.println("auth string: " + authString);
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        String authStringEnc = new String(authEncBytes);
        System.out.println("Base64 encoded auth string: " + authStringEnc);

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
//            HttpRequest request = new DefaultFullHttpRequest(
//                    HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath());
//            request.headers().set(HttpHeaders.Names.HOST, host);
//            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
//            request.headers().set(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);

            HttpRequest request = new DefaultFullHttpRequest(
                    RtspVersions.RTSP_1_0, RtspMethods.DESCRIBE, "rtsp://91.236.250.26:554/cam/realmonitor?channel=1&subtype=1");
//            request.headers().set(RtspHeaders.Names.CONNECTION, RtspHeaders.Values.KEEP_ALIVE);
//            request.headers().set(RtspHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());
            request.headers().set(RtspHeaders.Names.CSEQ, 1);
            request.headers().set(RtspHeaders.Names.REQUIRE, "implicit-play");
            request.headers().set(RtspHeaders.Names.AUTHORIZATION, "Basic " + authStringEnc);
//            request.headers().set(RtspHeaders.Names.AUTHORIZATION, "Basic YWRtaW46YWRtaW4");

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
