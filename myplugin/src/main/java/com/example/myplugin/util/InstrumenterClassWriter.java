package com.example.myplugin.util;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.net.URLClassLoader;

public class InstrumenterClassWriter extends ClassWriter {

    private ClassLoader loader;

    public InstrumenterClassWriter(ClassReader classReader, int flags, ClassLoader loader) {
        super(classReader, flags);
        this.loader = loader;
    }

    public InstrumenterClassWriter(int flags, URLClassLoader loader) {
        super(flags);
        this.loader = loader;
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        Class<?> c, d;
        try {
            c = Class.forName(type1.replace('/', '.'), false, loader);
            d = Class.forName(type2.replace('/', '.'), false, loader);
        } catch (ClassNotFoundException e) {
            return "java/lang/Object";
        }catch(NullPointerException nep){
        	return "java/lang/Object";
        }
        if (c.isAssignableFrom(d)) {
            return type1;
        }
        if (d.isAssignableFrom(c)) {
            return type2;
        }
        if (c.isInterface() || d.isInterface()) {
            return "java/lang/Object";
        } else {
            do {
                c = c.getSuperclass();
            } while (!c.isAssignableFrom(d));
            return c.getName().replace('.', '/');
        }
    }
}