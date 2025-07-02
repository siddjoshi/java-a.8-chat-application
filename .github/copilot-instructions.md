<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

This is a Java 1.8 Spring Boot project for a real-time anonymous chat application.

## Project Structure
- Uses Spring Boot 1.5.22 (compatible with Java 1.8)
- Spring WebSocket with STOMP protocol for real-time communication
- Thymeleaf for server-side templating
- H2 in-memory database for session storage
- Bootstrap 4 for responsive UI

## Key Features
- Anonymous user pairing for 1-on-1 chats
- Real-time messaging with WebSocket
- Typing indicators
- User session management
- Automatic cleanup of inactive users
- Mobile-responsive design

## Coding Guidelines
- Follow Java 8 syntax and features
- Use Spring Boot 1.5.x annotations and patterns
- Implement proper error handling and logging
- Maintain thread safety for concurrent operations
- Use SLF4J for logging
- Follow REST API conventions for endpoints
