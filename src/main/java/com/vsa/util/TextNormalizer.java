package com.vsa.util;

import java.text.Normalizer;

//Basic normalization: lowercase, remove diacritics, collapse whitespace.

public final class TextNormalizer {

    public static String normalize(String input) {
        if (input == null) return null;
        String n = Normalizer.normalize(input, Normalizer.Form.NFKD);
        n = n.replaceAll("\\p{M}", ""); // strip diacritics
        n = n.replaceAll("[^\\p{Alnum}\\s]", ""); // remove punctuation
        n = n.toLowerCase().trim();
        n = n.replaceAll("\\s+", " ");
        return n;
    }
}
