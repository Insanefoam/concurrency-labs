package classes;

public class E implements D {
    @Override
    public void interfaceDMethod() {
        System.out.println("Implemented interface D method");
    }

    public void invokeInterfaceMethod() {
        interfaceDMethod();
    }
}
