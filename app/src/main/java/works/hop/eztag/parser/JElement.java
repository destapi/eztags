package works.hop.eztag.parser;

import org.mvel2.MVEL;
import org.mvel2.templates.TemplateRuntime;
import works.hop.eztag.clone.JCopy;
import works.hop.eztag.pubsub.JSubscribe;

import javax.xml.stream.XMLStreamException;
import java.util.*;
import java.util.stream.Collectors;

public class JElement extends JObject implements Cloneable{

    public static final List<String> selfClosingTags = List.of("area", "base", "br", "col", "embed", "hr", "img", "input", "link", "meta", "param", "source", "track", "wbr");
    public static final List<String> decoratorTags = List.of("doctype", "meta", "link", "style", "script");
    public static final List<String> booleanAttributes = List.of("checked", "disabled", "required", "open", "defer");
    protected String tagName;
    protected String slotName;
    protected String slotRef;
    protected String fragmentRef;
    protected Object context;
    protected String ifExpression;
    protected String showExpression;
    protected String listExpression;
    protected String listItemsKey;
    protected String textExpression;
    protected String textContent;
    protected String evalContent;
    protected String includePath;
    protected String templatePath;
    protected String docTypeTag;
    protected JElement templateElement;
    protected boolean isComponent;
    protected boolean isTextNode;
    protected boolean isEvalNode;
    protected boolean isLayoutSlot;
    protected boolean isSlotNode;
    protected boolean isFragmentNode;
    protected boolean isLayoutNode;
    protected boolean isDecoratorNode;
    protected JElement docTypeElement;
    protected List<String> interests = new LinkedList<>();
    protected Map<String, String> attributes = new LinkedHashMap<>();
    protected Map<String, JElement> slots = new LinkedHashMap<>();
    protected Map<String, List<JElement>> decorators = new HashMap<>() {
        {
            put("script", new LinkedList<>());
            put("link", new LinkedList<>());
            put("meta", new LinkedList<>());
        }
    };
    protected Map<String, JElement> fragments = new HashMap<>();
    protected List<JElement> children = new LinkedList<>();

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getSlotName() {
        return slotName;
    }

    public void setSlotName(String slotName) {
        this.slotName = slotName;
    }

    public String getSlotRef() {
        return slotRef;
    }

    public void setSlotRef(String slotRef) {
        this.slotRef = slotRef;
    }

    public String getFragmentRef() {
        return fragmentRef;
    }

    public void setFragmentRef(String fragmentRef) {
        this.fragmentRef = fragmentRef;
    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    public String getIfExpression() {
        return ifExpression;
    }

    public void setIfExpression(String ifExpression) {
        this.ifExpression = ifExpression;
    }

    public String getShowExpression() {
        return showExpression;
    }

    public void setShowExpression(String showExpression) {
        this.showExpression = showExpression;
    }

    public String getListExpression() {
        return listExpression;
    }

    public void setListExpression(String listExpression) {
        this.listExpression = listExpression;
    }

    public String getListItemsKey() {
        return listItemsKey;
    }

    public void setListItemsKey(String listItemsKey) {
        this.listItemsKey = listItemsKey;
    }

    public String getTextExpression() {
        return textExpression;
    }

    public void setTextExpression(String textExpression) {
        this.textExpression = textExpression;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getEvalContent() {
        return evalContent;
    }

    public void setEvalContent(String evalContent) {
        this.evalContent = evalContent;
    }

    public String getDocTypeTag() {
        return docTypeTag;
    }

    public void setDocTypeTag(String docTypeTag) {
        this.docTypeTag = docTypeTag;
    }

    public boolean isComponent() {
        return isComponent;
    }

    public void setComponent(boolean component) {
        isComponent = component;
    }

    public boolean isTextNode() {
        return isTextNode;
    }

    public void setTextNode(boolean textNode) {
        isTextNode = textNode;
    }

    public boolean isEvalNode() {
        return isEvalNode;
    }

    public void setEvalNode(boolean evalNode) {
        isEvalNode = evalNode;
    }

    public String getIncludePath() {
        return includePath;
    }

    public void setIncludePath(String includePath) {
        this.includePath = includePath;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public JElement getTemplateElement() {
        return templateElement;
    }

    public void setTemplateElement(JElement templateElement) {
        this.templateElement = templateElement;
    }

    public boolean isLayoutSlot() {
        return isLayoutSlot;
    }

    public void setLayoutSlot(boolean layoutSlot) {
        isLayoutSlot = layoutSlot;
    }

    public boolean isSlotNode() {
        return isSlotNode;
    }

    public void setSlotNode(boolean slotNode) {
        isSlotNode = slotNode;
    }

    public boolean isFragmentNode() {
        return isFragmentNode;
    }

    public void setFragmentNode(boolean fragmentNode) {
        isFragmentNode = fragmentNode;
    }

    public boolean isLayoutNode() {
        return isLayoutNode;
    }

    public void setLayoutNode(boolean layoutNode) {
        isLayoutNode = layoutNode;
    }

    public boolean isDecoratorNode() {
        return isDecoratorNode;
    }

    public void setDecoratorNode(boolean decoratorNode) {
        isDecoratorNode = decoratorNode;
    }

    public JElement getDocTypeElement() {
        return docTypeElement;
    }

    public void setDocTypeElement(JElement docTypeElement) {
        this.docTypeElement = docTypeElement;
    }

    public String render() {
        StringBuilder builder = new StringBuilder();
        if (isComponent) {
            if (listExpression == null) {
                builder.append(renderComponent());
            } else {
                builder.append(renderListComponent());
            }
        } else {
            renderNonComponent(builder);
        }
        return builder.toString();
    }

    public void renderLayout(StringBuilder builder) {
        try {
            JContext templateProcessor = new JContext(context);
            JParser templateParser = new JParser(templatePath, templateProcessor);
            JElement templateRoot = templateParser.parse();
            setTemplateElement(templateRoot);

            for (JElement child : children) {
                child.setContext(context);
                //delay rendering until when the slot in the template is reached
                if (child.isSlotNode && templateRoot.slots.containsKey(child.slotName)) {
                    templateRoot.slots.put(child.slotName, child);
                    continue;
                }

                String tagName = child.tagName.replaceFirst("x-", "");
                if (decoratorTags.contains(tagName)) {
                    if (tagName.equals("doctype")) {
                        templateRoot.setDocTypeElement(child);
                    } else {
                        child.setDecoratorNode(true);
                        templateRoot.decorators.get(tagName).add(child);
                    }
                    continue;
                }

                if ("include".equals(tagName)) {
                    JContext includeProcessor = new JContext(context);
                    JParser includeParser = new JParser(child.includePath, includeProcessor);
                    JElement includeRoot = includeParser.parse();
                    includeRoot.slots.put(child.slotName, includeRoot);
                }
            }

            if (((JElement) root()).docTypeTag != null) {
                builder.append(((JElement) root()).docTypeTag);
            }

            builder.append(templateProcessor.process(templateRoot));
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    public void renderNonComponent(StringBuilder builder) {
        if (isTextNode()) {
            builder.append(textContent);
        } else {
            builder.append("<").append(tagName);
            renderTagAttributes(builder);
            builder.append(">");

            for (JElement child : children) {
                child.setContext(context);
                builder.append(child.render());
            }

            if (selfClosingTags.contains(tagName.toLowerCase())) {
                builder.deleteCharAt(builder.length() - 1).append("/>");
            } else {
                builder.append("</").append(tagName).append(">");
            }
        }
    }

    public void renderTag(StringBuilder builder) {
        builder.append("<").append(tagName.replaceFirst("x-", ""));
        renderTagAttributes(builder);
        builder.append(">");

        if (textExpression != null) {
            builder.append(MVEL.eval(textExpression, context));
        } else if (evalContent != null) {
            builder.append(TemplateRuntime.eval(evalContent, context));
        } else {
            for (JElement child : children) {
                child.setContext(context);
                builder.append(child.render());
            }
        }

        if (selfClosingTags.contains(tagName.toLowerCase().replaceFirst("x-", ""))) {
            builder.deleteCharAt(builder.length() - 1).append("/>");
        } else {
            builder.append("</").append(tagName.replaceFirst("x-", "")).append(">");
        }
    }

    public String renderComponent() {
        if (ifExpression == null || (Boolean) MVEL.eval(ifExpression, context)) {
            if (!interests.isEmpty()) {
                root().observer().subscribe(this.interests.stream().map(i -> new JSubscribe(new String[]{i}, this)).toList());
            }

            StringBuilder builder = new StringBuilder();
            if (tagName.equals("x-layout")) {
                renderLayout(builder);
            } else if (includePath != null) {
                renderIncluded(builder);
            } else if (isLayoutSlot) {
                setLayoutSlot(false);
                builder.append(((JElement) root()).slots.get(getSlotRef()).render());
            } else if (decoratorTags.contains(tagName.replaceFirst("x-", "")) && !isDecoratorNode()) {
                renderDecorator(builder);
            } else {
                renderTag(builder);
            }
            return builder.toString();
        }
        return "";
    }

    public String renderListComponent() {
        if (ifExpression == null || (Boolean) MVEL.eval(ifExpression, context)) {
            if (!interests.isEmpty()) {
                root().observer().subscribe(this.interests.stream().map(i -> new JSubscribe(new String[]{i}, this)).toList());
            }

            StringBuilder builder = new StringBuilder();
            if (isTextNode()) {
                builder.append(textContent);
            } else {
                builder.append("<").append(tagName.replaceFirst("x-", ""));
                renderTagAttributes(builder);
                builder.append(">");

                Collection<Object> collection = (Collection<Object>) MVEL.eval(listExpression, context);
                if (children.size() != 1) {
                    throw new RuntimeException("for a loop parent, there should be ONLY one child");
                }
                JElement child = children.get(0);
                for (Object item : collection) {
                    JElement element = child.clone();
                    element.setContext(item);
                    element.attributes.put("data-x-id", MVEL.eval(listItemsKey, item).toString());
                    builder.append(element.render());
                }

                if (selfClosingTags.contains(tagName.toLowerCase())) {
                    builder.deleteCharAt(builder.length() - 1).append("/>");
                } else {
                    builder.append("</").append(tagName.replaceFirst("x-", "")).append(">");
                }
            }
            return builder.toString();
        }
        return "";
    }

    private void renderTagAttributes(StringBuilder builder) {
        for (String attr : attributes.keySet()) {
            String attrValue = attributes.get(attr);
            if (attrValue.contains("@")) {
                attrValue = TemplateRuntime.eval(attrValue, context).toString();
            }
            if (attr.equals("data-x-show")) {
                attrValue = MVEL.eval(attrValue, context).toString();
                builder.append(" ").append(attr).append("=").append("\"").append(attrValue).append("\"");
                continue;
            }
            if (booleanAttributes.contains(attr)) {
                if (attrValue.equals("true")) {
                    builder.append(" ").append(attr);
                }
                continue;
            }
            builder.append(" ").append(attr).append("=").append("\"").append(attrValue).append("\"");
        }
    }

    public void renderDecorator(StringBuilder builder) {
        switch (tagName) {
            case "x-meta" -> {
                for (JElement meta : ((JElement) root()).decorators.get("meta")) {
                    builder.append(meta.renderComponent());
                }
            }
            case "x-link" -> {
                for (JElement link : ((JElement) root()).decorators.get("link")) {
                    builder.append(link.render());
                }
            }
            case "x-script" -> {
                for (JElement script : ((JElement) root()).decorators.get("script")) {
                    builder.append(script.render());
                }
            }
            default -> throw new RuntimeException("Unsupported decorator tag - '" + tagName + "'");
        }
    }

    public void renderIncluded(StringBuilder builder) {
        try {
            JContext includeProcessor = new JContext(context);
            JParser parser = new JParser(includePath, includeProcessor);
            JElement includeRoot = parser.parse();
            builder.append(includeProcessor.process(includeRoot));
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JElement clone() {
        JElement clone = (JElement) super.clone();
        clone.templateElement = this.templateElement != null? this.templateElement.clone() : null;
        clone.docTypeElement = this.docTypeElement != null? this.docTypeElement.clone() : null;
        clone.attributes = new HashMap<>();
        clone.attributes.putAll(this.attributes);
        clone.interests = new LinkedList<>();
        clone.interests.addAll(this.interests);
        clone.children = new LinkedList<>();
        this.children.forEach(element -> {
            JElement cloneChild = element.clone();
            clone.children.add(cloneChild);
        });
        clone.slots = new HashMap<>();
        this.slots.forEach((k, v) -> {
            clone.slots.put(k, v.clone());
        });
        clone.fragments = new HashMap<>();
        this.fragments.forEach((k, v) -> {
            clone.fragments.put(k, v.clone());
        });
        clone.decorators = new HashMap<>();
        this.decorators.forEach((k, list) -> {
            clone.decorators.put(k, list.stream().map(JElement::clone).collect(Collectors.toList()));
        });
        return clone;
    }
}
