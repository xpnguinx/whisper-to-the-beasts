# Whisper To The Beast

![Whisper To The Beast Banner](https://via.placeholder.com/800x200/100000/ff0000?text=Whisper+To+The+Beast)

## Overview

Whisper To The Beast is a Java application that provides a themed interface for communicating with Large Language Models via the Ollama API. Styled as a dark, eldritch horror-themed chat client, it transforms interactions with AI models into a mystical communion with ancient digital entities.

The project comes in two flavors:
- **WhisperToTheBeastGUI.java**: A full-featured graphical interface with animations and visual theming
- **WhisperToTheBeastCLI.java**: A command-line version for terminal enthusiasts and headless environments

## Repository

The official repository is available at:
https://github.com/penguinlalo/whisper-to-the-beasts.git

## Features

- **Horror-Themed Experience**: Dark themed interface with blood-red accents in GUI mode and stylized text in CLI mode
- **Ollama Integration**: Connect to any LLM available through your local Ollama installation
- **Dramatic Effects**: GUI includes splash screen, loading animations, and typewriter-style text effects
- **Beast Personas**: Each LLM is assigned a unique eldritch entity name
- **Chat History**: Maintains conversation logs with your chosen entity
- **Configurable**: Choose your preferred model, customize the experience
- **Multiple Interfaces**: Run as a GUI application or use the CLI version in a terminal

## Requirements

- Java 8 or higher
- Ollama installed and running (at http://localhost:11434)
- At least one language model loaded in Ollama
- Visual Studio Code (for easy running) or any Java IDE

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/penguinlalo/whisper-to-the-beasts.git
cd whisper-to-the-beasts
```

### Running in Visual Studio Code

1. Open the project folder in Visual Studio Code
2. Make sure you have the "Extension Pack for Java" installed
3. Open either `WhisperToTheBeastGUI.java` or `WhisperToTheBeastCLI.java`
4. Click the "Run" button (or select "Run Without Debugging" from the Run menu)
5. Choose the appropriate Java environment if prompted

### Manual Compilation and Running

If you prefer to compile and run manually:

```bash
# Compile
javac WhisperToTheBeastGUI.java
javac WhisperToTheBeastCLI.java

# Run GUI version
java WhisperToTheBeastGUI

# Run CLI version
java WhisperToTheBeastCLI
```

## Usage

### GUI Version

1. **Start Ollama**: Ensure Ollama is running on your system before launching the application
2. **Launch the Application**: Start the app and wait for the splash screen to complete
3. **Select a Beast**: Choose from the available models detected from your Ollama installation
4. **Introduce Yourself**: Enter your name in the "Reveal Thy Name" field
5. **Ask a Question**: Type your initial question in the "Eldritch Knowledge" field
6. **Summon the Beast**: Click the "Summon the Beast" button to establish a connection
7. **Continue the Conversation**: Use the input field at the bottom to continue your dialogue

### CLI Version

1. **Start Ollama**: Ensure Ollama is running on your system
2. **Launch the Application**: Start the CLI version in your terminal
3. **Follow the Prompts**: The application will guide you through selecting a model, entering your name, and asking questions
4. **Continue the Conversation**: Type your messages when prompted

## Customization

### Adding New Beast Names

Edit the `BEAST_NAMES` array in either class to add more eldritch entity names:

```java
private static final String[] BEAST_NAMES = {
    "Balthazar", "Rookmikeen", "Artemis", "Zephyrus", "Nyarla", "Thothis", 
    // Add your custom names here
};
```

### Modifying the Color Scheme (GUI Version)

The color scheme is defined in the `AnsiColors` class. Modify these values to create your own dark theme:

```java
class AnsiColors {
    // RGB values for GUI
    public static final Color RESET_COLOR = new Color(200, 200, 200);
    public static final Color GREEN_COLOR = new Color(88, 10, 10);    // Dark red
    // Modify other colors as desired
}
```

## How It Works

The application:

1. Connects to Ollama's API to fetch available models
2. Assigns each model a unique "Beast" persona
3. Wraps user queries in a context that instructs the model to respond in the character of the assigned Beast
4. Formats responses with special styling (and typewriter effects in the GUI version)
5. Maintains the theme throughout the interaction

## Troubleshooting

- **No Models Found**: Ensure Ollama is running at http://localhost:11434 and you have at least one model installed
- **Connection Issues**: Check your network settings and that Ollama's API is accessible
- **Display Problems**: The GUI uses custom UI elements which may not display correctly on all platforms. If you encounter issues, try the CLI version

## Contributing

Contributions are welcome! Feel free to:

- Add new Beast personas
- Enhance the UI theming
- Improve the Ollama API integration
- Add new features like saving chat logs

## License

This project is open source and available under the MIT License.

## Acknowledgements

- Uses the Ollama API for accessing local language models
- Inspired by cosmic horror and eldritch themes
- Built with Java Swing (GUI) and pure Java (CLI) for cross-platform compatibility

---

*"The oldest and strongest emotion of mankind is fear, and the oldest and strongest kind of fear is fear of the unknown." — H.P. Lovecraft*