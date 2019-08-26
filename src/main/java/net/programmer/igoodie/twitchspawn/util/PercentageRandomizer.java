package net.programmer.igoodie.twitchspawn.util;

import net.programmer.igoodie.twitchspawn.tslanguage.parser.TSLParser;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PercentageRandomizer<T> {

    private static final int MAX_PERCENTAGE = 100_00;

    private class RandomNode implements Comparable<RandomNode> {
        int percentage; // 100_00 == 100.00%
        T data;

        @Override
        public int compareTo(RandomNode other) {
            if (this.percentage == other.percentage) return 0;
            return other.percentage > this.percentage ? 1 : -1;
        }

        @Override
        public String toString() {
            return (percentage / 100f) + "%-" + data.toString();
        }
    }

    /* -------------------------------- */

    private Random random;
    private int totalPercentage;
    private List<RandomNode> elements;

    public PercentageRandomizer() {
        this.random = new Random();
        this.elements = new LinkedList<>();
        this.totalPercentage = 0;
    }

    public int getTotalPercentage() {
        return totalPercentage;
    }

    public void forEachElement(Consumer<T> consumer) {
        elements.forEach(element -> consumer.accept(element.data));
    }

    public int size() {
        return elements.size();
    }

    public List<T> elements() {
        return elements.stream()
                .map(element -> element.data)
                .collect(Collectors.toList());
    }

    public void addElement(T data, String percentageString) {
        addElement(data, TSLParser.parsePercentage(percentageString));
    }

    public void addElement(T data, float percentage) {
        addElement(data, (int) (percentage * 100));
    }

    public void addElement(T data, int percentage) {
        RandomNode node = new RandomNode();
        node.data = data;
        node.percentage = percentage;

        // Assert that the percentage stays between [0,100]%
        if (totalPercentage + node.percentage > MAX_PERCENTAGE)
            throw new IllegalStateException("Cannot add element, which goes over 100% -> "
                    + totalPercentage + "% + " + node.percentage + "%");

        totalPercentage += node.percentage;

        elements.add(node);

        Collections.sort(elements);
    }

    public T randomItem() {
        int chance = random.nextInt(MAX_PERCENTAGE);

        for (RandomNode element : elements) {
            if (chance < element.percentage)
                return element.data;
            chance -= element.percentage;
        }

        return elements.get(elements.size() - 1).data;
    }

    @Override
    public String toString() {
        return elements.toString();
    }

}
