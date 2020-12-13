import java.util.ArrayList;
import java.util.List;

public class ExtendsAnsClasses {
    private final List<String> nameClassArr = new ArrayList<>();

    private final List<String> nameExtendsClassArr = new ArrayList<>();

    public void addPair(final String nameClass, final String nameExtendsClass) {
        nameClassArr.add(nameClass);
        nameExtendsClassArr.add(nameExtendsClass);
    }

    public List<Pair<String, String>> getPairs() {
        final List<Pair<String, String>> resp = new ArrayList();

        for (int i = 0; i < nameClassArr.size(); i++) {
            resp.add(new Pair<>(nameClassArr.get(i), nameExtendsClassArr.get(i)));
        }

        return resp;
    }
}
