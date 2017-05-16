package com.umg.utilities.droolsandconfig.pubsub

import com.umg.utilities.droolsandconfig.util.Efficacy
import org.apache.commons.csv.CSVParser

import java.nio.file.Paths

import static org.apache.commons.csv.CSVFormat.DEFAULT

class DestsCSV {

    def generate(String env, String ipFileName, String opFileName) {
        // portName,transport,OneWay,soap_ver,sb_content_type,dnode,isInternal,isPingable,canHandleTestMessage,qa2Include,uatInclude,prodInclude,qa2Url,qa2BasicAuth,qa2SSL,uatUrl,uatBasicAuth,prodUrl,prodBasicAuth,comments,contactNames,contactDetails,end
        String header = "portName,url,transport,BasicAuth,OneWay,soap_ver,sb_content_type,dnode,end" + '\n'

        String _basicAuth = env + "BasicAuth"
        String _url = env + "Url"
        //env = "prod"
        String _include = env + "Include"

        Efficacy.INSTANCE.deleteFile(opFileName)
        Efficacy.INSTANCE.writeTofile(opFileName, header)

        Paths.get(ipFileName).withReader { reader ->
            CSVParser csv = new CSVParser(reader, DEFAULT.withHeader())

            for (record in csv.iterator()) {
                if ('y'.equalsIgnoreCase(Efficacy.INSTANCE._trim(record."$_include"))) {
                    String data = [Efficacy.INSTANCE._trim(record.portName), Efficacy.INSTANCE._trim(record."$_url"),
                                   Efficacy.INSTANCE._trim(record.transport), Efficacy.INSTANCE._trim(record."$_basicAuth"),
                                   Efficacy.INSTANCE._trim(record.OneWay), Efficacy.INSTANCE._trim(record.soap_ver),
                                   Efficacy.INSTANCE._trim(record.sb_content_type), Efficacy.INSTANCE._trim(record.dnode),
                                   Efficacy.INSTANCE._trim(record.end)].join(',')
                    Efficacy.INSTANCE.writeTofile(opFileName, data + '\n')
                } /*else {
                    println("skipping line number " + record.getRecordNumber())
                }*/
            }
        }
        println("Generated CSV for $env at $opFileName")
    }


}