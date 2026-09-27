package site.klade.simulation.condition;

/**
 * A numeric comparison producing a boolean, e.g. {@code Mrph[1] > 1.0}.
 *
 * <p>Both operands are {@code Expr} (numeric atoms only), so a comparison cannot be nested
 * inside another comparison and comparison operators always act on numbers.</p>
 */
public final class Compare implements Cond {

    public final Expr left;
    public final CmpOp op;
    public final Expr right;

    public Compare(Expr left, CmpOp op, Expr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Compare)) {
            return false;
        }
        Compare c = (Compare) o;
        return left.equals(c.left) && op == c.op && right.equals(c.right);
    }

    @Override
    public int hashCode() {
        int r = left.hashCode();
        r = 31 * r + op.hashCode();
        r = 31 * r + right.hashCode();
        return r;
    }

    @Override
    public String toString() {
        return left + " " + symbol(op) + " " + right;
    }

    private static String symbol(CmpOp op) {
        switch (op) {
            case LT: return "<";
            case LE: return "<=";
            case GT: return ">";
            case GE: return ">=";
            case EQ: return "==";
            case NE: return "!=";
            default: throw new IllegalStateException("unexpected CmpOp " + op);
        }
    }
}