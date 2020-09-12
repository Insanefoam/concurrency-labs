import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        String str = readFromFile("text.txt");

        List<String> words = Stream.of(str)
                .map(String::trim)
                .map(w -> w.replaceAll("[.,?-]", ""))
                .map(w -> w.split("\\s+"))
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());

        System.out.println(words);

        Map<String, Integer> wordsFrequency = words.stream()
                .collect(Collectors.toMap(w -> w.toLowerCase(), w -> 1, (prev, next) -> prev + next));

        System.out.println(wordsFrequency);
    }

    public static String readFromFile(String filename) {
        try {
            FileReader fr = new FileReader(filename);
            Scanner scanner = new Scanner(fr);
            return scanner.nextLine();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }
}
