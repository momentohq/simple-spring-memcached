package com.google.code.ssm.providers.momento;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.code.ssm.Cache;
import com.google.code.ssm.CacheFactory;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughSingleCache;
import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.config.AbstractSSMConfiguration;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.io.Serializable;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader= AnnotationConfigContextLoader.class)
public class TestPOC {

    private static final String MOMENTO_AUTH_TOKEN = "<YOUR MOMENTO TOKEN GOES HERE>";
    // IMPORTANT: This presumes you have already created a cache in Momento named 'example-cache'
    private static final String MOMENTO_CACHE_NAME = "example-cache";

    @Configuration
    public static class ExampleCacheConfiguration extends AbstractSSMConfiguration {
        @Bean
        @Override
        public CacheFactory defaultMemcachedClient() {
            final MomentoConfiguration conf = new MomentoConfiguration();
            conf.setConsistentHashing(true);
            conf.setDefaultTtl(300);
            conf.setCacheName(MOMENTO_CACHE_NAME);
            conf.setMomentoAuthToken(MOMENTO_AUTH_TOKEN);
            final CacheFactory cacheFactory = new CacheFactory();
            cacheFactory.setCacheClientFactory(new MomentoCacheClientFactory());
            // Use a MomentoAddressProvider to be explicit
            cacheFactory.setAddressProvider(new MomentoAddressProvider());
            cacheFactory.setConfiguration(conf);
            return cacheFactory;
        }
    }

    @Autowired
    private CacheFactory exampleCacheFactory;

    @Data
    @JsonSerialize
    public static class TestObject implements Serializable {
        int id;
        String name;
    }


    @ReadThroughSingleCache(namespace = "TestString", expiration = 3600)
    @SneakyThrows
    public String getSimpleObject(@ParameterValueKeyProvider String complexObjectPk) {
        Cache cache = exampleCacheFactory.getObject();
        if (cache != null) {
            String result = cache.get(complexObjectPk, SerializationType.PROVIDER);
            if (result != null) {
                return result;
            }
        }
        // Otherwise, retrieve object from source location, e.g. DynamoDB, S3
        // return getObjectFromSource(complexObjectPk);
        return null;
    }

    @ReadThroughSingleCache(namespace = "TestObject", expiration = 3600)
    @SneakyThrows
    public TestObject getComplexObject(@ParameterValueKeyProvider String objectKey) {
        Cache cache = exampleCacheFactory.getObject();
        ObjectMapper mapper = new ObjectMapper();
        if (cache != null) {
            TestObject obj = mapper.readValue((String) cache.get(objectKey, SerializationType.PROVIDER), TestObject.class);
            if (obj != null) {
                return obj;
            }
        }
        // Otherwise, retrieve object from source location, e.g. DynamoDB, S3
        // return getObjectFromSource(objectKey);
        return null;
    }


    @Test
    @SneakyThrows
    @Ignore // Comment out if you'd like to test this out yourself
    public void testPocSimple() {
        String result = getSimpleObject("test-key");
        if (result == null) {
            System.out.println("Nothing found");
        } else {
            System.out.println("Got: " + result);
        }
    }

    @Test
    @SneakyThrows
    @Ignore // Comment out if you'd like to test this out yourself
    public void testPocComplex() {
        TestObject result = getComplexObject("test-complex-key");
        if (result == null) {
            System.out.println("Nothing found");
        } else {
            System.out.println("Got " + result);
        }
    }
}
