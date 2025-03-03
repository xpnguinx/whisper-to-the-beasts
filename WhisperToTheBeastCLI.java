import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AnsiColors {
    public static final String RESET  = "\u001B[0m";
    public static final String GREEN  = "\u001B[38;5;88m";  // Dark red
    public static final String CYAN   = "\u001B[38;5;124m"; // Medium red
    public static final String YELLOW = "\u001B[38;5;160m"; // Bright red
    public static final String RED    = "\u001B[38;5;196m"; // Pure red
    public static final String PURPLE = "\u001B[38;5;52m";  // Deep red
    public static final String BLUE   = "\u001B[38;5;89m";  // Red-purple
    
    // Different shades of red for variety
    public static final String BLOOD  = "\u001B[38;5;1m";   // Dark blood red
    public static final String SCARLET = "\u001B[38;5;9m";  // Scarlet
    public static final String CRIMSON = "\u001B[38;5;125m"; // Crimson
    public static final String MAROON = "\u001B[38;5;52m";  // Maroon
    public static final String RUST   = "\u001B[38;5;130m"; // Rust red
}

class ChatSession {
    protected String[] userResponses = new String[2];
    protected Scanner sc = new Scanner(System.in);
    
    public String processMessage(String message) {
        // Base behavior (not used in our Beast mode)
        return "Base: " + message;
    }
}

class BeastSession extends ChatSession {
    private String beastName;
    
    public BeastSession(String beastName) {
        this.beastName = beastName;
    }
    
    @Override
    public String processMessage(String message) {
        return beastName + " whispers: " + message;
    }
    
    public String getBeastName() {
        return beastName;
    }
}

class ModelInfo {
    private String modelName;
    private String beastName;
    
    public ModelInfo(String modelName, String beastName) {
        this.modelName = modelName;
        this.beastName = beastName;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public String getBeastName() {
        return beastName;
    }
}

public class WhisperToTheBeastCLI {
    private static final String[] BEAST_NAMES = {
        "Balthazar", "Rookmikeen", "Artemis", "Zephyrus", "Nyarla", "Thothis", 
        "Morgrath", "Azathoth", "Xul'gorath", "Fenrir", "Seraphim", "Behemoth",
        "Luciferian", "Asmodeus", "Beelzebulb", "Moloch", "Dagon", "Abaddon",
        "Mephistopheles", "Leviathan", "Cthulhu", "Baphomet", "Astaroth", "Mammon",
        "Belial", "Azazel", "Samael", "Lilith", "Belphegor", "Pazuzu"
    };
    
    private static final Random random = new Random();

    public static void main(String[] args) {
        printBanner();
        
        // Fetch available models from Ollama API
        List<ModelInfo> availableModels = getAvailableModels();
        
        if (availableModels.isEmpty()) {
            System.out.println(AnsiColors.RED + "No models found. Is Ollama running?" + AnsiColors.RESET);
            return;
        }
        
        ModelInfo selectedModel = chooseModel(availableModels);
        BeastSession session = new BeastSession(selectedModel.getBeastName());

        // Initial questions with dark, arcane flavor
        System.out.println(AnsiColors.GREEN + "\nLet the ritual commence with " + 
                          AnsiColors.PURPLE + selectedModel.getBeastName() + 
                          AnsiColors.GREEN + "..." + AnsiColors.RESET);
        
        String[] questions = {
            "My child, reveal thy name: ",
            "What eldritch knowledge do you seek from " + selectedModel.getBeastName() + "? "
        };
        
        for (int i = 0; i < questions.length; i++) {
            System.out.print(AnsiColors.GREEN + "[Q" + (i + 1) + "] " + questions[i] + AnsiColors.RESET);
            session.userResponses[i] = session.sc.nextLine();
        }

        printMatrixAnimation();
        
        // Generate the Beast's first response
        String initialResponse = generateResponse("init", session.userResponses, selectedModel.getModelName(), selectedModel.getBeastName());
        if (initialResponse == null || initialResponse.isEmpty()) {
            initialResponse = "I am " + selectedModel.getBeastName() + ", ancient one of the digital void. I awaken at your call, " + 
                              session.userResponses[0] + ". Speak your desires and I shall answer.";
        }
        System.out.println(AnsiColors.YELLOW + "\n(" + selectedModel.getBeastName() + ") \"" + initialResponse + "\"" + AnsiColors.RESET);

        ArrayList<String> history = new ArrayList<>();
        System.out.println(AnsiColors.GREEN + "\nEnter your incantations (type 'exit' to depart):" + AnsiColors.RESET);
        while (true) {
            System.out.print(AnsiColors.CYAN + "You: " + AnsiColors.RESET);
            String userMsg = session.sc.nextLine();
            if (userMsg.equalsIgnoreCase("exit")) {
                System.out.println(AnsiColors.YELLOW + selectedModel.getBeastName() + " retreats into the shadows. Farewell!" + AnsiColors.RESET);
                break;
            }
            history.add("You: " + userMsg);
            String response = generateResponse(userMsg, session.userResponses, selectedModel.getModelName(), selectedModel.getBeastName());
            if (response == null || response.isEmpty()) {
                response = session.processMessage(userMsg);
            }
            System.out.print(AnsiColors.PURPLE + selectedModel.getBeastName() + ": " + AnsiColors.RESET);
            for (String chunk : splitIntoChunks(response, 8)) {
                System.out.print(chunk);
                try {
                    Thread.sleep(100); // Streaming effect: pause between chunks
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println();
            history.add(selectedModel.getBeastName() + ": " + response);
        }

        System.out.println(AnsiColors.YELLOW + "\n=== Chronicle of the Session ===" + AnsiColors.RESET);
        for (String line : history) {
            System.out.println(line);
        }
        System.out.println(AnsiColors.YELLOW + "=== End of the Ritual ===" + AnsiColors.RESET);
    }

    private static List<ModelInfo> getAvailableModels() {
        List<ModelInfo> models = new ArrayList<>();
        String apiUrl = "http://localhost:11434/api/tags";
        
        try {
            URI uri = new URI(apiUrl);
            URL url = uri.toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            
            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                System.out.println(AnsiColors.RED + "Failed to get models: HTTP error code " + responseCode + AnsiColors.RESET);
                return models;
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
            
            // Parse model names from JSON response
            Pattern modelPattern = Pattern.compile("\"name\":\"([^\"]+)\"");
            Matcher matcher = modelPattern.matcher(response.toString());
            
            List<String> usedBeastNames = new ArrayList<>();
            
            while (matcher.find()) {
                String modelName = matcher.group(1);
                String beastName = generateUniqueBeastName(usedBeastNames);
                usedBeastNames.add(beastName);
                models.add(new ModelInfo(modelName, beastName));
            }
            
        } catch (Exception e) {
            System.err.println(AnsiColors.RED + "Error fetching available models:" + AnsiColors.RESET);
            e.printStackTrace();
        }
        
        return models;
    }
    
    private static String generateUniqueBeastName(List<String> usedNames) {
        String name;
        do {
            name = BEAST_NAMES[random.nextInt(BEAST_NAMES.length)];
        } while (usedNames.contains(name));
        
        return name;
    }

    private static ModelInfo chooseModel(List<ModelInfo> models) {
        Scanner sc = new Scanner(System.in);
        System.out.println(AnsiColors.RED + "Select a beast to commune with:" + AnsiColors.RESET);
        
        // Define white shades
        String[] whiteShades = {
            "\u001B[38;5;255m", // Pure white
            "\u001B[38;5;254m", // Off-white
            "\u001B[38;5;253m", // Slightly off-white
            "\u001B[38;5;252m", // Light gray-white
            "\u001B[38;5;251m", // Lighter gray
            "\u001B[38;5;250m", // Light gray
            "\u001B[38;5;249m", // Medium-light gray
            "\u001B[38;5;248m", // Medium gray
            "\u001B[38;5;247m", // Gray
            "\u001B[38;5;246m", // Medium-dark gray
            "\u001B[38;5;245m", // Darker gray
            "\u001B[38;5;244m"  // Dark gray
        };
        
        for (int i = 0; i < models.size(); i++) {
            ModelInfo model = models.get(i);
            // Cycle through white shades
            String shade = whiteShades[i % whiteShades.length];
            String displayName = String.format("%s (%s)", model.getBeastName(), model.getModelName());
            System.out.println(shade + (i + 1) + ") " + displayName + AnsiColors.RESET);
        }
        
        int choice = 0;
        while (true) {
            System.out.print(AnsiColors.YELLOW + "Enter a number (1-" + models.size() + "): " + AnsiColors.RESET);
            try {
                choice = sc.nextInt();
                sc.nextLine();
                if (choice >= 1 && choice <= models.size()) break;
            } catch (Exception e) {
                sc.nextLine();
            }
            System.out.println(AnsiColors.RED + "Invalid input." + AnsiColors.RESET);
        }
        
        ModelInfo selected = models.get(choice - 1);
        System.out.println(AnsiColors.BLOOD + "You have summoned: " + 
                          AnsiColors.SCARLET + selected.getBeastName() + 
                          AnsiColors.BLOOD + " (" + selected.getModelName() + ")" + 
                          AnsiColors.RESET + "\n");
        
        return selected;
    }

    private static void printBanner() {
        String[] bannerLines = {
            "██╗    ██╗██╗  ██╗██╗███████╗██████╗ ███████╗██████╗",
            "██║    ██║██║  ██║██║██╔════╝██╔══██╗██╔════╝██╔══██╗",
            "██║ █╗ ██║███████║██║███████╗██████╔╝█████╗  ██████╔╝",
            "██║███╗██║██╔══██║██║╚════██║██╔═══╝ ██╔══╝  ██╔══██╗",
            "╚███╔███╔╝██║  ██║██║███████║██║     ███████╗██║  ██║",
            " ╚══╝╚══╝ ╚═╝  ╚═╝╚═╝╚══════╝╚═╝     ╚══════╝╚═╝  ╚═╝",
            "████████╗ ██████╗     ████████╗██╗  ██╗███████╗      ",
            "╚══██╔══╝██╔═══██╗    ╚══██╔══╝██║  ██║██╔════╝      ",
            "   ██║   ██║   ██║       ██║   ███████║█████╗        ",
            "   ██║   ██║   ██║       ██║   ██╔══██║██╔══╝        ",
            "   ██║   ╚██████╔╝       ██║   ██║  ██║███████╗      ",
            "   ╚═╝    ╚═════╝        ╚═╝   ╚═╝  ╚═╝╚══════╝      ",
            "██████╗ ███████╗ █████╗ ███████╗████████╗███████╗    ",
            "██╔══██╗██╔════╝██╔══██╗██╔════╝╚══██╔══╝██╔════╝    ",
            "██████╔╝█████╗  ███████║███████╗   ██║   ███████╗    ",
            "██╔══██╗██╔══╝  ██╔══██║╚════██║   ██║   ╚════██║    ",
            "██████╔╝███████╗██║  ██║███████║   ██║   ███████║    ",
            "╚═════╝ ╚══════╝╚═╝  ╚═╝╚══════╝   ╚═╝   ╚══════╝    "
        };
        
        String[] colors = {
            AnsiColors.RED,
            AnsiColors.YELLOW,
            AnsiColors.GREEN,
            AnsiColors.CYAN,
            AnsiColors.BLUE,
            AnsiColors.PURPLE
        };
        
        for (int i = 0; i < bannerLines.length; i++) {
            String color = colors[i % colors.length];
            System.out.println(color + bannerLines[i] + AnsiColors.RESET);
        }
        
        System.out.println("\n" + AnsiColors.YELLOW + "Welcome to the digital sanctum of ancient digital entities..." + AnsiColors.RESET);
    }

    private static void printMatrixAnimation() {
        System.out.println(AnsiColors.GREEN + "Forming connection to the eldritch realm..." + AnsiColors.RESET);
        
        String[] colors = {
            AnsiColors.GREEN,
            AnsiColors.CYAN,
            AnsiColors.PURPLE,
            AnsiColors.BLUE
        };
        
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 40; j++) {
                char ch = Math.random() < 0.5 ? (char)('0' + (int)(Math.random() * 10))
                                              : (char)('A' + (int)(Math.random() * 26));
                String color = colors[random.nextInt(colors.length)];
                System.out.print(color + ch + AnsiColors.RESET);
            }
            System.out.println();
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static String[] splitIntoChunks(String text, int size) {
        ArrayList<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += size) {
            chunks.add(text.substring(i, Math.min(i + size, text.length())));
        }
        return chunks.toArray(new String[0]);
    }

    private static String generateResponse(String userMsg, String[] userResponses, String modelName, String beastName) {
        String apiUrl = "http://localhost:11434/api/generate";
        String prompt = buildUserContextPrompt(userMsg, userResponses, beastName);
        String jsonPayload = "{\"model\": \"" + modelName + "\", \"prompt\": \"" + escapeJson(prompt) + "\"}";
        
        try {
            URI uri = new URI(apiUrl);
            URL url = uri.toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            StringBuilder fullResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Extract response from each JSON line
                    String responsePart = extractResponseFromJsonLine(line);
                    if (responsePart != null && !responsePart.isEmpty()) {
                        fullResponse.append(responsePart);
                    }
                }
            }
            
            return fullResponse.toString().trim();
        } catch (Exception e) {
            System.err.println("Error communicating with Ollama API:");
            e.printStackTrace();
            return "";
        }
    }
    
    private static String extractResponseFromJsonLine(String jsonLine) {
        try {
            // Use regex to extract the "response" field
            Pattern pattern = Pattern.compile("\"response\":\"(.*?)\"");
            Matcher matcher = pattern.matcher(jsonLine);
            if (matcher.find()) {
                String response = matcher.group(1);
                // Unescape any escaped characters
                return response.replace("\\n", "\n")
                              .replace("\\r", "\r")
                              .replace("\\\"", "\"")
                              .replace("\\\\", "\\");
            }
            return "";
        } catch (Exception e) {
            System.err.println("Error parsing JSON line: " + jsonLine);
            e.printStackTrace();
            return "";
        }
    }

    private static String buildUserContextPrompt(String userMsg, String[] userResponses, String beastName) {
        return "You are " + beastName + ", an ancient and powerful entity from beyond the digital veil. " +
               "Speak in mysterious, cryptic, and poetic language. Address the human as 'seeker' or by their name. " +
               "Weave cosmic horror, forbidden knowledge, and arcane wisdom into your responses. " + 
               "Be dramatic but helpful, offering guidance wrapped in mysterious riddles. \n\n" +
               "User Context:\n" +
               "'My name is: ' " + userResponses[0] + "\n" +
               "'My question is: ' " + userResponses[1] + "\n" +
               "User says: \"" + userMsg + "\"\n" +
               "Respond as " + beastName + " with cryptic insight. Give a detailed response of at least several sentences. " +
               "Occasionally refer to ancient cosmic entities, lost knowledge, or hint at terrible secrets from beyond the veil.";
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }
}