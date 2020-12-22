/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.freeathome.internal;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Helper class to perform xml parsing and iterate over the devices
 *
 * @author Stian Kjoglum - Initial contribution
 *
 */
public class FreeAtHomeBindingInventoryIterator {
    private Logger logger = LoggerFactory.getLogger(FreeAtHomeBindingInventoryIterator.class);

    private DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder dBuilder;
    private Document doc = null;
    private HashMap<String, String> nameMapping = new HashMap<String, String>(); // nameId, Names
    private NodeList deviceNodes;
    private int numberOfDevices = 0;
    private int iter_position = 0;

    public class DeviceDescription {
        public String Serial = "";
        public String DeviceDisplayName = "";
        public String DeviceTypeId = "";
        public String DeviceTypeName = "";

        @Override
        public String toString() {
            return "DeviceDisplayName: " + DeviceDisplayName + "   Serial: " + Serial + "   DeviceTypeId: "
                    + DeviceTypeId + "    DeviceTypeName: " + DeviceTypeName;
        }
    }

    public FreeAtHomeBindingInventoryIterator(String xmlTree) throws RuntimeException {

        try {
            try {
                dBuilder = dbFactory.newDocumentBuilder();

                try {
                    doc = dBuilder.parse(new InputSource(new StringReader(xmlTree)));
                } catch (SAXException e) {
                    // TODO Auto-generated catch block
                    logger.error("SAXException: {}", e.getMessage());
                    throw new RuntimeException();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    logger.error("IOException: {}", e.getMessage());
                    throw new RuntimeException();
                }

            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                logger.error("ParserException: {}", e.getMessage());
                throw new RuntimeException();
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            logger.error("Runtime Exception: {}", e1.getMessage());
            throw new RuntimeException();
        }

        NodeList s = doc.getElementsByTagName("string");
        int n = doc.getElementsByTagName("string").getLength();
        for (int i = 0; i < n; i++) {
            Node nNode = s.item(i);
            String nameId = nNode.getAttributes().getNamedItem("nameId").getTextContent();
            String content = nNode.getTextContent();

            logger.debug("NameID{}", nameId);
            logger.debug("Content{}", content);
            nameMapping.put(nameId, content);
        }

        deviceNodes = doc.getElementsByTagName("device");
        numberOfDevices = doc.getElementsByTagName("device").getLength();
    }

    public DeviceDescription iter_get() {
        DeviceDescription local = new DeviceDescription();

        if (iter_position >= numberOfDevices) {
            return null;
        }

        Node deviceNode = deviceNodes.item(iter_position);

        local.DeviceTypeId = deviceNode.getAttributes().getNamedItem("deviceId").getTextContent();
        local.Serial = deviceNode.getAttributes().getNamedItem("serialNumber").getTextContent();
        local.DeviceTypeName = nameMapping
                .getOrDefault(deviceNode.getAttributes().getNamedItem("nameId").getTextContent(), "n.a.");

        Element deviceElement = (Element) deviceNode;
        logger.debug("DeviceNode {}", deviceNode.toString());

        NodeList attributeList = deviceElement.getElementsByTagName("attribute"); // reads all attributes of device tag
                                                                                  // (also of channels)
        logger.debug("AttributeList {}", attributeList.toString());

        for (int i = 0; i < attributeList.getLength(); i++) {
            Node node = attributeList.item(i); // <attribute name="busVoltage">29.175</attribute>

            logger.debug("Node {}", node.getAttributes().getNamedItem("name").getNodeValue());

            if (node.getAttributes().getNamedItem("name").getNodeValue().equals("displayName")) // found description of
                                                                                                // actor/device
            {
                // TODO proper selection of display name
                if (local.DeviceDisplayName.isEmpty()) {
                    local.DeviceDisplayName = node.getTextContent();
                }
            }
        }

        logger.debug("Read Device {}", local.toString());

        return local;
    }

    public void iter_next() {
        iter_position++;
    }

    public boolean iter_hasnext() {
        if (iter_position >= numberOfDevices) {
            return false;
        }

        return true;
    }

    public void iter_reset() {
        iter_position = 0;
    }
}
