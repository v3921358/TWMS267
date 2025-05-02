package tools;

import java.io.Serializable;

public class Quintuple<E, F, G, H, I>
        implements Serializable {
    public final E one;
    public final F two;
    public final G three;
    public final H four;
    public final I five;

    public Quintuple(E one, F two, G three, H four, I five) {
        /* 19 */
        this.one = one;
        /* 20 */
        this.two = two;
        /* 21 */
        this.three = three;
        /* 22 */
        this.four = four;
        /* 23 */
        this.five = five;
    }

    public E getOne() {
        /* 27 */
        return this.one;
    }

    public F getTwo() {
        /* 31 */
        return this.two;
    }

    public G getThree() {
        /* 35 */
        return this.three;
    }

    public H getFour() {
        /* 39 */
        return this.four;
    }

    public I getFive() {
        /* 43 */
        return this.five;
    }

    public String toString() {
        /* 48 */
        return this.one.toString() + ":" + this.two.toString() + ":" + this.three.toString() + ":"
                + this.four.toString() + ":" + this.five.toString();
    }

    public int hashCode() {
        /* 53 */
        int prime = 31;
        /* 54 */
        int result = 1;
        /* 55 */
        result = 31 * result + ((this.one == null) ? 0 : this.one.hashCode());
        /* 56 */
        result = 31 * result + ((this.two == null) ? 0 : this.two.hashCode());
        /* 57 */
        result = 31 * result + ((this.three == null) ? 0 : this.three.hashCode());
        /* 58 */
        result = 31 * result + ((this.four == null) ? 0 : this.four.hashCode());
        /* 59 */
        result = 31 * result + ((this.five == null) ? 0 : this.five.hashCode());
        /* 60 */
        return result;
    }

    public boolean equals(Object obj) {
        /* 65 */
        if (this == obj) {
            /* 66 */
            return true;
        }
        /* 68 */
        if (obj == null) {
            /* 69 */
            return false;
        }
        /* 71 */
        if (getClass() != obj.getClass()) {
            /* 72 */
            return false;
        }
        /* 74 */
        Quintuple other = (Quintuple) obj;
        /* 75 */
        if (this.one == null) {
            /* 76 */
            if (other.one != null) {
                /* 77 */
                return false;
            }
            /* 79 */
        } else if (!this.one.equals(other.one)) {
            /* 80 */
            return false;
        }
        /* 82 */
        if (this.two == null) {
            /* 83 */
            if (other.two != null) {
                /* 84 */
                return false;
            }
            /* 86 */
        } else if (!this.two.equals(other.two)) {
            /* 87 */
            return false;
        }
        /* 89 */
        if (this.three == null) {
            /* 90 */
            if (other.three != null) {
                /* 91 */
                return false;
            }
            /* 93 */
        } else if (!this.three.equals(other.three)) {
            /* 94 */
            return false;
        }
        /* 96 */
        if (this.four == null) {
            /* 97 */
            if (other.four != null) {
                /* 98 */
                return false;
            }
            /* 100 */
        } else if (!this.four.equals(other.four)) {
            /* 101 */
            return false;
        }
        /* 103 */
        if (this.five == null) {
            /* 104 */
            if (other.five != null) {
                /* 105 */
                return false;
            }
            /* 107 */
        } else if (!this.five.equals(other.five)) {
            /* 108 */
            return false;
        }
        /* 110 */
        return true;
    }
}
