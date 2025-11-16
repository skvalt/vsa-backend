package com.vsa.service;

import com.vsa.model.Item;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

//Stores minimal action info per user (or null for anonymous).
@Service
public class UndoService {

    public static class UndoAction {
        public enum Type { ADD, DELETE, UPDATE }
        public Type type;
        public Item before; 
        public Item after;  
    }

    private final Map<String, Deque<UndoAction>> stacks = new ConcurrentHashMap<>();

    private Deque<UndoAction> stackFor(String userKey) {
        return stacks.computeIfAbsent(userKey == null ? "__ANON__" : userKey, k -> new ArrayDeque<>());
    }

    public void pushAdd(String userKey, Item added) {
        UndoAction a = new UndoAction();
        a.type = UndoAction.Type.ADD;
        a.after = added;
        stackFor(userKey).push(a);
    }

    public void pushDelete(String userKey, Item deleted) {
        UndoAction a = new UndoAction();
        a.type = UndoAction.Type.DELETE;
        a.before = deleted;
        stackFor(userKey).push(a);
    }

    public void pushUpdate(String userKey, Item beforeOrAfter) {

        UndoAction a = new UndoAction();
        a.type = UndoAction.Type.UPDATE;
        a.after = beforeOrAfter;
        stackFor(userKey).push(a);
    }

    public Optional<UndoAction> popLastRaw(String userKey) {
        Deque<UndoAction> d = stackFor(userKey);
        if (d.isEmpty()) return Optional.empty();
        return Optional.of(d.pop());
    }

    //Provided for VoiceController to pop and interpret; we return object raw

    public Optional<Object> popLast(String userKey) {
        Optional<UndoAction> oa = popLastRaw(userKey);
        if (oa.isEmpty()) return Optional.empty();
        UndoAction a = oa.get();
        // return a simple map describing the undoable action
        return Optional.of(Map.of("type", a.type.name(), "before", a.before, "after", a.after));
    }
}
