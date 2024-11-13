package org.demo.webflux.service.impl.mapping;

import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MappingService extends ConfigurableMapper {

    private MapperFactory factory;

    @Override
    public void configure(MapperFactory factory) {
        this.factory = factory;
    }

    private void registerCustomMappers(MapperFactory factory) {
    }

    @PostConstruct
    public void registerStuff() {
        registerCustomMappers(factory);
    }

}
