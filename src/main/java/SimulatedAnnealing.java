import com.sun.istack.internal.NotNull;
import sudoku.Sudoku;
import sudoku.Sudoku.NeighborStrategy;
import sudoku.Sudoku.SingleDigitValueFileConfig;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class SimulatedAnnealing {

    // TODO: Clean code
    public static void main(String[] args) {
        Path userDirectory = Paths.get(System.getProperty("user.dir"));
        try {
            Files.createDirectories(userDirectory);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        SimulatedAnnealingReport report = runSimulatedAnnealing(
                Sudoku.of(new Sudoku.ValueSeparatorFileConfig(userDirectory.resolve("test_separator.txt"), ' ')),
                NeighborStrategy.RANDOM_SWAP_SQUARE,
                10000.0,
                0.85,
                0.01);

        Path resultsFilePath = userDirectory.resolve("results.txt");
        if (Files.notExists(resultsFilePath)) {
            try {
                Files.createFile(resultsFilePath);
            } catch (IOException ignored) {
                // Do nothing
            }
        }

        try (BufferedWriter output = Files.newBufferedWriter(resultsFilePath)) {
            report.dump(output);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private static SimulatedAnnealingReport runSimulatedAnnealing(@NotNull Sudoku initial,
                                                                  @NotNull NeighborStrategy neighborStrategy,
                                                                  double temperature,
                                                                  double coolingRate,
                                                                  double minimumTemperature) {
        SimulatedAnnealingReport report = new SimulatedAnnealingReport();
        Sudoku current = initial;
        current.populateNonFixed();
        int currentEnergy = current.repetitions();
        Sudoku best = current;
        int bestEnergy = currentEnergy;
        long equilibriumIterationsAmount = (long) Math.pow(current.getSquareSize(), 7);

        report.appendIterationReport(currentEnergy, bestEnergy);
        while (temperature > minimumTemperature) {
            for (long i = 0; i < equilibriumIterationsAmount; i++) {
                Sudoku neighbor = current.neighbor(neighborStrategy);
                int neighborEnergy = neighbor.repetitions();

                if (shouldAcceptNeighbor(currentEnergy, neighborEnergy, temperature)) {
                    current = neighbor;
                    currentEnergy = neighborEnergy;

                    if (currentEnergy < bestEnergy) {
                        best = current;
                        bestEnergy = currentEnergy;
                    }
                }

                report.appendIterationReport(currentEnergy, bestEnergy);
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

    void appendIterationReport(int cost, int bestCost) {
        long currentTime = System.currentTimeMillis();
        int iterationNumber = iterationReports.size();
        iterationReports.add(new ReportNode(iterationNumber, cost, bestCost, currentTime - initialTime));
    }

    void dump(@NotNull BufferedWriter writer) throws IOException {
        writer.append("iteration cost bestCost time");
        String lineFormat = "%d %d %d %d";

        for (ReportNode node : iterationReports) {
            writer.newLine();
            writer.append(String.format(lineFormat, node.iteration, node.cost, node.bestCost, node.time));
        }
    }

    private static class ReportNode {
        final int iteration;
        final int cost;
        final int bestCost;
        final long time;

        private ReportNode(int iteration, int cost, int bestCost, long time) {
            this.iteration = iteration;
            this.cost = cost;
            this.bestCost = bestCost;
            this.time = time;
        }
    }

}