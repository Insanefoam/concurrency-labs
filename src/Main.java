import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static Pattern pattern = Pattern.compile("(.*?)(class|interface)(.*?)(extends|implements)(\\s\\w+)(.*?)");

    private static Map<String, List<String>> classToHisExtended = new HashMap<>();


    public static void main(String[] args) throws Exception {
        ExecutorService threadsService = Executors.newFixedThreadPool(4);

        ArrayList<File> files = new ArrayList<>();
        Files.walk(Paths.get("./spring"))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(".java"))
                .forEach(files::add);

        final int sizeQueue = files.size();
        final BlockingQueue<File> tasks = new ArrayBlockingQueue<>(sizeQueue);
        final BlockingQueue<Pair<String, String>> results = new ArrayBlockingQueue<>(sizeQueue);


        int tasksCount = files.size();
        for (int i = 0; i < tasksCount; i++) {
            threadsService.execute(new ClassAnalyzerThread(tasks, results));
        }

        files.forEach((file) -> {
            try {
                tasks.put(file);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (tasks.size() == 0) {
                    threadsService.shutdown();

                    results.forEach((pair) -> saveInMap(pair.getPairKey(), pair.getPairValue()));
                    classToHisExtended
                            .entrySet()
                            .stream()
                            .filter((entry) -> entry.getValue().size() >= 1)
                            .forEach(System.out::println);
                    timer.purge();
                    timer.cancel();
                    System.exit(0);
                }
            }
        }, 100, 5000);
    }

    private static void saveInMap(String nameClass, String nameExtendsClass) {
        if (classToHisExtended.containsKey(nameExtendsClass)) {
            classToHisExtended.get(nameExtendsClass).add(nameClass);
        } else {
            classToHisExtended.putIfAbsent(nameExtendsClass,
                    new ArrayList<>(Arrays.asList(nameClass)));
        }
    }

    private static class ClassAnalyzerThread implements Runnable {
        private BlockingQueue<File> tasks;
        private BlockingQueue<Pair<String, String>> results;

        public ClassAnalyzerThread(BlockingQueue<File> tasks, BlockingQueue<Pair<String, String>> results) {
            this.tasks = tasks;
            this.results = results;
        }

        @Override
        public void run() {
            try {
                File file = tasks.take();
//                System.out.println("Tasks queue size: " + tasks.size());
//                System.out.println("Run task for file: " + file.getName());
                Matcher matcher = fileToMather().apply(file);
                while (matcher.find()) {
                    String child = matcher.group(3).trim();
                    String parent = matcher.group(5).trim();
                    results.put(new Pair<>(child, parent));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static Function<File, Matcher> fileToMather() {
        return new Function<File, Matcher>() {
            @Override
            public Matcher apply(File file) {
                try {
                    return pattern.matcher(new String(Files.readAllBytes(file.toPath())));
                } catch (IOException exception) {
                    return pattern.matcher("");
                }
            }

            @Override
            public <V> Function<V, Matcher> compose(Function<? super V, ? extends File> function) {
                return null;
            }

            @Override
            public <V> Function<File, V> andThen(Function<? super Matcher, ? extends V> function) {
                return null;
            }
        };
    }
}
