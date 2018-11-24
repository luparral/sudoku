public class SimulatedAnnealing {

    public static void main(String[] args) {
        // Initialize system
        Sudoku current = new Sudoku(3, 17);
        int currentEnergy = current.repetitions();
        System.out.println("Initial sudoku repetitions: " + currentEnergy);
        Sudoku best = current;
        int bestEnergy = currentEnergy;

        double temperature = 100000.0;
        double coolingRate = 0.85;
        double minimumTemperature = 0.01;
        int equilibriumIterationsAmount = 20000;

        // Loop until system has cooled
        while (temperature > minimumTemperature) {
            for (int i = 0; i < equilibriumIterationsAmount; i++) {
                Sudoku neighbor = current.randomSwap();
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