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

import lombok.SneakyThrows;
import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;

import com.google.code.ssm.providers.CacheTranscoder;
import com.google.code.ssm.providers.CachedObject;

/**
 *
 * @author Jakub Białek
 * @since 3.5.0
 *
 */
class TranscoderAdapter implements Transcoder<Object> {

    private final CacheTranscoder transcoder;

    TranscoderAdapter(final CacheTranscoder transcoder) {
        this.transcoder = transcoder;
    }

    @Override
    public boolean asyncDecode(final CachedData d) {
        return false;
    }

    @Override
    public Object decode(final CachedData d) {
        return transcoder.decode(new com.google.code.ssm.providers.momento.CachedObjectWrapper(d));
    }

    @Override
    public CachedData encode(final Object o) {
        CachedObject cachedObject = transcoder.encode(o);
        return new CachedData(cachedObject.getFlags(), cachedObject.getData(), CachedObject.MAX_SIZE);
    }

    @Override
    public int getMaxSize() {
        return CachedObject.MAX_SIZE;
    }

}
