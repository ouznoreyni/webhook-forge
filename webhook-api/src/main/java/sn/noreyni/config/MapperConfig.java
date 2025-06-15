package sn.noreyni.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class MapperConfig {

    /**
     * ModelMapper configuration for entity-DTO mapping
     */
    @Produces
    @Singleton
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setFullTypeMatchingRequired(true)
                .setPropertyCondition(context -> context.getSource() != null)
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true)
                .setAmbiguityIgnored(true);
        return modelMapper;
    }

    /**
     * Custom ObjectMapper configuration using Quarkus ObjectMapperCustomizer
     */
    @ApplicationScoped
    public static class CustomObjectMapperConfig implements ObjectMapperCustomizer {

        @Override
        public void customize(ObjectMapper mapper) {
            // Stream constraints
            mapper.getFactory().setStreamReadConstraints(
                    StreamReadConstraints.builder()
                            .maxStringLength(Integer.MAX_VALUE)
                            .build()
            );

            // Deserialization features
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
            mapper.enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY);
            mapper.enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);

            // Serialization features
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            // Include non-null values only
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            // Register modules
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            mapper.registerModule(javaTimeModule);
            mapper.registerModule(new CustomTimeModule());
        }
    }

    /**
     * Custom time serialization module
     */
    public static class CustomTimeModule extends SimpleModule {

        public CustomTimeModule() {
            super("CustomTimeModule");

            // Custom date/time serializers
            addSerializer(LocalDate.class,
                    new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            addSerializer(LocalDateTime.class,
                    new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
            addSerializer(LocalTime.class, new LocalTimeSerializer());
        }

        /**
         * Custom LocalTime serializer
         */
        private static class LocalTimeSerializer extends JsonSerializer<LocalTime> {
            @Override
            public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers)
                    throws IOException {
                gen.writeString(value.format(DateTimeFormatter.ofPattern("HH:mm")));
            }
        }
    }
}
