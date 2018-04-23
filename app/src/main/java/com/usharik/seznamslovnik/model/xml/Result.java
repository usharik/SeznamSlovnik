package com.usharik.seznamslovnik.model.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "result")
public class Result {

    @Root(name = "item")
    public static class Item {

        @Attribute(name = "value")
        public String value;

        @Attribute(name = "relevance", required = false)
        public Integer relevance;

        @Attribute(name = "highlightStart", required = false)
        public Integer highlightStart;

        @Attribute(name = "highlightEnd", required = false)
        public Integer highlightEnd;
    }

    @Attribute(name = "source", required = false)
    public String source;

    @ElementList(name = "suggest",  entry = "item", type = Item.class)
    public List<Item> suggest;
}
