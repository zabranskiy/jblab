package examples.java;

import examples.java.support.SuperClass;
import examples.java.support.SuperInterface;

public class InnerAndAnonymousClasses {
    public int publicField = 123;
    private int privateField = 123;


    class Inner1 {
        class InnerInner {
            public int rty = zxc;
            public int fgh = privateField + publicField;
        }

        public int zxc = publicField;

    }

    class Inner2 {}

    enum InnerEnum {ONE, TWO, THREE}

    public void methodWithInnerClasses() {
        class MethodInner { class InnerMethodInner {} }

        int c = 123;
        if (c > 5) {
            class NestedInner {}
        }
        class NestedInner { class InnerNestedInner {} }

        Inner1 inner1 = new Inner1();
        Inner1.InnerInner innerInner = inner1.new InnerInner();

        NestedInner nestedInner = new NestedInner();
        NestedInner.InnerNestedInner innerNestedInner = nestedInner.new InnerNestedInner();

        SuperClass.Kolasd qew;

        final int qwe = 123;
        SuperInterface sc = new SuperInterface() {
            @Override
            public void interfaceMethod() {
                int a = qwe;
                methodWithInnerClasses();
                privateMethod();
            }
        };
    }

    private void privateMethod() {}
}
