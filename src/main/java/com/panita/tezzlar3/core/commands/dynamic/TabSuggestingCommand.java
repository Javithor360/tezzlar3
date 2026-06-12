package com.panita.tezzlar3.core.commands.dynamic;

import com.panita.tezzlar3.core.commands.identifiers.CommandMeta;

/**
 * Interface for commands that can suggest tab completions.
 */
public interface TabSuggestingCommand {
    void applySuggestions(CommandMeta meta);
}

