package sakki;

import java.util.ArrayList;

/**
 * Model of a chess board and pieces on it.
 *
 * This program uses standard chess board with 64 squares
 * in eight files and ranks.
 *
 * {@link http://en.wikipedia.org/wiki/Chessboard}
 *
 * @author Tuomas Starck
 */
class Board {
    private int[] material;
    private boolean[] checked;
    private Type[][] state;
    private ArrayList<Piece> board;

    /**
     * Constructs the initial position. Pieces are placed to their
     * standard start-of-game positions.
     */
    public Board() {
        this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", null);
    }

    /**
     * Constructs a game board with piece positions parsed from given FEN.
     *
     * @param fen First part of FEN string containing board outlook.
     * @param enpassant En passant square if any.
     */
    public Board(String fen, Coord enpassant) {
        int file = 0;
        int rank = 0;

        material = new int[2];
        checked = new boolean[2];
        state = new Type[8][8];
        board = new ArrayList<Piece>();

        for (char chr : fen.toCharArray()) {
            Coord co = null;

            try {
                co = new Coord(file, rank);
            }
            catch (IllegalArgumentException pass) {}

            if (chr == '/') {
                file = 0;
                rank++;
            }
            else if (Character.isDigit(chr)) {
                file += Character.digit(chr, 10);
            }
            else if (Character.isLetter(chr)) {
                board.add(pieceByName(chr, co));
                file++;
            }
            else {
                throw new IllegalArgumentException();
            }
        }

        try {
            update(enpassant);
        }
        catch (NullPointerException npe) {
            throw new IllegalArgumentException();
        }
    }

    private Piece pieceByType(Type type, Coord co) {
        return pieceByName(type.name().charAt(0), co);
    }

    private Piece pieceByName(char chr, Coord co) {
        switch (chr) {
            case 'P': return new WhitePawn(co);
            case 'p': return new BlackPawn(co);
            case 'B': return new WhiteBishop(co);
            case 'b': return new BlackBishop(co);
            case 'N': return new WhiteKnight(co);
            case 'n': return new BlackKnight(co);
            case 'R': return new WhiteRook(co);
            case 'r': return new BlackRook(co);
            case 'Q': return new WhiteQueen(co);
            case 'q': return new BlackQueen(co);
            case 'K': return new WhiteKing(co);
            case 'k': return new BlackKing(co);
        }

        return null;
    }

    private void update(Coord enpassant) {
        material = new int[2];
        checked = new boolean[2];

        for (int i=0; i<8; i++) {
            for (int j=0; j<8; j++) {
                state[i][j] = Type.empty;
            }
        }

        for (Piece piece : board) {
            if (piece == null) throw new NullPointerException();

            Coord loc = piece.location();

            state[loc.rank][loc.file] = piece.type();
        }

        for (Piece piece : board) {
            piece.update(state, enpassant);

            Type type = piece.type();
            Side side = type.getSide();
            Side target = piece.isChecking();

            if (target != null) {
                checked[target.index] = true;
            }

            material[side.index] += type.getValue();
        }
    }

    public boolean isOccupied(Coord co) {
        return (state[co.rank][co.file] != Type.empty);
    }

    private Piece pieceAt(Coord target) {
        for (Piece piece : board) {
            if (piece.location().equals(target)) {
                return piece;
            }
        }

        return null;
    }

    private ArrayList<Piece> crop(ArrayList<Piece> alt, String from) {
        if (from.isEmpty()) return alt;

        ArrayList<Piece> cropd = new ArrayList<Piece>();

        for (Piece piece : alt) {
            if (piece.location().toString().indexOf(from) != -1) {
                cropd.add(piece);
            }
        }

        return cropd;
    }

    private Piece whichPiece(Move move) throws MoveException {
        ArrayList<Piece> alt = new ArrayList<Piece>();

        for (Piece piece : board) {
            if (piece.type() == move.piece()) {
                if (move.isCapturing()) {
                    if (piece.viewAt(move.to()) == Type.capturable) {
                        alt.add(piece);
                    }
                }
                else {
                    if (piece.viewAt(move.to()) == Type.moveable) {
                        alt.add(piece);
                    }
                }
            }
        }

        alt = crop(alt, move.from());

        if (alt.isEmpty()) {
            throw new MoveException("No such move available");
        }

        if (alt.size() != 1) {
            throw new MoveException("Ambiguous move");
        }

        return alt.get(0);
    }

    private String capture(Coord target, boolean capturable) throws MoveException {
        String effect = "";
        Piece piece = pieceAt(target);

        if (piece != null) {
            if (capturable) {
                effect = piece.castlingEffect;
                board.remove(piece);
            }
            else {
                throw new MoveException("Unclaimed capture");
            }
        }
        else if (capturable) {
            throw new MoveException("Capture claimed in vain");
        }

        return effect;
    }

    private void checkCheck(Move move) throws MoveException {
        int side = move.side().index;
        int otherside = (side == 0)? 1: 0;

        if (checked[side]) {
            throw new MoveException("Illegal move", true);
        }

        if (checked[otherside]) {
            if (!move.isChecking()) {
                throw new MoveException("Check without notice", true);
            }
        }
        else {
            if (move.isChecking()) {
                throw new MoveException("Lame check claim", true);
            }
        }
    }

    public Rebound move(Move move, Coord enpassant) throws MoveException {
        String castling = "";
        Rebound rebound = null;
        Side turn = move.side();

        Piece piece = whichPiece(move);

        if (move.piece().isPawn() && move.to().equals(enpassant)) {
            if (turn == Side.w) {
                capture(enpassant.south(1), move.isCapturing());
            }
            else /* turn == Side.b */ {
                capture(enpassant.north(1), move.isCapturing());
            }
        }
        else {
            castling = capture(move.to(), move.isCapturing());
        }

        rebound = piece.move(move);

        rebound.disableCastling(castling);

        if (rebound.canPromote()) {
            Type officer = move.promotion();

            if (officer != null) {
                board.remove(piece);
                board.add(pieceByType(officer, move.to()));
            }
        }

        update(rebound.getEnpassant());

        checkCheck(move);

        return rebound;
    }

    public Rebound castling(Move move, Castle castling) throws MoveException {
        Rebound rebound = null;

        if (!castling.isAllowed(move)) {
            throw new MoveException("Castling not possible");
        }

        for (Coord co : castling.getFreeSqrs(move)) {
            if (isOccupied(co)) {
                throw new MoveException("Castling requires vacant squares");
            }
        }

        /* FIXME
         * King must have safe passage,
         * see castling.getSafeSqrs
         */

        Piece king = pieceAt(castling.getKingsSqr(move));
        Piece rook = pieceAt(castling.getRooksSqr(move));

        if (king == null || rook == null) {
            throw new MoveException("Unable to castle");
        }

        rebound = king.move(castling.getKingsTarget(move));
        rook.move(castling.getRooksTarget(move));

        update(null);

        checkCheck(move);

        return rebound;
    }

    private String packRank(Type[] rank) {
        int empty = 0;
        String str = "";

        for (Type file : rank) {
            if (file == Type.empty) {
                empty++;
            }
            else if (empty != 0) {
                str += String.valueOf(empty) + file;
                empty = 0;
            }
            else {
                str += file;
            }
        }

        if (empty != 0) {
            str += String.valueOf(empty);
        }

        return "/" + str;
    }

    public Type[][] getState() {
        return state;
    }

    public int[] getMaterial() {
        return material;
    }

    @Override
    public String toString() {
        String fen = "";

        for (Type[] rank : state) {
            fen += packRank(rank);
        }

        return fen.substring(1);
    }
}
