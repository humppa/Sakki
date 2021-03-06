package fi.starck.sakki.board;

/**
 * Black bishop.
 *
 * @author Tuomas Starck
 */
class BlackBishop extends Piece {
    public BlackBishop(Coord birthplace) {
        super(Type.b, birthplace);
    }

    @Override
    public void update(Type[][] status, Coord ep) {
        reset();
        markDiagonal(status);
    }
}
