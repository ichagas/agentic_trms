package com.trms.ai.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * FunctionCallTracker - Thread-safe tracker for AI function calls
 *
 * This component tracks which functions are called during AI processing,
 * regardless of whether they're called by rule-based logic or LLM decision.
 * Uses ThreadLocal to ensure thread safety in concurrent requests.
 */
@Component
public class FunctionCallTracker {

    private final ThreadLocal<List<String>> functionCalls = ThreadLocal.withInitial(ArrayList::new);

    /**
     * Register a function call
     * @param functionName The name of the function that was called
     */
    public void trackFunctionCall(String functionName) {
        functionCalls.get().add(functionName);
    }

    /**
     * Get all tracked function calls for the current thread
     * @return List of function names that were called
     */
    public List<String> getFunctionCalls() {
        return new ArrayList<>(functionCalls.get());
    }

    /**
     * Clear all tracked function calls for the current thread
     */
    public void clear() {
        functionCalls.get().clear();
    }

    /**
     * Remove the ThreadLocal to prevent memory leaks
     */
    public void cleanup() {
        functionCalls.remove();
    }
}
