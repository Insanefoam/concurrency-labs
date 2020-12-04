import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {


    public static void main(String[] args) {
        ArrayList<String> classesSourceCodes = getClassesSourceCodeInFolder("./spring");

        ReentrantLock reentrantLock = new ReentrantLock();
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch doneSignal = new CountDownLatch(classesSourceCodes.size());
        HashMap<String, String> classesHierarchy = new HashMap<>();

        for (String sourceCode : classesSourceCodes) {
            new Thread(new CodeProcessor(
                    sourceCode,
                    reentrantLock,
                    classesHierarchy,
                    doneSignal
            )).start();
        }

        try {
            startSignal.countDown();
            doneSignal.await();
            classesHierarchy.forEach((parent, subclasses) -> {
                System.out.println(parent + ":" + subclasses);
                System.out.println("**********************");
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

class CodeProcessor implements Runnable {
    String sourceCode;
    ReentrantLock reentrantLock;
    HashMap<String, String> classesHierarchy;
    CountDownLatch doneSignal;

    CodeProcessor(String sourceCode,
                  ReentrantLock reentrantLock,
                  HashMap<String, String> classesHierarchy,
                  CountDownLatch doneSignal
    ) {
        this.sourceCode = sourceCode;
        this.reentrantLock = reentrantLock;
        this.classesHierarchy = classesHierarchy;
        this.doneSignal = doneSignal;
    }

    @Override
    public void run() {
        String[] mainAndParentClasses = getMainAndParentClassNamesFromText(sourceCode);

        String defaultParentValue = classesHierarchy.getOrDefault(mainAndParentClasses[1],
                "");
        if (defaultParentValue.length() == 0) {
            classesHierarchy.put(mainAndParentClasses[1], mainAndParentClasses[0] + " ");
        } else {
            classesHierarchy.put(mainAndParentClasses[1],
                    defaultParentValue.concat(" " + mainAndParentClasses[0]));
        }
        doneSignal.countDown();
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