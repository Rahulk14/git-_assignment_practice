import java.util.*;

public class Assignment {

    public static Map<String, String> matchCandidatesToLocations(
            Map<String, List<String>> candidatesPrefs,
            Map<String, List<String>> locationsPrefs) {

        Map<String, String> candidateToLocation = new HashMap<>();
        Map<String, String> locationToCandidate = new HashMap<>();
        Map<String, Queue<String>> candidateProposals = new HashMap<>();
        Map<String, Map<String, Integer>> locationRankings = new HashMap<>();
        Set<String> freeCandidates = new HashSet<>(candidatesPrefs.keySet());

        // Initialize candidate proposals
        for (String candidate : candidatesPrefs.keySet()) {
            candidateProposals.put(candidate, new LinkedList<>(candidatesPrefs.get(candidate)));
        }

        // Initialize location rankings with default if missing
        for (Map.Entry<String, List<String>> entry : locationsPrefs.entrySet()) {
            Map<String, Integer> ranking = new HashMap<>();
            List<String> prefs = entry.getValue();
            for (int i = 0; i < prefs.size(); i++) {
                ranking.put(prefs.get(i), i);
            }
            locationRankings.put(entry.getKey(), ranking);
        }

        // Ensure all locations have rankings
        for (String location : locationsPrefs.keySet()) {
            locationRankings.putIfAbsent(location, new HashMap<>());
        }

        // Gale-Shapley Algorithm
        while (!freeCandidates.isEmpty()) {
            String candidate = freeCandidates.iterator().next();
            Queue<String> proposals = candidateProposals.get(candidate);
            if (proposals.isEmpty()) {
                freeCandidates.remove(candidate);
                continue;
            }

            String proposedLocation = proposals.poll();
            if (!locationToCandidate.containsKey(proposedLocation)) {
                candidateToLocation.put(candidate, proposedLocation);
                locationToCandidate.put(proposedLocation, candidate);
                freeCandidates.remove(candidate);
            } else {
                String currentCandidate = locationToCandidate.get(proposedLocation);
                if (prefersNewCandidate(proposedLocation, candidate, currentCandidate, locationRankings)) {
                    candidateToLocation.put(candidate, proposedLocation);
                    candidateToLocation.remove(currentCandidate);
                    freeCandidates.add(currentCandidate);
                    freeCandidates.remove(candidate);
                    locationToCandidate.put(proposedLocation, candidate);
                } else {
                    candidateProposals.put(candidate, proposals);
                }
            }
        }

        return candidateToLocation;
    }

    private static boolean prefersNewCandidate(String location, String newCandidate, String currentCandidate,
            Map<String, Map<String, Integer>> locationRankings) {
        Map<String, Integer> rankings = locationRankings.get(location);
        // Handle case where ranking is missing
        return rankings.getOrDefault(newCandidate, Integer.MAX_VALUE) < rankings.getOrDefault(currentCandidate,
                Integer.MAX_VALUE);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Map<String, List<String>> candidatesPrefs = new HashMap<>();
        Map<String, List<String>> locationsPrefs = new HashMap<>();

        System.out.println("Enter number of candidates:");
        int numCandidates = scanner.nextInt();
        scanner.nextLine(); // Consume the newline

        for (int i = 0; i < numCandidates; i++) {
            System.out.println("Enter candidate name:");
            String candidate = scanner.nextLine();

            System.out.println("Enter preferences for " + candidate + " (comma-separated):");
            String[] prefs = scanner.nextLine().split(",");

            candidatesPrefs.put(candidate, Arrays.asList(prefs));
        }

        System.out.println("Enter number of locations:");
        int numLocations = scanner.nextInt();
        scanner.nextLine(); // Consume the newline

        for (int i = 0; i < numLocations; i++) {
            System.out.println("Enter location name:");
            String location = scanner.nextLine();

            System.out.println("Enter preferences for " + location + " (comma-separated, or leave blank for none):");
            String input = scanner.nextLine();
            String[] prefs = input.isEmpty() ? new String[] {} : input.split(",");

            locationsPrefs.put(location, Arrays.asList(prefs));
        }

        // Run the algorithm
        Map<String, String> match = matchCandidatesToLocations(candidatesPrefs, locationsPrefs);

        // Output the results
        for (Map.Entry<String, String> entry : match.entrySet()) {
            System.out.println("Candidate " + entry.getKey() + " is assigned to " + entry.getValue());
        }

        scanner.close();
    }
}
