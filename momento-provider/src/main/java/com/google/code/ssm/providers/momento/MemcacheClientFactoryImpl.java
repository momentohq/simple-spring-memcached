/*
 * Copyright (c) 2014-2019 Jakub Białek
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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import momento.sdk.SimpleCacheClient;
//import momento.sdk.exceptions.AlreadyExistsException;
//import momento.sdk.messages.CacheGetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionFactoryBuilder.Locator;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.MemcachedClient;

import com.google.code.ssm.providers.CacheClient;
import com.google.code.ssm.providers.CacheClientFactory;
import com.google.code.ssm.providers.CacheConfiguration;

/**
 * 
 * @author Jakub Białek
 * @since 3.5.0
 * 
 */
public class MemcacheClientFactoryImpl implements CacheClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemcacheClientFactoryImpl.class);
    

    @Override
    public CacheClient create(final List<InetSocketAddress> addrs, final CacheConfiguration conf) throws IOException {

        SimpleCacheClient.builder("", 300).build();
        throw new RuntimeException("not implemented");
    }



}
