package com.umg.utilities.droolsandconfig.pubsub

import com.umg.utilities.droolsandconfig.util.Efficacy
import org.apache.commons.csv.CSVParser

import static org.apache.commons.csv.CSVFormat.DEFAULT

class Destinations {
    def generate(String inFileName, String opFileName) {
        Efficacy efficacy = Efficacy.INSTANCE
        efficacy.deleteFile(opFileName)

        def file = new File(inFileName)
        String line = null
        Map<String, Object> port = new HashMap<String, Object>();
        boolean firstLine = true
        file.withReader { reader ->

            CSVParser csv = new CSVParser(reader, DEFAULT.withHeader())
            for (record in csv.iterator()) {
                try {
                    Map<String, String> map = new HashMap<String, String>();


                    putToMap(map, "BasicAuth", record.BasicAuth)
                    putToMap(map, "OneWay", record.OneWay)
                    putToMap(map, "transport", record.transport)
                    putToMap(map, "url", record.url)
                    putToMap(map, "soap_ver", record.soap_ver)
                    putToMap(map, "transport", record.transport)
                    putToMap(map, "sb_content_type", record.sb_content_type)
                    putToMap(map, "dnode", record.dnode)
                    port.put(efficacy._trim(record.portName), map)

                } catch (Exception e) {
                    println(line + " :: " + e
                    )
                    break;
                }
            }
            efficacy.writeTofile(opFileName, "POST lnk_configs/pubsub/current\n"+ new groovy.json.JsonBuilder(port).toPrettyString())
            println("Json generated at $opFileName")
        }
    }

    def putToMap(Map<String, String> configMap, String key, def element) {
        String _val = Efficacy.INSTANCE._trim(element)
        if (_val)
            configMap.put(key, _val);

        configMap
    }

}