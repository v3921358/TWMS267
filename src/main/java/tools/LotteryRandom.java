package tools;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class LotteryRandom {
    private final List<Pair<Object, Integer>> datas = new LinkedList<>();
    private int sumProbability = 0;

    public void addData(Object obj, int value) {
        datas.add(new Pair<>(obj, value));
        sumProbability += value;
    }

    public Object random() {
        if (datas.isEmpty()) {
            return null;
        }
        List<Double> sortAwardProbabilityList = new LinkedList<>();
        int tempSumProbability = 0;
        for (Pair<Object, Integer> award : datas) {
            tempSumProbability += award.getRight();
            sortAwardProbabilityList.add((double) (tempSumProbability) / sumProbability);
        }

        double randomDouble = Math.random();
        sortAwardProbabilityList.add(randomDouble);
        Collections.sort(sortAwardProbabilityList);
        int lotteryIndex = sortAwardProbabilityList.indexOf(randomDouble);
        if (lotteryIndex < 0 || lotteryIndex >= datas.size()) {
            return null;
        }
        return datas.get(lotteryIndex).getLeft();
    }

    public int size() {
        return datas == null ? 0 : datas.size();
    }
}
