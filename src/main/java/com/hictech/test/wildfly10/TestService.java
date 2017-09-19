package com.hictech.test.wildfly10;

import static org.infinispan.configuration.cache.CacheMode.LOCAL;
import static org.infinispan.configuration.cache.CacheMode.REPL_SYNC;
import static org.infinispan.transaction.TransactionMode.TRANSACTIONAL;
import static org.infinispan.util.concurrent.IsolationLevel.SERIALIZABLE;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Transport;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.lookup.GenericTransactionManagerLookup;

@Stateless
public class TestService {

	private static Logger logger = Logger.getLogger(TestService.class);
	@Resource(lookup = "java:jboss/infinispan/container/server")
	private EmbeddedCacheManager manager;

	public Integer test() {
		String test;
		try {
			manager.defineConfiguration("default", conf());
			
			test  = "cache manager class: " + cls(manager) + "\n";

			Cache<Object, Object> cache = manager.getCache();
			test += "cache default class: " + cls(cache) + "\n";

			AdvancedCache<Object, Object> advanced_cache = cache.getAdvancedCache();
			test += "tx manager class:    " + advanced_cache.getTransactionManager() + "\n";
			
			TestClass myval = (TestClass)advanced_cache.get("mykey");
			
			if(myval == null) {
				myval  = new TestClass();
				logger.info("mykey wasn't found in cache");
				advanced_cache.put("mykey", myval);
			}
			
			Integer retVal = myval.incrementAndGet();
			advanced_cache.replace("mykey", myval);
			logger.info("myval = " + retVal);
			logger.info("test = " + test);

			return retVal;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Configuration conf() {
		Transport transport = manager.getGlobalComponentRegistry().getGlobalConfiguration().transport().transport();
		
		logger.info("Transport: " + transport);
		
		return new ConfigurationBuilder()
			.clustering()
				.cacheMode(transport == null? LOCAL : REPL_SYNC)
				
			.transaction()
				.transactionMode(TRANSACTIONAL)
				.transactionManagerLookup(new GenericTransactionManagerLookup())
				.autoCommit(false)
			
			.lockingMode(LockingMode.PESSIMISTIC)
				.locking()
				.isolationLevel(SERIALIZABLE)
				
			.persistence()
				.passivation(false)
				.addSingleFileStore()
				.purgeOnStartup(true)
		.build();
	}
	
	private String cls(Object o) {
		return o != null? o.getClass().toString() : "null";
	}

}
