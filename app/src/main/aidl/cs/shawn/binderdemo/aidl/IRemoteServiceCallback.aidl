// ISecondary.aidl
package cs.shawn.binderdemo.aidl;

// Declare any non-default types here with import statements

interface IRemoteServiceCallback {

    void valueChanged(int value);
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}
