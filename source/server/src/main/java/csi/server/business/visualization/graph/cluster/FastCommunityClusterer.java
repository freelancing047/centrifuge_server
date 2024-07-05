package csi.server.business.visualization.graph.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.uci.ics.jung.graph.Graph;

public class FastCommunityClusterer<V,E> implements GraphClusterer<V,E> {

   protected Graph<V,E> graph;
   protected GraphMatrix<V,E> matrix;

   private double[] qValues;
   private List<int[]> mergeList;

   // result area of clustering
   private Map<V,Integer> communityMap = new HashMap<V,Integer>();
   private Map<Integer,Set<V>> memberSets = new HashMap<Integer,Set<V>>();
   private int nCommunity = 0;
   private int maxCommunity = 0;

   public FastCommunityClusterer() {
   }

   @Override
   public Set<Set<V>> apply(Graph<V,E> graph) {
      matrix = new GraphMatrix<V,E>(graph);

      CommunityStructure internal_Groupings = new CommunityStructure();
      internal_Groupings.run(matrix);

      mergeList = internal_Groupings.getMergeList();
      qValues = internal_Groupings.getQValues();

      reconstruct(getMaxQIndex());

      int communityCount = getCommunityCount();

      Set<Set<V>> communities = new HashSet<Set<V>>();

      for (int i = 0; i < communityCount; i++) {
         communities.add(getCommunity(i));
      }
      return communities;
   }

   public void clear() {
      communityMap.clear();
      memberSets.clear();
      nCommunity = 0;
      maxCommunity = 0;
   }

   protected int getMaxQIndex() {
      double max = Double.MIN_VALUE;
      int index = -1;
      for (int i = 0; i < qValues.length; i++) {
         if (qValues[i] > max) {
            max = qValues[i];
            index = i;
         }
      }

      return index;
   }

   public int getCommunityCount() {
      return nCommunity;
   }

   public Set<V> getCommunity(int i) {
      Set<V> community = memberSets.get(Integer.valueOf(i));
      return community;
   }

   public Set<V> getCommunity(V vertex) {
      Integer communityID = communityMap.get(vertex);
      Set<V> community = memberSets.get(communityID);
      return community;
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   public void reconstruct(int time) {
      clear();

      time = Math.min(time, qValues.length - 1);
      HashMap<Integer,List<V>> merge = new LinkedHashMap<Integer,List<V>>();
      Iterator<int[]> iterator = mergeList.iterator();
      int i = 0;

      while (iterator.hasNext() && (i < time)) {
         int[] edge = iterator.next();
         Integer k1 = Integer.valueOf(edge[0]);
         Integer k2 = Integer.valueOf(edge[1]);
         List<V> one = merge.get(k1);

         if (one == null) {
            one = new ArrayList();
            merge.put(k1, one);

            one.add(matrix.getNode(k1));
         }
         List<V> two = merge.get(k2);

         if (two == null) {
            one.add(matrix.getNode(k2));
         } else {
            one.addAll(two);
            merge.remove(k2);
         }
         i++;

         if (merge.size() > maxCommunity) {
            maxCommunity = merge.size();
         }
      }
      nCommunity = merge.size();

      int id = 0;
      Iterator<Integer> mergedResults = merge.keySet().iterator();

      while (mergedResults.hasNext()) {
         Integer setIndex = Integer.valueOf(id);
         List<V> members = merge.get(mergedResults.next());

         for (V o : members) {
            communityMap.put(o, setIndex);

            Set<V> set = memberSets.get(setIndex);

            if (set == null) {
               set = new HashSet<V>();
               memberSets.put(setIndex, set);
            }
            set.add(o);
         }
         id++;
      }
   }
}
