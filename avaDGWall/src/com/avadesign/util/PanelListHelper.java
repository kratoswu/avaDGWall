package com.avadesign.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import com.avadesign.model.bean.PanelItemBean;

public class PanelListHelper {
    public static List<PanelItemBean> parseListXml(Document doc) {
        List<PanelItemBean> result = new ArrayList<PanelItemBean>();

        for (Object o : doc.selectNodes("Panel_List/Panel")) {
            Element ele = (Element) o;
            result.add(parseElement(ele));
        }

        return result;
    }

    public static PanelItemBean parseElement(Element ele) {
        PanelItemBean b = new PanelItemBean();
        HashMap<String, String> tempData = new HashMap<String, String>();

        for (Object o : ele.attributes()) {
            Attribute attr = (Attribute) o;
            tempData.put(attr.getName(), attr.getValue());
        }

        b.id = tempData.get("id");
        b.label = tempData.get("label");
        b.typeId = tempData.get("typeId");

        return b;
    }
}
