package org.av360.maverick.eventdispatcher.shared;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapToJsonConverter {



    @WritingConverter
    public static class MapToJson implements Converter<Map<String, String>, String> {
        ObjectMapper objectMapper = new ObjectMapper();
        @Override
        public String convert(Map<String, String> source) {
            try {
                return objectMapper.writeValueAsString(source);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Unable to convert map to JSON", e);
            }
        }
    }

    private static class MapDeserializer extends JsonDeserializer<Map<String, String>> {

        @Override
        public Map<String, String> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            Map<String, String> map = new HashMap<>();
            JsonNode node = jp.getCodec().readTree(jp);
            // TODO: Fix this, map is always empty
            node.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                String value = entry.getValue().asText();
                map.put(key, value);
            });
            return map;
        }
    }

    @ReadingConverter
    public static class JsonToMap implements Converter<String, Map<String, String>> {

        private final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new SimpleModule().addDeserializer(Map.class, new MapDeserializer()));

        @Override
        public Map<String, String> convert(String source) {
            try {
                JsonFactory factory = new JsonFactory();
                factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
                JsonParser parser = factory.createParser(source);
                JsonNode node = objectMapper.readTree(parser);
                return objectMapper.convertValue(node, objectMapper.getTypeFactory().constructParametricType(Map.class, String.class, String.class));
            } catch (IOException e) {
                throw new RuntimeException("Unable to convert JSON to map", e);
            }
        }
    }
}