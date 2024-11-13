package org.demo.webflux.service.impl;

import org.demo.webflux.changelog.AbstractMongoDataLoader;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class MongoDataLoaderService extends AbstractMongoDataLoader {

    public MongoDataLoaderService(ReactiveMongoTemplate mongoTemplate) {
        super(mongoTemplate);
    }

    @PreDestroy
    private void destroy() {
        cleanData();
    }

    @PostConstruct
    private void init() {
        loadData();
    }

}
