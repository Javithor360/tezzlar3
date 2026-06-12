package com.panita.tezzlar3.core.commands.dynamic;

import java.util.List;

/**
 * Functional interface for providing tab completion suggestions.
 */
@FunctionalInterface
public interface SuggestionProvider {
    List<String> suggest(TabContext context);
}

