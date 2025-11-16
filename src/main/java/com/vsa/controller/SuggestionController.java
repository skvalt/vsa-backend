package com.vsa.controller;

import com.vsa.model.Product;
import com.vsa.model.response.ParsedIntentResponse;
import com.vsa.service.SuggestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//Endpoints for suggestions (recommendations).

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    
    @GetMapping
    public ResponseEntity<List<Product>> getSuggestions(
            @RequestParam(required = false) String userId
    ) {
        return ResponseEntity.ok(suggestionService.suggest(userId));
    }

    //Get suggestions within a specific category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> suggestionsByCategory(
            @PathVariable String category
    ) {
        return ResponseEntity.ok(suggestionService.suggestByCategory(category));
    }

    //Suggest substitutes based on parsed voice intent.
    @PostMapping("/substitutes")
    public ResponseEntity<List<Product>> substitutes(
            @RequestBody ParsedIntentResponse parsed
    ) {
        return ResponseEntity.ok(suggestionService.suggestSubstitutes(parsed));
    }
}
