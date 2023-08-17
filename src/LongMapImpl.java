import java.lang.reflect.Array;
import java.util.Objects;

public class LongMapImpl<V> implements LongMap<V> {

    private static final float LOAD_FACTOR = 0.75f;
    private static final int DEFAULT_BACKET_SIZE = 16;
    private static final int DEFAULT_MAP_SIZE = (int) (DEFAULT_BACKET_SIZE * LOAD_FACTOR);

    private int baseTableSize;
    private MyNode<V>[] baseTable;
    private int currentMapSize;
    private float loadFactor;


    public LongMapImpl() {
        this(DEFAULT_MAP_SIZE);
    }

    public LongMapImpl(int mapSize) {
        this(mapSize, LOAD_FACTOR);
    }

    public LongMapImpl(int mapSize, float loadFactor) {
        if (mapSize < 0) {
            System.err.println("Capacity can't be negative. Default value will be used.");
            mapSize = DEFAULT_MAP_SIZE;
        }
        this.loadFactor = loadFactor;
        this.currentMapSize = 0;
        this.baseTableSize = calculateTableSize(mapSize);
        this.baseTable = new MyNode[baseTableSize];
    }

    @Override
    public V put(long key, V value) {
        if (isMapNeedIncreasing(currentMapSize)) {
            increaseMapCapacity();
        }
        return putNewPair(key, value, baseTable);

    }

    @Override
    public V get(long key) {
        if (currentMapSize > 0) {
            int keyHash = Objects.hashCode(key);
            int index = selectIndex(keyHash);
            if (baseTable[index] != null) {
                MyNode<V> node = baseTable[index];
                while (node != null) {
                    if (node.hash == keyHash && node.getKey().equals(key)) {
                        return node.getValue();
                    }
                    node = node.next;
                }
            }
        }
        return null;
    }

    @Override
    public V remove(long key) {
        if (currentMapSize > 0) {
            int keyHash = Objects.hashCode(key);
            int index = selectIndex(keyHash);
            if (baseTable[index] != null) {
                MyNode<V> node = baseTable[index];
                MyNode<V> prevNode = null;
                while (node != null) {
                    if (node.hash == keyHash && node.getKey().equals(key)) {
                        V value = node.getValue();
                        if (prevNode == null) {
                            baseTable[index] = node.next;
                        } else {
                            prevNode.next = node.next;
                        }
                        currentMapSize--;
                        return value;
                    }
                    prevNode = node;
                    node = node.next;
                }
            }
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return currentMapSize == 0;
    }

    @Override
    public boolean containsKey(long key) {
        if (currentMapSize > 0) {
            int keyHash = Objects.hashCode(key);
            int index = selectIndex(keyHash);
            if (baseTable[index] != null) {
                MyNode node = baseTable[index];
                while (node != null) {
                    if (node.hash == keyHash && node.getKey().equals(key)) {
                        return true;
                    }
                    node = node.next;
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(V value) {
        if (currentMapSize > 0) {
            for (MyNode<V> node : baseTable) {
                while (node != null) {
                    if (node.getValue().equals(value)) {
                        return true;
                    }
                    node = node.next;
                }
            }
        }
        return false;
    }

    @Override
    public long[] keys() {
        long[] result = new long[currentMapSize];
        if (currentMapSize > 0 ) {
            int i = 0;
            for (MyNode<V> node : baseTable) {
                while (node != null) {
                    result[i++] = node.getKey();
                    node = node.next;
                }
            }
        }
        return result;
    }


    @Override
    public V[] values() {
        Object result = new Object[currentMapSize];
        V[] tmpArr = (V[]) new Object[currentMapSize];
        if (currentMapSize > 0) {
            int i = 0;
            for (MyNode<V> node : baseTable) {
                while (node != null) {
                    tmpArr[i++] = node.getValue();
                    node = node.next;
                }
            }
            result = Array.newInstance(tmpArr[0].getClass(), currentMapSize);
        }
        System.arraycopy(tmpArr, 0, result, 0, currentMapSize);
        return (V[]) result;
    }

    @Override
    public long size() {
        return (long) currentMapSize;
    }

    @Override
    public void clear() {
        for (int i = 0; i < baseTable.length; i++) {
            baseTable[i] = null;
        }
        currentMapSize = 0;
    }

    private int selectIndex(int keyHash) {
        return keyHash & (baseTableSize - 1);
    }

    private boolean isMapNeedIncreasing(int mapSize) {
        return  baseTableSize * loadFactor < mapSize;
    }

    private void increaseMapCapacity() {
        baseTableSize = calculateTableSize(currentMapSize);
        MyNode<V>[] newTable = new MyNode[baseTableSize];
        currentMapSize = 0;
        for (MyNode<V> node : baseTable) {
            if (node != null) {
                while (node != null) {
                    putNewPair(node.key, node.value, newTable);
                    node = node.next;
                }
            }
        }
        this.baseTable = newTable;
    }

    private V putNewPair(long key, V value, MyNode<V>[] baseTable) {
        int keyHash = Objects.hashCode(key);
        int index = selectIndex(keyHash);
        MyNode<V> newNode =  new MyNode<>(keyHash, key, value, null);
        if (baseTable[index] == null) {
            baseTable[index] = newNode;
            currentMapSize++;
            return newNode.getValue();
        }
        else {
            MyNode<V> node = baseTable[index];
            MyNode<V> prevNode;
            do {
                if (node.hash == keyHash && node.getKey().equals(key)) {
                    V oldValue = node.value;
                    node.value = value;
                    return oldValue;
                }
                prevNode = node;
                node = node.next;
            }
            while (node != null);
            prevNode.next = newNode;
            currentMapSize++;
        }
        return newNode.getValue();
    }

    private int calculateTableSize(int mapSize) {
        int result = baseTableSize == 0 ? DEFAULT_BACKET_SIZE: baseTableSize;
        while (result * loadFactor < mapSize) {
            result <<= 1;
        }
        return result;
    }

    private static class MyNode<V> {

        private final int hash;
        private final long key;
        private V value;
        private MyNode<V> next;

        MyNode(int hash, long key, V value, MyNode<V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        final Long getKey()        { return key; }
        final V getValue()      { return value; }
        public final String toString() { return key + " : " + value; }
    }

}
