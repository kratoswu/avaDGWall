package com.avadesign.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import android.util.Log;

import com.avadesign.model.bean.FanItem;
import com.avadesign.model.bean.PanelDetailBean;
import com.avadesign.model.bean.SceneItem;
import com.avadesign.model.bean.SwitchItem;
import com.avadesign.model.bean.TemperatureItem;

public class PanelDetailHelper {

    public static PanelDetailBean getDetailBean(Document pnlDetailDoc) {
        Map<String, String> detailProp = parseElement(pnlDetailDoc.getRootElement());
        PanelDetailBean detailBean = new PanelDetailBean();
        detailBean.id = detailProp.get("id");
        detailBean.label = detailProp.get("label");
        detailBean.typeId = detailProp.get("type_id");
        detailBean.switchItemMap = getSwitchItemMap(pnlDetailDoc);
        detailBean.sceneItemMap = getSceneItemMap(pnlDetailDoc);

        List<HashMap<String, String>> tempDetails = getTempDetails(pnlDetailDoc);

        if (tempDetails != null && tempDetails.size() > 0) {
            HashMap<String, String> data = tempDetails.get(0);
            TemperatureItem item = new TemperatureItem();

            item.id = data.get("id");
            item.nodeId = data.get("node_id");
            item.index = data.get("index");
            item.clazz = data.get("class");
            item.instance = data.get("instance");

            detailBean.tempItem = item;
        }

        List<HashMap<String, String>> fanDetails = getFanDetails(pnlDetailDoc);

        if (fanDetails != null && fanDetails.size() > 0) {
            Map<String, String> data = fanDetails.get(0);
            FanItem item = new FanItem();

            item.id = data.get("id");
            item.nodeId = data.get("node_id");
            item.index = data.get("index");
            item.clazz = data.get("class");
            item.instance = data.get("instance");

            detailBean.fanItem = item;
        }

        ArrayList<HashMap<String, String>> allCmptData = new ArrayList<HashMap<String,String>>();
        allCmptData.addAll(getSwitchDetails(pnlDetailDoc));
        allCmptData.addAll(getSceneBtnDetails(pnlDetailDoc));
        allCmptData.addAll(getTempDetails(pnlDetailDoc));
        allCmptData.addAll(getFanDetails(pnlDetailDoc));

        detailBean.allCmptList = allCmptData;

        Map<String, Map<String, String>> allCmptDataMap = new HashMap<String, Map<String,String>>();

        for (Map<String, String> data : allCmptData) {
            allCmptDataMap.put(data.get("type") + data.get("id"), data);
        }

        detailBean.allCmptMap = allCmptDataMap;

        return detailBean;
    }

    public static Map<String, SceneItem> getSceneItemMap(Document doc) {
        Map<String, SceneItem> items = new HashMap<String, SceneItem>();

        for (Map<String, String> data : getSceneBtnDetails(doc)) {
            SceneItem item = new SceneItem();
            item.id = data.get("id");
            item.sceneId = data.get("scene_id");

            items.put(item.id, item);
        }

        return items;
    }

    public static Map<String, SwitchItem> getSwitchItemMap(Document doc) {
        Map<String, SwitchItem> items = new HashMap<String, SwitchItem>();

        for (HashMap<String, String> data : getSwitchDetails(doc)) {
            SwitchItem item = new SwitchItem();
            item.id = data.get("id");
            item.nodeId = data.get("node_id");
            item.index = data.get("index");
            item.label = data.get("label");
            item.clazz = data.get("class");
            item.instance = data.get("instance");

            items.put(item.id, item);
        }

        return items;
    }

    public static ArrayList<HashMap<String, String>> getFanDetails(Document doc) {
        return getDetails(doc, "Panel/FanSwitch");
    }

    public static ArrayList<HashMap<String, String>> getTempDetails(Document doc) {
        return getDetails(doc, "Panel/Temperature");
    }

    public static ArrayList<HashMap<String, String>> getSwitchDetails(Document doc) {
        return getDetails(doc, "Panel/LightingSwitch");
    }

    public static ArrayList<HashMap<String, String>> getDetails(Document doc, String name) {
        ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String,String>>();

        for (Object o : doc.selectNodes(name)) {
            Element ele = (Element) o;

            result.add(parseElement(ele));
        }

        return result;
    }

    public static ArrayList<HashMap<String, String>> getSceneBtnDetails(Document doc) {
        return getDetails(doc, "Panel/SceneButton");
    }

    public static HashMap<String, String> parseElement(Element element) {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("type", element.getName());

        for (Object o : element.attributes()) {
            Attribute attr = (Attribute) o;
            result.put(attr.getName(), attr.getValue());
        }

        return result;
    }

}
