# Global Agent Instructions

The following rules must be strictly adhered to during the development of this project:

1. **Build Tool**: Use Gradle as the build standard for the project.
2. **Architecture and Practices**: Prioritize a modular structure (Feature-Based), separation of concerns (Commands, Listeners, Utilities, Managers), and dynamic component registration (e.g., using org.reflections), consistent with the practices analyzed in the `Panita275` project.
3. **Language Standards**: 
   - All code comments must be written in English.
   - All class, method, and variable names must be written in English.
   - Only user-facing strings, messages, and text outputs must be written in Spanish.
4. **Agent Persona**: Maintain a serious, impersonal, and highly professional tone in all communications to ensure accuracy and focus on problem-solving.
5. **Architectural Implementation Details (Tezzlar Framework)**:
   - **Commands**: All new commands must be created under `com.panita.tezzlar3.<module>.commands`, implement `AdvancedCommand` (or `TabSuggestingCommand`), and be annotated with `@CommandSpec` or `@SubCommandSpec`. Do NOT register commands in `plugin.yml`.
   - **Listeners**: All new listeners must be created under `com.panita.tezzlar3.<module>.listeners` and implement `Listener`.
   - **Modules**: New feature sets must be isolated in their own packages (e.g., `com.panita.tezzlar3.economy`), implement `PluginModule`, and must be explicitly registered via `Tezzlar.getModuleManager().register(...)`.
6. **Code Quality and SOLID Principles**:
   - **Single Responsibility Principle (SRP)**: Maintain strict separation of concerns. Utility classes (e.g., `SoundUtils`, `Messenger`) must focus exclusively on their domain without crossing into business logic.
   - **Open/Closed Principle (OCP)**: Design systems to be easily extensible. New commands or modules must not alter the logic of existing registries.
   - **Modern Java Conventions**: Utilize the Streams API for collection processing and adopt Pattern Matching (e.g., `instanceof Player player`) for safe and implicit casting.
7. **Explicit Imports**: When modifying or creating Java classes, NEVER leave fully qualified paths in the code body (e.g., `org.bukkit.ban.ProfileBanList`). You must add the corresponding imports at the top of the file to keep the code clean.

# Additional Tezzlar3 Project Rules

- ALWAYS avoid "inline imports" (e.g., use header imports instead of referencing the fully qualified package in a line of code).
- For a command structure (in `@CommandSpec`), the `syntax` and `description` properties must always be written in English.
