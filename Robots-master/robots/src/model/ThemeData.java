package model;

import java.awt.Color;

public class ThemeData {

    public static class RobotTheme {
        public String name;
        public String bodyColor;
        public String borderColor;
        public String eyeColor;
        public String eyeBorderColor;
        public int bodyWidth;
        public int bodyHeight;
        public int eyeSize;
        public int eyeOffsetX;

        public Color getBodyColor() {
            return Color.decode(bodyColor);
        }

        public Color getBorderColor() {
            return Color.decode(borderColor);
        }

        public Color getEyeColor() {
            return Color.decode(eyeColor);
        }

        public Color getEyeBorderColor() {
            return Color.decode(eyeBorderColor);
        }
    }

    public static class TargetTheme {
        public String name;
        public String color;
        public int size;
        public String shape;

        public Color getColor() {
            return Color.decode(color);
        }
    }

    public static class BackgroundTheme {
        public String name;
        public String backgroundColor;
        public String pathColor;
        public String draftPathColor;

        public Color getBackgroundColor() {
            return Color.decode(backgroundColor);
        }

        public Color getPathColor() {
            return Color.decode(pathColor);
        }

        public Color getDraftPathColor() {
            return Color.decode(draftPathColor);
        }
    }
}