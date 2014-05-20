/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.raspberry.demo.server.sensor;

/**
 *
 * @author mitthoma
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;

public class TempSensor {
// This directory created by 1-wire kernel modules
static String w1DirPath = "/sys/bus/w1/devices";
private String sensorPath = "";

public static void main(String[] argv) {
TempSensor sensor = new TempSensor();
String temp = sensor.readTemperature();
System.out.println("temperature=" + temp);

}

public TempSensor() {
inititateSensor();
}

public void inititateSensor() {
File dir = new File(w1DirPath);
File[] files = dir.listFiles(new DirectoryFileFilter());
if (files != null) {

for (File file : files) {
System.out.print(file.getName() + ": ");
// Device data in w1_slave file
sensorPath = w1DirPath + "/" + file.getName() + "/w1_slave";
}
}
}

public String readTemperature() {
String strTemp = "hi";
if (sensorPath != null) {
File f = new File(sensorPath);
try (BufferedReader br = new BufferedReader(new FileReader(f))) {
String output;
while ((output = br.readLine()) != null) {
int idx = output.indexOf("t=");
if (idx > -1) {
// Temp data (multiplied by 1000) in 5 chars after t=
float tempC = Float.parseFloat(output.substring(output
.indexOf("t=") + 2));
tempC /= 1000;
strTemp = String.format("%.3f ", tempC);
}
}
} catch (Exception ex) {
System.out.println(ex.getMessage());
}
}
return strTemp;
}
}

// This FileFilter selects subdirs with name beginning with 28-
// Kernel module gives each 1-wire temp sensor name starting with 28-
class DirectoryFileFilter implements FileFilter {
public boolean accept(File file) {
String dirName = file.getName();
String startOfName = dirName.substring(0, 3);
return (file.isDirectory() && startOfName.equals("28-"));
}
}