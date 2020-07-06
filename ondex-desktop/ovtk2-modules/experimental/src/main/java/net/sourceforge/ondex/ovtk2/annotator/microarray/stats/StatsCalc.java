package net.sourceforge.ondex.ovtk2.annotator.microarray.stats;

/**
 * Borrowed code from someware, cleaned up by matt
 * @author hindlem
 *
 */
    public class StatsCalc {
        
        private int count;   // Number of numbers that have been entered.
        private double sum;  // The sum of all the items that have been entered.
        private double squareSum;  // The sum of the squares of all the items.
        private double max = Double.NEGATIVE_INFINITY;  // Largest item seen.
        private double min = Double.POSITIVE_INFINITY;  // Smallest item seen.
     
        /**
         * 
         * @param num add the number to the dataset.
         */
        public void enter(double num) {
           count++;
           sum += num;
           squareSum += num*num;
           if (num > max)
              max = num;
           if (num < min)
              min = num;
        }
     
        /**
         * 
         * @return number of items that have been entered.
         */
        public int getCount() {   
           return count;
        }
     
        /**
         * 
         * @return the sum of all the items that have been entered.
         */
        public double getSum() {
           return sum;
        }
     
        /**
         * Return average of all the items that have been entered.
         * Value is Double.NaN if count == 0.
         * @return average of all the items that have been entered
         */
        public double getMean() {
           return sum / count;  
        }
     
        /**
         * Return standard deviation of all the items that have been entered.
         * Value will be Double.NaN if count == 0.
         * @return standard deviation of all the items that have been entered.
         */
        public double getStandardDeviation() {  

           double mean = getMean();
           return Math.sqrt( squareSum/count - mean*mean );
        }
        
        /**
         * Return the smallest item that has been entered.
         * Value will be infinity if no items have been entered.
         * @return the smallest item that has been entered.
         */
        public double getMin() {
           return min;
        }
        
        /**
         *  Return the largest item that has been entered.
         *  Value will be -infinity if no items have been entered.
         * @return the largest item that has been entered.
         */
        public double getMax() {
           return max;
        }
     
     } 

