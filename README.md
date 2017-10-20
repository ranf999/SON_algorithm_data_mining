# SON_algorithm_data_mining
Implement SON algorithm to find frequent itemsets.

# Description
## Implementation of SON algorithm
The implementation of SON algorithm includes two parts, the first part is the two passes Map-Reduce functions of SON algorithm, the second part is the Apriori algorithm to compute frequency of itemsets in each paritition.  

I set the number of partitions to 4 because my laptop processor has 4 cores.

Each of the partitions can be processed in parallel using mapPartitions method, and the frequent itemsets from each partition combined to form the candidates.
### 1. pseudo code of SON

```scala  
def SON(baskets, support):
 // first_map emits key-value pairs (F,1), where F is frequent itemset.
 First_Map = baskets.mapPartitions(Apriori(support/numOfPartitions))  
 // first_reduce produces keys(itemsets) which appears one or more times
 First_Reduce = First_Map.reduceByKey(candidateItemsets) 
 // second_map emits key-value pairs (Candidate, Support)
 Second_Map = baskets.mapPartitions(countTheSupportForEachCandidate)
 // second_reduce computes the total support for each candidate itemset
 Second_Reduce = Second_Map.reduceByKey(x,y => x + y)
 Result = Second_Reduce.filter( x => x.count >= support )
```

### 2. pseudo code of Apriori
```scala  
def Apriori(baskets, support):
  k = 1                         //k-tuples
  frequent_sets: List[Set[]]
  results: Map(key = k , value = List[Set[]])
  singleMap: Map(key = singleId, value = count)
  for each basket in baskets:
  	singleMap[singleId] += count in basket 
  filter(singleMap, minSupport)
  results[1] = singleMap.toList

  k = 2
  prevFrequentItemsetList = results[1]
  while (prevFrequentItemsetList.isNotEmpty) {
    candidate_sets = getCombination(prevFrequentItemsetList, k)
//generate k-tuples from the result of (k-1)-tuples
    frequent_sets = getFrequent(candidate_sets, minSupport) 
//count the frequency of candidate itemsets, return itemsets which support >= minSupport
    prevFrequentItemsetList = frequent_sets
	if(frequent_sets.isNotEmpty)
		results[k] = frequent_sets
	k = k + 1
  }

```
## Usage
```
$SPARK_HOME/bin/spark-submit --class Ran_Feng_SON --master local[n]  arg0 arg1 arg2 arg3 arg4   
```  

* arg0 - scala jar path
* arg1 - case number (1/2)
* arg2 - path for ratings.dat
* arg3 - path for users.dat
* arg4 - support value.

Example:  
```
./bin/spark-submit --class Ran_Feng_SON --master local[4] ~/Desktop/Ran_Feng_SON.jar 2 ~/Desktop/ratings.dat ~/Desktop/users.dat 600
```

**caution:**   
1. Paths for files must be absolute path.  
2. Result files will be generated to the same path where you run the above command.
