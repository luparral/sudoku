import com.sun.istack.internal.NotNull;
import sudoku.Sudoku;
import sudoku.Sudoku.NeighborStrategy;
import sudoku.Sudoku.SingleDigitValueFileConfig;
import sudoku.Sudoku.ValueSeparatorFileConfig;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.Objects;

public class SimulatedAnnealing {

    private static final String USER_DIRECTORY_PATH = System.getProperty("user.dir");

    public static void main(String[] args) {
        runParametersFixationTrials(NeighborStrategy.RANDOM_SWAP_SQUARE, 10000.0, 0.01, 0.85);
    }

    public static void runParametersFixationTrials(@NotNull NeighborStrategy strategy,
                                                   double initialTemperature,
                                                   double minimumTemperature,
                                                   double coolingRate) {
        Path datasetDirectoryPath = Paths.get(USER_DIRECTORY_PATH, "datasets", "params_fixation");
        Path resultsFilePath = Paths.get(USER_DIRECTORY_PATH, "results", "params_fixation", "results_params_fixation.txt");

        try (BufferedWriter output = Files.newBufferedWriter(resultsFilePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            for (File file : Objects.requireNonNull(datasetDirectoryPath.toFile().listFiles())) {
                if (!file.getName().endsWith(".txt")) continue;

                /*
                 * Format is ["s", Square Size, Fixed Quantity, Instance Number]
                 */
                String[] nameSplit = file.getName().replaceAll(".txt", "").split("_");
                String instanceNumber = nameSplit[3];

                System.out.println("Running Simulated Annealing with " + file.getName());
                SimulatedAnnealingReport report = runSimulatedAnnealing(
                        Sudoku.of(new ValueSeparatorFileConfig(file.toPath(), ' ')),
                        strategy,
                        initialTemperature,
                        minimumTemperature,
                        coolingRate);

                report.dump(output, String.valueOf(instanceNumber));
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
        SimulatedAnnealingReport report = new SimulatedAnnealingReport(
                initial.getSquareSize(),
                neighborStrategy,
                temperature,
                minimumTemperature,
                coolingRate);

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
    
}

// TODO: Comment
final class SimulatedAnnealingReport {

    private final LinkedList<ReportNode> iterationReports = new LinkedList<>();
    private final long initialTime = System.currentTimeMillis();
    private final int squareSize;
    private final NeighborStrategy strategy;
    private final double initialTemperature;
    private final double minimumTemperature;
    private final double coolingRate;

    SimulatedAnnealingReport(int squareSize,
                             @NotNull NeighborStrategy strategy,
                             double initialTemperature,
                             double minimumTemperature,
                             double coolingRate) {
        this.squareSize = squareSize;
        this.strategy = strategy;
        this.initialTemperature = initialTemperature;
        this.minimumTemperature = minimumTemperature;
        this.coolingRate = coolingRate;
    }

    void appendIterationReport(int cost, int bestCost, double temperature) {
        long currentTime = System.currentTimeMillis();
        int iterationNumber = iterationReports.size();
        iterationReports.add(new ReportNode(iterationNumber, cost, bestCost, currentTime - initialTime, temperature));
    }

    void dump(@NotNull BufferedWriter writer,
              @NotNull String id) throws IOException {
        writer.append("id squareSize strategy iteration cost bestCost time currentTemperature initialTemperature minimumTemperature coolingRate");
        String lineFormat = "%s %d %s %d %d %d %d %.0f %.0f %.2f %.2f";

        for (ReportNode node : iterationReports) {
            writer.newLine();
            writer.append(String.format(lineFormat,
                    id,
                    squareSize,
                    strategy.toString(),
                    node.iteration,
                    node.cost,
                    node.bestCost,
                    node.time,
                    node.temperature,
                    initialTemperature,
                    minimumTemperature,
                    coolingRate));
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