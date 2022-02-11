/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 ~ 2010 Patrick Huy <patrick.huy@frz.cc> 
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License version 3
as published by the Free Software Foundation. You may not use, modify
or distribute this program under any other version of the
GNU Affero General Public License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package provider;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MapleData implements MapleDataEntity, Iterable<MapleData> {


    private static final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private Node node;
    private File imageDataDir;

    private MapleData(final Node node) {
        this.node = node;
    }

    public MapleData(final FileInputStream fis, final File imageDataDir) {
        try {
            this.node = documentBuilderFactory.newDocumentBuilder().parse(fis).getFirstChild();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            //throw new RuntimeException(e);
            System.err.println(e);
        }
        this.imageDataDir = imageDataDir;
    }
    public MapleData getChildByPath(final String path) {
        final String segments[] = path.split("/");
        if (segments[0].equals("..")) {
            return ((MapleData) getParent()).getChildByPath(path.substring(path.indexOf("/") + 1));
        }

        Node myNode = node;
        for (String segment : segments) {
            NodeList childNodes = myNode.getChildNodes();
            boolean foundChild = false;
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node childNode = childNodes.item(i);
                if (childNode != null && childNode.getAttributes() != null && childNode.getAttributes().getNamedItem("name") != null && childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getAttributes().getNamedItem("name").getNodeValue().equals(segment)) {
                    myNode = childNode;
                    foundChild = true;
                    break;
                }
            }
            if (!foundChild) {
                return null;
            }
        }
        final MapleData ret = new MapleData(myNode);
        ret.imageDataDir = new File(imageDataDir, getName() + "/" + path).getParentFile();
        return ret;
    }

    public List<MapleData> getChildren() {
        final List<MapleData> ret = new ArrayList<>();
        final NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node childNode = childNodes.item(i);
            if (childNode != null && childNode.getNodeType() == Node.ELEMENT_NODE) {
                final MapleData child = new MapleData(childNode);
                child.imageDataDir = new File(imageDataDir, getName());
                ret.add(child);
            }
        }
        return ret;
    }

    public Object getData() {
        final NamedNodeMap attributes = node.getAttributes();
        final MapleDataType type = getType();
        switch (type) {
            case DOUBLE -> {
                return Double.parseDouble(attributes.getNamedItem("value").getNodeValue());
            }
            case FLOAT -> {
                return Float.parseFloat(attributes.getNamedItem("value").getNodeValue());
            }
            case INT -> {
                return Integer.parseInt(attributes.getNamedItem("value").getNodeValue());
            }
            case SHORT -> {
                return Short.parseShort(attributes.getNamedItem("value").getNodeValue());
            }
            case STRING, UOL -> {
                return attributes.getNamedItem("value").getNodeValue();
            }
            case VECTOR -> {
                return new Point(Integer.parseInt(attributes.getNamedItem("x").getNodeValue()), Integer.parseInt(attributes.getNamedItem("y").getNodeValue()));
            }
            case CANVAS -> {
                return new MapleCanvas(Integer.parseInt(attributes.getNamedItem("width").getNodeValue()), Integer.parseInt(attributes.getNamedItem("height").getNodeValue()), new File(imageDataDir, getName() + ".png"));
            }
        }
        return null;
    }

    public final MapleDataType getType() {
        final String nodeName = node.getNodeName();
        switch (nodeName) {
            case "imgdir" -> {
                return MapleDataType.PROPERTY;
            }
            case "canvas" -> {
                return MapleDataType.CANVAS;
            }
            case "convex" -> {
                return MapleDataType.CONVEX;
            }
            case "sound" -> {
                return MapleDataType.SOUND;
            }
            case "uol" -> {
                return MapleDataType.UOL;
            }
            case "double" -> {
                return MapleDataType.DOUBLE;
            }
            case "float" -> {
                return MapleDataType.FLOAT;
            }
            case "int" -> {
                return MapleDataType.INT;
            }
            case "short" -> {
                return MapleDataType.SHORT;
            }
            case "string" -> {
                return MapleDataType.STRING;
            }
            case "vector" -> {
                return MapleDataType.VECTOR;
            }
            case "null" -> {
                return MapleDataType.IMG_0x00;
            }
            default -> {
            }
        }
        return null;
    }

    @Override
    public MapleDataEntity getParent() {
        final Node parentNode = node.getParentNode();
        if (parentNode.getNodeType() == Node.DOCUMENT_NODE) {
            return null; // can't traverse outside the img file - TODO is this a problem?
        }
        final MapleData parentData = new MapleData(parentNode);
        parentData.imageDataDir = imageDataDir.getParentFile();
        return parentData;
    }

    @Override
    public String getName() {
        return node.getAttributes().getNamedItem("name").getNodeValue();
    }

    public Iterator<MapleData> iterator() {
        return getChildren().iterator();
    }
}