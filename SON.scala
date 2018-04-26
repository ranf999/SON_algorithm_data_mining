//import org.apache.spark.SparkContext
////import org.apache.spark.SparkContext._
////import org.apache.spark.SparkConf
////import org.apache.spark.rdd.RDD
////
////import scala.collection.mutable
////import scala.collection.mutable.ListBuffer
////import scala.collection.mutable.{Set => mutSet}
////import scala.collection.mutable.HashMap
////import scala.util.Random
////
////import java.io._
/////**
////  * Created by ranf999 on 10/6/17.
////  */
////object SON {
////    def main(args : Array[String]) {
////        val ratings = "file://" + args(1)
////        val users = "file://" + args(2)
////        val minSup = args(3).toInt
////        val task_case = args(0).toInt
////
//////        val conf = new SparkConf().setAppName("Ran_Feng_Task1").setMaster("local")//.set("spark.default.parallelism", "8")
////      val conf = new SparkConf().setAppName("SON")
////      val sc = new SparkContext(conf)
////      val ratingsTextFile = sc.textFile(ratings)
////      val usersTextFile = sc.textFile(users)
////
////      val numPartitions = ratingsTextFile.getNumPartitions
////
////      var baskets:RDD[List[Int]] = null
////
////      if(task_case == 1) {
////         var usersDataTmp = usersTextFile
////          .filter(line => line.contains("::M::"))
////          .map(line => line.split("::"))
////          .map((rateArray: Array[String]) => (rateArray(0).toInt, 1))
////
////         var basketsTmp = ratingsTextFile
////          .map(line => line.split("::"))
////          .map((rates: Array[String]) => (rates(0).toInt,List(rates(1).toInt)))
////          .join(usersDataTmp)
////          .reduceByKey((x:(List[Int], Int),y:(List[Int], Int)) => (x._1 ++: y._1,1))
////          .map{ case (a,(b,c)) => b }
////
////        baskets = basketsTmp
////      } else if(task_case == 2) {
////          var usersDataTmp = usersTextFile
////          .filter(line => line.contains("::F::"))
////          .map(line => line.split("::"))
////          .map((rateArray: Array[String]) => (rateArray(0).toInt, 1))
////
////         var basketsTmp = ratingsTextFile
////          .map(line => line.split("::"))
////          .map((rates: Array[String]) => (rates(0).toInt,rates(1).toInt))
////          .join(usersDataTmp)
////          .map { case (a,(b,c)) => (b, (List(a),1))}
////          .reduceByKey((x:(List[Int], Int),y:(List[Int], Int)) => (x._1 ++: y._1,1))
////          .map{ case (a,(b,c)) => b }
////
////         baskets = basketsTmp
////      }
////
////        var first_map = baskets.mapPartitions( lists =>
////            runApriori(lists, minSup / numPartitions)
////          ).sortByKey()
////          .map { case (a, b) => b }
////          .flatMap( a => {
////            for ( set <- a ) yield ( set, (set, 1) )
////          })
////
////        var first_reduce = first_map
////            .reduceByKey((a:(Set[Int], Int), b:(Set[Int], Int)) => (a._1,1))
////            .map { case (a,b) => b }
////
////        var candidates = first_reduce.map(b => b._1).collect().toList
////        var broadcasting_cnt = sc.broadcast(candidates)
////
////        var second_map = baskets.flatMap(
////          basket => {
////            var counts = ListBuffer.empty[Tuple2[Set[Int], Int]]
////            for (cnt <- broadcasting_cnt.value) {
////              if (cnt.subsetOf(basket.toSet)) {
////                counts += Tuple2(cnt, 1)
////              }
////            }
////            counts.toList
////          })
////
////        var second_reduce = second_map.reduceByKey((x,y) => x+y)
////
////        var final_freq = second_reduce
////          .filter(x => (x._2 >= minSup))
////          .map { case (a,b) => (a.size, List(a.toList.sorted)) }
////          .reduceByKey( (x:List[List[Int]],y:List[List[Int]]) => x ++: y )
////          .coalesce(1)
////          .map { case (a, b) => (a, sort(b)) }
////          .sortBy(_._1)
////          .map { case (a, b) => {
////            var tmp = ListBuffer.empty[String]
////            for (list <- b) {
////              tmp += list.mkString("(",",",")")
////            }
////            tmp.toList
////          } }
////
////        val pw = new PrintWriter(new File("SON.case" + args(0).toString + "_"+ args(3) +".txt" ))
////        //baskets.saveAsTextFile("file://" + args(2))
////        val result = final_freq.map( list => list.mkString(",") ).collect()
////        for (line <- result) {
////          pw.write(line + "\n")
////        }
////        pw.close
////    }
////
////    def sort[A](coll: Seq[Iterable[A]])(implicit ordering: Ordering[A]) = coll.sorted
////
////    def getCombination(freqItemsets: List[Set[Int]], k: Int): List[Set[Int]] = {
////      var singles = mutSet.empty[Int]
////      for (itemsets <- freqItemsets) {
////        for (item <- itemsets) {
////          singles += item
////        }
////      }
////      var combSet = mutSet.empty[Set[Int]]
////      for (itemset <- freqItemsets) {
////        for (singleItem <- singles) {
////          var newItemset = mutSet() ++ itemset
////          newItemset += singleItem
////          if (newItemset.size == k) {
////            //check k-tuples is valid?
////            var checkList = newItemset.subsets(k - 1).toList
////            var valid = true
////            if(k > 2) {
////              for (checkSet <- checkList) {
////                if (!freqItemsets.contains(checkSet)) {
////                  valid = false
////                }
////              }
////            }
////
////            if(valid) {
////              combSet += newItemset.toSet
////            }
////          }
////        }
////      }
////      combSet.toList
////    }
////
////    def getFrequent(candidates: List[Set[Int]], baskets: List[List[Int]], minSupport: Double): List[Set[Int]] = {
////      var basketsSet = ListBuffer.empty[Set[Int]]
////      for (basket <- baskets) {
////        basketsSet += basket.toSet
////      }
////      var itemsetMap = HashMap.empty[Set[Int], Int]
////      for (itemset <- candidates) {
////        for (basket <- basketsSet) {
////          if (itemset.subsetOf(basket)) {
////            if (itemsetMap.contains(itemset)) {
////              itemsetMap(itemset) += 1
////            } else {
////              itemsetMap += (itemset -> 1)
////            }
////          }
////        }
////      }
////      var itemsets = ListBuffer.empty[Set[Int]]
////      for (itemset <- itemsetMap) {
////        if (itemset._2 >= minSupport) {
////          itemsets += itemset._1
////        }
////      }
////      itemsets.toList
////    }
////
////  def runApriori(iter: Iterator[List[Int]], minSupport: Int): Iterator[(Int, List[Set[Int]])] = {
////    var singleMap = HashMap.empty[Int, Int]
////    var baskets = ListBuffer.empty[List[Int]]
////    var freqSetsListBuffer = ListBuffer.empty[Set[Int]]
////    var freqSetsList = List.empty[Set[Int]]
////    var results = HashMap.empty[Int, List[Set[Int]]]
////
////    //singles
////    while (iter.hasNext) {
////      val basket = iter.next()
////      baskets += basket
////      for (single <- basket) {
////        if (singleMap.contains(single)) {
////          singleMap(single) += 1
////        } else {
////          singleMap += (single -> 1)
////        }
////      }
////    }
////
////    for (singlePair <- singleMap) {
////      if(singlePair._2 >= minSupport) {
////        freqSetsListBuffer += Set(singlePair._1)
////      }
////    }
////
////    var k = 2
////
////    if(!results.contains(k-1)) {
////      results += (k-1 -> freqSetsListBuffer.toList)
////    }
////
////    freqSetsList = freqSetsListBuffer.toList
////    while (!freqSetsList.isEmpty) {
////      freqSetsList = getCombination(freqSetsList, k)
////      var newFreqSetsList = getFrequent(freqSetsList, baskets.toList, minSupport)
////      if (!newFreqSetsList.isEmpty) {
////        results += (k -> newFreqSetsList)
////      }
////      freqSetsList = newFreqSetsList
////      k = k + 1
////    }
////    results.iterator
////  }
////}
