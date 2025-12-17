package com.firstapp.uber.google;

import java.util.List;

public class DistanceMatrixResponse {
    public List<Row> rows;
    public List<String> origin_addresses;
    public List<String> destination_addresses;
    public String status;

    public static class Row {
        public List<Element> elements;
    }

    public static class Element {
        public TextValue distance;
        public TextValue duration;
        public String status;
    }

    public static class TextValue {
        public String text;
        public long value;
    }
}
