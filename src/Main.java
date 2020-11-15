import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {


    public static void main(String[] args) {
        ArrayList<String> classesSourceCodes = getClassesSourceCodeInFolder("./src/classes");

        System.out.println("All classes source codes" + classesSourceCodes);

        //String[]: [mainClass, parentClass? = ""]
        List<String[]> classesNames = classesSourceCodes
                .stream()
                .map(Main::getMainAndParentClassNamesFromText)
                .collect(Collectors.toList());

        Map<String, String> classesHierarchy = new HashMap<>();
        classesNames.forEach(names -> {
            String defaultParentValue = classesHierarchy.getOrDefault(names[1],
                    "");
            if(defaultParentValue.length() == 0) {
                classesHierarchy.put(names[1], names[0]);
                return;
            }

            classesHierarchy.put(names[1], defaultParentValue.concat(names[0]));
        });

        //Map:
        //Key: parent class name
        //Value: subclasses names
        System.out.println(classesHierarchy);
    }


    public static String[] getMainAndParentClassNamesFromText(String text) {
        String mainClassRegex = "(?<=(class|interface)\\s)\\w*";
        String parentClassRegex = "(?<=(extends|implements)\\s)\\w*";

        Pattern mainClassPattern = Pattern.compile((mainClassRegex));
        Pattern parentClassPattern = Pattern.compile((parentClassRegex));

        Matcher mainClassMatcher = mainClassPattern.matcher(text);
        Matcher parentClassMatcher = parentClassPattern.matcher(text);

        String mainClass = "";
        String parentClass = "";

        if (mainClassMatcher.find()) {
            mainClass = text.substring(mainClassMatcher.start(),
                    mainClassMatcher.end());
        }

        if (parentClassMatcher.find()) {
            parentClass = text.substring(parentClassMatcher.start(),
                    parentClassMatcher.end());
        }

        return new String[]{mainClass, parentClass};
    }

    public static ArrayList<String> getClassesSourceCodeInFolder(String folder) {
        ArrayList<String> classesSourceCodes = new ArrayList<>();
        Scanner scanner;

        File classesFolder = new File(folder);
        for (File file : Objects.requireNonNull(classesFolder.listFiles())) {
            try {
                scanner = new Scanner(file);
                String currentClassCode = "";
                while (scanner.hasNext()) {
                    currentClassCode = currentClassCode.concat(scanner.nextLine());
                }
                classesSourceCodes.add(currentClassCode);
            } catch (FileNotFoundException e) {
                System.out.println("Oops, non-file object...");
            }
        }

        return classesSourceCodes;
    }
}
