/*
 * Copyright 2014 The Netty Project
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

package com.github.ordiychen.test.netty.helloworld.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.extern.log4j.Log4j2;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;

/**
 * An HTTP/2 Server that responds to requests with a Hello World. Once started, you can test the
 * server with the example client.
 */
@Log4j2
public final class Http2Server {


    public void startServer(String host, int port, KeyManagerFactory keyManagerFactory) throws SSLException {
        log.info("-=====> server start {}:{}" , host,port);
        // Configure SSL.
        final SslContext sslCtx = SslContextBuilder.forServer(keyManagerFactory)
                /* NOTE: the cipher filter may not include all ciphers required by the HTTP/2 specification.
                 * Please refer to the HTTP/2 specification for cipher requirements. */
                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .protocols("TLSv1.2","TLSv1.3")
                .applicationProtocolConfig(new ApplicationProtocolConfig(
                        Protocol.ALPN,
                        // NO_ADVERTISE is currently the only mode supported by both OpenSsl and JDK providers.
                        SelectorFailureBehavior.NO_ADVERTISE,
                        // ACCEPT is currently the only mode supported by both OpenSsl and JDK providers.
                        SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2))
                .build();
        // Configure the server.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new Http2ServerInitializer(sslCtx));
            Channel ch = b.bind(host,port).sync().channel();
            System.err.println("Open your HTTP/2-enabled web browser and navigate to https"  + "://127.0.0.1:" + port + '/');

            ch.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
