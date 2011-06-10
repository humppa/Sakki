package sakki;

/**
 * Abstraction and common operations for various Chess pieces.
 *
 * @author Tuomas Starck
 */
abstract class Piece {
    protected Type me;
    protected Coord loc;
    protected Type[][] view;
    protected String castlingEffect;

    public Piece(Type type, Coord birthplace) {
        if (type == null || birthplace == null) {
            throw new IllegalArgumentException();
        }

        me = type;
        loc = birthplace;
        view = new Type[8][8];

        if (me == Type.K) {
            castlingEffect = "KQ";
        }
        else if (me == Type.k) {
            castlingEffect = "kq";
        }
        else if (me == Type.R) {
            if (loc.equals("h1")) castlingEffect = "K";
            if (loc.equals("a1")) castlingEffect = "Q";
        }
        else if (me == Type.r) {
            if (loc.equals("h8")) castlingEffect = "k";
            if (loc.equals("a8")) castlingEffect = "q";
        }
        else {
            castlingEffect = "";
        }

        reset();
    }

    protected final void reset() {
        for (int i=0; i<8; i++) {
            for (int j=0; j<8; j++) {
                view[i][j] = Type.empty;
            }
        }

        view[loc.rank][loc.file] = me;
    }

    public Type type() {
        return me;
    }

    public Coord location() {
        return loc;
    }

    public String castlingEffect() {
        return castlingEffect;
    }

    public boolean update(Type[][] status, Coord enpassant) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public Rebound move(Coord target) {
        loc = target;
        return null;
    }

    public Rebound move(Move move) {
        Rebound rebound = new Rebound();
        rebound.disableCastling(castlingEffect);
        move(move.to());
        return rebound;
    }

    public boolean canMove(Coord target) {
        return (view[target.rank][target.file] == Type.moveable);
    }

    public boolean canCapture(Coord target) {
        return (view[target.rank][target.file] == Type.capturable);
    }

    protected boolean markIfMoveable(Coord target, Type[][] status) {
        if (target == null) return false;

        if (status[target.rank][target.file] == Type.empty) {
            view[target.rank][target.file] = Type.moveable;
            return true;
        }

        return false;
    }

    protected boolean markIfCapturable(Coord sqr, Type[][] status) {
        boolean checked = false;

        if (sqr == null) return checked;

        Type target = status[sqr.rank][sqr.file];

        if (me.isEnemy(target)) {
            if (target.name().toLowerCase().equals("k")) {
                checked = true;
                view[sqr.rank][sqr.file] = Type.checked;

                /*** * * * DEBUG * * * ***/
                System.out.println("Deep shit at [" + sqr + "] by " + me);
            }
            else {
                view[sqr.rank][sqr.file] = Type.capturable;
            }
        }

        return checked;
    }

    protected void markAdjacent(Type[][] status) {
        if (!markIfMoveable(loc.north(1), status)) {
            markIfCapturable(loc.north(1), status);
        }

        if (!markIfMoveable(loc.northeast(1), status)) {
            markIfCapturable(loc.northeast(1), status);
        }

        if (!markIfMoveable(loc.east(1), status)) {
            markIfCapturable(loc.east(1), status);
        }

        if (!markIfMoveable(loc.southeast(1), status)) {
            markIfCapturable(loc.southeast(1), status);
        }

        if (!markIfMoveable(loc.south(1), status)) {
            markIfCapturable(loc.south(1), status);
        }

        if (!markIfMoveable(loc.southwest(1), status)) {
            markIfCapturable(loc.southwest(1), status);
        }

        if (!markIfMoveable(loc.west(1), status)) {
            markIfCapturable(loc.west(1), status);
        }

        if (!markIfMoveable(loc.northwest(1), status)) {
            markIfCapturable(loc.northwest(1), status);
        }
    }

    protected void markStraight(Type[][] status) {
        int i;

        for (i=1; i<8; i++) {
            if (!markIfMoveable(loc.north(i), status)) {
                break;
            }
        }

        markIfCapturable(loc.north(i), status);

        for (i=1; i<8; i++) {
            if (!markIfMoveable(loc.east(i), status)) {
                break;
            }
        }

        markIfCapturable(loc.east(i), status);

        for (i=1; i<8; i++) {
            if (!markIfMoveable(loc.south(i), status)) {
                break;
            }
        }

        markIfCapturable(loc.south(i), status);

        for (i=1; i<8; i++) {
            if (!markIfMoveable(loc.west(i), status)) {
                break;
            }
        }

        markIfCapturable(loc.west(i), status);
    }

    protected void markDiagonal(Type[][] status) {
        int i;

        for (i=1; i<8; i++) {
            if (!markIfMoveable(loc.northeast(i), status)) {
                break;
            }
        }

        markIfCapturable(loc.northeast(i), status);

        for (i=1; i<8; i++) {
            if (!markIfMoveable(loc.southeast(i), status)) {
                break;
            }
        }

        markIfCapturable(loc.southeast(i), status);

        for (i=1; i<8; i++) {
            if (!markIfMoveable(loc.southwest(i), status)) {
                break;
            }
        }

        markIfCapturable(loc.southwest(i), status);

        for (i=1; i<8; i++) {
            if (!markIfMoveable(loc.northwest(i), status)) {
                break;
            }
        }

        markIfCapturable(loc.northwest(i), status);
    }

    @Override
    public String toString() {
        String str = "";

        for (Type[] rank : view) {
            str += "\n";
            for (Type file : rank) {
                str += " " + file;
            }
        }

        return str;
    }
}
