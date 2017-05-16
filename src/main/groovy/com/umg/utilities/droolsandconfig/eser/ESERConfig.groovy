package com.umg.utilities.droolsandconfig.eser

import com.umg.utilities.droolsandconfig.util.Efficacy
import org.apache.commons.csv.CSVParser

import static org.apache.commons.csv.CSVFormat.DEFAULT

class ESERConfig {

    def generate(String inFileName, String opFileName) {
        Efficacy efficacy = Efficacy.INSTANCE
        efficacy.deleteFile(opFileName)

        def file = new File(inFileName)
        String line = null
        Map<String, Object> source = new HashMap<String, Object>();
        boolean firstLine = true
        file.withReader {
            reader ->
                CSVParser csv = new CSVParser(reader, DEFAULT.withHeader())
                for (record in csv.iterator()) {
                    try {
                        Map storageConfig = source.get(efficacy._trim(record.source)?.toLowerCase()) ?: [:] as Map

                        source.put(efficacy._trim(record.source)?.toLowerCase(), storageConfig << getStorageConfig(record))
                    }
                    catch (Exception e) {
                        println(line + " :: " + e)
                        break;
                    }
                }
                efficacy.writeTofile(opFileName, "POST lnk_configs/eser/current\n"+
                        new groovy.json.JsonBuilder(source).toPrettyString())
                println("Json generated at $opFileName")
        }
    }

    def getStorageConfig(def record) {
        [(Efficacy.INSTANCE._trim(record.resourceName)?.toLowerCase()):
                 ['storeToESER': (Efficacy.INSTANCE._trim(record.storeToESER) ==~ "(?i)y"),
                  //'toMulePubSub' : (_trim(record.include) ==~ "(?i)y")
                 ]
        ] as Map
    }

    def putToMap(Map<String, String> configMap, String key, def element) {
        String _val = Efficacy.INSTANCE._trim(element)
        if (_val)
            configMap.put(key, _val);

        configMap
    }

}