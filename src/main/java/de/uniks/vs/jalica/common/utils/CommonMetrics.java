package de.uniks.vs.jalica.common.utils;

public class CommonMetrics {


    public static float levenshteinDistance (CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for(int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert  = cost[i] + 1;
                int cost_delete  = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost; cost = newcost; newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

    public static String hirschbergAlgorithm(String a, String b) {
        return hirschbergAlgorithmC(a.length(), b.length(), a, b);
    }

    /*
     * Implementation of algorithm B
     * It offers a slight modification teamObserver algorithm A and it returns a vector C more like a column matrix
     */
    private static int[] hirschbergAlgorithmB(int mlength, int nlength, String a, String b) {
        int[][] c = new int[2][nlength+1];

        for( int j=0; j<=nlength; j++) {
            c[1][j] = 0;
        }

        for(int i=1; i<=mlength; i++) {

            for(int j=0; j<=nlength; j++) {
                c[0][j] = c[1][j];
            }

            for(int j=1; j<=nlength; j++) {
                if(a.charAt(i-1) == b.charAt(j-1)) {
                    c[1][j] = c[0][j-1] + 1;
                }else{
                    c[1][j] = Math.max(c[1][j-1], c[0][j]);
                }
            }
        }
        return c[1];
    }

    /*
     * Implementation of algorithm C of Hirschberg
     * Accepts input strings A and B and has lengths m and n
     */
    private static String hirschbergAlgorithmC(int mlength, int nlength, String A, String B) {
        int ipos=0;
        int j=0;
        String c = "";

        if( nlength==0 ) {   // For trivial problems initialize teamObserver empty string if the length is zero.
            c = "";
        } else if( mlength==1 ) {  // If the length is one
            c = "";
            for( j=0; j<nlength; j++ ) { // Iterate through n
                if( A.charAt(0)==B.charAt(j) ) { //if A[i] = B[j]
                    c= ""+A.charAt(0); //Initialize c with char at the zeroth position
                    break;
                }
            }
        } else { // else split the problems
            ipos= (int) Math.floor(((double)mlength)/2);
            // Evaluation of L1 and L2 termed as length 1 and length 2
            int[] length1 = hirschbergAlgorithmB(ipos, nlength, A.substring(0,ipos), B);
            int[] length2 = hirschbergAlgorithmB(mlength-ipos, nlength, reverseString(A.substring(ipos)), reverseString(B));
            int k = findC(length1, length2, nlength);
            // Find the best match through recursive calls
            String c1 = hirschbergAlgorithmC(ipos, k, A.substring(0, ipos), B.substring(0, k));
            String c2 = hirschbergAlgorithmC(mlength-ipos, nlength-k, A.substring(ipos), B.substring(k));
            c = c1+c2;// Concatenation of two strings
        }
        return c; // return the LCS
    }

    private static String reverseString(String string) {
        StringBuilder stringBuilder = new StringBuilder(string);
        return stringBuilder.reverse().toString();
    }

//    /*
//     * Reversing the strings when invoked through algorithm B
//     */
//    public String reverse_String(String in) {
//        String output = "";
//
//        for(int i=in.length()-1; i>=0; i--) {
//            output = output+in.charAt(i);
//        }
//
//        return output;
//    }


    private static int findC(int[] length1, int[] length2, int n) {
        int m = 0;
        int c = 0;

        for(int j=0; j<=n; j++) {
            if(m < (length1[j]+length2[n-j])) {
                m = length1[j]+length2[n-j];
                c = j;
            }
        }
        return c;
    }

}
