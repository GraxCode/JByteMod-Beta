package org.objectweb.asm.optimizer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.objectweb.asm.Type;

/**
 * A MAPPING from names to names, used to rename classes, fields and methods.
 * 
 * @author Eric Bruneton
 */
public class NameMapping {

    public final Properties mapping;

    public final Set<Object> unused;

    public NameMapping(final String file) throws IOException {
        mapping = new Properties();
        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            mapping.load(is);
            unused = new HashSet<Object>(mapping.keySet());
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public String map(final String name) {
        String s = (String) mapping.get(name);
        if (s == null) {
            int p = name.indexOf('.');
            if (p == -1) {
                s = name;
            } else {
                int q = name.indexOf('(');
                if (q == -1) {
                    s = name.substring(p + 1);
                } else {
                    s = name.substring(p + 1, q);
                }
            }
        } else {
            unused.remove(name);
        }
        return s;
    }

    public String fix(final String desc) {
        if (desc.startsWith("(")) {
            Type[] arguments = Type.getArgumentTypes(desc);
            Type result = Type.getReturnType(desc);
            for (int i = 0; i < arguments.length; ++i) {
                arguments[i] = fix(arguments[i]);
            }
            result = fix(result);
            return Type.getMethodDescriptor(result, arguments);
        } else {
            return fix(Type.getType(desc)).getDescriptor();
        }
    }

    private Type fix(final Type t) {
        if (t.getSort() == Type.OBJECT) {
            return Type.getObjectType(map(t.getInternalName()));
        } else if (t.getSort() == Type.ARRAY) {
            String s = fix(t.getElementType()).getDescriptor();
            for (int i = 0; i < t.getDimensions(); ++i) {
                s = '[' + s;
            }
            return Type.getType(s);
        } else {
            return t;
        }
    }
}
