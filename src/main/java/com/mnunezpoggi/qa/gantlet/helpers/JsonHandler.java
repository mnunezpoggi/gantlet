package com.mnunezpoggi.qa.gantlet.helpers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class meant to be used or extended by any other class that will
 * require JSON string handling
 *
 * 
 */
public class JsonHandler {

    protected JsonParser parser = new JsonParser();

    /**
     * Function that takes a JSON object as string and parses it to a Map with
     * its corresponding fields as keys, and values as strings
     *
     * @param json String to parse
     * @return Map of keys and its values in String format
     */
    public Map<String, String> jsonToMap(String json) {
        HashMap<String, String> map = new HashMap();
        JsonObject obj = parser.parse(json).getAsJsonObject();        
        for (Map.Entry entry : obj.entrySet()) {
            
            map.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return map;
    }
    
    public Map<String, String> removeQuotes(Map<String, String> map){
        Map m = map;
        for(String s: map.values()){
            m.replace("\"", "");
        }
        return m;
    }
    
    public List<String> removeQuotes(List<String> list){
        ArrayList l = new ArrayList();
        for(String s: list){
            l.add(s.replace("\"", ""));
        }
        return l;
    }

    /**
     * Function that grabs a JSON object as string and returns the value of a
     * field of it
     *
     * @param json String to parse
     * @param attrib Attribute/Field/Key
     * @return The value of the Attribute/Field/Key, otherwise nulll
     */
    public String getJsonAttribute(String json, String attrib) {
        return jsonToMap(json).get(attrib).replace("\"", "");
    }
    /**
     * Wrapper function that returns if it's a json object or json array
     * @param json String to parse
     * @return true if it is json object or json array
     */
    public boolean isJson(String json){
        JsonElement je = parser.parse(json);
        return je.isJsonObject() || je.isJsonArray();
    }

    /**
     * Function that grabs a JSON array as string and maps it to a list
     *
     * @param json String to parse
     * @return The parsed list
     */
    public List<String> jsonToList(String json) {
        if(json == null) return null;
        JsonElement je = parser.parse(json);
        if (!je.isJsonArray()) {
            return null;
        }
        ArrayList<String> list = new ArrayList();
        JsonArray ja = je.getAsJsonArray();
        for (int i = 0; i < ja.size(); i++) {
            list.add(ja.get(i).toString());
        }
        return list;
    }
    
    /**
     * Extracted from http://tinybrain.de:8080/jsonminify/<br>
     * Function that returns a minified version of a JSON
     * @param jsonString The string to minify
     * @return A minified string
     */ 
    public String minify(String jsonString) {
        boolean in_string = false;
        boolean in_multiline_comment = false;
        boolean in_singleline_comment = false;
        char string_opener = 'x';

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < jsonString.length(); i++) {
            char c = jsonString.charAt(i);
            String cc = jsonString.substring(i, Math.min(i + 2, jsonString.length()));

            if (in_string) {
                if (c == string_opener) {
                    in_string = false;
                    out.append(c);
                } else if (c == '\\') {
                    out.append(cc);
                    ++i;
                } else {
                    out.append(c);
                }
            } else if (in_singleline_comment) {
                if (c == '\r' || c == '\n') {
                    in_singleline_comment = false;
                }
            } else if (in_multiline_comment) {
                if (cc.equals("*/")) {
                    in_multiline_comment = false;
                    ++i;
                }
            } else if (cc.equals("/*")) {
                in_multiline_comment = true;
                ++i;
            } else if (cc.equals("//")) {
                in_singleline_comment = true;
                ++i;
            } else if (c == '"' || c == '\'') {
                in_string = true;
                string_opener = c;
                out.append(c);
            } else if (!Character.isWhitespace(c)) {
                out.append(c);
            }
        }
        return out.toString();
    }
}
