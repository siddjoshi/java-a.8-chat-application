# Anonymous Chat Application

A real-time anonymous chat application built with Java 1.8 and Spring Boot that allows two users to chat anonymously with each other.

## Features

- **Anonymous Pairing**: Users are automatically paired with other online users
- **Real-time Messaging**: Instant message delivery using WebSocket and STOMP
- **Typing Indicators**: See when your chat partner is typing
- **Find New Partner**: Easily disconnect and find a new chat partner
- **Mobile Responsive**: Works seamlessly on desktop and mobile devices
- **Auto Cleanup**: Automatic removal of inactive users
- **Connection Status**: Clear indication of connection and pairing status

## Technologies Used

- **Java 1.8**
- **Spring Boot 1.5.22**
- **Spring WebSocket & STOMP**
- **Thymeleaf** for templating
- **H2 Database** (in-memory)
- **Bootstrap 4** for UI
- **SockJS** for WebSocket fallback
- **Maven** for dependency management

## Getting Started

### Prerequisites

- Java 1.8 or higher
- Maven 3.3+

### Installation & Running

1. **Clone or download the project**
   ```bash
   cd c:\temp\Java-demos
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Open your browser and go to: `http://localhost:8080`
   - Open multiple browser tabs/windows to test the chat functionality

### Alternative Running Methods

**Using Maven wrapper:**
```bash
./mvnw spring-boot:run     # Linux/Mac
mvnw.cmd spring-boot:run   # Windows
```

**Running the JAR:**
```bash
mvn package
java -jar target/anonymous-chat-1.0.0.jar
```

## How It Works

1. **Connection**: When a user opens the application, they automatically connect to the WebSocket server
2. **Pairing**: The system pairs users in a first-come-first-served basis
3. **Chatting**: Once paired, users can exchange messages in real-time
4. **New Partner**: Users can click "Find New Partner" to disconnect and find someone new
5. **Auto Cleanup**: Inactive users are automatically removed after 30 minutes

## API Endpoints

### REST Endpoints
- `GET /` - Main chat interface
- `GET /api/stats` - Get current online users count
- `GET /health` - Application health check
- `GET /h2-console` - H2 database console (development)

### WebSocket Endpoints
- `/ws` - Main WebSocket connection
- `/app/chat.send` - Send a message
- `/app/chat.typing` - Send typing indicator
- `/app/chat.findNew` - Find new partner
- `/user/queue/messages` - Receive private messages

## Project Structure

```
src/
├── main/
│   ├── java/com/example/anonymouschat/
│   │   ├── AnonymousChatApplication.java      # Main application class
│   │   ├── config/
│   │   │   ├── WebSocketConfig.java           # WebSocket configuration
│   │   │   └── SchedulingConfig.java          # Scheduled tasks config
│   │   ├── controller/
│   │   │   ├── ChatController.java            # WebSocket message handling
│   │   │   └── WebController.java             # REST endpoints
│   │   ├── model/
│   │   │   ├── ChatMessage.java               # Message model
│   │   │   └── ChatUser.java                  # User model
│   │   └── service/
│   │       └── ChatService.java               # Core chat business logic
│   └── resources/
│       ├── templates/
│       │   └── index.html                     # Main chat interface
│       └── application.properties             # Application configuration
└── test/
    └── java/com/example/anonymouschat/
        └── AnonymousChatApplicationTests.java # Basic tests
```

## Configuration

The application can be configured through `src/main/resources/application.properties`:

- **Server Port**: `server.port=8080`
- **Database**: H2 in-memory database
- **Logging**: Configurable log levels
- **WebSocket**: Heartbeat and connection settings

## Security Features

- Input sanitization to prevent XSS attacks
- Message length limiting (500 characters)
- Rate limiting on message sending
- Automatic session cleanup
- No persistent data storage for privacy

## Development

### Building
```bash
mvn clean compile
```

### Testing
```bash
mvn test
```

### Packaging
```bash
mvn package
```

### Development Mode
The application supports hot reloading for development. Thymeleaf caching is disabled in development mode.

## Browser Compatibility

- Chrome 60+
- Firefox 55+
- Safari 11+
- Edge 16+

## Troubleshooting

### Common Issues

1. **Port already in use**
   - Change the port in `application.properties`: `server.port=8081`

2. **WebSocket connection fails**
   - Check firewall settings
   - Ensure the application is running on the correct port

3. **Messages not appearing**
   - Check browser console for JavaScript errors
   - Verify WebSocket connection status

### Logs
Application logs are printed to the console. Check for any error messages during startup or operation.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if necessary
5. Submit a pull request

## License

This project is open source and available under the MIT License.

## Support

For issues or questions, please check the troubleshooting section or create an issue in the project repository.
