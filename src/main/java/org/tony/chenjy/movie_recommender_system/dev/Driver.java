package org.tony.chenjy.movie_recommender_system.dev;

public class Driver {
	public static void main(String[] args) throws Exception {
		args = new String[]{"src/main/resources/input_small/*",
							// "src/main/resources/input_big/*",
							"src/main/resources/output/userRatingList",
							"src/main/resources/output/coOccurrenceMatrix",
							"src/main/resources/output/normalizeMatrix",
							"src/main/resources/output/multiplicationUnit",
							"src/main/resources/output/sum",
							"src/main/resources/output/compare"};

		String userRating = args[0];
		String userRatingList = args[1];
		String coOccurrenceMatrix = args[2];
		String normalizeMatrix = args[3];
		String multiplicationUnit = args[4];
		String sum = args[5];
		String compare = args[6];

		String[] path1 = {userRating, userRatingList};
		String[] path2 = {userRatingList, coOccurrenceMatrix};
		String[] path3 = {coOccurrenceMatrix, normalizeMatrix};

		DataDividerByUser.main(path1);
		CoOccurrenceMatrixGenerator.main(path2);
		Normalize.main(path3);

		// without average rating
		String[] path4 = {normalizeMatrix, userRating, multiplicationUnit};
		String[] path5 = {multiplicationUnit, sum};
		String[] path6 = {userRating, sum, compare};

		Multiplication.main(path4);
		Sum.main(path5);
		Compare.main(path6);
	}

}
