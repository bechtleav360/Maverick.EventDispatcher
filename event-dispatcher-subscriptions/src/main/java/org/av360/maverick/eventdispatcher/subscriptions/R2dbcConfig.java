package org.av360.maverick.eventdispatcher.subscriptions;
import io.r2dbc.spi.ConnectionFactory;
import org.av360.maverick.eventdispatcher.shared.MapToJsonConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class R2dbcConfig {

    private final ConnectionFactory connectionFactory;

    public ConnectionFactory connectionFactory() {
        return connectionFactory;
    }
    public R2dbcConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(R2dbcDialect dialect) {
        List<Object> converters = new ArrayList<>();
        converters.add(new MapToJsonConverter.MapToJson());
        converters.add(new MapToJsonConverter.JsonToMap());
        return new R2dbcCustomConversions(R2dbcCustomConversions.StoreConversions.of(dialect.getSimpleTypeHolder()), converters);
    }

    @Bean
    public MappingR2dbcConverter mappingR2dbcConverter(R2dbcCustomConversions r2dbcCustomConversions) {
        R2dbcMappingContext mappingContext = new R2dbcMappingContext();
        mappingContext.setSimpleTypeHolder(r2dbcCustomConversions.getSimpleTypeHolder());
        MappingR2dbcConverter mappingR2dbcConverter = new MappingR2dbcConverter(mappingContext, r2dbcCustomConversions);
        return mappingR2dbcConverter;
    }

    @Bean
    public R2dbcDialect r2dbcDialect() {
        return DialectResolver.getDialect(connectionFactory);
    }


}