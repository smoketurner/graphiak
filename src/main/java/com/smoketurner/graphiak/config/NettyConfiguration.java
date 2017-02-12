/**
 * Copyright 2017 Smoke Turner, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.smoketurner.graphiak.config;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.smoketurner.graphiak.handler.GraphiakInitializer;
import com.smoketurner.graphiak.managed.ChannelFutureManager;
import com.smoketurner.graphiak.managed.EventLoopGroupManager;
import com.smoketurner.graphiak.store.MetricStore;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Size;
import io.dropwizard.util.SizeUnit;
import io.dropwizard.validation.MaxSize;
import io.dropwizard.validation.MinSize;
import io.dropwizard.validation.PortRange;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyConfiguration {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NettyConfiguration.class);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.BYTES)
    @MaxSize(value = 250, unit = SizeUnit.KILOBYTES)
    private Size maxLength = Size.kilobytes(64);

    @PortRange
    private int listenPort = 2003;

    @JsonProperty
    public Size getMaxLength() {
        return maxLength;
    }

    @JsonProperty
    public void setMaxLength(Size maxLength) {
        this.maxLength = maxLength;
    }

    @JsonProperty
    public int getListenPort() {
        return listenPort;
    }

    @JsonProperty
    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    @JsonIgnore
    public ChannelFuture build(@Nonnull final Environment environment,
            @Nonnull final MetricStore store) {

        final GraphiakInitializer initializer = new GraphiakInitializer(store,
                maxLength.toBytes());

        final EventLoopGroup bossGroup;
        final EventLoopGroup workerGroup;
        if (Epoll.isAvailable()) {
            LOGGER.info("Event Loop: epoll");
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup();
        } else {
            LOGGER.info("Event Loop: NIO");
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
        }

        environment.lifecycle().manage(new EventLoopGroupManager(bossGroup));
        environment.lifecycle().manage(new EventLoopGroupManager(workerGroup));

        final ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(initializer);

        if (Epoll.isAvailable()) {
            bootstrap.channel(EpollServerSocketChannel.class);
        } else {
            bootstrap.channel(NioServerSocketChannel.class);
        }

        // Start the server
        final ChannelFuture future = bootstrap.bind(listenPort);
        environment.lifecycle().manage(new ChannelFutureManager(future));
        return future;
    }
}
