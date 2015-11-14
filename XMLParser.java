/**
 * Created by Sarah on 11/14/2015.
 */
package com.cannedfruit.weatherapp;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Sarah on 03/10/2015.
 */

public class XMLParser{
    // We don't use namespaces
    private static final String ns = null;


    protected Day parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readDay(parser);
        } finally {
            in.close();
        }
    }

    protected static class Time {
        protected String date;
        protected String temperature;
        protected String humidity;
        protected String pressure;
        protected String icon;

        protected Time() {
            this.date = null;
            this.temperature = null;
            this.humidity = null;
            this.pressure = null;
            this.icon = null;
        }
        protected Time(String date, String temperature, String humidity, String pressure,  String icon) {
            this.date = date;
            this.temperature = temperature;
            this.humidity = humidity;
            this.pressure = pressure;
            this.icon = icon;
        }
    }
    protected static class Day {
        protected final Time day1;
        protected final Time day2;
        protected final Time day3;
        protected final Time day4;

        protected Day(Time day1, Time day2, Time day3, Time day4) {
            this.day1 = day1;
            this.day2 = day2;
            this.day3 = day3;
            this.day4 = day4;
        }
    }

    // Parses the contents of the current weather from Open Weather Map API.
    private Day readDay(XmlPullParser parser) throws XmlPullParserException, IOException {
        //OpenWeather map returns same day
        //dayNull burns that first day returned
        Time dayNull = new Time();
        Time day1 = new Time();
        Time day2 = new Time();
        Time day3 = new Time();
        Time day4 = new Time();
        String name;
        parser.require(XmlPullParser.START_TAG, ns, "weatherdata");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            eventType = parser.next();
            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }
            name = parser.getName();

            if (name.equals("forecast")) {
                dayNull = readTime(parser);
                day1 = readTime(parser);
                day2 = readTime(parser);
                day3 = readTime(parser);
                day4 = readTime(parser);
            }
        }
        return new Day(day1, day2, day3, day4);
    }

    private Time readTime(XmlPullParser parser) throws XmlPullParserException, IOException{
        String date = null;
        String temperature = null;
        String humidity = null;
        String pressure = null;
        String icon = null;
        String name = "";
        while (parser.next() != XmlPullParser.END_TAG || !name.equals("clouds")) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            name = parser.getName();

            switch (name) {
                case "time":
                    date = parser.getAttributeValue(null, "day");
                    date = date.substring(5, 10);
                    break;
                case "symbol":
                    icon = parser.getAttributeValue(null, "var");
                    break;
                case "temperature":
                    temperature = parser.getAttributeValue(null, "day");
                    double temp = Double.parseDouble(temperature);
                    double tempC = temp - 273.15;
                    double tempF = ((temp * 9.00) / 5.00) - 459.67;
                    temperature = String.format("%.2f", tempC) + "\u2103\n" + String.format("%.2f", tempF) + "\u2109";
                    break;
                case "pressure":
                    pressure = parser.getAttributeValue(null, "value");
                    int length = pressure.length();
                    pressure = pressure.substring(0, (length - 3));
                    pressure += "hPa";
                    break;
                case "humidity":
                    humidity = parser.getAttributeValue(null, "value");
                    humidity += "\u0025";
                    break;
            }
        }
        return new Time(date, temperature, humidity, pressure, icon);
    }
}
