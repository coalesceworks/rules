package com.umg.utilities.droolsandconfig

import com.umg.utilities.droolsandconfig.eser.ESERConfig
import com.umg.utilities.droolsandconfig.pubsub.Destinations
import com.umg.utilities.droolsandconfig.pubsub.DestsCSV
import com.umg.utilities.droolsandconfig.pubsub.DroolsCSV
import com.umg.utilities.droolsandconfig.pubsub.Rules
import com.umg.utilities.droolsandconfig.pubsub.ZuulProps
import com.umg.utilities.droolsandconfig.util.Efficacy

import java.nio.file.FileSystem

class BootDroolsAndConf
{
    public static void main(String[] args) {
        String envs = System.getProperty("env");
        String loc = System.getProperty("loc");
        String esLoc = System.getProperty("esLoc");
        if( !(envs && loc && esLoc))
        {
            println("usage :: java -Denv=<env> -Dloc=<flat-drrols location> -DesLoc=<est-elasticsearch location> -jar <<droolsandconfig-xx-all.jar>>")
        }
        else
        {
            envs.split(",").each { env ->
                println("Generating configs for :: "+env +" :: from :: "+loc)
                String fs = File.separator
                String envPath = loc+fs+env+fs
                String ba =  loc+fs+"pubsub"+fs+"ba"+fs
                String csv =  envPath+"csv"+fs
                String esConfigs = esLoc+fs+"src"+fs+"main"+fs+"resources"+fs+"config"+fs+env+fs

                new DroolsCSV().generate(env, ba+"flat-Drools.csv", csv+"flat-Drools.csv")
                new DestsCSV().generate(env, ba+"destinations.csv", csv+"destinations.csv")
                new Rules().generate(csv+"flat-Drools.csv", envPath+"pubsub-subscribers.drl")
                new Destinations().generate(csv+"destinations.csv", esConfigs+"pubsub.json")
                new ESERConfig().generate(csv+"flat-Drools.csv", esConfigs+"eser.json")
                new ZuulProps().generate(csv+"flat-Drools.csv")
                Efficacy.INSTANCE.deleteFile(env+"_derive-endpoint.drl")
                Efficacy.INSTANCE.writeTofile(env+"_derive-endpoint.drl", new com.umg.utilities.droolsandconfig.apigw.Rules().generatePubSubRule(csv+"flat-Drools.csv", env))

                println("Config generated for "+loc)
            }

        }

    }
}