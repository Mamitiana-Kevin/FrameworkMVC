package framework.utils;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private String view;
    private Map<String, Object> model = new HashMap<>();

    public ModelAndView(String view) {
        this.view = view;
    }

    public ModelAndView(String view, Map<String, Object> model) {
        this.view = view;
        if (model != null) this.model.putAll(model);
    }

    public String getView() { return view; }
    public Map<String, Object> getModel() { return model; }

    public ModelAndView addObject(String key, Object value) {
        model.put(key, value);
        return this;
    }
}