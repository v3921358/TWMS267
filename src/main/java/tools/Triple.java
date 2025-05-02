package tools;

import java.io.Serializable;

public class Triple<E, F, G>
        implements Serializable {
    private static final long serialVersionUID = 9179541993413739999L;
    public E left;
    public F mid;
    public G right;

    public Triple(E left, F mid, G right) {
        /* 13 */
        this.left = left;
        /* 14 */
        this.mid = mid;
        /* 15 */
        this.right = right;
    }

    public E getLeft() {
        /* 19 */
        return this.left;
    }

    public F getMid() {
        /* 23 */
        return this.mid;
    }

    public G getRight() {
        /* 27 */
        return this.right;
    }

    public String toString() {
        /* 32 */
        return this.left.toString() + ":" + this.mid.toString() + ":" + this.right.toString();
    }

    public int hashCode() {
        /* 37 */
        int prime = 31;
        /* 38 */
        int result = 1;
        /* 39 */
        result = 31 * result + ((this.left == null) ? 0 : this.left.hashCode());
        /* 40 */
        result = 31 * result + ((this.mid == null) ? 0 : this.mid.hashCode());
        /* 41 */
        result = 31 * result + ((this.right == null) ? 0 : this.right.hashCode());
        /* 42 */
        return result;
    }

    public boolean equals(Object obj) {
        /* 47 */
        if (this == obj) {
            /* 48 */
            return true;
        }
        /* 50 */
        if (obj == null) {
            /* 51 */
            return false;
        }
        /* 53 */
        if (getClass() != obj.getClass()) {
            /* 54 */
            return false;
        }
        /* 56 */
        Triple other = (Triple) obj;
        /* 57 */
        if (this.left == null) {
            /* 58 */
            if (other.left != null) {
                /* 59 */
                return false;
            }
            /* 61 */
        } else if (!this.left.equals(other.left)) {
            /* 62 */
            return false;
        }
        /* 64 */
        if (this.mid == null) {
            /* 65 */
            if (other.mid != null) {
                /* 66 */
                return false;
            }
            /* 68 */
        } else if (!this.mid.equals(other.mid)) {
            /* 69 */
            return false;
        }
        /* 71 */
        /* 76 */
        if (this.right == null) {
            /* 72 */
            /* 73 */
            return other.right == null;
            /* 75 */
        } else return this.right.equals(other.right);
        /* 78 */
    }
}
