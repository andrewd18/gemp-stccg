package com.gempukku.stccg.fieldprocessor;

import com.gempukku.stccg.cards.BuiltLotroCardBlueprint;
import com.gempukku.stccg.cards.CardGenerationEnvironment;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.common.Keyword;

import java.util.HashMap;
import java.util.Map;

public class KeywordFieldProcessor implements FieldProcessor {
    @Override
    public void processField(String key, Object value, BuiltLotroCardBlueprint blueprint, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String[] keywords = FieldUtils.getStringArray(value, key);
        blueprint.setKeywords(convertKeywords(keywords, key));
    }

    private Map<Keyword, Integer> convertKeywords(String[] keywords, String key) throws InvalidCardDefinitionException {
        Map<Keyword, Integer> result = new HashMap<>();
        for (String keywordString : keywords) {
            final String[] keywordSplit = keywordString.split("\\+");
            Keyword keyword = FieldUtils.getEnum(Keyword.class, keywordSplit[0], key);
            int value = 1;
            if (keywordSplit.length == 2)
                value = Integer.parseInt(keywordSplit[1]);
            result.put(keyword, value);
        }
        return result;
    }

}
