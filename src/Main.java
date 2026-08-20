import ui.MainUI;

/**
 * Application entry point.
 * Launches the Part 15 Swing GUI, which wires together every module built
 * across Parts 1 through 14 (structures, algorithms, operations, database,
 * performance) into one tabbed window an examiner can drive without editing
 * source code.
 */
public class Main {
    public static void main(String[] args) {
        MainUI.launch();
    }
}
