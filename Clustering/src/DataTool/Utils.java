package DataTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import NeurTools.TradeInfo;

public class Utils {

	public static LinkedHashMap<Integer, TradeInfo> sortHashMapByValues(Map<Integer, TradeInfo> tradeData) {

		Set<Entry<Integer, TradeInfo>> entries = tradeData.entrySet();
		
		Comparator<Entry<Integer, TradeInfo>> valueComparator = new Comparator<Entry<Integer, TradeInfo>>() {
			@Override
			public int compare(Entry<Integer, TradeInfo> e1, Entry<Integer, TradeInfo> e2) {
				Integer v1 = e1.getValue().getClusterID();
				Integer v2 = e2.getValue().getClusterID();
				return v1.compareTo(v2);
			}
		};

		List<Entry<Integer, TradeInfo>> listOfEntries = new ArrayList<Entry<Integer, TradeInfo>>(entries);
		
		Collections.sort(listOfEntries, valueComparator);
		
		LinkedHashMap<Integer, TradeInfo> sortedByValue = new LinkedHashMap<Integer, TradeInfo>(listOfEntries.size());

		for (Entry<Integer, TradeInfo> entry : listOfEntries) {
			sortedByValue.put(entry.getKey(), entry.getValue());
		}

//		System.out.println("HashMap after sorting entries by values ");
//		Set<Entry<Integer, TradeInfo>> entrySetSortedByValue = sortedByValue.entrySet();
//		for (Entry<Integer, TradeInfo> mapping : entrySetSortedByValue) {
//			System.out.println(mapping.getKey() + " ==> " + mapping.getValue());
//		}

		return sortedByValue;
	}
}
