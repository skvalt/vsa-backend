package com.vsa.nlp;

import com.vsa.model.response.ParsedIntentResponse;

import java.util.Map;

//Produces ParsedIntentResponse used by the Controller
public class ParserEngine {

    private final IntentDetector intentDetector = new IntentDetector();
    private final EntityExtractor entityExtractor = new EntityExtractor();

    public ParsedIntentResponse parse(String text) {
        String intent = intentDetector.detect(text);
        Map<String, String> entities = entityExtractor.extract(text);

        return ParsedIntentResponse.builder()
                .intent(intent)
                .entities(entities)
                .rawText(text)
                .build();
    }
}
