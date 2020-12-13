public class Pair<T, K> {
    private T pairKey;
    private K pairValue;

    public Pair(T pairKey, K pairValue) {
        this.pairKey = pairKey;
        this.pairValue = pairValue;
    }

    public K getPairValue() {
        return pairValue;
    }

    public T getPairKey() {
        return pairKey;
    }

    public void setPairKey(T pairKey) {
        this.pairKey = pairKey;
    }

    public void setPairValue(K pairValue) {
        this.pairValue = pairValue;
    }
}
