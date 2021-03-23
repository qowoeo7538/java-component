package org.lucas.component.common.compiler.support;

import java.util.concurrent.atomic.AtomicInteger;

class JavaCodeTest {

    final static AtomicInteger SUBFIX = new AtomicInteger(8);

    String getSimpleCode() {
        StringBuilder code = new StringBuilder();
        code.append("package org.lucas.component.common.compiler.support;");

        code.append("class HelloServiceImpl" + SUBFIX.getAndIncrement() + " implements HelloService {");
        code.append("   String sayHello() { ");
        code.append("       return \"Hello world!\"; ");
        code.append("   }");
        code.append("}");
        return code.toString();
    }

    String getSimpleCodeWithoutPackage(){
        StringBuilder code = new StringBuilder();
        code.append("class HelloServiceImpl" + SUBFIX.getAndIncrement() + "implements org.lucas.component.common.compiler.support.HelloService.HelloService {");
        code.append("   String sayHello() { ");
        code.append("       return \"Hello world!\"; ");
        code.append("   }");
        code.append("}");
        return code.toString();
    }

    String getSimpleCodeWithSyntax(){
        StringBuilder code = new StringBuilder();
        code.append("package org.lucas.component.common.compiler.support;");

        code.append("class HelloServiceImpl" + SUBFIX.getAndIncrement() + " implements HelloService {");
        code.append("   String sayHello() { ");
        code.append("       return \"Hello world!\"; ");
        return code.toString();
    }

    // only used for javassist
    String getSimpleCodeWithSyntax0(){
        StringBuilder code = new StringBuilder();
        code.append("package org.lucas.component.common.compiler.support;");

        code.append("class HelloServiceImpl_0 implements HelloService {");
        code.append("   String sayHello() { ");
        code.append("       return \"Hello world!\"; ");
        return code.toString();
    }

    String getSimpleCodeWithImports() {
        StringBuilder code = new StringBuilder();
        code.append("package org.lucas.component.common.compiler.support;");

        code.append("import java.lang.*;\n");
        code.append("import org.lucas.component.common.compiler.support;\n");

        code.append("class HelloServiceImpl2" + SUBFIX.getAndIncrement() + " implements HelloService {");
        code.append("   String sayHello() { ");
        code.append("       return \"Hello world!\"; ");
        code.append("   }");
        code.append("}");
        return code.toString();
    }

    String getSimpleCodeWithWithExtends() {
        StringBuilder code = new StringBuilder();
        code.append("package org.lucas.component.common.compiler.support;");

        code.append("import java.lang.*;\n");
        code.append("import org.lucas.component.common.compiler.support;\n");

        code.append("class HelloServiceImpl" + SUBFIX.getAndIncrement() + " extends org.lucas.component.common.compiler.support.HelloServiceImpl0 {\n");
        code.append("   String sayHello() { ");
        code.append("       return \"Hello world3!\"; ");
        code.append("   }");
        code.append("}");
        return code.toString();
    }
}
