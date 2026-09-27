package site.klade.simulation.condition;

/**
 * Boolean combination {@code A <AND|OR|XOR> B}.
 *
 * <p>Precedence (lowest to highest): {@code OR < XOR < AND < NOT < comparison} (§3).
 * {@code toString()} enforces the round-trip paren rule (§4): a composite operand is
 * parenthesized when {@code childPrec < parentPrec}, and also when
 * {@code childPrec == parentPrec} and it is the <b>right</b> operand. This makes the
 * emission independent of any left-vs-right associativity assumption in the parser.</p>
 */
public final class BoolOp implements Cond {

    public final Cond left;
    public final BoolOpKind kind;
    public final Cond right;

    public BoolOp(Cond left, BoolOpKind kind, Cond right) {
        this.left = left;
        this.kind = kind;
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BoolOp)) {
            return false;
        }
        BoolOp b = (BoolOp) o;
        return left.equals(b.left) && kind == b.kind && right.equals(b.right);
    }

    @Override
    public int hashCode() {
        int r = left.hashCode();
        r = 31 * r + kind.hashCode();
        r = 31 * r + right.hashCode();
        return r;
    }

    private static final int PREC_OR = 1;
    private static final int PREC_XOR = 2;
    private static final int PREC_AND = 3;
    private static final int PREC_NOT = 4;
    private static final int PREC_COMPARE = 5;

    private static int kindPrec(BoolOpKind k) {
        switch (k) {
            case OR: return PREC_OR;
            case XOR: return PREC_XOR;
            case AND: return PREC_AND;
            default: throw new IllegalStateException("unexpected BoolOpKind " + k);
        }
    }

    /** Precedence of a boolean node; used by {@link #operand(Cond, int, boolean)}. */
    static int condPrec(Cond c) {
        if (c instanceof Compare) {
            return PREC_COMPARE;
        }
        if (c instanceof Not) {
            return PREC_NOT;
        }
        return kindPrec(((BoolOp) c).kind);
    }

    @Override
    public String toString() {
        int parentPr = kindPrec(kind);
        return operand(left, parentPr, true)
                + " " + kind + " "
                + operand(right, parentPr, false);
    }

    private static String operand(Cond c, int parentPr, boolean isLeft) {
        int childPr = condPrec(c);
        if (childPr < parentPr || (!isLeft && childPr == parentPr)) {
            return "(" + c + ")";
        }
        return c.toString();
    }
}