import com.sun.istack.internal.NotNull;
import sudoku.Sudoku;
import sudoku.Sudoku.NeighborStrategy;
import sudoku.Sudoku.ValueSeparatorFileConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.Objects;

public class SimulatedAnnealing {

    private static final String USER_DIRECTORY_PATH = System.getProperty("user.dir");

    public static void main(String[] args) {
        runPerDifficultyTrials(4, NeighborStrategy.RANDOM_SWAP_SQUARE, 10000.0, 0.01, 0.85, Difficulty.EASY);
    }

    // TODO: Allow difficulty setting
    public static void runKaggleTrials(int squareSize,
                                       @NotNull NeighborStrategy strategy,
                                       double initialTemperature,
                                       double minimumTemperature,
                                       double coolingRate) {
        Path datasetDirectoryPath = Paths.get(USER_DIRECTORY_PATH, "datasets", "kaggle");
        try {
            Files.createDirectories(datasetDirectoryPath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String resultsFileName = String.format("r_%d_%s_%.0f_%.2f_%.2f.txt",
                squareSize, strategy.shorthand(), initialTemperature, minimumTemperature, coolingRate);
        Path resultsFilePath = Paths.get(USER_DIRECTORY_PATH, "results", "kaggle", resultsFileName);

        boolean writeHeader = true;
        try (BufferedWriter output = Files.newBufferedWriter(resultsFilePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            FilenameFilter filenameFilter = (dir, name) -> name.matches(String.format("s_%d.*\\.txt", squareSize));

            for (File file : Objects.requireNonNull(datasetDirectoryPath.toFile().listFiles(filenameFilter))) {
                /*
                 * Format is ["s", Square Size, Difficulty, Instance Number]
                 */
                String[] nameSplit = file.getName().replaceAll(".txt", "").split("_");
                String instanceNumber = nameSplit[3];

                SimulatedAnnealingReport report = runSimulatedAnnealing(
                        Sudoku.of(new ValueSeparatorFileConfig(file.toPath(), ' ')),
                        strategy,
                        initialTemperature,
                        minimumTemperature,
                        coolingRate);

                report.dump(output, String.valueOf(instanceNumber), writeHeader);
                writeHeader = false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void runPerDifficultyTrials(int squareSize,
                                              @NotNull NeighborStrategy strategy,
                                              double initialTemperature,
                                              double minimumTemperature,
                                              double coolingRate,
                                              @NotNull Difficulty difficulty) {
        String difficultyName = difficulty.name().toLowerCase();
        Path datasetDirectoryPath = Paths.get(USER_DIRECTORY_PATH, "datasets", "difficulty");
        try {
            Files.createDirectories(datasetDirectoryPath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String resultsFileName = String.format("r_%d_%s_%s_%.0f_%.2f_%.2f.txt",
                squareSize, difficultyName, strategy.shorthand(), initialTemperature, minimumTemperature, coolingRate);
        Path resultsFilePath = Paths.get(USER_DIRECTORY_PATH, "results", "difficulty", resultsFileName);

        boolean writeHeader = true;
        try (BufferedWriter output = Files.newBufferedWriter(resultsFilePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            FilenameFilter filenameFilter = (dir, name) -> name.matches(String.format("s_%d_%s.*\\.txt", squareSize, difficultyName));

            for (File file : Objects.requireNonNull(datasetDirectoryPath.toFile().listFiles(filenameFilter))) {
                /*
                 * Format is ["s", Square Size, Difficulty, Instance Number]
                 */
                String[] nameSplit = file.getName().replaceAll(".txt", "").split("_");
                String instanceNumber = nameSplit[3];

                SimulatedAnnealingReport report = runSimulatedAnnealing(
                        Sudoku.of(new ValueSeparatorFileConfig(file.toPath(), ' ')),
                        strategy,
                        initialTemperature,
                        minimumTemperature,
                        coolingRate);

                report.dump(output, String.valueOf(instanceNumber), writeHeader);
                writeHeader = false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void runParametersFixationTrials(int squareSize,
                                                   @NotNull NeighborStrategy strategy,
                                                   double initialTemperature,
                                                   double minimumTemperature,
                                                   double coolingRate) {
        Path datasetDirectoryPath = Paths.get(USER_DIRECTORY_PATH, "datasets", "params_fixation");
        try {
            Files.createDirectories(datasetDirectoryPath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String resultsFileName = String.format("r_%d_%s_%.0f_%.2f_%.2f.txt",
                squareSize, strategy.shorthand(), initialTemperature, minimumTemperature, coolingRate);
        Path resultsFilePath = Paths.get(USER_DIRECTORY_PATH, "results", "params_fixation", resultsFileName);

        boolean writeHeader = true;
        try (BufferedWriter output = Files.newBufferedWriter(resultsFilePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            FilenameFilter filenameFilter = (dir, name) -> name.matches(String.format("s_%d_.*\\.txt", squareSize));

            for (File file : Objects.requireNonNull(datasetDirectoryPath.toFile().listFiles(filenameFilter))) {
                /*
                 * Format is ["s", Square Size, Fixed Quantity, Instance Number]
                 */
                String[] nameSplit = file.getName().replaceAll(".txt", "").split("_");
                String instanceNumber = nameSplit[3];

                SimulatedAnnealingReport report = runSimulatedAnnealing(
                        Sudoku.of(new ValueSeparatorFileConfig(file.toPath(), ' ')),
                        strategy,
                        initialTemperature,
                        minimumTemperature,
                        coolingRate);

                report.dump(output, String.valueOf(instanceNumber), writeHeader);
                writeHeader = false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static SimulatedAnnealingReport runSimulatedAnnealing(@NotNull Sudoku initial,
                                                                  @NotNull NeighborStrategy neighborStrategy,
                                                                  double temperature,
                                                                  double minimumTemperature,
                                                                  double coolingRate) {
        SimulatedAnnealingReport report = new SimulatedAnnealingReport();
        Sudoku current = initial;
        current.populateNonFixed();
        int currentEnergy = current.repetitions();
        int bestEnergy = currentEnergy;
        long equilibriumIterationsAmount = (long) Math.pow(current.getSquareSize(), 6);

        report.appendIterationReport(currentEnergy, bestEnergy, temperature);
        while (temperature > minimumTemperature) {
            for (long i = 0; i < equilibriumIterationsAmount; i++) {
                Sudoku neighbor = current.neighbor(neighborStrategy);
                int neighborEnergy = neighbor.repetitions();

                if (shouldAcceptNeighbor(currentEnergy, neighborEnergy, temperature)) {
                    current = neighbor;
                    currentEnergy = neighborEnergy;

                    if (currentEnergy < bestEnergy) {
                        bestEnergy = currentEnergy;
                    }
                }

                report.appendIterationReport(currentEnergy, bestEnergy, temperature);
            }

            // Cool system
            temperature *= 1.0 - coolingRate;
        }

        return report;
    }

    private static boolean shouldAcceptNeighbor(int currentEnergy, int neighborEnergy, double temperature) {
        return acceptanceProbability(currentEnergy, neighborEnergy, temperature) > Math.random();
    }

    private static double acceptanceProbability(int currentEnergy, int neighborEnergy, double temperature) {
        // If the new solution is better, accept it
        if (neighborEnergy < currentEnergy) {
            return 1.0;
        }
        // If the new solution is worse, calculate an acceptance probability
        return Math.exp((currentEnergy - neighborEnergy) / temperature);
    }

    private enum Difficulty {
        EASY, MEDIUM, HARD, GENIUS
    }
    
}

// TODO: Comment
final class SimulatedAnnealingReport {

    private final LinkedList<ReportNode> iterationReports = new LinkedList<>();
    private final long initialTime = System.currentTimeMillis();

    void appendIterationReport(int cost, int bestCost, double temperature) {
        long currentTime = System.currentTimeMillis();
        int iterationNumber = iterationReports.size();
        iterationReports.add(new ReportNode(iterationNumber, cost, bestCost, currentTime - initialTime, temperature));
    }

    void dump(@NotNull BufferedWriter writer,
              @NotNull String id,
              boolean writeHeader) throws IOException {
        if (writeHeader) {
            writer.append("id iteration cost bestCost time currentTemperature");
        }

        String lineFormat = "%s %d %d %d %d %.0f";

        for (ReportNode node : iterationReports) {
            writer.newLine();
            writer.append(String.format(lineFormat,
                    id,
                    node.iteration,
                    node.cost,
                    node.bestCost,
                    node.time,
                    node.temperature));
        }
    }

    private static class ReportNode {
        final int iteration;
        final int cost;
        final int bestCost;
        final long time;
        final double temperature;

        private ReportNode(int iteration, int cost, int bestCost, long time, double temperature) {
            this.iteration = iteration;
            this.cost = cost;
            this.bestCost = bestCost;
            this.time = time;
            this.temperature = temperature;
        }
    }

}