package tools;

import java.io.Serializable;

public class Quadruple<E, F, G, H>
        implements Serializable {
    private static final long serialVersionUID = 9179541993413749999L;
    public final E one;
    public final F two;
    public final G three;
    public final H four;

    public Quadruple(E one, F two, G three, H four) {
        /* 19 */
        this.one = one;
        /* 20 */
        this.two = two;
        /* 21 */
        this.three = three;
        /* 22 */
        this.four = four;
    }

    public E getOne() {
        /* 26 */
        return this.one;
    }

    public F getTwo() {
        /* 30 */
        return this.two;
    }

    public G getThree() {
        /* 34 */
        return this.three;
    }

    public H getFour() {
        /* 38 */
        return this.four;
    }

    public String toString() {
        /* 43 */
        return this.one.toString() + ":" + this.two.toString() + ":" + this.three.toString() + ":"
                + this.four.toString();
    }

    public int hashCode() {
        /* 48 */
        int prime = 31;
        /* 49 */
        int result = 1;
        /* 50 */
        result = 31 * result + ((this.one == null) ? 0 : this.one.hashCode());
        /* 51 */
        result = 31 * result + ((this.two == null) ? 0 : this.two.hashCode());
        /* 52 */
        result = 31 * result + ((this.three == null) ? 0 : this.three.hashCode());
        /* 53 */
        result = 31 * result + ((this.four == null) ? 0 : this.four.hashCode());
        /* 54 */
        return result;
    }

    public boolean equals(Object obj) {
        /* 59 */
        if (this == obj) {
            /* 60 */
            return true;
        }
        /* 62 */
        if (obj == null) {
            /* 63 */
            return false;
        }
        /* 65 */
        if (getClass() != obj.getClass()) {
            /* 66 */
            return false;
        }
        /* 68 */
        Quadruple other = (Quadruple) obj;
        /* 69 */
        if (this.one == null) {
            /* 70 */
            if (other.one != null) {
                /* 71 */
                return false;
            }
            /* 73 */
        } else if (!this.one.equals(other.one)) {
            /* 74 */
            return false;
        }
        /* 76 */
        if (this.two == null) {
            /* 77 */
            if (other.two != null) {
                /* 78 */
                return false;
            }
            /* 80 */
        } else if (!this.two.equals(other.two)) {
            /* 81 */
            return false;
        }
        /* 83 */
        if (this.three == null) {
            /* 84 */
            if (other.three != null) {
                /* 85 */
                return false;
            }
            /* 87 */
        } else if (!this.three.equals(other.three)) {
            /* 88 */
            return false;
        }
        /* 90 */
        /* 95 */
        if (this.four == null) {
            /* 91 */
            /* 92 */
            return other.four == null;
            /* 94 */
        } else return this.four.equals(other.four);
        /* 97 */
    }
}
