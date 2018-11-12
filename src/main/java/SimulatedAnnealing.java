public class SimulatedAnnealing {

    private static double acceptanceProbability(int energy, int newEnergy, double temperature) {
        // If the new solution is better, accept it
        if (newEnergy < energy) {
            return 1.0;
        }
        // If the new solution is worse, calculate an acceptance probability
        return Math.exp((energy - newEnergy) / temperature);
    }

    public static void main(String[] args) {
        // TODO: Create initial Sudoku
        Sudoku current = new Sudoku(5, 5);
        // TODO: Initialize initial Sudoku
        // System.out.println("Initial sudoku repetitions: " + current.repetitions());
        Sudoku best = current;

        double temp = 10000.0;

        double coolingRate = 0.003;

        // Loop until system has cooled
        while (temp > 1.0) {
            // TODO: Create neighbor solution
            //Sudoku neighbor = null;

            // TODO: Get real energy of solutions
            //int currentEnergy = 0;
            //int neighbourEnergy = 0;

            // Decide if we should accept the neighbour
            //if (acceptanceProbability(currentEnergy, neighbourEnergy, temp) > Math.random()) {
            //    current = new Sudoku(neighbor);
            //}

            // Keep track of the best solution found
            //if (current.repetitions() < best.repetitions()) {
            //    best = new Sudoku(current);
            //    System.out.println("New best sudoku repetitions: " + best.repetitions());
            //}

            // Cool system
            temp *= 1 - coolingRate;
        }

        // System.out.println("Final solution repetitions: " + best.repetitions());
        System.out.println("Final Sudoku: " + best);
    }
    
}