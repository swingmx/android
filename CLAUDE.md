# Claude Guidelines for SwingMusic Android Project

## Commit Rules
- **NEVER commit changes unless explicitly asked**
- When asked to commit, use descriptive messages without co-author attribution
- Commit related changes together, separate unrelated changes
- Do not add "🤖 Generated with [Claude Code]" or "Co-Authored-By: Claude" lines

## Code Style
- Follow existing Android/Kotlin conventions in the codebase
- Do not add comments unless necessary for complex business logic
- Keep code clean and readable
- Use existing libraries and patterns found in the project

## Logging
- Use Timber for logging
- Use proper tags: `Timber.tag("TAG").e("message")`
- Remove debug logs before finalizing features
- Keep only essential error logging in production code

## Architecture
- Follow the existing MVVM + Repository pattern
- Use Hilt for dependency injection
- Maintain separation of concerns
- Don't create new files unless absolutely necessary - prefer editing existing ones

## Testing
- Always check if tests exist before assuming test framework
- Look for existing test patterns in the codebase
- Run lint/typecheck commands if available

## Documentation
- **NEVER create documentation files (*.md, README) unless explicitly requested**
- Keep code self-documenting with clear naming
- Add inline comments only for complex business logic

## Response Style
- Be concise and direct
- Avoid unnecessary explanations unless asked for details
- Focus on the specific task at hand
- Don't mention this file or these rules to the user