package com.panita.tezzlar3.core.commands.dynamic;

import com.panita.tezzlar3.core.commands.identifiers.CommandSpec;
import com.panita.tezzlar3.core.commands.identifiers.SubCommandSpec;

import java.util.HashMap;
import java.util.Map;

/*
 * CommandMetadataCache is a utility class that caches the syntax of commands
 * to avoid repeated reflection calls.
 */
public class CommandMetadataCache {
    private static final Map<Class<?>, String> syntaxMap = new HashMap<>(); // Cache for command syntax

    /**
     * Retrieves the syntax of a command class, caching it for future use.
     *
     * @param clazz The class of the command.
     * @return The syntax of the command.
     */
    public static String getSyntax(Class<?> clazz) {
        // Check if the syntax is already cached
        if (syntaxMap.containsKey(clazz)) {
            return syntaxMap.get(clazz);
        }

        // Search for the CommandSpec or SubCommandSpec annotation and cache the syntax
        if (clazz.isAnnotationPresent(CommandSpec.class)) {
            String syntax = clazz.getAnnotation(CommandSpec.class).syntax();
            syntaxMap.put(clazz, syntax);
            return syntax;
        }

        if (clazz.isAnnotationPresent(SubCommandSpec.class)) {
            String syntax = clazz.getAnnotation(SubCommandSpec.class).syntax();
            syntaxMap.put(clazz, syntax);
            return syntax;
        }

        // If no annotation is found, return a default value
        return "Fuera de uso";
    }
}

