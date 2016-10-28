package cc.args;

/**
 * Class ArgsParseException
 *
 * Exception that gets thrown when something fails to parse properly
 */
public class ArgsParseException extends Exception {
    public ArgsParseException(final String msg) {
        super(msg);
    }
}
