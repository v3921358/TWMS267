package tools;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ConcurrentEnumMap<K extends Enum<K>, V>
        extends EnumMap<K, V> implements Serializable {
    private static final long serialVersionUID = 11920818021L;
    /* 14 */ private final ReentrantReadWriteLock reentlock = new ReentrantReadWriteLock();
    /* 15 */ private final Lock rL = this.reentlock.readLock();
    /* 16 */ private final Lock wL = this.reentlock.writeLock();

    public ConcurrentEnumMap(Class<K> keyType) {
        /* 19 */
        super(keyType);
    }

    public void clear() {
        /* 24 */
        this.wL.lock();
        try {
            /* 26 */
            super.clear();
        } finally {
            /* 28 */
            this.wL.unlock();
        }
    }

    public EnumMap<K, V> clone() {
        /* 34 */
        return super.clone();
    }

    public boolean containsKey(Object key) {
        /* 39 */
        this.rL.lock();
        try {
            /* 41 */
            return super.containsKey(key);
        } finally {
            /* 43 */
            this.rL.unlock();
        }
    }

    public boolean containsValue(Object value) {
        /* 49 */
        this.rL.lock();
        try {
            /* 51 */
            return super.containsValue(value);
        } finally {
            /* 53 */
            this.rL.unlock();
        }
    }

    public Set<Entry<K, V>> entrySet() {
        /* 59 */
        this.rL.lock();
        try {
            /* 61 */
            return super.entrySet();
        } finally {
            /* 63 */
            this.rL.unlock();
        }
    }

    public V get(Object key) {
        /* 69 */
        this.rL.lock();
        try {
            /* 71 */
            return super.get(key);
        } finally {
            /* 73 */
            this.rL.unlock();
        }
    }

    public Set<K> keySet() {
        /* 79 */
        this.rL.lock();
        try {
            /* 81 */
            return super.keySet();
        } finally {
            /* 83 */
            this.rL.unlock();
        }
    }

    public V put(K key, V value) {
        /* 89 */
        this.wL.lock();
        try {
            /* 91 */
            return super.put(key, value);
        } finally {
            /* 93 */
            this.wL.unlock();
        }
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        /* 99 */
        this.wL.lock();
        try {
            /* 101 */
            super.putAll(m);
        } finally {
            /* 103 */
            this.wL.unlock();
        }
    }

    public V remove(Object key) {
        /* 109 */
        this.wL.lock();
        try {
            /* 111 */
            return super.remove(key);
        } finally {
            /* 113 */
            this.wL.unlock();
        }
    }

    public int size() {
        /* 119 */
        this.rL.lock();
        try {
            /* 121 */
            return super.size();
        } finally {
            /* 123 */
            this.rL.unlock();
        }
    }

    public Collection<V> values() {
        /* 129 */
        this.rL.lock();
        try {
            /* 131 */
            return super.values();
        } finally {
            /* 133 */
            this.rL.unlock();
        }
    }
}
