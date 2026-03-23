package com.tracker.DSA.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Topic::class, Problem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun trackerDao(): TrackerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dsa_tracker_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.trackerDao())
                }
            }
        }

        suspend fun populateDatabase(dao: TrackerDao) {
            val topics = listOf(
                Topic("t1", "Arrays"),
                Topic("t2", "Binary Search"),
                Topic("t3", "Strings"),
                Topic("t4", "Linked Lists"),
                Topic("t5", "Stacks, Queues & Sliding Window"),
                Topic("t6", "Binary Trees"),
                Topic("t7", "Graphs"),
                Topic("t8", "Dynamic Programming")
            )
            dao.insertTopics(topics)

            val problems = listOf(
                Problem("arr1", "t1", "Find the Largest / Second Largest Element", "easy", "https://www.geeksforgeeks.org/problems/second-largest3735/1"),
                Problem("arr2", "t1", "Check if the Array is Sorted", "easy", "https://leetcode.com/problems/check-if-array-is-sorted-and-rotated/"),
                Problem("arr3", "t1", "Remove Duplicates from Sorted Array", "easy", "https://leetcode.com/problems/remove-duplicates-from-sorted-array/"),
                Problem("arr4", "t1", "Left/Right Rotate an Array by K Places", "easy", "https://leetcode.com/problems/rotate-array/"),
                Problem("arr5", "t1", "Move All Zeros to the End", "easy", "https://leetcode.com/problems/move-zeroes/"),
                Problem("arr6", "t1", "Find the Missing Number", "easy", "https://leetcode.com/problems/missing-number/"),
                Problem("arr7", "t1", "Max Consecutive Ones", "easy", "https://leetcode.com/problems/max-consecutive-ones/"),
                Problem("arr8", "t1", "Two Sum Problem", "medium", "https://leetcode.com/problems/two-sum/"),
                Problem("arr9", "t1", "Sort Colors (0s, 1s, and 2s)", "medium", "https://leetcode.com/problems/sort-colors/"),
                Problem("arr10", "t1", "Majority Element (> N/2 times)", "medium", "https://leetcode.com/problems/majority-element/"),
                Problem("arr11", "t1", "Maximum Subarray Sum (Kadane's)", "medium", "https://leetcode.com/problems/maximum-subarray/"),
                Problem("arr12", "t1", "Rearrange Array Elements by Sign", "medium", "https://leetcode.com/problems/rearrange-array-elements-by-sign/"),
                Problem("arr13", "t1", "Next Permutation", "medium", "https://leetcode.com/problems/next-permutation/"),
                Problem("arr14", "t1", "Set Matrix Zeroes", "medium", "https://leetcode.com/problems/set-matrix-zeroes/"),
                Problem("arr15", "t1", "Spiral Traversal of a Matrix", "medium", "https://leetcode.com/problems/spiral-matrix/"),

                Problem("bs1", "t2", "Standard Binary Search", "easy", "https://leetcode.com/problems/binary-search/"),
                Problem("bs2", "t2", "Implement Lower Bound", "easy", "https://www.geeksforgeeks.org/problems/floor-in-a-sorted-array-1587115620/1"),
                Problem("bs3", "t2", "Search Insert Position", "easy", "https://leetcode.com/problems/search-insert-position/"),
                Problem("bs4", "t2", "Find First and Last Occurrence", "medium", "https://leetcode.com/problems/find-first-and-last-position-of-element-in-sorted-array/"),
                Problem("bs5", "t2", "Search in a Rotated Sorted Array", "medium", "https://leetcode.com/problems/search-in-rotated-sorted-array/"),
                Problem("bs6", "t2", "Find Minimum in a Rotated Sorted Array", "medium", "https://leetcode.com/problems/find-minimum-in-rotated-sorted-array/"),
                Problem("bs7", "t2", "Find Peak Element", "medium", "https://leetcode.com/problems/find-peak-element/"),
                Problem("bs8", "t2", "Koko Eating Bananas", "medium", "https://leetcode.com/problems/koko-eating-bananas/"),
                Problem("bs9", "t2", "Capacity to Ship Packages within D Days", "medium", "https://leetcode.com/problems/capacity-to-ship-packages-within-d-days/"),

                Problem("str1", "t3", "Remove Outermost Parentheses", "easy", "https://leetcode.com/problems/remove-outermost-parentheses/"),
                Problem("str2", "t3", "Reverse Words in a String", "easy", "https://leetcode.com/problems/reverse-words-in-a-string/"),
                Problem("str3", "t3", "Largest Odd Number in a String", "easy", "https://leetcode.com/problems/largest-odd-number-in-string/"),
                Problem("str4", "t3", "Longest Common Prefix", "easy", "https://leetcode.com/problems/longest-common-prefix/"),
                Problem("str5", "t3", "Check if Two Strings are Anagrams", "easy", "https://leetcode.com/problems/valid-anagram/"),
                Problem("str6", "t3", "Sort Characters by Frequency", "medium", "https://leetcode.com/problems/sort-characters-by-frequency/"),
                Problem("str7", "t3", "Maximum Nesting Depth of Parentheses", "medium", "https://leetcode.com/problems/maximum-nesting-depth-of-the-parentheses/"),
                Problem("str8", "t3", "Roman to Integer", "medium", "https://leetcode.com/problems/roman-to-integer/"),
                Problem("str9", "t3", "String to Integer (atoi)", "medium", "https://leetcode.com/problems/string-to-integer-atoi/"),

                Problem("ll1", "t4", "Reverse a Linked List", "easy", "https://leetcode.com/problems/reverse-linked-list/"),
                Problem("ll2", "t4", "Find the Middle of a Linked List", "easy", "https://leetcode.com/problems/middle-of-the-linked-list/"),
                Problem("ll3", "t4", "Linked List Cycle", "easy", "https://leetcode.com/problems/linked-list-cycle/"),
                Problem("ll4", "t4", "Merge Two Sorted Linked Lists", "easy", "https://leetcode.com/problems/merge-two-sorted-lists/"),
                Problem("ll5", "t4", "Add Two Numbers", "medium", "https://leetcode.com/problems/add-two-numbers/"),
                Problem("ll6", "t4", "Odd Even Linked List", "medium", "https://leetcode.com/problems/odd-even-linked-list/"),
                Problem("ll7", "t4", "Remove Nth Node from the End of the List", "medium", "https://leetcode.com/problems/remove-nth-node-from-end-of-list/"),
                Problem("ll8", "t4", "Find the Starting Point of the Loop", "medium", "https://leetcode.com/problems/linked-list-cycle-ii/"),
                Problem("ll9", "t4", "Check if a Linked List is a Palindrome", "medium", "https://leetcode.com/problems/palindrome-linked-list/"),
                Problem("ll10", "t4", "Intersection of Two Linked Lists", "medium", "https://leetcode.com/problems/intersection-of-two-linked-lists/"),

                Problem("sq1", "t5", "Implement Stack using Queues", "easy", "https://leetcode.com/problems/implement-stack-using-queues/"),
                Problem("sq2", "t5", "Valid Parentheses", "easy", "https://leetcode.com/problems/valid-parentheses/"),
                Problem("sq3", "t5", "Implement Min Stack", "easy", "https://leetcode.com/problems/min-stack/"),
                Problem("sq4", "t5", "Next Greater Element I", "medium", "https://leetcode.com/problems/next-greater-element-i/"),
                Problem("sq5", "t5", "Online Stock Span", "medium", "https://leetcode.com/problems/online-stock-span/"),
                Problem("sq6", "t5", "Asteroid Collision", "medium", "https://leetcode.com/problems/asteroid-collision/"),
                Problem("sq7", "t5", "Longest Substring Without Repeating Characters", "medium", "https://leetcode.com/problems/longest-substring-without-repeating-characters/"),
                Problem("sq8", "t5", "Max Consecutive Ones III", "medium", "https://leetcode.com/problems/max-consecutive-ones-iii/"),
                Problem("sq9", "t5", "Fruit Into Baskets", "medium", "https://leetcode.com/problems/fruit-into-baskets/"),
                Problem("sq10", "t5", "Longest Repeating Character Replacement", "medium", "https://leetcode.com/problems/longest-repeating-character-replacement/"),

                Problem("bt1", "t6", "Binary Tree Inorder Traversal", "easy", "https://leetcode.com/problems/binary-tree-inorder-traversal/"),
                Problem("bt2", "t6", "Maximum Depth of a Binary Tree", "easy", "https://leetcode.com/problems/maximum-depth-of-binary-tree/"),
                Problem("bt3", "t6", "Balanced Binary Tree", "easy", "https://leetcode.com/problems/balanced-binary-tree/"),
                Problem("bt4", "t6", "Diameter of a Binary Tree", "easy", "https://leetcode.com/problems/diameter-of-binary-tree/"),
                Problem("bt5", "t6", "Binary Tree Level Order Traversal", "medium", "https://leetcode.com/problems/binary-tree-level-order-traversal/"),
                Problem("bt6", "t6", "Binary Tree Zigzag Level Order Traversal", "medium", "https://leetcode.com/problems/binary-tree-zigzag-level-order-traversal/"),
                Problem("bt7", "t6", "Boundary Traversal of Binary Tree", "medium", "https://www.geeksforgeeks.org/problems/boundary-traversal-of-binary-tree/1"),
                Problem("bt8", "t6", "Binary Tree Right Side View", "medium", "https://leetcode.com/problems/binary-tree-right-side-view/"),
                Problem("bt9", "t6", "Lowest Common Ancestor of a Binary Tree", "medium", "https://leetcode.com/problems/lowest-common-ancestor-of-a-binary-tree/"),

                Problem("gr1", "t7", "BFS of graph", "easy", "https://www.geeksforgeeks.org/problems/bfs-traversal-of-graph/1"),
                Problem("gr2", "t7", "DFS of Graph", "easy", "https://www.geeksforgeeks.org/problems/depth-first-traversal-for-a-graph/1"),
                Problem("gr3", "t7", "Number of Provinces", "medium", "https://leetcode.com/problems/number-of-provinces/"),
                Problem("gr4", "t7", "Rotting Oranges", "medium", "https://leetcode.com/problems/rotting-oranges/"),
                Problem("gr5", "t7", "Detect Cycle in an Undirected Graph", "medium", "https://www.geeksforgeeks.org/problems/detect-cycle-in-an-undirected-graph/1"),
                Problem("gr6", "t7", "Is Graph Bipartite?", "medium", "https://leetcode.com/problems/is-graph-bipartite/"),
                Problem("gr7", "t7", "Course Schedule", "medium", "https://leetcode.com/problems/course-schedule/"),

                Problem("dp1", "t8", "Climbing Stairs", "easy", "https://leetcode.com/problems/climbing-stairs/"),
                Problem("dp2", "t8", "Fibonacci Number", "easy", "https://leetcode.com/problems/fibonacci-number/"),
                Problem("dp3", "t8", "Frog Jump", "medium", "https://www.geeksforgeeks.org/problems/geek-jump/1"),
                Problem("dp4", "t8", "House Robber", "medium", "https://leetcode.com/problems/house-robber/"),
                Problem("dp5", "t8", "Unique Paths", "medium", "https://leetcode.com/problems/unique-paths/"),
                Problem("dp6", "t8", "0/1 Knapsack Problem", "medium", "https://www.geeksforgeeks.org/problems/0-1-knapsack-problem0945/1"),
                Problem("dp7", "t8", "Longest Common Subsequence", "medium", "https://leetcode.com/problems/longest-common-subsequence/"),
                Problem("dp8", "t8", "Coin Change", "medium", "https://leetcode.com/problems/coin-change/")
            )
            dao.insertProblems(problems)
        }
    }
}
