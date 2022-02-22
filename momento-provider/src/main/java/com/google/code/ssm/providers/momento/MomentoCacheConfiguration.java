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

import java.util.Collection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.ConnectionFactoryBuilder.Locator;
import net.spy.memcached.ClientMode;
import net.spy.memcached.ConnectionObserver;
import net.spy.memcached.FailureMode;
import net.spy.memcached.HashAlgorithm;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.metrics.MetricCollector;
import net.spy.memcached.metrics.MetricType;
import net.spy.memcached.transcoders.Transcoder;

import com.google.code.ssm.providers.CacheConfiguration;

/**
 * Allows setting provider specific settings. If property is not set (null) default value defined by provider will be
 * used. Description of each property can be found in {@link ConnectionFactoryBuilder} class.
 * 
 * @author Jakub Białek
 * @since 3.5.0
 * 
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MomentoCacheConfiguration extends CacheConfiguration {

    /**
     * default transcoder or null if not set
     * 
     * @see ConnectionFactoryBuilder#setTranscoder(Transcoder)
     */
    private Transcoder<Object> defaultTranscoder;
}
