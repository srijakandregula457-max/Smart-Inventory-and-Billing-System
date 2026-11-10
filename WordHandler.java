
import java.io.*;

public class WordHandler {

    public static void save(String data) throws IOException {

        FileWriter writer = new FileWriter("MyDocument.txt");

        writer.write(data);

        writer.close();
    }

    public static String read() throws IOException {

        BufferedReader reader =
            new BufferedReader(
                new FileReader("MyDocument.txt")
            );

        StringBuilder content = new StringBuilder();

        String line;

        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }

        reader.close();

        return content.toString();
    }
}
