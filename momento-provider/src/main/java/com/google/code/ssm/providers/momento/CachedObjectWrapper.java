/*
 * Copyright (c) 2014-2019 Jakub Białek
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

import net.spy.memcached.CachedData;

import com.google.code.ssm.providers.CachedObject;

class CachedObjectWrapper implements CachedObject {

    private final CachedData cachedData;

    CachedObjectWrapper(final CachedData cachedData) {
        this.cachedData = cachedData;
    }

    @Override
    public byte[] getData() {
        return cachedData.getData();
    }

    @Override
    public int getFlags() {
        return cachedData.getFlags();
    }

    @Override
    public String toString() {
        return "CachedObjectWrapper [cachedData=" + cachedData + "]";
    }

}
