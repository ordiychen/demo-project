/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.github.ordiychen.test.netty.helloworld.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import lombok.extern.log4j.Log4j2;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * An HTTP2 client that allows you to send HTTP2 frames to a server using HTTP1-style approaches
 * (via {@link io.netty.handler.codec.http2.InboundHttp2ToHttpAdapter}). Inbound and outbound
 * frames are logged.
 * When run from the command-line, sends a single HEADERS frame to the server and gets back
 * a "Hello World" response.
 * See the ./http2/helloworld/frame/client/ example for a HTTP2 client example which does not use
 * HTTP1-style objects and patterns.
 */
@Log4j2
public final class Http2Client {

//    static final boolean SSL = System.getProperty("ssl") != null;
//    static final String HOST = System.getProperty("host", "127.0.0.1");
//    static final int PORT = Integer.parseInt(System.getProperty("port", SSL ? "8443" : "8080"));
//    static final String URL = System.getProperty("url", "/whatever");
//    static final String URL2 = System.getProperty("url2");
//    static final String URL2DATA = System.getProperty("url2data", "test data!");

    Http2ClientInitializer initializer;

    public void startHttp2Server(String host, int port, KeyManagerFactory keyManagerFactory, Map<String, String> bodyContent) throws SSLException {
        // Configure SSL.
        log.info("---> client start connection {}:{}", host, port);
        final SslContext sslCtx = SslContextBuilder.forClient()
                .keyManager(keyManagerFactory)
                .protocols("TLSv1.2","TLSv1.3")
                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        Protocol.ALPN,
                        // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                        SelectorFailureBehavior.NO_ADVERTISE,
                        // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                        SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2))
                .build();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        initializer = new Http2ClientInitializer(sslCtx, host, port, Integer.MAX_VALUE);

        try {
            // Configure the client.
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.remoteAddress(host, port);
            b.handler(initializer);

            // Start the client.
            Channel channel = b.connect().syncUninterruptibly().channel();

            // Wait for the HTTP/2 upgrade to occur.
            Http2SettingsHandler http2SettingsHandler = initializer.settingsHandler();
            http2SettingsHandler.awaitSettings(5, TimeUnit.SECONDS);

            HttpResponseHandler responseHandler = initializer.responseHandler();

            int streamId = 3;
            for (Map.Entry<String, String> bodyEntry : bodyContent.entrySet()) {
                String url = bodyEntry.getKey();
                String body = bodyEntry.getValue();

                HttpScheme scheme = HttpScheme.HTTPS;
                AsciiString hostName = new AsciiString(host + ':' + port);
                log.info("Sending request(s)...");
                // Create a simple GET request. HTTP_1_1
                FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, url, Unpooled.EMPTY_BUFFER);
                request.headers().add(HttpHeaderNames.HOST, hostName);
                request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), scheme.name());
                request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
                request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);

                responseHandler.put(streamId, channel.write(request), channel.newPromise());
                streamId += 2;
            }
            channel.flush();
            responseHandler.awaitResponses(5, TimeUnit.SECONDS);
            log.info("----> responseHandler:{}", responseHandler.toString());
            System.out.println("Finished HTTP/2 request(s)");

            // Wait until the connection is closed.
            channel.close().syncUninterruptibly();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

}
