import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {


    public static void main(String[] args) {
        ArrayList<String> classesSourceCodes = getClassesSourceCodeInFolder("./spring");


        for (String sourceCode : classesSourceCodes) {
            Thread sourceCodeThread = new Thread(() -> {
                //String[]: [mainClass, parentClass? = ""]
                String[] mainAndParentClasses = getMainAndParentClassNamesFromText(sourceCode);

                System.out.println("***************************************");
                System.out.println("Parent class: " + mainAndParentClasses[0]);
                System.out.println("Subclasses: " + mainAndParentClasses[1]);

                try {
                    BufferedWriter bufferedWriter = new BufferedWriter(
                            new FileWriter("spring_classes_output.txt", true));

                    try {
                        bufferedWriter.write("***************************************");
                        bufferedWriter.newLine();
                        bufferedWriter.write("Parent class: " + mainAndParentClasses[0]);
                        bufferedWriter.newLine();
                        bufferedWriter.write("Subclasses: " + mainAndParentClasses[1]);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            sourceCodeThread.start();
        }
    }


    public static String[] getMainAndParentClassNamesFromText(String text) {
        String mainClassRegex = "(?<=(private|public|protected)\\s(class|interface)\\s)\\w*";
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

        File classesFolder = new File(folder);
        for (File file : Objects.requireNonNull(classesFolder.listFiles())) {
            if (file.isDirectory()) {
                String directoryPath = file.getAbsolutePath()
                        .replace("/Users/daniil/Documents/Projects/concurrency-labs/", "");
                classesSourceCodes.addAll(getClassesSourceCodeInFolder(directoryPath));
            }

            if (file.getName().contains(".java")) {
                new Thread(() -> {
                    try {
                        Scanner scanner = new Scanner(file);
                        String currentClassCode = "";
                        while (scanner.hasNext()) {
                            currentClassCode = currentClassCode.concat(scanner.nextLine());
                        }
                        classesSourceCodes.add(currentClassCode);
                    } catch (FileNotFoundException e) {
                        System.out.println("Oops, non-file object...");
                    }
                }).start();
            }
        }

        return classesSourceCodes;
    }
}