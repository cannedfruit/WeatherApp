package com.cannedfruit.weatherapp;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Sarah on 03/10/2015.
 */

//instantiate the parser
public class CurrentWeatherXMLParser {
    private static final String ns = null;

    protected Current parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readCurrent(parser);
        } finally {
            in.close();
        }
    }

    public static class Current {
        protected final String city;
        protected final String country;
        protected final String temperature;
        protected final String humidity;
        protected final boolean highHum;
        protected final boolean medHum;
        protected final String pressure;
        protected final boolean highAP;
        protected final String weather;
        protected final String icon;

        protected Current(String city, String country, String temperature, String humidity, boolean highHum, boolean medHum, String pressure, boolean highAP, String weather, String icon) {
            this.city = city;
            this.country = country;
            this.temperature = temperature;
            this.humidity = humidity;
            this.highHum = highHum;
            this.medHum = medHum;
            this.pressure = pressure;
            this.highAP = highAP;
            this.weather = weather;
            this.icon = icon;
        }
    }

    // Parses the contents of the current weather from Open Weather Map API.
    private Current readCurrent(XmlPullParser parser) throws XmlPullParserException, IOException {
        String city = null;
        String country = null;
        String temperature = null;
        String humidity = null;
        boolean highHum = false;
        boolean medHum = false;
        String pressure = null;
        boolean highAP = false;
        String weather = null;
        String icon = null;
        parser.require(XmlPullParser.START_TAG, ns, "current");
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = parser.getName();
            if (eventType == XmlPullParser.START_TAG) {
                switch (name) {
                    case "city":
                        city = parser.getAttributeValue(null, "name");
                        break;
                    case "country":
                        if (parser.next() == XmlPullParser.TEXT) {
                            country = parser.getText();
                        }
                        break;
                    case "temperature":
                        temperature = parser.getAttributeValue(null, "value");
                        double temp = Double.parseDouble(temperature);
                        double tempC = temp - 273.15;
                        double tempF = ((temp * 9.00) / 5.00) - 459.67;
                        temperature = String.format("%.2f", tempC) + "\u2103\n" + String.format("%.2f", tempF) + "\u2109";
                        break;
                    case "humidity":
                        humidity = parser.getAttributeValue(null, "value");
                        double hum = Double.parseDouble(humidity);
                        if (hum > 70) {
                            highHum = true;
                        } else if (hum > 50) {
                            medHum = true;
                        }
                        humidity += "\u0025";
                        break;
                    case "pressure":
                        pressure = parser.getAttributeValue(null, "value");
                        double ap = Double.parseDouble(pressure);
                        if (ap > 1013.25) {
                            highAP = true;
                        }
                        pressure += "hPa";
                        break;
                    case "weather":
                        weather = parser.getAttributeValue(null, "value");
                        icon = parser.getAttributeValue(null, "icon");
                        break;
                }
                eventType = parser.next();
            } else { eventType = parser.next();}

        }
        return new Current(city, country, temperature, humidity, highHum, medHum, pressure, highAP, weather, icon);
    }
}
