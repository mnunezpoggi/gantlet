package com.mnunezpoggi.qa.gantlet.helpers;

import com.mnunezpoggi.qa.gantlet.helpers.log.LogHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author mauricio
 * @param <K>
 * @param <V>
 */

/*
    From https://dzone.com/articles/regular-expression-hashmap
 */
public class RegExHashMap<K, V> extends HashMap<K, V> {

    // list of regular expression patterns
    private final ArrayList<Pattern> regExPatterns = new ArrayList();
    // list of regular expression values which match patterns
    private final ArrayList<V> regExValues = new ArrayList();

    private final Logger log = LogHelper.getLogger(this);

    public RegExHashMap(Map<? extends K, ? extends V> m) {
        super();
        m.keySet().stream().forEach((k) -> {
            put(k, m.get(k));
        });
    }

    /**
     * Compile regular expression and add it to the regexp list as key.
     *
     * @return
     */
    @Override
    public V put(K key, V value) {
        regExPatterns.add(Pattern.compile(key.toString()));
        regExValues.add(value);
        return value;
    }

    /**
     * If requested value matches with a regular expression, returns it from
     * regexp lists.
     *
     * @param key
     * @return
     */
    @Override
    public V get(Object key) {
        CharSequence cs = key.toString();
        log.debug("Trying to find: " + cs);
        for (int i = 0; i < regExPatterns.size(); i++) {
            if (regExPatterns.get(i).matcher(cs).matches()) {
                log.debug("Matches");
                return regExValues.get(i);
            }
        }
        return super.get(key);
    }
}
