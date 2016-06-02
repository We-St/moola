package org.moola.eclipse.highlighting;

import java.util.Arrays;
import java.util.List;

import org.codehaus.groovy.eclipse.editor.highlighting.IHighlightingExtender;
import org.eclipse.jface.text.rules.IRule;

public class MoolaKeywordHighlighter implements IHighlightingExtender {

    public List<String> getAdditionalGJDKKeywords() {
        return null;
    }
    
    public List<String> getAdditionalGroovyKeywords() {
        return Arrays.asList(
        	"modeltypes", "model", "operation", "expects", "returns",
        	"before", "after", "task", "run", "parallel", "await"
        );
    }

    public List<IRule> getAdditionalRules() {
        return null;
    }

}