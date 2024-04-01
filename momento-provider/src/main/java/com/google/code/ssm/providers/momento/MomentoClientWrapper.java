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

import com.google.code.ssm.providers.*;
import com.google.code.ssm.providers.momento.transcoders.SerializingTranscoder;
import com.google.code.ssm.providers.momento.transcoders.Transcoder;
import momento.sdk.responses.cache.GetResponse;
import momento.sdk.responses.cache.SetResponse;
import net.spy.memcached.CachedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

class MomentoClientWrapper extends AbstractMemcacheClientWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(MomentoClientWrapper.class);
    private static final int SIZE_OF_INT = 4;

    private final Map<CacheTranscoder, Transcoder<Object>> adapters = new HashMap<>();

    private final String defaultCacheName;
    private final int defaultTTL;
    private final momento.sdk.CacheClient momentoClient;

    MomentoClientWrapper(final momento.sdk.CacheClient momentoClient, final String defaultCacheName, int defaultTTL) {
        this.momentoClient = momentoClient;
        this.defaultCacheName = defaultCacheName;
        this.defaultTTL = defaultTTL;
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
            momentoClient.delete(defaultCacheName, key);
            LOGGER.debug(MessageFormat.format("Delete: Success Item removed from cache key: {1}", key));
            return true;
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
        try {
            return readFromMomento(keys);
        } catch (RuntimeException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public <T> Map<String, T> getBulk(final Collection<String> keys, final CacheTranscoder transcoder) throws CacheException {
        try {
            return readFromMomento(keys, transcoder);
        } catch (RuntimeException e) {
            throw new CacheException(e);
        }
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
            CachedObject cachedObject = getTranscoder().encode(value);

            // simple spring memcache defaults exp(ttl) to 0 which momento treats as invalidate immediately,
            // so we set default ttl here if we are passed 0
            int ttl = exp == 0 ? defaultTTL : exp;

            boolean result = writeOutToMomento(key, ttl, cachedObject.getFlags(), cachedObject.getData());
            accessLogWrite("Set", key, result, cachedObject.getData().length, ttl, cachedObject.getFlags());
            return result;
        } catch (RuntimeException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public <T> boolean set(final String key, final int exp, final T value, final CacheTranscoder transcoder) throws CacheException {
        try {
            CachedData cachedData = getTranscoder(transcoder).encode(value);

            // simple spring memcache defaults exp(ttl) to 0 which momento treats as invalidate immediately,
            // so we set default ttl here if we are passed 0
            int ttl = exp == 0 ? defaultTTL : exp;

            boolean result = writeOutToMomento(key, ttl, cachedData.getFlags(), cachedData.getData());
            accessLogWrite("Set", key, result, cachedData.getData().length, ttl, cachedData.getFlags());
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
        SetResponse response = momentoClient.set(
                defaultCacheName, key.getBytes(StandardCharsets.UTF_8), buffer.array(), Duration.ofSeconds(exp)
        ).join();
        return response instanceof SetResponse.Success;
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

    // Helper function that mimics multi-get by asynchronously calling get on all the keys. Large enough sets of keys
    // are split into chunks to prevent too many simultaneous calls.
    private <T> Map<String, T> readFromMomento(final Collection<String> keys, final CacheTranscoder transcoder) {
        final Transcoder<T> cacheTranscoder = getTranscoder(transcoder);

        final Set<Set<String>> keyChunks = splitKeyset(keys);

        final Map<String, T> output = new HashMap<>();
        for (Set<String> keyChunk : keyChunks) {
            final Map<String, CachedData> cachedData = performMultiGet(keyChunk);
            for (Map.Entry<String, CachedData> entry : cachedData.entrySet()) {
                output.put(entry.getKey(), cacheTranscoder.decode(entry.getValue()));
            }
        }

        return output;
    }

    // Helper function that mimics multi-get by asynchronously calling get on all the keys. Large enough sets of keys
    // are split into chunks to prevent too many simultaneous calls.
    private Map<String, Object> readFromMomento(final Collection<String> keys) {
        final CacheTranscoder cacheTranscoder = getTranscoder();

        final Set<Set<String>> keyChunks = splitKeyset(keys);

        final Map<String, Object> output = new HashMap<>();
        for (Set<String> keyChunk : keyChunks) {
            final Map<String, CachedData> cachedData = performMultiGet(keyChunk);
            for (Map.Entry<String, CachedData> entry : cachedData.entrySet()) {
                output.put(entry.getKey(), cacheTranscoder.decode(new CachedObjectWrapper(entry.getValue())));
            }
        }

        return output;
    }

    private Optional<CachedData> performGet(final String key) {
        GetResponse response = momentoClient.get(defaultCacheName, key).join();
        if (response instanceof GetResponse.Hit) {
            return Optional.of(convertToCachedData(((GetResponse.Hit) response).valueByteArray()));
        }
        return Optional.empty();
    }

    private Map<String, CachedData> performMultiGet(final Collection<String> keys) {
        final Map<String, CompletableFuture<Optional<byte[]>>> futureMap = keys.stream()
                .collect(Collectors.toMap(key -> key, key -> momentoClient.get(defaultCacheName, key)
                        .thenApply(response -> {
                            if (response instanceof GetResponse.Hit) {
                                return Optional.of(((GetResponse.Hit) response).valueByteArray());
                            }
                            return Optional.empty();
                        })));

        final Map<String, CachedData> result = new HashMap<>();
        for (Map.Entry<String, CompletableFuture<Optional<byte[]>>> entry : futureMap.entrySet()) {
            final Optional<byte[]> bytesOpt = entry.getValue().join();
            if (bytesOpt.isPresent()) {
                final byte[] bytes = bytesOpt.get();
                accessLogRead("Multiget", entry.getKey(), bytes);
                result.put(entry.getKey(), convertToCachedData(bytes));
            } else {
                accessLogRead("Multiget", entry.getKey(), null);
            }
        }

        return result;
    }

    private CachedData convertToCachedData(final byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int flags = byteBuffer.getInt();
        // Ensure we perform a deep copy of the remaining bytes into a separate byte array
        byte[] originalData = new byte[byteBuffer.remaining()];
        byteBuffer.get(originalData);
        return new CachedData(flags, originalData, originalData.length);
    }

    private Set<Set<String>> splitKeyset(final Collection<String> keys) {
        final int chunkSize = 50;
        final Set<Set<String>> keyChunks = new HashSet<>();

        final Spliterator<String> split = keys.spliterator();
        while (true) {
            final Set<String> keyChunk = new HashSet<>(chunkSize);
            for (int i = 0; i < chunkSize; ++i) {
                if (!split.tryAdvance(keyChunk::add)) {
                    break;
                }
            }
            if (keyChunk.isEmpty()) {
                break;
            }
            keyChunks.add(keyChunk);
        }

        return keyChunks;
    }

    private void accessLogRead(String method, String key, Object data) {
        if (data == null) {
            LOGGER.debug(MessageFormat.format(
                    "{0}: MISS: no item found in cache for key: {1}",
                    method, key
            ));
        } else {
            LOGGER.debug(MessageFormat.format(
                    "{0}: HIT: item found in cache for key: {1}",
                    method, key
            ));
        }
    }

    private void accessLogWrite(String method, String key, boolean writeSuccess, int size, int ttl, int flags) {
        if (writeSuccess) {
            LOGGER.debug(MessageFormat.format(
                    "{0}: SetSuccess: item stored in cache key: {1} value_size: {2} ttl: {3} flags: {4}",
                    method, key, size, ttl, flags
            ));
        } else {
            LOGGER.debug(MessageFormat.format(
                    "{0}: SetFailure: item not stored in cache empty response for key: {1} value_size: {2} ttl: {3} flags: {4}",
                    method, key, size, ttl, flags
            ));
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
