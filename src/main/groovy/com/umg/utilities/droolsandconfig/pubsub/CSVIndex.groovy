/**
 *
 */
package com.umg.utilities.droolsandconfig.pubsub;


/**
 * @author sande
 *
 */
enum CSVIndex {

    include(0), source(1),resourceName(2), pubSubDocument(3), publish_type_SOAP_EM_OHI(4), namespace(5),
    subscriberPortName(6), mapper(7), messageVariant(8), extraFilter(9), addResourcesAndQueryParams(10);

    private final int index;

    private CSVIndex(int value) {
        this.index = value;
    }

    public int getIndex() {
        return index;
    }


}
