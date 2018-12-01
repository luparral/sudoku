import com.sun.istack.internal.NotNull;
import sudoku.Sudoku;
import sudoku.Sudoku.Config;
import sudoku.Sudoku.NeighborStrategy;

public class SimulatedAnnealing {

    public static void main(String[] args) {
        runSimulatedAnnealing(
                Sudoku.of(new Config(3, 17)),
                NeighborStrategy.RANDOM_SWAP_BOARD,
                10000.0,
                0.85,
                0.01);
    }

    private static void runSimulatedAnnealing(@NotNull Sudoku initial,
                                              @NotNull NeighborStrategy neighborStrategy,
                                              double temperature,
                                              double coolingRate,
                                              double minimumTemperature) {
        // Initialize system
        Sudoku current = initial;
        current.populateNonFixed();
        int currentEnergy = current.repetitions();
        System.out.println("Initial sudoku repetitions: " + currentEnergy);
        Sudoku best = current;
        int bestEnergy = currentEnergy;

        double equilibriumIterationsAmount = 3 * Math.pow(current.getSquareSize(), 6);

        while (temperature > minimumTemperature) {
            for (int i = 0; i < equilibriumIterationsAmount; i++) {
                Sudoku neighbor = current.neighbor(neighborStrategy);
                int neighborEnergy = neighbor.repetitions();

                // Decide if we should accept the neighbour
                if (shouldAcceptNeighbor(currentEnergy, neighborEnergy, temperature)) {
                    current = neighbor;
                    currentEnergy = neighborEnergy;

                    if (currentEnergy < bestEnergy) {
                        best = current;
                        bestEnergy = currentEnergy;
                        System.out.println("New best sudoku repetitions: " + bestEnergy);
                    }
                }
            }

            // Cool system
            temperature *= 1.0 - coolingRate;
            equilibriumIterationsAmount *= 2;
        }

        System.out.println("Best Sudoku");
        best.dump(System.out);
        System.out.println("Best Sudoku #repetitions: " + bestEnergy);
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