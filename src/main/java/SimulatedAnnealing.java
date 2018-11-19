public class SimulatedAnnealing {

    public static void main(String[] args) {
        // Initialize system
        Sudoku current = new Sudoku(3, 9);
        int currentEnergy = current.repetitions();
        System.out.println("Initial sudoku repetitions: " + currentEnergy);
        Sudoku best = current;
        int bestEnergy = currentEnergy;

        double temperature = 10000.0;
        double coolingRate = 0.003;

        // Loop until system has cooled
        while (temperature > 1.0) {
            Sudoku neighbor = current.randomSwap();
            int neighbourEnergy = neighbor.repetitions();

            // Decide if we should accept the neighbour
            if (acceptanceProbability(currentEnergy, neighbourEnergy, temperature) > Math.random()) {
                current = neighbor;
                currentEnergy = neighbourEnergy;

                // Keep track of the best solution found
                if (currentEnergy < bestEnergy) {
                    best = current;
                    bestEnergy = currentEnergy;
                    System.out.println("New best sudoku repetitions: " + bestEnergy);
                }
            }

            // Cool system
            temperature *= 1 - coolingRate;
        }

        System.out.println("Final Sudoku");
        best.show();
        System.out.println("Final Sudoku #repetitions: " + bestEnergy);
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