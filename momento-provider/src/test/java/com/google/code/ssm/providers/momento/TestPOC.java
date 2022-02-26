package com.google.code.ssm.providers.momento;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.code.ssm.Cache;
import com.google.code.ssm.CacheFactory;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughSingleCache;
import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.config.AbstractSSMConfiguration;
import com.google.code.ssm.config.DefaultAddressProvider;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.ToString;
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
    @Configuration
    public static class ContextConfiguration extends AbstractSSMConfiguration {
        @Bean
        @Override
        public CacheFactory defaultMemcachedClient() {
            final MomentoConfiguration conf = new MomentoConfiguration();
            conf.setConsistentHashing(true);
            conf.setDefaultTtl(300);
            // IMPORTANT: This presumes you have already created a cache in Momento named 'example-cache'
            conf.setDefaultCacheName("example-cache");
            conf.setMomentoAuthToken("<YOUR AUTH TOKEN GOES HERE>");
            final CacheFactory cf = new CacheFactory();
            cf.setCacheClientFactory(new MomentoClientFactoryImpl());
            // Doesn't actually do anything
            cf.setAddressProvider(new DefaultAddressProvider("127.0.0.1:11211"));
            cf.setConfiguration(conf);
            return cf;
        }
    }
    @Autowired
    private CacheFactory cacheFactory;

    @Data
    @JsonSerialize
    public static class TestObject implements Serializable {
        int id;
        String name;
    }


    @ReadThroughSingleCache(namespace = "TestString", expiration = 3600)
    @SneakyThrows
    public String getSimpleObject(@ParameterValueKeyProvider String complexObjectPk) {
        Cache cache = cacheFactory.getObject();
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
        Cache cache = cacheFactory.getObject();
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
    public void testPocSimple() {
        String test = "fooBar";
        cacheFactory.getObject().set("test-key", 500, test, SerializationType.PROVIDER);
        String result = getSimpleObject("test-key");
        if (result == null) {
            System.out.println("Nothing found");
        } else {
            System.out.println("Expected fooBar, got: " + result);
        }
    }

    @Test
    @SneakyThrows
    public void testPocComplex() {
        TestObject t = new TestObject();
        t.setId(3);
        t.setName("testName");
        // The couchbase serializer has some issues with basic POJOs, so we just use Jackson to serialize
        // and deserialize the objects when interacting with the cache for an easy path forward
        ObjectMapper mapper = new ObjectMapper();
        cacheFactory.getObject().set("test-complex-key", 500, mapper.writeValueAsString(t), SerializationType.PROVIDER);
        TestObject result = getComplexObject("test-complex-key");
        if (result == null) {
            System.out.println("Nothing found");
        } else {
            System.out.println("Expected " + t + " got " + result);
        }
    }
}
