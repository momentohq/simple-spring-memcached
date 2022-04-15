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

import java.net.InetSocketAddress;
import java.util.List;

import momento.sdk.SimpleCacheClient;
import momento.sdk.SimpleCacheClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheClientFactory;
import com.google.code.ssm.providers.CacheConfiguration;

public class MomentoCacheClientFactory implements CacheClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MomentoCacheClientFactory.class);

    @Override
    public CacheClient create(final List<InetSocketAddress> addrs, final CacheConfiguration conf) {
        MomentoConfiguration momentoConfiguration = null;
        if (conf instanceof MomentoConfiguration) {
            momentoConfiguration = (MomentoConfiguration) conf;
        }
        if (momentoConfiguration != null && momentoConfiguration.getMomentoAuthToken() != null) {

            SimpleCacheClientBuilder builder = SimpleCacheClient.builder(
                    momentoConfiguration.getMomentoAuthToken(),
                    momentoConfiguration.getDefaultTtl()
            );
            if (momentoConfiguration.getRequestTimeout().isPresent()) {
                builder.requestTimeout(momentoConfiguration.getRequestTimeout().get());
            }
            SimpleCacheClient client = builder.build();
            return new MomentoClientWrapper(
                    client,
                    momentoConfiguration.getCacheName(),
                    momentoConfiguration.getDefaultTtl()
            );
        }
        throw new RuntimeException("Momento auth token must be provided in CacheConfiguration");
    }
}
