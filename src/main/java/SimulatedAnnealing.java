import sudoku.Sudoku;
import sudoku.Sudoku.Config;
import sudoku.Sudoku.NeighborStrategy;

public class SimulatedAnnealing {

    public static void main(String[] args) {
        runSimulatedAnnealing(4, 17, NeighborStrategy.RANDOM_SWAP_SQUARE);
    }

    private static void runSimulatedAnnealing(int squareSize, int fixedQuantity, NeighborStrategy neighborStrategy) {
        // Initialize system
        Sudoku current = Sudoku.of(new Config(squareSize, fixedQuantity));
        int currentEnergy = current.repetitions();
        System.out.println("Initial sudoku repetitions: " + currentEnergy);
        Sudoku best = current;
        int bestEnergy = currentEnergy;

        double temperature = 100000.0;
        double coolingRate = 0.85;
        double minimumTemperature = 0.1;
        double equilibriumIterationsAmount = Math.pow(squareSize, 7);

        // Loop until system has cooled
        while (temperature > minimumTemperature) {
            for (int i = 0; i < equilibriumIterationsAmount; i++) {
                Sudoku neighbor = current.neighbor(neighborStrategy);
                int neighborEnergy = neighbor.repetitions();

                // Decide if we should accept the neighbour
                if (shouldAcceptNeighbor(currentEnergy, neighborEnergy, temperature)) {
                    current = neighbor;
                    currentEnergy = neighborEnergy;

                    // Keep track of the best solution found
                    if (currentEnergy < bestEnergy) {
                        best = current;
                        bestEnergy = currentEnergy;
                        System.out.println("New best sudoku repetitions: " + bestEnergy);
                    }

                    if (bestEnergy == 0) {
                        temperature = minimumTemperature;
                        break;
                    }
                }
            }

            // Cool system
            temperature *= 1.0 - coolingRate;
            equilibriumIterationsAmount *= 2;
        }

        System.out.println("Final Sudoku");
        best.show();
        System.out.println("Final Sudoku #repetitions: " + bestEnergy);
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