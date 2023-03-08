package com.google.code.ssm.providers.momento;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.code.ssm.Cache;
import com.google.code.ssm.CacheFactory;
import com.google.code.ssm.api.ParameterValueKeyProvider;
import com.google.code.ssm.api.ReadThroughSingleCache;
import com.google.code.ssm.api.format.SerializationType;
import com.google.code.ssm.config.AbstractSSMConfiguration;
import com.google.code.ssm.providers.CacheException;
import lombok.Data;
import lombok.SneakyThrows;
import momento.sdk.SimpleCacheClient;
import momento.sdk.messages.CacheInfo;
import momento.sdk.messages.ListCachesResponse;
import org.junit.BeforeClass;
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
import java.util.*;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class TestPOC {

    private static final String MOMENTO_AUTH_TOKEN = System.getenv("MOMENTO_AUTH_TOKEN");
    // IMPORTANT: This presumes you have already created a cache in Momento named 'example-cache'
    private static final String MOMENTO_CACHE_NAME = "example-cache";

    @BeforeClass
    public static void setup() {
        final SimpleCacheClient client = SimpleCacheClient.builder(MOMENTO_AUTH_TOKEN, 600).build();

        final ListCachesResponse listCachesResponse = client.listCaches(Optional.empty());
        final boolean cacheExists = listCachesResponse.caches().stream()
                .map(CacheInfo::name)
                .anyMatch(name -> name.equals(MOMENTO_CACHE_NAME));

        if (!cacheExists) {
            client.createCache(MOMENTO_CACHE_NAME);
        }
    }

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
        SubObject subObject;

    }

    @Data
    @JsonSerialize
    public static class SubObject implements Serializable {
        String customerId;
        String customerName;
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
        if (cache != null) {
            return cache.get(objectKey, SerializationType.PROVIDER);
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
        // Uncomment if you want to verify objects are de/serialized properly
        // putTestObjectInCache();
        TestObject result = getComplexObject("test-complex-key");
        if (result == null) {
            System.out.println("Nothing found");
        } else {
            System.out.println("Got " + result);
        }
    }

    @Test
    @Ignore
    public void testGetBulk() throws Exception {
        final Cache cache = exampleCacheFactory.getCache();

        final Set<String> keys = putComplexObjectsInCache(100);

        final Map<String, Object> getBulkResult = cache.getBulk(keys, SerializationType.PROVIDER);

        assertEquals(getBulkResult.size(), keys.size());
        for (Object value : getBulkResult.values()) {
            assertTrue(value instanceof TestObject);
        }
    }

    // This can only be used if you have a valid MOMENTO_AUTH_TOKEN in your environment
    // along with a cache created named "example-cache"
    private void putTestObjectInCache() throws CacheException, TimeoutException {
        putComplexObjectsInCache(1);
    }

    private Set<String> putComplexObjectsInCache(int numberOfObjects) throws CacheException, TimeoutException {
        final Set<String> keys = new HashSet<>();
        for (int i = 0; i < numberOfObjects; ++i) {
            final String key = UUID.randomUUID().toString();

            final SubObject subObject = new SubObject();
            subObject.setCustomerId("abc" + i);
            subObject.setCustomerName("customerName" + i);
            final TestObject testObject = new TestObject();
            testObject.setName("testObjectName" + i);
            testObject.setId(i);
            testObject.setSubObject(subObject);

            exampleCacheFactory.getCache().set(key, 30, testObject, SerializationType.PROVIDER);
            keys.add(key);
        }
        return keys;
    }
}
