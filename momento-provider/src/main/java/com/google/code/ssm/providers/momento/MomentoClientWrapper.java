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

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import com.google.code.ssm.providers.momento.transcoders.SerializingTranscoder;
import com.google.code.ssm.providers.momento.transcoders.Transcoder;
import momento.sdk.SimpleCacheClient;
import momento.sdk.messages.CacheGetResponse;
import momento.sdk.messages.CacheSetResponse;
import net.spy.memcached.CachedData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.code.ssm.providers.AbstractMemcacheClientWrapper;
import com.google.code.ssm.providers.CacheException;
import com.google.code.ssm.providers.CacheTranscoder;
import com.google.code.ssm.providers.CachedObject;
import com.google.code.ssm.providers.CachedObjectImpl;

/**
 * @author Jakub Białek
 * @since 3.5.0
 */
class MomentoClientWrapper extends AbstractMemcacheClientWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MomentoClientWrapper.class);

    private final Map<CacheTranscoder, Transcoder<Object>> adapters = new HashMap<>();

    private final String defaultCacheName;
    private final SimpleCacheClient momentoClient;

    MomentoClientWrapper(final SimpleCacheClient momentoClient, final String defaultCacheName) {
        this.momentoClient = momentoClient;
        this.defaultCacheName = defaultCacheName;
    }

    @Override
    public boolean add(final String key, final int exp, final Object value) throws TimeoutException, CacheException {
        throw new CacheException(new RuntimeException("not implemented"));
    }

    @Override
    public <T> boolean add(final String key, final int exp, final T value, final CacheTranscoder transcoder) throws TimeoutException,
            CacheException {
        throw new CacheException(new RuntimeException("not implemented"));
    }

    @Override
    public long decr(final String key, final int by) throws TimeoutException, CacheException {
        throw new CacheException(new RuntimeException("not implemented"));
    }

    @Override
    public long decr(final String key, final int by, final long def) throws CacheException {
        throw new CacheException(new RuntimeException("not implemented"));
    }

    @Override
    public boolean delete(final String key) throws TimeoutException, CacheException {
        throw new CacheException(new RuntimeException("not implemented"));
    }

    @Override
    public void flush() throws CacheException {
        throw new CacheException(new RuntimeException("not implemented"));
    }

    @Override
    public Object get(final String key) {
        CacheTranscoder cacheTranscoder = getTranscoder();
        CacheGetResponse response = momentoClient.get(defaultCacheName, key);
        if (response.byteArray().isPresent()) {
            byte[] returnedBytes = response.byteArray().get();
            return cacheTranscoder.decode(new CachedObjectWrapper(
                    new CachedData(0, returnedBytes, returnedBytes.length)
            ));
        }
        return null;
    }

    @Override
    public <T> T get(final String key, final CacheTranscoder transcoder) {
        Transcoder<T> cacheTranscoder = getTranscoder(transcoder);
        CacheGetResponse response = momentoClient.get(defaultCacheName, key);
        Optional<byte[]> cacheGetResponse = response.byteArray();
        if (cacheGetResponse.isPresent()) {
            byte[] returnedBytes = cacheGetResponse.get();
            return cacheTranscoder.decode(new CachedData(0, returnedBytes, returnedBytes.length));
        }
        return null;
    }

    @Override
    public <T> T get(final String key, final CacheTranscoder transcoder, final long timeout) throws TimeoutException, CacheException {
        Transcoder<T> cacheTranscoder = getTranscoder(transcoder);
        CacheGetResponse response = momentoClient.get(defaultCacheName, key);
        Optional<byte[]> cacheGetResponse = response.byteArray();
        if (cacheGetResponse.isPresent()) {
            byte[] returnedBytes = cacheGetResponse.get();
            return cacheTranscoder.decode(new CachedData(returnedBytes.length, returnedBytes, returnedBytes.length));
        }
        return null;
    }

    @Override
    public Collection<SocketAddress> getAvailableServers() {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> getBulk(final Collection<String> keys) throws CacheException {
        throw new CacheException(new RuntimeException("not implemented"));
    }

    @Override
    public <T> Map<String, T> getBulk(final Collection<String> keys, final CacheTranscoder transcoder) throws CacheException {
        throw new CacheException(new RuntimeException("not implemented"));
    }

    @Override
    public long incr(final String key, final int by) throws CacheException {
        throw new CacheException(new RuntimeException("not implemented"));
    }

    @Override
    public long incr(final String key, final int by, final long def) throws CacheException {
        throw new CacheException(new RuntimeException("not implemented"));
    }

    @Override
    public long incr(final String key, final int by, final long def, final int expiration) throws CacheException {
        throw new CacheException(new RuntimeException("not implemented"));
    }

    @Override
    public boolean set(final String key, final int exp, final Object value) {
        CacheTranscoder transcoder = getTranscoder();
        ByteBuffer buffer = ByteBuffer.wrap(transcoder.encode(value).getData());
            CacheSetResponse response = momentoClient.set(defaultCacheName, key, buffer, exp);
            return response != null;
    }

    @Override
    public <T> boolean set(final String key, final int exp, final T value, final CacheTranscoder transcoder) {
            Transcoder<T> cacheTranscoder = getTranscoder(transcoder);
            ByteBuffer buffer = ByteBuffer.wrap(cacheTranscoder.encode(value).getData());
            CacheSetResponse response = momentoClient.set(defaultCacheName, key, buffer, exp);
            return response != null;
    }

    @Override
    public void shutdown() {
        momentoClient.close();
    }

    @Override
    public CacheTranscoder getTranscoder() {
        return new TranscoderWrapper(new SerializingTranscoder());
    }

    @Override
    public Object getNativeClient() {
        return momentoClient;
    }

    @SuppressWarnings("unchecked")
    private <T> Transcoder<T> getTranscoder(final CacheTranscoder transcoder) {
        Transcoder<T> transcoderAdapter = (Transcoder<T>) adapters.get(transcoder);
        if (transcoderAdapter == null) {
            transcoderAdapter = (Transcoder<T>) new TranscoderAdapter(transcoder);
            adapters.put(transcoder, (Transcoder<Object>) transcoderAdapter);
        }

        return transcoderAdapter;
    }

    private void cancel(final Future<?> f) {
        if (f != null) {
            f.cancel(true);
        }
    }

    private boolean translateException(final RuntimeException e) {
        return e.getCause() instanceof InterruptedException || e.getCause() instanceof ExecutionException;
    }

    private static class TranscoderWrapper implements CacheTranscoder {

        private final Transcoder<Object> transcoder;

        public TranscoderWrapper(final Transcoder<Object> transcoder) {
            this.transcoder = transcoder;
        }

        @Override
        public Object decode(final CachedObject data) {
            return transcoder.decode(new CachedData(data.getFlags(), data.getData(), CachedObject.MAX_SIZE));
        }

        @Override
        public CachedObject encode(final Object o) {
            CachedData cachedData = transcoder.encode(o);
            return new CachedObjectImpl(cachedData.getFlags(), cachedData.getData());
        }
    }

}
