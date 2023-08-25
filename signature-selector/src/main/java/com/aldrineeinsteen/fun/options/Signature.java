package com.aldrineeinsteen.fun.options;

public class Signature {
    private final String text;
    private final String type;
    private final double weight;

    public Signature(String text, String type, double weight) {
        this.text = text;
        this.type = type;
        this.weight = weight;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public double getWeight() {
        return weight;
    }
}
