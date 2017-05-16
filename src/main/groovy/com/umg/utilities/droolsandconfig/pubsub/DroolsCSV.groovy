package com.umg.utilities.droolsandconfig.pubsub

import com.umg.utilities.droolsandconfig.util.Efficacy
import org.apache.commons.csv.CSVParser

import java.nio.file.Paths

import static org.apache.commons.csv.CSVFormat.DEFAULT

class DroolsCSV {

    def generate(String env, String ipFileName, String opFileName) {
        //include,RuleComment,publisher,source,resourceName,pubSubDocument,publish_type_SOAP_EM_OHI,namespace,subscriber,subscriberPortName,mapper,Routed_to_subscriber_via_biztalk_OR_mule,messageVariant,extraFilter,addResourcesAndQueryParams,uatEndpoint,Comments,operationalGrouping,end
        String header = "include,source,resourceName,pubSubDocument,publish_type_SOAP_EM_OHI,namespace,subscriberPortName,mapper,messageVariant,extraFilter,addResourcesAndQueryParams,storeToESER,end" + '\n'
        Efficacy efficacy = Efficacy.INSTANCE


        efficacy.deleteFile(opFileName)
        efficacy.writeTofile(opFileName, header)

        Paths.get(ipFileName).withReader { reader ->
            CSVParser csv = new CSVParser(reader, DEFAULT.withHeader())

            for (record in csv.iterator()) {
                String data = [efficacy.toSingleLine(record."$env"), efficacy.toSingleLine(record.source),
                               efficacy.toSingleLine(record.resourceName), efficacy.toSingleLine(record.pubSubDocument),
                               efficacy.toSingleLine(record.publish_type_SOAP_EM_OHI), efficacy.toSingleLine(record.namespace),
                               efficacy.toSingleLine(record.subscriberPortName), efficacy.toSingleLine(record.mapper),
                               efficacy.toSingleLine(record.messageVariant), efficacy.toSingleLine(record.extraFilter),
                               efficacy.toSingleLine(record.addResourcesAndQueryParams), efficacy.toSingleLine(record.storeToESER),
                               efficacy.toSingleLine(record.end)].join(',')
                efficacy.writeTofile(opFileName, efficacy.toSingleLine(data) + '\n')
            }
        }
        System.out.println("CSV generation complete :: " + opFileName);
    }


}