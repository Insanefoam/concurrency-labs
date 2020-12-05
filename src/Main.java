import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {


    public static void main(String[] args) {
        ArrayList<String> classesSourceCodes = getClassesSourceCodeInFolder("./spring");

        HashMap<String, String> classesHierarchy = new HashMap<>();

        ExecutorService threads = Executors.newFixedThreadPool(100);

        try {
            List<Future<String[]>> futures = threads.invokeAll(classesSourceCodes
                    .stream()
                    .map(ProcessCodeTask::new)
                    .collect(Collectors.toList()));

            futures.forEach(future -> {
                try {
                    String[] mainAndParentClasses = future.get();
                    String defaultParentValue = classesHierarchy.getOrDefault(mainAndParentClasses[1],
                            "");
                    if (defaultParentValue.length() == 0) {
                        classesHierarchy.put(mainAndParentClasses[1], mainAndParentClasses[0] + " ");
                    } else {
                        classesHierarchy.put(mainAndParentClasses[1],
                                defaultParentValue.concat(" " + mainAndParentClasses[0]));
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        classesHierarchy.forEach((parent, subclasses) -> {
            System.out.println("Parent class: " + parent + "\nSubclasses: " + subclasses);
            System.out.println("**********************");
        });
        threads.shutdown();
        System.out.println(Thread.activeCount());
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

class ProcessCodeTask implements Callable<String[]> {
    String sourceCode;

    ProcessCodeTask(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    @Override
    public String[] call() {
        return getMainAndParentClassNamesFromText(this.sourceCode);
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
}