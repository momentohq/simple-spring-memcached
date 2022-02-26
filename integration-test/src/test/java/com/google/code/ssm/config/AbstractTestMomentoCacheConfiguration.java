/*
 * Copyright (c) 2018-2019 Jakub Białek
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

package com.google.code.ssm.config;

import com.google.code.ssm.CacheFactory;
import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.providers.CacheClientFactory;
import com.google.code.ssm.providers.momento.MomentoConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 *
 * @author Jakub Białek
 *
 */
@ComponentScan(basePackages = "com.google.code.ssm.test")
public abstract class AbstractTestMomentoCacheConfiguration extends AbstractSSMConfiguration {

    // easy way to test different types of serialization mechanizm by passing it as system property
    // (-Dssm.provider=JSON)
    @Value("#{systemProperties['ssm.defaultSerializationType']?:'PROVIDER'}")
    private SerializationType defaultSerialization;

    @Bean
    @Override
    public CacheFactory defaultMemcachedClient() {
        final CacheFactory cf = new CacheFactory();
        cf.setCacheClientFactory(getCacheClientFactory());
        cf.setAddressProvider(new DefaultAddressProvider("127.0.0.1:11211"));
        cf.setConfiguration(getCacheConfiguration());
        cf.setDefaultSerializationType(defaultSerialization);
        return cf;
    }


    @EventListener(ContextRefreshedEvent.class)
    public void flushDefaultMemcached() throws Exception {
        defaultMemcachedClient().getObject().flush();
    }

    protected MomentoConfiguration getCacheConfiguration() {
        final MomentoConfiguration conf = new MomentoConfiguration();
        conf.setDefaultCacheName("example-cache");
        conf.setDefaultTtl(300);
        conf.setMomentoAuthToken("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0eWxlciIsImNwIjoiY29udHJvbC5kZXZlbG9wZXItdHlsZXItZGV2LnByZXByb2QuYS5tb21lbnRvaHEuY29tIiwiYyI6ImNhY2hlLmRldmVsb3Blci10eWxlci1kZXYucHJlcHJvZC5hLm1vbWVudG9ocS5jb20ifQ.6H_orvmdWSBSuOTrv-Vdwv2xfuispj5VPy5o3EIMhrIXiGPPNFe9ankTVuLQry47ODJeDSciI1eEMtnW8DfY5A");
        return conf;
    }

    protected abstract CacheClientFactory getCacheClientFactory();

}
