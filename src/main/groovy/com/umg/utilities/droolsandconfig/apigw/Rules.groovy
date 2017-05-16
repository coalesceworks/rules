package com.umg.utilities.droolsandconfig.apigw

import org.apache.commons.csv.CSVParser

import java.nio.file.Paths

import static org.apache.commons.csv.CSVFormat.DEFAULT

class Rules {

    String generatePubSubRule(String fileName, String env) {

        Map rules = [:]
        Map _uat_excludeFromPsbRules = ["umg.cprs": ["billofmaterialsv6", "physicalreleaseschedule", "territorialrights"]*.toLowerCase() as Set,
                                        "umg.r2"  : ["release", "talentname"]*.toLowerCase() as Set] as Map

        Map _prod_excludeFromPsbRules = ["umg.cprs"  : ["billofmaterialsv6", "physicalreleaseschedule", "territorialrights"]*.toLowerCase() as Set,
                                         "umg.r2"    : ["release", "talentname"]*.toLowerCase() as Set,
                                         "umg.dsched": ["releaseschedule"]*.toLowerCase() as Set] as Map

        int linenum = 2
        //String env = 'prod'
        //String env = 'uat'
        String qName = env == 'prod' ? '"pubsub.Receive"' : (env == 'qa2' ? '"inbound.pubsub.demo.Receive"' : '"inbound.pubsub.Receive"')

        Map excludeFromPsbRules = (env == 'prod' ? _prod_excludeFromPsbRules : _uat_excludeFromPsbRules)

        //excludeFromPsbRules = [:] as Map

        Paths.get(fileName).withReader {
            reader ->
                CSVParser csv = new CSVParser(reader, DEFAULT.withHeader())

                for (record in csv.iterator()) {
                    //println "$record.source $record.resourceName"
                    String source = (record.source)?.toLowerCase()
                    String resourceName = (record.resourceName)?.toLowerCase()
                    if (!((excludeFromPsbRules?.get(source)?.contains(resourceName)) || !'y'.equalsIgnoreCase(record.include)))
                    {
                        Set resources = rules.get(source, [] as Set)
                        resources << resourceName
                        rules.put(source, resources)
                    }
                    /*else {
                        println "$linenum excluding source - $source and resourceName - $resourceName from pubsub rules"
                    }*/
                    linenum++
                }
        }

        StringBuilder s = new StringBuilder()
        s.append('rule "APIGATEWAY OUTBOUND TO PUB SUB JMS QUEUE"').append('\n')
        s.append('dialect "mvel"').append('\n')
        s.append('when').append('\n')
        s.append(
                '$enterpriseMessageRoute : EnterpriseMessageRoute(logicalTarget == null && (target == "Mule-PubSub" || (')

        boolean orClause = false;
        rules.each
                { k, v ->
                    if (orClause)
                        s.append(' || ')
                    s.append('(source == "').append(k).append('" && (resourceName=="').append(v.join('" || resourceName=="')).append('"))')
                    orClause = true
                }
        s.append(')))').append('\n')
        s.append('then').append('\n')
        s.append('EnterpriseMessageRouteProps $enterpriseMessageRouteProps = new EnterpriseMessageRouteProps();').append('\n')
        s.append('$enterpriseMessageRouteProps.setExchangePattern(MessageExchangePattern.ONE_WAY);').append('\n')
        s.append('$enterpriseMessageRouteProps.setEndpointType("JMS");').append('\n')
        s.append('$enterpriseMessageRouteProps.setMessageFormat("SIFT-XML");').append('\n')
        s.append('$enterpriseMessageRouteProps.setAddress(').append(qName).append(');').append('\n')
        s.append('$enterpriseMessageRouteProps.setJmsxGroupId($enterpriseMessageRoute.getMessageId());').append('\n')
        s.append('List $enterpriseMessageRoutePropsList = new ArrayList();').append('\n')
        s.append('$enterpriseMessageRoutePropsList.add($enterpriseMessageRouteProps);').append('\n')
        s.append('$enterpriseMessageRoute.setEnterpriseMessageRoutePropsList($enterpriseMessageRoutePropsList);').append('\n')
        s.append('end').append('\n')
        //println s.toString()
        s.toString()
    }
}