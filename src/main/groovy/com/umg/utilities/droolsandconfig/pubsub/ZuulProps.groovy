package com.umg.utilities.droolsandconfig.pubsub

import org.apache.commons.csv.CSVParser

import static org.apache.commons.csv.CSVFormat.DEFAULT

/**
 * Created by sande on 24/04/2017.
 */
class ZuulProps {

    public void generate(String csv){
        def file = new File(csv)
        Map emasis = [:]
        Set pubWrapped = [] as Set
        Set raw = [] as Set
        file.withReader { reader ->
            CSVParser csvParser = new CSVParser(reader, DEFAULT.withHeader())
            for (record in csvParser.iterator()) {
                if(asLowerCase(record.include) == 'y') {
                    String messageVariant = asLowerCase(record.messageVariant)
                    if (messageVariant == 'emasis') {
                        Set resNames = emasis.get(asLowerCase(record.source), [] as Set)
                        resNames.add(asLowerCase(record.resourceName))
                        emasis.put(asLowerCase(record.source), resNames)
                    } else if (messageVariant == 'raw') {
                        raw.add(asLowerCase(record.resourceName))
                    } else if (messageVariant == 'pubsubwrapped') {
                        pubWrapped.add(asLowerCase(record.resourceName))
                    }
                }
            }

        }

        StringBuilder strb = new StringBuilder()
        emasis.each {k,v ->
            strb.append(k).append(":").append(v.join(",")).append(";")
        }
        println("| em.aswell | "+strb.toString()+"|")

        println("| raw.payload.aswell | "+(raw.intersect(pubWrapped)).join(",")+"|")
    }

    private  String asLowerCase(String val)
    {
        val?.toLowerCase()
    }

}
