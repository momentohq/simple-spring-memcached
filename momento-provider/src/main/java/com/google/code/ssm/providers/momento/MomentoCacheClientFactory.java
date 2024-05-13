/*
 * Copyright (c) 2022 Momento, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.google.code.ssm.providers.momento;

import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheClientFactory;
import com.google.code.ssm.providers.CacheConfiguration;
import momento.sdk.auth.CredentialProvider;
import momento.sdk.config.Configuration;
import momento.sdk.config.Configurations;
import momento.sdk.config.transport.GrpcConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;

public class MomentoCacheClientFactory implements CacheClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MomentoCacheClientFactory.class);

    @Override
    public CacheClient create(final List<InetSocketAddress> addrs, final CacheConfiguration conf) {
        MomentoConfiguration momentoConfiguration = null;
        if (conf instanceof MomentoConfiguration) {
            momentoConfiguration = (MomentoConfiguration) conf;
        }
        if (momentoConfiguration != null && momentoConfiguration.getMomentoAuthToken() != null) {

            Configuration config = Configurations.InRegion.latest();
            GrpcConfiguration grpcConfiguration = config.getTransportStrategy().getGrpcConfiguration();
            config = config.withTransportStrategy(config.getTransportStrategy()
                    // Hard code this to 8 TCP connections for now.
                    // TODO make client config as config item user can pass in
                    .withGrpcConfiguration(grpcConfiguration.withMinNumGrpcChannels(8)));
            if (momentoConfiguration.getRequestTimeout().isPresent()) {
                config = config.withTimeout(momentoConfiguration.getRequestTimeout().get());
            }

            final momento.sdk.CacheClient client = momento.sdk.CacheClient.create(
                    CredentialProvider.fromString(momentoConfiguration.getMomentoAuthToken()),
                    config,
                    Duration.ofSeconds(momentoConfiguration.getDefaultTtl())
            );
            return new MomentoClientWrapper(
                    client,
                    momentoConfiguration.getCacheName(),
                    momentoConfiguration.getDefaultTtl()
            );
        }
        throw new RuntimeException("Momento auth token must be provided in CacheConfiguration");
    }
}
