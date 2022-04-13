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

class MomentoClientWrapper extends AbstractMemcacheClientWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MomentoClientWrapper.class);
    private static final int SIZE_OF_INT = 4;

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
    public boolean delete(final String key) throws CacheException {
        try {
            CacheTranscoder transcoder = getTranscoder();
            CachedObject cachedObject = transcoder.encode(""); // Default to empty string for now on delete
            boolean result = writeOutToMomento(
                    key,
                    1,  // Default to 1 second ttl for now on delete
                    cachedObject.getFlags(),
                    cachedObject.getData()
            );
            accessLogWrite("Delete", key, result, cachedObject.getData().length);
            return result;
        } catch (RuntimeException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void flush() throws CacheException {
        throw new CacheException(new RuntimeException("not implemented"));
    }

    @Override
    public Object get(final String key) throws CacheException {
        try {
            Object data = readFromMomento(key);
            accessLogRead("Get", key, data);
            return data;
        } catch (RuntimeException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public <T> T get(final String key, final CacheTranscoder transcoder) throws CacheException {
        try {
            T data = readFromMomento(key, transcoder);
            accessLogRead("Get", key, data);
            return data;
        } catch (RuntimeException e) {
            throw new CacheException(e);
        }
    }

    // Right now we are just using our synchronous client, so timeout is ignored. We will
    // bring this back in once we resolve some underlying Netty issues
    @Override
    public <T> T get(final String key, final CacheTranscoder transcoder, final long timeout) throws CacheException {
        try {
            T data = readFromMomento(key, transcoder);
            accessLogRead("Get", key, data);
            return data;
        } catch (RuntimeException e) {
            throw new CacheException(e);
        }
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
    public boolean set(final String key, final int exp, final Object value) throws CacheException {
        try {
            CacheTranscoder transcoder = getTranscoder();
            CachedObject cachedObject = transcoder.encode(value);
            boolean result = writeOutToMomento(key, exp, cachedObject.getFlags(), cachedObject.getData());
            accessLogWrite("Set", key, result, cachedObject.getData().length);
            return result;
        } catch (RuntimeException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public <T> boolean set(final String key, final int exp, final T value, final CacheTranscoder transcoder) throws CacheException {
        try {
            Transcoder<T> cacheTranscoder = getTranscoder(transcoder);
            CachedData cachedData = cacheTranscoder.encode(value);
            boolean result = writeOutToMomento(key, exp, cachedData.getFlags(), cachedData.getData());
            accessLogWrite("Set", key, result, cachedData.getData().length);
            return result;
        } catch (RuntimeException e) {
            throw new CacheException(e);
        }
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

    // Helper function that concatenates the CachedObject to raw bytes before calling `set`.
    // This helps maintain our serialization flags so that we can deserialize the object upon a `get`
    private boolean writeOutToMomento(final String key, final int exp, final int flagsUsed, final byte[] data) {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE_OF_INT + data.length);
        buffer.putInt(flagsUsed);
        buffer.put(data);
        buffer.rewind();
        CacheSetResponse response = momentoClient.set(defaultCacheName, key, buffer, exp);
        return response != null;
    }

    // Helper function that reads the raw bytes from Momento and splits up the bytes
    // to extract the serialization flags we used as well as the raw bytes of data that contain our
    // actual object
    private <T> T readFromMomento(final String key, final CacheTranscoder transcoder) {
        Transcoder<T> cacheTranscoder = getTranscoder(transcoder);
        Optional<CachedData> maybeCachedData = performGet(key);
        return maybeCachedData.map(cacheTranscoder::decode)
                .orElse(null);
    }

    // Helper function that reads the raw bytes from Momento and splits up the bytes
    // to extract the serialization flags we used as well as the raw bytes of data that contain our
    // actual object, uses our default Transcoder
    private Object readFromMomento(final String key) {
        CacheTranscoder cacheTranscoder = getTranscoder();
        Optional<CachedData> maybeCachedData = performGet(key);
        return maybeCachedData.map(cachedData -> cacheTranscoder.decode(new CachedObjectWrapper(cachedData)))
                .orElse(null);
    }

    private Optional<CachedData> performGet(final String key) {
        CacheGetResponse response = momentoClient.get(defaultCacheName, key);
        Optional<byte[]> cacheGetResponse = response.byteArray();
        if (cacheGetResponse.isPresent()) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(cacheGetResponse.get());
            int flags = byteBuffer.getInt();
            // Ensure we perform a deep copy of the remaining bytes into a separate byte array
            byte[] originalData = new byte[byteBuffer.remaining()];
            byteBuffer.get(originalData);
            return Optional.of(new CachedData(flags, originalData, originalData.length));
        }
        return Optional.empty();
    }

    private void accessLogRead(String method, String key, Object data) {
        if (data == null) {
            LOGGER.debug(method + ": MISS: no item found in cache for key: " + key);
        } else {
            LOGGER.debug(method + ": HIT: item found in cache for key: " + key);
        }
    }

    private void accessLogWrite(String method, String key, boolean writeSuccess, int size) {
        if (writeSuccess) {
            LOGGER.debug(method + ": SetSuccess: item stored in cache key: + " + key + " value_size: " + size);
        } else {
            LOGGER.debug(method + ": SetFailure: item not stored in cache empty response for key: " + key + " value_size: " + size);
        }
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
