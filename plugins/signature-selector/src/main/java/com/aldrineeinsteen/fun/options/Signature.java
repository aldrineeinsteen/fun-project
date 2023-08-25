package com.aldrineeinsteen.fun.options;

public class Signature {
    private final String text;
    private final double weight;

    public Signature(String text, String type, double weight) {
        this.text = text;
        this.weight = weight;
    }

    public String getText() {
        return text;
    }

    public double getWeight() {
        return weight;
    }
}
