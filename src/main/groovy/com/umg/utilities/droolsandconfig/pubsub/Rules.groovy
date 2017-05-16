/**
 *
 */
package com.umg.utilities.droolsandconfig.pubsub

import com.umg.utilities.droolsandconfig.util.Efficacy
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

import static java.nio.file.StandardOpenOption.CREATE
import static org.apache.commons.csv.CSVFormat.DEFAULT;

/**
 * @author sande
 *
 */
class Rules {

    private String AND = "";
    private Set<String> resourceNmaes = new HashSet<String>();
    private Set<String> portNames = new HashSet<String>();

    /**
     * @param args
     * @throws FileNotFoundException
     */
    public void generate(String csv, String drl) throws FileNotFoundException {


        System.out.println("init drools generation from CSV");
        Rules rules = new Rules();
        Efficacy.INSTANCE.deleteFile(drl);

        rules.imports(drl);

        Map<String, Set<Map<CSVIndex, String>>> ruleMap = rules.getRuleMap(csv);
        ruleMap.each { key, props ->
            Efficacy.INSTANCE.writeTofile(drl, rules.generateRule(key, props))
        };

        System.out.println("Drrols generation completed :: " + drl);
    }

    private void imports(String drl) {
        StringBuilder builder = new StringBuilder();
        builder.append("package com.umgi.es.pubsub.drools;");
        builder.append("\n\n");

        builder.append("import com.umgi.es.pubsub.drools.EnterpriseMessageRoute;");
        builder.append("\n");
        builder.append("import com.umgi.es.pubsub.drools.EnterpriseMessageRouteProps;");
        builder.append("\n");
        builder.append("import java.util.List;");
        builder.append("\n\n");

        builder.append("global org.mule.module.bpm.MessageService mule;");
        builder.append("\n\n");

        try {
            Files.write(Paths.get(drl), builder.toString().getBytes(), CREATE);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private String generateRule(String key, Set<Map<CSVIndex, String>> props) {
        StringBuilder builder = new StringBuilder();
        AtomicInteger atomicInteger = new AtomicInteger(0);
        props.each { prop ->

            this.resourceNmaes.add(getValueFromCSVIndex(prop, CSVIndex.resourceName));
            this.portNames.add('"' + getValueFromCSVIndex(prop, CSVIndex.subscriberPortName) + '"');
            AND = "";
            if (atomicInteger.getAndIncrement() == 0) {
                builder.append("rule \"" + key + '"');
                builder.append("\n");
                builder.append("dialect \"mvel\"");
                builder.append("\n");
                builder.append('when $enterpriseMessageRoute : EnterpriseMessageRoute(');
                appendAndClause(builder, "messageSource", getValueFromCSVIndex(prop, CSVIndex.source));
                appendAndClause(builder, "resourceName", getValueFromCSVIndex(prop, CSVIndex.resourceName));
                appendAndClause(builder, "NameSpace", getValueFromCSVIndex(prop, CSVIndex.namespace));
                appendAndClause(builder, "DocumentName", getValueFromCSVIndex(prop, CSVIndex.pubSubDocument));
                appendAndClause(builder, "msgVariant", getValueFromCSVIndex(prop, CSVIndex.messageVariant));
                if (!getValueFromCSVIndex(prop, CSVIndex.extraFilter).equals("")) {
                    builder.append(AND);
                    String filter = getValueFromCSVIndex(prop, CSVIndex.extraFilter);
                    // eg. change "p.R2Country2 == ""JP""" to p.R2Country2 == "JP"
                    //filter = filter.substring(1,filter.length()-1).replaceAll("\'"', '"');
                    builder.append("(" + filter + ")");
                }
                builder.append(")");
                builder.append("\n");
                builder.append("then");
                builder.append("\n");
            }
            builder.append('$enterpriseMessageRoute.addRoute(');
            builder.append('"' + getValueFromCSVIndex(prop, CSVIndex.subscriberPortName) + '"');
            builder.append(",\"" + getValueFromCSVIndex(prop, CSVIndex.mapper) + "\",\"");
            if (getValueFromCSVIndex(prop, CSVIndex.addResourcesAndQueryParams).equalsIgnoreCase("Y")) builder.append("true");
            builder.append('");');
            builder.append("\n");
        };
        builder.append("end");
        builder.append("\n\n");


        return builder.toString();
    }

    private String getValueFromCSVIndex(Map<CSVIndex, String> csvRowMap, CSVIndex csvIndex) {
        String value = csvRowMap.get(csvIndex);
        return "N/A".equalsIgnoreCase(value) ? "" : value;
    }

    private void appendAndClause(StringBuilder builder, String field, String value) {

        if (!value.equals("")) {
            builder.append(AND);
            builder.append("p." + field + " == ");
            builder.append('"');
            builder.append(value);
            builder.append('"');
            AND = " && ";
        }
    }


    public Integer getIndex(String env) {

        return CSVIndex.include.getIndex();

    }

    private Map<String, Set<Map<CSVIndex, String>>> getRuleMap(String _csv) throws FileNotFoundException {
        Map<String, Set<Map<CSVIndex, String>>> ruleMap = new LinkedHashMap<String, Set<Map<CSVIndex, String>>>();
        Map<String, String> filterNameMapper = new HashMap<String, String>()
        int uniqueIndex = 0;
        CSVRecord _csvRecord = null;

        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(_csv), StandardCharsets.UTF_8);
            CSVParser csv = new CSVParser(reader, DEFAULT.withHeader());
            Iterator<CSVRecord> iter = csv.iterator();
            while (iter.hasNext()) {

                CSVRecord csvRecord = iter.next();
                _csvRecord = csvRecord;

                String includeRow = getStringVal(csvRecord, CSVIndex.include.getIndex()).toLowerCase();

                if (includeRow.equalsIgnoreCase("Y") || includeRow.equalsIgnoreCase("S")) {

                    String sourceSystem = getStringVal(csvRecord, CSVIndex.source.getIndex()).toLowerCase();
                    String resourceName = getStringVal(csvRecord, CSVIndex.resourceName.getIndex()).toLowerCase();
                    String psDoc = getStringVal(csvRecord, CSVIndex.pubSubDocument.getIndex());
                    String psNs = getStringVal(csvRecord, CSVIndex.namespace.getIndex());
                    String msgVariant = getStringVal(csvRecord, CSVIndex.messageVariant.getIndex());
                    String filters = getStringVal(csvRecord, CSVIndex.extraFilter.getIndex(), false);
                    String subscriberPortName = getStringVal(csvRecord, CSVIndex.subscriberPortName.getIndex()).trim();
                    String mapper = getStringVal(csvRecord, CSVIndex.mapper.getIndex());
                    String queryParams = getStringVal(csvRecord, CSVIndex.addResourcesAndQueryParams.getIndex()).trim();

                    String key = (sourceSystem.equals("") ? "AllSources" : sourceSystem) +
                            "." + (resourceName.equals("") ? "AllResources" : resourceName) +
                            "." + (psNs.equals("") ? "AllNamespaces" : psNs) +
                            "." + (msgVariant.equals("") ? "AllVariants" : msgVariant);

                    if (!filters.equals(""))
                       key = filterNameMapper.get(encode(key+filters), key += " (filtered " + uniqueIndex++ + ")")
                     
                   
                        //key += " (filtered " + uniqueIndex++ + ")";

                    Set<Map<CSVIndex, String>> subProps = ruleMap.get(key) == null ? new LinkedHashSet<Map<CSVIndex, String>>() : ruleMap.get(key);
                    Map<CSVIndex, String> propsMap = new LinkedHashMap<CSVIndex, String>();
                    propsMap.put(CSVIndex.source, sourceSystem);
                    propsMap.put(CSVIndex.resourceName, resourceName);
                    propsMap.put(CSVIndex.pubSubDocument, psDoc);
                    propsMap.put(CSVIndex.namespace, psNs);
                    propsMap.put(CSVIndex.messageVariant, msgVariant);
                    propsMap.put(CSVIndex.subscriberPortName, subscriberPortName);
                    propsMap.put(CSVIndex.mapper, mapper);
                    propsMap.put(CSVIndex.extraFilter, filters);
                    propsMap.put(CSVIndex.addResourcesAndQueryParams, queryParams);

                    subProps.add(propsMap);
                    ruleMap.put(key, subProps);
                }
            }
            reader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.err.println("error with line number " + _csvRecord.getRecordNumber() + " :: and data is :: " + _csvRecord.toString() + " :: no of columns = " + _csvRecord.size());
        }
        return Collections.unmodifiableMap(ruleMap);
    }

    private String getStringVal(CSVRecord csvRecord, int index) {
        return getStringVal(csvRecord, index, true);
    }

    private String getStringVal(CSVRecord csvRecord, int index, boolean toLower) {
        String value = csvRecord.get(index).trim();
        value = value != null && (!"n/a".equalsIgnoreCase(value)) ? value.trim() : "";
        return (toLower ? value.toLowerCase() : value).replaceAll("[^a-zA-Z0-9.&= !|\":/_()]", "");
    }

    private String encode(String toEncode)
    {
        toEncode.bytes.encodeBase64().toString()
    }
}
