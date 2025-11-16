package com.vsa.nlp;

//Returns a string representing user intent based on keywords in text.
public class IntentDetector {

    public String detect(String text) {
        String t = text.toLowerCase();

        if (t.matches(".*\\b(add|buy|need|get|order|bring)\\b.*"))
            return "ADD_ITEM";

        if (t.matches(".*\\b(remove|delete|drop|discard|subtract)\\b.*"))
            return "REMOVE_ITEM";

        if (t.matches(".*\\b(find|search|look for|show me|locate)\\b.*"))
            return "SEARCH_ITEM";

        if (t.matches(".*\\b(list|show my list|my list)\\b.*"))
            return "SHOW_LIST";

        return "UNKNOWN";
    }
}
